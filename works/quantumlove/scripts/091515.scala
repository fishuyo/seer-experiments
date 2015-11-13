
// toward better masking, bounding ellipse box and sillouette

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._
import com.fishuyo.seer.video._
import scala.math._

import java.nio.ByteBuffer


object VideoTest extends SeerScript {

  var videos = collection.mutable.ListBuffer[VideoTexture]()
  var smodel = collection.mutable.ListBuffer[Model]()

  var initd = false

  var node:MaskBlendNode = _


  var quad:Model = _

  // OpenNI.initAll()
  // OpenNI.start()
  // OpenNI.pointCloud = true
  // OpenNI.pointCloudDensity = 4
  // KPC.loadCalibration("calibration.txt")
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

    RootNode.renderer.camera = new OrthographicCamera(1,1) //2.0f*(1280.0f/800.0f),2)
    RootNode.renderer.camera.nav.pos.z = 2


    RenderGraph.reset
    RenderGraph.addNode(node)
    node.outputTo(fbnode)
    fbnode.outputTo(new ScreenNode())

    RenderGraph.addNode(node2)
    node2.renderer.clear = false

    // RootNode.outputTo(blur)

    videos += new VideoTexture("/Users/fishuyo/Desktop/water.mov")
    videos += new VideoTexture("/Users/fishuyo/Desktop/fire.mov")
    videos += new VideoTexture("/Users/fishuyo/Desktop/land.mov")
    videos(1).setRate(0.1)
    // video2.setRate(0.5)

    smodel += Sphere().scale(0.1)
    smodel += Sphere().scale(0.1)
    smodel += Sphere().scale(0.1)

    // videos.foreach( node.renderer.scene.push(_) )
    // smodel.foreach( node.renderer.scene.push(_) )
    smodel.foreach( node2.renderer.scene.push(_) )
    // videos.foreach( node2.renderer.scene.push(_) )
    // node.renderer.scene.push(video2)

    initd = true
  }

  override def onUnload(){
    videos.foreach( _.dispose )
  }

  override def draw(){

    FPS.print
    

    node.renderer.environment.depth = false
    node.renderer.environment.blend = true
    node.renderer.environment.blendFunc(SrcAlpha,DstAlpha)
    node2.renderer.environment.depth = false
    node2.renderer.environment.blend = true
    node2.renderer.environment.blendFunc(SrcAlpha,OneMinusSrcAlpha)
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
        val out = KPC.worldToScreen(p) * 
        videos(i).quad.pose.pos.lerpTo(out,0.1f)
        smodel(i).pose.pos.lerpTo(out,0.1f)
       // videos(i).quad.pose.pos.lerpTo(pos,0.1f)
        // println(pos)
        // println(out)
      }
    }
    // video2.setRate(Mouse.x())
    // fbnode.blend0 = 2*Mouse.x()-1
    // fbnode.blend1 = 2*Mouse.y()-1

    // blur.size = 0.01 * abs( 2*sin(Time()))
    // blur.intensity = 0.2 * abs( 2*sin(0.33*Time())) + 0.1

    // tex1.data.asInstanceOf[ByteBuffer].put(OpenNI.maskBytes1)
    // tex1.update
    // tex2.data.asInstanceOf[ByteBuffer].put(OpenNI.maskBytes1)
    // tex2.update
    // tex3.data.asInstanceOf[ByteBuffer].put(OpenNI.maskBytes1)
    // tex3.update
    // tex4.data.asInstanceOf[ByteBuffer].put(OpenNI.maskBytes1)
    // tex4.update


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

    t.count match {
      case 2 => videos(0).quad.pose.pos += v * 0.01
      case 3 => videos(1).quad.pose.pos += v * 0.01
      case 4 => videos(2).quad.pose.pos += v * 0.01
      case _ => ()
    }
  }

  // Keyboard.bind("1", ()=>{ video2.load("/Users/fishuyo/Desktop/land.mov") })
  // Keyboard.bind("2", ()=>{ video2.load("/Users/fishuyo/Desktop/fire.mov") })
  // Keyboard.bind("3", ()=>{ video2.load("/Users/fishuyo/Desktop/water.mov") })
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
      gl_FragColor.a *= (1.0-length(v_pos)); // * 0.5;
      gl_FragColor = vec4(1,1,1,1); 
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
