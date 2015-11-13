

package com.fishuyo.seer
package examples.actor

import graphics._
import actor._
import dynamic._
import io._

// import com.badlogic.gdx.Gdx
// import com.badlogic.gdx.graphics.GL20
  
object GraphicsActorTest extends SeerApp {

  var time = 0.0f
  // DesktopApp.loadLibs()
  // Scene.push(this)
  // DesktopApp.run()
  val scripts = ScriptManager.load("default/")
  // val scripts = ScriptManager.load("dir/rotato.scala")

  override def animate(dt:Float){
    time += dt
    if(time > 5){

    }
  }
  override def draw(){



    // GraphicsActor.actor ! (() => { 
    //   // println("hi")
    //   Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT)
    //   Renderer().shader.begin() 

    //   // MatrixStack.clear()
    //   // Renderer().setMatrixUniforms()
    //   // Renderer().setEnvironmentUniforms()
    //   // Renderer().environment.setGLState()
    //   // Renderer().setMaterialUniforms(material)
      
    //   // Renderer().shader.setUniforms() // set buffered uniforms in shader program

    //   model.draw()
    //   model.rotate(0,0.01f,0)
      
    //   Renderer().shader.end()
     
    // })
  }
  Keyboard.bind("p", ()=>{System().actorSelection("/user/agent.*") ! "hi"})

}