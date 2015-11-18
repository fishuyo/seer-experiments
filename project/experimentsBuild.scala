import sbt._

import Keys._


object ExperimentsBuild extends Build {

  import SeerSettings._
  import SeerBuild._
  import SeerModulesBuild._

  lazy val experiments = project.in(file("experiments")).
    settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni, seer_opencv, tree)

  lazy val fieldViewer = project.in(file("experiments/fieldViewer")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app)

  lazy val particle = project.in(file("experiments/particle")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app)

  // lazy val kinect = project.settings(app: _*).
  //   dependsOn(seer_gdx_desktop_app, seer_kinect)

  lazy val opencv = project.in(file("experiments/opencv")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_opencv, seer_osx_multitouch)

  lazy val video = project.in(file("experiments/video")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_video)

  lazy val openni = project.in(file("experiments/openni")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_openni)

  lazy val morea = project.in(file("experiments/morea")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch)

  lazy val tree = project.in(file("experiments/tree")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni)

  lazy val box2d = project.in(file("experiments/box2d")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_box2d, seer_script)

  lazy val pie = project.in(file("experiments/pie")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni, seer_video, seer_opencv, seer_script)

  lazy val dailyworlds = project.settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni, seer_video, seer_opencv, seer_script)
}


