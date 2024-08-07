# Sprint 6: Aggregation of core components

| Ticket Id | Summary                                                       | Backlog item          | Assigned to                                                         | Estimation | Actual | Type          |
|-----------|---------------------------------------------------------------|-----------------------|---------------------------------------------------------------------|------------|--------|---------------|
| PPS-61    | Coordinator: add validation rules                             | Domain implementation | Giovanni Antonioni                                                  | 1h         | 1h     | Dev           |
| PPS-55    | Integration between Scraper and Exporter                      | Integration           | Giovanni Antonioni                                                  | 3h         | 5h     | Integration   |
| PPS-45    | Exporter Actor                                                | Actor implementation  | Giovanni Antonioni, Francesco Magnani                               | 4h         | 2h     | Dev           |
| PPS-60    | Create configuration classes                                  | Configuration library | Valerio Di Zio, Francesco Magnani                                   | 7h         | 5h     | Dev           |
| PPS-47    | Parameterize the application startup                          | Entrypoint            | Giovanni Antonioni, Valerio Di Zio, Luca Rubboli, Francesco Magnani | 2h         | 1h 30m | Design        |
| PPS-48    | Scooby Class                                                  | Entrypoint            | Giovanni Antonioni, Valerio Di Zio, Luca Rubboli, Francesco Magnani | 4h         | 3h     | Dev           |
| PPS-59    | Define the status of terminated computation                   | Actor orchestration   | Francesco Magnani                                                   | 4h         | 3h     | Fix           |
| PPS-58    | Exporter refactoring                                          | Actor implementation  | Giovanni Antonioni, Francesco Magnani                               | 1h         | 1h     | Refactoring   |
| PPS-52    | Integration Between Scraper and Crawler                       | Integration           | Valerio Di Zio                                                      | 1h 30m     | 1h     | Integration   |
| PPS-62    | Remove Document in Scraper, replaced with ScrapeDocument      | Enhancement           | Valerio Di Zio                                                      | 1h         | 1h 30m | Refactoring   |
| PPS-50    | Gracefully handle Explorer Document error                     | Enhancement           | Luca Rubboli                                                        | 2h         | 1h 30m | Fix           |
## Sprint goal

- Minor refactoring;
- Manage termination status;
- Domain entities integration;
- Extract application parameters;
- Build scooby class.

## Sprint deadline

14/07/2024

## Sprint review

All goals have been fulfilled.

## Sprint retrospective

Given code modularity adopted during previous phases, has been pretty trivial to extract parameters and build the application's entrypoint, as well as domain entities integration due to constant sharing of entities updates among all the members.
