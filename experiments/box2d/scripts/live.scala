
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.math._

object Script extends SeerScript{

  Box2D.init()
  val world = new World(new Vector2(0, -10), true); 
  var debugRenderer:Box2DDebugRenderer = _
  val camera = new OrthographicCamera(20,20)
  camera.nav.pos.z = 2

  var initd = false

  Keyboard.cameraNavInput.nav = camera.nav

  // setup world
  val groundBodyDef =new BodyDef();  
  groundBodyDef.position.set(new Vector2(0, -10));  
  val groundBody = world.createBody(groundBodyDef);  

  val groundBox = new PolygonShape();  
  // (setAsBox takes half-width and half-height as arguments)
  groundBox.setAsBox(4, 0.5f);
  groundBody.createFixture(groundBox, 0.0f);
  groundBox.setAsBox(1f,2, new Vector2(-4.5,2), 0f);
  groundBody.createFixture(groundBox, 0.0f); 
  groundBox.setAsBox(1f,2, new Vector2(4.5,2), 0f);
  groundBody.createFixture(groundBox, 0.0f); 
  groundBox.dispose();

  var body:Body = _

  addCircle(Vec2(0,5))

  var startPut = false

  def addCircle(p:Vec2){
    // First we create a body definition
    val bodyDef = new BodyDef();
    bodyDef.`type` = BodyDef.BodyType.DynamicBody;
    bodyDef.position.set(p.x, p.y);
    body = world.createBody(bodyDef);
    val circle = new CircleShape();
    circle.setRadius(0.1f);
    val fixtureDef = new FixtureDef();
    fixtureDef.shape = circle;
    fixtureDef.density = 0.5f; 
    fixtureDef.friction = 0.4f;
    fixtureDef.restitution = 0.6f; // Make it bounce a little bit
    val fixture = body.createFixture(fixtureDef);
    circle.dispose();
  }

  override def init(){
    debugRenderer = new Box2DDebugRenderer();
    initd = true
  }
  override def draw(){
    debugRenderer.render(world, camera.combined);
  }

  var start = Vec2()
  var end = Vec2()
  override def animate(dt:Float){
    if(!initd) init()
    world.step(1/60f, 6, 2)

    if(Mouse.status() == "down"){
      startPut = true
      val x = (Mouse.x()-0.5) * camera.viewportWidth
      val y = (Mouse.y()-0.5) * camera.viewportHeight
      start = Vec2(x,y)
      // addCircle(Vec2(x,y))
    } else if( Mouse.status() == "drag"){
      val x = (Mouse.x()-0.5) * camera.viewportWidth
      val y = (Mouse.y()-0.5) * camera.viewportHeight
      end = Vec2(x,y)
    } else if (Mouse.status() == "up" && startPut){
      val x = (Mouse.x()-0.5) * camera.viewportWidth
      val y = (Mouse.y()-0.5) * camera.viewportHeight
      end = Vec2(x,y)

      startPut = false
      val dir = (start - end) * 0.1f
      val pos = body.getPosition()
      body.applyLinearImpulse(dir.x, dir.y, pos.x, pos.y, true);
    }
  }

}

Script