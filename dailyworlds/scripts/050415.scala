

// single string

import com.fishuyo.seer.particle._

import collection.mutable.ListBuffer

object Script extends SeerScript {

  val particles = ListBuffer[Particle]()
  val springs = ListBuffer[LinearSpringConstraint]()
  val pins = ListBuffer[AbsoluteConstraint]()

  particles += Particle(Vec3())
  pins += AbsoluteConstraint(particles.last, particles.last.position)

  for( i <- 0 until 4){
    val p = Particle(Vec3())
    val s = LinearSpringConstraint(p, particles.last, 0.1, 0.1)
    particles += p 
    springs += s 
  }

  val mesh = Mesh()
  mesh.primitive = Lines
  val model = Model(mesh)

  val mesh2 = Mesh()
  mesh2.primitive = Lines

  for( i <- 0 until 4){
    mesh2.vertices ++= List(Vec3(),Vec3(0.1),Vec3(0.2),Vec3(0.3),Vec3(0.4))
    mesh2.indices ++= List(0,1,1,2,2,3,3,4)
  }

  val springMesh = new SpringMesh(mesh2)
  springMesh.updateNormals = false
  val springModel = Model(springMesh)

  override def draw(){
    mesh.clear
    mesh.vertices ++= particles.map( _.position )
    mesh.indices ++= List(0,1,1,2,2,3,3,4)
    mesh.update
    model.draw

    // springModel.draw
  }

  override def animate(dt:Float){
    // for( t <- (0 until steps)){
      for( s <- (0 until 3) ){ 
        springs.foreach( _.solve() )
        pins.foreach( _.solve() )
      }

      particles.foreach( (p) => {
        p.applyGravity()
        // p.applyDamping(damping)
        p.step() // timeStep
        p.collideGround(-1f, 0.999999f) 
      })

      // springMesh.animate(dt)
    // }
  }

  Trackpad.clear
  Trackpad.connect
  Trackpad.bind { case touch =>
    touch.count match {
      case 1 =>
      case 2 => pins(0).q.position += Vec3(touch.vel.x, touch.vel.y, 0) * 0.01
      case _ => ()
    }
  }

}

Script