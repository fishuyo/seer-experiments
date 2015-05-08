

// transparency test


import com.badlogic.gdx.graphics.GL20
// import com.badlogic.gdx.graphics.GL10
import com.badlogic.gdx.Gdx


object Script extends SeerScript {


  Renderer().environment.alpha = 1 //0.5
  Renderer().environment.blend = true
  Renderer().environment.depth = false
  Renderer().environment.blendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)


  val sphere1 = Sphere().translate(-0.25,0,0)
  sphere1.material = Material.basic
  sphere1.material.color = RGBA(0,0.3,0.6,0.5)

  val sphere2 = Sphere().translate(0.25,0,0)
  sphere2.material = Material.basic
  sphere2.material.color = RGBA(0.4,0,0.6,0.5)

  override def draw(){
    Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace(GL20.GL_BACK);

    sphere1.draw
    sphere2.draw
  }


}



Script