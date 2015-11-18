
import com.fishuyo.seer._

import dynamic.SeerScript
import graphics._
import spatial._
import io._
import util._
import particle._

import collection.mutable.ArrayBuffer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

import rx._

Scene.alpha = 0.15f
SceneGraph.root.depth = false


object Script extends SeerScript {

  implicit def f2i(f:Float) = f.toInt

  var t = 0.0f 

  val mesh = Plane.generateMesh(10,10,50,50,Quat.forward)
  mesh.primitive = Lines

  val fabric = new SpringMesh(mesh,1.0f,4.0f)
  val constraints = new Array[LinearSpringConstraint](4)
  for( i <- 0 until 4) constraints(i) = LinearSpringConstraint(Particle(Vec3()),Particle(Vec3()),1.0f,1.0f)
  // fabric.springs ++= constraints

  val model = Model(mesh) 
  model.material = Material.basic

  var msg = "It's "

  var vel = Vec2()
  var lpos = Vec2()

  var inited = false
  override def init(){
    inited = true
    Text.loadFont()
  }

  // draw model
  override def draw(){

    FPS.print

    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
    Gdx.gl.glLineWidth(1)

    model.mesh.primitive = Lines
    model.draw

    Text.render(msg,0.0f,0.0f)

  }

  // animate the mesh
  override def animate(dt:Float){
    if(!inited) init()
    t += dt

    constraints.foreach(_.solve())
    fabric.animate(dt)

    if( Mouse.status() == "drag"){
      vel = (Mouse.xy() - lpos)/dt
      val r = Camera.ray(Mouse.x()*Window.width, (1.0f-Mouse.y()) * Window.height)
      
      val v = Camera.nav.ur() * vel.x + Camera.nav.uu() * vel.y

      fabric.particles.foreach( (p) => {
        val t = r.intersectSphere(p.position, 0.25f)
        if(t.isDefined){
          p.applyForce(v*150.0f)
        }
      })

      // fabric.springs.foreach( (s) => {
      //   val t = r.intersectSphere(s.p.position, 0.25f)
      //   if(t.isDefined){
      //     s.torn = true
      //   }
      // })
    }
    lpos = Mouse.xy()
  }

  val down = Array(false,false,false,false)
  val dist = Array(0.0f,0.0f,0.0f,0.0f)
  Trackpad.clear
  Trackpad.connect
  // Trackpad.bind((touch) => {
  //   for( i <- 4 until touch.count by -1) down(i-1) = false

  //   touch.fingers.take(4).zipWithIndex.foreach {
  //     case (f,i) =>
  //       if(down(i)){
  //         val r = Camera.ray(f.pos.x*Window.width, (1.0f-f.pos.y) * Window.height)
  //         constraints(i).q.position = r(dist(i))
  //       }else{
  //         val r = Camera.ray(f.pos.x*Window.width, (1.0f-f.pos.y) * Window.height)

  //         fabric.particles.foreach( (p) => {
  //           val t = r.intersectSphere(p.position, 0.1f)
  //           if(t.isDefined){
  //             down(i) = true
  //             dist(i) = t.get
  //             constraints(i) = LinearSpringConstraint(p,Particle(Vec3(p.position)),0.01f,1.0f)
  //           }
  //         })
  //       }
  //   }
  // })

  Mouse.clear
  Mouse.use

  var gravity = 0
  Keyboard.clear
  Keyboard.use
  Keyboard.bind("g", ()=>{ gravity = (gravity+1)%2; Gravity.set(Vec3(0,-10,0)*gravity) })
  val keyObs = Obs(Keyboard.key){ msg += Keyboard.key() }


}

Script