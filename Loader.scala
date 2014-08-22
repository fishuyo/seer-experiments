
package com.fishuyo.seer
package experiments

import com.fishuyo.seer.dynamic._ 

object Loader extends SeerApp {

  val live = new SeerScriptLoader("scripts/loader.scala")

  override def draw(){}
  override def animate(dt:Float){}
}