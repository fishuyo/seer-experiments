
import com.fishuyo.seer._

import dynamic.SeerScript
import graphics._
import audio._
import io._

object Script extends SeerScript {

  var t = 0.f
  var depth = 8.f
  var x,y = 0.1f

  val osc = new Sine(440,1)
  val lfo = new Saw(4.f,10)
  val freq = lfo.map( _ * -1.f + depth*80.f)

  osc.f = freq

  override def draw(){
    Shader.alpha(0.1f)
    Shader.blend("oneMinusSrc")
    Shader.lineWidth(1.f)

    Sphere().mesh.primitive = Lines

    recurse(depth.toInt)
  }

  def recurse(d:Int){
    import MatrixStack._
    if(d > 0){
      push()
      translate(0,y*0.1f,0)
      rotate(0,0.0f,x)
      translate(0,y*0.1f,0)
      Shader.defaultMaterial.color = RGB(d*y.abs,y.abs,0)
      Sphere().scale(.01f,y*0.1f,.01f).draw
      recurse(d-1)
      pop()

      push()
      translate(0,y*0.1f,0)
      rotate(0,0.0f,-x)
      translate(0,y*0.1f,0)
      Shader.defaultMaterial.color = RGB(d*y.abs,x.abs,0)
      Sphere().scale(.01f,y*0.1f,.01f).draw
      recurse(d-1)
      pop()
    }
  }

  override def animate(dt:Float){
    t += dt
  }

  override def audioIO(io:AudioIOBuffer){
    osc.audioIO(io)
  }

  Trackpad.clear
  Trackpad.connect
  Trackpad.bind((touch) => {
    touch.count match {
      case 1 => 
        x += (touch.vel.x*0.01f)
        y += touch.vel.y*0.01f
        lfo.f = x*50
        lfo.a = y*100
      case 3 =>
        depth += touch.vel.y*0.1f
        if(depth > 11.f) depth = 11.f
      case _ => ()  
    }
  })
}

Script