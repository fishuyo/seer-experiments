
import com.fishuyo.seer._

import allosphere._
import allosphere.actor._

import graphics._
import dynamic._
import spatial._
import io._
import util._
import particle._

import allosphere.livecluster.Node

import collection.mutable.ArrayBuffer

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator

import ClusterSystem.{ system, system10g }
// import ClusterSystem.{ test1 => system, test1_10g => system10g }

var vel = Vec2()
var ray = Camera.ray(0,0)

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
    // position = orientation.toZ() * 3.0f
    // lPosition = position
    position.lerpTo( position.normalized * 3.0f, 0.01)
    // position.set( position.normalized * 3.0f)
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

  Camera.nav.pos.set(0,0,0)


object Script extends SeerScript {
  

  val players = ArrayBuffer[Player]()
  val moons = ArrayBuffer[Moon]()
  var thrust = Vec2()

  val thrustMesh = new Mesh()
  thrustMesh.primitive = Lines
  thrustMesh.vertices += Vec3()
  thrustMesh.vertices += Vec3()
  val thrustModel = Model(thrustMesh)
  
  players += new Player()

  for (i <- 0 until 500){
  
    val s = Random.float()*10.0f + 1.0f
    val m = new Moon(0.01*s)
    m.position.set(Random.vec3().normalized * 3.0f)
    m.lPosition.set(m.position - Random.vec3()*0.01)
    m.mass = s
    
    // m.orientation.set(Random.quat())
    // m.lOrientation.set(m.orientation) //*Quat(0,0.01,0))
    // m.inertia = s

    m.model.scale(0.01*s)
    moons += m
  }

  override def preUnload(){
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
}

object SimulatorScript extends SeerScript{
  
  var t = 0.0f

  val controllerStateListener:ActorRef = system.actorOf(Props( new ControllerListener()), name = "controllerlistener")
  var statePublisher:ActorRef = system10g.actorOf(Props( new StatePublisher()), name = "statepublisher")



  override def preUnload(){
    controllerStateListener ! PoisonPill
    statePublisher ! PoisonPill
  }

  override def animate(dt:Float){ 
    Script.animate(dt)  
    t += dt

    statePublisher ! Script.moons.map(_.position)
    statePublisher ! Camera.nav
    statePublisher ! t
  }
}



class StatePublisher extends Actor {
  import DistributedPubSubMediator.Publish
  val mediator = DistributedPubSubExtension(system10g).mediator
 
  // val a = new Array[Float](30000)
  def receive = {
    case f:Float =>
      mediator ! Publish("state", f)
    case n:Nav => 
      mediator ! Publish("state", Array(n.pos.x,n.pos.y,n.pos.z,n.quat.w,n.quat.x,n.quat.y,n.quat.z) )
    case m:ArrayBuffer[Vec3] =>
      val verts = m.flatMap( (p) => { List(p.x,p.y,p.z) })
      mediator ! Publish("state", verts.toArray)
      // mediator ! Publish("state", m.vertices.flatMap( (v) => List(v.x,v.y,v.z)).toArray )
  }
}

class ControllerListener extends Actor {

  import DistributedPubSubMediator.{ Subscribe, SubscribeAck }
  val mediator = DistributedPubSubExtension(system).mediator

  mediator ! Subscribe("controllerState", self)
 
  def receive = {
    case SubscribeAck(Subscribe("controllerState", None, `self`)) ⇒
      context become ready
  }
 
  def ready: Actor.Receive = {
    case a:Array[Float] if a.length == 7 => 
      Camera.nav.pos.set(a(0),a(1),a(2))
      Camera.nav.quat.set(a(3),a(4),a(5),a(6))

    case a:Array[Float] if a.length == 8 =>
      vel.set(a(0),a(1))
      ray.o.set(a(2),a(3),a(4))
      ray.d.set(a(5),a(6),a(7))

  }
}




SimulatorScript
