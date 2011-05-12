package graphics;

import game.Game;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

import actor.Actor;

import com.jogamp.opengl.util.FPSAnimator;

/* @author Chris Lundquist
 *  Based on the work by  Julien Gouesse (http://tuer.sourceforge.net)
 */
public class Renderer implements GLEventListener {
    GLU glu;
    GLCanvas canvas;
    Frame frame;
    //Animator animator;
    FPSAnimator animator;
    Shader shader;
    Hud hud;
    Camera camera;
    //ParticleFire particle;
    public Renderer(Camera camera) {
        glu = new GLU();
        canvas = new GLCanvas();
        frame = new Frame("cs143 projectx");
        animator = new FPSAnimator(canvas,60);
        shader = new Shader("lambert.vert","lambert.frag");
        hud = new Hud();
        this.camera = camera;
    }
    // Display is our main game loop since the animator calls it
    public void display(GLAutoDrawable glDrawable) {
        GL2 gl = getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();
        // update the camera position here so it doesn't fire on the dedicated server
        // Push the transformation for our player's Camera

        camera.setPerspective(gl);
        Light.update(gl, camera);
        Game.getMap().getSkybox().render(gl, camera);

        // Render each actor
        for(Actor a: game.Game.getActors())
            a.render(gl);

        hud.drawStaticHud(gl);
        
        checkForGLErrors(gl);
        
       /* particle.setParameters(0, 0, 0);
        particle.draw(gl);*/
        
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

        GL2 gl = getGL2();
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.setSwapInterval(1); // Enable V-Sync supposedly
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

        ((Component) gLDrawable).addKeyListener(game.Game.getInputHandler());
        Model.initialize(gl); /* calls Texture.initialize */
        ///hud.init(gLDrawable);
        sound.Manager.initialize();
        graphics.particles.ParticleSystem.initialize(gl);

        try {
            shader.init(gl);
        } catch (java.io.IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //shader.enable(gl);
        // We have to setup the lights after we enable the shader so we can set the uniform
        Light.initialize(gl, 2);

        System.gc(); // This is probably a good a idea
    }




    public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
        GL2 gl = getGL2();
        if (height <= 0) {
            height = 1;
        }
        float h = (float) width / (float) height;
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(50.0f, h, 1.0, 1000.0);
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void dispose(GLAutoDrawable gLDrawable) {
        animator.stop();
        frame.dispose();
        canvas.destroy();
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
    private GL2 getGL2(){
        GL2 gl = null;
        try {
            gl = canvas.getGL().getGL2();
        } catch(Exception e) {
            System.err.println("Error getting OpenGL Context:\n" + e.toString());
            System.err.println("--------");
            e.printStackTrace();
            System.exit(-1);
        }
        return gl;
    }
    
    public Shader getShader() {
        return shader;
    }
}
