
// openni user looper
// song and dance all powerful version..

import com.fishuyo.seer.openni._
import com.fishuyo.seer.cv._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio.gen._
import com.fishuyo.seer.audio._

import org.lwjgl.opengl.GL11

import org.opencv.core._
import org.opencv.highgui._
import org.opencv.imgproc._

import collection.mutable.ArrayBuffer
import collection.mutable.ListBuffer

import com.twitter.chill.KryoInjection
import scala.util.Success

import scala.concurrent.duration._


object Script extends SeerScript {

  var inited = false

  var fbnode:FeedbackNode = _
  var noisenode:NoiseNode = _
  var composite:CompositeNode = _

  OpenNI.initAll()
  // OpenNI.alignDepthToRGB()
  OpenNI.start()
  OpenNI.pointCloud = true
  OpenNI.pointCloudDensity = 4
  // OpenNI.makeDebugImage = true

  OpenCV.loadLibrary()

  // video looper
  var videoLoop = false
  val vloop = new VideoLoop()
  val loopQuad = Plane().scale(1,-480f/640f,1).translate(0,0,0)
  var loopTexture:Texture = _
  var loopImage = Image(640,480,3,1)
  var maskMat = new Mat(480,640,CvType.CV_8UC1)
  var videoMat = new Mat(480,640,CvType.CV_8UC3)
  val bytes = new Array[Byte](640*480)
  val bytesRGB = new Array[Byte](640*480*3)
  var bg = false
  var subtract = true
  var depth = true

  // user looper
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
  model.material.color = RGBA(1,1,1,1)

