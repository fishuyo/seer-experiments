
import com.fishuyo.seer.actor._
import akka.actor._

class Agent extends SeerActor {
  val ref = context.actorSelection("/user/agent2.*")
  override def receive = super.receive orElse {
    case "hi" => log.info("agent"); ref ! "hi"
    case _ => log.info("what?")
  }

  override def draw(){
    Cube().draw
  }
}
// SeerActor.actorOf(new Agent(), "agent")
// System().actorOf(SeerActor.props(new Agent()), "agent")
System().actorOf(Props[Agent], s"agent.${Random.int()}")
