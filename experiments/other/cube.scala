

package com.fishuyo.seer
package template

import graphics._

object Main extends App with Animatable {

	DesktopApp.loadLibs()

	val cube = Cube()

	Scene.push(this)

	DesktopApp()

	override def draw(){
		cube.draw()
	}
	override def animate(dt:Float){
		cube.rotate(0,.01f,0)
	}

}