
import com.fishuyo.seer._

import graphics._
import spatial._
import particle._
import io._
import util._
import dynamic._

import de.sciss.osc.Message

import collection.mutable.ArrayBuffer

class SParticle(val r:Float) extends KinematicState with RotationalState {
	def step(){
		Integrators.verlet(this)
		Integrators.rotationalVerlet(this)
	}

	def applyGravity( m:SParticle ){
		val x = m.position - position
		val r2 = x.magSq

		// val q = m.orientation * orientation.inverse
		// val r2 = q.magSq
		if( r2 > r){
			val f = 0.01 * mass*m.mass / r2 //- spin.mag * 0.0001
			// val f = mass*m.mass / r2
			applyForce( x.normalized * f )
			// applyTorque( (orientation.slerp(m.orientation, 0.0001)*orientation.inverse * f) )
			// orientation.slerpTo(m.orientation, f)
		}
	}
}

class Entity(override val r:Float) extends SParticle(r) with Animatable {
	val model = Sphere()
	model.material = Material.specular

	override def draw() = model.draw
	override def animate(dt:Float) = {
		step()
		// model.pose.pos.set(position)
		// position = orientation.toZ() * 3.f
		// lPosition = position
		position.set( position.normalized * 3.f)
		model.pose.pos.set(position)
	}
}

class Player extends Entity(0.05) {
	model.scale(0.1)
	model.material.color = RGB.red
	position.set(0,0,-3)
	lPosition.set(0,0,-3)
}

class Moon(override val r:Float) extends Entity(r) {
	// override val model = Sphere()
}


object Script extends SeerScript {
	
	Camera.nav.pos.set(0,0,0)

	val players = ArrayBuffer[Player]()
	val moons = ArrayBuffer[Moon]()
	var thrust = Vec2()

	val thrustMesh = new Mesh()
	thrustMesh.primitive = Lines
	thrustMesh.vertices += Vec3()
	thrustMesh.vertices += Vec3()
	val thrustModel = Model(thrustMesh)
	
	players += new Player()

	for (i <- 0 until 0){
	
		val s = Random.float()*10.f + 1.f
		val m = new Moon(0.01*s)
		m.position.set(Random.vec3().normalized * 3.f)
		m.lPosition.set(m.position - Random.vec3()*0.01)
		m.mass = s
		
		// m.orientation.set(Random.quat())
		// m.lOrientation.set(m.orientation) //*Quat(0,0.01,0))
		// m.inertia = s

		m.model.scale(0.01*s)
		moons += m
	}

	override def preUnload(){
		OSC.disconnect()
	}

	override def draw(){
		moons.foreach( _.draw )
		players.foreach( _.draw )
		thrustModel.draw
	}
	override def animate(dt:Float){
		players.foreach { case p => 
			moons.foreach { case n => p.applyGravity(n) }
			// p.applyAngularDamping(.01f)
		}
		moons.foreach { case m => 
			moons.foreach { case n => m.applyGravity(n) }
		}
		moons.foreach( _.animate(dt) )
		players.foreach( _.animate(dt) )

		val pos = players(0).position
		val tx = -(pos cross Vec3(0,1,0)).normalize
		val ty = -(pos cross Vec3(1,0,0)).normalize
		val t = (tx * thrust.x + ty * thrust.y)
		thrust.zero
		players(0).applyForce(t*0.1)

		thrustMesh.vertices(1).set(-t)
		thrustMesh.update
		thrustModel.pose.pos.set(players(0).position)
	}

	OSC.listen(8082)
	OSC.bindp {
		case Message("/slider", f:Float, i:Int) => moons(i).position.y = f //println(p + " " + f)
		case _ => ()
	}

	Trackpad.clear
	Trackpad.connect
	Trackpad.bind( (i:Int, f:Array[Float]) => {
		i match {
			case 2 =>
				val x = f(0)*2-1
				val y = f(1)*2-1
				thrust.set(x,y)
				// players(0).applyForce(Vec3(x,y,0)*0.1)
				// players(0).applyTorque(Vec3(-y,x,0)*0.00001)
			case _ => ()
		}
	})

}

Script