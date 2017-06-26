/**
 * 
 * LiquidFunProcessing | Copyright 2017 Thomas Diewald - www.thomasdiewald.com
 * 
 * https://github.com/diwi/LiquidFunProcessing.git
 * 
 * Box2d / LiquidFun Library for Processing.
 * MIT License: https://opensource.org/licenses/MIT
 * 
 */



package testbed;


import com.thomasdiewald.liquidfun.java.DwWorld;
import com.thomasdiewald.liquidfun.java.interaction.DwParticleDestroyer;
import com.thomasdiewald.liquidfun.java.interaction.DwParticleSpawn;
import com.thomasdiewald.liquidfun.java.render.DwBodyGroup;
import com.thomasdiewald.liquidfun.java.render.DwParticleRenderGL;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleType;

import processing.core.*;
import processing.opengl.PGraphics2D;


public class liquidfun_ParticleTypes extends PApplet {

  int viewport_w = 1280;
  int viewport_h = 720;
  int viewport_x = 230;
  int viewport_y = 0;

  boolean UPDATE_PHYSICS = true;
  boolean USE_DEBUG_DRAW = false;

  DwWorld world;
  DwBodyGroup bodies;
  DwParticleRenderGL particles;

  DwParticleDestroyer  particle_destroyer;

//  PImage sprite;
  
  PFont font;

  public void settings(){
    size(viewport_w, viewport_h, P2D);
    smooth(8);
  }


  public void setup(){ 
    surface.setLocation(viewport_x, viewport_y);

//    sprite = loadImage("sprite.png");
    font = createFont("data/SourceCodePro-Regular.ttf", 12);

    reset();
    frameRate(120);
  }


  public void release(){
    if(bodies    != null) bodies   .release(); bodies    = null;
    if(particles != null) particles.release(); particles = null;
    if(world     != null) world    .release(); world     = null;
  }


  public void reset(){
    // release old resources
    release();

    world = new DwWorld(this, 18);
    world.transform.setScreen(width, height, 20, width/2, height);
    world.setParticleRadius(0.20f);

    // Renderer
    bodies = new DwBodyGroup(this, world, world.transform);

    particles = new DwParticleRenderGL(this, world, world.transform);
//    particles.param.tex_sprite = sprite;
    particles.param.falloff_exp1 = 1;
    particles.param.falloff_exp2 = 3;
    particles.param.radius_scale = 3f;
    particles.param.falloff_mult = 1;

    particle_destroyer  = new DwParticleDestroyer(world, world.transform);

    setParticleSpawnProperties(spawn_type);
    
    // create scene: rigid bodies, particles, etc ...
    initScene();
  }



  public void draw(){

    bodies.addBullet(true, color(255), true, color(0), 1f);

    if(UPDATE_PHYSICS){
      mouseDrawAction();
      world.update();
      particles.update();
    }

    PGraphics2D canvas = (PGraphics2D) this.g;
    canvas.background(32);
    canvas.pushMatrix();
    world.applyTransform(canvas);
    world.drawBulletSpawnTrack(canvas);
    if(USE_DEBUG_DRAW){
      world.displayDebugDraw(canvas);
      // DwDebugDraw.display(canvas, world);
    } else {
      bodies.display(canvas);
      particles.display(canvas);
    }
    canvas.popMatrix();
    
    int tx = 16;
    int ty = 16;
    int gy = 14;

    textFont(font);
    fill(220);
    textAlign(LEFT);
    text("LMB + SHIFT   shoot bullet" , tx, ty+=gy);
    text("MMB           draw particles"  , tx, ty+=gy);
    text("RMB           delete particles", tx, ty+=gy);
    ty+=gy;
    text("'1'  water"   , tx, ty+=gy);
    text("'2'  viscous" , tx, ty+=gy);
    text("'3'  tensile" , tx, ty+=gy);
    text("'4'  powder"  , tx, ty+=gy);
    text("'5'  elastic"  , tx, ty+=gy);
    text("'6'  wall"    , tx, ty+=gy);
    
    float bucket_w = width / (float) (num_buckets);
    tx = (int) (bucket_w/2);
    ty = height-10;
    textAlign(CENTER);
    text("water"   , tx, ty);
    text("viscous" , tx+=bucket_w, ty);
    text("tensile" , tx+=bucket_w, ty);
    text("powder"  , tx+=bucket_w, ty);
    text("elastic" , tx+=bucket_w, ty);
    
    // info
    int num_bodies    = world.getBodyCount();
    int num_particles = world.getParticleCount();
    String txt_fps = String.format(getClass().getName()+ " [bodies: %d]  [particles: %d]  [fps %6.2f]", num_bodies, num_particles, frameRate);
    surface.setTitle(txt_fps);
  }


  //////////////////////////////////////////////////////////////////////////////
  // User Interaction
  //////////////////////////////////////////////////////////////////////////////
  public void keyReleased(){
    if(key == 't') UPDATE_PHYSICS = !UPDATE_PHYSICS;
    if(key == 'r') reset();
    if(key == 'f') USE_DEBUG_DRAW = !USE_DEBUG_DRAW;
    if(key >= '1' && key <= '6') setParticleSpawnProperties(key - '1');
  }

  
  int spawn_radius = 40;

  public void mousePressed() {
    if(mouseButton == RIGHT){
      particle_destroyer.destroyParticles(mouseX, mouseY, spawn_radius);
    }
  }
  