  var asParticles = false
  val particles = new ParticleEmitter(50000){
    // val s = Model(Sphere.generateMesh(0.01,4))
    val mesh = Mesh()
    mesh.primitive = Points
    mesh.maxVertices = maxParticles
    val model = Model(mesh)
    model.material = Material.basic
    model.material.color = RGBA(1,1,1,0.7)

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

  val n = 20
  val field = new VecField3D(n,Vec3(0),5f)
  var updateField = false
  particles.field = Some(field)
  particles.fieldAsForce = false
  randomizeField()

  def randomizeField(){
    for( z<-(0 until n); y<-(0 until n); x<-(0 until n)){
      val cen = field.centerOfBin(x,y,z).normalize
      // field.set(x,y,z,Vec3(0))
      if(particles.fieldAsForce) field.set(x,y,z, Random.vec3()*4)
      else field.set(x,y,z, Random.vec3()*0.01)
      //field.set(x,y,z, cen * -.1f)
      //field.set(x,y,z, Vec3(x,y,z).normalize * .1f)
      //field.set(x,y,z, Vec3( -cen.z + -cen.x*.1f, -cen.y, cen.x ).normalize * .1f )
      //field.set(x,y,z, Vec3( math.sin(cen.x*3.14f), 0, math.cos(cen.z*3.14f) ).normalize * .1f)  
      //field.set(x,y,z, Vec3( cen.x, y/10.0f, cen.z).normalize * .1f )
      //field.set(x,y,z, Vec3(0,.1f,0) )
      //field.set(x,y,z, (Vec3(0,1,0)-cen).normalize * .1f )
    }
  }

  Gravity.set(0,0,0)
  // Schedule.every(4 seconds){
    // val v = Random.vec3()
    // Schedule.over(1.0 seconds){ case t => Gravity.lerpTo(v,0.01)}
    // Gravity.set(Random.vec3())
  // }

  var drawTerrain = false
  val (tx,ty) = (129,129)
  val terrain = Plane.generateMesh(50f,50f,tx,ty,Quat.up)
  fractalize(terrain,tx,ty)
  val cen = terrain.vertices(terrain.vertices.length/2)
  val ground = Model(terrain).translate(0,-cen.y-2,0)
  ground.material = Material.specular
  ground.material.color = RGBA(0.6,0,0.8,0.3)

  val groundLines = Model(terrain).translate(0,-cen.y-2+0.01,0)
  groundLines.material = Material.specular
  groundLines.material.color = RGBA(0,0.6,0.8,0.3)

  var autocam = false 
  var maxCamSpeed = 1f
  var orbitPeriod = 1f
  var focalPoint = Vec3()

  override def init(){
    inited = true

    // Renderer().environment.depth = false
    // Renderer().environment.blend = true
    // Renderer().environment.blendFunc(SrcAlpha,OneMinusSrcAlpha)
    // Renderer().environment.blendFunc(SrcAlpha,DstAlpha)
    // Renderer().environment.blendFunc(One,One)

    // fbnode = new FeedbackNode(0.995,0.005)
    fbnode = new FeedbackNode(0.9,0.1)
    composite = new CompositeNode(1,0)
    noisenode = new NoiseNode 
    // fbnode.renderer.resize = false
    // composite.renderer.resize = false

    RenderGraph.reset
    RenderGraph.addNode(noisenode)
    
    RootNode.outputTo(composite)
    noisenode.outputTo(composite)

    composite.outputTo(fbnode)
    // RootNode.outputTo(fbnode)
    fbnode.outputTo(new ScreenNode())

    loopTexture = Texture(loopImage)
    loopQuad.material = Material.basic
    loopQuad.material.loadTexture(loopTexture)

    terrain.recalculateNormals
    terrain.update

    val frames = loadLoop("emily_fela_dance.bin")
    if(frames.isDefined) loops(3).frames = frames.get

  }

  override def draw(){
    FPS.print

    // fbnode.renderer.camera.viewportWidth = 2.0 * fbnode.renderer.viewport.aspect
    // fbnode.renderer.camera.viewportHeight = 2.0


    if(drawTerrain){
      GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL)
      ground.draw
      GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE)
      groundLines.draw
      GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL)
    }

    if(videoLoop){
      loopQuad.draw
    } else {
      if(asParticles) particles.draw
      else model.draw
    }

  }

  override def animate(dt:Float){
    if(!inited) init()
    noisenode.time += dt

    if(videoLoop){
      if(depth){
        OpenNI.debugImage.buffer.rewind
        OpenNI.debugImage.buffer.get(bytesRGB)
        videoMat.put(0,0,bytesRGB)
      } else {
        OpenNI.rgbImage.buffer.rewind
        OpenNI.rgbImage.buffer.get(bytesRGB)
        videoMat.put(0,0,bytesRGB)
      }
    
      OpenNI.userMaskImage.buffer.rewind
      OpenNI.userMaskImage.buffer.get(bytes)
      maskMat.put(0,0,bytes)

      var img = new Mat()
      if(subtract) videoMat.copyTo(img, maskMat)
      else videoMat.copyTo(img)

      val out = new Mat()
      vloop.videoIO(img, out)

      if( out.empty()) return

      if( bg ){
        val bgmask = new Mat()
        Core.compare(out, new Scalar(0), bgmask, Core.CMP_EQ)
        videoMat.copyTo(out,bgmask)
        bgmask.release
      }

      // copy MAT to texture image
      out.get(0,0,bytesRGB)
      loopImage.buffer.rewind
      loopImage.buffer.put(bytesRGB)
      loopTexture.update

      img.release
      out.release
    } else {
      if(audioReactive){
        val bg = follow.value //* 2
        Renderer().environment.backgroundColor.set(bg,bg,bg)
        if(doBeat){
          if(drawTerrain){
            val r = Random.float(-.1,.1)
            // terrain.vertices.foreach{ case v => v.y += math.abs(v.x)*r()}
            terrain.vertices.foreach{ case v => v.y += bg*r()}
            terrain.recalculateNormals
            terrain.update
          }
          val c = HSV2RGBA(HSV(util.Random.float(),1,1)); c.a = 0.7
          particles.model.material.color = c
          model.material.color = c
          doBeat = false
        }
      }

      // update users 
      val users = OpenNI.users.values.filter(_.tracking)
      users.foreach { case user => 
        user.skeleton.updateJoints
        // set field to skeleton velocity
        if(updateField){
          for( j <- Joint.strings){
            if(particles.fieldAsForce) field(user.skeleton.joints(j)) = user.skeleton.vel(j)*4
            else field(user.skeleton.joints(j)) = user.skeleton.vel(j)*0.01
          }      
        }
        user.points.clear 
      }

      try{
        // hack to put point data in user object for now
        if(!users.isEmpty) users.head.points ++= OpenNI.pointMesh.vertices
        
        // add user point data to particles
        // particles ++= OpenNI.pointMesh.vertices.map(Particle(_, Random.vec3()*0.001))

        // copy users
        val in = ListBuffer[User]()
        in ++= users.map(User(_))

        // run looper
        out.clear
        loops(l).io(in, out)

        // setup meshes and particle system
        mesh.clear

        var i = 0
        out.foreach{ case user =>
          // mesh.vertices ++= user.skeleton.joints.values
          if(asParticles) particles ++= user.points.map(Particle(_, Random.vec3()*0.001))
          else mesh.vertices ++= user.points

          var v = user.skeleton.joints("head")
          // println(v.x + " " + v.y)
          osc(i).f = new Ramp(osc(i).f.value, ((v.y+1f)*50f + 60f),100)
          // osc(i).f = new Ramp(osc(i).f.value, ((v.y+1f)*50f + 80f),100)
          pan(i) = new Ramp(pan(i).value, v.x, 100)

          impulseAmp(i) = 0f
          v = user.skeleton.vel("l_hand")
          var mag = v.mag
          println(mag)

          if(mag > 0.05){
            impulseAmp(i) = 1f 
          }
    
          v = user.skeleton.vel("r_hand")
          mag = v.mag
          if(mag > 0.05){
            impulseAmp(i) = 1f 
          }

          val l = user.skeleton.joints("l_hand")
          val r = user.skeleton.joints("r_hand")

          val dist = (r-l).mag()
          del(i).delay = new Ramp(del(i).delay.value, (r-l).mag()*4000f, 100)
          pulse(i).width = new Ramp(pulse(i).width.value, dist*10000f, 100)

          // lfo(i).a (v.y+1f) / 2f //new Ramp(lfo.a, v.z)

          lfo(i).a *= 1.15
          if(lfo(i).a > 0.8f) lfo(i).a = 0.8f 

          lfo(i).f = new Ramp(lfo(i).f.value, (r.y+1f), 100)

          // Script.impulseAmp = 0f
          // v = user.skeleton.vel("l_hand")
          // var mag = v.mag
          // if(mag > 0.01){
          //   Script.impulseAmp = mag 
          // }
          i += 1
        }
        for(j <- (i until 10)){
          impulseAmp(j) = 0f
          val f = j*80f + 20f //Random.float() * 220f
          osc(j).f = new Ramp(osc(j).f.value, f, 44100)
          lfo(j).a *= 0.996f
        }


        if(asParticles) particles.animate(dt)
        else mesh.update

      } catch { case e:Exception => println(e) }

    }

  }

  def loadShaders(){
    // Shader.load("rd", File("shaders/basic.vert"), File("shaders/rd_img.frag")).monitor
    // Shader.load("colorize", File("shaders/basic.vert"), File("shaders/colorize.frag")).monitor
  }

  // override def onUnload(){
    // OpenNI.disconnect
  // }

  Keyboard.clear
  Keyboard.use
  var speed = 1f
  Keyboard.bind("r", () => if(videoLoop) vloop.toggleRecord else loops(l).toggleRecord() )
  Keyboard.bind("t", () => if(videoLoop) vloop.togglePlay else loops(l).togglePlay() )
  Keyboard.bind("x", () => if(videoLoop) vloop.stack else loops(l).stack() )
  Keyboard.bind("c", () => if(videoLoop) vloop.clear else loops(l).clear() )
  Keyboard.bind("\t", () => if(videoLoop) vloop.reverse else loops(l).reverse() )
  // Keyboard.bind("j", () => loops(l).setAlphaBeta(1f,.99f) )
  Keyboard.bind("b", () => bg = !bg )
  Keyboard.bind("v", () => subtract = !subtract )
  Keyboard.bind("z", () => depth = !depth )
  Keyboard.bind("y", () => autocam = !autocam )
  Keyboard.bind("g", () => drawTerrain = !drawTerrain )
  Keyboard.bind(".", () => {toggleHeavyFeedback()}) //audioReactive = !audioReactive; drawTerrain = !drawTerrain; particles.model.material.color.set(1,1,1,0.7); model.material.color.set(1,1,1,1) })
  Keyboard.bind(",", () => {
    particles.fieldAsForce = !particles.fieldAsForce
    randomizeField()
  })
  Keyboard.bind("i", () => {speed *=2; if(videoLoop) vloop.setSpeed(speed) else loops(l).setSpeed(speed) })
  Keyboard.bind("k", () => {speed /=2; if(videoLoop) vloop.setSpeed(speed) else loops(l).setSpeed(speed) })
  Keyboard.bind("=", () => {l += 1; if(l > 3) l = 3 })
  Keyboard.bind("-", () => {l -= 1; if(l < 0) l = 0 })

  // Keyboard.bind("p", () => com.fishuyo.seer.video.ScreenCapture.toggleRecord )
  // Keyboard.bind("o", () => loops(l).writeToFile("",1.0,"mpeg4") )
  Keyboard.bind("o", () => saveLoop(loops(l).frames))
  Keyboard.bind("u", () => {
    val frames = loadLoop("2015-08-31-22.53.37.bin")
    if(frames.isDefined) loops(l).frames = frames.get
  })

  Keyboard.bind("m", () => { mode += 1; if( mode > 3) mode = 0; changeMode() })
  Keyboard.bind("n", () => { mode -= 1; if( mode < 0) mode = 1; changeMode() })
  var (db0,db1) = (0.9,0.1)
  def changeMode(){
    mode match {
      // case 0 => videoLoop = true; fbnode.blend0 = 0.9; fbnode.blend1 = 0.1;
      case 0 => videoLoop = false; asParticles = false; OpenNI.pointCloudDensity = 4; db0=0.98; db1=0.1; fbnode.setBlend(db0,db1); composite.setBlend(1,0)
      case 1 => asParticles = true; particles.clear; OpenNI.pointCloudDensity = 4; db0=0.9; db1=0.1; fbnode.setBlend(db0,db1); composite.setBlend(1,0); setTerrainMode(false)
      case 2 => asParticles = true; setTerrainMode(true); composite.setBlend(1,0); db0=0.98; db1=0.1; fbnode.setBlend(db0,db1);
      // case 2 => videoLoop = false; asParticles = true; fbnode.blend0 = 0.9999; fbnode.blend1 = 0.033;
      case 3 => 
        Schedule.over(10 seconds){ case t =>
          composite.setBlend(1.0f-t,t)
          if(t < 0.5) fbnode.setBlend(1.0f-0.5*t,t*0.5)
          else fbnode.setBlend(1.0f-t,t*0.5)
          if(t == 1){
            setTerrainMode(false);
            Schedule.over(30 seconds){ case t =>
              fbnode.setBlend(t,0.5)
            }
          }
        }
      case _ => ()
    }    
  }

  var heavyFeedback = false
  def toggleHeavyFeedback(){
    heavyFeedback = !heavyFeedback
    if(heavyFeedback) fbnode.setBlend(0.9999, 0.033)
    else fbnode.setBlend(db0,db1)
  }

  def setTerrainMode(enable:Boolean){
    drawTerrain = enable
    audioReactive = enable
    particles.model.material.color.set(1,1,1,0.7)
    model.material.color.set(1,1,1,1)
  }


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
    vloop.setAlphaBeta(x,y)
    fbnode.blend0 = y
    fbnode.blend1 = x

  })

  val follow = new EnvFollow(5000)
  val beat = new BeatTrack
  var doBeat = false
  var audioReactive = false

  val noise = new Noise
  val osc = new Array[Sine](10) 
  val lfo = new Array[Sine](10) 
  val pan = new Array[Ramp](10)
  val del = new Array[Delay](10)
  val pulse = new Array[PulseTrain](10)
  val impulseAmp = new Array[Float](10)
  for(i <- (0 until 10)){
    osc(i) = new Sine(0f)
    lfo(i) = new Sine(0.1, 0.025)
    pan(i) = new Ramp(0.5,0.5,100)
    del(i) = new Delay(4000f, 0.98f)
    pulse(i) = new PulseTrain(44100f)
  }
  // val lfo2 = new Sine(1,0.1)
  // val del2 = new Delay(100f, 0.9f)
  // var pan = new Sine(0.1, 0.5)

  // var pulse = new PulseTrain(44100f)
  // var impulseAmp = 0f
  // val del2 = new Delay(4000f, 0.98f)


  override def audioIO(io:AudioIOBuffer){
    while(io()){
      follow(io.in(0))
      beat(io.in(0))
      if( beat.value > 0f) doBeat = true

      var l=0f
      var r=0f
      for( i <- 0 until 10){
        val s = lfo(i)()*osc(i)() + del(i)(pulse(i)()*impulseAmp(i))
        // val s = del(i)(lfo(i)()*osc(i)())
        val p = (pan(i)() + 1f) * 0.5f
        l += s*(1f-p)
        r += s*p 
      }

      // var s = del(lfo()*noise() + del2(pulse()*impulseAmp) )
      // var s2 = lfo2()*osc()
      // s +=
      // val p = pan()+0.5f 
      // val r = s*p + s2
      // val l = s*(1f-p) + s2
      io.outSet(0)(l/10f)
      io.outSet(1)(r/10f)
    }
  }

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

