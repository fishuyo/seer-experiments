

package com.fishuyo.seer
package examples.audio

import audio._

object Main extends App {

	// initialize PortAudio driver, experimental
	PortAudio.init()

	Audio().start

	val s = new Sine( 440, 0.5f)

	Audio().push(s)

	var t = 0
	while(t < 5){ Thread.sleep(1000); t += 1}
}