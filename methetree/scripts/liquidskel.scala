

import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._

import com.fishuyo.seer.spacetree._

import com.badlogic.gdx.graphics.{Texture => GdxTexture}
import com.badlogic.gdx.graphics.Pixmap

import collection.mutable.ArrayBuffer


import collection.mutable.ListBuffer
import collection.mutable.HashMap
import collection.mutable.HashSet


object Script extends SeerScript {

  var inited = false

  OpenNI.initAll()
  OpenNI.start()
  OpenNI.pointCloud = true

  val stickman = new LiquidSkeleton(OpenNI.getSkeleton(1))

  stickman.skeleton.joints("head") = Vec3(0,1,0)
  stickman.skeleton.joints("neck") = Vec3(0,0.9,0)
  stickman.skeleton.joints("torso") = Vec3(0,0.6,0)
  stickman.skeleton.joints("l_shoulder") = Vec3(-0.1,0.8,0)
  stickman.skeleton.joints("l_elbow") = Vec3(-0.3,0.6,0)
  stickman.skeleton.joints("l_hand") = Vec3(-0.5,0.4,0)
  stickman.skeleton.joints("r_shoulder") = Vec3(0.1,0.8,0)
  stickman.skeleton.joints("r_elbow") = Vec3(0.3,0.6,0)
  stickman.skeleton.joints("r_hand") = Vec3(0.5,0.4,0)
  stickman.skeleton.joints("l_hip") = Vec3(-0.1,0.45,0)
  stickman.skeleton.joints("l_knee") = Vec3(-0.2,0.2,0)
  stickman.skeleton.joints("l_foot") = Vec3(-0.2,0,0)
  stickman.skeleton.joints("r_hip") = Vec3(0.1,0.45,0)
  stickman.skeleton.joints("r_knee") = Vec3(0.2,0.2,0)
  stickman.skeleton.joints("r_foot") = Vec3(0.2,0,0)

  val mesh = new Mesh()
  mesh.primitive = Lines //Points 
  mesh.maxVertices = 640*480
  mesh.maxIndices = 10000
  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGBA(0.1,0.1,0.1,0.01)
  model.material.transparent = true
  var numIndices = 10000


  val sphere = Sphere.generateMesh(0.01, 4)
  val sphereModel = Model(sphere)
  sphereModel.material = Material.specular

  var traces = ArrayBuffer[Trace3D]()

  var time = 0f 
  var timeout = 10f 

  val maxTraces = 1000


  var grow = false
  val tree = new Tree

  val leafMesh = Mesh()
  leafMesh.primitive = Points
  leafMesh.maxVertices = 100000 //tree.leafCount

  val treeMesh = Mesh()
  treeMesh.primitive = Lines
  treeMesh.maxVertices = 100000
  val treeModel = Model(treeMesh)
  treeModel.material = Material.basic
  treeModel.material.color = RGBA(0.6,0.6,0.6,0.1)
  treeModel.material.transparent = true


  var limit = 0f


  Keyboard.clear
  Keyboard.use
  Keyboard.bind("g", () => { grow = !grow })
  Keyboard.bind("r", () => { tree.reset })

  

  override def init(){

    inited = true
    for(i <- 0 until maxTraces){
      val t = new Trace3D(20) //+ Random.int(20)
      t.mesh.primitive = TriangleStrip
      t.setColors(Vec3(0.01f,0.01,0.01), Vec3(0.001f,0.001,0.001))
      traces += t 
    }
    Camera.nav.pos.set(0f,0f,-0.8334836f)
  }

  override def draw(){
    FPS.print

    stickman.draw

    Renderer().environment.depth = false
    Renderer().environment.blend = true
    Renderer().environment.alpha = 0.1f
    Renderer().environment.lineWidth = 1f


    model.draw

    // leafMesh.clear()
    // leafMesh.vertices ++= tree.leaves.map( _.pos )
    // leafMesh.update()
    // leafMesh.draw()

    // Renderer().environment.lineWidth = 2f
    Renderer().environment.alpha = 1f

    treeMesh.clear()
    tree.branches.getAll.values.foreach( (b) => {
      if(b.parent != null){
        // treeMesh.vertices += b.pos
        // treeMesh.vertices += b.parent.pos
        drawBranchRect(treeMesh, b)
      }
    })
    treeMesh.update()
    treeModel.draw()
    // mesh.vertices.foreach{ case v =>
    //   sphereModel.pose.pos.set(v)
    //   sphereModel.draw
    // }
    // traces.foreach(_.draw)
  }

