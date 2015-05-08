
// LSYSTEMSzz




import collection.mutable.Stack

class LSystem(val productions:PartialFunction[String,String]){
  
  var axiom = ""
  var generation = ""

  def apply(in:String) = {
    axiom = in
    generation = in
    this
  }

  def step(steps:Int) = {
    for( i <- 0 until steps){
      generation = generation.map( (c) => productions(c.toString) ).mkString
    }
    generation
  }
}

class TurtleGraphics {

  val mesh:Mesh = Mesh()
  mesh.primitive = Lines

  var turtle = new Nav()
  turtle.quat = Quat.down

  val stack = Stack[Pose]()

  var angles = Vec3(0.5,0.5,0.5)
  var scale = 1.0

  def run(word:Char) = word match {
     case '+' => angle(1)
     case '-' => angle(-1)
     case '<' | '\\' => roll(1)
     case '>' | '/' => roll(-1)
     case '&' => pitch(1)
     case '^' => pitch(-1)
     case '|' => flip()
     case 'f' | 'F' => draw(0.01)
     case 'g' => skip(0.01)
     case '[' => push()
     case ']' => pop()
     case _ => ()
  }

  def apply(sentence:String) = {
    mesh.clear()
    turtle = new Nav()
    turtle.quat = Quat.down

    sentence.foreach( (c) => {
      run(c)
    })
  }

  def angle(i:Float) = turtle.rotate(i*angles.x,0,0)
  def roll(i:Float) = turtle.rotate(0,i*angles.y,0)
  def pitch(i:Float) = turtle.rotate(0,0,i*angles.z)
  def flip() = turtle.rotate(180.toRadians,0,0)
  def draw(i:Float) = { mesh.vertices += turtle.pos; turtle.pos = turtle.pos + turtle.uf()*i*scale; mesh.vertices += turtle.pos }
  def skip(i:Float) = turtle.pos = turtle.pos + turtle.uf()*i*scale 
  def push() = { stack.push(Pose(turtle)); } //turtle.rotate(angles.x,0,0); scale *= 0.9 }
  def pop() = { turtle.set(stack.pop); } //turtle.rotate(-angles.x,0,0); scale /= 0.9 }
  

}

object Script extends SeerScript {

  Renderer().environment.alpha = 0.1
  Renderer().environment.blend = true
  Renderer().environment.depth = false

  // val lsys = new LSystem({
  //   case "a" => "a|b"
  //   case "b" => "f+[b]-f[a]"
  //   case a:String => a
  // })

  // val sentence = lsys("a").step(14)
  // println(sentence)

  val lsys = new LSystem({
    case "a" => "b|b"
    case "b" => "f+[b]-f[b]"
    case a:String => a
  })

  val sentence = lsys("a").step(14)

  val turtle = new TurtleGraphics
  turtle.angles = Vec3(30.toRadians)
  turtle(sentence)

  val model = Model(turtle.mesh)
  model.material = Material.basic
  model.material.color = RGB(0.1,0.1,0.1)

  override def draw(){
    FPS.print
    model.draw
  }

  override def animate(dt:Float){
    val angle = Mouse.x() * 2 - 1
    turtle.angles.set(angle*0.2 + 45f.toRadians)
    turtle(sentence)
    turtle.mesh.update

  }


}

Script