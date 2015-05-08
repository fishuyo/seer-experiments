


object Script extends SeerScript {

  var time = 0f 
  var rot = Vec2()

  val model = Sphere()
  var count = 1

  Renderer().shader = Shader.load( DefaultShaders.basic._1, S.frag)

  override def draw() = {
    for( x <- 0 until count; y <- 0 until count){
      MatrixStack.push
      MatrixStack.translate(x,y,0)
      model.draw  
      MatrixStack.pop
    }
    
  }
  override def animate(dt:Float){
    time += dt 
    Renderer().shader.uniforms("time") = time
    if(doBeat){
      Renderer().shader.uniforms("color") = Vec3(1,1,1) //HSV2RGB(HSV(util.Random.float(),1,1))
      doBeat = false
    }
    
    count = (follow.value * 100).toInt + 1//util.Random.int(1,6)() + 1


    model.rotate(rot.x, rot.y, 0)
    model.scale.set( follow.value * 1f + 0.5f ) 
    // if( beat.value > 0f) println("!")
    // println(follow.value)
  }

  Trackpad.clear
  Trackpad.connect
  Trackpad.bind {
    case touch if touch.fingers.length == 2 =>
      rot = touch.vel * 0.1f
    case _ =>

  }

  val follow = new EnvFollow(5000)
  val beat = new BeatTrack
  var doBeat = false
  override def audioIO(io:audio.AudioIOBuffer){
    while( io()){
      follow(io.in(0))
      beat(io.in(0))
      if( beat.value > 0f) doBeat = true//println(util.Random.int())

    }
  }

}

class EnvFollow(val size:Int = 2048) extends audio.Gen {
  def apply() = this(0f)
  def apply(in:Float) = {
    value -= value/size
    value += math.abs(in)/size
    value
  }
}

class BeatTrack extends audio.Gen {
  var thresh = 0.1f
  var decay = 0.0000001f
  var hold = 2000
  var t = hold
  def apply() = this(0f)
  def apply(in:Float) = {
    value = 0f
    thresh -= decay
    t += 1
    if( t > hold && math.abs(in) > thresh){
      value = 1f
      thresh = math.abs(in)
      t = 0
    }
    value
  }
}



object S {
  def frag = """
    #ifdef GL_ES
     precision mediump float;
    #endif

    uniform sampler2D u_texture0;

    uniform float u_alpha;
    uniform float u_fade;
    uniform float u_textureMix;
    uniform float u_lightingMix;
    uniform vec4 u_lightAmbient;
    uniform vec4 u_lightDiffuse;
    uniform vec4 u_lightSpecular;
    uniform float u_shininess;

    uniform float time;
    uniform vec3 color;

    varying vec2 v_texCoord;
    varying vec3 v_normal;
    varying vec3 v_eyeVec;
    varying vec3 v_lightDir;
    varying vec4 v_color;
    varying vec3 v_pos;

    void main() {
      
      float c = mod(v_texCoord.y, 0.1) * (4.0*(sin(time*4.0)+1.0) + 2.0);
      gl_FragColor = vec4(color * c, 1.0);
    }
  """
}

Script