  def drawBranchRect(m:Mesh, b:Branch){
    val s = Vec3(0.00001,0,0)
    m.vertices += b.pos - s * b.age
    m.vertices += b.parent.pos - s * b.parent.age
    m.vertices += b.parent.pos - s * b.parent.age
    m.vertices += b.parent.pos + s * b.parent.age
    m.vertices += b.parent.pos + s * b.parent.age
    m.vertices += b.pos + s * b.age
    m.vertices += b.pos + s * b.age
    m.vertices += b.pos - s * b.age
  }

  override def animate(dt:Float){
    if(!inited) init()

    time += dt
    // if( time > timeout){
    //   timeout = timeout / 2f
    //   time = 0f
    //   numIndices += 2
    //   model.material.color = RGBA(100f/numIndices,100f/numIndices,100f/numIndices,0.01)
    // }
    // sphereModel.scale.set((2*math.sin(time)+2.2f))

    stickman.animate(dt)
    
    try{
    //   // OpenNI.updatePoints()
      mesh.clear
      mesh.vertices ++= OpenNI.pointMesh.vertices
      val index = Random.int(mesh.vertices.length)
      mesh.indices ++= (0 until numIndices).map( _ => index() )
      mesh.update

      tree.leaves.clear
      tree.leaves ++= OpenNI.pointMesh.vertices.map( new Leaf(_) )
      if(grow) tree.grow()


    // for( i <- 0 until maxTraces){
    //   val v = OpenNI.pointMesh.vertices(i)
    //   traces(i)(v)
    // }
    
    } catch { case e:Exception => println(e) }

  }
  
  val noise = new Noise
  val lfo = new Sine(0.1, 0.025)
  val osc = new Sine(80f)
  val lfo2 = new Sine(1,0.1)
  val del = new Delay(100f, 0.9f)
  var pan = new Sine(0.1, 0.5)

  var pulse = new PulseTrain(44100f)
  var impulseAmp = 0f
  val del2 = new Delay(4000f, 0.98f)


  override def audioIO(io:AudioIOBuffer){
    while(io()){
      var s = del(lfo()*noise() + del2(pulse()*impulseAmp) )
      var s2 = lfo2()*osc()
      // s +=
      val p = pan()+0.5f 
      val r = s*p + s2
      val l = s*(1f-p) + s2
      io.outSet(0)(l)
      io.outSet(1)(r)
    }
  }
}

class LiquidSkeleton(override val skeleton:Skeleton) extends SkeletonVisualization(skeleton){

  var t = 0f
  val mesh = Mesh()
  mesh.primitive = Points

  // agents
  val particles = ArrayBuffer[Particle]()
  val traces = ArrayBuffer[Trace3D]()
  val current = ArrayBuffer[String]()
  val targets = ArrayBuffer[String]()
  val times = ArrayBuffer[Float]()

  def addAgent(){
    if(particles.size > 200) return
    println("adding agent: " + particles.size)
    val j = Random.oneOf(Joint.strings :_*)()
    val p = Particle(skeleton.joints(j)) //, Random.vec3()*0.001)
    val t = new Trace3D(50)
    t.mesh.primitive = TriangleStrip
    particles += p 
    traces += t
    current += j
    targets += Random.oneOf(Joint.connections(j) :_*)()
    val v = (skeleton.joints(targets.last) - p.position).normalize
    p.lPosition = p.position - v*0.01f
    times += 0f
  }

