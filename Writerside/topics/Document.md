# Document Library

Since the Scooby application needs to continuously manage HTML documents and links within webpages, an abstraction for
these concepts is necessary.

To address this, we developed a **Document Library** that encapsulates everything needed to handle HTML documents.

## Structure

```plantuml
@startuml
hide empty members

class Document {
    content: String
    url: URL
}
class Explorer << (T, #FF7700) Trait >> {}

class RegExpExplorer << (T, #FF7700) Trait >> {
    find(regex: String): Seq[String]
}

class HTMLExplorer << (T, #FF7700) Trait >> {
    -parseDocument(): HTMLDom
}

class CommonHTMLExplorer << (T, #FF7700) Trait >> {
    getElementById(id: String): Option[HTMLElement]
    getElementsByTag(tag: String): Seq[HTMLElement]
    getElementsByClass(className: String): Seq[HTMLElement]
}

class SelectorExplorer << (T, #FF7700) Trait >> {
    select(selectors: String*): Seq[HTMLElement]
}

Explorer --|> Document
RegExpExplorer --|> Explorer
HTMLExplorer --|> Explorer
CommonHTMLExplorer --|> HTMLExplorer
SelectorExplorer --|> HTMLExplorer

class CrawlDocument {}
class ScrapeDocument {}

CrawlDocument --|> Document
CrawlDocument --|> RegExpExplorer
ScrapeDocument --|> Document
ScrapeDocument --|> RegExpExplorer
ScrapeDocument --|> CommonHTMLExplorer
ScrapeDocument --|> SelectorExplorer
@enduml
```

This library is designed with Scala Traits in mind, leveraging their modularity and composability to separate document
capabilities in a very fine-grained manner.

We use what we call **Explorers** to define the capabilities for extracting information from a document's content.
Depending on the use case, not all document types require all capabilities. For instance, when crawling a webpage, the
primary interest is in finding hyperlinks rather than using HTML selectors, hence `CrawlDocument` does not require
the `SelectorExplorer`.

There is also a hierarchy among the Explorers: both `CommonHTMLExplorer` and `SelectorExplorer` need to access an HTML
document internally, which requires parsing the document's string content. Therefore, we defined an `HTMLExplorer` that
lazily parses a document's content into HTML and makes it available to the other Explorers that extend it (keeping it
private to maintain encapsulation).

## HTML Library

To facilitate the interaction and parsing of HTML content, we designed a small HTML library. This library is mainly
utilized by the Explorers (particularly the `CommonHTMLExplorer`).

```plantuml
@startuml
hide empty members

class HTMLDom << Case class >> {
    select(selectors: String*): Seq[HTMLElement]
    getElementById(id: String): Option[HTMLElement]
    getElementByTag(tag: String): Seq[HTMLElement]
    getElementByClass(className: String): Seq[HTMLElement]
}

class HTMLElement << Case class >> {
    text: String
    attr(attribute: String): String
    classes: Set[String]
    tag: String
    id: String
    outerHTML: String
    parent: HTMLElement
    children: Seq[HTMLElement]
}

class "Parser<T>" as Parser_t << (T, #FF7700) Trait >>  {
    parse(s: String): T
}

class HTMLParser<HTMLDom> <<(O, #906901) Object>> {
    parse(s: String): HTMLDom
}

HTMLParser --|> Parser_t
HTMLParser ..> HTMLDom: <<uses>>
HTMLDom ..> HTMLElement: <<uses>>

class HTMLExplorer << (T, #FF7700) Trait >> {
    -parseDocument(): HTMLDom
}

HTMLExplorer .> HTMLParser: <<uses>>

@enduml
```