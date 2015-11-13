
//kinect silouette contours
// hairy contours...

// nope particle person wind..

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

  OpenNI.pointCloudDensity = 4

  Renderer().camera = new OrthographicCamera(1,1)
  Renderer().camera.nav.pos.z = 2
  // Renderer().camera = Camera

  KPC.loadCalibration("../methetree/calibration.txt")


  val mesh = new Mesh()
  mesh.primitive = Lines 
  mesh.maxVertices = 640*480
  mesh.maxIndices = 10000
  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGBA(1,1,1,1)
  // model.material.transparent = true
  var numIndices = 10000

  // val springs = new SpringMesh(mesh)

  // for(p <- spring.particles.sliding(1,nx/12).flatten){ 
  //   spring.pins += AbsoluteConstraint(p,p.position * Vec3(1,1,1) + Vec3(0,4,Random.float()*0.01f))
  // //   spring.pins += AbsoluteConstraint(p,p.position * Vec3(0.9,1,1) + Vec3(0,4 - math.abs(s()),Random.float()*0.01f))
  // }
  Gravity.set(0,4,0)
  Schedule.every(2 seconds){
    val v = Random.vec3() + Vec3(0,1,0)
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

    // model.draw
    particles.draw
  }


  override def animate(dt:Float){
    if(!inited) init()

    try{
      particles ++= OpenNI.pointMesh.vertices.map( (v) => {
        val p = v*1000; p.z *= -1; val out = KPC.worldToScreen(p)
        Particle(out, Random.vec3()*0.001)
      })
      particles.animate(dt)
      // mesh.clear
      // mesh.vertices ++= OpenNI.pointMesh.vertices
      // val index = Random.int(mesh.vertices.length)
      // mesh.indices ++= (0 until numIndices).map( _ => index() )
      // mesh.update

    } catch { case e:Exception => println(e) }

  }
}

Script