  override def draw(){
    // mesh.draw()
    if(skeleton.tracking) traces.foreach(_.draw())
  }
  override def animate(dt:Float){

    t += dt
    val time = t*10f % 1f 

    // println(Camera.nav.pos)

    if(skeleton.tracking) skeleton.updateJoints()

    mesh.clear
    mesh.vertices ++= skeleton.joints.values
    mesh.update

    var v = skeleton.joints("torso")
    val osc = Script.osc
    osc.f = new Ramp(osc.f.value, v.y*10f + 80f,10)

    v = skeleton.joints("head")
    val nlfo = Script.lfo
    nlfo.a = (v.y+1f) / 200f //new Ramp(lfo.a, v.z)

    Script.impulseAmp = 0f
    v = skeleton.vel("l_hand")
    var mag = v.mag
    if(mag > 0.01){
      Script.impulseAmp = mag 
    }
    if(Script.time > 20f && mag > 0.4) addAgent()
    

    v = skeleton.vel("r_hand")
    mag = v.mag
    if(mag > 0.01){
      Script.impulseAmp = mag 
    }
    if(Script.time > 20f && mag > 0.4) addAgent()



    val dist = (skeleton.joints("r_hand") - skeleton.joints("l_hand")).mag
    Script.del2.delay = new Ramp(Script.del2.delay.value, dist*4000f, 10)
    Script.pulse.width = new Ramp(Script.pulse.width.value, dist*10000f, 10)

    val c = math.abs(Script.lfo.value) * 10f
    Script.model.material.color = RGBA(c,c,c,c)

    for( i <- 0 until particles.size){
      val p = particles(i)
      var joint = current(i)
      var target = targets(i)
      times(i) = (times(i) + dt*skeleton.vel(joint).mag + 4*dt) % 1f

      
      val was = skeleton.joints(joint)
      val dest = skeleton.joints(target)
      p.position = was.lerp(dest, times(i)) + Random.vec3()*0.01
      // p.position.lerpTo(dest, 0.001)
      // p.lPosition.lerpTo(dest, 0.001)

      val dist = dest - p.position
      val r2 = dist.magSq
      if(r2 > 0.1){
        p.applyForce( dist.normalized * 0.02 / r2 )
      }
      if(dist.mag < 0.1){
        times(i) = 0f
        current(i) = target
        targets(i) = Random.oneOf(Joint.connections(target):_*)()
        val v = (skeleton.joints(targets(i)) - p.position).normalize
        p.lPosition = p.position - v*0.01f //+ Random.vec3()*0.001f
      }

      // val neighbors = Joint.connections(joint)
      // neighbors.foreach { case n =>
      //   val d = skeleton.joints(n)
      //   val x = d - p.position
      //   val r2 = x.magSq
      //   if( r2 > 0.1){
      //     val f = 0.01 /* * mass*m.mass*/ / r2 //- spin.mag * 0.0001
      //     p.applyForce( x.normalized * f )
      //   }
      //   if(r2 < 0.001){
      //     current(i) = n
      //     targets(i) = Random.oneOf(Joint.connections(n):_*)()
      //     val v = (skeleton.joints(targets(i)) - p.position).normalize
      //     p.lPosition = p.position - v*0.01f //+ Random.vec3()*0.001f
      //   }
      // }

      // p.step()
      traces(i).setColors(Vec3(math.abs(Script.lfo2.value)*2f),Vec3(0.01f))
      traces(i)(p.position)
    }

  }

  def applyGravity( p:Particle, d:Vec3 ){
    val x = d - p.position
    val r2 = x.magSq

    if( r2 > 0.1){
      val f = 0.01 /* * mass*m.mass*/ / r2 //- spin.mag * 0.0001
      // val f = mass*m.mass / r2
      p.applyForce( x.normalized * f )
      // applyTorque( (orientation.slerp(m.orientation, 0.0001)*orientation.inverse * f) )
      // orientation.slerpTo(m.orientation, f)
    }
  }
}

// Keyboard.clear
// Keyboard.use
// Keyboard.bind("g", ()=>{Script.stickman.addAgent()})
// Keyboard.bind("c", ()=>{println(Camera.nav.pos)})


Script