def fractalize(m:Mesh, nx:Int, ny:Int) = {

  val roughness = 0.05f

  divide(nx-1)

  def divide(size:Int){
    val half = size/2
    var scale = roughness * size
    if(half < 1) return
    for(y <- half until (ny) by size; x <- half until (nx) by size)
      square(x,y,half, Random.float() * scale * 2 - scale)

    for(y <- 0 until (ny) by half; x <- ((y+half)%size) until (nx) by size)
      diamond(x,y,half, Random.float() * scale * 2 - scale)
    
    divide(size/2)
  }

  def indx(i:Int,j:Int ) = {
    var x = i; var y = j;
    while( x < 0 ) x += nx; while( x >= nx ) x -= nx;
    while( y < 0 ) y += ny; while( y >= ny ) y -= ny;
    nx*y + x
  }

  def square(x:Int, y:Int, size:Int, offset:Float){
    val avg = Vec3()
    avg += m.vertices(indx(x-size, y-size))
    avg += m.vertices(indx(x+size, y-size))
    avg += m.vertices(indx(x+size, y+size))
    avg += m.vertices(indx(x-size, y+size))
    avg /= 4f

    m.vertices(y*nx+x).y = avg.y + offset
    // m.vertices(y*nx+x) = avg * offset
  }

  def diamond(x:Int, y:Int, size:Int, offset:Float){
    val avg = Vec3()
    avg += m.vertices(indx(x, y-size))
    avg += m.vertices(indx(x+size, y))
    avg += m.vertices(indx(x, y+size))
    avg += m.vertices(indx(x-size, y))
    avg /= 4f

    m.vertices(y*nx+x).y = avg.y + offset
    // m.vertices(y*nx+x) = avg * offset

  }
}

class EnvFollow(val size:Int = 2048) extends audio.Gen {
  def apply() = this(0f)
  def apply(in:Float) = {
    value -= value/size
    value += math.abs(in)/size
    value
  }
}

class BeatTrack extends audio.Gen {
  var thresh = 0.1f
  var decay = 0.0000001f
  var hold = 2000
  var t = hold
  def apply() = this(0f)
  def apply(in:Float) = {
    value = 0f
    thresh -= decay
    t += 1
    if( t > hold && math.abs(in) > thresh){
      value = 1f
      thresh = math.abs(in) + 0.000001
      t = 0
    }
    value
  }
}



Script