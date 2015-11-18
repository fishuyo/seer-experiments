
import com.fishuyo.seer._

import allosphere._
import allosphere.actor._

import graphics._
import dynamic._
import spatial._
import io._
import particle._
import util._

import collection.mutable.ArrayBuffer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

import akka.actor._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator

import ClusterSystem.{ system, system10g }
// import ClusterSystem.{ test2_10g => system10g }

import allosphere.livecluster.Node

Scene.alpha = .3
SceneGraph.root.depth = false

Camera.nav.pos.set(0,0,0)


object Script extends SeerScript {
  

  val players = ArrayBuffer[Model]()
  val moons = ArrayBuffer[Model]()

  var thrust = Vec2()

  val thrustMesh = new Mesh()
  thrustMesh.primitive = Lines
  thrustMesh.vertices += Vec3()
  thrustMesh.vertices += Vec3()
  val thrustModel = Model(thrustMesh)
  
  players += Sphere().scale(0.1)

  for (i <- 0 until 500){
  
    val s = i*0.05 + 1.0f
    val m = Sphere().scale(0.01*s)
    m.material = Material.specular
    moons += m
  }

  override def preUnload(){
  }

  override def draw(){
    moons.foreach( _.draw )
    // players.foreach( _.draw )
    // thrustModel.draw
  }
  override def animate(dt:Float){
  }
}

Script

object RendererScript extends SeerScript {
	

  Node.mode = "omni"

  var t = 0.0f
	val stateListener = system10g.actorOf(Props( new StateListener()), name = "statelistener")

	override def preUnload(){
		stateListener ! PoisonPill
	}

	var inited = false
	override def init(){
    Node.omniShader = Shader.load("omni", OmniShader.glsl + S.basic._1, S.basic._2 )

    Node.omni.mStereo = 1
    Node.omni.mMode = StereoMode.ACTIVE
    Node.lens.eyeSep = 0.05
    // Node.omni.renderFace(0) = true
    // Node.omni.renderFace(1) = true
    // Node.omni.renderFace(2) = true
    // Node.omni.renderFace(3) = true
    // Node.omni.renderFace(4) = true
    // Node.omni.renderFace(5) = true

		inited = true
	}

  override def draw(){

    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
    Gdx.gl.glDisable( GL20.GL_DEPTH_TEST )

    Script.draw()    
  }

  override def animate(dt:Float){
  	if(!inited) init()

    Node.lens.eyeSep = 0.1 //math.sin(t)

    Script.animate(dt)
  }
}


class StateListener extends Actor with ActorLogging {
  import DistributedPubSubMediator.{ Subscribe, SubscribeAck }

  val mediator = DistributedPubSubExtension(system10g).mediator
  mediator ! Subscribe("state", self)
 
  def receive = {
    case SubscribeAck(Subscribe("state", None, `self`)) ⇒
      context become ready
  }
 
  def ready: Actor.Receive = {
    case f:Float =>
      RendererScript.t = f
    case a:Array[Float] if a.length == 7 =>
      Camera.nav.pos.set(a(0),a(1),a(2))
      Camera.nav.quat.set(a(3),a(4),a(5),a(6))
    case a:Array[Float] =>
      val v = a.grouped(3).map((g)=>Vec3(g(0),g(1),g(2)))
      v.zipWithIndex.foreach { case(v,i) => Script.moons(i).pose.pos.set(v) }
  }
}


object S {
 val basic = (
    // Vertex Shader
    """
      attribute vec3 a_position;
      attribute vec3 a_normal;
      attribute vec4 a_color;
      attribute vec2 a_texCoord0;

      uniform int u_hasColor;
      uniform vec4 u_color;
      uniform mat4 u_projectionViewMatrix;
      uniform mat4 u_modelViewMatrix;
      uniform mat4 u_viewMatrix;
      uniform mat4 u_modelMatrix;
      uniform mat4 u_normalMatrix;
      uniform vec4 u_cameraPosition;

      uniform vec3 u_lightPosition;

      varying vec4 v_color;
      varying vec3 v_normal, v_pos, v_lightDir, v_eyeVec;
      varying vec2 v_texCoord;
      varying float v_fog;

      void main(){
        // if( u_hasColor == 0){
        if( a_color.xyz == vec3(0,0,0)){
          v_color = u_color;
        } else {
          v_color = a_color;
        }
        v_color = u_color;

        vec4 pos = u_modelViewMatrix * vec4(a_position,1);
        v_pos = vec3(pos) / pos.w;

        v_normal = vec3(u_normalMatrix * vec4(a_normal,0));
        
        v_eyeVec = normalize(-pos.xyz);

        v_lightDir = vec3(u_viewMatrix * vec4(u_lightPosition,0));

        v_texCoord = a_texCoord0;
        gl_Position = omni_render(u_modelViewMatrix * vec4(a_position,1));
        // gl_Position = u_projectionViewMatrix * vec4(a_position,1); 
      }
    """,
    // Fragment Shader
    """
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

      varying vec2 v_texCoord;
      varying vec3 v_normal;
      varying vec3 v_eyeVec;
      varying vec3 v_lightDir;
      varying vec4 v_color;
      varying vec3 v_pos;

      void main() {
        
        vec4 colorMixed;
        if( u_textureMix > 0.0){
          vec4 textureColor = texture2D(u_texture0, v_texCoord);
          colorMixed = mix(v_color, textureColor, u_textureMix);
        }else{
          colorMixed = v_color;
        }
        // colorMixed = vec4(1,0,1,1);

        vec4 final_color = colorMixed * u_lightAmbient;

        vec3 N = normalize(v_normal);
        vec3 L = normalize(v_lightDir);

        float lambertTerm = dot(N,L);
        final_color += u_lightDiffuse * colorMixed * max(lambertTerm,0.0);

        float specularTerm = 0.0;

        //phong
        vec3 R = reflect(-L, N);
        vec3 E = normalize(v_eyeVec); //normalize(-v_pos);
        //float specAngle = max(dot(R,E), 0.0);
        //specularTerm = pow(specAngle, 8.0);

        //blinn
        float halfDotView = max(0.0, dot(N, normalize(L + E)));
        specularTerm = pow(halfDotView, 20.0);
        specularTerm = specularTerm * smoothstep(0.0,0.2,lambertTerm);

        final_color += u_lightSpecular * specularTerm;
        gl_FragColor = mix(colorMixed, final_color, u_lightingMix);
        gl_FragColor *= (1.0 - u_fade);
        gl_FragColor.a *= u_alpha;

        // gl_FragColor = vec4(1,0,0,1); //(1.0 - u_fade);

      }
    """
  )
 }



RendererScript

