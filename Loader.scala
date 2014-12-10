
package com.fishuyo.seer
package experiments

import com.fishuyo.seer.dynamic._ 

object Loader extends SeerApp {

  val live = ScriptLoader("scripts/openni.scala")

  override def draw(){}
  override def animate(dt:Float){}
}