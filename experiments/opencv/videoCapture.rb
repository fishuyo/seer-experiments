require 'java'

module M
  include_package "com.fishuyo.seer.io"
  include_package "com.fishuyo.seer.maths"
  include_package "com.fishuyo.seer.graphics"
  include_package "com.fishuyo.seer.examples.opencv.cap"
end

class Object
  class << self
    alias :const_missing_old :const_missing
    def const_missing c
      M.const_get c
    end
  end
end


def animate dt
  # Main.bgsub.updateBackgroundNextFrame()
  Main.bgsub.setThreshold(50.0)
  # puts Main.player.queue.size
end