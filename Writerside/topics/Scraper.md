# Scraper

A scraper is a system entity tasked with analyzing page content to gather structured information.
Its work is triggered by the Crawler, which provides the document to scrape. Once its analysis is finished, it notifies
the Exporter with the result obtained and stops.

Interaction between other system entities can be depicted with the following diagram:

```plantuml
@startuml
participant Crawler as crawler order 10
activate crawler
actor Scraper as scraper order 20
participant Exporter as exporter order 30

crawler->scraper :Create(scraper configs)
activate scraper
crawler->scraper : Scrape(document)
scraper->scraper : Apply policies to document
scraper->exporter : Export(result)
activate exporter
destroy scraper
@enduml
```

## Structure


```plantuml

@startuml Scraper
hide empty members
class Crawler<<(A, #FF7700) Actor>> {
    exporter: ActorRef[ExporterCommands]
    scraper: ActorRef[ScraperCommands]
    scraperPolicy: ScraperPolicy[T]
}

class ScrapeDocument {
    find(regExp: String): Seq[String]
    group(toGroup: Iterator[Regex.Match]): Seq[String]
    frontier(): Seq[URL]
    getAllLinkOccurrences(): Seq[URL]
    parseDocument(using parser: Parser[HTMLDom]): HTMLDom
    select(selectors: String*): Seq[HTMLElement]
    getElementById(id: String): Option[HTMLElement]
    getElementsByTag(tag: String): Seq[HTMLElement]
    getElementsByClass(className: String): Seq[HTMLElement]
    getAllElements(): Seq[HTMLElement]
}

enum ScraperCommand {
    Scrape(document:ScrapeDocument)
}

protocol ScraperPolicy<T> 

class Scraper<<(A, #FF7700) Actor>> {
    exporter: ActorRef[ExporterCommands]
    policy: ScraperPolicy[T]
    scrape(document: ScrapeDocument): Iterable[T]
}

class Exporter<<(A, #FF7700) Actor>> {
    export(result: Iterable[T]): Unit
}

Scraper ..> ScraperCommand: <<uses>>
Scraper ..> ScraperPolicy: <<uses>>
Scraper ..> Exporter: <<signal>>

ScraperPolicy ..> ScrapeDocument: <<uses>>

Crawler ..> Scraper: <<creates>>
Crawler ..> Scraper: <<signal>>

@enduml
```

## Scraper Policy

A Scraper Policy is the transformation that the Scraper applies to the page provided by Crawler to gather structured
information, which are then delivered to the Exporter entity.