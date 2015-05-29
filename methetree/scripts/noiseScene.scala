
import com.fishuyo.seer.openni._

object NoiseScene extends SeerScript {

  var noiseNode:RenderNode = _

  var inited = false

  OpenNI.initAll()
  OpenNI.start()
  OpenNI.pointCloud = true
  OpenNI.pointCloudDensity = 2

  node.renderer.camera = new OrthographicCamera(1,1)
  node.renderer.camera.nav.pos.z = 2
  node.renderer.clear = true

  KPC.loadCalibration("../methetree/calibration.txt")

  val mesh = new Mesh()
  mesh.primitive = Lines
  mesh.maxVertices = 640*480
  mesh.maxIndices = 10000
  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGBA(0.1,0.1,0.1,0.1)
  model.material.transparent = true
  var numIndices = 10000

  val skeleton = OpenNI.getSkeleton(1)

  override def draw(){ model.draw }

  override def animate(dt:Float){
    if(!inited){
      // RenderGraph.reset
      noiseNode = new NoiseNode
      node.outputTo(noiseNode)
      inited = true
    }
    noiseNode.renderer.shader.uniforms("time") = Time()

    try{
      mesh.clear
      mesh.vertices ++= OpenNI.pointMesh.vertices.map( (v) => { val p = v*1000; p.z *= -1; KPC.worldToScreen(p)})
      val index = Random.int(mesh.vertices.length)
      mesh.indices ++= (0 until numIndices).map( _ => index() )
      mesh.update

    } catch { case e:Exception => () } //println(e) }
  }


}

class NoiseNode extends RenderNode {

  renderer.scene.push(Plane())

  renderer.shader = Shader.load(
    """
    attribute vec4 a_position;
    attribute vec4 a_normal;
    attribute vec2 a_texCoord0;
    attribute vec4 a_color;

    uniform mat4 u_projectionViewMatrix;
    varying vec2 v_uv;

    void main() {
      gl_Position = u_projectionViewMatrix * a_position;
      v_uv = a_texCoord0;
    }
    """,

    """
    #ifdef GL_ES
        precision mediump float;
    #endif

    varying vec2 v_uv;
    uniform float time;

    uniform sampler2D u_texture0;

    float snoise(in vec2 co){
      return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
    }
    void main(){
      vec4 color = texture2D(u_texture0, v_uv);
      float n = snoise(vec2(v_uv.x*cos(time),v_uv.y*sin(time))); 
      gl_FragColor = vec4(n, n, n, 1.0 ) + color;
    }
    """
  )
}

NoiseScene