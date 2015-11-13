
import com.fishuyo.seer._

import dynamic.SeerScript
import graphics._
import audio._
import io._
import types._

class Delay(in:Gen) extends Gen {
  var buf = new RingBuffer[Float](44100)
  var d:Gen = new Var(1.f)
  var s = 0.f
  def apply() = {
    buf += in()
    val z = math.abs(d())
    while( z < buf.size) s = buf.next
    s
  }
}
object D {
  def apply(in:Gen) = new Delay(in)
}

object Script extends SeerScript {

  val osc = new Sine(440,1)
  val lfo = new Saw(4,1)
  // val n = m map ( _ * -1.f + 440.f)

  var pitch = 440.f
  // var lforate = 0.001f
  // var lfodepth = 1.f

  // c.f = n
  val fm = new Gen {
    def apply() = {
      osc.f = pitch + (lfo() * -1.f)
      osc()
    }
  }
  val am = new Gen {
    def apply() = {
      osc.f = pitch
      osc() * (lfo() * -.01f * 0.5f + .5f)
    }
  }
  var g = am
  Audio().push(g)

  override def preUnload(){
    Audio().sources -= g
  }

  override def draw(){}
  override def animate(dt:Float){}

  Trackpad.clear
  Trackpad.connect
  Trackpad.bind((touch) => {
    touch.count match{
      case 1 => 
        pitch = touch.pos.x * 1000
      case 2 =>
        lfo.f = touch.pos.x * 100.f - 50.f 
        lfo.a = touch.pos.y * 100 

        // d.d = touch.pos.x * 44100
      case _ => ()
    }
  })
}

Script