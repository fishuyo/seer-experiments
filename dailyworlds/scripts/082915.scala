
// song and dance

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._
import com.fishuyo.seer.video._
import scala.math._

import java.nio.ByteBuffer


object SongAndDance extends SeerScript {

  var initd = false

  var fbnode:FeedbackNode = _

  OpenNI.initAll()
  OpenNI.start()
  OpenNI.pointCloud = true
  OpenNI.pointCloudDensity = 4
  // KPC.loadCalibration("calibration.txt")
  val skeletons = (1 to 4).map(OpenNI.getSkeleton(_))

  // RootNode.renderer.camera = new OrthographicCamera(2,2)
  // RootNode.renderer.camera.nav.pos.z = 2
  // Renderer().camera = Camera

  val mesh = new Mesh()
  mesh.primitive = Points 
  mesh.maxVertices = 640*480
  mesh.maxIndices = 10002
  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGB(1)



  override def init(){
    // fbnode = new FeedbackNode(0.95,0.05)
    // RenderGraph.reset
    // RenderGraph.addNode(node)
    // node.outputTo(fbnode)
    // fbnode.outputTo(new ScreenNode())

    // RootNode.outputTo(blur)

    initd = true
  }

  override def onUnload(){
  }

  override def draw(){

    FPS.print

    Renderer().environment.depth = false
    Renderer().environment.blend = true
    Renderer().environment.lineWidth = 2f
    
    model.draw

    val c = Sphere()
    c.material = Material.specular
    c.scale(0.1)
    c.pose.pos.set( Renderer().environment.lightPosition)
    c.draw

    // node.renderer.environment.depth = false
    // node.renderer.environment.blend = true
    // node.renderer.environment.blendFunc(SrcAlpha,DstAlpha)
    // Renderer().environment.alpha = 0.1f
    // Renderer().environment.lineWidth = 1f

  }

  var time = 0f
  override def animate(dt:Float){

    if(!initd) init()

    skeletons.foreach { case s =>
      if(s.tracking) s.updateJoints()
    }

    time += dt
    var a = 0.6
    var b = 0.01
    var c = 0.1
    var pa = 1f
    var pb = 0f
    var pc = 0f
    // Renderer().environment.lightPosition.set( sin(a*time+pa), sin(b*time+pb), sin(c*time+pc))
    // Renderer().environment.lightPosition += skeletons(0).joints("torso")

    try{
      mesh.clear
      mesh.vertices ++= OpenNI.pointMesh.vertices
      // val index = Random.int(mesh.vertices.length)
      // mesh.indices ++= (0 until mesh.maxIndices).map( _ => index() )
      // mesh.recalculateNormals
      mesh.update
    
    } catch { case e:Exception => println(e) }
    // for( i <- 0 until videos.length){
      // if(skeletons(i).tracking){
        // skeletons(i).updateJoints()
        // val pos = skeletons(i).joints("torso")
        // val p = pos * 1000
        // p.z *= -1
        // val out = KPC.worldToScreen(p)
        // videos(i).quad.pose.pos.lerpTo(out,0.1f)
        // videos(i).quad.pose.pos.lerpTo(pos,0.1f)
        // println(pos)
        // println(out)
      // }
    // }
    // fbnode.blend0 = 2*Mouse.x()-1
    // fbnode.blend1 = 2*Mouse.y()-1

    // blur.size = 0.01 * abs( 2*sin(Time()))
    // blur.intensity = 0.2 * abs( 2*sin(0.33*Time())) + 0.1

  }

  Keyboard.clear
  Keyboard.bind("a", () => { println("a")})
  Keyboard.bind("b", () => { println("b")})
  Keyboard.bind("-", () => { println("-")})
  Keyboard.bind("=", () => { println("+")})
  Keyboard.bind("0", () => { println("home")})
  Keyboard.bind("1", () => { println("1")})
  Keyboard.bind("2", () => { println("2")})

  import de.sciss.osc.Message
  OSC.clear()
  OSC.disconnect()
  OSC.listen(15000)
  OSC.bindp {
    case Message("/pitch", f:Float) => 
    case Message("/roll", f:Float) =>
    // case Message("/yaw", f:Float) => println(s"yaw $f"); videos(0).setRate(f)
    case msg => println(msg)
  }

  Trackpad.bind { case t => 

    val v = Vec3(t.vel)

    t.count match {
      // case 2 => videos(0).quad.pose.pos += v * 0.01
      // case 3 => videos(1).quad.pose.pos += v * 0.01
      // case 4 => videos(2).quad.pose.pos += v * 0.01
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
    }
    """
  )
  
  override def render(){
    // renderer.shader.uniforms("blurSize") = size
    // renderer.shader.uniforms("intensity") = intensity
    super.render()
  }
}

SongAndDance
