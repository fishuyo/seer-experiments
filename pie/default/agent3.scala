
import com.fishuyo.seer.actor._
import akka.actor._

class Agent3 extends SeerActor {
  val ref = context.actorSelection("/user/agent.*")
  override def receive = {
    case "hi" => () //log.info("hi, im agent 3"); ref ! "hi"
    case _ => log.info("what?")
  }

}

// SeerActor.actorOf(new Agent(), "agent")
// System().actorOf(SeerActor.props(new Agent()), "agent")
// System().actorOf(Props[Agent3], s"agent3.${Random.int()}")
