
package com.fishuyo.seer
package examples.audio

import dynamic._
import audio._

/**
  * This example sets up a SeerScriptLoader which compiles
  * and runs the scala script on modification.
  * 
  * See "./scripts/live.scala"
  * 
  */
object LiveScript extends SeerApp {

  GdxAudio.init
  Audio().start

  val live = ScriptManager.load("scripts/live.scala")

  override def draw(){}
  override def animate(dt:Float){}
}