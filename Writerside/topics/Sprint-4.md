# Sprint 4: Actor driven development of Crawler and Scraper

| Ticket Id | Summary                                                     | Backlog item         | Assigned to                                                         | Estimation | Actual | Type          |
|-----------|-------------------------------------------------------------|----------------------|---------------------------------------------------------------------|------------|--------|---------------|
| PPS-31    | Rule refactoring                                            | Rule library         | Giovanni Antonioni                                                  | 30m        | 30m    | Refactoring   |
| PPS-24    | Exporter Prototyping                                        | Domain prototyping   | Luca Rubboli                                                        | 2h         | 2h     | Design        |
| PPS-37    | Define and implement the Crawler Actor                      | Actor implementation | Giovanni Antonioni, Valerio Di Zio                                  | 6h         | 5h     | Design        |
| PPS-30    | Tuning of HTTP library                                      | HTTP library         | Francesco Magnani                                                   | 2h         | 2h     | Enhancement   |
| PPS-38    | Implement actor-based logic within the coordinator          | Actor implementation | Valerio Di Zio                                                      | 3h 30m     | 3h     | Dev           |
| PPS-39    | Do the integration of previous developments: Document - URL | Integration          | Francesco Magnani                                                   | 30m        | 1h     | Integration   |

## Sprint goal

- Implement the Crawler and the Scraper classes following the Actor Model paradigm;
- Implement the Actor System and the Actor Manager.

## Sprint deadline

30/06/2024

## Sprint review

We managed to complete the previous sprint goals, implementing the Crawler and the Scraper classes, following the Actor Model paradigm. We also started the implementation of the Coordinator class, which will be completed in the next sprint.

## Sprint retrospective
In this sprint we spent lots of time on creating tests for the Actor entities. We used the TestKit library, which was new for us, and we had to learn how to use it, which took more time than expected. Also the integration between Document and URL was more complex than expected, and we had to refactor the code several times.