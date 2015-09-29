

package com.fishuyo.seer

import graphics._
import util._
import io._
import spatial._

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._
import com.fishuyo.seer.video._
import scala.math._

import java.nio.ByteBuffer

import collection.mutable.HashMap
import collection.mutable.ArrayBuffer
import collection.mutable.ListBuffer

import com.badlogic.gdx.Gdx

object Master extends SeerApp {

  val music = Gdx.audio.newMusic(Gdx.files.absolute("/Users/fishuyo/Desktop/silverthreads.mp3"));

  var rawaccel1 = Vec3()
  var accel1 = Vec3()
  var env1 = new EnvFollow(20)
  var beat1 = new Threshold(0.1f)

  var rawaccel2 = Vec3()
  var accel2 = Vec3()
  var gyro1 = Vec3()
  var gyro2 = Vec3()

  var files = HashMap[String,String]()
  files += "bubbles" -> "/Users/fishuyo/Desktop/quantumlove/bubbles.mp4"
  files += "cave" -> "/Users/fishuyo/Desktop/quantumlove/cave.mov"
  files += "citystreet" -> "/Users/fishuyo/Desktop/quantumlove/citystreet.mov"
  files += "clouds" -> "/Users/fishuyo/Desktop/quantumlove/clouds.mov"
  files += "crabs" -> "/Users/fishuyo/Desktop/quantumlove/crabs.mov"
  files += "drivetrees" -> "/Users/fishuyo/Desktop/quantumlove/drivetrees.mov"
  files += "fire" -> "/Users/fishuyo/Desktop/quantumlove/fire.mov"
  files += "grass" -> "/Users/fishuyo/Desktop/quantumlove/grass.mov"
  files += "grass1" -> "/Users/fishuyo/Desktop/quantumlove/grass1.mov"
  files += "grass2" -> "/Users/fishuyo/Desktop/quantumlove/grass2.mov"
  files += "onboat" -> "/Users/fishuyo/Desktop/quantumlove/onboat.mov"
  files += "ontrain" -> "/Users/fishuyo/Desktop/quantumlove/ontrain.mov"
  files += "stream" -> "/Users/fishuyo/Desktop/quantumlove/stream.mov"
  files += "subway" -> "/Users/fishuyo/Desktop/quantumlove/subway.mov"
  files += "tidepool" -> "/Users/fishuyo/Desktop/quantumlove/tidepool.mov"
  files += "underwater" -> "/Users/fishuyo/Desktop/quantumlove/underwater.mp4"
  files += "underwater2" -> "/Users/fishuyo/Desktop/quantumlove/underwater2.mp4"
  files += "underwater3" -> "/Users/fishuyo/Desktop/quantumlove/underwater3.mp4"
  files += "water" -> "/Users/fishuyo/Desktop/quantumlove/water.mov"
  files += "wave" -> "/Users/fishuyo/Desktop/quantumlove/waves.mov"

  val randomVideo = Random.oneOf(files.keys.toArray: _*)

  // var videos = ListBuffer[VideoTexture]()
  var video1:VideoTexture = _
  var video2:VideoTexture = _

  var initd = false

  var node:MaskBlendNode = _
  // var node2:MaskBlendNode = _
  // var fbnode:FeedbackNode = _

  OpenNI.initAll()
  OpenNI.start()
  // OpenNI.pointCloud = true
  // OpenNI.pointCloudDensity = 4
  KPC.loadCalibration("calibration.txt")
  val skeletons = (1 to 4).map(OpenNI.getSkeleton(_))


  override def init(){
    println("init")
    node = new MaskBlendNode
    // node2 = new MaskBlendNode
    // fbnode = new FeedbackNode(0.995,0.005)

    RenderGraph.reset
    RenderGraph.addNode(node)
    // node.renderer.clear = false
    // node.outputTo(fbnode)
    // fbnode.outputTo(new ScreenNode())

    // RenderGraph.addNode(node2)
    // node2.renderer.clear = false

    loadVideo1("grass")
    loadVideo2("drivetrees")

    println(s"${video1.width} x ${video1.height}")
    if(video1.height > 0) node.quad.scale.set(1f*video1.width/video1.height, -1, 1)

    initd = true
  }

