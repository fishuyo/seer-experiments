

import com.fishuyo.seer._

import graphics._
import spatial._
import dynamic._
import io._
import util._

import com.fishuyo.seer.openni._

import com.badlogic.gdx.graphics.{Texture => GdxTexture}
import com.badlogic.gdx.graphics.Pixmap

// import org.openni._
// import java.nio.ShortBuffer
// import java.nio.ByteBuffer


// Scene.alpha = 1.
// SceneGraph.root.depth = true

// import concurrent.Await
// import concurrent.duration._


object Script extends SeerScript {

  var inited = false

  OpenNI.initAll()
  OpenNI.start()


  val stickman = new StickMan(OpenNI.getSkeleton(1))

	val quad1 = Plane().scale(1,-480f/640f,1).translate(-1.5,0,0)
	val quad2 = Plane().scale(1,-480f/640f,1).translate(1,0,0)
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
		quad1.material.textureMix = 1f
		quad2.material = Material.basic
		quad2.material.texture = Some(tex2)
		quad2.material.textureMix = 1f
  	inited = true
  }

	override def draw(){
		FPS.print

		// OpenNI.updateDepth()

    stickman.draw

		quad1.draw
		quad2.draw

	}

	override def animate(dt:Float){
		if(!inited) init()

    stickman.animate(dt)
    
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