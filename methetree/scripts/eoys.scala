
/*
 * SeerScript (scala 2.11.2)
 *
 * Space colonizing tree growth onto user depth image
 *  
 *   put this code into:
 *     https://github.com/fishuyo/seer/tree/devel/examples/scripts/live.scala
 *
 * 2015 Tim Wood
 */

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._

import collection.mutable.ArrayBuffer
import collection.mutable.ListBuffer
import collection.mutable.HashSet


class Branch(var parent:Branch, var pos:Vec3, var growDirection:Vec3){
  var growDirection0 = Vec3(growDirection)
  var growCount = 0
  var age = 0

  def grow(){
    age += 1
    if(parent != null) parent.grow()
  }

  def reset(){
    growCount = 0
    growDirection = growDirection0
  }
}

class Leaf(var pos:Vec3){
  var closest:Branch = null
}

class Tree {
  var done = false
  var pos = Vec3()

  var minDistance = 0.05
  var maxDistance = 0.1 //0.35
  var branchLength = 0.04
 
  var root = new Branch(null, Vec3(), Vec3(0,1,0))
  var leaves = ListBuffer[Leaf]()

  var branches = Octree[Branch](Vec3(0),5)

  branches += (root.pos -> root)

  def reset(){
    branches.clear
    leaves.clear
    branches += (root.pos -> root)
  }

  def grow(){

    if (leaves.size == 0) { 
        return
    }

    //process the leaves
    var i = 0
    while( i < leaves.size){

      var leafRemoved = false

      var direction = Vec3()
      val leaf = leaves(i)
      leaf.closest = null

      //Find the nearest branch for this leaf
      var break = false
      val near = branches.getInSphere(leaf.pos, maxDistance)
      near.values.foreach( (b) => { 
        if(!break){
          direction = leaf.pos - b.pos
          val dist = direction.mag
          direction.normalize 

          if( dist <= minDistance){
            leaves -= leaf
            i -= 1
            leafRemoved = true
            break = true
          } else if( dist <= maxDistance){
            if( leaf.closest == null)
              leaf.closest = b 
            else if ( (leaf.pos - leaf.closest.pos).mag > dist)
              leaf.closest = b
          }
        }
      })

      //if the leaf was removed, skip
      if (!leafRemoved){
          //Set the grow parameters on all the closest branches that are in range
          if (leaf.closest != null){
              val dir = leaf.pos - leaf.closest.pos
              dir.normalize()
              leaf.closest.growDirection += dir       //add to grow direction of branch
              leaf.closest.growCount += 1
          }
      }

      i += 1
    }

    //Generate the new branches
    val newBranches = HashSet[Branch]()
    branches.foreach( (p,b) => {
      if (b.growCount > 0){    //if at least one leaf is affecting the branch
      
          val avgDirection = b.growDirection / b.growCount
          avgDirection.normalize()

          val newBranch = new Branch(b, b.pos + avgDirection * branchLength, avgDirection);
          b.grow()

          newBranches += newBranch
          b.reset()
      }
    })

    //Add the new branches to the tree
    var branchAdded = false;
    newBranches.foreach( (b) => {
        branches += (b.pos -> b)
        branchAdded = true
    })
  }
}

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

  var grow = false
  val tree = new Tree

  tree.minDistance = 0.05
  tree.maxDistance = 0.1
  tree.branchLength = 0.04

  val treeMesh = Mesh()
  treeMesh.primitive = Points
  treeMesh.maxVertices = 100000
  val treeModel = Model(treeMesh)
  treeModel.material = Material.basic
  treeModel.material.color = RGBA(1,1,1,1)

  var write = false

  Keyboard.clear
  Keyboard.use
  Keyboard.bind("g", () => { grow = !grow })
  Keyboard.bind("r", () => { tree.reset;  })
  Keyboard.bind("p", () => { write = true })


  override def init(){
    inited = true
    Camera.nav.pos.set(0f,0f,-0.8334836f)
    tree.root.pos.set(0,0.1,-1.6)
  }

  override def draw(){
    FPS.print

    model.draw

    treeMesh.clear()
    tree.branches.getAll.values.foreach( (b) => {
      if(b.parent != null){
        // drawBranchRect(treeMesh, b)
        drawBranchRing(treeMesh, b)
      }
    })
    treeMesh.update()
    treeModel.draw()

    if(write){
      treeMesh.writePointCloud()
      mesh.writePointCloud()
      write = false
    }
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

    for( i <- 0 until n){
      val phase = i.toFloat / n * 2 * Pi
      val cos = r*math.cos(phase)
      val sin = r*math.sin(phase)
      val vx = Vec3(1,0,0)
      val vz = Vec3(0,0,1)
      val off1 = vx * cos * b.age + vz * sin * b.age
      if(off1.mag < 0.005) off1.set(off1.normalized * 0.005)
      m.vertices += b.pos + off1
      m.normals += off1.normalized
    }
  }

  override def animate(dt:Float){
    if(!inited) init()
    
    try{
      skeleton.updateJoints()
      // println(skeleton.joints("head"))

      mesh.clear
      mesh.vertices ++= OpenNI.pointMesh.vertices
      mesh.update

      tree.leaves.clear
      tree.leaves ++= OpenNI.pointMesh.vertices.map( new Leaf(_) )
      if(grow) tree.grow()
    
    } catch { case e:Exception => println(e) }
  }
}

Script