  public void mouseDrawAction(){    
    if(mousePressed){
      if(mouseButton == RIGHT){
        particle_destroyer.destroyParticles(mouseX, mouseY, spawn_radius);
      }
    }
  }
  

  
  
  //////////////////////////////////////////////////////////////////////////////
  // Scene Setup
  //////////////////////////////////////////////////////////////////////////////

  int spawn_type = 0;
 
  public void setParticleSpawnProperties(int type){
    
    spawn_type = type;

    int COLOR_MIX = 0;
//    COLOR_MIX = ParticleType.b2_colorMixingParticle;
    
    DwParticleSpawn particle_spawn = world.mouse_spawn_particles;
    particle_spawn.join_groups = false;
    particle_spawn.setCircleShape(40);

    switch (type) {
      case 0:
        particle_spawn.group_def.setColor(createHSBColor(225, 96, 96));
        particle_spawn.group_def.flags = ParticleType.b2_waterParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 1:
        particle_spawn.group_def.setColor(createHSBColor(100, 96, 96));
        particle_spawn.group_def.flags = ParticleType.b2_viscousParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 2:
        particle_spawn.group_def.setColor(createHSBColor(5, 96, 96));
        particle_spawn.group_def.flags = ParticleType.b2_tensileParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 3:
        particle_spawn.group_def.setColor(createHSBColor(70, 50, 96));
        particle_spawn.group_def.flags = ParticleType.b2_powderParticle | COLOR_MIX;
        particle_spawn.group_def.groupFlags = 0;
        break;
      case 4:
        particle_spawn.group_def.setColor(createHSBColor(300, 96, 96));
//        particle_spawn.group_def.flags = ParticleType.b2_elasticParticle | ParticleType.b2_springParticle | COLOR_MIX;
        particle_spawn.group_def.flags = ParticleType.b2_elasticParticle | COLOR_MIX;

        particle_spawn.group_def.groupFlags = 0;
        break;
      case 5:
        particle_spawn.group_def.setColor(createHSBColor(120, 90, 50));
        particle_spawn.group_def.flags = ParticleType.b2_wallParticle;
        particle_spawn.group_def.groupFlags = ParticleGroupType.b2_solidParticleGroup;
        particle_spawn.join_groups = true;
        particle_spawn.setCircleShape(15);
        break;
    }

  }

  
  int num_buckets = 5;
  
  public void initScene() {
    
    float dimx = world.transform.box2d_dimx;
    float dimy = world.transform.box2d_dimy;
    float thick = 30f / world.transform.screen_scale;

    float dimxh = dimx/2;
    float dimyh = dimy/2;
    
    float bucket_w = dimx / num_buckets;

    {
      BodyDef bd = new BodyDef();
      Body ground = world.createBody(bd);

      ChainShape shape = new ChainShape();
      Vec2[] vertices = {new Vec2(-dimxh, 0), new Vec2(dimxh, 0), new Vec2(dimxh, dimy), new Vec2(-dimxh, dimy)};
      shape.createLoop(vertices, 4);
      ground.createFixture(shape, 0.0f);
      
 
      PolygonShape pshape = new PolygonShape();
      pshape.setAsBox(dimxh, thick);
      Fixture solid_bottom = ground.createFixture(pshape, 0.0f);
      
      EdgeShape eshape = new EdgeShape();
      
      float shift_x = -dimxh + bucket_w;
      for(int i = 0; i < num_buckets - 1; i++){
        Vec2 p1 = new Vec2(shift_x + i * bucket_w, 0);
        Vec2 p2 = new Vec2(shift_x + i * bucket_w, 5f * dimy / 6f);
        eshape.set(p1, p2);
        
        ground.createFixture(eshape, 0.0f);
      }

      bodies.add(ground, false, color(0), true, color(0), 2f);
      world.setStyle(solid_bottom, true, color(0), false, color(0), 1f);
    }
    

    // save current spawn type
    int spawn_type_cpy = spawn_type;
    
    
    float bucket_w_screen = width / (float) num_buckets;
    for(int i = 0; i < num_buckets; i++){
      float cx = i * bucket_w_screen + bucket_w_screen * 0.5f;
      float cy = 1 * height / 3f;
      
      setParticleSpawnProperties(i);

      float screen_x = cx;
      float screen_y = cy;
      float size_x = bucket_w_screen * 0.66f;
      float size_y = size_x * 2;
      world.mouse_spawn_particles.setBoxShape(size_x, size_y);
      world.mouse_spawn_particles.spawn(screen_x, screen_y);
    }
    
    // reset type/color to last used type
    setParticleSpawnProperties(spawn_type_cpy);
  }




  Color3f createHSBColor(float hsb_h, float hsb_s, float hsb_b){
    colorMode(HSB, 360, 100, 100);
    int rgb = color(hsb_h, hsb_s, hsb_b);
    colorMode(RGB, 255, 255, 255);

    float r = ((rgb >> 16) & 0xFF) / 255f;
    float g = ((rgb >>  8) & 0xFF) / 255f;
    float b = ((rgb >>  0) & 0xFF) / 255f;
    return new Color3f(r,g,b);
  }

  
  
  public static void main(String args[]) {
    PApplet.main(new String[] { liquidfun_ParticleTypes.class.getName() });
  }

}