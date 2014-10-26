
package com.fishuyo.seer
package examples.bullet

import graphics._
import spatial._

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.physics.bullet._
import collision._
import dynamics._
import linearmath._

import collection.mutable.ArrayBuffer


class Agent extends btMotionState {
	val transform = new Matrix4().translate(0,10,0)
	var model = Model()
	
	override def getWorldTransform(worldTrans){
		worldTrans.set(transform)
	}
	override def setWorldTransform(worldTrans){
		transform.set(worldTrans)
		val v = transform.getTranslation(new Vector3())
		model.pose.pos.set(v.x,v.y,v.z)
	}
}

object World extends SeerApp {

	Bullet.init()

	val collisionConfig = new btDefaultCollisionConfiguration()
  val dispatcher = new btCollisionDispatcher(collisionConfig)
  val broadphase = new btDbvtBroadphase()
  val constraintSolver = new btSequentialImpulseConstraintSolver()
  val dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig)
  dynamicsWorld.setGravity(new Vector3(0, -10f, 0))

	val groundShape = new btBoxShape(new Vector3(5,0.5,5))

  var localInertia = new Vector3()
	val rbInfo = new btRigidBody.btRigidBodyConstructionInfo(0.f, null, groundShape, localInertia);
  val body = new btRigidBody(rbInfo)

  var agents = ArrayBuffer[Agent]()



  dynamicsWorld.addRigidBody(body)

  val groundModel = Cube()
  groundModel.scale(10,1,10)
  groundModel.material = Material.specular
  groundModel.material.color = RGB(1,0.3,0.3)


  def spawn(){
  	val agent = new Agent()
	  agents += agent
	  agent.model = Sphere().translate(0,10,0)
	  agent.model.material = Material.specular
		val ballShape = new btSphereShape(1.f)
	  ballShape.calculateLocalInertia(5.f,localInertia)
		val rbInfo2 = new btRigidBody.btRigidBodyConstructionInfo(5.f, agent, ballShape, localInertia);
	  val body2 = new btRigidBody(rbInfo2)
	  dynamicsWorld.addRigidBody(body2)
  }

  override def draw(){

  	groundModel.draw()
  	agents.foreach( _.model.draw() )

  }

  var t = 0.f
  override def animate(dt:Float){
  	t += dt 
  	if( t > 0.5f){
  		spawn()
  		t = 0.f
  	}

  	dynamicsWorld.stepSimulation(dt)

  }

}