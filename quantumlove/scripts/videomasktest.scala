

// video masking test

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._
import com.fishuyo.seer.video._
import scala.math._

import java.nio.ByteBuffer


object VideoTest extends SeerScript {

  var videos = collection.mutable.ListBuffer[VideoTexture]()
  // var video2:VideoTexture = _

  var initd = false

  var node:MaskBlendNode = _
  var node2:MaskBlendNode = _
  var fbnode:FeedbackNode = _

  var tex1:Texture = _
  var tex2:Texture = _
  var tex3:Texture = _
  var tex4:Texture = _

  var user:Texture = _

  var quad:Model = _

  OpenNI.initAll()
  OpenNI.start()
  // OpenNI.pointCloud = true
  // OpenNI.pointCloudDensity = 4
  KPC.loadCalibration("calibration.txt")
  val skeletons = (1 to 4).map(OpenNI.getSkeleton(_))


  // RootNode.renderer.camera = new OrthographicCamera(2,2)
  // RootNode.renderer.camera.nav.pos.z = 2
  // Renderer().camera = Camera


  override def init(){
    node = new MaskBlendNode
    node2 = new MaskBlendNode
    fbnode = new FeedbackNode(0.995,0.005)
    // fbnode = new FeedbackNode(0.99,0.1)
    // fbnode = new FeedbackNode(0.95,0.05)
    // fbnode = new FeedbackNode(0.9,0.1)
    // fbnode = new FeedbackNode(0,1)
    // fbnode.mode = 0

    RenderGraph.reset
    RenderGraph.addNode(node)
    // node.outputTo(fbnode)
    // fbnode.outputTo(new ScreenNode())

    // RenderGraph.addNode(node2)
    // node2.renderer.clear = false

    // RootNode.outputTo(blur)

    videos += new VideoTexture("/Users/fishuyo/Desktop/water.mov")
    videos += new VideoTexture("/Users/fishuyo/Desktop/fire.mov")
    videos += new VideoTexture("/Users/fishuyo/Desktop/land.mov")
    // videos += new VideoTexture("/Users/fishuyo/Desktop/uwater.mp4")
    // videos += new VideoTexture("/Users/fishuyo/Desktop/bubbles.mp4")
    videos(0).setRate(0.1)
    videos(1).setRate(0.1)
    videos(2).setRate(0.1)
    // video2.setRate(0.5)

    // tex1 = Texture(640,480)
    // tex2 = Texture(640,480)
    // tex3 = Texture(640,480)
    // tex4 = Texture(640,480)

    // user = Texture(OpenNI.userImage)

    // quad = Plane()
    // quad.material = Material.basic
    // quad.material.color = RGBA(1,0,0,1)
    // quad.material.texture = Some(user)
    // quad.material.textureMix = 1f

    // node.renderer.scene.push(quad)
    // node2.renderer.scene.push(quad)

    videos.foreach( node.renderer.scene.push(_) )
    // videos.foreach( node2.renderer.scene.push(_) )
    // node.renderer.scene.push(video2)

    initd = true
  }

  override def onUnload(){
    videos.foreach( _.dispose )
    // video2.dispose
  }

  override def draw(){

    FPS.print
    
    // tex1.bind(1);
    // node2.renderer.shader.uniforms("u_texture1") = 1


    node.renderer.environment.depth = false
    node.renderer.environment.blend = true
    node.renderer.environment.blendFunc(SrcAlpha,DstAlpha)
    // node2.renderer.environment.depth = false
    // node2.renderer.environment.blend = true
    // node2.renderer.environment.blendFunc(SrcAlpha,OneMinusSrcAlpha)
    // Renderer().environment.alpha = 0.1f
    // Renderer().environment.lineWidth = 1f

  }
  override def animate(dt:Float){
    if(!initd) init()

    for( i <- 0 until videos.length){
      if(skeletons(i).tracking){
        skeletons(i).updateJoints()
        val pos = skeletons(i).joints("torso")
        val p = pos * 1000
        p.z *= -1
        val out = KPC.worldToScreen(p)
        // videos(i).quad.pose.pos.lerpTo(out,0.1f)
        videos(i).quad.pose.pos.lerpTo(pos,0.1f)
        // println(pos)
        // println(out)
      }
    }
    // video2.setRate(Mouse.x())
    // fbnode.blend0 = 2*Mouse.x()-1
    // fbnode.blend1 = 2*Mouse.y()-1

    // blur.size = 0.01 * abs( 2*sin(Time()))
    // blur.intensity = 0.2 * abs( 2*sin(0.33*Time())) + 0.1

  }

  import de.sciss.osc.Message
  OSC.clear()
  OSC.disconnect()
  OSC.listen(15000)
  OSC.bindp {
    case Message("/pitch", f:Float) => 
    case Message("/roll", f:Float) =>
    case Message("/yaw", f:Float) => println(s"yaw $f"); videos(0).setRate(f)
    case msg => println(msg)
  }

  Trackpad.bind { case t => 

    val v = Vec3(t.vel)
    val i = selected - 1

    t.count match {
      case 2 => videos(i).quad.pose.pos += v * 0.01
      case 3 => videos(i).scale += v.y * 0.01
      // case 4 => videos(2).quad.pose.pos += v * 0.01
      case _ => ()
    }
  }

  var selected = 1
  Keyboard.bind("1", ()=>{ selected = 1 })
  Keyboard.bind("2", ()=>{ selected = 2 })
  Keyboard.bind("3", ()=>{ selected = 3 })
}

class MaskBlendNode extends RenderNode {

  var size = 1f/512f
  var intensity = 0.35f

  // renderer.scene = Scene //.push(Plane())
  // renderer.camera.nav.pos.z = 2


  renderer.shader = Shader.load(
    """
    attribute vec4 a_position;
    attribute vec4 a_normal;
    attribute vec2 a_texCoord0;
    attribute vec4 a_color;

    uniform mat4 u_projectionViewMatrix;

    varying vec2 v_uv;
    varying vec3 v_pos;

    void main() {
      gl_Position = u_projectionViewMatrix * a_position;
      v_pos = a_position.xyz;
      v_uv = a_texCoord0;
      // v_color = a_color;
      // v_pos = a_position.xyz;
    }
    """,

    """
    #ifdef GL_ES
        precision mediump float;
    #endif

    varying vec2 v_uv;
    varying vec3 v_pos;

    uniform sampler2D u_texture0;
    // uniform sampler2D u_texture1;
    // uniform float blurSize;
    // uniform float intensity;

    void main(){
      // const float blurSize = 1.0/512.0;
      // const float intensity = 0.35;

      vec4 sum = vec4(0);
      vec2 texcoord = v_uv;
      vec2 uv = (2.0*v_uv) - 1.0;

      // vec4 mask = texture2D(u_texture1, v_uv);
      gl_FragColor = texture2D(u_texture0, v_uv); 
      // gl_FragColor.a *= (1.0-length(v_pos)); // * 0.5; 
    }
    """
  )
  
  override def render(){
    // renderer.shader.uniforms("blurSize") = size
    // renderer.shader.uniforms("intensity") = intensity
    super.render()
  }
}

VideoTest
