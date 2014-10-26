
import com.fishuyo.seer._

import dynamic.SeerScript
import graphics._
import spatial._
import io._
import util._

import collection.mutable.ArrayBuffer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.physics.bullet._
import collision._
import dynamics._
import linearmath._

Scene.alpha = 0.09f
SceneGraph.root.depth = false

class Player extends btMotionState {
  val boxShape = new btBoxShape(new Vector3(0.5,1.,0.5))

  var transform = new Matrix4().translate(0,10,0)
  var pose = Pose()
  pose.pos.set(0,10,0)
  Camera.nav.pos.set(0,10,0)

  var localInertia = new Vector3()
  boxShape.calculateLocalInertia(5.f,localInertia)
  val rbInfo = new btRigidBody.btRigidBodyConstructionInfo(5.f, this, boxShape, localInertia);
  val body = new btRigidBody(rbInfo)

  body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_STATIC_OBJECT);

  // World.dynamicsWorld.addRigidBody(body)
  
  override def getWorldTransform(worldTrans:Matrix4){
    // val p = pose.pos
    // val q = pose.quat
    val p = Camera.nav.pos
    val q = Camera.nav.quat
    val quat = new Quaternion(q.x,q.y,q.z,q.w)
    transform = new Matrix4().translate(p.x,p.y,p.z).rotate(quat)
    worldTrans.set(transform)
  }
  override def setWorldTransform(worldTrans:Matrix4){
    transform.set(worldTrans)
    val v = transform.getTranslation(new Vector3())
    val q = transform.getRotation(new Quaternion())
    pose.pos.set(v.x,v.y,v.z)
    pose.quat.set(q.w,q.x,q.y,q.z)
    Camera.nav.pos.set(pose.pos)
  }
}

class Agent extends btMotionState {
  var transform = new Matrix4().translate(0,0,0)
  var model = Model()

  var body:btRigidBody = null
  
  override def getWorldTransform(worldTrans:Matrix4){
    val p = model.pose.pos
    val q = model.pose.quat
    val quat = new Quaternion(q.x,q.y,q.z,q.w)
    transform = new Matrix4().translate(p.x,p.y,p.z).rotate(quat)
    worldTrans.set(transform)
  }
  override def setWorldTransform(worldTrans:Matrix4){
    transform.set(worldTrans)
    val v = transform.getTranslation(new Vector3())
    val q = transform.getRotation(new Quaternion())
    model.pose.pos.set(v.x,v.y,v.z)
    model.pose.quat.set(q.w,q.x,q.y,q.z)
  }
}

object Agent {
  val ballShape = new btSphereShape(0.1f)
  val boxShape = new btBoxShape(new Vector3(0.1,0.1,0.1))


  def sphere() = {
    val agent = new Agent()
    World.agents += agent
    agent.model = Sphere().scale(0.1)
    agent.model.material = Material.basic

    var localInertia = new Vector3()
    ballShape.calculateLocalInertia(5.f,localInertia)
    val rbInfo = new btRigidBody.btRigidBodyConstructionInfo(5.f, agent, ballShape, localInertia);
    agent.body = new btRigidBody(rbInfo)
    World.dynamicsWorld.addRigidBody(agent.body)
    agent
  }

  def cube() = {
    val agent = new Agent()
    World.agents += agent
    agent.model = Cube().scale(0.2)
    agent.model.material = Material.basic

    var localInertia = new Vector3()
    boxShape.calculateLocalInertia(5.f,localInertia)
    val rbInfo = new btRigidBody.btRigidBodyConstructionInfo(5.f, agent, boxShape, localInertia);
    agent.body = new btRigidBody(rbInfo)
    World.dynamicsWorld.addRigidBody(agent.body)
    agent
  }
}

object World extends SeerScript {

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

  val player = new Player
  player.pose.pos.set(0,10,0)


  dynamicsWorld.addRigidBody(body)
  dynamicsWorld.addRigidBody(player.body)

  val groundModel = Cube()
  groundModel.scale(10,1,10)
  groundModel.material = Material.specular
  groundModel.material.color = RGB(1,0.3,0.3)


  override def draw(){

    groundModel.draw()
    agents.foreach( _.model.draw() )

  }

  var t = 0.f
  override def animate(dt:Float){
    t += dt 
    if( t > 0.5f){
      Agent.cube()
      t = 0.f
    }

    player.body.activate(true);

    dynamicsWorld.stepSimulation(dt)

  }

}

object Script extends SeerScript {

  implicit def f2i(f:Float) = f.toInt

  var t = 0.f 

  val mesh = Mesh()
  mesh.primitive = Lines

  val model = Model(mesh).translate(0,-3,0)
  model.material = Material.basic

  val cubes = ArrayBuffer[Model]()

  var vel = Vec2()
  var lpos = Vec2()

  // reset the mesh, generate new random vertices, and push to gpu
  def generateMesh(m:Mesh){
    m.clear
    for( i <- 0 until 1000) m.vertices += Random.vec3() // 34 random triangles
    mesh.vertices.foreach( (v) => v.y = math.sin(v.z*v.x*10).toFloat )
    

    // m.recalculateNormals()
    m.update()  // update mesh on gpu, must be called from render thread (init, draw, animate functions)
  }

  var inited = false
  override def init(){
    generateMesh(mesh)
    inited = true
  }

  // draw model
  override def draw(){

    MatrixStack.worldPose.quat.set(1,0,0,0) //rotate(0,0.001f,0)
    cubes.foreach(_.draw)

    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
    Gdx.gl.glLineWidth(1)
    model.draw
  }

  // animate the mesh
  override def animate(dt:Float){
    if(!inited) init()
    t += dt

    // random walk the vertices in the mesh and update on the gpu
    mesh.vertices.foreach( _ += Random.vec3()*0.002f )
    cubes.foreach( _.pose.pos += Random.vec3()*0.001f )
    // mesh.recalculateNormals()
    mesh.update()

    // generate new random mesh ever 2.5 seconds
    if( t > 5f){
      generateMesh(mesh)
      t = 0.f
    }

    // model.rotate(0,0.001f,0)

    if( Mouse.status() == "down"){
      vel = (Mouse.xy() - lpos)/dt
      val r = Camera.ray(Mouse.x()*Window.width, (1.f-Mouse.y()) * Window.height)
      val p = r(3.f)
      cubes += Cube().translate(p).scale(0.1)
    }
    lpos = Mouse.xy()
  }

  Trackpad.clear
  Trackpad.connect
  Trackpad.bind((touch) => {
  })

  Mouse.clear
  Mouse.use

}

World