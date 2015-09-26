
// reaction diffusion ressurection

import com.fishuyo.seer.openni._


object Script extends SeerScript {

  val pos = Vec2()

  var node:RDNode = _
  var colorize:ColorizeNode = _

  var inited = false
  override def draw(){
    for(i <- 0 until 10) node.render
  }

  override def animate(dt:Float){
    if(!inited){
      node = new RDNode
      colorize = new ColorizeNode
      RenderGraph.reset
      RenderGraph.addNode(node)
      ScreenNode.inputs.clear
      node.outputTo(colorize)

      colorize.color1 = RGBA(0,0,0,0)
      colorize.color2 = RGBA(1,1,1,.3f)
      colorize.color3 = RGBA(0,1,1,.4f)
      colorize.color4 = RGBA(0,0,1,.5f)
      colorize.color5 = RGBA(0,0,0,.6f)

      inited = true
    }
    node.renderer.shader.uniforms("brush") = Mouse.xy()
    node.renderer.shader.uniforms("width") = Window.width.toFloat
    node.renderer.shader.uniforms("height") = Window.height.toFloat
    node.renderer.shader.uniforms("feed") = 0.037 //62
    node.renderer.shader.uniforms("kill") = 0.06 //093
    node.renderer.shader.uniforms("dt") = dt
    node.renderer.shader.uniforms("u_texture1") = 2


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

class RDNode extends BackBufferNode {

  renderer.resize = false
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
      v_normal = a_normal.xyz;
      v_uv = a_texCoord0;
      // v_color = a_color;
      // v_pos = a_position.xyz;
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

    uniform float width;
    uniform float height;
    // uniform float dt;
    // uniform float feed;
    // uniform float kill;
    uniform vec2 brush;

    float dt = 0.8; //0.8; //515;

    float dx = 1.0/width;
    float dy = 1.0/height;
    // float dy = 1.0/height;

    float da = 0.0002;
    float db = 0.00001;


    // nine point stencil
    vec4 laplacian9() {
        return  
        0.5* texture2D( u_texture0,  v_uv + vec2(-dx,-dy)) // first row
        + texture2D( u_texture0,  v_uv + vec2(0,-dy) )
        +  0.5* texture2D( u_texture0,  v_uv + vec2(dx,-dy))
        +  texture2D( u_texture0,  v_uv + vec2(-dx,0)) // seond row
        - 6.0* texture2D( u_texture0,  v_uv )
        +   texture2D( u_texture0,  v_uv + vec2(dx,0))
        +  0.5*texture2D( u_texture0,  v_uv + vec2(-dx,dy))  // third row
        + texture2D( u_texture0,  v_uv + vec2(0,dy) )
        +   0.5*texture2D( u_texture0,  v_uv + vec2(dx,dy));    
    }

    // five point stencil
    vec4  laplacian5() {
        return 
        +  texture2D( u_texture0, v_uv + vec2(0,-dy))
        +  texture2D( u_texture0, v_uv + vec2(-dx,0)) 
        -  4.0 * texture2D( u_texture0,  v_uv )
        +  texture2D( u_texture0, v_uv + vec2(dx,0)) 
        +  texture2D( u_texture0, v_uv + vec2(0,dy));
    }

    void main(){

        float F = 0.034; //mitosis
        float K = 0.063;

        // float F = 0.025; //pulse
        // float K = 0.06;

        // float F = 0.014; //waves
        // float K = 0.045;

        // float F = 0.026; //brains
        // float K = 0.055;

        // float F = 0.082; //worms
        // float K = 0.061;

        // float F = 0.082; //worm channels
        // float K = 0.059;

        // float F = 0.078; // + 0.085*(v_uv.y*0.015); 
        // float K = 0.061;

        //Uskate
        // float F = 0.062;
        // float K = 0.06093;
        // float F = 0.062;
        // float K = 0.0609;
        // float F = 0.062;
        // float K = 0.06093;

        // float F = v_uv.y * 0.083;
        // float K = v_uv.x * 0.073;

        vec2 alpha = vec2(da/(dx*dx), db/(dy*dy));
        alpha = vec2(0.2097, 0.105);
        // alpha = vec2(0.64, 0.32);
        
        vec2 oldV = texture2D(u_texture0, v_uv).rg;
        vec2 L = laplacian9().rg;
        vec2 V = oldV + L*alpha*dt; // diffused value

        // float du = /*0.00002*/0.2097*L.r - oldV.r*oldV.g*oldV.g + F*(1.0 - oldV.r);
        // float dv = /*0.00001*/0.105*L.g + oldV.r*oldV.g*oldV.g - (F+K)*oldV.g;
        
        // grey scott
        float ABB = V.r*V.g*V.g;
        float rA = -ABB + F*(1.0 - V.r);
        float rB = ABB - (F+K)*V.g;

        vec2 R = dt*vec2(rA,rB);

        // output diffusion + reaction
        vec2 dst = V + R;


        // dst = oldV + dt*vec2(du, dv);


        // vec2 dV = vec2( alpha.x * lapl.x - xyy + feed*(1.-uv.x), alpha.y*lapl.y + xyy - (feed+kill)*uv.y);
        // dst = uv + dt*dV;

        vec4 inV = texture2D(u_texture1, v_uv);
        if(brush.x > 0.0)
        {
            vec2 brsh = brush;
            //brsh.y = 1.0 - brsh.y;
            vec2 diff = (v_uv - brsh)/vec2(dx,dy);
            float dist = dot(diff, diff);
            if(dist < 100.0)
                dst.g = 0.9;
        }
        
        if( inV.g > 0.0 && inV.r >= 0.0 && inV.b == 0.0)
            gl_FragColor = vec4(dst.r,inV.g,0.0,1.0);
        else 
            gl_FragColor = vec4(dst.r,dst.g, 0.0, 1.0);
    }
  """)
}

Script