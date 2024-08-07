import org.unibo.scooby.dsl.ScoobyApplication
import scala.concurrent.duration.DurationInt

object MyObject extends ScoobyApplication:

  scooby:
    config:
      network:
        Timeout is 9.seconds
        MaxRequests is 10
        headers:
          "User-Agent" to "Scooby/1.0-alpha (https://github.com/PPS-22-Scooby/PPS-22-Scooby)"
      options:
        MaxDepth is 2
        MaxLinks is 20

    crawl:
      url:
        "https://www.myTestUrl.com"
      policy:
        hyperlinks not external
    scrape:
      elements
    exports:
      batch:
        strategy:
          results get(el => (el.tag, el.text)) output:
            toFile("test.json") withFormat json
        aggregate:
          _ ++ _
      streaming:
        results get tag output:
          toConsole withFormat text