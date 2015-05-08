

import com.fishuyo.seer.spacetree._

import com.badlogic.gdx.utils.PerformanceCounter

object Script extends SeerScript {

  var grow = false
  val tree = new Tree

  tree.minDistance = 0.05
  tree.maxDistance = 0.1 //0.35
  tree.branchLength = 0.03

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

  override def draw(){
    FPS.print
    // println(s"grow: ${growTime.load.latest}")
    // println(s"draw: ${drawTime.load.latest}")
    println(treeMesh.vertices.length)

    leafMesh.clear()
    leafMesh.vertices ++= tree.leaves.map( _.pos )
    leafMesh.update()
    leafMesh.draw()

    drawTime.start()
    if(grow){
      treeMesh.clear()
      // tree.branches.getAll.values.foreach( (b) => {
      tree.branches.foreach( (p,b) => {
        // if(b.parent != null){
          // drawBranchRect(treeMesh, b)
          drawBranchRing(treeMesh, b)
        // }
      })
      treeMesh.update()
    }
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
      val cos = math.cos(phase)
      val sin = math.sin(phase)
      // val vx = Vec3(1,0,0)
      var dir = b.growDirection
      // if(b.parent == null)
      val q = Quat().getRotationTo(Vec3(0,0,1), dir.normalize) //(b.pos - b.parent.pos).normalize )
      val vx = q.toX
      // val vz = Vec3(0,0,1)
      val vz = q.toY         //Vec3(0,0,1)
      val thick = math.log(r*b.age+1)
      val off1 = vx * cos * thick + vz * sin * thick
      // if(off1.mag < 0.005) off1.set(off1.normalized * 0.005)
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