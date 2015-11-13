
import com.fishuyo.seer.tuningtree._
import com.fishuyo.seer.audio._

object Script extends SeerScript {


Trackpad.clear
Trackpad.connect
Trackpad.bind( (tp) => {

  // var t = new ATree
  // if( idx >= 0){ 
    val t = TuningTree.tree
    t.visible = 1
  // }

  tp.count match {
    case 1 =>
      val ur = Vec3(1,0,0) //Camera.nav.ur()
      val uf = Vec3(0,0,1) //Camera.nav.uf()

      
        t.root.applyForce( ur*(tp.pos.x-0.5) * 2.0*tp.size )
        t.root.applyForce( uf*(tp.pos.y-0.5) * -2.0*tp.size )
    case 2 =>
      t.mx += tp.vel.x*0.05  
      t.my += tp.vel.y*0.05
    case 3 =>
      t.ry += tp.vel.x*0.05  
      t.mz += tp.vel.y*0.01
      if (t.mz < 0.08) t.mz = 0.08
      if (t.mz > 3.0) t.mz = 3.0 
    case 4 =>
      t.rz += tp.vel.y*0.05
      t.rx += tp.vel.x*0.05
    case _ => ()
  }

  // t.root.pose.pos.set(t.mx,t.my,0)

  if(tp.count > 2){
    t.update(t.mz,t.rx,t.ry,t.rz) 

  }
})

  val looper = TuningTree.looper
  var l = 0
  var bank = 0
  var b = 0

  var gain = 0f
  var pedalMode = 0

val m = Model()
looper.plots(2).pose.quat *= Quat().fromEuler(0,90f.toRadians,0)
looper.plots(2).pose.pos += Vec3(1,0,0)
m.addPrimitive(looper.plots(2))
TreeNode.model = m

  // looper.setMode("sync")
  // looper.setMaster(0)

  Keyboard.clear()
  Keyboard.use()
  Keyboard.bind("1",() => { l=0; TuningTree.l = 0 })
  Keyboard.bind("2",() => { l=1; TuningTree.l = 1 })
  Keyboard.bind("3",() => { l=2 })
  Keyboard.bind("4",() => { l=3 })
  Keyboard.bind("5",() => { l=4 })
  Keyboard.bind("6",() => { l=5 })
  Keyboard.bind("7",() => { l=6 })
  Keyboard.bind("8",() => { l=7 })
  Keyboard.bind("r",() => { looper.toggleRecord(l) })
  Keyboard.bind("c",() => { looper.stop(l); looper.clear(l) })
  Keyboard.bind("x",() => { looper.stack(l) })
  Keyboard.bind("t",() => { looper.togglePlay(l) })
  Keyboard.bind(" ",() => { looper.reverse(l) })
  Keyboard.bind("p",() => { looper.switchTo(l) })
  Keyboard.bind("m",() => { looper.setMaster(l) })
  Keyboard.bind("b",() => { looper.duplicate(l,1) })
  Keyboard.bind("l",() => { Audio().toggleRecording() })
  Keyboard.bind("y",() => {
    if(looper.loops.apply(l).vocoderActive){
      looper.loops.apply(l).vocoderActive(false)
    } else {
      looper.loops.apply(l).analyze() 
      looper.loops.apply(l).vocoder.timeShift(1.0) 
      looper.loops.apply(l).vocoder.pitchShift(1.0) 
      looper.loops.apply(l).vocoderActive(true)
    }
  })
  Keyboard.bind("u",() => {
    if(looper.loops.apply(l).vocoder.convert){
      looper.loops.apply(l).vocoder.convert(false)
    } else {
      looper.loops.apply(l).vocoder.convert(true)
    }
  })

  Keyboard.bind("j",() => { looper.save("") })
  Keyboard.bind("k",() => { looper.load("project-Nov-3,-2013-1-21-06-AM") })

  Keyboard.bind("f", () => {
    // OSC.send("127.0.0.1", 8001, "/test", "f")
    looper.setGain(l,1)
    looper.setSpeed(l,1)
    looper.setBounds(l,0,1)
    looper.toggleRecord(l)
  })
  Keyboard.bind("g", () => {
    looper.setGain(l,1)
    looper.setSpeed(l,1)
    looper.setBounds(l,0,1)
    looper.setDecay(l,0.99)
    looper.stack(l)
  })

  var down = false
  // s = []
  // t = []
  var width = 0.001
  
  Trackpad.bind( (touch) => {
      // #  a = []
      // # for ff in f do
      // #  a.push(ff)
      // # end
          // #puts a.slice(5,4).join(" ")

    touch.count match {
      case 3 => 
        val f1 = touch.fingers(0)   
        val f2 = touch.fingers(1)   
        val f3 = touch.fingers(2)   
        looper.loops(l).vocoder.timeShift(f3.pos.y*8.0-4.0)
        looper.loops(l).vocoder.pitchShift(f2.pos.y*4.0)
        looper.loops(l).vocoder.gain(f1.pos.y)
        down = true
      case 2 =>
        val f1 = touch.fingers(0)   
        val f2 = touch.fingers(1)   
        looper.setGain(l,f1.pos.y)
        looper.setSpeed(l,f2.pos.y*2.0)
        looper.setBounds(l,f1.pos.x,f2.pos.x)
        
        looper.loops.apply(l).vocoder.pitchShift(f2.pos.y*4.0)
        looper.loops.apply(l).vocoder.gain(f1.pos.y)
        down = false
      case 1 =>
        val f1 = touch.fingers(0)   

        looper.setPan(l,f1.pos.x)
        looper.setDecay(l,f1.pos.y)

        width += 0.01f*touch.vel.y
        if(width < 0.0) width = 0.0
        if (width > 1.0 - touch.pos.x) width = 1.0 - touch.pos.x
        var e = touch.pos.x + width
        if (e > 1.0) e = 1.0 
        // #looper.setBounds(l,f(0),e)

        down = false
      case _ => ()
    }
  })




}





// must return this from script
Script