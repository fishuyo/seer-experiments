import sbt._

import Keys._


object ExperimentsBuild extends Build {

	import SeerBuild._

  lazy val aaa_experiments = SeerProject(
  	id = "experiments",
  	base = file("."),
  	settings = BuildSettings.app
  ) dependsOn( seer_gdx_desktop_app, seer_multitouch, seer_openni, seer_opencv )
  

  lazy val experiments_fieldViewer = SeerProject (
    "experiments-fieldViewer",
    file("fieldViewer"),
    settings = BuildSettings.app
  ) dependsOn seer_gdx_desktop_app

  lazy val experiments_particle = SeerProject (
    "experiments-particle",
    file("particle"),
    settings = BuildSettings.app
  ) dependsOn( seer_gdx_desktop_app )

  lazy val experiments_kinect = SeerProject (
    "experiments-kinect",
    file("kinect"),
    settings = BuildSettings.app
  ) dependsOn( seer_gdx_desktop_app, seer_kinect )

  lazy val experiments_opencv = SeerProject (
    "experiments-opencv",
    file("opencv"),
    settings = BuildSettings.app
  ) dependsOn( seer_gdx_desktop_app, seer_opencv, seer_jruby, seer_multitouch )

  lazy val experiments_video = SeerProject (
    "experiments-video",
    file("video"),
    settings = BuildSettings.app
  ) dependsOn( seer_gdx_desktop_app, seer_video )

  lazy val experiments_openni = SeerProject (
    "experiments-openni",
    file("openni"),
    settings = BuildSettings.app
  ) dependsOn( seer_gdx_desktop_app, seer_multitouch, seer_video )

}
