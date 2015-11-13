
import com.fishuyo.seer._

import dynamic.SeerScript
import graphics._
import io._

object Script extends SeerScript {

  OSC.clear
  OSC.disconnect

  OSC.connect("localhost",12000)

  val app = """
    app = {
      name:'cats',

      receivers: [
        { type:'OSC', ip:'127.0.0.1', port:12001 },
      ],
      
      inputs: {
        blah:  { min: 200, max: 300, receivers:0 },
        blah2: { min: 0, max: 1 },
      },
      
      outputs :{},

      mappings: [
        { input: { io:'keypress', name:'b' }, output:{ io:'cats', name:'blah2' } },
      ]
    }
  """

  OSC.listen(12001)
  OSC.bindp {
    case msg => println(msg)
  }

  Keyboard.clear
  Keyboard.use

  Keyboard.bind("g", () =>{
    println("hi")
    OSC.send("/interface/applicationManager/createApplicationWithText", app)
  })

}

Script