import sbt._

import Keys._


object ExperimentsBuild extends Build {

  import SeerBuildSettings._
	import SeerBuild._
  import SeerModulesBuild._

  lazy val experiments = project.in(file(".")).
    settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni, seer_opencv, tree)

  lazy val fieldViewer = project.settings(app: _*).
    dependsOn(seer_gdx_desktop_app)

  lazy val particle = project.settings(app: _*).
    dependsOn(seer_gdx_desktop_app)

  // lazy val kinect = project.settings(app: _*).
  //   dependsOn(seer_gdx_desktop_app, seer_kinect)

  lazy val opencv = project.settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_opencv, seer_osx_multitouch)

  lazy val video = project.settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_video)

  lazy val openni = project.settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_openni)

  lazy val morea = project.settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch)

  lazy val tree = project.settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni)

  lazy val methetree = project.settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni)

  lazy val dailyworlds = project.settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni)

}



import org.scalajs.sbtplugin.ScalaJSPlugin
import ScalaJSPlugin._
import autoImport._

import spray.revolver.RevolverPlugin._

object ChatBuild extends Build {

  import SeerBuildSettings._

  lazy val seer_server =
    Project("seer_server", file("seer-server"))
      .aggregate(frontend, backend)

  // Scala-Js frontend
  lazy val frontend =
    Project("frontend", file("seer-server/frontend"))
      .enablePlugins(ScalaJSPlugin)
      .settings(commonSettings: _*)
      .settings(
        persistLauncher in Compile := true,
        persistLauncher in Test := false,
        testFrameworks += new TestFramework("utest.runner.Framework"),
        libraryDependencies ++= Seq(
          "org.scala-js" %%% "scalajs-dom" % "0.8.0",
          "com.lihaoyi" %%% "utest" % "0.3.0" % "test"
        )
      )

  // Akka Http based backend
  lazy val backend =
    Project("backend", file("seer-server/backend"))
      .settings(Revolver.settings: _*)
      .settings(common: _*)
      .settings(
        libraryDependencies ++= Seq(
          "com.typesafe.akka" %% "akka-http-scala-experimental" % "1.0-RC2",
          "org.specs2" %% "specs2" % "2.3.12" % "test"
        ),
        (resourceGenerators in Compile) <+=
          (fastOptJS in Compile in frontend, packageScalaJSLauncher in Compile in frontend)
            .map((f1, f2) => Seq(f1.data, f2.data)),
        watchSources <++= (watchSources in frontend)
      )

  def commonSettings = Seq(
    scalaVersion := "2.11.6"
  )
}
