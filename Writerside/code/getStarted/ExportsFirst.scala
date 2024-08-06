exports:
  batch:
    strategy:
      results get(el => (el.tag, el.text)) output:
        toFile("testJson.txt") withFormat json

    aggregate:
      _ ++ _
  batch:
    strategy:
      results get(el => (el.tag, el.text)) output:
        toFile("testText.txt") withFormat text

    aggregate:
      _ ++ _
  streaming:
    results get tag output:
      toConsole withFormat text