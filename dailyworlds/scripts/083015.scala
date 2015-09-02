
// openni user looper

import com.fishuyo.seer.openni._
import com.fishuyo.seer.cv._
import com.fishuyo.seer.particle._

import org.opencv.core._
import org.opencv.highgui._
import org.opencv.imgproc._

import collection.mutable.ArrayBuffer
import collection.mutable.ListBuffer
// import com.badlogic.gdx.graphics.{Texture => GdxTexture}
// import com.badlogic.gdx.graphics.Pixmap

import com.twitter.chill.KryoInjection
import scala.util.Success

import scala.concurrent.duration._


object Script extends SeerScript {

  var inited = false

  OpenNI.initAll()
  OpenNI.alignDepthToRGB()
  OpenNI.start()
  OpenNI.pointCloud = true
  OpenNI.pointCloudDensity = 8
  OpenNI.makeDebugImage = true

  OpenCV.loadLibrary()

  val loops = (0 until 4).map( (i) => new UserLoop())
  var l = 0
  var mode = 0

  val out = ListBuffer[User]()

  val mesh = new Mesh()
  mesh.primitive = Points 
  mesh.maxVertices = 640*480
  // mesh.maxIndices = 10002
  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGB(1)

  var asParticles = true
  val particles = new ParticleEmitter(60000){
    // val s = Model(Sphere.generateMesh(0.01,4))
    val mesh = Mesh()
    mesh.primitive = Points
    mesh.maxVertices = maxParticles
    val model = Model(mesh)
    model.material = Material.basic
    model.material.color = RGB(0.9)

    override def draw(){
      mesh.clear
      mesh.vertices ++= particles.map( _.position )
      mesh.update
      model.draw
      // particles.foreach( (p) => {
        // s.pose.pos = p.position 
        // s.draw
      // })
    }
  }
  Gravity.set(0,4,0)
  Schedule.every(4 seconds){
    val v = Random.vec3()
    Schedule.over(1.0 seconds){ case t => Gravity.lerpTo(v,0.01)}
    Gravity.set(Random.vec3())
  }

  override def init(){
    inited = true
  }

  override def draw(){
    FPS.print

    // quad1.draw
    // quad2.draw
    if(asParticles) particles.draw
    else model.draw

  }

  override def animate(dt:Float){
    if(!inited) init()

    val users = OpenNI.users.values.filter(_.tracking)
    users.foreach( _.skeleton.updateJoints )
    users.foreach( _.points.clear )
    try{
      if(!users.isEmpty){
        users.head.points ++= OpenNI.pointMesh.vertices
      }

      // particles ++= OpenNI.pointMesh.vertices.map(Particle(_, Random.vec3()*0.001))


      // copy users
      val in = ListBuffer[User]()
      in ++= users.map(User(_))

      out.clear
      loops(l).io(in, out)

      mesh.clear
      out.foreach{ case user =>
        // mesh.vertices ++= user.skeleton.joints.values
        if(asParticles) particles ++= user.points.map(Particle(_, Random.vec3()*0.001))
        else mesh.vertices ++= user.points
      }

      if(asParticles) particles.animate(dt)
      else mesh.update

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
  Keyboard.bind("r", () => loops(l).toggleRecord() )
  Keyboard.bind("t", () => loops(l).togglePlay() )
  Keyboard.bind("x", () => loops(l).stack() )
  Keyboard.bind("c", () => loops(l).clear() )
  Keyboard.bind("\t", () => loops(l).reverse() )
  // Keyboard.bind("j", () => loops(l).setAlphaBeta(1f,.99f) )
  // Keyboard.bind("b", () => bg = !bg )
  // Keyboard.bind("v", () => subtract = !subtract )
  // Keyboard.bind("z", () => depth = !depth )
  Keyboard.bind("i", () => {speed *=2; loops(l).setSpeed(speed) })
  Keyboard.bind("k", () => {speed /=2; loops(l).setSpeed(speed) })
  Keyboard.bind("=", () => {l += 1; if(l > 3) l = 3 })
  Keyboard.bind("-", () => {l -= 1; if(l < 0) l = 0 })
  Keyboard.bind("m", () => { 
    mode += 1; if( mode > 1) mode = 0
    mode match {
      case 0 => asParticles = false; OpenNI.pointCloudDensity = 4
      case 1 => asParticles = true; particles.clear; OpenNI.pointCloudDensity = 8
      case _ => ()
    }
  })

  Keyboard.bind("p", () => com.fishuyo.seer.video.ScreenCapture.toggleRecord )
  // Keyboard.bind("o", () => loops(l).writeToFile("",1.0,"mpeg4") )
  Keyboard.bind("o", () => saveLoop(loops(l).frames))
  Keyboard.bind("u", () => {
    val frames = loadLoop("out.loop")
    if(frames.isDefined) loops(l).frames = frames.get
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
    loops(l).setAlphaBeta(x,y)
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
def saveLoop(u:ArrayBuffer[ListBuffer[User]], filename:String=""){
// def saveLoop(u:User, filename:String){
  import java.io._
  val form = new java.text.SimpleDateFormat("yyyy-MM-dd-HH.mm.ss")
  val filename = form.format(new java.util.Date()) + ".bin" 

  val bytes = KryoInjection(u)
  val bos = new BufferedOutputStream(new FileOutputStream(filename))
  Stream.continually(bos.write(bytes))
  bos.close()
}


Script