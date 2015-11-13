
// import com.fishuyo.seer._
import com.fishuyo.seer.kodama._
import com.fishuyo.seer.openni._
// import graphics._
// import dynamic._
// import spatial._
// import io._
// import cv._
// import video._
// import util._
// import kinect._
// import actor._

// import trees._
// import particle._
// import structures._

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

import akka.actor._
import akka.event.Logging

import de.sciss.osc.Message

import concurrent.duration._

// import openni._
 

///
/// Trees
///
var idx = 0

class ATree(b:Int=8) extends Tree {
  var visible = 0
  var (mx,my,mz,rx,ry,rz) = (0f,0f,0f,0f,0f,0f)
  setAnimate(true)
  setReseed(true)
  setDepth(b)
  branch(b)

  def update(){ update(mz,rx,ry,rz)}

  override def draw(){ if(visible==1) super.draw() }
  override def animate(dt:Float){ if(visible==1) super.animate(dt) }
}


object SaveTheTrees {
  def save(name:String){
    var project = "tree-" + (new java.util.Date()).toLocaleString().replace(' ','-').replace(':','-') + ".json"
    if( name != "") project = name
    var path = "TreeData/" + project
    var file = Gdx.files.internal("TreeData/").file()
    file.mkdirs()
    file = Gdx.files.internal(path).file()

    var map = Map[String,Any]()
    Script.trees.zipWithIndex.foreach { case(t,i) =>
      map = map + (("t"+i) -> List(t.mx,t.my,t.mz,t.rx,t.ry,t.rz,t.visible,t.seed))
    }

    val p = new java.io.PrintWriter(file)
    p.write( scala.util.parsing.json.JSONObject(map).toString( (o) =>{
      o match {
        case o:List[Any] => s"""[${o.mkString(",")}]"""
        case s:String => s"""${'"'}${s}${'"'}"""
        case a:Any => a.toString()  
      }
    }))
    p.close
  }

  def load(name:String){
    val path = "TreeData/" + name
    val file = Gdx.files.internal(path).file()

    val sfile = scala.io.Source.fromFile(file)
    val json_string = sfile.getLines.mkString
    sfile.close

    val parsed = scala.util.parsing.json.JSON.parseFull(json_string)
    if( parsed.isEmpty ){
      println(s"failed to parse: $path")
      return
    }

    Script.trees.zipWithIndex.foreach { case(t,i) =>
      val map = parsed.get.asInstanceOf[Map[String,Any]]
      val l = map("t"+i).asInstanceOf[List[Double]]

      t.mx = l(0).toFloat
      t.my = l(1).toFloat
      t.mz = l(2).toFloat
      t.rx = l(3).toFloat
      t.ry = l(4).toFloat
      t.rz = l(5).toFloat
      t.visible = l(6).toInt
      t.seed = l(7).toLong
      t.root.pose.pos.set(t.mx,t.my,0)
      t.update(t.mz,t.rx,t.ry,t.rz)
    }
  }
}

class SkeletonBuffer(val size:Int) extends Animatable {
  val buffer = new Array[Skeleton](size)
  var rpos = 0f
  var len = 0
  var wpos = 0
  var frame = new Skeleton(0)

  var playing = true
  var recording = false

  def togglePlay(){ playing = !playing }
  def toggleRecord(){ recording = !recording }
  def clear(){ rpos = 0; wpos = 0; len = 0 }
  def +=(s:Skeleton){
    if(recording){
      val skel = new Skeleton(0) //QuadMan(0)
      skel.tracking = true
      skel.joints = s.joints.clone
      buffer(wpos) = skel
      if(len < size) len += 1
      wpos += 1
      if( wpos >= size) wpos = 0 
    }
  }
  override def draw(){
    if(playing){
      // frame.draw
      // println(frame.joints("head"))
    }
  }
  override def animate(dt:Float){
    if(playing && len > 0){
      frame = buffer(rpos.toInt)
      // frame.animate(dt)
      rpos += dt
      if( rpos >= len) rpos = 0
    }
  }
}


object Script extends SeerScript {

  var dirty = true
  var update = false

  var receiver:ActorRef = _

  var trees = ListBuffer[ATree]()
  trees += new ATree(9)
  trees += new ATree(8)
  trees += new ATree(7)
  trees += new ATree(7)
  val tree = trees(0)
  tree.visible = 1

  // tree.root.position.set(0,-2,-4)
  tree.root.pose.pos.set(0,-2,-4)

 //  OpenNI.connect()
  // val context = OpenNI.context

  // println(OpenNI.depthMD.getFullXRes)

  val skeletons = ArrayBuffer[Skeleton]()
  for( i <- 0 until 4) skeletons += new Skeleton(i) //QuadMan(i)

  val buffers = ArrayBuffer[SkeletonBuffer]()
  for( i <- 0 until 4) buffers += new SkeletonBuffer(500)


