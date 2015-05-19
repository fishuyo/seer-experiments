
import com.fishuyo.seer.io
import com.fishuyo.seer.maths
import com.fishuyo.seer.maths.particle
import com.fishuyo.seer.graphics

import com.fishuyo.seer.examples.particleSystems.verletFabric

class Script extends SeerScript {

  var fabric = Main.fabric()



}



Keyboard.clear()
Keyboard.use()
Keyboard.bind("g",function(){ 
  if( Gravity.y() == 0.0) Gravity.set(0,-10,0)
  else Gravity.set(0,0,0)
})


Trackpad.connect()
Trackpad.clear()
Trackpad.bind((t) => {
  var s=10.0
  if( t.count == 1){
  }else if( t.count == 2){
    // Main.fabric().pins().head().position().lerpTo(Vec3(s*f[5],s*f[6],0), 0.1)
    // Main.fabric().pins().last().position().lerpTo(Vec3(s*f[7],s*f[8],0), 0.1)
  }
})



