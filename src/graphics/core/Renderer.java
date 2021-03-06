package graphics.core;

import game.Game;
import graphics.Camera;
import graphics.Hud;
import graphics.InGameMenu;
import graphics.Light;
import graphics.RespawnMenu;
import graphics.Shader;
import graphics.Skybox;
import graphics.particles.ParticleSystem;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import math.Vector3f;

import actor.Actor;

import com.jogamp.opengl.util.FPSAnimator;

/* @author Chris Lundquist
 *  Based on the work by  Julien Gouesse (http://tuer.sourceforge.net)
 */
public class Renderer implements GLEventListener {
    public static String shaderString  = "lighting";

    private static final int NUM_LIGHTS = 4;

    private static final float FAR_PLANE = Skybox.SKYBOX_SIZE * 6.0f;

    private static final double FOV = 60;
    GLU glu;

    GLCanvas canvas;
    Frame frame;
    FPSAnimator animator;
    Shader shader;
    Hud hud;
    InGameMenu inGameMenu;
    RespawnMenu respawnMenu;
    Camera camera;
    private GL2 gl; // Must be recached each frame

    public Renderer(Camera camera) {
        glu = new GLU();
        canvas = new GLCanvas();
        frame = new Frame("cs143 project");
        animator = new FPSAnimator(canvas,60);
        shader = new Shader(shaderString + ".vert", shaderString + ".frag");
        hud = null; // default no HUD;
        inGameMenu = new InGameMenu();
        respawnMenu = new RespawnMenu();
        this.camera = camera;
    }

    // Display is our main game loop since the animator calls it
    public void display(GLAutoDrawable glDrawable) {
        getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();
        // update the camera position here so it doesn't fire on the dedicated server
        // Push the transformation for our player's Camera

        camera.setPerspective(gl);
        Light.update(gl, camera);
        render(Game.getMap().getSkybox());

        actor.ActorId playerShipId = Game.getPlayer().getShip().getId();

        // Render each actor
        for(Actor a: game.Game.getActors())
            //Don't render the players ship
            if (a.getId().equals(playerShipId) == false)
                render(a);

      shader.setUniform1b(gl, "lightingEnabled", false);
        if(ParticleSystem.isEnabled()){
            shader.setUniform1b(gl, "isTextured", false);
            ParticleSystem.render(gl);
            shader.setUniform1b(gl, "isTextured", true);
        }


        if (hud != null) {
            hud.drawStaticHud(gl);
        }

        if (inGameMenu != null) {
            inGameMenu.drawInGameMenu(gl);
        }
        
        if (respawnMenu != null) {
            respawnMenu.drawRespawnMenu(gl);
        }
        shader.setUniform1b(gl, "lightingEnabled", true);

        checkForGLErrors(gl);
    }

    private static void checkForGLErrors(GL2 gl) {
        int errno = gl.glGetError();
        switch (errno) {
            case GL2.GL_INVALID_ENUM:
                System.err.println("OpenGL Error: Invalid ENUM");
                break;
            case GL2.GL_INVALID_VALUE:
                System.err.println("OpenGL Error: Invalid Value");
                break;
            case GL2.GL_INVALID_OPERATION:
                System.err.println("OpenGL Error: Invalid Operation");
                break;
            case GL2.GL_STACK_OVERFLOW:
                System.err.println("OpenGL Error: Stack Overflow");
                break;
            case GL2.GL_STACK_UNDERFLOW:
                System.err.println("OpenGL Error: Stack Underflow");
                break;
            case GL2.GL_OUT_OF_MEMORY:
                System.err.println("OpenGL Error: Out of Memory");
                break;
            default:
                return;
        }
    }

    public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {
    }

