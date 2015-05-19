
import com.fishuyo.seer.tuningtree._

object Script extends SeerScript {


  val tree = new ATree(10)

  // GdxAudio.init
  // Audio().start


  override def draw() = tree.draw()
  override def animate(dt:Float) = tree.animate(dt)


  Trackpad.clear
  Trackpad.connect
  Trackpad.bind( (tp) => {

    // var t = new ATree
    // if( idx >= 0){ 
      val t = tree
      t.visible = 1
    // }

    tp.count match {
      case 1 =>
        val ur = Vec3(1,0,0) //Camera.nav.ur()
        val uf = Vec3(0,0,1) //Camera.nav.uf()

        
          t.root.applyForce( ur*(tp.pos.x-0.5) * 2.0*tp.size )
          t.root.applyForce( uf*(tp.pos.y-0.5) * -2.0*tp.size )
      case 2 =>
        t.mx += tp.vel.x*0.05  
        t.my += tp.vel.y*0.05
      case 3 =>
        t.ry += tp.vel.x*0.05  
        t.mz += tp.vel.y*0.01
        if (t.mz < 0.08) t.mz = 0.08
        if (t.mz > 3.0) t.mz = 3.0 
      case 4 =>
        t.rz += tp.vel.y*0.05
        t.rx += tp.vel.x*0.05
      case _ => ()
    }

    // t.root.pose.pos.set(t.mx,t.my,0)

    if(tp.count > 2){
      t.update(t.mz,t.rx,t.ry,t.rz) 

    }
  })

}


Script