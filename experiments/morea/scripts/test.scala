
import com.fishuyo.seer._
import morea._

import graphics._
import spatial._
import dynamic._
import util._

import collection.mutable.ArrayBuffer

object Script extends SeerScript {

	val img = TerrainTest.imp(0)
	val w = img.getWidth
	val h = img.getHeight

	val mw = 256
	val mh = 256

	val meshGrid = Array.ofDim[Mesh](5,5)
	val models = ArrayBuffer[Model]()

	for(j <- 0 until 5; i <- 0 until 5){
		meshGrid(j)(i) = Plane.generateMesh(10.0f,10.0f,mw,mh, Quat.up)
		models += Model(meshGrid(j)(i)).translate(10*i,0,-10*j)
	}

	models.foreach(_.material = Material.specular)

	val mesh = Plane.generateMesh(10.0f,10.0f,mw,mh, Quat.up)

	val model = Model(mesh)
	model.material = Material.specular

	var inited = false


// 0 1 2 3 4 5 6




// 0 1 2 3 4 5 6 7 8 9 10 11
// 0 0 0 0     2 2 2 2
//       1 1 1 1     3 3  3  3
// 0 1 2 3     0 1 2 3
//       0 1 2 3     0 1  2  3
// 

	def initd(){
		inited = true

		println("hi")
		for(j <- 0 until 5; i <- 0 until 5){
			val m = meshGrid(j)(i)
			val mx = i*(mw-1)
			val my = j*(mh-1)
			for( x <- mx until mx+mw; y <- my until my+mh){
				val pix = img.getPixel(x,y)
				val h = pix(0)
				val idx = (y-my)*mw + (x-mx)
				meshGrid(j)(i).vertices(idx).y = h/255.0f * 5.0f
			}
		}

		// for( x <- 0 until w; y <- 0 until h){
		// 	val pix = img.getPixel(x,y)
		// 	val h = pix(0)

		// 	val mx = x % (mw)
		// 	val my = y % (mh)
		// 	val mi = x / mw
		// 	val mj = y / mh
		// 	val i = (my)*mw + (mx)

		// 	if( mx == 0 && mi != 0 ){
		// 		meshGrid(mj)(mi-1).vertices(i).y = h/255.0f * 5.0f
		// 	}
		// 	if( my == 0 ){}

		// 	// println(s"$mx $my $mi $mj")

		// 	meshGrid(mj)(mi).vertices(i).y = h/255.0f * 5.0f

		// 	// println(s"${pix(0)} ${pix(1)} ${pix(2)} ${pix(3)}")
		// 	// mesh.vertices(i).y = h
		// }

		// val hs = mesh.vertices.map( _.y )
		// val max = hs.max
		// val min = hs.min
		// println(min + " " + max)
		// mesh.vertices.foreach( (v) => v.y = map(v.y,min,max,0,5))

		// mesh.vertices(0).y = 1.0f
		// mesh.vertices(mw-1).y = 1.0f
		// mesh.vertices((mh*(mw-1))).y = 1.0f
		// mesh.vertices((mh*(mw))-1).y = 1.0f

		for(j <- 0 until 5; i <- 0 until 5){
			meshGrid(j)(i).recalculateNormals
			meshGrid(j)(i).update
		}
		mesh.recalculateNormals
		mesh.update
		println("update.")
	}

	override def draw(){ 
		if(!inited) initd()

		// FPS.print
		// model.draw
		models.foreach(_.draw)
	}

}

Script