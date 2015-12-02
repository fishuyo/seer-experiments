
import com.fishuyo.seer.actor._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import concurrent.Await
import concurrent.duration._


class Agent extends SeerActor {
  val world = context.actorSelection("/user/world.*")
  implicit val timeout = Timeout(3 seconds)
  var field = Await.result(world ? "field", 3 seconds).asInstanceOf[VecField3D]

  val body = Sphere() //.generateMesh()
  body.scale.set(0.1f)
  val nav = Nav()

  override def receive = super.receive orElse {
    case m => log.info(s"agent says: what? ($m)")
  }

  override def draw(){
    body.draw
  }
  override def animate(dt:Float){
    // nav.worldVel.x = 0.1
    nav.worldVel.set(field(nav.pos))
    nav.step(dt)
    body.pose.set(nav)
  }
}

// return a new world actor
System().actorOf(Props[Agent], s"agent.${Random.int()}")
