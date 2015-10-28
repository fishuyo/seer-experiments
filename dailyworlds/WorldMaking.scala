
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
  
  // val live = ScriptLoader("scripts/042015.scala")
  // val live = ScriptLoader("scripts/042115_2.scala")
  // val live = ScriptLoader("scripts/042915.scala")
  // val live = ScriptLoader("scripts/043015.scala")
  // val live = ScriptLoader("scripts/050315.scala")
  // val live = ScriptLoader("scripts/052015.scala")
  // val live = ScriptLoader("scripts/052415.scala")
  // val live = ScriptLoader("scripts/052615.scala")
  // val live = ScriptLoader("scripts/060215.scala")
  // val live = ScriptLoader("scripts/052315.scala")
  // val live = ScriptLoader("scripts/050315.scala")
  // val live = ScriptLoader("scripts/073015.scala")
  // val live = ScriptLoader("scripts/082915_2.scala")
  // val live = ScriptLoader("scripts/083015.scala")
  val live = ScriptLoader("scripts/090115.scala")
  // val live = ScriptLoader("scripts/orthotest.scala")
  // val live = ScriptLoader("scripts/091915_ccl5.scala")

  override def draw(){}
  override def animate(dt:Float){}
}