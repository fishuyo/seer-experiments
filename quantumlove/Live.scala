
package com.fishuyo.seer
package quantumlove

import dynamic._
import audio._

/**
  * This example sets up a SeerScriptLoader which compiles
  * and runs the scala script on modification.
  * 
  * See "./scripts/live.scala"
  * 
  */
object WorldScript extends SeerApp {
  GdxAudio.init
  Audio().start
  
  // val live = ScriptLoader("scripts/UserMask.scala")
  val live = ScriptLoader("scripts/videomasktest.scala")

  override def draw(){}
  override def animate(dt:Float){}
}