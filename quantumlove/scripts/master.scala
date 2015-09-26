
// toward better masking, bounding ellipse box and sillouette

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._
import com.fishuyo.seer.video._
import scala.math._

import java.nio.ByteBuffer


object Master extends SeerScript {

  var videos = collection.mutable.ListBuffer[VideoTexture]()
  var video1:VideoTexture = _
  var video2:VideoTexture = _

  var initd = false

  var node:MaskBlendNode = _
  var node2:MaskBlendNode = _
  var fbnode:FeedbackNode = _


  var quad:Model = _

  // OpenNI.initAll()
  // OpenNI.start()
  // OpenNI.pointCloud = true
  // OpenNI.pointCloudDensity = 4
  // KPC.loadCalibration("calibration.txt")
  val skeletons = (1 to 4).map(OpenNI.getSkeleton(_))


  override def init(){
    node = new MaskBlendNode
    node2 = new MaskBlendNode
    fbnode = new FeedbackNode(0.995,0.005)

    // RootNode.renderer.camera = new OrthographicCamera(1,1) //2.0f*(1280.0f/800.0f),2)
    // RootNode.renderer.camera.nav.pos.z = 2

    RenderGraph.reset
    RenderGraph.addNode(node)
    // node.outputTo(fbnode)
    // fbnode.outputTo(new ScreenNode())

    // RenderGraph.addNode(node2)
    // node2.renderer.clear = false

    videos += new VideoTexture("/Users/fishuyo/Desktop/land.mov")
    videos += new VideoTexture("/Users/fishuyo/Desktop/water.mov")
    // videos += new VideoTexture("/Users/fishuyo/Desktop/fire.mov")
    videos += new VideoTexture("/Users/fishuyo/Desktop/bubbles.mp4")
    video1 = videos(0)
    video2 = videos(1)

    videos.foreach { case v =>
      v.setVolume(0f)
      v.setRate(0.3)
    }

    node.quad.scale(1f*videos(0).width/videos(0).height, -1, 1)

    // videos.foreach( node.renderer.scene.push(_) )
    // videos.foreach( node2.renderer.scene.push(_) )

    initd = true
  }

  override def onUnload(){
    videos.foreach( _.dispose )
  }

  override def draw(){

    FPS.print

    video1.update
    video2.update
    video1.texture.bind(0)
    video2.texture.bind(1)


    node.renderer.environment.depth = false
    node.renderer.environment.blend = true
    node.renderer.environment.blendFunc(SrcAlpha,DstAlpha)
    node2.renderer.environment.depth = false
    node2.renderer.environment.blend = true
    node2.renderer.environment.blendFunc(SrcAlpha,OneMinusSrcAlpha)

  }
  override def animate(dt:Float){
    if(!initd) init()

    accel1.lerpTo(rawaccel1, 0.1)
    // println(accel1.mag)
    env1(rawaccel1.mag)
    println(s"env: ${env1.value}")
    beat1(rawaccel1.mag)
    if(beat1.value > 0f){ 
      println("beat!!!!!!!!!!!!!!!!!!")
      video1 = videos(Random.int(0,3)())
    }

    for( i <- 0 until videos.length){
      if(skeletons(i).tracking){
        skeletons(i).updateJoints()
        val pos = skeletons(i).joints("torso")
        val p = pos * 1000
        p.z *= -1
        val out = KPC.worldToScreen(p) * 1f
        videos(i).quad.pose.pos.lerpTo(out,0.1f)
        // smodel(i).pose.pos.lerpTo(out,0.1f)
       // videos(i).quad.pose.pos.lerpTo(pos,0.1f)
        // println(pos)
        // println(out)
      }
    }
    // fbnode.blend0 = 2*Mouse.x()-1
    // fbnode.blend1 = 2*Mouse.y()-1
    // blur.size = 0.01 * abs( 2*sin(Time()))
    // blur.intensity = 0.2 * abs( 2*sin(0.33*Time())) + 0.1

  }

  var rawaccel1 = Vec3()
  var accel1 = Vec3()
  var env1 = new EnvFollow(20)
  var beat1 = new Threshold(0.1f)

