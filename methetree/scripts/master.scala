

import com.fishuyo.seer.spacetree2._

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._

import collection.mutable.ListBuffer
import collection.mutable.HashMap

import scala.concurrent.duration._

object MasterScript extends SeerScript {

  OpenNI.initAll()
  OpenNI.alignDepthToRGB()
  OpenNI.start()
  OpenNI.pointCloud = true
  OpenNI.pointCloudDensity = 6

  Renderer().camera = new OrthographicCamera(1,1)
  Renderer().camera.nav.pos.z = 2

  KPC.loadCalibration("../methetree/calibration.txt")

  // Body models
  val skeletons = (1 to 4).map(OpenNI.getSkeleton(_)) //ListBuffer(OpenNI.getSkeleton(1))
  var mode = "noise"
  // val bodies = ListBuffer("noise", "spotlight", "spotlight", "spotlight")
  // val bodiesActive = ListBuffer(true,true,true,true)
  val meshes = ListBuffer(new Mesh(),new Mesh(),new Mesh(), new Mesh())
  meshes.foreach { case m =>
    m.primitive = Lines
    m.maxVertices = 640*480
    m.maxIndices = 10000
  }
  val models = meshes.map(Model(_))
  models.foreach { case m =>
    m.material = Material.basic
    m.material.color = RGBA(0.8,0.8,0.8,1.0)
  }

  // spotlight spheres
  val spotlights = ListBuffer[Model]()
  spotlights += Sphere().scale(0.15,0.25,1) 
  spotlights += Sphere().scale(0.15,0.25,1) 
  spotlights += Sphere().scale(0.15,0.25,1) 
  spotlights += Sphere().scale(0.15,0.25,1) 
  val rockspot = Sphere().scale(0.14,0.2,1) 
  rockspot.pose.pos.set(-0.08,-0.5,0)
  rockspot.material = Material.basic 
  rockspot.material.color = RGB(1,1,1)

  // tree
  val tree = new Tree
  tree.minDistance = 0.01 //0.05
  tree.maxDistance = 0.05 //0.35
  tree.branchLength = 0.007 //0.03
  tree.thresholdVel = 1.2 //0.1 //0.05
  tree.thresholdVelMax = 10.75 //0.2 //0.05
  tree.trimDistance = 0.1
  tree.sleep = 100

  val treeMesh = Mesh()
  treeMesh.primitive = Points
  treeMesh.maxVertices = 1000000
  val treeModel = Model(treeMesh)
  treeModel.material = Material.basic
  treeModel.material.color = RGBA(1,1,1,1)

  val falling = HashMap[Stick, Model]()
  Gravity.set(0,-.1,0)

