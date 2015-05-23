
package com.fishuyo.seer
package methetree

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
  
  // val live = ScriptLoader("scripts/live.scala")
  // val live = ScriptLoader("scripts/oldtree.scala")
  val live = ScriptLoader("scripts/treerootjoints.scala")

  override def draw(){}
  override def animate(dt:Float){}
}