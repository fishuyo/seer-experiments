



object Script extends SeerScript {

  val mesh = Mesh()
  mesh.maxVertices = 10000
  mesh.primitive = Lines

  val model = Model(mesh)

  var time = 0f 

  val (nx,nz) = (10,10)
  for( x <- ( -nx until nx); z <- (-nz until nz); y <- (0 until 4)){
    val p = Vec3(x,y,z) * util.Random.vec3()* 0.1 //Vec3(0.1,0.1,0.1)
    mesh.vertices += p 
    mesh.vertices += p + Vec3(0,0.1,0)
  }


  override def draw() = {
    FPS.print
    Renderer().environment.alpha = 0.01f  // broken XXX ???
    Renderer().environment.blend = true
    Renderer().environment.depth = false
    model.draw
  }

  override def animate(dt:Float) = {
    time += dt
    val phs = 0.1 * time
    mesh.vertices.grouped(8).foreach( (vs) => {
      vs.grouped(2).foldLeft(vs(0)){ case (p,v) => 
        v(0).set(p)
        v(1).set(p + Vec3(0,0.1,0) + util.Random.vec3()*0.01)
        v(1)
      }
    })
    mesh.update
  }

  Keyboard.bind("`", ()=> Camera.nav.moveToOrigin )
}

Script