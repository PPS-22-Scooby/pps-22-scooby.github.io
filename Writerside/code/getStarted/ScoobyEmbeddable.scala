class MyClass extends ScoobyEmbeddable:

  val app: ScoobyRunnable[?] = scooby:
    ...

  val result: Result[?] = Await.result(app.run(), Duration.Inf)