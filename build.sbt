import com.typesafe.web.sbt.WebPlugin
import com.typesafe.jse.sbt.JsEnginePlugin
import com.typesafe.jse.sbt.JsEnginePlugin.JsEngineKeys
import com.typesafe.jshint.sbt.JSHintPlugin
import com.typesafe.jshint.sbt.JSHintPlugin.JshintKeys

val jsHintConfig = file("common/.jshintrc")

val uiSettings =
  WebPlugin.webSettings ++
    JsEnginePlugin.jsEngineSettings ++
    JSHintPlugin.jshintSettings ++ Seq(
      JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
      JshintKeys.config := Some(jsHintConfig),
      resolvers += "spray repo" at "http://repo.spray.io",
      resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies += "io.spray" % "spray-routing" % "1.2.0",
      libraryDependencies += "io.spray" % "spray-can" % "1.2.0",
      libraryDependencies += "com.typesafe.akka" %% s"akka-actor" % "2.2.3"
    )

name := "UI Wrapper Project"

lazy val common = project.in(file("common"))
  .settings(uiSettings: _*)

lazy val ui1 = project.in(file("ui1"))
  .settings(uiSettings: _*)
  .dependsOn(common)

lazy val ui2 = project.in(file("ui2"))
  .settings(uiSettings: _*)
  .dependsOn(common)
