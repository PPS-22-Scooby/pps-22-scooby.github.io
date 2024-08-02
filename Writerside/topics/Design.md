# Design
We've designed the library using the actor paradigm approach: we've identified the main entities in the system and how 
they interact with each other and modeled them in terms of behaviors and messages they can receive.


## Domain Glossary
For identifying the main entities in the system we've created a glossary of the terms used in the domain:

| Term                 | Description                                                                                                         |
|----------------------|---------------------------------------------------------------------------------------------------------------------|
| Crawler              | Entity that navigates through links, building a data structure representing the paths taken during the navigation   |
| Scraper              | Is a program that extracts specific data from Web sites by analyzing page content to gather structured information. |
| Traversable          | Something that can be traversed, or scraped, obtaining relevant information                                         |
| Link                 | Something that can be explored to get other resources and that increases the depth of the search                    |
| Exploration Policy | Can be represented as a crawler’s behavior → is the method that is used by a crawler for exploring a website.       |
| Scraping Policy      | The behavior of a scraper → it will scrape a page fetching the elements inside it based on a defined condition.     |
| Exporter             | An element that, given some scraped data will export in a given format (like json).                                 |

## Reactive Entities

- **Crawler**: Responsible for fetching links from pages and spawning new crawlers to analyze them.
- **Scraper**: Responsible for extracting data from a page.
- **Coordinator**: Single entity responsible for managing the system and coordinating the crawlers.
- **Exporter**: Responsible for exporting the scraped data.

## Passive Entities
- **Document**: Represent an HTML document that is begin fetched from an URL.
  - **ScrapeDocument**: Represent a document that is being scraped.
  - **CrawlDocument**: Represent a document that is being crawled.
  
- **Result**: Represents a result of a scraping operation. 

## General UML
```plantuml
@startuml Crawler
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