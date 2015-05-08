
package com.fishuyo.seer
package dailyworlds

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
  
  val live = ScriptLoader("scripts/050315.scala")

  override def draw(){}
  override def animate(dt:Float){}
}