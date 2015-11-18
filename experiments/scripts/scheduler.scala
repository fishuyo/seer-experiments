
import com.fishuyo.seer._
import graphics._
import dynamic._
import maths._
import io._
import util._

import collection.mutable.ListBuffer

import concurrent.duration._

object Script extends SeerScript {

	// val s = Plane()
	// s.material = Material.basic

	var c = 0.0f
	
	Schedule.every(100 millis){
		val s = Sphere().scale(0.05f)
		s.material = Material.specular
		s.material.color = HSV(Random.float(),0.7,0.7)
		Scene.push(s)
		val pos = Vec3(-1,1,-1) //Random.vec3()
		val dest = Random.vec3()
		Schedule.over(1 second){
			case f if f >= 1.0f => Scene.remove(s)
			case f => s.pose.pos = pos.lerp(dest,f)
		}
	}
	// Schedule.after(1 second){ println("castle"); Schedule.after(1 second){println("salad"); Schedule.every(1 second){println("sucks")}}}
	override def draw(){

	}

	override def animate(dt:Float){

	}

	override def preUnload(){
		Schedule.clear
	}

}
Script
