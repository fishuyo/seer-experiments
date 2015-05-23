
// 2d? screen coords?

import com.fishuyo.seer.openni._

object Script extends SeerScript {

  val model = Sphere().scale(0.1f)
  model.material = Material.basic
  model.material.color = RGB(1,0,0)

  val model2 = Sphere().scale(0.1f)
  
  Renderer().camera = new OrthographicCamera(1,1)
  Renderer().camera.nav.pos.z = 2
  // Renderer().camera = Camera

  KPC.loadCalibration("../methetree/calibration.txt")
  println(KPC.matrix(0))

  override def draw(){
    model.draw()
    model2.draw()
  }

  override def animate(dt:Float){
  
  }

  Trackpad.clear
  Trackpad.connect
  Trackpad.bind { case touch =>
    touch.count match {
      case 1 => 
        // model2.pose.pos.set( touch.pos.x-1, 2*touch.pos.y-1, 0)

      case 2 =>
        model.pose.pos.x += touch.vel.x * 0.01
        model.pose.pos.z -= touch.vel.y * 0.01
        val p = model.pose.pos * 1000 
        p.z *= -1
        val out = KPC.worldToScreen(p)
        model2.pose.pos.set(out)
        println(out)
      case _ => ()
    }
  }

}

Script