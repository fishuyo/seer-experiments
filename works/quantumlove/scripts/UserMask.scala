

// video masking test

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._
import com.fishuyo.seer.video._
import scala.math._

import java.nio.ByteBuffer


object UserMask extends SeerScript {

  var initd = false

  var node:MaskBlendNode = _

  var userTexture:Texture = _

  var quad:Model = _

  val user = OpenNI.getUser(1)

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

    RenderGraph.reset
    RenderGraph.addNode(node)

    userTexture = Texture(user.mask) //OpenNI.userImage)

    quad = Plane()
    quad.material = Material.basic
    quad.material.texture = Some(userTexture)
    quad.material.textureMix = 1f

    node.renderer.scene.push(quad)

    initd = true
  }

  override def onUnload(){
  }

  override def draw(){

    FPS.print
    
    // node.renderer.environment.depth = false
    // node.renderer.environment.blend = true
    // node.renderer.environment.blendFunc(SrcAlpha,DstAlpha)

  }
  override def animate(dt:Float){
    if(!initd) init()
    userTexture.update
  }

  import de.sciss.osc.Message
  OSC.clear()
  OSC.disconnect()
  OSC.listen(15000)
  OSC.bindp {
    case Message("/pitch", f:Float) => 
    case Message("/roll", f:Float) =>
    case Message("/yaw", f:Float) => println(s"yaw $f"); //videos(0).setRate(f)
    case msg => println(msg)
  }

  Trackpad.bind { case t => 

    val v = Vec3(t.vel)

    t.count match {
      case _ => ()
    }
  }
}

class MaskBlendNode extends RenderNode {

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

    void main(){

      vec2 uv = (2.0*v_uv) - 1.0;

      // vec4 mask = texture2D(u_texture1, v_uv);
      vec4 user = texture2D(u_texture0, v_uv); 
      gl_FragColor = user;
      // gl_FragColor *= 10000.0;
      // if(user.r == 0.0){
        // gl_FragColor = vec4(1,1,1,1);
      // } else if( user.r > 0.0 && user.r < 0.1){
        // gl_FragColor = vec4(0,1,0,1);
      // }
      // gl_FragColor.r = 1.0;
      // gl_FragColor.a *= (1.0-length(v_pos)); // * 0.5; 
    }
    """
  )
  
  override def render(){
    // renderer.shader.uniforms("blurSize") = size
    super.render()
  }
}

UserMask
