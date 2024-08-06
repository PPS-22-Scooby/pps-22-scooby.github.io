# Design
The library has been designed adopting the actor paradigm approach; with this regard, the main challenging modelling
steps were the identification of the main entities in the system and manage their interaction, expressed in terms of
behaviors and messages they can receive.

## Domain Glossary

In order to make it easier to identify the main entities of the system, a detailed glossary describing terms used in the
domain has been built:

| Term                 | Description                                                                                                        |
|----------------------|--------------------------------------------------------------------------------------------------------------------|
| Crawler              | Entity that navigates through links, building a data structure representing the paths taken during the navigation  |
| Scraper              | Entity that extracts specific data from Web sites by analyzing page content to gather structured information.      |
| Traversable          | Something that can be traversed, or scraped, obtaining relevant information                                        |
| Link                 | Something that can be explored to get other resources, which increases the depth of the search                     |
| Exploration Policy | Can be represented as a crawler’s behavior → is the method that is used by a crawler for exploring a website.      |
| Scraping Policy      | The behavior of a scraper → it will scrape a page fetching the elements inside it based on some defined condition. |
| Exporter             | An element that, given some scraped data, will export them in a given format (like json).                          |

## Reactive Entities

- **Crawler**: Fetches links from pages and spawn new crawlers to analyze them.
- **Scraper**: Extracts data from a page.
- **Coordinator**: Single entity which manages the system and coordinates crawlers.
- **Exporter**: Exports scraped data.

## Passive Entities
- **Document**: Represent an HTML document fetched from a URL.
- **ScrapeDocument**: Represent a facade of a document that can be scraped.
- **CrawlDocument**: Represent a facade of a document that can be crawled.
- **Result**: Represents a result of a scraping operation. 

## General UML
```plantuml
@startuml Crawler
    hide empty members
    interface Document
    interface ScrapeDocument extends Document
    interface CrawlDocument extends Document
    
    interface Result
    
    class Crawler <<(A, #FF7700) Actor>>
    class Coordinator <<(A, #FF7700) Actor>>
    class Scraper <<(A, #FF7700) Actor>>
    class Exporter <<(A, #FF7700) Actor>>
    
    Crawler .u.> Coordinator: <<signal>>
    Coordinator ..> Crawler: <<signal>>
    
    Crawler ..> CrawlDocument: <<uses>>
    Scraper ..> ScrapeDocument: <<uses>>
    
    Crawler .l.> Scraper: <<creates>>
    Scraper ..> Result: <<creates>>
    Scraper ..> Exporter: <<signal>>
    Exporter ..> Result: <<uses>> 
@enduml
``