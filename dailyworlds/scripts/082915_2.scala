
// openni looper

import com.fishuyo.seer.openni._
import com.fishuyo.seer.cv._

import org.opencv.core._
import org.opencv.highgui._
import org.opencv.imgproc._

// import com.badlogic.gdx.graphics.{Texture => GdxTexture}
// import com.badlogic.gdx.graphics.Pixmap


object Script extends SeerScript {

  var inited = false

  OpenNI.initAll()
  OpenNI.alignDepthToRGB()
  OpenNI.start()
  OpenNI.makeDebugImage = true

  OpenCV.loadLibrary()

  val loop = new VideoLoop

  val quad1 = Plane().scale(1,-480f/640f,1).translate(-1.5,0,0)
  val quad2 = Plane().scale(1,-480f/640f,1).translate(1,0,0)
  var tex1:Texture = _
  var tex2:Texture = _

  var image = Image(640,480,3,1)

  var maskMat = new Mat(480,640,CvType.CV_8UC1)
  var videoMat = new Mat(480,640,CvType.CV_8UC3)

  val bytes = new Array[Byte](640*480)
  val bytesRGB = new Array[Byte](640*480*3)
  val bytesRGBA = new Array[Byte](640*480*4)

  var bg = false
  var subtract = false
  var depth = false

  override def init(){
    // loadShaders()

    tex1 = Texture(OpenNI.debugImage)
    tex2 = Texture(image)
    // tex2 = Texture(OpenNI.rgbImage)
    quad1.material = Material.basic
    quad1.material.loadTexture(tex1)
    quad2.material = Material.basic
    quad2.material.loadTexture(tex2)
    inited = true
  }

  override def draw(){
    FPS.print

    quad1.draw
    quad2.draw
  }

  override def animate(dt:Float){
    if(!inited) init()

    tex1.update

    if(depth){
      OpenNI.debugImage.buffer.rewind
      OpenNI.debugImage.buffer.get(bytesRGB)
      videoMat.put(0,0,bytesRGB)
    } else {
      OpenNI.rgbImage.buffer.rewind
      OpenNI.rgbImage.buffer.get(bytesRGB)
      videoMat.put(0,0,bytesRGB)
    }
  
    // OpenNI.userImage.buffer.asShortBuffer.get(shorts)
    OpenNI.userMaskImage.buffer.rewind
    OpenNI.userMaskImage.buffer.get(bytes)
    maskMat.put(0,0,bytes)

    var img = new Mat()
    if(subtract) videoMat.copyTo(img, maskMat)
    else videoMat.copyTo(img)

    val out = new Mat()
    loop.videoIO(img, out)

    if( out.empty()) return

    if( bg ){
      val bgmask = new Mat()
      Core.compare(out, new Scalar(0), bgmask, Core.CMP_EQ)
      videoMat.copyTo(out,bgmask)
      bgmask.release
    }

    // copy MAT to texture image
    out.get(0,0,bytesRGB)
    image.buffer.rewind
    image.buffer.put(bytesRGB)
    tex2.update

    img.release
    out.release
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
  var speed = 1f
  Keyboard.bind("r", () => loop.toggleRecord() )
  Keyboard.bind("t", () => loop.togglePlay() )
  Keyboard.bind("x", () => loop.stack() )
  Keyboard.bind("c", () => loop.clear() )
  Keyboard.bind("\t", () => loop.reverse() )
  Keyboard.bind("j", () => loop.setAlphaBeta(1f,.99f) )
  Keyboard.bind("b", () => bg = !bg )
  Keyboard.bind("v", () => subtract = !subtract )
  Keyboard.bind("z", () => depth = !depth )
  Keyboard.bind("i", () => {speed *=2; loop.setSpeed(speed) })
  Keyboard.bind("k", () => {speed /=2; loop.setSpeed(speed) })

  Keyboard.bind("p", () => com.fishuyo.seer.video.ScreenCapture.toggleRecord )
  Keyboard.bind("o", () => loop.writeToFile("",1.0,"mpeg4") )


  Mouse.clear()
  Mouse.use()
  Mouse.bind("drag", (i) => {
    val y = (Window.height - i(1)*1f) / Window.height
    val x = (i(0)*1f) / Window.width
    // # decay = (decay + 4)/8
    // # Loop.loop.setSpeed(speed)
    // loop.setAlphaBeta(decay, speed)
    println(s"$x $y")
    // loop.setAlpha(x)
    loop.setAlphaBeta(x,y)
  })

}


Script