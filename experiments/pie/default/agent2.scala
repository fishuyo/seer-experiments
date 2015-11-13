
import com.fishuyo.seer.actor._
import akka.actor._

class Agent2 extends SeerActor {
  val ref = context.actorSelection("/user/agent3.*")
  override def receive = {
    case "hi" => log.info("agent2"); ref ! "hi"
    case _ => log.info("what?")
  }

}
// SeerActor.actorOf(new Agent(), "agent")
// System().actorOf(SeerActor.props(new Agent()), "agent")
// System().actorOf(Props[Agent2], s"agent2.${Random.int()}")
