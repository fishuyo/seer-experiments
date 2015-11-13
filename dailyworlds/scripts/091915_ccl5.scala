


// video masking test

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._
import com.fishuyo.seer.video._
import scala.math._

import java.nio.ByteBuffer


object AVFeedback extends SeerScript {

  var initd = false

  var node:MaskBlendNode = _

  OpenNI.initAll()
  OpenNI.start()
  // OpenNI.pointCloud = true
  // OpenNI.pointCloudDensity = 4
  val skeletons = (1 to 4).map(OpenNI.getSkeleton(_))

  var quad:Model = _
  var rgbTexture:Texture = _
  var stillTexture:Texture = _

  override def init(){
    node = new MaskBlendNode

    rgbTexture = Texture(OpenNI.rgbImage)
    stillTexture = Texture(rgbTexture.w, rgbTexture.h, rgbTexture.format)
    quad = Plane().scale(640f/480f,-1,1).translate(0,0,0)
    quad.material = Material.basic
    quad.material.texture = Some(rgbTexture)
    quad.material.textureMix = 1f


    node.renderer.camera = new OrthographicCamera(2,2) //2.0f*(1280.0f/800.0f),2)
    // node.renderer.camera.nav.pos.z = 2

    RenderGraph.reset
    RenderGraph.addNode(node)
    // node.outputTo(fbnode)
    // fbnode.outputTo(new ScreenNode())
    node.renderer.scene.push(quad)

    initd = true
  }

  // override def onUnload(){
  // }

  override def draw(){

    FPS.print
    // quad.draw

    // node.renderer.environment.depth = false
    // node.renderer.environment.blend = true
    // node.renderer.environment.blendFunc(SrcAlpha,DstAlpha)
    // Renderer().environment.alpha = 0.1f
    // Renderer().environment.lineWidth = 1f

  }
  override def animate(dt:Float){
    if(!initd) init()

    val cam = node.renderer.camera
    // println(s"${cam.viewportWidth} ${cam.viewportHeight}")
    // quad.scale.set(cam.viewportWidth, -cam.viewportHeight, 0)
    rgbTexture.update
    if(stillModeGo){
      stillTexture.byteBuffer.put(rgbTexture.byteBuffer)
      stillTexture.update
      quad.material.texture = Some(stillTexture)
      stillModeGo = false
    } else if( stillModeStop){
      quad.material.texture = Some(rgbTexture)
      stillModeStop = false
    }
    
    drot *= 0.25
    rot += drot*dt*4
    quad.pose.quat.slerpTo( Quat(0,0,rot), 0.01)    

  }

  // import de.sciss.osc.Message
  // OSC.clear()
  // OSC.disconnect()
  // OSC.listen(15000)
  // OSC.bindp {
  //   case Message("/pitch", f:Float) => 
  //   case Message("/roll", f:Float) =>
  //   case Message("/yaw", f:Float) => println(s"yaw $f"); videos(0).setRate(f)
  //   case msg => println(msg)
  // }

  var rot = 0f
  var drot = 0f
  Trackpad.bind { case t => 

    val v = Vec3(t.vel)

    t.count match {
      // case 2 => videos(0).quad.pose.pos += v * 0.01
      // case 3 => videos(1).quad.pose.pos += v * 0.01
      case 4 => drot = -v.x //*0.01f
                // rot += drot
                // quad.pose.quat.slerpTo( Quat(0,0,rot), 0.01)
      case _ => ()
    }
  }

  var stillMode = false
  var stillModeGo = false
  var stillModeStop = false
  Keyboard.bind("s", ()=>{ stillMode = !stillMode;  if(stillMode) stillModeGo = true else stillModeStop = true})
  Keyboard.bind("q", ()=>{ drot=0; rot += (90.0f.toRadians) })
  Keyboard.bind("e", ()=>{ drot=0; rot -= (90.0f.toRadians) })
  Keyboard.bind("a", ()=>{ quad.scale.x *= -1 })
  Keyboard.bind("d", ()=>{ quad.scale.y *= -1 })
  Keyboard.bind("r", ()=>{ quad.pose.quat.setIdentity(); drot=0; rot=0 })
  Keyboard.bind("1", ()=>{ moveUL = true })
  Keyboard.bind("2", ()=>{ moveUL = false })
  Keyboard.bind("i", ()=>{ move(0,dist) })
  Keyboard.bind("k", ()=>{ move(0,-dist) })
  Keyboard.bind("j", ()=>{ move(dist,0) })
  Keyboard.bind("l", ()=>{ move(-dist,0) })
  Keyboard.bind("y", ()=>{ node.posUL += Vec2(dist,dist); node.posBR += Vec2(-dist,-dist)})
  Keyboard.bind("h", ()=>{ node.posUL += Vec2(-dist,-dist); node.posBR += Vec2(dist,dist)})
  Keyboard.bind("u", ()=>{ node.posUL.set(0,0); node.posBR.set(1,1) })

  var moveUL = true
  var dist = 0.01
  def move(x:Float,y:Float){
    if(moveUL){
      node.posUL += Vec2(x,y)
    }else {
      node.posBR += Vec2(x,y)
    }
  }
}


class MaskBlendNode extends RenderNode {

  var posUL = Vec2(0,0)
  var posBR = Vec2(1,1)


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
    uniform vec2 ul;
    uniform vec2 br;

    void main(){

      // vec2 uv = (2.0*v_uv) - 1.0;
      vec2 diff = br - ul;
      vec2 uv = ul + v_uv*diff;
      // vec4 mask = texture2D(u_texture1, v_uv);
      gl_FragColor = texture2D(u_texture0, uv); 
      // gl_FragColor.a *= (1.0-length(v_pos)); // * 0.5;
      // gl_FragColor = vec4(1,1,1,1); 
    }
    """
  )
  
  override def render(){
    renderer.shader.uniforms("ul") = posUL
    renderer.shader.uniforms("br") = posBR
    super.render()
  }
}

AVFeedback
