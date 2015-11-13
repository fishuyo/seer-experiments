

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._

import com.badlogic.gdx.graphics.{Texture => GdxTexture}
import com.badlogic.gdx.graphics.Pixmap

import collection.mutable.ArrayBuffer


import collection.mutable.ListBuffer
import collection.mutable.HashMap
import collection.mutable.HashSet


object Script extends SeerScript {

  var inited = false

  OpenNI.initAll()
  OpenNI.start()
  OpenNI.pointCloud = true
  OpenNI.pointCloudDensity = 6

  Renderer().camera = new OrthographicCamera(1,1)
  Renderer().camera.nav.pos.z = 2
  // Renderer().camera = Camera

  KPC.loadCalibration("../methetree/calibration.txt")
  KPC.matrix.foreach(println(_))
  println(KPC.worldToScreen(Vec3(-204.80182, 429.27292, 2208.0)))

  val mesh = new Mesh()
  mesh.primitive = Points
  mesh.maxVertices = 640*480
  mesh.maxIndices = 10000
  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGBA(1,1,1,1)
  // model.material.transparent = true
  var numIndices = 10000

  val skeleton = OpenNI.getSkeleton(1)

  val sphere = Sphere().scale(0.1f)

  override def init(){
    inited = true
    Camera.nav.pos.set(0f,0f,-0.8334836f)
  }

  override def draw(){
    FPS.print

    // Renderer().environment.depth = false
    // Renderer().environment.blend = true
    // Renderer().environment.alpha = 0.1f
    // Renderer().environment.lineWidth = 1f

    model.draw
    sphere.draw
  }

  var head = Vec3()
  override def animate(dt:Float){
    if(!inited) init()

    if(skeleton.tracking) skeleton.updateJoints()
    val newhead = skeleton.joints("head")
    if(newhead.x != head.x){
      println(newhead)
      head = Vec3(newhead)

      val p = newhead *1000
      p.z *= -1
      val out = KPC.worldToScreen(p)
      sphere.pose.pos.set(out)
      println("screen: " + out)
    }

// [ -204.80182, 429.27292, 2208.0 ]
// [ 665.1891, 501.93344, 0.0 ]

// -4, -3.8

    try{
      mesh.clear
      mesh.vertices ++= OpenNI.pointMesh.vertices.map( (v) => { val p = v*1000; p.z *= -1; KPC.worldToScreen(p)})
      // val index = Random.int(mesh.vertices.length)
      // mesh.indices ++= (0 until numIndices).map( _ => index() )
      mesh.update

    } catch { case e:Exception => println(e) }

  }
}

Script