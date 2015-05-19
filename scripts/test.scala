

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.Gdx

object Script extends SeerScript {

  val mesh = new Mesh()
  mesh.primitive = Lines
  mesh.vertices ++= (0 until 1000).map( _ => Random.vec3() )

  val model = Model(mesh)
  model.material = Material.basic
  model.material.color = RGBA(1,1,1,0.1)

  override def draw(){
    Renderer().environment.blend = true

    Renderer().environment.depth = false
    Renderer().environment.alpha = 0.1
    Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE)


    model.draw
  }

  override def animate(dt:Float){
    // mesh.update
  }
}
Script