  def loadVideo1(name:String, wall:Boolean = false){
    var v = video1
    video1 = new VideoTexture(files(name))
    if(wall) OSC.send("/left/load", files(name))
    video1.setRate(0.3)
    video1.setVolume(0.5)
    video1.setAudioChannel(0)
    if(v != null) v.dispose 
  }
  def loadVideo2(name:String, wall:Boolean = false){
    var v = video2
    video2 = new VideoTexture(files(name))
    if(wall) OSC.send("/right/load", files(name))
    video2.setRate(0.3)
    video2.setVolume(0.5)
    video2.setAudioChannel(1)
    if(v != null) v.dispose 
  }
  def loadVideo(name:String, wall:Boolean = false){
    var v = video1
    video1 = new VideoTexture(files(name))
    node.div = 1.1f
    if(wall){
      OSC.send("/left/load", files(name))
      OSC.send("/right/load", files(name))
    }
    video1.setRate(0.3)
    video1.setVolume(0.5)
    video1.setAudioChannel(0)
    if(v != null) v.dispose 
  }

  // override def onUnload(){
    // oscPhones.disconnect
    // videos.foreach( _.dispose )
    // if(video1 != null) video1.dispose
    // if(video2 != null) video2.dispose
    // music.stop; music.dispose
  // }

  override def draw(){

    FPS.print

    video1.update
    video2.update
    video1.texture.bind(0)
    video2.texture.bind(1)

    node.renderer.environment.depth = false
    node.renderer.environment.blend = true
    node.renderer.environment.blendFunc(SrcAlpha,DstAlpha)
    // node2.renderer.environment.depth = false
    // node2.renderer.environment.blend = true
    // node2.renderer.environment.blendFunc(SrcAlpha,OneMinusSrcAlpha)

  }
  override def animate(dt:Float){
    try {
    if(!initd) init()

    // println(s"${video1.width} x ${video1.height}")
    if(video1.height > 0 ) node.quad.scale.set(1f*video1.width/video1.height, -1, 1)

    accel1.lerpTo(rawaccel1, 0.1)
    // println(accel1.mag)
    env1(rawaccel1.mag)
    // println(s"env: ${env1.value}")
    beat1(rawaccel1.mag)
    if(beat1.value > 0f){ 
      println("beat!!!!!!!!!!!!!!!!!!")

    }

    var user = 0
    for( i <- 0 until 4){
      if(skeletons(i).tracking){
        user += 1
        skeletons(i).updateJoints()
        val pos = skeletons(i).joints("torso") * 1000
        pos.z *= -1
        val cam = node.renderer.camera
        val out = KPC.worldToScreen(pos) * Vec3(2,2,0) + Vec3(0.0,-0.1,0)
        // val out = KPC.worldToScreen(pos) * Vec3(cam.viewportWidth, cam.viewportHeight,0) + Vec3(0.0,-0.1,0)
        user match {
          case 1 => node.hole0.set(0.5f*(out.x+1f), 0.5f*(-out.y+1f) )
          case 2 => node.hole1.set(0.5f*(out.x+1f), 0.5f*(-out.y+1f) )
          case _ => ()
        }
      }
    }
    // fbnode.blend0 = 2*Mouse.x()-1
    // fbnode.blend1 = 2*Mouse.y()-1
    // blur.size = 0.01 * abs( 2*sin(Time()))
    // blur.intensity = 0.2 * abs( 2*sin(0.33*Time())) + 0.1
   } catch { case e:Exception => ()} //println(e.getMessage())}
  }

  

