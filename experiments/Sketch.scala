
package com.fishuyo.seer

import dynamic._

/**
  * This example sets up a SeerScriptLoader which compiles
  * and runs the scala script on modification.
  * 
  * See "./scripts/live.scala"
  * 
  */
object Sketch extends SeerApp {
  val live = ScriptManager.load("scripts/sketch.scala")

  override def draw(){}
  override def animate(dt:Float){}
}