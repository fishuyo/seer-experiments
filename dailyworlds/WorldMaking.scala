
package com.fishuyo.seer
package dailyworlds

import graphics._
import dynamic._
import io._
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

  var indx = 0
  val dir = new java.io.File("scripts")
  val scripts = dir.listFiles().filter(_.getName.endsWith(".scala"))
  var actor = ScriptManager.load(scripts(indx).getPath)

  def next(){
    indx += 1
    if(indx > scripts.length) indx = 0
    actor ! "unload"
    actor ! akka.actor.PoisonPill
    RootNode.reset()
    actor = ScriptManager.load(scripts(indx).getPath)
  }
  def prev(){
    indx -= 1
    if(indx < 0) indx = scripts.length-1
    actor ! "unload"
    actor ! akka.actor.PoisonPill
    RootNode.reset()
    actor = ScriptManager.load(scripts(indx).getPath)
  }

  val keyboard = Keyboard()
  keyboard.bind("i",next)
  keyboard.bind("u",prev)
  
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
  // val live = ScriptLoader("scripts/090115.scala")
  // val live = ScriptLoader("scripts/orthotest.scala")
  // val live = ScriptLoader("scripts/091915_ccl5.scala")

  // override def draw(){}
  // override def animate(dt:Float){}
}