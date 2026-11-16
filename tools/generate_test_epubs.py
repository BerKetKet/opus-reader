#!/usr/bin/env python3
import os
import zipfile
import io
import pathlib

def create_valid_epub(output_path):
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, 'w') as zf:
        # 1. mimetype (must be uncompressed according to EPUB spec)
        zf.writestr('mimetype', 'application/epub+zip', compress_type=zipfile.ZIP_STORED)
        
        # 2. META-INF/container.xml
        container_xml = """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
    <rootfiles>
        <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
    </rootfiles>
</container>"""
        zf.writestr('META-INF/container.xml', container_xml)

        # 3. OEBPS/content.opf
        content_opf = """<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookID" version="2.0">
    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:opf="http://www.idpf.org/2007/opf">
        <dc:title>Pride and Prejudice</dc:title>
        <dc:creator opf:role="aut">Jane Austen</dc:creator>
        <dc:identifier id="BookID">urn:uuid:test-austen-001</dc:identifier>
        <dc:language>en</dc:language>
        <meta name="cover" content="cover-image"/>
    </metadata>
    <manifest>
        <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
        <item id="cover-image" href="cover.png" media-type="image/png"/>
        <item id="chapter1" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
        <item id="chapter2" href="chapter2.xhtml" media-type="application/xhtml+xml"/>
    </manifest>
    <spine toc="ncx">
        <itemref idref="chapter1"/>
        <itemref idref="chapter2"/>
    </spine>
</package>"""
        zf.writestr('OEBPS/content.opf', content_opf)

        # 4. OEBPS/toc.ncx
        toc_ncx = """<?xml version="1.0" encoding="UTF-8"?>
<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
    <head>
        <meta name="dtb:uid" content="urn:uuid:test-austen-001"/>
    </head>
    <docTitle><text>Pride and Prejudice</text></docTitle>
    <navMap>
        <navPoint id="navPoint-1" playOrder="1">
            <navLabel><text>Chapter 1</text></navLabel>
            <content src="chapter1.xhtml"/>
        </navPoint>
        <navPoint id="navPoint-2" playOrder="2">
            <navLabel><text>Chapter 2</text></navLabel>
            <content src="chapter2.xhtml"/>
        </navPoint>
    </navMap>
</ncx>"""
        zf.writestr('OEBPS/toc.ncx', toc_ncx)

        # 5. OEBPS/chapter1.xhtml
        chapter1_html = """<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head><title>Chapter 1</title></head>
<body>
    <h1>Chapter 1</h1>
    <p>It is a truth universally acknowledged, that a single man in possession of a good fortune, must be in want of a wife.</p>
</body>
</html>"""
        zf.writestr('OEBPS/chapter1.xhtml', chapter1_html)

        # 6. OEBPS/chapter2.xhtml
        chapter2_html = """<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head><title>Chapter 2</title></head>
<body>
    <h1>Chapter 2</h1>
    <p>Mr. Bennet was among the earliest of those who waited on Mr. Bingley.</p>
</body>
</html>"""
        zf.writestr('OEBPS/chapter2.xhtml', chapter2_html)

        # 7. OEBPS/cover.png (minimal valid 1x1 transparent PNG)
        png_1x1 = b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x06\x00\x00\x00\x1f\x15c4\x00\x00\x00\nIDATx\x9cc\x00\x01\x00\x00\x05\x00\x01\r\n-\xb4\x00\x00\x00\x00IEND\xaeB`\x82'
        zf.writestr('OEBPS/cover.png', png_1x1)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, 'wb') as f:
        f.write(buffer.getvalue())

def create_minimal_epub(output_path):
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, 'w') as zf:
        zf.writestr('mimetype', 'application/epub+zip', compress_type=zipfile.ZIP_STORED)
        container_xml = """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
    <rootfiles>
        <rootfile full-path="content.opf" media-type="application/oebps-package+xml"/>
    </rootfiles>
</container>"""
        zf.writestr('META-INF/container.xml', container_xml)
        content_opf = """<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookID" version="2.0">
    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:title>Minimal Book</dc:title>
        <dc:creator>Anonymous</dc:creator>
        <dc:identifier id="BookID">urn:uuid:test-minimal</dc:identifier>
        <dc:language>en</dc:language>
    </metadata>
    <manifest>
        <item id="c1" href="chap1.html" media-type="application/xhtml+xml"/>
    </manifest>
    <spine>
        <itemref idref="c1"/>
    </spine>
</package>"""
        zf.writestr('content.opf', content_opf)
        zf.writestr('chap1.html', '<html><body><p>Hello World</p></body></html>')

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, 'wb') as f:
        f.write(buffer.getvalue())

def create_malformed_corrupt(output_path):
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, 'wb') as f:
        f.write(b'This is definitely not an epub or zip file at all! Just raw corrupted bytes.')

def create_malformed_missing_container(output_path):
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, 'w') as zf:
        zf.writestr('mimetype', 'application/epub+zip')
        zf.writestr('some_file.txt', 'random text')
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, 'wb') as f:
        f.write(buffer.getvalue())

def create_malformed_missing_opf(output_path):
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, 'w') as zf:
        zf.writestr('mimetype', 'application/epub+zip')
        container_xml = """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
    <rootfiles>
        <rootfile full-path="non_existent/content.opf" media-type="application/oebps-package+xml"/>
    </rootfiles>
</container>"""
        zf.writestr('META-INF/container.xml', container_xml)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, 'wb') as f:
        f.write(buffer.getvalue())

if __name__ == '__main__':
    root_dir = pathlib.Path(__file__).resolve().parent.parent
    targets = [
        root_dir / 'data' / 'src' / 'test' / 'resources' / 'epubs',
        root_dir / 'data' / 'src' / 'androidTest' / 'assets' / 'epubs'
    ]
    for base_dir in targets:
        create_valid_epub(base_dir / 'valid_book.epub')
        create_minimal_epub(base_dir / 'minimal_book.epub')
        create_malformed_corrupt(base_dir / 'malformed_corrupt.epub')
        create_malformed_missing_container(base_dir / 'malformed_missing_container.epub')
        create_malformed_missing_opf(base_dir / 'malformed_missing_opf.epub')
        print(f'Test EPUBs generated successfully in {base_dir}')
