
package com.fishuyo.seer
package graphics


class NoiseNode extends RenderNode {
  var time = 0f
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

  override def render(){
    renderer.shader.uniforms("time") = time
    super.render()
  }

}