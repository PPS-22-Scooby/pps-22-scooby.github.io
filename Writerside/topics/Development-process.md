# Development Process

The development process adopted follows the [SCRUM](https://www.scrum.org/) framework, incorporating its guidelines and
applying insights gained during the course.

The project was managed in an agile manner to maximize **teamwork**. Automatic tools tailored for this development were
used, promoting both personal and group growth through frequent interactions among team members.

## Meetings

The team engaged in various types of meetings throughout the project development.

### Initial Planning

The team conducted approximately 2-3 meetings at the project’s outset to define the various requirements
(_Product backlog_) with an appropriate level of abstraction. These meetings also clarified the use cases for various
_stakeholders_ and organized the initial stages of work, such as the initial sprint.

### Sprint Review and Sprint Planning

Sprint review and sprint planning meetings were held at the end and beginning of each sprint (Sunday and Monday,
respectively). These meetings were scheduled consecutively to accommodate the needs of all group members.

Starting from the _macro_ requirements (e.g., developing a core API feature, formulating the DSL) outlined in the
product backlog, the objectives of each sprint were defined. Activities for each group member were also determined
during these meetings.

At the end of each sprint, a meeting was held to evaluate the week’s work, identify any incomplete features or
shortcomings, and highlight problems encountered and key points for consideration in the next sprint.

### Daily Scrum

Daily meetings were held to align the group members, typically lasting less than an hour. If no updates were needed, the
meetings were occasionally shortened. Additional meetings were convened as needed to provide assistance or analyze
critical problems, which could extend the duration of these meetings.

## YouTrack

The tool [YouTrack](https://www.jetbrains.com/youtrack/) was used to organize activities, manage the backlog, allocate
tasks, and track additional project materials. YouTrack provides numerous features to support agile development: the
following two were intensively used.

### Knowledge Base

The _Knowledge Base_ allows the creation of hypertext articles to form a publicly available knowledge repository. It was
used to maintain important textual artifacts that documented crucial decisions about various project aspects. For
instance, the **Glossary** contained a list of significant terms and entities relevant to the design phase.

### Agile Board

The _Agile board_ facilitated the organization, visualization, and tracking of tasks assigned to group members (
i.e., _tickets_). Four columns were set up: Open, In Progress, To Be Checked, and Completed. Each ticket was labeled
with one of the following categories:

* Configuration
* Test
* Enhancement
* Report
* Design
* Development
* Refactoring
* Integration
* Fix
* Documentation

During each sprint planning session, tickets for the upcoming week were created and mostly assigned to group members,
though some tickets remained unassigned for flexibility. Members would later assign these tasks to themselves as needed.

## Version Control (DVCS)

_Git_ was used in conjunction with _GitHub_, following the **Gitflow** model and
using [semantic commits](https://www.conventionalcommits.org/en/v1.0.0/). Two main branches were maintained: `main` for
stable releases and code, and `develop` for sufficiently tested development code.

New branches were created following the Gitflow nomenclature, such as `feature:<FEATURE_NAME>`, `fix:<FIXED_NAME>`,
and `test:<TESTED_NAME>`.

## CI/CD

[GitHub Actions](https://github.com/features/actions) were employed to automate deployment and code verification. These
actions included:

* Automatic deployment of documentation to [GitHub Pages](https://pages.github.com/).
* Automatic code testing on pull requests.
* Automatic releases with attached artifacts upon tagging the `main` branch.

## Testing

A _Test Driven Development_ (TDD) approach was followed to ensure robust development. This methodology involves defining
and implementing a test before developing the actual feature, leading to the creation of comprehensive tests for a
significant portion of the code.

Apart from trivial features, the process included **defining and implementing a test**, then **developing the desired
feature** until the test(s) passed, and finally **refactoring** to improve the written code.


### Testing on Pull Requests

The GitHub action for automatic testing was configured to trigger on any pull request. However, **successful completion
of all tests was required only for pull requests to the develop branch**. This additional constraint ensured more
rigorous development and greater attention to the implemented tests.

## Build

We used [SBT](https://www.scala-sbt.org/) (_Simple Build Tool_) for build automation. SBT facilitated easy dependency
management, compilation, test execution, and artifact creation, such as generating fat JAR files.

## Documentation

The entire project's code is documented using _scaladoc_, the standard documentation tool for Scala projects.

Both the project documentation and this report are written in Markdown format and deployed
using [Writerside](https://www.jetbrains.com/writerside/), a tool that helps create clear and organized documentation.

## Additional Tools

Several additional tools were utilized in this project:

### Cucumber

[Cucumber](https://cucumber.io/) is a tool for **Behavior Driven Development** (BDD). Its integration into the project
testing allowed for the implementation of test step definitions using feature files. This made it an excellent tool for
validating project requirements written in natural language using the Gherkin syntax.

### Scalafmt

[Scalafmt](https://scalameta.org/scalafmt/) is a code formatter for Scala. It helped reduce the time spent on code
reviewing and cleaning.

## Testing

A _Test Driven Development_ (TDD) approach was followed to ensure robust development. This methodology involves defining
and implementing a test before developing the actual feature, leading to the creation of comprehensive tests for a
significant portion of the code.

Apart from trivial features, the process included **defining and implementing a test**, then **developing the desired
feature**
until the test(s) passed, and finally **refactoring** to improve the written code.
