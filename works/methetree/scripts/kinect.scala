

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
  }


  override def animate(dt:Float){
    if(!inited) init()

    if(skeleton.tracking) skeleton.updateJoints()
    println(skeleton.joints("head"))

    try{
      mesh.clear
      mesh.vertices ++= OpenNI.pointMesh.vertices
      // val index = Random.int(mesh.vertices.length)
      // mesh.indices ++= (0 until numIndices).map( _ => index() )
      mesh.update

    } catch { case e:Exception => println(e) }

  }
}

Script