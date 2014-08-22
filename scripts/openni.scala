

import com.fishuyo.seer._

import graphics._
import spatial._
import dynamic._
import io._
import util._

import openni._

import com.badlogic.gdx.graphics.{Texture => GdxTexture}
import com.badlogic.gdx.graphics.Pixmap

// import org.openni._
// import java.nio.ShortBuffer
// import java.nio.ByteBuffer


// Scene.alpha = 1.
// SceneGraph.root.depth = true


object Script extends SeerScript {

  var inited = false

  OpenNI.connect()
	val context = OpenNI.context

	val quad1 = Plane().scale(1,-480.f/640.f,1).translate(-1.5,0,0)
	val quad2 = Plane().scale(1,-480.f/640.f,1).translate(1,0,0)
  val dpix = new Pixmap(640,480, Pixmap.Format.RGBA8888)
  val vpix = new Pixmap(640,480, Pixmap.Format.RGBA8888)
  var tex1:GdxTexture = _
  var tex2:GdxTexture = _


  override def init(){
  	// loadShaders()

		tex1 = new GdxTexture(dpix)
		tex2 = new GdxTexture(vpix)
		quad1.material = Material.basic
		quad1.material.texture = Some(tex1)
		quad1.material.textureMix = 1.f
		quad2.material = Material.basic
		quad2.material.texture = Some(tex2)
		quad2.material.textureMix = 1.f
  	inited = true
  }

	override def draw(){
		FPS.print

		OpenNI.updateDepth()

		quad1.draw
		quad2.draw

	}

	override def animate(dt:Float){
		if(!inited) init()

  	val bb = dpix.getPixels
		bb.put(OpenNI.imgbytes)
		bb.rewind
		tex1.draw(dpix,0,0)

		val bb2 = vpix.getPixels
		bb2.put(OpenNI.rgbbytes)
		bb2.rewind
		tex2.draw(vpix,0,0)
	}

	def loadShaders(){
    // Shader.load("rd", File("shaders/basic.vert"), File("shaders/rd_img.frag")).monitor
  	// Shader.load("colorize", File("shaders/basic.vert"), File("shaders/colorize.frag")).monitor
  }

  override def onUnload(){
  	// OpenNI.disconnect
  }

}


Script