  // Particle System
  Schedule.every(4 seconds){
    if(mode == "particles"){
      val v = Random.vec3() //+ Vec3(0,1,0)
      Schedule.over(1.0 seconds){ case t => Gravity.lerpTo(v,0.01)}
      // Gravity.set(Random.vec3())
    } else Gravity.set(0,-.1,0)
  }

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
    }
  }

  var inited = false
  var running = false
  var rockspotOn = true
  var grow = false

  // RenderNodes
  var node:RenderNode = _
  var blur:BlurNode = _
  var composite:Composite3Node = _
  var noiseNode:NoiseNode = _
  var rd:RDNode = _
  var colorize:ColorizeNode = _


  override def init(){
    // node = new RenderNode
    // node.renderer.camera = new OrthographicCamera(1,1)
    // node.renderer.camera.nav.pos.z = 2
    // node.renderer.shader = RootNode.renderer.shader
    tree.root.pos.set(0,0,0)
    tree.reset()

    blur = new BlurNode
    composite = new Composite3Node
    composite.blend0 = 1f
    composite.blend1 = 1f
    composite.blend2 = 1f
    noiseNode = new NoiseNode
    rd = new RDNode 
    colorize = new ColorizeNode
    colorize.color1 = RGBA(0,0,0,0)
    colorize.color2 = RGBA(1,1,1,.3f)
    colorize.color3 = RGBA(1,1,1,.4f)
    colorize.color4 = RGBA(1,1,1,.5f)
    colorize.color5 = RGBA(0,0,0,.6f)

    RenderGraph.reset

    RootNode.outputTo(blur)
    blur.outputTo(composite)
    blur.outputTo(rd)

    RenderGraph.addNode(rd)
    rd.outputTo(colorize)
    colorize.outputTo(composite)

    RenderGraph.addNode(noiseNode)
    noiseNode.outputTo(composite)

    inited = true
  }
  
  override def draw(){
    FPS.print

    Renderer().environment.depth = false
    Renderer().environment.blend = true
    Renderer().environment.alpha = 0.1f

    var drawParticles = false
    
      // if( bodiesActive(i)){
        mode match {
          case "spotlight" => 
            for(i <- 0 until 4)
              if(skeletons(i).tracking) spotlights(i).draw
          case "particles" => particles.draw
          case _ => models(0).draw
        }
      // }

    if(rockspotOn) rockspot.draw
    
    if(tree.dirty){
      treeMesh.clear()
      Draw.drawTreeRing(treeMesh,tree)
      treeMesh.update()
      tree.dirty = false
    }
    treeModel.draw()

    falling.foreach { case (s,m) =>
      m.draw
    }

    for( i <- 0 until 5) rd.render 
  }
  
  override def animate(dt:Float){
    if(!inited) init()

    skeletons.foreach{ case s =>
      if(s.tracking) s.updateJoints()
      //TODO
    }
    
    blur.size = 0.005
    blur.intensity = math.abs( 2*math.sin(Time())) + 0.1

    try{
      // meshes.zipWithIndex.foreach { case (m,i) =>
        // if( bodiesActive(i)){
          val m = meshes(0)
          mode match {
            case "noise" =>
              m.clear
              m.primitive = Lines
              m.vertices ++= OpenNI.pointMesh.vertices.map( (v) => { val p = v*1000; p.z *= -1; KPC.worldToScreen(p)})
              val index = Random.int(m.vertices.length)
              m.indices ++= (0 until m.maxIndices).map( _ => index() )
              m.update
            case "points" =>
              m.clear
              m.primitive = Points
              m.vertices ++= OpenNI.pointMesh.vertices.map( (v) => { val p = v*1000; p.z *= -1; KPC.worldToScreen(p)})
              m.update
            case "particles" =>
              particles ++= OpenNI.pointMesh.vertices.map( (v) => {
                val p = v*1000; p.z *= -1; val out = KPC.worldToScreen(p)
                Particle(out, Random.vec3()*0.001)
              })
              particles.animate(dt)
            case "spotlight" =>
              for( i <- 0 until 4){
                if(skeletons(i).tracking){
                  val pos = skeletons(i).joints("torso")
                  val p = pos * 1000
                  p.z *= -1
                  val out = KPC.worldToScreen(p)
                  spotlights(i).pose.pos.lerpTo(out,0.1f)
                  // println(out)
                }
              }
            case _ => ()
          }
        // }
      // }

      if(grow){
        tree.leaves.clear
        tree.leaves ++= m.vertices.map( new Leaf(_) )
        // println(tree.leaves.size)

        tree.antiLeaves.clear
        for( i <- 0 until 4){
          if(skeletons(i).tracking){
            tree.antiLeaves ++= (skeletons(i).joints.map { case(n,v) => 
              val p = v*1000; p.z *= -1; val out = KPC.worldToScreen(p)
              new AntiLeaf(out, skeletons(i).vel(n)/dt)
            })
          }
        }
      }

      var didTrim = false
      if(true){ 
        // tree.grow()
        val trimmed = tree.trim()
        trimmed.foreach { case branch =>
          didTrim = true
          val mesh = Mesh()
          mesh.primitive = Points
          Draw.branchesRing(mesh,branch)
          val model = Model(mesh)
          model.material = Material.basic
          model.material.color = RGBA(1,1,1,1)
          val stick = Stick(Vec3(), Random.vec3()*0.001, Quat(), Random.vec3()*0.015)
          falling += (stick -> model)
        }
      }

      falling.foreach{ case (s,m) =>
        s.applyGravity()
        // s.applyDamping(damping)
        s.step() // timeStep
        // s.collideGround(-1f, 0.5f)
        m.pose.pos.set(s.position)
        m.pose.quat.set(s.orientation)
        if( s.position.y < -5){
          falling.remove(s)
          m.mesh.dispose
        }
      }

      // sound
      if( sound == "wobble"){
        for( i <- 0 until 4){
          if(skeletons(i).tracking){
            var v = skeletons(i).joints("torso")
            osc.f = new Ramp(osc.f.value, v.y*40f + 90f,10)
            poemSound.setPitch(poemID, 0.8 + v.y*0.2)

            v = skeletons(i).joints("head")
            lfo.a = (v.y+1f) / 200f //new Ramp(lfo.a, v.z)

            impulseAmp = 0f
            if(didTrim){
              v = skeletons(i).vel("l_hand")
              impulseAmp = v.mag 
              v = skeletons(i).vel("r_hand")
              if(v.mag > impulseAmp) impulseAmp = v.mag
            }

            val dist = (skeletons(i).joints("r_hand") - skeletons(i).joints("l_hand")).mag
            del2.delay = new Ramp(del2.delay.value, dist*4000f, 10)
            pulse.width = new Ramp(pulse.width.value, dist*10000f, 10)
          }
        }
      }
      

    } catch { case e:Exception => () } //println(e) }

  }

  def startPerformance(){
    Schedule.clear
    composite.blend0 = 0
    composite.blend1 = 0
    composite.blend2 = 0
    mode = "noise"
    triggerNoiseRamp()
    rockspotOn = false
    rockspot.material.color.set(0,0,0)
    Schedule.over( 6 seconds){ case t => //fade noise
      composite.blend0 = t
      composite.blend1 = 0
      composite.blend2 = t
      if( t == 1f) Schedule.after(34 seconds){ //sustain noise to cut
        composite.blend2 = 0
        mode = "points"

        // Cut fall
        Schedule.after(7 seconds){ // wait before spot on
          Schedule.over(10 seconds){ case t => // spot fade in
            rockspotOn = true
            rockspot.material.color.set(t,t,t)
            //rock alive
            if( t == 1f){ // after fade, cue birds
              triggerBirds()
              Schedule.after(25 seconds){ //rock roll, spotlight
                mode = "spotlight"
                Schedule.over(20 seconds){ case t => // rock fade out
                  val v = 1f-t
                  rockspot.material.color.set(v,v,v)
                  if(t==1f) rockspotOn = false
                }

                Schedule.after(120 seconds){
                  mode = "particles"
                  sound = "wobble"
                  wobbleGain = 0f
                  Schedule.over(10 seconds){ case t =>
                    birdSound.setVolume(birdID, 0.3*(1-t))
                    wobbleGain = t*0.5
                  }

                  //Walking to audience
                  Schedule.after( 120 seconds){
                    // fade in RD
                    Schedule.over(20 seconds){ case t =>
                      composite.blend1 = t
                    }
                    Schedule.after( 90 seconds){
                      mode = "points"
                      Schedule.after(15 seconds){
                        Schedule.over(10 seconds){ case t =>
                          wobbleGain = (1-t)*0.5
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  def startPoem(){
    Schedule.clear
    poemID = poemSound.play()
    mode = "points"
    composite.blend1 = 0f
    composite.blend2 = 0f 

    Schedule.after( 17 seconds){
      grow = true
      // Schedule.after
    }
  }

  def startInstallation(){
    Schedule.clear
    composite.blend0 = 1
    composite.blend1 = 1
    composite.blend2 = 0
    Schedule.every( 30 seconds){
      mode = Random.oneOf("noise","points","spotlight","particles")()
    }
    Schedule.every( 45 seconds){
      val now = composite.blend1
      var dest = 0f
      if( Random.float() > 0.5f) dest = 1f 
      Schedule.over( 10 seconds){ case t =>
        composite.blend1 = now*(1-t)+dest*t
      }
    }
  }



  var sound = "none"
  //Scene 1
  val noiseTime1 = 40
  var noiseMax1 = 0.3
  var noiseGain = 1f
  val noise1 = new Noise
  var ramp1 = new Ramp(0,0.1,44100*noiseTime1)
  // Schedule.after(noiseTime1.seconds){ ramp1 = new Ramp(0.8,0,10000)}

  def triggerNoiseRamp(){
    sound = "noise"
    ramp1 = new Ramp(0,noiseMax1,44100*noiseTime1)
    Schedule.after(noiseTime1.seconds){ ramp1 = new Ramp(noiseMax1,0,10000)}
  }
  def triggerBirds(){
    sound = "birds"
    birdID = birdSound.loop(0.0)
    Schedule.over(10 seconds){ case t => birdSound.setVolume(birdID, t*0.3f) }
  }
  
  //Scene 2
  var wobbleGain = 0.5f
  val noise = new Noise
  val lfo = new Sine(0.1, 0.025)
  val osc = new Sine(80f)
  val lfo2 = new Sine(1,0.1)
  val del = new Delay(100f, 0.9f)
  // val del = new Delay((new Sine(1,1f))*100f+101f, 0.6f)
  var pan = new Sine(0.1, 0.5)

  var pulse = new PulseTrain(44100f)
  var impulseAmp = 0f
  val del2 = new Delay(4000f, 0.98f)

  //Poem
  // val birds = new Loop(100)
  // birds.load("../methetree/rainbirds.wav")
  // val poem = new Loop(100)
  // poem.load("../methetree/poem.wav")

  val poemSound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("../methetree/poem.wav"));
  var poemID = 0L
  val birdSound = com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("../methetree/rainbirds.wav"));
  var birdID = 0L

  override def audioIO(io:AudioIOBuffer){

    // birds.audioIO(io)
    // poem.audioIO(io)

    sound match {
      case "noise" =>
        while(io()){
          val s = (noise1() * ramp1()) * noiseGain
          io.outSet(0)(s)
          io.outSet(1)(s)
        }

      case "wobble" =>
        while(io()){

          var s = del(lfo()*noise() + del2(pulse()*impulseAmp) )
          var s2 = lfo2()*osc()
          // s +=
          val p = pan()+0.5f 
          val r = s*p + s2
          val l = s*(1f-p) + s2


          io.outSet(0)(l*wobbleGain)
          io.outSet(1)(r*wobbleGain)
        }
      case _ => ()
    }

  }

  Keyboard.bind("1", () => startPerformance() )
  Keyboard.bind("0", () => startInstallation() )
  Keyboard.bind("2", () => startPoem() )


  import de.sciss.osc.Message
  OSC.clear()
  OSC.disconnect()
  OSC.listen(8000)
  OSC.bindp {
    // case Message("/pedal", v:Int) => pedal(v/127f);
    case Message("/1/fader1", f:Float) => composite.blend0 = f
    case Message("/1/toggle1", f:Float) => if(f == 1.0) RootNode.renderer.active = true else RootNode.renderer.active = false
    case Message("/1/fader2", f:Float) => composite.blend1 = f
      if(f==0f){ rd.renderer.active = false; colorize.renderer.active=false }
      else{ rd.renderer.active = true; colorize.renderer.active=true }
    case Message("/1/fader3", f:Float) => composite.blend2 = f
      if(f==0f){ noiseNode.renderer.active = false }
      else{ noiseNode.renderer.active = true }
    case Message("/1/push1", f:Float) => mode = "noise"
    case Message("/1/push2", f:Float) => mode = "spotlight"
    case Message("/1/push3", f:Float) => mode = "particles"
    case Message("/1/push4", f:Float) => mode = "points"
    case Message("/1/push5", f:Float) => grow = !grow
    case Message("/2/fader1", f:Float) => noiseGain = f*2
    case Message("/2/toggle1", f:Float) => sound = "noise"
    case Message("/2/fader2", f:Float) => wobbleGain = f*2
    case Message("/2/toggle2", f:Float) => sound = "wobble"
    case Message("/2/fader3", f:Float) => poemSound.setVolume(poemID, f)
    case Message("/2/toggle3", f:Float) => poemID = poemSound.play()
    case Message("/2/fader4", f:Float) => poemSound.setPitch(poemID, f*2)
    case Message("/2/toggle4", f:Float) => poemSound.stop()
    case Message("/2/fader5", f:Float) => birdSound.setVolume(birdID, f)
    case Message("/2/toggle5", f:Float) => birdID = birdSound.play(0.1)
    case Message("/2/fader6", f:Float) => ()
    case Message("/2/toggle6", f:Float) => birdSound.stop()

    case Message("/3/toggle1", f:Float) => startPerformance()
    case Message("/3/toggle2", f:Float) => startPoem()
    case Message("/3/toggle8", f:Float) => startInstallation()


    case m:Message => println(m)

    // case Message("/1/y", i:Int) => y(0) = i; onPress(0)
    // case Message("/1/x", i:Int) => x(0) = i
    // case Message("/1/y", i:Int) => y(0) = i; onPress(0)
    // case Message("/6/x", i:Int) => val v = if(i < 32) 32 else i; looper.setSpeed(0,v/63f); println(v)
    // case Message("/6/y", i:Int) => y(1) = i; onPress(1)


    case msg => println(msg)
  }
}



object Draw {

  def branchesRect(m:Mesh, b:Branch){
    drawBranchRect(m,b)
    b.children.foreach( branchesRect(m,_) )
  }

  def drawBranchRect(m:Mesh, b:Branch){
    if(b.parent == null) return
    
    val r = 0.00001
    val s = Vec3(1,0,0)
    var t1 = math.log(r*b.age+1)
    var t2 = math.log(r*b.parent.age+1)

    m.vertices += b.pos - s * t1
    m.vertices += b.parent.pos - s * t2
    m.vertices += b.parent.pos - s * t2
    m.vertices += b.parent.pos + s * t2
    m.vertices += b.parent.pos + s * t2
    m.vertices += b.pos + s * t1
    m.vertices += b.pos + s * t1
    m.vertices += b.pos - s * t1
  }

  def branchesRing(m:Mesh, b:Branch){
    drawBranchRing(m,b)
    b.children.foreach( branchesRing(m,_) )
  }

  def drawBranchRing(m:Mesh, b:Branch, r:Float = 0.00001f){
    val minThick = 0.001 //0.001
    val minDist = 0.002 //0.005

    if(b.parent == null) return
    // var dir = b.growDirection
    val dir = b.pos - b.parent.pos
    val dist = dir.mag()
    val steps = (dist / minDist).toInt

    val q = Quat().getRotationTo(Vec3(0,0,1), dir.normalized)
    val vx = q.toX
    val vz = q.toY 

    for(s <- 0 until steps){
      val t = s.toFloat / steps
      val age = b.parent.age * (1-t) + b.age * t
      var thick = math.log(r*age+1)
      if( thick < minThick ) thick = minThick

      var n = (2f*math.Pi*thick / minDist).toInt
      // if( n < 4) n = 4

      val pos = b.parent.pos * (1-t) + b.pos * t
      for( i <- 0 until n){
        val phase = i.toFloat / n * 2 * Pi
        val cos = math.cos(phase)
        val sin = math.sin(phase)
        val off1 = vx * cos * thick + vz * sin * thick
        // if(off1.mag < minThick) off1.set(off1.normalized * minThick)
        m.vertices += pos + off1
        m.normals += off1.normalized
      }
    }
  }

  def drawTreeRing(m:Mesh, tree:Tree, r:Float = 0.00001f){
    
    val minThick = 0.001 //0.001
    val minDist = 0.002 //0.005

    tree.branches.foreach( (p,b) => {

      if(b.parent != null){
        // var dir = b.growDirection
        val dir = b.pos - b.parent.pos
        val dist = dir.mag()
        val steps = (dist / minDist).toInt

        val q = Quat().getRotationTo(Vec3(0,0,1), dir.normalized)
        val vx = q.toX
        val vz = q.toY 

        for(s <- 0 until steps){
          val t = s.toFloat / steps
          val age = b.parent.age * (1-t) + b.age * t
          var thick = math.log(r*age+1)
          if( thick < minThick ) thick = minThick

          var n = (2f*math.Pi*thick / minDist).toInt
          // if( n < 4) n = 4

          val pos = b.parent.pos * (1-t) + b.pos * t
          for( i <- 0 until n){
            val phase = i.toFloat / n * 2 * Pi
            val cos = math.cos(phase)
            val sin = math.sin(phase)
            val off1 = vx * cos * thick + vz * sin * thick
            // if(off1.mag < minThick) off1.set(off1.normalized * minThick)
            m.vertices += pos + off1
            m.normals += off1.normalized
          }
        }
      }
    })  
  }
}

class NoiseNode extends RenderNode {

  renderer.scene.push(Plane())

  renderer.shader = Shader.load(
    """
    attribute vec4 a_position;
    attribute vec4 a_normal;
    attribute vec2 a_texCoord0;
    attribute vec4 a_color;

    uniform mat4 u_projectionViewMatrix;
    varying vec2 v_uv;

    void main() {
      gl_Position = u_projectionViewMatrix * a_position;
      v_uv = a_texCoord0;
    }
    """,

    """
    #ifdef GL_ES
        precision mediump float;
    #endif

    varying vec2 v_uv;
    uniform float time;

    uniform sampler2D u_texture0;

    float snoise(in vec2 co){
      return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
    }
    void main(){
      vec4 color = texture2D(u_texture0, v_uv);
      float n = snoise(vec2(v_uv.x*cos(time),v_uv.y*sin(time))); 
      gl_FragColor = vec4(n, n, n, 1.0 ) + color;
    }
    """
  )
  override def render() = {
    renderer.shader.uniforms("time") = Time()
    super.render()
  }
}

MasterScript

