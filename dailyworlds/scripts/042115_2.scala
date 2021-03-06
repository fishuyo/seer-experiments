

// fractal terrain, glow trace..

// next do camera cross fade test
// thinking about how to build this into timeline performance event api

// import com.badlogic.gdx.Gdx.{gl20 => gl }

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL43

object Script extends SeerScript {

  Renderer().environment.backgroundColor.set(0,0,0,1)
  Renderer().environment.alpha = 0.1
  Renderer().environment.blend = false
  Renderer().environment.depth = true
  // Renderer().environment.blendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)


  val (tx,ty) = (129,129)

  // val terrain = Sphere.generateMesh(10f, tx)
  val terrain = Plane.generateMesh(50f,50f,tx,ty,Quat.up)
  // terrain.primitive = Lines

  fractalize(terrain,tx,ty)
  var dirty = true

  val model = Model(terrain)
  model.material = Material.specular
  // model.material.color = RGBA(0.01,0.01,0.01,0.1)
  // model.material.color = RGBA(0.5,0.5,0.5,0.1)
  model.material.color = RGBA(1,1,1,1)

  val model2 = Model(terrain).translate(0,0.01,0)
  model2.material = Material.basic
  model2.material.color = RGBA(0,0,0,1)

  val sphere = Sphere()
  sphere.material = Material.specular
  sphere.material.color = RGBA(0.4,0,0.6,0.5)

  override def draw(){

    if(dirty){
      dirty = false
      terrain.recalculateNormals
      terrain.update
    }

    // GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST);
    // GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    // GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
    // GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
    // GL11.glEnable(GL11.GL_LINE_SMOOTH);
    // GL11.glEnable(GL11.GL_POINT_SMOOTH);

    // terrain.primitive = Triangles
    GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL)
    model.draw

    GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE)
    model2.draw

    // terrain.primitive = Lines
    // model2.draw
    // sphere.draw
  }

  def fractalize(m:Mesh, nx:Int, ny:Int) = {

    val roughness = 0.05f

    divide(nx-1)

    def divide(size:Int){
      val half = size/2
      var scale = roughness * size
      if(half < 1) return
      for(y <- half until (ny) by size; x <- half until (nx) by size)
        square(x,y,half, Random.float() * scale * 2 - scale)

      for(y <- 0 until (ny) by half; x <- ((y+half)%size) until (nx) by size)
        diamond(x,y,half, Random.float() * scale * 2 - scale)
      
      divide(size/2)
    }

    def indx(i:Int,j:Int ) = {
      var x = i; var y = j;
      while( x < 0 ) x += nx; while( x >= nx ) x -= nx;
      while( y < 0 ) y += ny; while( y >= ny ) y -= ny;
      nx*y + x
    }

    def square(x:Int, y:Int, size:Int, offset:Float){
      val avg = Vec3()
      avg += m.vertices(indx(x-size, y-size))
      avg += m.vertices(indx(x+size, y-size))
      avg += m.vertices(indx(x+size, y+size))
      avg += m.vertices(indx(x-size, y+size))
      avg /= 4f

      m.vertices(y*nx+x).y = avg.y + offset
      // m.vertices(y*nx+x) = avg * offset
    }

    def diamond(x:Int, y:Int, size:Int, offset:Float){
      val avg = Vec3()
      avg += m.vertices(indx(x, y-size))
      avg += m.vertices(indx(x+size, y))
      avg += m.vertices(indx(x, y+size))
      avg += m.vertices(indx(x-size, y))
      avg /= 4f

      m.vertices(y*nx+x).y = avg.y + offset
      // m.vertices(y*nx+x) = avg * offset

    }
  }

  // io.VRPN.clear
  // io.VRPN.bind("thing", (p:Pose) => {
  //   println(p.pos)
  //   Camera.nav.quat.slerpTo(p.quat,0.2)
  // })
}



Script