  var rawaccel2 = Vec3()
  var accel2 = Vec3()
  var gyro1 = Vec3()
  var gyro2 = Vec3()

  import de.sciss.osc.Message
  OSC.clear()
  OSC.disconnect()
  OSC.listen(15000)
  OSC.bindp {
    case Message("/pitch1", f:Float) => gyro1.x = f
    case Message("/roll1", f:Float) => gyro1.z = f; node.div = f
    case Message("/yaw1", f:Float) => gyro1.y = f
    case Message("/x1", f:Float) => rawaccel1.x = f
    case Message("/y1", f:Float) => rawaccel1.y = f
    case Message("/z1", f:Float) => rawaccel1.z = f
    case Message("/pitch2", f:Float) => gyro2.x = f
    case Message("/roll2", f:Float) => gyro2.z = f;
    case Message("/yaw2", f:Float) => gyro2.y = f
    case Message("/x2", f:Float) => rawaccel2.x = f
    case Message("/y2", f:Float) => rawaccel2.y = f
    case Message("/z2", f:Float) => rawaccel2.z = f
    case msg => println(msg)
  }

  Trackpad.bind { case t => 

    val v = Vec3(t.vel)
    // node.div = t.pos.x
        node.hole0.set(-10,-10)
        node.hole1.set(-10,-10)

    t.count match {
      case 1 => node.hole0.set(t.fingers(0).pos);
                node.hole0.y = 1.0f - node.hole0.y
      case 2 => node.hole0.set(t.fingers(0).pos); node.hole1.set(t.fingers(1).pos)
                node.hole0.y = 1.0f - node.hole0.y
                node.hole1.y = 1.0f - node.hole1.y
      case 3 => node.div += v.x * 0.002f; if(node.div < 0) node.div = 0; if(node.div > 1) node.div = 1
      case 4 => videos(2).quad.pose.pos += v * 0.01
      case _ =>  
    }
  }

  Keyboard.bind("1", ()=>{ video1 = videos(0)})
  Keyboard.bind("2", ()=>{ video1 = videos(1)})
  Keyboard.bind("3", ()=>{ video1 = videos(2)})
  Keyboard.bind("8", ()=>{ video2 = videos(0)})
  Keyboard.bind("9", ()=>{ video2 = videos(1)})
  Keyboard.bind("0", ()=>{ video2 = videos(2)})
}

class MaskBlendNode extends RenderNode {
  val quad = Plane() //.scale(width/height,-1,1)
  renderer.scene.push( quad )
  renderer.shader = Shader.load("shaders/mask")
  // renderer.shader.monitor
  renderer.shader.uniforms("u_texture0") = 0
  renderer.shader.uniforms("u_texture1") = 1
  
  var div = 0.5f
  var hole0 = Vec2(-10,-10)
  var hole1 = Vec2(-10,-10)

  override def render(){
    renderer.shader.uniforms("u_div") = div
    renderer.shader.uniforms("u_hole0") = hole0
    renderer.shader.uniforms("u_hole1") = hole1
    super.render()
  }
}

class EnvFollow(val size:Int = 100) extends audio.Gen {
  def apply() = this(0f)
  def apply(in:Float) = {
    value -= value/size
    value += math.abs(in)/size
    value
  }
}

class Threshold(var thresh:Float) extends audio.Gen {
  var hold = 35
  var t = hold
  def apply() = this(0f)
  def apply(in:Float) = {
    value = 0f
    t += 1
    if( t > hold && math.abs(in) > thresh){
      value = 1f 
      t = 0
    }
    value
  }
}

class BeatTrack extends audio.Gen {
  var thresh = 0.1f
  var decay = 0.0001f
  var hold = 5
  var t = hold
  def apply() = this(0f)
  def apply(in:Float) = {
    value = 0f
    thresh -= decay
    t += 1
    if( t > hold && math.abs(in) > thresh){
      value = 1f
      thresh = math.abs(in) + 0.0000001
      t = 0
    }
    value
  }
}

Master
