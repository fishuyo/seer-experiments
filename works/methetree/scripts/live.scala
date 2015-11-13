
// now with blur 

import com.fishuyo.seer.spacetree2._

import com.badlogic.gdx.utils.PerformanceCounter

object Script extends SeerScript {

  var grow = false
  val tree = new Tree

  tree.minDistance = 0.03 //0.05
  tree.maxDistance = 0.1 //0.35
  tree.branchLength = 0.02 //0.03

  val leafMesh = Mesh()
  leafMesh.primitive = Points
  leafMesh.maxVertices = 100000 //tree.leafCount

  val treeMesh = Mesh()
  treeMesh.primitive = Points
  treeMesh.maxVertices = 1000000
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

  var blur:BlurNode = _
  var inited = false
  // import scala.concurrent.duration._
  // Schedule.every(10 seconds){
  //   write = true
  // }

  override def draw(){
    FPS.print
    // println(s"grow: ${growTime.load.latest}")
    // println(s"draw: ${drawTime.load.latest}")
    if( Time() % 5f == 0f ) println(treeMesh.vertices.length)

    leafMesh.clear()
    leafMesh.vertices ++= tree.leaves.map( _.pos )
    leafMesh.update()
    leafMesh.draw()

    drawTime.start()
    if(tree.dirty){  //grow){
      treeMesh.clear()
      // tree.branches.getAll.values.foreach( (b) => {
      // tree.branches.foreach( (p,b) => {
      //   // if(b.parent != null){
      //     // drawBranchRect(treeMesh, b)
      //     drawBranchRing(treeMesh, b)
      //   // }
      // })
      drawTreeRing(treeMesh,tree, 0.000015)
      treeMesh.update()
      tree.dirty = false
    }
    treeModel.draw()
    drawTime.stop()

    growTime.tick()
    drawTime.tick()

    if(write){ treeMesh.writePointCloud(); write = false }
    // MatrixStack.push
    // MatrixStack.translate(cursor)
    Sphere().translate(cursor).scale(0.05).draw
    // MatrixStack.pop

  }

  def drawBranchRect(m:Mesh, b:Branch){
    val s = Vec3(0.00001,0,0)
    val sb = s * b.age
    val sbp = s * b.parent.age
    val p1 = b.pos - sb
    val p2 = b.pos + sb 
    val p3 = b.parent.pos - sbp
    val p4 = b.parent.pos + sbp
    m.vertices += p1
    m.vertices += p3
    m.vertices += p3
    m.vertices += p4
    m.vertices += p4
    m.vertices += p2
    m.vertices += p2
    m.vertices += p1
  }

  def drawBranchRing(m:Mesh, b:Branch, r:Float = 0.00001f, steps:Int=1){
    
    val minThick = 0.005
    val minArcLen = 0.01
    val thick = math.log(r*b.age+1)

    
    var n = (2f*math.Pi*thick / minArcLen).toInt
    if( n < 3) n = 3

    // var n = 1
    // if(b.age < 50) n = 3
    // else if(b.age < 100) n = 4
    // else if(b.age < 150) n = 5
    // else if(b.age < 400) n = 6
    // else if(b.age < 600) n = 10
    // else n = 20

    for( i <- 0 until n){
      val phase = i.toFloat / n * 2 * Pi
      val cos = math.cos(phase)
      val sin = math.sin(phase)
      // val vx = Vec3(1,0,0)
      var dir = b.growDirection
      // if(b.parent == null)
      val q = Quat().getRotationTo(Vec3(0,0,1), dir.normalize) //(b.pos - b.parent.pos).normalize )
      val vx = q.toX
      val vz = q.toY 
      val off1 = vx * cos * thick + vz * sin * thick
      if(off1.mag < minThick) off1.set(off1.normalized * minThick)
      // val off2 = vx * cos * b.parent.age + vz * sin * b.parent.age
      // val off2 = child.pose.ur()*x*sc.x + child.pose.uu()*y*sc.y
      m.vertices += b.pos + off1
      m.normals += off1.normalized
      // m.vertices += b.parent.pos + off2
      // m.normals += off2.normalized
    }
  }

  def drawTreeRing(m:Mesh, b:Tree, r:Float = 0.00001f){
    
    val minThick = 0.005
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
          var thick = math.log(r*age+1) + minThick
          // if( thick < minThick ) thick = minThick

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

    if(!inited){
      blur = new BlurNode
      RenderGraph.reset
      RootNode.outputTo(blur)

      inited = true
    }

    blur.intensity = math.abs( math.sin(Time())) + 1f
    // blur.size =
    limit += dt
    if( limit > 0.05) limit = 0f

    growTime.start()
    // if(grow) tree.grow()
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

      touch.count match {
        case 2 => off = Camera.nav.ur * v.x + Camera.nav.uu * v.y
        case 3 => off = Camera.nav.ur * v.x + Camera.nav.uf * v.y
        case 4 => blur.size += v.y * 0.001f
        case _ => ()
      }

      cursor += off * 0.01f

      if(limit == 0f){
        val p = cursor + (Random.vec3() * 0.05f)
        tree.leaves += new Leaf(p)
      }
    }
  })
}

Script