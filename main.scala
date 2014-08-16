
package com.fishuyo.seer
package template

import com.fishuyo.seer.dynamic._ 

object Main extends SeerApp {

  val live = new SeerScriptLoader("live.scala")

  override def draw(){}
  override def animate(dt:Float){}
}