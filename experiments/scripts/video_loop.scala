

import com.fishuyo.seer._

import graphics._
import spatial._
import dynamic._
import io._
import util._

import openni._
import cv._

import org.opencv.core._
import org.opencv.highgui._
import org.opencv.imgproc._

import com.badlogic.gdx.graphics.{Texture => GdxTexture}
import com.badlogic.gdx.graphics.Pixmap


object Script extends SeerScript {

  var inited = false

  OpenNI.connect()
	val context = OpenNI.context

	OpenCV.loadLibrary()

	val loop = new VideoLoop

	val quad1 = Plane().scale(1,-480.0f/640.0f,1).translate(-1.5,0,0)
	val quad2 = Plane().scale(1,-480.0f/640.0f,1).translate(1,0,0)
  val dpix = new Pixmap(640,480, Pixmap.Format.RGBA8888)
  val vpix = new Pixmap(640,480, Pixmap.Format.RGBA8888)
  var tex1:GdxTexture = _
  var tex2:GdxTexture = _

  var maskMat = new Mat(480,640,CvType.CV_8UC1)
  var videoMat = new Mat(480,640,CvType.CV_8UC4)

  val bytes = new Array[Byte](640*480*4)


  override def init(){
  	// loadShaders()

		tex1 = new GdxTexture(dpix)
		tex2 = new GdxTexture(vpix)
		quad1.material = Material.basic
		quad1.material.texture = Some(tex1)
		quad1.material.textureMix = 1.0f
		quad2.material = Material.basic
		quad2.material.texture = Some(tex2)
		quad2.material.textureMix = 1.0f
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

  	val bb0 = vpix.getPixels
		bb0.put(OpenNI.imgbytes)
		bb0.rewind
		tex2.draw(vpix,0,0)

		videoMat.put(0,0,OpenNI.rgbbytes)
		maskMat.put(0,0,OpenNI.maskbytes)

		val img = new Mat()
		videoMat.copyTo(img, maskMat)

  //   val rsmall = new Mat()
  // 	val small = new Mat()
  // 	Imgproc.resize(img,small, new Size(), 0.5,0.5,0)
  //   Core.flip(small,rsmall,1)
  //   Imgproc.cvtColor(rsmall,small, Imgproc.COLOR_BGR2RGB)

  	val out = new Mat()
  	loop.videoIO( img, out)

    if( out.empty()) return

    // if( subtract ){
      val bgmask = new Mat()
      Core.compare(out, new Scalar(0), bgmask, Core.CMP_EQ)
      videoMat.copyTo(out,bgmask)
    // }

 		// copy MAT to pixmap
    out.get(0,0,bytes)  
    val bb = dpix.getPixels()
  	bb.put(bytes)
  	bb.rewind()
		tex1.draw(dpix,0,0)

		// val bb2 = vpix.getPixels
		// bb2.put(OpenNI.rgbbytes)
		// bb2.rewind
		// tex2.draw(vpix,0,0)

		img.release
		out.release
		bgmask.release
	}

	def loadShaders(){
    // Shader.load("rd", File("shaders/basic.vert"), File("shaders/rd_img.frag")).monitor
  	// Shader.load("colorize", File("shaders/basic.vert"), File("shaders/colorize.frag")).monitor
  }

  override def onUnload(){
  	// OpenNI.disconnect
  }

  Keyboard.clear
  Keyboard.use
  Keyboard.bind("r", () => loop.toggleRecord() )
  Keyboard.bind("t", () => loop.togglePlay() )
  Keyboard.bind("x", () => loop.stack() )
  Keyboard.bind("c", () => loop.clear() )
  Keyboard.bind("	", () => loop.reverse() )
  Keyboard.bind("j", () => loop.setAlphaBeta(1.0f,.99f) )
  
  Keyboard.bind("p", () => video.ScreenCapture.toggleRecord )
  Keyboard.bind("o", () => loop.writeToFile() )



  Mouse.clear()
	Mouse.use()
	Mouse.bind("drag", (i) => {
		val speed = (400 - i(1)) / 100.0f
	  val decay = (i(0) - 400) / 100.0f
	  // # decay = (decay + 4)/8
	  // # Loop.loop.setSpeed(speed)
		// loop.setAlphaBeta(decay, speed)
	  loop.setAlpha(decay)
	})

}


Script