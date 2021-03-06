package com.fishuyo.seer
package test

import graphics._
import io._
import maths._
import particle._
import dynamic._
import audio._
import util._

import scala.collection.mutable.ListBuffer

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.glutils._

object Main extends App with Animatable{

  DesktopApp.loadLibs()

  Scene.push(this)


  val cube = Cube() //new SpringMesh(Cube.generateMesh(), 1.0f)
  val cubes = ListBuffer[Model]()
  val n = 10
  val size = .1f
  for( i<-(-n until n); j<-(-n until n)){
    val x = i * size
    val z = j * size
    val d = (x*x+z*z)

    val c = Cube().scale(size/2.0f).translate(x,0,z)
    val v = (j+5*math.sin(x)).toInt
    if( v/3 % 2 == 0) c.color.set(0,0,0,1)
    if( i*i + j*j < 200 || math.abs(v) < 2) c.color.set(1,.7f,0,1)
    c.scale.y = Random.float(0,0.5)()
    cubes += c
  }

  val node = new RenderNode
  val s = new SpringMesh( Plane.generateMesh(2,2,10,10), 1.0f) //Sphere()
  s.particles.takeRight(10).foreach( (p) => s.pins += AbsoluteConstraint(p, p.position))
  val model = Model(s)
  // s.color.set(1,0,0,1)
  // node.scene.push(s)
  // SceneGraph.addNode(node)
  // Scene.push(model)

  val mesh = Cylinder.mesh.getOrElse(new Mesh()) //Sphere.generateMesh()

  // var modelBuilder = Some(parsers.EisenScriptParser(""))
  // var model = Some(Model())

  // Scene.push(cube)

  val live = new Ruby("test.rb")

  val words = List("soup","salad","is","super","\n","\n","\n","azure","why","eyes","dark","\n", "\n","\n","\n","sexy","time","space","can be", "will", "to be about to", "has", "futile", "dog", "witch")
  var text = ".."
  var x = 10.0f
  var y = 100.0f
  var t = 0.0f 
  var next = 1.0f
  def movet(a:Float,b:Float){ x=a; y=b}

  DesktopApp()  

  override def init(){
    Text.loadFont()

  }
  override def draw(){

    // Text.render(text,x,y)

    live.draw()
    cubes.foreach( _.draw() )

  }

  override def animate(dt:Float){

    if( t > next){
      text += " " + Random.oneOf(words: _*)()
      t=0.0f 
      next = Random.float()*5.0f
    }
    t += dt

    cubes.foreach( (c) => {
       c.scale(1,1+Random.float(-1,1)()*0.01,1)
    })
    // s.animate(dt)
    live.animate(dt)
  }

}



