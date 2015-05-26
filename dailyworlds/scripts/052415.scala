
//stick test

import com.fishuyo.seer.particle._

import collection.mutable.ListBuffer
import collection.mutable.Map

object Script extends SeerScript {


  val falling = Map[Stick, Model]()

  Gravity.set(0,-1,0)

  import scala.concurrent.duration._
  Schedule.every(2 seconds){
    val mesh = Mesh()
    mesh.primitive = LineStrip
    mesh.vertices ++= (0 until 10).map( Vec3(_)*0.01 + Random.vec3()*0.01)
    val model = Model(mesh)
    model.material = Material.basic
    model.material.color = RGBA(1,1,1,1)
    val stick = Stick(Vec3(), Random.vec3()*0.01, Quat(), Random.vec3()*0.1)
    falling += (stick -> model)
  }


  override def draw(){
    falling.foreach { case (s,m) =>
      m.draw
    }
  }

  override def animate(dt:Float){

    falling.foreach{ case (s,m) =>
      s.applyGravity()
      // s.applyDamping(damping)
      s.step() // timeStep
      s.collideGround(-1f, 0.5f)
      m.pose.pos.set(s.position)
      m.pose.quat.set(s.orientation)
    }

  }

  // Trackpad.clear
  // Trackpad.connect
  // Trackpad.bind { case touch =>
  //   touch.count match {
  //     case 1 =>
  //     case 2 => pins(0).q.position += Vec3(touch.vel.x, touch.vel.y, 0) * 0.01
  //     case _ => ()
  //   }
  // }

}

Script