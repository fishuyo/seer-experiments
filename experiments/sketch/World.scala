
import com.fishuyo.seer.actor._
import com.fishuyo.seer.particle._
import akka.actor._
import collection.mutable.ListBuffer

class World extends SeerActor {
  // val ref = context.actorSelection("/user/agent2.*")
  val field = new VecField3D(40,Vec3(0),10f)
  val fieldMesh = Mesh()
  fieldMesh.primitive = Lines
  randomizeField()
  val fieldModel = Model(fieldMesh)

  val foodModel = Sphere()
  foodModel.scale.set(0.05f)
  foodModel.material = Material.basic
  foodModel.material.color = RGB(1,0,0)
  val foodParticles = ListBuffer[Particle]()
  (0 until 100).foreach { case i =>
    val p = Particle(Random.vec3()) //, Random.vec()*0.1)
    foodParticles += p
  }


  def randomizeField(){
    val n = field.n
    for( z<-(0 until n); y<-(0 until n); x<-(0 until n)){
      val cen = field.centerOfBin(x,y,z)
      // val v = Random.vec3()
      // val v = cen.normalized
      val v = Vec3(0,1,0)
      field.set(x,y,z,v)
      fieldMesh.vertices += cen 
      fieldMesh.vertices += cen + v*0.5 
    }
  }


  override def receive = super.receive orElse {
    case "field" => sender ! field
    case _ => log.info("world says: what?")
  }

  override def draw(){
    fieldModel.draw
    foodParticles.foreach { case p =>
      foodModel.pose.pos = p.position
      foodModel.draw
    }
  }

  override def animate(dt:Float){
    foodParticles.foreach { case p =>
      if(!field.contains(p.position)){ 
        p.position = Random.vec3()
        p.lPosition = p.position - p.velocity
      }
      p.applyForce(field(p.position))
      // p.setVelocity(field(p.position)*dt)
      p.step()
    }
  }
}

// return a new world actor
System().actorOf(Props[World], s"world.${Random.int()}")
