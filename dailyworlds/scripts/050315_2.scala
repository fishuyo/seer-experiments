
//kinect silouette contours
// hairy contours...

// nope particle person wind..

// stringy persons

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._

import com.badlogic.gdx.graphics.{Texture => GdxTexture}
import com.badlogic.gdx.graphics.Pixmap

import collection.mutable.ArrayBuffer


import collection.mutable.ListBuffer
import collection.mutable.HashMap
import collection.mutable.HashSet

import scala.concurrent.duration._

object Script extends SeerScript {

  var inited = false

  OpenNI.initAll()
  OpenNI.start()
  OpenNI.pointCloud = true

  OpenNI.pointCloudDensity = 6

  val mesh = new Mesh()
  mesh.primitive = Lines 
  mesh.maxVertices = 640*480
  mesh.maxIndices = 50000
  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGBA(1,1,1,1)
  // model.material.transparent = true
  var numIndices = 10000

  val strings = new ListBuffer[SpringString]()
  for(i <- 0 until 4000){
    strings += new SpringString(Vec3(), 4, 0.01)
  }

  for(i <- 0 until 4000){
    mesh.vertices ++= List(Vec3(0),Vec3(0.1),Vec3(0.2),Vec3(0.3),Vec3(0.4))
    mesh.indices ++= List(5*i,5*i+1,5*i+1,5*i+2,5*i+2,5*i+3,5*i+3,5*i+4)
  }

  var update = true
  Keyboard.clear
  Keyboard.bind("t", () => update = !update)
  // val springs = new SpringMesh(mesh,1f)
  // for(p <- springs.particles.sliding(1,4).flatten){ 
    // springs.pins += AbsoluteConstraint(p,Vec3(p.position))
  //   spring.pins += AbsoluteConstraint(p,p.position * Vec3(0.9,1,1) + Vec3(0,4 - math.abs(s()),Random.float()*0.01f))
  // }
  Gravity.set(0,-1,0)
  Schedule.every(2 seconds){
    val v = Random.vec3()
    Schedule.over(1.0 seconds){ case t => Gravity.lerpTo(v,0.01)}
    // Gravity.set(Random.vec3())
  }

  val particles = new ParticleEmitter(60000){
    // val s = Model(Sphere.generateMesh(0.01,4))
    val mesh = Mesh()
    mesh.primitive = Points
    mesh.maxVertices = maxParticles
    val model = Model(mesh)
    model.material = Material.basic
    model.material.color = RGB(0.9)

    override def draw(){
      mesh.clear
      mesh.vertices ++= particles.map( _.position )
      mesh.update
      model.draw
      // particles.foreach( (p) => {
        // s.pose.pos = p.position 
        // s.draw
      // })
    }
  }

  override def init(){
    inited = true
    Camera.nav.pos.set(0f,0f,-0.8334836f)
  }

  override def draw(){
    FPS.print

    Renderer().environment.depth = false
    Renderer().environment.blend = true
    Renderer().environment.alpha = 0.1f
    // Renderer().environment.lineWidth = 1f

    model.draw
    // particles.draw
  }

  var vel = Vec2()
  var lpos = Vec2()

  override def animate(dt:Float){
    if(!inited) init()

    implicit def f2i(f:Float) = f.toInt

    // if( Mouse.status() == "drag"){
    //   vel = (Mouse.xy() - lpos)/dt
    //   // println(vel)
    //   // s.applyForce( Vec3(vel.x,vel.y,0)*10.f)
    //   val r = Camera.ray(Mouse.x()*Window.width, (1f-Mouse.y()) * Window.height)
    //   springs.particles.foreach( (p) => {
    //     val t = r.intersectSphere(p.position, 0.25f)
    //     if(t.isDefined){
    //       // val p = r(t.get)
    //       p.applyForce(Vec3(vel.x,vel.y,0)*150f)
    //       // cursor.pose.pos.set(r(t.get))
    //     }
    //   })
    // }
    // lpos = Mouse.xy()

    // springs.animate(dt)


    try{
      // particles ++= OpenNI.pointMesh.vertices.map(Particle(_, Random.vec3()*0.001))
      // particles.animate(dt)

      // mesh.clear
      // mesh.vertices ++= OpenNI.pointMesh.vertices
      // val index = Random.int(mesh.vertices.length)
      // mesh.indices ++= (0 until numIndices).map( _ => index() )
      // mesh.update

      if(update){ 
        strings.zip( OpenNI.pointMesh.vertices).foreach { 
          case (s,v) => s.pins(0).set(v)
        }
      }
      mesh.vertices.clear
      strings.foreach( (s) => {
        s.animate(dt)
        mesh.vertices ++= s.particles.map( _.position )
      })
      mesh.update

    } catch { case e:Exception => println(e) }

  }
}

Script