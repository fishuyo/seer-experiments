
// Particle sim shader.. weird

object ParticleScript extends SeerScript {

  val pos = Vec2()

  var node:ParticleNode = _
  var visualize:VisualizeNode = _

  var inited = false
  override def draw(){
    // for(i <- 0 until 10) node.render
  }

  override def animate(dt:Float){
    if(!inited){
      node = new ParticleNode
      visualize = new VisualizeNode
      RenderGraph.reset
      RenderGraph.addNode(node)
      ScreenNode.inputs.clear
      // node.outputTo(ScreenNode)
      node.outputTo(visualize)

      inited = true
    }

    node.renderer.shader.uniforms("brush") = Mouse.xy()
    node.renderer.shader.uniforms("dt") = dt
    node.renderer.shader.uniforms("u_texture1") = 0
  }

  Trackpad.clear
  Trackpad.connect
  Trackpad.bind { case touch =>
    touch.count match {
      case 1 => 
      case 2 =>
        pos.x += touch.vel.x * 0.01
        pos.y -= touch.vel.y * 0.01
      case _ => ()
    }
  }

}

class ParticleNode extends BackBufferNode {

  renderer.shader = Shader.load(
    """
    attribute vec4 a_position;
    attribute vec4 a_normal;
    attribute vec2 a_texCoord0;
    attribute vec4 a_color;

    uniform mat4 u_projectionViewMatrix;

    varying vec2 v_uv;
    varying vec3 v_normal;

    void main() {
      gl_Position = u_projectionViewMatrix * a_position;
      // v_normal = a_normal.xyz;
      v_uv = a_texCoord0;
    }
    """
    ,
    """
    #ifdef GL_ES
        precision mediump float;
    #endif

    varying vec2 v_uv;

    uniform sampler2D u_texture0;
    uniform sampler2D u_texture1;

    uniform vec2 brush;
    uniform float dt; 

    float snoise(in vec2 co){
      return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
    }

    void main(){
      vec3 pos = texture2D(u_texture0, v_uv).xyz;
      vec3 vel = texture2D(u_texture1, v_uv).xyz;
      pos += (vel) * dt; 
      // pos.x += (2.0*snoise(vec2(pos.x,pos.y))-1.0) * dt; 
      // pos.y += (2.0*snoise(vec2(pos.x,pos.y))-1.0) * dt; 
      // pos.z += snoise(vec2(pos.x,pos.y)) * dt; 
      // pos += vec3(0.1,0,0)*dt;

      if(brush.x > 0.0){
          vec2 brsh = brush;
          //brsh.y = 1.0 - brsh.y;
          vec2 diff = (v_uv - brsh);
          float dist = dot(diff, diff);
          if(dist < 0.001)
              pos = vec3(v_uv.x,v_uv.y,0);
      }

      // Write new position out
      gl_FragColor = vec4(pos, 1.0);
     
    }
  """)
}

class VisualizeNode extends RenderNode {

  val mesh = Plane.generateMesh(2,2,300,300)
  mesh.primitive = Points
  val model = Model(mesh)
  renderer.scene.push(model)

  renderer.shader = Shader.load(
    """
    attribute vec4 a_position;
    attribute vec4 a_normal;
    attribute vec2 a_texCoord0;
    attribute vec4 a_color;

    uniform mat4 u_projectionViewMatrix;

    uniform sampler2D u_texture0;

    void main() {
      vec3 position = ( texture2D( u_texture0, a_texCoord0 ).rgb  );
      gl_Position = u_projectionViewMatrix * vec4(position, 1.0);
    }
    """
    ,
    """
    #ifdef GL_ES
        precision mediump float;
    #endif

    void main(){
      // gl_FragColor = vec4(0.2,0.1,0.8,1.0);
      gl_FragColor = vec4(1.0,1.0,1.0,1.0);
    }
  """)
}


ParticleScript