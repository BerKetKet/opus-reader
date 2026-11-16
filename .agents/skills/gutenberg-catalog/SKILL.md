---
name: gutenberg-catalog
description: Integrates Project Gutenberg as the book source that replaces the Kindle store. Use when building Home shelves, browse, search, book details or EPUB downloads.
---

# Project Gutenberg as the store

- Source: the Gutendex JSON API (https://gutendex.com/books). Check its docs for query parameters before coding; do not assume.
- EPUB link: the `application/epub+zip` entry in each book's `formats`. Cover: the `image/jpeg` entry.
- Home shelves map to queries (popular, by topic, by language). Chips map to topic or language filters.
- Cache responses in Room for offline browsing. Download EPUBs with WorkManager into app storage, then add to the library like any imported file.
- Be polite: no bulk crawling, a descriptive User-Agent, debounce search.
- Everything sits behind a `CatalogRepository` interface, with a fake for previews and tests.
