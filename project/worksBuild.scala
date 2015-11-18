import sbt._

import Keys._


object WorksBuild extends Build {

  import SeerSettings._
  import SeerBuild._
  import SeerModulesBuild._

  lazy val kodama = project.in(file("works/kodama")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni, seer_opencv, seer_script)

  lazy val methetree = project.in(file("works/methetree")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni, seer_script)

  lazy val quantumlove = project.in(file("works/quantumlove")).settings(app: _*).
    dependsOn(seer_gdx_desktop_app, seer_osx_multitouch, seer_openni, seer_video, seer_opencv, seer_script)
}


