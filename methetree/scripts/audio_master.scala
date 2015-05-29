

import com.fishuyo.seer.audio.gen._
import com.fishuyo.seer.audio._

import scala.concurrent.duration._

object AudioMaster extends SeerScript {


  //Scene 1
  val noiseTime1 = 10
  val noise1 = new Noise
  var ramp1 = new Ramp(0,0.6,44100*noiseTime1)
  Schedule.after(noiseTime1.seconds){ ramp1 = new Ramp(0.8,0,10000)}
  
  //Scene 2
  val noise = new Noise
  val lfo = new Sine(0.1, 0.025)
  val osc = new Sine(80f)
  val lfo2 = new Sine(1,0.1)
  val del = new Delay(100f, 0.9f)
  // val del = new Delay((new Sine(1,1f))*100f+101f, 0.6f)
  var pan = new Sine(0.1, 0.5)

  var pulse = new PulseTrain(44100f)
  var impulseAmp = 0f
  val del2 = new Delay(4000f, 0.98f)


  override def audioIO(io:AudioIOBuffer){
    while(io()){
      val s = noise1() * ramp1()
      io.outSet(0)(s)
      io.outSet(1)(s)
    }
    // while(io()){

    //   var s = del(lfo()*noise() + del2(pulse()*impulseAmp) )
    //   var s2 = lfo2()*osc()
    //   // s +=
    //   val p = pan()+0.5f 
    //   val r = s*p + s2
    //   val l = s*(1f-p) + s2


    //   io.outSet(0)(l)
    //   io.outSet(1)(r)
    // }
  }


}

AudioMaster