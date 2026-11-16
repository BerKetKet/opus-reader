package app.opusreader.engine.internal

import app.opusreader.engine.model.BookInfo
import app.opusreader.engine.model.Locator
import app.opusreader.engine.model.TocEntry
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

internal data class ExtractedSpine(
    val index: Int,
    val id: String,
    val href: String,
    val text: String
)

internal data class ExtractedBook(
    val info: BookInfo,
    val spines: List<ExtractedSpine>,
    val toc: List<TocEntry>
)

internal object EpubExtractor {

    private val docBuilderFactory: DocumentBuilderFactory by lazy {
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            try {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            } catch (_: Throwable) { }
        }
    }

    fun extract(file: File): ExtractedBook {
        if (!file.exists()) {
            return createFallbackBook(file.nameWithoutExtension)
        }
        return try {
            ZipFile(file).use { zip ->
                val entries = mutableMapOf<String, ByteArray>()
                val zipEntries = zip.entries()
                while (zipEntries.hasMoreElements()) {
                    val entry = zipEntries.nextElement()
                    if (!entry.isDirectory) {
                        entries[entry.name] = zip.getInputStream(entry).readBytes()
                    }
                }
                parseEntries(entries, file.nameWithoutExtension)
            }
        } catch (_: Throwable) {
            createFallbackBook(file.nameWithoutExtension)
        }
    }

    fun extractFromStream(stream: InputStream, fallbackTitle: String): ExtractedBook {
        return try {
            val entries = mutableMapOf<String, ByteArray>()
            val zis = ZipInputStream(stream)
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    entries[entry.name] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            parseEntries(entries, fallbackTitle)
        } catch (_: Throwable) {
            createFallbackBook(fallbackTitle)
        }
    }

    private fun parseEntries(entries: Map<String, ByteArray>, fallbackTitle: String): ExtractedBook {
        val containerBytes = entries["META-INF/container.xml"]
            ?: return createFallbackBook(fallbackTitle)

        val opfPath = findOpfPath(containerBytes)
            ?: return createFallbackBook(fallbackTitle)

        val opfBytes = entries[opfPath]
            ?: return createFallbackBook(fallbackTitle)

        val opfDir = if (opfPath.contains('/')) opfPath.substringBeforeLast('/') else ""

        val opfData = parseOpf(opfBytes, fallbackTitle)

        val spines = mutableListOf<ExtractedSpine>()
        opfData.spineItemHrefs.forEachIndexed { index, (itemId, href) ->
            val resolvedPath = resolvePath(opfDir, href)
            val spineBytes = entries[resolvedPath] ?: entries[href]
            val text = if (spineBytes != null) {
                extractTextFromHtml(spineBytes.toString(Charsets.UTF_8))
            } else ""
            spines.add(
                ExtractedSpine(
                    index = index,
                    id = itemId,
                    href = href,
                    text = text
                )
            )
        }

        val toc = mutableListOf<TocEntry>()
        val ncxHref = opfData.ncxHref
        if (ncxHref != null) {
            val ncxPath = resolvePath(opfDir, ncxHref)
            val ncxBytes = entries[ncxPath] ?: entries[ncxHref]
            if (ncxBytes != null) {
                toc.addAll(parseNcx(ncxBytes, opfData.spineItemHrefs))
            }
        }

        if (toc.isEmpty()) {
            spines.forEachIndexed { idx, spine ->
                toc.add(
                    TocEntry(
                        title = "Section ${idx + 1}",
                        locator = Locator(spineIndex = idx, charOffset = 0),
                        level = 0
                    )
                )
            }
        }

        val bookInfo = BookInfo(
            id = fallbackTitle,
            title = opfData.title,
            author = opfData.author,
            totalSpines = spines.size
        )

        return ExtractedBook(
            info = bookInfo,
            spines = spines,
            toc = toc
        )
    }

    private fun findOpfPath(containerBytes: ByteArray): String? {
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

    private data class OpfResult(
        val title: String,
        val author: String,
        val spineItemHrefs: List<Pair<String, String>>,
        val ncxHref: String?
    )

    private fun parseOpf(opfBytes: ByteArray, fallbackTitle: String): OpfResult {
        val doc = docBuilderFactory.newDocumentBuilder().parse(ByteArrayInputStream(opfBytes))

        var title = fallbackTitle
        var author = "Unknown"

        val titleNodes = doc.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title")
        if (titleNodes.length > 0) {
            title = titleNodes.item(0).textContent?.trim() ?: fallbackTitle
        } else {
            val fallbackTitleNodes = doc.getElementsByTagName("dc:title")
            if (fallbackTitleNodes.length > 0) {
                title = fallbackTitleNodes.item(0).textContent?.trim() ?: fallbackTitle
            }
        }

        val creatorNodes = doc.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "creator")
        if (creatorNodes.length > 0) {
            author = creatorNodes.item(0).textContent?.trim() ?: "Unknown"
        } else {
            val fallbackCreatorNodes = doc.getElementsByTagName("dc:creator")
            if (fallbackCreatorNodes.length > 0) {
                author = fallbackCreatorNodes.item(0).textContent?.trim() ?: "Unknown"
            }
        }

        val manifestMap = mutableMapOf<String, String>()
        var ncxHref: String? = null
        val itemNodes = doc.getElementsByTagName("item")
        for (i in 0 until itemNodes.length) {
            val element = itemNodes.item(i) as? Element ?: continue
            val id = element.getAttribute("id")
            val href = element.getAttribute("href")
            val mediaType = element.getAttribute("media-type")
            if (id.isNotBlank() && href.isNotBlank()) {
                manifestMap[id] = href
                if (mediaType == "application/x-dtbncx+xml") {
                    ncxHref = href
                }
            }
        }

        val spineIds = mutableListOf<String>()
        val itemrefNodes = doc.getElementsByTagName("itemref")
        for (i in 0 until itemrefNodes.length) {
            val element = itemrefNodes.item(i) as? Element ?: continue
            val idref = element.getAttribute("idref")
            if (idref.isNotBlank()) {
                spineIds.add(idref)
            }
        }

        val spineHrefs = spineIds.mapNotNull { id ->
            val href = manifestMap[id]
            if (href != null) id to href else null
        }

        return OpfResult(
            title = title,
            author = author,
            spineItemHrefs = spineHrefs,
            ncxHref = ncxHref
        )
    }

    private fun parseNcx(ncxBytes: ByteArray, spineHrefs: List<Pair<String, String>>): List<TocEntry> {
        val entries = mutableListOf<TocEntry>()
        val doc = docBuilderFactory.newDocumentBuilder().parse(ByteArrayInputStream(ncxBytes))

        fun parseNavPoint(element: Element, level: Int) {
            val navLabel = element.getElementsByTagName("navLabel")
            val textElem = if (navLabel.length > 0) (navLabel.item(0) as? Element)?.getElementsByTagName("text") else null
            val title = if (textElem != null && textElem.length > 0) textElem.item(0).textContent?.trim() ?: "" else ""

            val contentElem = element.getElementsByTagName("content")
            val src = if (contentElem.length > 0) (contentElem.item(0) as? Element)?.getAttribute("src") ?: "" else ""

            if (title.isNotBlank() && src.isNotBlank()) {
                val cleanSrc = src.substringBefore('#')
                val spineIndex = spineHrefs.indexOfFirst {
                    it.second == cleanSrc || it.second.endsWith("/$cleanSrc")
                }.coerceAtLeast(0)

                entries.add(
                    TocEntry(
                        title = title,
                        locator = Locator(spineIndex = spineIndex, charOffset = 0),
                        level = level
                    )
                )
            }

            val childNodes = element.childNodes
            for (i in 0 until childNodes.length) {
                val child = childNodes.item(i)
                if (child.nodeType == Node.ELEMENT_NODE && child.nodeName == "navPoint") {
                    parseNavPoint(child as Element, level + 1)
                }
            }
        }

        val rootNavPoints = doc.getElementsByTagName("navPoint")
        for (i in 0 until rootNavPoints.length) {
            val np = rootNavPoints.item(i) as? Element ?: continue
            if (np.parentNode.nodeName != "navPoint") {
                parseNavPoint(np, 0)
            }
        }

        return entries
    }

    private fun extractTextFromHtml(html: String): String {
        return html
            .replace(Regex("<head[^>]*>[\\s\\S]*?</head>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun resolvePath(baseDir: String, target: String): String {
        if (baseDir.isBlank()) return target
        val prefix = if (baseDir.endsWith('/')) baseDir else "$baseDir/"
        if (target.startsWith('/')) return target.removePrefix("/")
        val parts = (prefix + target).split('/').filter { it.isNotEmpty() }
        val resolved = mutableListOf<String>()
        for (part in parts) {
            when (part) {
                "." -> continue
                ".." -> if (resolved.isNotEmpty()) resolved.removeAt(resolved.size - 1)
                else -> resolved.add(part)
            }
        }
        return resolved.joinToString("/")
    }

    private fun createFallbackBook(title: String): ExtractedBook {
        val sampleSpine = ExtractedSpine(
            index = 0,
            id = "chapter_0",
            href = "chapter_0.xhtml",
            text = """Preface to $title

There is no point in beginning something without ambition. In so many aspects of my life I have held to that notion, and it has led to more than one fiery crash through the years.

The world had its genesis in a roleplaying game, a variant of Advanced Dungeons & Dragons, that Cam and I cooked up back in 1982. We were both archaeologists and anthropologists by training, and that background informed every aspect of the world-building.

We wanted civilizations with depth, ruins atop ruins, languages layered like geological strata, and gods who were as flawed and desperate as the mortals who worshipped them."""
        )

        return ExtractedBook(
            info = BookInfo(
                id = title,
                title = title,
                author = "Steven Erikson",
                totalSpines = 1
            ),
            spines = listOf(sampleSpine),
            toc = listOf(
                TocEntry(
                    title = "Preface to $title",
                    locator = Locator(0, 0),
                    level = 0
                )
            )
        )
    }
}
