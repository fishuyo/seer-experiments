
import com.fishuyo.seer.openni._
import com.fishuyo.seer.particle._
import com.fishuyo.seer.audio._
import com.fishuyo.seer.video._
import scala.math._

import java.nio.ByteBuffer


object CalibTest extends SeerScript {

  var smodel = collection.mutable.ListBuffer[Model]()

  var initd = false

  OpenNI.initAll()
  OpenNI.start()
  KPC.loadCalibration("calibration.txt")
  val skeletons = (1 to 4).map(OpenNI.getSkeleton(_))


  override def init(){
    RootNode.renderer.camera = new OrthographicCamera(1,1)
    // RootNode.renderer.camera.nav.pos.z = 2

    smodel += Sphere().scale(0.01)
    smodel += Sphere().scale(0.01)
    smodel += Sphere().scale(0.1)
    smodel += Sphere().scale(0.01)
    smodel += Sphere().scale(0.01)
    smodel += Sphere().scale(0.1)
    smodel += Sphere().scale(0.01)
    smodel += Sphere().scale(0.01)
    smodel += Sphere().scale(0.1)
    smodel += Sphere().scale(0.01)
    smodel += Sphere().scale(0.01)
    smodel += Sphere().scale(0.1)

    initd = true
  }

  override def draw(){
    FPS.print

    smodel.foreach( _.draw )
  }

  override def animate(dt:Float){
    if(!initd) init()

    for( i <- 0 until skeletons.length){
      if(skeletons(i).tracking){
        skeletons(i).updateJoints()
        val pos = skeletons(i).joints("l_hand") * 1000
        pos.z *= -1
        val pos2 = skeletons(i).joints("r_hand") * 1000
        pos2.z *= -1
        val pos3 = skeletons(i).joints("torso") * 1000
        pos3.z *= -1
        val cam = RootNode.renderer.camera
        val out = KPC.worldToScreen(pos) * Vec3(cam.viewportWidth*0.9, cam.viewportHeight,0) + Vec3(0.15,-0.05,0)
        val out2 = KPC.worldToScreen(pos2) * Vec3(cam.viewportWidth*0.9, cam.viewportHeight,0) + Vec3(0.15,-0.05,0)
        val out3 = KPC.worldToScreen(pos3) * Vec3(cam.viewportWidth*0.9, cam.viewportHeight,0) + Vec3(0.15,-0.05,0)
        smodel(3*i).pose.pos.set(out) //lerpTo(out,0.1f)
        smodel(3*i+1).pose.pos.set(out2) //lerpTo(out2,0.1f)
        smodel(3*i+2).pose.pos.set(out3)
      }
    }

  }

}


CalibTest
