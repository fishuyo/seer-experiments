
package com.fishuyo.seer

import graphics._
import spatial._
import particle._
import io._
import util._
import dynamic._

import de.sciss.osc.Message

import collection.mutable.ArrayBuffer

// class Player extends Particle with Animatable {
// 	val model = Cube().scale(0.01,0.05,0.01)

// 	override def draw() = model.draw
// 	override def animate(dt:Float) = {
// 		step()
// 		model.pose.pos.set(position)
// 	}
// }

// class Moon(val r:Float) extends Particle with Animatable {

// 	val model = Sphere()
// 	model.material = Material.specular

// 	override def draw() = model.draw
// 	override def animate(dt:Float) = {
// 		step()
// 		model.pose.pos.set(position)
// 	}

// 	// def collide(m:Moon){

// 	// 	if( m.model.scale )
// 	// }

// 	def applyGravity( m:Particle ){
// 		val x = m.position - position
// 		val r2 = x.magSq
// 		if( r2 > r){
// 			val f = 0.001 * mass*m.mass / r2
// 			applyForce( x.normalized * f )
// 		}
// 	}
// }

object Moonjump extends SeerApp {

	val live = ScriptLoader("scripts/moonjump.scala")

	// val players = ArrayBuffer[Player]()
	// val moons = ArrayBuffer[Moon]()
	// for (i <- 0 until 7){
	// 	val s = Random.float()*10.0f + 1.0f
	// 	val m = new Moon(0.01*s)
	// 	m.position.set(Random.vec3()*1)
	// 	m.lPosition.set(m.position - Random.vec3()*0.01)
	// 	m.mass = s
	// 	m.model.scale(0.01*s)
	// 	moons += m
	// }

	override def draw(){
		// moons.foreach( _.draw )
	}
	override def animate(dt:Float){
		// moons.foreach { case m => moons.foreach { case n => m.applyGravity(n) }}
		// moons.foreach( _.animate(dt) )
	}

	// OSC.listen(8082)
	// OSC.bindp {
		// case Message("/slider", f:Float, i:Int) => moons(i).position.y = f //println(p + " " + f)
		// case _ => ()
	// }

}