
package com.fishuyo.seer 
package tuningtree

import graphics._
import spatial._

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.{Texture => GdxTexture}

class Spectrogram extends Drawable {

	var numWin = 0
	var numBins = 0

	var pix:Pixmap = null
	var texture:GdxTexture = null

	var tID = 0
	val model = Plane()
	model.scale.set(0.25)
	model.material = new BasicMaterial()
	model.material.textureMix = 1f

	def setData(data:Array[Array[Float]], complex:Boolean=false){
		numWin = data.length
		numBins = data(0).length / 2

		pix = new Pixmap(numWin,numBins, Pixmap.Format.RGBA8888)
		pix.setColor(1,1,1,1)
		// pix.fill()
		for( x <- (0 until numWin); y <- (0 until numBins)){
			val (re,im) = (data(x)(2*y),data(x)(2*y+1)) // re/im or mag/phase
			val mag = (if(complex) math.sqrt(re*re+im*im) else re)
			val c = math.max(1.0 - mag,0f)
			// val c = clamp(1f-(10*math.log10(mag)),0f,1f)
			// val f = math.min((im / 22050f * numBins),numBins-1)
			pix.setColor(c,c,c,c)
			if(complex) pix.drawPixel(x,y) //f.toInt)
			else pix.drawPixel(x,y) //f.toInt)
		}

		texture = new GdxTexture(pix)
		texture.setFilter( GdxTexture.TextureFilter.Linear, GdxTexture.TextureFilter.Linear)
		model.material.texture = Some(texture)
	}

  override def init(){
  }
	override def draw(){
		model.draw()
	}

}