  override def preUnload(){
    recv.clear()
    recv.disconnect()
    // SceneGraph.root.outputs.clear
    // ScreenNode.inputs.clear
  }

  override def onLoad(){
  }

  // def loadShaders(){
  //   Shader.load("s1", File("shaders/basic.vert"), File("shaders/skel.frag")).monitor
  //   Shader.load("t", File("shaders/basic.vert"), File("shaders/tree.frag")).monitor
  // }

  var inited = false
  var feedback:RenderNode = null
  override def init(){
    // loadShaders()
    // TreeNode.model.shader = "t"
    inited = true

    // SceneGraph.root.outputs.clear
    // ScreenNode.inputs.clear

    // feedback = new RenderNode
    // feedback.shader = "composite"
    // feedback.clear = false
    // feedback.scene.push(Plane())
    // SceneGraph.root.outputTo(feedback)
    // feedback.outputTo(feedback)
    // feedback.outputTo(ScreenNode)
  }

  override def draw(){
    FPS.print


    trees.foreach(_.draw)
  }

  var t = 0f
  override def animate(dt:Float){

    if(!inited) init()

    trees.foreach(_.animate(dt))

  }

  // Schedule.clear
  // Schedule.every(1 seconds){
  //   if( Random.float() < .5){
  //     val i = Random.int(0,3)()
  //     buffers(i).togglePlay
  //   }
  // }

  // input events
  Keyboard.clear()
  Keyboard.use()
  Keyboard.bind("t", ()=>{SaveTheTrees.save("t.json")})
  Keyboard.bind("r", ()=>{SaveTheTrees.load("t.json")})
  Keyboard.bind("1", ()=>{idx=0;})
  Keyboard.bind("2", ()=>{idx=1;})
  Keyboard.bind("3", ()=>{idx=2;})
  Keyboard.bind("4", ()=>{idx=3;})
  Keyboard.bind("0", ()=>{idx= -1;})
  Keyboard.bind("p", ()=>{buffers(1).togglePlay})
  Keyboard.bind("o", ()=>{buffers(1).toggleRecord})


  Trackpad.clear
  Trackpad.connect
  Trackpad.bind( (touch) => {

    val t = trees(idx)
    t.visible = 1

    val p = touch.pos 
    val v = touch.vel

    touch.count match {
      case 1 =>
        val ur = Vec3(1,0,0) //Camera.nav.ur()
        val uf = Vec3(0,0,1) //Camera.nav.uf()

        trees.foreach{ case t =>
          t.root.applyForce( ur*(p.x-0.5) * 2.0*touch.size )
          t.root.applyForce( uf*(p.y-0.5) * -2.0*touch.size )
        }
      case 2 =>
        t.mx += v.x*0.05  
        t.my += v.y*0.05
      case 3 =>
        t.ry += v.x*0.05  
        t.mz += v.y*0.01
        if (t.mz < 0.08) t.mz = 0.08
        if (t.mz > 3.0) t.mz = 3.0 
      case 4 =>
        t.rz += v.y*0.05
        t.rx += v.x*0.05
      case _ => ()
    }

    t.root.pose.pos.set(t.mx,t.my,0)

    if(touch.count > 2){
      t.update(t.mz,t.rx,t.ry,t.rz) 

    }
  })


  val recv = new OSCRecv
  recv.listen(7110)
  // recv.bindp {
  //   case Message(regex(idS,name),x:Float,y:Float,z:Float) =>
  //     val id = idS.toInt - 1
  //     skeletons(id).joints(name).lerpTo(Vec3(x,y,z), 0.5f)
  //     skeletons(id).tracking = true
  //   case Message("/calibrating", id:Int) => 
  //     skeletons(id-1).calibrating = true
  //   case Message("/tracking", id:Int) => 
  //     skeletons(id-1).calibrating = false
  //     skeletons(id-1).tracking = true
  //   case Message("/lost", id:Int) => 
  //     skeletons(id-1).calibrating = false
  //     skeletons(id-1).tracking = false
  //   case _ => ()
  // }
  recv.bindp {
    case Message("/joint", name:String, id:Int, x:Float, y:Float, z:Float) =>
      // val zz = map(z,0,7,.1,-.1)
      // val pos = Vec3(-2*x+1,1-y,zz) + Vec3(1,-1,0)
      val pos = Vec3(-2*x+1,1-y,z)
      if(id > 3){} else{
        skeletons(id).updateJoint(name,pos)
        // if(name == "l_hand") println(skeletons(id).vel("l_hand").mag)
        skeletons(id).tracking = true
        buffers(id).toggleRecord
      }
    case Message("/lost_user", id:Int) =>
      if(id > 3){} else{
        buffers(id).toggleRecord
        skeletons(id).tracking = false;
        skeletons(id).calibrating = false;
      }

    case m => ()
  }

}









// must return this from script
Script