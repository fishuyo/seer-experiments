package com.fishuyo
package sphere

import graphics._
import io._
import dynamic._
import audio._
import maths._
import spatial._

import scala.collection.mutable.ListBuffer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.math.Matrix4


object Main extends App with GLAnimatable{

  SimpleAppRun.loadLibs()
	GLScene.push(this)

  val live = new Ruby("sphere/sphere.rb")

  var origin = new Model(Pose(), Vec3(.1f)).add(Sphere())

  var cams = Primitive3D.cube(Pose(), Vec3(1.0f))
  var cameras = ListBuffer[GLPrimitive]()
 	var cameraPoses = ListBuffer[Pose]()

 	val vecMeshs = new Array[Mesh](14)
  val vecVerts = new Array[Array[Float]](14)
  val vecRay = new Array[GLPrimitive](14)
  for( i<-(0 until 14)){
    vecMeshs(i) = new Mesh(false,(4+16),0,VertexAttribute.Position)
    vecVerts(i) = new Array[Float]((4+16)*(3))
    vecRay(i) = Primitive3D.cylinder(Pose(),Vec3(.05,.05,5.0f),1.0f, 3.0f)

  }

  var fov = 45.0f

	OwlParser("sphere/record.owl").foreach{
		case p:Pose => 
      cameraPoses += Pose(p)
			val i = cameras.length
			val c = Primitive3D.cube(p, Vec3(.108f,.057f,.092f))
			c.color = RGBA(1,1,1,1)
			cameras += c
			var uf = p.pos + p.uf()*3.0f
			var uu = p.pos + p.uu()
			var ur = p.pos + p.ur()
			vecVerts(i)(0) = p.pos.x
			vecVerts(i)(1) = p.pos.y
			vecVerts(i)(2) = p.pos.z
			vecVerts(i)(3) = uf.x
			vecVerts(i)(4) = uf.y
			vecVerts(i)(5) = uf.z
			vecVerts(i)(6) = p.pos.x
			vecVerts(i)(7) = p.pos.y
			vecVerts(i)(8) = p.pos.z
			vecVerts(i)(9) = uu.x
			vecVerts(i)(10) = uu.y
			vecVerts(i)(11) = uu.z
      vecRay(i).pose.set(p)
      vecRay(i).pose.quat *= Quat().fromEuler(Vec3(0,180.0f.toRadians,0))

      var b1 = -(p.quat * Quat().fromEuler(Vec3(0,fov.toRadians,0))).toZ * 1.0f
      var b2 = -(p.quat * Quat().fromEuler(Vec3(fov.toRadians,0,0))).toZ * 1.0f
      var b3 = -(p.quat * Quat().fromEuler(Vec3(0,-fov.toRadians,0))).toZ * 1.0f
      var b4 = -(p.quat * Quat().fromEuler(Vec3(-fov.toRadians,0,0))).toZ * 1.0f
      vecVerts(i)(12) = p.pos.x
      vecVerts(i)(13) = p.pos.y
      vecVerts(i)(14) = p.pos.z
      vecVerts(i)(15) = b1.x
      vecVerts(i)(16) = b1.y
      vecVerts(i)(17) = b1.z
      vecVerts(i)(18) = p.pos.x
      vecVerts(i)(19) = p.pos.y
      vecVerts(i)(20) = p.pos.z
      vecVerts(i)(21) = b2.x
      vecVerts(i)(22) = b2.y
      vecVerts(i)(23) = b2.z
      vecVerts(i)(24) = p.pos.x
      vecVerts(i)(25) = p.pos.y
      vecVerts(i)(26) = p.pos.z
      vecVerts(i)(27) = b3.x
      vecVerts(i)(28) = b3.y
      vecVerts(i)(29) = b3.z
      vecVerts(i)(30) = p.pos.x
      vecVerts(i)(31) = p.pos.y
      vecVerts(i)(32) = p.pos.z
      vecVerts(i)(33) = b4.x
      vecVerts(i)(34) = b4.y
      vecVerts(i)(35) = b4.z

      val a = vecVerts(i)
      vecMeshs(i).setVertices(a)

			ur *= 1000
			uu *= 1000
			uf *= 1000

			println( ur.x + " " + ur.y + " " + ur.z)
			println( uu.x + " " + uu.y + " " + uu.z)
			println( uf.x + " " + uf.y + " " + uf.z)
	}

