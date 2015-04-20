
package com.fishuyo.seer
package experiments

import dynamic._ 
import audio._

object Loader extends SeerApp {
  GdxAudio.init
  Audio().start

  // val live = ScriptLoader("scripts/test.scala")
  val live = ScriptLoader("scripts/liquidSkel.scala")
  // val live = ScriptLoader("scripts/tree.scala")

  override def draw(){}
  override def animate(dt:Float){}
}