#ifdef GL_ES
  precision mediump float;
#endif

varying vec2 v_uv;
varying vec3 v_pos;

uniform sampler2D u_texture0;
uniform sampler2D u_texture1;

uniform float u_div;

uniform vec2 u_hole0;
uniform vec2 u_hole1;

void main(){

  vec2 uv = (2.0*v_uv) - 1.0;

  vec4 tex0 = texture2D(u_texture0, v_uv); 
  vec4 tex1 = texture2D(u_texture1, v_uv);

  if( v_uv.x < u_div - 0.1){
    gl_FragColor = tex0;
  } else if( v_uv.x < u_div + 0.1){
    gl_FragColor = mix(tex1,tex0, (u_div + 0.1 - v_uv.x)/0.2 );
  } else {
    gl_FragColor = tex1;
  }
  
  
  // if( length(v_uv.x - u_hole0.x) < 0.1){
    // if( length(v_uv.y - (u_hole0.y)) < 0.2){
      vec2 diff = v_uv - u_hole0;
      gl_FragColor.a *= diff.x*diff.x/(0.2*0.2) + diff.y*diff.y/(0.5*0.5);
      
      vec2 diff2 = v_uv - u_hole1;
      gl_FragColor.a *= diff2.x*diff2.x/(0.2*0.2) + diff2.y*diff2.y/(0.5*0.5);
    // }
  // }
  // gl_FragColor.a *= 1.5*length(v_uv - u_hole1);
  // gl_FragColor = vec4(1,0,1,1); 
}