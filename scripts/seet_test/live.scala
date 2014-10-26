
import com.fishuyo.seer._

import dynamic.SeerScript
import graphics._
import io._

object Script extends SeerScript {

  var t = 0.f
  var boost = 0.f
  var depth = 10.f
  var dy = 0.1f

  override def draw(){
    Shader.alpha(0.1f)
    Shader.blend("oneMinusSrc")
    Shader.lineWidth(1.f)

    Sphere().mesh.primitive = Lines
    // Sphere().draw
    // MatrixStack.rotate(0,boost*t,0)
    // Sphere().mesh.vertices.foreach( Sphere().translate(_).scale(0.01f).draw() )
    // MatrixStack.pop
    recurse(depth.toInt)
  }

  def recurse(d:Int){
    if(d > 0){
      MatrixStack.translate(0,dy,0)
      MatrixStack.rotate(0,0.0f,boost)
      Sphere().scale(0.01f).draw
      recurse(d-1)

      // MatrixStack.pop
      MatrixStack.rotate(0.02f*boost,0.0f,0)
      Sphere().scale(0.03f).draw
      recurse(d-1)
    }
  }

  override def animate(dt:Float){
    t += dt
  }

  Trackpad.clear
  Trackpad.connect
  Trackpad.bind((touch) => {
    touch.count match {
      case 1 => 
        boost += (touch.vel.x*0.01f)
        dy += touch.vel.y*0.01f
      case 2 =>
        // depth += touch.vel.y*0.1f
      case _ => ()  
    }
  })
}

Script