  import de.sciss.osc.Message
  OSC.clear()
  OSC.disconnect()
  OSC.listen(8082)
  val oscPhones = new OSCSend
  oscPhones.connect("localhost", 8083)
  OSC.connect("169.231.113.146", 8008)
  OSC.bindp {
    case Message("/gyro1", roll:Float, pitch:Float, yaw:Float) => gyro1.set(pitch,yaw,roll)
    case Message("/gyro2", roll:Float, pitch:Float, yaw:Float) => gyro2.set(pitch,yaw,roll)
    case Message("/accel1", x:Float, y:Float, z:Float) => rawaccel1.set(x,y,z)
    case Message("/accel2", x:Float, y:Float, z:Float) => rawaccel2.set(x,y,z)
    
    case Message("/1/fader1", f:Float) => node.fade = f
    case Message("/1/fader2", f:Float) => OSC.send("/left/fade",f)
    case Message("/1/fader3", f:Float) => OSC.send("/right/fade",f)
    case Message("/1/fader4", f:Float) => node.div = f*1.2f - 0.1f
    case Message("/1/push1", f:Float) => if(f == 1f) loadVideo("grass", true)
    case Message("/1/push2", f:Float) => if(f == 1f) loadVideo("drivetrees", true)
    case Message("/1/push5", f:Float) => if(f == 1f) loadVideo("citystreet", true)
    case Message("/2/fader1", f:Float) => node.size0 = f*4.0f
    case Message("/2/toggle1", f:Float) => node.mode0 = f
    case Message("/2/fader8", f:Float) => node.size1 = f*4.0f
    case Message("/2/toggle8", f:Float) => node.mode1 = f
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
      case 3 => node.div += v.x * 0.002f; if(node.div < -0.1f) node.div = -0.1f; if(node.div > 1.1f) node.div = 1.1f
      case 4 => //videos(2).quad.pose.pos += v * 0.01
                // OSC.send("/left/fade",t.pos.y)
                // node.size0 += v.y * 0.01
                // node.size1 += v.x * 0.01
                // volume += v.y * 0.01
                // rate += v.x * 0.01
                // video1.setVolume(volume)
                // video1.setRate(rate)

      case _ =>  
    }
  }

  // Keyboard.bind("1", ()=>{ video1 = videos(0)})
  // Keyboard.bind("2", ()=>{ video1 = videos(1)})
  // Keyboard.bind("3", ()=>{ video1 = videos(2)})
  Keyboard.bind("4", ()=>{ loadVideo1(randomVideo(),true) })
  // Keyboard.bind("7", ()=>{ video2 = videos(0)})
  // Keyboard.bind("8", ()=>{ video2 = videos(1)})
  // Keyboard.bind("9", ()=>{ video2 = videos(2)})
  Keyboard.bind("0", ()=>{ loadVideo2(randomVideo(),true) })

  Keyboard.bind("p", ()=>{ if(music.isPlaying()) music.pause() else music.play() })
  Keyboard.bind("m", ()=>{ if(node.mode0 == 0f) node.mode0 = 1f else node.mode0 = 0f })
}

class MaskBlendNode extends RenderNode {
  val quad = Plane() //.scale(width/height,-1,1)
  renderer.scene.push( quad )
  renderer.shader = Shader.load("shaders/mask")
  renderer.shader.monitor
  renderer.shader.uniforms("u_texture0") = 0
  renderer.shader.uniforms("u_texture1") = 1
  
  var div = 0.5f
  var fade = 1.0f
  var hole0 = Vec2(-10,-10)
  var hole1 = Vec2(-10,-10)
  var size0 = 1.0f
  var size1 = 1.0f
  var mode0 = 0.0
  var mode1 = 0.0

  override def render(){
    renderer.shader.uniforms("u_texture0") = 0
    renderer.shader.uniforms("u_texture1") = 1
    renderer.shader.uniforms("u_div") = div
    renderer.shader.uniforms("u_fade") = fade
    renderer.shader.uniforms("u_hole0") = hole0
    renderer.shader.uniforms("u_hole1") = hole1
    renderer.shader.uniforms("u_size0") = size0
    renderer.shader.uniforms("u_size1") = size1
    renderer.shader.uniforms("u_mode0") = mode0
    renderer.shader.uniforms("u_mode1") = mode1
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
      thresh = math.abs(in) + 0.0000001f
      t = 0
    }
    value
  }
}

