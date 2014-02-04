import spray.routing.SimpleRoutingApp

object UI1 extends App with SimpleRoutingApp {

  implicit val system = ActorSystem("ui1")

  startServer(interface = "localhost", port = 8090) {
    path("hello") {
      get {
        complete {
          <h1>Say hello to spray</h1>
        }
      }
    }
  }

}
