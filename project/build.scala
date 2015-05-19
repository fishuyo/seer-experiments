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
