import spray.routing.SimpleRoutingApp

object UI2 extends App with SimpleRoutingApp {

  implicit val system = ActorSystem("ui2")

  startServer(interface = "localhost", port = 8091) {
    path("hello") {
      get {
        complete {
          <h1>Say hello to spray</h1>
        }
      }
    }
  }

}
