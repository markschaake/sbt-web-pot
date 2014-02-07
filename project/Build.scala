import sbt._
import Keys._

import com.typesafe.web.sbt.WebPlugin
import com.typesafe.web.sbt.WebPlugin.WebKeys
import com.typesafe.jse.sbt.JsEnginePlugin
import com.typesafe.jse.sbt.JsEnginePlugin.JsEngineKeys
import com.typesafe.jshint.sbt.JSHintPlugin
import com.typesafe.jshint.sbt.JSHintPlugin.JshintKeys
import spray.revolver.RevolverPlugin._

object WebBuild extends Build {

  val jsHintConfig = file("common/.jshintrc")

  val serverSettings = Revolver.settings ++ Seq(
    resolvers += "spray repo" at "http://repo.spray.io",
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    libraryDependencies += "io.spray" % "spray-routing" % "1.2.0",
    libraryDependencies += "io.spray" % "spray-can" % "1.2.0",
    libraryDependencies += "com.typesafe.akka" %% s"akka-actor" % "2.2.3"
  )

  val uiSettings =
    WebPlugin.webSettings ++
      JsEnginePlugin.jsEngineSettings ++
      JSHintPlugin.jshintSettings ++ Seq(
        JsEngineKeys.engineType := JsEngineKeys.EngineType.Node)

  /** Helper that walks the directory tree and returs list of files only */
  private def filesOnly(source: File): Seq[File] =
    if (!source.isDirectory) source :: Nil
    else Option(source.listFiles) match {
      case None => Nil
      case Some(files) => files flatMap filesOnly
    }

  // Settings that:
  // 1. Copy over generated common UI elements in resource managed
  // 2. Copy generated UI files for this project into resource managed
  lazy val commonUiUserSettings = uiSettings ++ Seq(
    // Copy over common UI files into managed resources
    resourceGenerators in Compile <+=  Def.task {
      val baseDirectories = (resourceManaged in common in WebKeys.Assets).value :: Nil
      val newBase = (resourceManaged in Compile).value / "public" / "common"
      val sourceFiles = baseDirectories flatMap filesOnly
      val mappings = sourceFiles pair rebase(baseDirectories, newBase)
      IO.copy(mappings, true).toSeq
    },
    // Copy over target/public files into managed resources
    resourceGenerators in Compile <+= Def.task {
      val baseDirectories = (target in Compile).value / "public" :: Nil
      val newBase = (resourceManaged in Compile).value / "public"
      val sourceFiles = baseDirectories flatMap filesOnly
      val mappings = sourceFiles pair rebase(baseDirectories, newBase)
      IO.copy(mappings, true).toSeq
    },
    // watch sources in common project to hot-reload
    watchTransitiveSources <++= Def.task {
      (unmanagedSources in common in WebKeys.Assets).value ++
        (unmanagedResources in common in WebKeys.Assets).value
    },
    (compile in Compile) <<= (compile in Compile).dependsOn(compile in Compile in common)
  )

  lazy val root = project.in(file(".")).aggregate(ui1, ui2).settings(
    name := "sbt-web-pot"
  )

  lazy val common = project.in(file("common"))
    .settings(uiSettings: _*)
    .settings(
      // Force copyResources on each compile - which puts compiled assets into common/target/public
      (compile in Compile) <<= (compile in Compile).dependsOn(copyResources in WebKeys.Assets)
    )

  lazy val ui1 = project.in(file("ui1"))
    .settings(serverSettings: _*)
    .settings(commonUiUserSettings: _*)

  lazy val ui2 = project.in(file("ui2"))
    .dependsOn(common)
    .settings(commonUiUserSettings: _*)
    .settings(serverSettings: _*)
}
