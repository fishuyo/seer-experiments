
package com.fishuyo.seer
package examples.audio

import graphics._
import audio._
import util._
import io._

object FMSynth extends SeerApp {

  // initialize the GdxAudio driver
  GdxAudio.init

  // start the audio actor
  Audio().start

  var t = 0.f

  

  override def animate(dt:Float){
    t += dt 

   
  }

  Trackpad.connect
  Trackpad.bind((touch) => {
    touch.count match{
      case 1 =>
      case _ => ()
    }
  })

}