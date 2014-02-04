import spray.routing.SimpleRoutingApp

import akka.actor._

object UI1 extends App with SimpleRoutingApp {

  implicit val system = ActorSystem("ui1")

  startServer(interface = "localhost", port = 8090) {
    pathEndOrSingleSlash {
      get {
        dynamic {
          println("Getting from resource")
          getFromResource("main.js")
        }
      }
    } ~
      path("hi") {
        complete("you")
      }
  }

}
