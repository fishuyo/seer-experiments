
package com.fishuyo.seer
package tuningtree

import graphics._
import dynamic._
import audio._

object TuningTree extends SeerApp {

  val tree = new ATree()


  var looper = new Looper
  var l = 0

  GdxAudio.init
  // PortAudio.init
  Audio().push( looper )
  Scene.push( looper )
  // looper.newLoop
  looper.init

  val script = ScriptLoader("scripts/live.scala")

  Audio().start


  override def draw() = tree.draw()
  override def animate(dt:Float) = tree.animate(dt)

}