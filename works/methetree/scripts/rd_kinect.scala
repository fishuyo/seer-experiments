
// reaction diffusion ressurection

import com.fishuyo.seer.openni._


object Script extends SeerScript {

  val pos = Vec2()

  var node:RDNode = _
  var colorize:ColorizeNode = _
  var blur:BlurNode = _


  OpenNI.initAll()
  OpenNI.start()
  OpenNI.pointCloud = true
  OpenNI.pointCloudDensity = 2

  Renderer().camera = new OrthographicCamera(1,1)
  Renderer().camera.nav.pos.z = 2

  KPC.loadCalibration("../methetree/calibration.txt")

  val mesh = new Mesh()
  mesh.primitive = Points
  mesh.maxVertices = 640*480
  mesh.maxIndices = 10000
  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGBA(0.7,0.7,0.7,1)
  model.material.transparent = true
  var numIndices = 10000

  val skeleton = OpenNI.getSkeleton(1)

  var inited = false
  override def draw(){

    // Renderer().environment.pointSize = 4
    model.draw

    for(i <- 0 until 10) node.render
  }

  override def animate(dt:Float){
    if(!inited){
      node = new RDNode
      colorize = new ColorizeNode
      blur = new BlurNode
      RenderGraph.reset
      RootNode.outputTo(blur)
      blur.outputTo(node)
      // RenderGraph.addNode(node)
      ScreenNode.inputs.clear
      node.outputTo(colorize)

      colorize.color1 = RGBA(0,0,0,0)
      colorize.color2 = RGBA(1,1,1,.3f)
      colorize.color3 = RGBA(1,1,1,.4f)
      colorize.color4 = RGBA(1,1,1,.5f)
      colorize.color5 = RGBA(0,0,0,.6f)

      inited = true
    }
  
    blur.intensity = math.abs( 2*math.sin(Time()))
    // blur.size = 0.01

    node.renderer.shader.uniforms("brush") = Mouse.xy()
    node.renderer.shader.uniforms("width") = Window.width.toFloat
    node.renderer.shader.uniforms("height") = Window.height.toFloat
    node.renderer.shader.uniforms("feed") = 0.037 //62
    node.renderer.shader.uniforms("kill") = 0.06 //093
    node.renderer.shader.uniforms("dt") = dt
    // node.renderer.shader.uniforms("u_texture1") = 2

    try{
      mesh.clear
      mesh.vertices ++= OpenNI.pointMesh.vertices.map( (v) => { val p = v*1000; p.z *= -1; KPC.worldToScreen(p)})
      if(mesh.vertices.length > 0){
        // val index = Random.int(mesh.vertices.length)
        // mesh.indices ++= (0 until numIndices).map( _ => index() )
        mesh.update
      }

    } catch { case e:Exception => println(e) }


  }

  Trackpad.clear
  Trackpad.connect
  Trackpad.bind { case touch =>
    touch.count match {
      case 1 => 
      case 2 =>
        pos.x += touch.vel.x * 0.01
        pos.y -= touch.vel.y * 0.01
      case _ => ()
    }
  }

}



Script