  var load = false

  //var sphere = ObjParser("src/main/scala/sphere/sphere_n.obj")//00_AlloSphere_Screen.obj","triangles",false ) 
  //var rails = ObjParser("src/main/scala/sphere/03_AlloSphere_Bridge_Sturct_Rails.obj","triangles",false)
  //var rails:StillModel = _
  var sphere:Model = _
  // var spherePose = Pose(Vec3(0,-5.619f,0), Quat().fromEuler(Vec3(0,math.Pi/2,0)))
  // var sphereScale = Vec3(0.0254f) // scale to meters


  SimpleAppRun()  

  override def step(dt:Float){
    // cameraPoses.zipWithIndex.foreach{
    //   case (c,i) => c.pose.quat = cameras(i).quat * cams.pose.quat
    // }
    live.step(dt)
  }

  override def draw(){
  	origin.draw()
  	if( !load ){
       val obj = Gdx.files.internal("sphere/sphere_n.obj")//00_AlloSphere_Screen.obj")
  		 // val obj = Gdx.files.internal("src/main/scala/drone/landscapealien.obj")//00_AlloSphere_Screen.obj")
  		// val obj2 = Gdx.files.internal("src/main/scala/sphere/03_AlloSphere_Bridge_Sturct_Rails.obj")
  		sphere = OBJ("sphere/sphere_n.obj")
  		// for( i<-(0 until sphere.subMeshes.length )) sphere.subMeshes(i).primitiveType = GL10.GL_LINES
  		// rails = new ObjLoader().loadObj(obj2)
  		// for( i<-(0 until rails.subMeshes.length )) rails.subMeshes(i).primitiveType = GL10.GL_LINES
      sphere.pose.set(Pose(Vec3(0,-5.619f,0), Quat().fromEuler(Vec3(0,math.Pi/2,0))))
      sphere.scale.set(Vec3(0.0254f)) // scale to meters

  		load = true
  	}
 
    sphere.draw()

  	//cams.draw()

    MatrixStack.clear()
    MatrixStack.transform(cams.pose,cams.scale)
    Shader.setMatrices()
  	cameras.foreach( _.draw() )
    vecRay.foreach( _.draw() )
  	Shader.setMatrices()


    // for( i<-(0 until vecMeshs.length)){
    //   if( i <= 4) Shader.setColor( Vec3(i/4.0f,1.0f,1.0f), 1.0f)
    //   else if( i <= 9) Shader.setColor( Vec3(1.0f,(i-4)/5.0f,1.0f), 1.0f)
    //   else Shader.setColor( Vec3(1.0f,1.0f,(i-8)/5.0f), 1.0f)
    //   vecMeshs(i).render(Shader(), GL10.GL_LINES)
    // }

  }


}




import scala.io.Source
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.combinator.JavaTokenParsers

abstract trait Statement
case class Camera(x:Float,y:Float,z:Float,qw:Float,qx:Float,qy:Float,qz:Float) extends Statement
case class Empty() extends Statement

object OwlParser extends JavaTokenParsers {
 
  def com = """#\.*""".r ^^ { case c => Empty() }
  def c = floatingPointNumber~floatingPointNumber~floatingPointNumber~floatingPointNumber~floatingPointNumber~floatingPointNumber~floatingPointNumber ^^ { case x~y~z~qw~qx~qy~qz => Camera(x.toFloat,y.toFloat,z.toFloat,qw.toFloat,qx.toFloat,qy.toFloat,qz.toFloat) }  

  def statement : Parser[Statement] = c | com
  def file : Parser[List[Statement]] = statement*


  def parseLine( line: String ){
    println ( parse( statement, line ))
  }

  def apply( filename: String ) : List[Pose] = {

    var list = List[Pose]()
    val file = Source.fromFile( filename )

    file.getLines.foreach( (l) => {
      parse( statement, l.trim ) match {
        case Success( s, _ ) => s match {
          case Camera(x,y,z,qw,qx,qy,qz) => list = Pose(Vec3(x/1000,y/1000,z/1000),Quat(qw,qx,qy,qz)) :: list
          case _ => None
        }
        case x => println( x )
      }
    })
    file.close

		list
  }
}



