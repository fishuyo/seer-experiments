

// package com.fishuyo.seer
// package methetree

// import graphics._
// import spatial._
// import audio._
// import io._
// import dynamic._
// import util._
// import openni._
// import particle._
// import spacetree2._

// import collection.mutable.ListBuffer
// import collection.mutable.HashMap

// import concurrent.duration._

// object Master extends SeerApp {

//   GdxAudio.init()
//   Audio().start

//   OpenNI.initAll()
//   // OpenNI.alignDepthToRGB()
//   OpenNI.start()
//   OpenNI.pointCloud = true
//   OpenNI.pointCloudDensity = 4

//   Renderer().camera = new OrthographicCamera(1,1)
//   Renderer().camera.nav.pos.z = 2

//   KPC.loadCalibration("../methetree/calibration.txt")

//   // Body models
//   val skeletons = ListBuffer(OpenNI.getSkeleton(1))
//   val bodies = ListBuffer("noise", "noise", "noise", "noise")
//   val bodiesActive = ListBuffer(true,true,true,true)
//   val meshes = ListBuffer(new Mesh(),new Mesh(),new Mesh(), new Mesh())
//   meshes.foreach { case m =>
//     m.primitive = Lines
//     m.maxVertices = 640*480
//     m.maxIndices = 10000
//   }
//   val models = meshes.map(Model(_))
//   models.foreach { case m =>
//     m.material = Material.basic
//     m.material.color = RGBA(0.9,0.9,0.9,1.0)
//   }

//   // spotlight spheres
//   val spotlights = ListBuffer[Model]()
//   spotlights += Sphere().scale(0.2,0.4,1) 
//   spotlights += Sphere().scale(0.2,0.4,1) 
//   spotlights += Sphere().scale(0.2,0.4,1) 
//   spotlights += Sphere().scale(0.2,0.4,1) 

//   // tree
//   val tree = new Tree
//   tree.minDistance = 0.01 //0.05
//   tree.maxDistance = 0.05 //0.35
//   tree.branchLength = 0.007 //0.03
//   tree.thresholdVel = 1.2 //0.1 //0.05
//   tree.thresholdVelMax = 10.75 //0.2 //0.05
//   tree.trimDistance = 0.1
//   tree.sleep = 100

//   val treeMesh = Mesh()
//   treeMesh.primitive = Points
//   treeMesh.maxVertices = 1000000
//   val treeModel = Model(treeMesh)
//   treeModel.material = Material.basic
//   treeModel.material.color = RGBA(1,1,1,1)

//   val falling = HashMap[Stick, Model]()
//   Gravity.set(0,-.1,0)

//   var inited = false
//   var running = false

//   // RenderNodes
//   var node:RenderNode = _
//   var blur:BlurNode = _
//   var composite:Composite3Node = _
//   var noise:NoiseNode = _
//   var rd:RDNode = _
//   var colorize:ColorizeNode = _

//   Keyboard.bind(" ", () => start() )

//   override def init(){
//     // node = new RenderNode
//     // node.renderer.camera = new OrthographicCamera(1,1)
//     // node.renderer.camera.nav.pos.z = 2
//     // node.renderer.shader = RootNode.renderer.shader

//     blur = new BlurNode
//     composite = new Composite3Node
//     composite.blend0 = 1f
//     composite.blend1 = 1f
//     composite.blend2 = 1f
//     noise = new NoiseNode
//     rd = new RDNode 
//     colorize = new ColorizeNode
//     colorize.color1 = RGBA(0,0,0,0)
//     colorize.color2 = RGBA(1,1,1,.3f)
//     colorize.color3 = RGBA(1,1,1,.4f)
//     colorize.color4 = RGBA(1,1,1,.5f)
//     colorize.color5 = RGBA(0,0,0,.6f)

//     RootNode.outputTo(blur)
//     blur.outputTo(composite)
//     // blur.outputTo(rd)

//     // RenderGraph.addNode(rd)
//     // rd.outputTo(colorize)
//     // colorize.outputTo(composite)

//     // RenderGraph.addNode(noise)
//     // noise.outputTo(composite)

//     // RenderGraph.clear

//     inited = true
//   }
  
//   override def draw(){
//     FPS.print

//     Sphere().draw

//     // node.render 
//     // blur.render
//     // noise.render
//     // for( i <- 0 until 5) rd.render 
//     // colorize.render 
//     // noise.render 
//     // composite.render


//   }
  
//   override def animate(dt:Float){
//     if(!inited) init()

//     skeletons.foreach{ case s =>
//       if(s.tracking) s.updateJoints()
//       //TODO
//     }
    
//     blur.size = 0.01
//     blur.intensity = math.abs( 2*math.sin(Time())) + 0.1

//     // try{

//     //   mesh.clear
//     //   mesh.vertices ++= OpenNI.pointMesh.vertices.map( (v) => { val p = v*1000; p.z *= -1; KPC.worldToScreen(p)})
//     //   val index = Random.int(mesh.vertices.length)
//     //   mesh.indices ++= (0 until numIndices).map( _ => index() )
//     //   mesh.update

//     // } catch { case e:Exception => println(e) }

//   }

//   def start(){
//     // if(running) return
//     Schedule.over( 5 seconds){ case t => composite.blend0 = t; if(t>=1) running = false }
//     running = true
//   }
// }


// class NoiseNode extends RenderNode {

//   renderer.scene.push(Plane())

//   renderer.shader = Shader.load(
//     """
//     attribute vec4 a_position;
//     attribute vec4 a_normal;
//     attribute vec2 a_texCoord0;
//     attribute vec4 a_color;

//     uniform mat4 u_projectionViewMatrix;
//     varying vec2 v_uv;

//     void main() {
//       gl_Position = u_projectionViewMatrix * a_position;
//       v_uv = a_texCoord0;
//     }
//     """,

//     """
//     #ifdef GL_ES
//         precision mediump float;
//     #endif

//     varying vec2 v_uv;
//     uniform float time;

//     uniform sampler2D u_texture0;

//     float snoise(in vec2 co){
//       return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
//     }
//     void main(){
//       vec4 color = texture2D(u_texture0, v_uv);
//       float n = snoise(vec2(v_uv.x*cos(time),v_uv.y*sin(time))); 
//       gl_FragColor = vec4(n, n, n, 1.0 ) + color;
//     }
//     """
//   )
//   override def render() = {
//     renderer.shader.uniforms("time") = Time()
//     super.render()
//   }
// }

