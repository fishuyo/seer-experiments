
package com.fishuyo.seer
package morea

import graphics._
import spatial._
import dynamic._
import util._

import ij.ImagePlus
import ij.io._
import loci.plugins.BF

object TerrainTest extends SeerApp {

	// val path = "data/Elev_1mcont_idw_36-2.tif"
	val path = "/Users/fishuyo/Desktop/moorea/scaled/elev_scaled_07.jpg"

	// val opener = new Opener()
	// val imp = opener.openImage(path)
	// val sp = (imp.getProcessor()).convertToShort(false)

	var live:SeerScriptLoader = _

	var imp:Array[ImagePlus] = _

	override def init(){
		imp = BF.openImagePlus(path)

		live = new SeerScriptLoader("scripts/test.scala")
	}

}