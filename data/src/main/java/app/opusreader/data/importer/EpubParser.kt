package app.opusreader.data.importer

import app.opusreader.data.entity.BookEntity
import app.opusreader.data.entity.SpineItemEntity
import app.opusreader.data.entity.TocEntryEntity
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class MalformedEpubException(message: String, cause: Throwable? = null) : Exception(message, cause)

data class ParsedEpub(
    val book: BookEntity,
    val spineItems: List<SpineItemEntity>,
    val tocEntries: List<TocEntryEntity>,
    val coverEntryPath: String?,
    val coverBytes: ByteArray?
)

object EpubParser {

    private val docBuilderFactory = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
    }

    fun parse(inputStream: InputStream, filePath: String): ParsedEpub {
        val zipEntries = mutableMapOf<String, ByteArray>()
        try {
            ZipInputStream(inputStream).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        zipEntries[entry.name] = zis.readBytes()
                    }
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            throw MalformedEpubException("Corrupt EPUB zip archive", e)
        }

        if (zipEntries.isEmpty()) {
            throw MalformedEpubException("EPUB archive is empty or invalid zip")
        }

        // 1. Locate container.xml
        val containerBytes = zipEntries["META-INF/container.xml"]
            ?: throw MalformedEpubException("Missing META-INF/container.xml")

        val opfPath = parseContainerForOpf(containerBytes)
            ?: throw MalformedEpubException("Could not find OPF path in META-INF/container.xml")

        // 2. Locate OPF package document
        val opfBytes = zipEntries[opfPath]
            ?: throw MalformedEpubException("OPF package document not found at $opfPath")

        val opfDir = if (opfPath.contains('/')) opfPath.substringBeforeLast('/') else ""

        // 3. Parse OPF package document
        return parseOpf(
            opfBytes = opfBytes,
            opfDir = opfDir,
            filePath = filePath,
            zipEntries = zipEntries
        )
    }

    private fun parseContainerForOpf(containerBytes: ByteArray): String? {
        val doc = docBuilderFactory.newDocumentBuilder().parse(ByteArrayInputStream(containerBytes))
        val rootfiles = doc.getElementsByTagName("rootfile")
        for (i in 0 until rootfiles.length) {
            val element = rootfiles.item(i) as? Element ?: continue
            val fullPath = element.getAttribute("full-path")
            if (fullPath.isNotBlank()) {
                return fullPath.trim()
            }
        }
        return null
    }

    private data class ManifestItem(
        val id: String,
        val href: String,
        val mediaType: String,
        val properties: String
    )

    private fun parseOpf(
        opfBytes: ByteArray,
        opfDir: String,
        filePath: String,
        zipEntries: Map<String, ByteArray>
    ): ParsedEpub {
        val doc = docBuilderFactory.newDocumentBuilder().parse(ByteArrayInputStream(opfBytes))

        // Title
        val titleNodes = doc.getElementsByTagNameNS("*", "title")
        val title = if (titleNodes.length > 0) {
            titleNodes.item(0).textContent.trim().ifEmpty { "Untitled Book" }
        } else {
            "Untitled Book"
        }

        // Author
        val creatorNodes = doc.getElementsByTagNameNS("*", "creator")
        val author = if (creatorNodes.length > 0) {
            creatorNodes.item(0).textContent.trim().ifEmpty { "Unknown Author" }
        } else {
            "Unknown Author"
        }

        // Identifier
        val idNodes = doc.getElementsByTagNameNS("*", "identifier")
        val bookId = if (idNodes.length > 0) {
            val rawId = idNodes.item(0).textContent.trim()
            rawId.replace("urn:uuid:", "").ifEmpty { UUID.randomUUID().toString() }
        } else {
            UUID.randomUUID().toString()
        }

        // Cover ID from meta
        var coverId: String? = null
        val metaNodes = doc.getElementsByTagName("meta")
        for (i in 0 until metaNodes.length) {
            val el = metaNodes.item(i) as? Element ?: continue
            if (el.getAttribute("name") == "cover") {
                coverId = el.getAttribute("content").trim()
                break
            }
        }

        // Manifest
        val manifestItems = mutableMapOf<String, ManifestItem>()
        val itemNodes = doc.getElementsByTagName("item")
        for (i in 0 until itemNodes.length) {
            val el = itemNodes.item(i) as? Element ?: continue
            val id = el.getAttribute("id").trim()
            val href = el.getAttribute("href").trim()
            val mediaType = el.getAttribute("media-type").trim()
            val properties = el.getAttribute("properties").trim()
            if (id.isNotEmpty() && href.isNotEmpty()) {
                manifestItems[id] = ManifestItem(id, href, mediaType, properties)
                if (properties.contains("cover-image") && coverId == null) {
                    coverId = id
                }
            }
        }

        // Cover bytes
        var coverEntryPath: String? = null
        var coverBytes: ByteArray? = null
        if (coverId != null && manifestItems.containsKey(coverId)) {
            val coverItem = manifestItems[coverId]!!
            val resolvedCoverPath = resolveHref(opfDir, coverItem.href)
            if (zipEntries.containsKey(resolvedCoverPath)) {
                coverEntryPath = resolvedCoverPath
                coverBytes = zipEntries[resolvedCoverPath]
            }
        }

        // Spine
        val spineItems = mutableListOf<SpineItemEntity>()
        val spineNodes = doc.getElementsByTagName("spine")
        var tocRef: String? = null
        if (spineNodes.length > 0) {
            val spineEl = spineNodes.item(0) as Element
            tocRef = spineEl.getAttribute("toc").trim().ifEmpty { null }
        }

        val itemRefNodes = doc.getElementsByTagName("itemref")
        for (i in 0 until itemRefNodes.length) {
            val el = itemRefNodes.item(i) as? Element ?: continue
            val idref = el.getAttribute("idref").trim()
            val linear = el.getAttribute("linear").trim() != "no"
            val item = manifestItems[idref]
            if (item != null) {
                val resolvedHref = resolveHref(opfDir, item.href)
                spineItems.add(
                    SpineItemEntity(
                        id = "$bookId#$i",
                        bookId = bookId,
                        href = resolvedHref,
                        mediaType = item.mediaType,
                        orderIndex = i,
                        linear = linear
                    )
                )
            }
        }

        // TOC
        val tocEntries = mutableListOf<TocEntryEntity>()
        // Check for NCX
        val ncxItem = (if (tocRef != null) manifestItems[tocRef] else null)
            ?: manifestItems.values.firstOrNull { it.mediaType == "application/x-dtbncx+xml" }

        if (ncxItem != null) {
            val ncxPath = resolveHref(opfDir, ncxItem.href)
            val ncxBytes = zipEntries[ncxPath]
            if (ncxBytes != null) {
                parseNcx(ncxBytes, opfDir, bookId, tocEntries)
            }
        }

        // Fallback: If no TOC entries found, create from spine items
        if (tocEntries.isEmpty()) {
            spineItems.forEachIndexed { index, spine ->
                val titlePart = spine.href.substringAfterLast('/').substringBeforeLast('.')
                tocEntries.add(
                    TocEntryEntity(
                        id = "$bookId#toc#$index",
                        bookId = bookId,
                        title = titlePart.replace('_', ' ').replace('-', ' ').capitalizeWords(),
                        href = spine.href,
                        orderIndex = index,
                        level = 0
                    )
                )
            }
        }

        val bookEntity = BookEntity(
            id = bookId,
            title = title,
            author = author,
            filePath = filePath,
            coverPath = null,
            spineIndex = 0,
            charOffset = 0,
            progressPercent = 0f,
            isSample = false,
            isNew = true,
            isDownloaded = true,
            collectionName = null,
            lastReadTimestamp = 0L,
            addedTimestamp = System.currentTimeMillis()
        )

        return ParsedEpub(
            book = bookEntity,
            spineItems = spineItems,
            tocEntries = tocEntries,
            coverEntryPath = coverEntryPath,
            coverBytes = coverBytes
        )
    }

    private fun parseNcx(
        ncxBytes: ByteArray,
        opfDir: String,
        bookId: String,
        outToc: MutableList<TocEntryEntity>
    ) {
        val doc = docBuilderFactory.newDocumentBuilder().parse(ByteArrayInputStream(ncxBytes))
        val navPoints = doc.getElementsByTagName("navPoint")
        for (i in 0 until navPoints.length) {
            val np = navPoints.item(i) as? Element ?: continue
            val labelNodes = np.getElementsByTagName("text")
            val navLabel = if (labelNodes.length > 0) labelNodes.item(0).textContent.trim() else "Chapter ${i + 1}"
            val contentNodes = np.getElementsByTagName("content")
            val src = if (contentNodes.length > 0) {
                (contentNodes.item(0) as Element).getAttribute("src").trim()
            } else ""

            val resolvedSrc = resolveHref(opfDir, src)
            outToc.add(
                TocEntryEntity(
                    id = "$bookId#toc#$i",
                    bookId = bookId,
                    title = navLabel,
                    href = resolvedSrc,
                    orderIndex = i,
                    level = 0
                )
            )
        }
    }

    private fun resolveHref(baseDir: String, href: String): String {
        if (baseDir.isEmpty()) return href
        val combined = "$baseDir/$href"
        val segments = mutableListOf<String>()
        for (part in combined.split("/")) {
            if (part == "." || part.isEmpty()) continue
            if (part == "..") {
                if (segments.isNotEmpty()) segments.removeAt(segments.size - 1)
            } else {
                segments.add(part)
            }
        }
        return segments.joinToString("/")
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
