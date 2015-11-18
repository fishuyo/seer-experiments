
import com.fishuyo.seer._

import dynamic.SeerScript
import graphics._
import io._

Scene.alpha = 0.1f
SceneGraph.root.depth = false

object Script extends SeerScript {

  var t = 0.0f
  var boost = 0.0f

  override def draw(){
    FPS.print

    Sphere().mesh.primitive = Triangles
    Sphere().draw
    MatrixStack.rotate(0,boost*t,0)
    Sphere().mesh.vertices.foreach( Sphere().translate(_).scale(0.01f).draw() )
    // MatrixStack.pop
  }

  override def animate(dt:Float){
    t += dt
  }

  Trackpad.clear
  Trackpad.connect
  Trackpad.bind((touch) => {
    touch.count match {
      case 1 => 
        boost = (touch.pos.x - 0.5f)
      case _ => ()  
    }
  })
}

Script