
// openni user looper

import com.fishuyo.seer.openni._
import com.fishuyo.seer.cv._

import org.opencv.core._
import org.opencv.highgui._
import org.opencv.imgproc._

import collection.mutable.ArrayBuffer
import collection.mutable.ListBuffer
// import com.badlogic.gdx.graphics.{Texture => GdxTexture}
// import com.badlogic.gdx.graphics.Pixmap

import com.twitter.chill.KryoInjection
import scala.util.Success



object Script extends SeerScript {

  var inited = false

  OpenNI.initAll()
  OpenNI.alignDepthToRGB()
  OpenNI.start()
  OpenNI.pointCloud = true
  OpenNI.pointCloudDensity = 4
  OpenNI.makeDebugImage = true

  OpenCV.loadLibrary()

  val loop = new UserLoop
  val out = ListBuffer[User]()

  val mesh = new Mesh()
  mesh.primitive = Points 
  mesh.maxVertices = 640*480
  // mesh.maxIndices = 10002
  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGB(1)


  override def init(){
    inited = true
  }

  override def draw(){
    FPS.print

    model.draw
    // quad1.draw
    // quad2.draw
  }

  override def animate(dt:Float){
    if(!inited) init()

    val users = OpenNI.users.values.filter(_.tracking)
    users.foreach( _.skeleton.updateJoints )
    try{
      if(!users.isEmpty){
        users.head.points.clear
        users.head.points ++= OpenNI.pointMesh.vertices
      }

      // copy users
      val in = ListBuffer[User]()
      in ++= users.map(User(_))

      out.clear
      loop.io(in, out)

      mesh.clear
      out.foreach{ case user =>
        // mesh.vertices ++= user.skeleton.joints.values
        mesh.vertices ++= user.points
      }
      mesh.update

    } catch { case e:Exception => println(e) }


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
  // Keyboard.bind("b", () => bg = !bg )
  // Keyboard.bind("v", () => subtract = !subtract )
  // Keyboard.bind("z", () => depth = !depth )
  Keyboard.bind("i", () => {speed *=2; loop.setSpeed(speed) })
  Keyboard.bind("k", () => {speed /=2; loop.setSpeed(speed) })

  Keyboard.bind("p", () => com.fishuyo.seer.video.ScreenCapture.toggleRecord )
  // Keyboard.bind("o", () => loop.writeToFile("",1.0,"mpeg4") )
  Keyboard.bind("o", () => saveLoop(loop.frames,"out.loop"))
  Keyboard.bind("u", () => {
    val frames = loadLoop("out.loop")
    if(frames.isDefined) loop.frames = frames.get
  })


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

def loadLoop(filename:String) = {
  import java.io._
  val bis = new BufferedInputStream(new FileInputStream(filename))
  val aval = bis.available
  val buffer = new Array[Byte](aval)
  val red = bis.read(buffer)
  println(s"read $red bytes of $aval")

  var user:Option[ArrayBuffer[ListBuffer[User]]] = None
  // var user:Option[User] = None
  val decode = KryoInjection.invert(buffer)
  decode match {
    case Success(u:ArrayBuffer[ListBuffer[User]]) => user = Some(u.clone)
    case m => println("Invert failed!" + m + " " + m.getClass.getSimpleName)
  }
  user
}
def saveLoop(u:ArrayBuffer[ListBuffer[User]], filename:String){
// def saveLoop(u:User, filename:String){
  import java.io._
  val bytes = KryoInjection(u)
  val bos = new BufferedOutputStream(new FileOutputStream(filename))
  Stream.continually(bos.write(bytes))
  bos.close()
}


Script