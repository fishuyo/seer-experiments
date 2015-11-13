
import com.fishuyo.seer.openni._

import com.badlogic.gdx.graphics.{Texture => GdxTexture}
import com.badlogic.gdx.graphics.Pixmap

object Script extends SeerScript {

  var inited = false

  OpenNI.initAll()
  OpenNI.start()
  OpenNI.pointCloud = true

  val stickman = new StickMan(OpenNI.getSkeleton(1))

	val quad1 = Plane().scale(1,-480f/640f,1).translate(-1.5,0,0)
	val quad2 = Plane().scale(1,-480f/640f,1).translate(1,0,0)
  val dpix = new Pixmap(640,480, Pixmap.Format.RGB888)
  val vpix = new Pixmap(640,480, Pixmap.Format.RGB888)
  var tex1:GdxTexture = _
  var tex2:GdxTexture = _

  val mesh = new Mesh()
  mesh.primitive = Points 
  mesh.maxVertices = 640*480
  val model = Model(mesh)

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

		// quad1.draw
		// quad2.draw

    model.draw

	}

	override def animate(dt:Float){
		if(!inited) init()

    stickman.animate(dt)
    
  // 	val bb = dpix.getPixels
		// bb.put(OpenNI.depthBytes)
		// bb.rewind
		// tex1.draw(dpix,0,0)

		// val bb2 = vpix.getPixels
		// bb2.put(OpenNI.rgbBuffer)
		// bb2.rewind
		// tex2.draw(vpix,0,0)

    try{
      // OpenNI.updatePoints()
      mesh.clear
      mesh.vertices ++= OpenNI.pointMesh.vertices
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

}




Script