
// space colonization
// use octtree..
// compare with 042215.scala

import com.fishuyo.seer._

import dynamic.SeerScript
import graphics._
import io._

import collection.mutable.ListBuffer
import collection.mutable.HashMap
import collection.mutable.HashSet

import com.badlogic.gdx.utils.PerformanceCounter


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

  // var leafCount = 400
  var minDistance = 0.05
  var maxDistance = 0.1 //0.35
  var branchLength = 0.04
 
  var root = new Branch(null, Vec3(), Vec3(0,1,0))
  var leaves = ListBuffer[Leaf]()
  // var branches = HashMap[Vec3,Branch]()

  var branches = Octree[Branch](Vec3(0),10)

  branches += (root.pos -> root)
  branches += (Vec3(0,0.1,0) -> new Branch(root, Vec3(0,0.1,0), Vec3(0,1,0)))

  // for( i <- (0 until leafCount))
    // leaves += new Leaf(Random.vec3())

  def reset(){
    branches.clear
    leaves.clear
    branches += (root.pos -> root)
    branches += (Vec3(0,0.1,0) -> new Branch(root, Vec3(0,0.1,0), Vec3(0,1,0)))
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
    branches.getAll().values.foreach( (b) => {
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
      // if (!branches.values.contains(b.pos)){
        branches += (b.pos -> b)
        branchAdded = true
      // }
    })
    

  }
}



object Script extends SeerScript {

  var grow = false
  val tree = new Tree

  val leafMesh = Mesh()
  leafMesh.primitive = Points
  leafMesh.maxVertices = 100000 //tree.leafCount

  val treeMesh = Mesh()
  treeMesh.primitive = Lines
  treeMesh.maxVertices = 100000
  val treeModel = Model(treeMesh)
  treeModel.material = Material.basic
  // treeModel.material.color = RGB(0.2,0.2,0.2)

  var limit = 0f

  Renderer().environment.alpha = 0.1
  Renderer().environment.blend = true
  Renderer().environment.depth = false

  var write = false

  val growTime = new PerformanceCounter("grow")
  val drawTime = new PerformanceCounter("draw")

  override def draw(){
    FPS.print
    println(s"grow: ${growTime.load.latest}")
    println(s"draw: ${drawTime.load.latest}")

    leafMesh.clear()
    leafMesh.vertices ++= tree.leaves.map( _.pos )
    leafMesh.update()
    leafMesh.draw()

    drawTime.start()
    treeMesh.clear()
    tree.branches.getAll.values.foreach( (b) => {
      if(b.parent != null){
        drawBranchRect(treeMesh, b)
        // drawBranchRing(treeMesh, b)
      }
    })
    treeMesh.update()
    treeModel.draw()
    drawTime.stop()

    growTime.tick()
    drawTime.tick()

    if(write){ writeToPointCloud(treeMesh); write = false }

    // MatrixStack.push
    // MatrixStack.translate(cursor)
    Sphere().translate(cursor).scale(0.05).draw
    // MatrixStack.pop

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


  override def animate(dt:Float){
    limit += dt
    if( limit > 0.05) limit = 0f

    growTime.start()
    if(grow) tree.grow()
    growTime.stop()

  }

  def writeToPointCloud(mesh:Mesh, path:String=""){
    var file = path
    if( file == "") file = "out-" + (new java.util.Date()).toLocaleString().replace(' ','-').replace(':','-') + ".xyz" 
        
    val out = new java.io.FileWriter( file )

    mesh.vertices.zip(mesh.normals).foreach { 
      case(v,n) =>
          out.write( s"${v.x} ${v.y} ${v.z} ${n.x} ${n.y} ${n.z}\n" )
    }
    out.close
  }

  Keyboard.clear
  Keyboard.use
  Keyboard.bind("g", () => { grow = !grow })
  Keyboard.bind("p", () => { write = true })
  Keyboard.bind("r", () => { tree.reset; cursor.zero })

  var cursor = Vec3()
  Trackpad.clear
  Trackpad.connect
  Trackpad.bind((touch) => {
    if(touch.count > 0){
      val v = touch.fingers(0).vel
      // val t = touch.fingers(0).pos
      var off = Vec3()
      if(touch.count == 2) off = Camera.nav.ur * v.x + Camera.nav.uu * v.y
      else if(touch.count == 3) off = Camera.nav.ur * v.x + Camera.nav.uf * v.y

      cursor += off * 0.01f

      if(limit == 0f){
        val p = cursor + (Random.vec3() * 0.05f)
        tree.leaves += new Leaf(p)
      }
    }
  })
}

Script