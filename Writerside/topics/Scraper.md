# Scraper

A scraper is a system entity tasked with analyzing page content to gather structured information.
Its work is triggered by the Crawler, which provides the document to scrape. Once its analysis is finished, it notifies
the Exporter with the result obtained and stops.

Interaction between other system entities can be depicted with the following diagram:

```plantuml
@startuml
participant Crawler as crawler order 10
actor Scraper as scraper order 20
participant Exporter as exporter order 30

crawler->scraper :Create(scraper configs)
crawler->scraper : Scrape(document)
scraper->scraper : Apply policies to document
scraper->exporter : Export(result)
scraper->scraper : Stop
@enduml
```

## Structure

```plantuml

@startuml Scraper

class Crawler {
    + Document crawl(path: URL)
}

class Scraper<T> {
    - policy: Document => Iterable[T]
    + Iterable[T] scrape(doc: Document)
}

class Exporter {
    + Unit export(result: Iterable[T])
}

Crawler --> Scraper: scrape
Scraper --> Exporter: export

@enduml
```