    public void init(GLAutoDrawable gLDrawable) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        getGL2(); // Repopulate gl each frame because it is not guaranteed to be persistent

        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.setSwapInterval(1); // Enable V-Sync supposedly
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-1, 1, -1, 1, Vector3f.EPSILON, FAR_PLANE);
        gl.glMatrixMode(GL2.GL_MODELVIEW);

        ((Component) gLDrawable).addKeyListener(game.Game.getInputHandler());

        Model.loadModels();
        Texture.initialize(gl);

        for(Model model: Model.loaded_models())
            build_display_list(model);

        try {
            shader.init(gl);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        shader.enable(gl);
        // We have to setup the lights after we enable the shader so we can set the uniform
        Light.initialize(gl, NUM_LIGHTS);
        shader.setUniform1i(gl, "numLights", NUM_LIGHTS);
        shader.setUniform1b(gl, "lightingEnabled", true);

        System.gc(); // This is probably a good a idea
    }

    private void build_display_list(Model model) {
        model.displayList = gl.glGenLists(1);
        gl.glNewList(model.displayList, GL2.GL_COMPILE);
        for (Polygon p: model.polygons)
            render(p);
        gl.glEndList();
    }

    private void render(Polygon p) {
        if (p.verticies.size() < 2)
            return;
        gl.glColor4f(0, 0, 0, 1);
        p.getMaterial().prepare(gl);
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glNormal3f(p.normal.x, p.normal.y, p.normal.z);
        if (p.verticies.size() == 3) {
            for (Vertex v: p.verticies){
                if(p.normal != null)
                    gl.glNormal3f(p.normal.x, p.normal.y, p.normal.z);
                gl.glTexCoord2f(v.u, v.v); 
                gl.glVertex3f(v.getX(), v.getY(), v.getZ());
            }
        } else {
            Vertex a = p.verticies.get(0);

            for (int i = 2; i < p.verticies.size(); i++) {
                Vertex b = p.verticies.get(i - 1);
                Vertex c = p.verticies.get(i);

                if(a.normal != null)
                    gl.glNormal3f(a.normal.x, a.normal.y, a.normal.z);
                gl.glTexCoord2f(a.u, a.v); 
                gl.glVertex3f(a.getX(), a.getY(), a.getZ());

                if(b.normal != null)
                    gl.glNormal3f(b.normal.x, b.normal.y, b.normal.z);
                gl.glTexCoord2f(b.u, b.v); 
                gl.glVertex3f(b.getX(), b.getY(), b.getZ());

                if(c.normal != null)
                    gl.glNormal3f(c.normal.x, c.normal.y, c.normal.z);
                gl.glTexCoord2f(c.u, c.v); 
                gl.glVertex3f(c.getX(), c.getY(), c.getZ());
            }
        }
        gl.glEnd();
    }

    public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
        getGL2();
        if (height <= 0)
            height = 1;

        float h = (float) width / (float) height;
        gl.glPushMatrix();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(FOV, h, 1.0, FAR_PLANE);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
    }

    public void dispose(GLAutoDrawable gLDrawable) {
        try {
            if (animator.isAnimating())
                animator.stop();
            
            Thread ani = animator.getThread(); 
            if (ani != null)
                ani.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void shutdown() throws InterruptedException {      
        frame.dispose();
    }

    public void start() {
        canvas.addGLEventListener(this);
        frame.add(canvas);
        frame.setSize(600, 480);
        frame.setUndecorated(true);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Game.exit();
            }
        });
        frame.setVisible(true);
        animator.start();
        canvas.requestFocus();
    }

    // CL - Private method that handles the exception code that would otherwise
    // be copy pasted. It seems that if other people use this method getGL() 
    // usually returns null and crashes the program
    private void getGL2(){
        try {
            gl = canvas.getGL().getGL2();
        } catch(Exception e) {
            System.err.println("Error getting OpenGL Context:\n" + e.toString());
            System.err.println("--------");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void render(Actor actor) {
        gl.glPushMatrix();
        gl.glMultMatrixf(actor.getTransform().toFloatArray(),0);
        // CL - Render our model.
        render(actor.getModel());
        gl.glPopMatrix();
    }

    private void render(Skybox skybox) {
        gl.glPushMatrix();
        math.Vector3f pos = camera.getPosition();
        gl.glTranslatef(pos.x, pos.y, pos.z);
        gl.glScalef(-Skybox.SKYBOX_SIZE, -Skybox.SKYBOX_SIZE , -Skybox.SKYBOX_SIZE);
        render(skybox.getModel());
        gl.glPopMatrix(); 
    }

    private void render(Model model) {
        // CL - The scaling, rotating, translating is handled per actor
        //      The display list should have already been "adjusted" if it
        //      wasn't at the center of mass or correct world orientation
        //      when it was loaded.
        shader.setUniform1b(gl, "isTextured", model.isTextured);

        if(gl.glIsList(model.displayList) == false)
            build_display_list(model);
        else
            gl.glCallList(model.displayList);
    }

    public void setHud(Hud hud) {
        this.hud = hud;
    }


    public Shader getShader() {
        return shader;
    }
}
