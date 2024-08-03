# DSL

A Domain-Specific Language (DSL) has been developed as an alternative to a standard API for configuring the application.

Adhering to design principles, the DSL is implemented **on top of the existing, functional API**. This ensures that the
system remains _independent_ of the DSL's design and implementation.

The DSL is organized into modules, each representing a different aspect of the application's configuration. There are
four modules: Config, Crawl, Scrape, and Export. Each module is divided into two parts: `Context` and `Ops`.

Here is an example within the Config module:

```plantuml
@startuml
package Config {
    class ConfigContext<<(O, #906901) Object>> {}
    class ConfigOps<<(O, #906901) Object>> {
        network: Unit
        headers: Unit
        option: Unit
    }

    class NetworkConfigurationContext
    class HeadersContext
    class OptionConfigurationContext

    ConfigOps ..> ConfigContext: <<uses>>
    ConfigContext o-- NetworkConfigurationContext
    ConfigContext o-- HeadersContext
    ConfigContext o-- OptionConfigurationContext
}
hide empty members
@enduml
```

`Contexts` represent the **scopes** within the DSL. For instance, in the following snippet:

```Scala
scooby:
    scrape:
        elements
```

Both `scooby` and `scrape` define `Contexts`.

`Ops`, on the other hand, are the **"keywords"** of the language. In the above snippet, all three
words (`scooby`, `scrape`, and `elements`) are considered `Ops`.

This design allows the restriction of valid `Ops` to specific `Contexts`. The actual mechanism to enforce this is an
implementation detail.

## Language Specification

The primary part of the DSL specification is represented in the following BNF code snippet:

**Note:** Since the language is implemented as an _internal_ DSL, there are many valid programs beyond this
specification. Thus, the provided specification is only partial.

```BNF
<program> ::= <scooby-block>

<scooby-block> ::= "scooby:" <config-block>? <crawl-block> <scrape-block>? <exports-block>?

<config-block> ::= "config:" <network-block>? <option-block>?
<network-block> ::= "network:" <network-setting>+
<network-setting> ::= "NetworkTimeout is" <time>
                    | "MaxRequests is" <number>
                    | "headers:" <header-setting>+
<header-setting> ::= <string> "to" <string>
<option-block> ::= "option:" <option-setting>+
<option-setting> ::= "MaxDepth is" <number>
                    | "MaxLinks is" <number>

<crawl-block> ::= "crawl:" <url-block> <policy-block>?
<url-block> ::= "url:" <string>
<policy-block> ::= "policy:" <policy-type>
<policy-type> ::= "hyperlinks" <not-external>?
                 | "allLinks" <not-external>?
<not-external> ::= "not external"

<scrape-block> ::= "scrape:" <element-selection>
<element-selection> ::= "elements"
                      | "elements that" <condition>
                      | "elements that:" <condition-block>
                      
<condition> ::= "(" <condition-expr> ")"
<condition-expr> ::= <simple-condition> ( "and" <simple-condition> )*
<simple-condition> ::= "haveTag(" <string> ")"
                     | "haveClass(" <string> ")"
                     | "haveId(" <string> ")"
                     | "haveAttribute(" <string> ")"
                     | "haveAttributeValue(" <string> ")"
                     | "followRule" "{" <rule> "}"
<condition-block> ::= <rule-condition> ( "and" <rule-condition> )*
<rule-condition> ::= "followRule" "{" <rule> "}"

(* The following one are just two examples, the actual rule grammar is
 dependent on the host language *)

<rule> ::= "element" <element-expr>

<element-expr> ::= ".attr(" <string> ") == " <string>
                  | ".text == " <string>
                  | ".tag == " <string>
                  | ".id == " <string>
                  | ".parent" <element-expr>

<exports-block> ::= "exports:" <export-type-block>+
<export-type-block> ::= "batch:" <batch-strategy-block> <aggregate-block>?
                      | "streaming:" <result-strategy>
<batch-strategy-block> ::= "strategy:" <result-strategy>

(* The following one are just some examples, the actual result strategy
grammar is dependent on the host language *)
 
<result-strategy> ::= <result-expr> <output-destination>
<result-expr> ::= "results" ( "get" <elem-property> )?
<elem-property> ::= "tag" | "id" | "attr(" <string> ")" | "text" | "outerHtml"
<aggregate-block> ::= "aggregate:" <aggregation>
<aggregation> ::= "_ ++ _"
<output-destination> ::= "toFile(" <string> ")" "withFormat" <format>
                       | "toConsole" "withFormat" <format>
<format> ::= "json"
           | "text"

<time> ::= <number> "." <unit>
<unit> ::= "seconds"
<number> ::= [0-9]+
<string> ::= "\"" [^"]* "\""
```

## Examples of Usage

Here are some examples of valid programs written in this DSL.

### Example 1: Full Settings Provided

This snippet demonstrates the use of all available settings, providing a comprehensive example of a Scooby DSL program.

```Scala
scooby:
    config:
        network:
            NetworkTimeout is 5.seconds
            MaxRequests is 100
            headers:
                "User-Agent" to "Scooby/1.0"
        option:
            MaxDepth is 2
            MaxLinks is 100
            
    crawl:
        url:
            "https://www.example.com/"
        policy:
            hyperlinks not external
            
    scrape: 
        elements
        
    exports:
        batch:
            strategy:
                results get tag output:
                    toFile("test.json") withFormat json
            aggregate:
                _ ++ _
```

This program is designed to crawl the URL `"https://www.example.com/"`, recursively visiting all found _hyperlinks_ that
do not redirect to external domains.

For each page, all HTML elements are scraped and their HTML tags are exported to a file named `test.json` in JSON
format.

### Example 2: Scrape and Export

This snippet includes only the `scrape` and `exports` sections, as well as the mandatory `crawl` section to set the root
URL.

```Scala
scooby:
    crawl:
        url:
            "https://www.example.com/"
    scrape:
        elements that (haveTag("a") and haveClass("gorgeous"))
    exports:
        streaming:
            results output:
                toConsole withFormat text
```

This program is designed to crawl the URL `"https://www.example.com/"`, recursively visiting all found _hyperlinks_ (
default behavior).

For each page crawled, the scraping will focus only on HTML elements with the tag `"a"` and the class
attribute `"gorgeous"`. Each of these elements' outer HTML will be exported in text format and printed to the console.

### Example 3: More Advanced

This snippet uses the DSL in a more advanced and less "pure" form, fully leveraging the advantages of an internal
implementation.

```Scala
scooby:
    crawl:
      url:
        "https://www.example.com"
      policy:
        allLinks that (((url: URL) => url.isRelative) and 
            (_.toString.endsWith("example")))
        
    scrape:
      elements that:
        followRule { element.parent.text.nonEmpty } and
          followRule { element.attr("alt").nonEmpty }

    exports:
      batch:
        strategy:
          results.groupMapReduce(_.tag)(_ => 1)(_ + _) output:
            toFile("test.json") withFormat json

        aggregate:
          _ ++ _
```

This program crawls the URL `"https://www.example.com/"`, recursively visiting all _links_ that are **relative URLs**
and whose text ends with "example".

For each page crawled, the scraping focuses on HTML elements whose parent's text is not empty and that contain a
non-empty "alt" attribute.

The export process outputs key-value pairs to a file named `test.json` in JSON format, where the keys are the HTML tags
of the scraped elements and the values are the number of occurrences of each tag.