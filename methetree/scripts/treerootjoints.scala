
import com.fishuyo.seer.spacetree._

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
  OpenNI.pointCloudDensity = 2

  val skeleton = OpenNI.getSkeleton(1)

  val mesh = new Mesh()
  mesh.primitive = Points 
  mesh.maxVertices = 640*480
  mesh.maxIndices = 10000
  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGBA(1,1,1,1)
  model.material.transparent = true
  var numIndices = 10000

  var grow = false
  val tree = new Tree

  // tree.minDistance = 0.05
  // tree.maxDistance = 0.1 //0.35
  // tree.branchLength = 0.04

  tree.minDistance = 0.03 //0.05
  tree.maxDistance = 0.1 //0.35
  tree.branchLength = 0.02 //0.03

  // val leafMesh = Mesh()
  // leafMesh.primitive = Points
  // leafMesh.maxVertices = 100000 //tree.leafCount

  val treeMesh = Mesh()
  treeMesh.primitive = Points
  treeMesh.maxVertices = 1000000
  val treeModel = Model(treeMesh)
  treeModel.material = Material.basic
  treeModel.material.color = RGBA(1,1,1,1)
  // treeModel.material.transparent = true

  var write = false

  Keyboard.clear
  Keyboard.use
  Keyboard.bind("g", () => { grow = !grow })
  Keyboard.bind("r", () => { tree.reset;  })
  Keyboard.bind("p", () => { write = true })

  import scala.concurrent.duration._
  Schedule.every(10 seconds){
    write = true
  }
 

  override def init(){
    inited = true
    Camera.nav.pos.set(0f,0f,-0.8334836f)
    // tree.root.pos.set(0,0,-1)
    tree.root.pos.set(0,0,-2)
  }

  override def draw(){
    FPS.print

    // Renderer().environment.depth = false
    // Renderer().environment.blend = true
    // Renderer().environment.alpha = 0.1f
    // Renderer().environment.lineWidth = 1f

    model.draw

    // Renderer().environment.lineWidth = 2f
    // Renderer().environment.alpha = 1f

    treeMesh.clear()
    // tree.branches.getAll.values.foreach( (b) => {
      // if(b.parent != null){
        // treeMesh.vertices += b.pos
        // treeMesh.normals += Vec3(0,0,1)
        // treeMesh.vertices += b.parent.pos
        // treeMesh.normals += Vec3(0,0,1)

        // drawBranchRect(treeMesh, b)
        // drawBranchRing(treeMesh, b)
      // }
    // })
    drawTreeRing(treeMesh,tree)
    treeMesh.update()
    treeModel.draw()
    if(write){
      treeMesh.writePointCloud()
      mesh.writePointCloud()
      write = false
    }
    // mesh.vertices.foreach{ case v =>
    //   sphereModel.pose.pos.set(v)
    //   sphereModel.draw
    // }
  }

  def drawBranchRect(m:Mesh, b:Branch){
    val s = Vec3(0.00001,0,0)
    m.vertices += b.pos - s * b.age
    m.vertices += b.parent.pos - s * b.parent.age
    m.vertices += b.parent.pos - s * b.parent.age
    m.vertices += b.parent.pos + s * b.parent.age
    m.vertices += b.parent.pos + s * b.parent.age
    m.vertices += b.pos + s * b.age
    m.vertices += b.pos + s * b.age
    m.vertices += b.pos - s * b.age
  }
  def drawBranchRing(m:Mesh, b:Branch, r:Float = 0.00001f){
    var n = 1
    if(b.age < 50) n = 3
    else if(b.age < 100) n = 4
    else if(b.age < 150) n = 5
    else if(b.age < 400) n = 6
    else if(b.age < 600) n = 10
    else n = 20

    // if(b.age > 50 ) println(n)

    for( i <- 0 until n){
      val phase = i.toFloat / n * 2 * Pi
      val cos = r*math.cos(phase)
      val sin = r*math.sin(phase)
      val vx = Vec3(1,0,0)
      // val vx = (b.pos - b.parent.pos) cross Vec3(0,0,1) //Vec3(1,0,0)
      val vz = Vec3(0,0,1)
      // val vz = (b.pos - b.parent.pos) cross vx          //Vec3(0,0,1)
      val off1 = vx * cos * b.age + vz * sin * b.age
      if(off1.mag < 0.005) off1.set(off1.normalized * 0.005)
      // val off2 = vx * cos * b.parent.age + vz * sin * b.parent.age
      // val off2 = child.pose.ur()*x*sc.x + child.pose.uu()*y*sc.y
      m.vertices += b.pos + off1
      m.normals += off1.normalized
      // m.vertices += b.parent.pos + off2
      // m.normals += off2.normalized
    }
  }

  def drawTreeRing(m:Mesh, b:Tree, r:Float = 0.00001f){
    
    val minThick = 0.01
    val minDist = 0.005

    tree.branches.foreach( (p,b) => {

      if(b.parent != null){
        // var dir = b.growDirection
        val dir = b.pos - b.parent.pos
        val dist = dir.mag()
        val steps = (dist / minDist).toInt

        val q = Quat().getRotationTo(Vec3(0,0,1), dir.normalized)
        val vx = q.toX
        val vz = q.toY 

        for(s <- 0 until steps){
          val t = s.toFloat / steps
          val age = b.parent.age * (1-t) + b.age * t
          var thick = math.log(r*age+1)
          if( thick < minThick ) thick = minThick

          var n = (2f*math.Pi*thick / minDist).toInt
          if( n < 4) n = 4

          val pos = b.parent.pos * (1-t) + b.pos * t
          for( i <- 0 until n){
            val phase = i.toFloat / n * 2 * Pi
            val cos = math.cos(phase)
            val sin = math.sin(phase)
            val off1 = vx * cos * thick + vz * sin * thick
            // if(off1.mag < minThick) off1.set(off1.normalized * minThick)
            m.vertices += pos + off1
            m.normals += off1.normalized
          }
        }
      }
    })  
  }

  override def animate(dt:Float){
    if(!inited) init()
    
    try{
      // skeleton.updateJoints()
      // println(skeleton.joints("head"))
      mesh.clear
      mesh.vertices ++= OpenNI.pointMesh.vertices
      // val index = Random.int(mesh.vertices.length)
      // mesh.indices ++= (0 until numIndices).map( _ => index() )
      mesh.update

      tree.leaves.clear
      tree.leaves ++= OpenNI.pointMesh.vertices.map( new Leaf(_) )
      if(grow) tree.grow()
    
    } catch { case e:Exception => println(e) }
  }

}

Script