import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.*; 
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;//for new version of gl
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

import com.jogamp.opengl.util.FPSAnimator;//for new version of gl


public class Controller extends JFrame
	implements GLEventListener, KeyListener, MouseListener, MouseMotionListener
{
	
	private static final long serialVersionUID = 1L;
	private final int DEFAULT_WINDOW_WIDTH=1024;
	private final int DEFAULT_WINDOW_HEIGHT=1024;
	private final float DEFAULT_LINE_WIDTH=1.0f;

	private GLCapabilities capabilities;
	private GLCanvas canvas;
	private FPSAnimator animator;

	private int numTestCase;
	private int testCase;
	private BufferedImage buff;
	@SuppressWarnings("unused")
	private ColorType color;
	private Random rng;
	
	// shade rendering modes for objects in the scene
	// 0 = FLAT SHADING
	// 1 = GOURAUD
	// 2 = PHONG
	private int renderer = 0;
	
	// Light switches
	private Boolean Light_1 = true;
	private Boolean Light_2 = true;
	private Boolean Light_3 = true;
	private Boolean Light_4 = true;
	
	// Flags to toggle lighting terms
	private Boolean toggle_ambient = true;
	private Boolean toggle_diffuse = true;
	private Boolean toggle_specular = true;
	
	// specular exponent ns
	private int ns = 1;
	
	private ArrayList<Point2D> lineSegs;
	private ArrayList<Point2D> triangles;
	private boolean doSmoothShading;
	private int Nsteps;

	/** The quaternion which controls the rotation of the world. */
    private Quaternion viewing_quaternion = new Quaternion();
    private Point3D viewing_center = new Point3D((float)(DEFAULT_WINDOW_WIDTH/2),(float)(DEFAULT_WINDOW_HEIGHT/2),(float)0.0);
    /** The last x and y coordinates of the mouse press. */
    private int last_x = 0, last_y = 0;
    /** Whether the world is being rotated. */
    private boolean rotate_world = false;
    
    /** Random colors **/
    private ColorType[] colorMap = new ColorType[100];
    private Random rand = new Random();
    
	public Controller()
	{
	    capabilities = new GLCapabilities(null);
	    capabilities.setDoubleBuffered(true);  // Enable Double buffering

	    canvas  = new GLCanvas(capabilities);
	    canvas.addGLEventListener(this);
	    canvas.addMouseListener(this);
	    canvas.addMouseMotionListener(this);
	    canvas.addKeyListener(this);
	    canvas.setAutoSwapBufferMode(true); // true by default. Just to be explicit
	    canvas.setFocusable(true);
	    getContentPane().add(canvas);

	    animator = new FPSAnimator(canvas, 60); // drive the display loop @ 60 FPS

	    numTestCase = 3;
	    testCase = 0;
	    Nsteps = 12;

	    setTitle("CS480/680 Lab for PA4");
	    setSize( DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setVisible(true);
	    setResizable(false);
	    
	    rng = new Random();
	    color = new ColorType(1.0f,0.0f,0.0f);
	    lineSegs = new ArrayList<Point2D>();
	    triangles = new ArrayList<Point2D>();
	    doSmoothShading = false;
	    
	    for (int i=0; i<100; i++) {
	    	this.colorMap[i] = new ColorType(i*0.005f+0.5f, i*-0.005f+1f, i*0.0025f+0.75f);
	    }
	}

	public void run()
	{
		animator.start();
	}

	public static void main( String[] args )
	{
		Controller P = new Controller();
	    P.run();
	}

	//*********************************************** 
	//  GLEventListener Interfaces
	//*********************************************** 
	public void init( GLAutoDrawable drawable) 
	{
	    GL gl = drawable.getGL();
	    gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f);
	    gl.glLineWidth( DEFAULT_LINE_WIDTH );
	    Dimension sz = this.getContentPane().getSize();
	    buff = new BufferedImage(sz.width,sz.height,BufferedImage.TYPE_3BYTE_BGR);
	    clearPixelBuffer();
	}

	// Redisplaying graphics
	public void display(GLAutoDrawable drawable)
	{
	    GL2 gl = drawable.getGL().getGL2();
	    WritableRaster wr = buff.getRaster();
	    DataBufferByte dbb = (DataBufferByte) wr.getDataBuffer();
	    byte[] data = dbb.getData();

	    gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);
	    gl.glDrawPixels (buff.getWidth(), buff.getHeight(),
                GL2.GL_BGR, GL2.GL_UNSIGNED_BYTE,
                ByteBuffer.wrap(data));
        drawTestCase();
	}

	// Window size change
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		// deliberately left blank
	}
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
	      boolean deviceChanged)
	{
		// deliberately left blank
	}
	
	void clearPixelBuffer()
	{
		lineSegs.clear();
    	triangles.clear();
		Graphics2D g = buff.createGraphics();
	    g.setColor(Color.BLACK);
	    g.fillRect(0, 0, buff.getWidth(), buff.getHeight());
	    g.dispose();
	}
	
	// drawTest
	void drawTestCase()
	{  
		/* clear the window and vertex state */
		clearPixelBuffer();
	  
		//System.out.printf("Test case = %d\n",testCase);

		switch (testCase){
		/** Test case 0
		 * 	Shapes: Grey Sphere, Green Cube, Teal Torus, Yellow Ellipsoid, Purple Cylinder
		 *  White ambient light w/
		 *  one blue infinite light source
		 *  one red point light source,
		 **/
		case 0:
			shadeTest1(true);
			break;
		/** Test case 1
		 * 	Shapes: Grey Sphere, Green Cube, Teal Torus, Yellow Ellipsoid, Purple Cylinder
		 *  White ambient light w/
		 *  one green point light source,
		 *  one purple point light source
		 **/
		case 1:
			shadeTest2(true);
			break;
		/** Test case 2
		 * 	Shapes: Grey Sphere, Green Cube, Teal Torus, Yellow Ellipsoid, Purple Cylinder
		 *  Orange ambient light w/
		 *  one white infinite light source,
		 *  one red infinite light source,
		 *  one green point light source
		 **/
		case 2:
			shadeTest3(true);
			break;
		}	
	}


	//*********************************************** 
	//          KeyListener Interfaces
	//*********************************************** 
	public void keyTyped(KeyEvent key)
	{
	//      Q,q: quit 
	//      C,c: clear polygon (set vertex count=0)
	//		R,r: randomly change the color
	//		S,s: toggle the smooth shading
	//		T,t: show testing examples (toggles between smooth shading and flat shading test cases)
	//		>:	 increase the step number for examples
	//		<:   decrease the step number for examples
	//     +,-:  increase or decrease spectral exponent

	    switch ( key.getKeyChar() ) 
	    {
	    case 'Q' :
	    case 'q' : 
	    	new Thread()
	    	{
	          	public void run() { animator.stop(); }
	        }.start();
	        System.exit(0);
	        break;
	    case 'R' :
	    case 'r' :
	    	color = new ColorType(rng.nextFloat(),rng.nextFloat(),
	    			rng.nextFloat());
	    	break;
	    case 'C' :
	    case 'c' :
	    	clearPixelBuffer();
	    	break;
	    case 'A' :
	    case 'a' :
	    	toggle_ambient = !toggle_ambient;
	    	drawTestCase();
	    	break;
	    case 'D' :
	    case 'd' :
	    	toggle_diffuse = !toggle_diffuse;
	    	drawTestCase();
	    	break;
	    case 'S' :
	    case 's' :
	    	toggle_specular = !toggle_specular;
	    	drawTestCase();
	    	break;
	    case 'T' :
	    case 't' : 
	    	testCase = (testCase+1)%numTestCase;
	    	reset();
	    	drawTestCase();
	        break; 
	    case '<':  
	        Nsteps = Nsteps < 4 ? Nsteps: Nsteps / 2;
	        System.out.printf( "Nsteps = %d \n", Nsteps);
	        drawTestCase();
	        break;
	    case '>':
	        Nsteps = Nsteps > 190 ? Nsteps: Nsteps * 2;
	        System.out.printf( "Nsteps = %d \n", Nsteps);
	        drawTestCase();
	        break;
	    case '+':
	    	ns++;
	        drawTestCase();
	    	break;
	    case '-':
	    	if(ns>0)
	    		ns--;
	        drawTestCase();
	    	break;
	    case 'F':
	    case 'f':
	    	renderer = 0;
	    	drawTestCase();
	    	break;
	    case 'G':
	    case 'g':
	    	renderer = 1;
	    	drawTestCase();
	    	break;
	    case 'P':
	    case 'p':
	    	renderer = 2;
	    	drawTestCase();
	    	break;
	    case '1':
	    	Light_1 = !Light_1;
	    	drawTestCase();
	    	break;
	    case '2':
	    	Light_2 = !Light_2;
	    	drawTestCase();
	    	break;
	    case '3':
	    	Light_3 = !Light_3;
	    	drawTestCase();
	    	break;
	    case '4':
	    	Light_4 = !Light_4;
	    	drawTestCase();
	    	break;
	    default :
	        break;
	    }
	}

	public void keyPressed(KeyEvent key)
	{
	    switch (key.getKeyCode()) 
	    {
	    case KeyEvent.VK_ESCAPE:
	    	new Thread()
	        {
	    		public void run()
	    		{
	    			animator.stop();
	    		}
	        }.start();
	        System.exit(0);
	        break;
	      default:
	        break;
	    }
	}

	public void keyReleased(KeyEvent key)
	{
		// deliberately left blank
	}

	//************************************************** 
	// MouseListener and MouseMotionListener Interfaces
	//************************************************** 
	public void mouseClicked(MouseEvent mouse)
	{
		// deliberately left blank
	}
	  public void mousePressed(MouseEvent mouse)
	  {
	    int button = mouse.getButton();
	    if ( button == MouseEvent.BUTTON1 )
	    {
	      last_x = mouse.getX();
	      last_y = mouse.getY();
	      rotate_world = true;
	    }
	  }

	  public void mouseReleased(MouseEvent mouse)
	  {
	    int button = mouse.getButton();
	    if ( button == MouseEvent.BUTTON1 )
	    {
	      rotate_world = false;
	    }
	  }

	public void mouseMoved( MouseEvent mouse)
	{
		// Deliberately left blank
	}

	/**
	   * Updates the rotation quaternion as the mouse is dragged.
	   * 
	   * @param mouse
	   *          The mouse drag event object.
	   */
	  public void mouseDragged(final MouseEvent mouse) {
	    if (this.rotate_world) {
	      // get the current position of the mouse
	      final int x = mouse.getX();
	      final int y = mouse.getY();

	      // get the change in position from the previous one
	      final int dx = x - this.last_x;
	      final int dy = y - this.last_y;

	      // create a unit vector in the direction of the vector (dy, dx, 0)
	      final float magnitude = (float)Math.sqrt(dx * dx + dy * dy);
	      if(magnitude > 0.0001)
	      {
	    	  // define axis perpendicular to (dx,-dy,0)
	    	  // use -y because origin is in upper lefthand corner of the window
	    	  final float[] axis = new float[] { -(float) (dy / magnitude),
	    			  (float) (dx / magnitude), 0 };

	    	  // calculate appropriate quaternion
	    	  final float viewing_delta = 3.1415927f / 180.0f;
	    	  final float s = (float) Math.sin(0.5f * viewing_delta);
	    	  final float c = (float) Math.cos(0.5f * viewing_delta);
	    	  final Quaternion Q = new Quaternion(c, s * axis[0], s * axis[1], s
	    			  * axis[2]);
	    	  this.viewing_quaternion = Q.multiply(this.viewing_quaternion);

	    	  // normalize to counteract acccumulating round-off error
	    	  this.viewing_quaternion.normalize();

	    	  // save x, y as last x, y
	    	  this.last_x = x;
	    	  this.last_y = y;
	          drawTestCase();
	      }
	    }

	  }
	  
	public void mouseEntered( MouseEvent mouse)
	{
		// Deliberately left blank
	}

	public void mouseExited( MouseEvent mouse)
	{
		// Deliberately left blank
	} 


	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}
	
	//************************************************** 
	// Test Cases
	// Nov 9, 2014 Stan Sclaroff -- removed line and triangle test cases
	//************************************************** 

	/** Test case 1 **/
	void shadeTest1(boolean doSmooth){
		// the simple example scene includes one sphere and one torus
		
		DepthBuffer depth_buffer = new DepthBuffer(buff.getHeight(),buff.getWidth());
		
		AmbientLight amb_light = new AmbientLight(new ColorType (1.0f, 1.0f, 1.0f));
        
        ArrayList<InfiniteLight> inf_lights = new ArrayList<InfiniteLight>();
        InfiniteLight inf_light_1 = new InfiniteLight (new ColorType (0.0f, 0.0f, 1.0f), new Point3D (1.0f, 1.0f, 1.0f));
        
        // Toggle blue infinite light
        if (Light_1)
        {
        	inf_lights.add(inf_light_1);
        }
        else
        {
        	inf_lights.remove(inf_light_1);
        }
        
        ArrayList<PointLight> pt_lights = new ArrayList<PointLight>();
        PointLight pt_light_1 = new PointLight (new ColorType(1.0f, 0.0f, 0.0f), new Point3D (900.0f, 400.0f, 100.0f));
        
        // Toggle red point light
        if (Light_2)
        {
        	pt_lights.add(pt_light_1);
        }
        else
        {
        	pt_lights.remove(pt_light_1);
        }
        
        SpotLight spot_atten = new SpotLight (new ColorType(1.0f, 1.0f, 1.0f), new Point3D (280.0f, 384.0f, 0.0f), new Point3D (1.0f, 0.0f, 0.0f), 0.0f);
        // weight variables for calculating the radial attenuation factor
        float spot_atten_a0 = 0.1f;
        float spot_atten_a1 = 0.1f;
        float spot_atten_a2 = 0.1f;
        // al exponent for angular attenuation
        float spot_atten_al = 2.0f;
        
		// view vector is defined along z axis
        // this example assumes simple othorgraphic projection
        // view vector is used in 
        //   (a) calculating specular lighting contribution
        //   (b) backface culling / backface rejection
        Point3D view_vector = new Point3D((float)0.0,(float)0.0,(float)1.0);
        
        // normal to the plane of a triangle
        // to be used in backface culling / backface rejection
        Point3D triangle_normal = new Point3D();
        
        // a triangle mesh
        Mesh3D mesh;
            
		int i, j, n, m;
		
		// temporary variables for triangle 3D vertices and 3D normals
		Point3D v0,v1, v2, n0, n1, n2;
		
		// projected triangle, with vertex colors
		Point3D[] tri = {new Point3D(), new Point3D(), new Point3D()};
		
		/************/
		/** Sphere **/
        /************/
		float sphere_radius_x = (float)60.0;
		float sphere_radius_y = (float)60.0;
		float sphere_radius_z = (float)60.0;
        Ellipsoid3D sphere = new Ellipsoid3D((float)250.0, (float)220.0, (float)0.0, (float)1.5*sphere_radius_x, (float)1.5*sphere_radius_y, (float)1.5*sphere_radius_z, Nsteps, Nsteps);
        
        // Sphere material and properties
        ColorType sphere_ka;
        ColorType sphere_kd;
        ColorType sphere_ks;
        
        if (toggle_ambient)
        {
        	sphere_ka = new ColorType (0.4f, 0.4f, 0.4f);
        }
        else
        {
        	sphere_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	 sphere_kd = new ColorType (0.2f, 0.2f, 0.2f);
        }
        else
        {
        	sphere_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	sphere_ks = new ColorType (0.8f, 0.8f, 0.8f);
        }
        else
        {
        	sphere_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
        
        Material mat_sphere = new Material(sphere_ka, sphere_kd, sphere_ks, ns);
		
		mesh=sphere.mesh;
		n=sphere.get_n();
		m=sphere.get_m();
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		// draw triangles for the current surface, using vertex colors
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_sphere);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading: 
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_sphere);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
	    
		/************/
		/** Ellipsoid **/
        /************/
		float ellip_radius_x = (float)60.0;
		float ellip_radius_y = (float)50.0;
		float ellip_radius_z = (float)80.0;
        Ellipsoid3D ellipsoid = new Ellipsoid3D((float)430.0, (float)400.0, (float)0.0, (float)1.5*ellip_radius_x, (float)1.5*ellip_radius_y, (float)1.5*ellip_radius_z, Nsteps, Nsteps);
        
        // Ellipsoid material and properties
        ColorType ellip_ka;
        ColorType ellip_kd;
        ColorType ellip_ks;
        
        if (toggle_ambient)
        {
        	ellip_ka = new ColorType (0.8f, 0.8f, 0.2f);
        }
        else
        {
        	ellip_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	ellip_kd = new ColorType (0.2f, 0.2f, 0.2f);
        }
        else
        {
        	ellip_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	ellip_ks = new ColorType (0.8f, 0.8f, 0.8f);
        }
        else
        {
        	ellip_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
        
        Material mat_ellip = new Material(ellip_ka, ellip_kd, ellip_ks, ns);
		
		mesh=ellipsoid.mesh;
		n=ellipsoid.get_n();
		m=ellipsoid.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_ellip);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_ellip);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		/**************/
		/** Cylinder **/
		/**************/
		
		float cyl_radius_x = (float)60.0;
		float cyl_radius_y = (float)60.0;
		float cyl_radius_z = (float)30.0;
		float cyl_z_end = (float)100.0;
		float cyl_center_x = (float)250.0;
		float cyl_center_y = (float)600.0;
		float cyl_center_z = (float)0.0;
		
		Cylinder3D cylinder = new Cylinder3D(cyl_center_x, cyl_center_y, cyl_center_z, (float)1.5*cyl_radius_x, (float)1.5*cyl_radius_y, (float)1.5*cyl_radius_z, (float) 1.5 * cyl_z_end, Nsteps, Nsteps);
		CylinderFront cyl_front = new CylinderFront(cyl_center_x, cyl_center_y, cyl_center_z, (float)1.5*cyl_radius_x, (float)1.5*cyl_radius_y, (float) 1.5*cyl_z_end, 3, Nsteps);
		CylinderBack cyl_back = new CylinderBack(cyl_center_x, cyl_center_y, cyl_center_z, (float)1.5*cyl_radius_x, (float)1.5*cyl_radius_y, (float) 1.5*cyl_z_end, 3, Nsteps);
        
        // Cylinder material and properties
        ColorType cyl_ka;
        ColorType cyl_kd;
        ColorType cyl_ks;
        
        if (toggle_ambient)
        {
        	cyl_ka = new ColorType (0.3f, 0.0f, 0.5f);
        }
        else
        {
        	cyl_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	cyl_kd = new ColorType (0.2f, 0.2f, 0.2f);
        }
        else
        {
        	cyl_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	cyl_ks = new ColorType (0.8f, 0.8f, 0.8f);
        }
        else
        {
        	cyl_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
        
        Material mat_cyl = new Material(cyl_ka, cyl_kd, cyl_ks, ns);
		
		mesh=cylinder.mesh;
		n=cylinder.get_n();
		m=cylinder.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
			}
	    }
		
		mesh=cyl_front.mesh;
		n=cyl_front.get_n();
		m=cyl_front.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
		
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}
	    }
		
		mesh=cyl_back.mesh;
		n=cyl_back.get_n();
		m=cyl_back.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
		
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}
	    }
		
		
		/***********/
		/** Torus **/
		/***********/
		float torus_radius_axial = (float)50.0;
		float torus_radius = (float)25.0;
		Torus3D torus = new Torus3D((float)630.0, (float)600.0, (float)0.0,
								(float)1.5*torus_radius_axial, (float)1.5*torus_radius,
								Nsteps, Nsteps);
		
		// Torus material and properties
        ColorType torus_ka;
        ColorType torus_kd;
        ColorType torus_ks;
        
        if (toggle_ambient)
        {
        	torus_ka = new ColorType (0.0f, 0.6f, 0.7f);
        }
        else
        {
        	torus_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	torus_kd = new ColorType (0.2f, 0.2f, 0.2f);
        }
        else
        {
        	torus_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	torus_ks = new ColorType (0.8f, 0.8f, 0.8f);
        }
        else
        {
        	torus_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
		
		Material mat_torus = new Material(torus_ka, torus_kd, torus_ks, ns);

		mesh=torus.mesh;
		n=torus.get_n();
		m=torus.get_m();
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		// draw triangles for the current surface, using vertex colors
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_torus);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_torus);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
			}	
	    }
		
		/***********/
		/** Cube **/
		/***********/
		float cube_size = (float)150.0;
		float cube_x = (float) 630.0;
		float cube_y = (float) 220.0;
		float cube_z = (float) 0.0;
		CubeFront cube_front = new CubeFront(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeBack cube_back = new CubeBack(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeTop cube_top = new CubeTop(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeBottom cube_bottom = new CubeBottom(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeRight cube_right = new CubeRight(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeLeft cube_left = new CubeLeft(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		
		// Torus material and properties
        ColorType cube_ka;
        ColorType cube_kd;
        ColorType cube_ks;
        
        if (toggle_ambient)
        {
        	cube_ka = new ColorType (0.2f, 0.7f, 0.2f);
        }
        else
        {
        	cube_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	cube_kd = new ColorType (0.2f, 0.2f, 0.2f);
        }
        else
        {
        	cube_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	cube_ks = new ColorType (0.8f, 0.8f, 0.8f);
        }
        else
        {
        	cube_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
		
		Material mat_cube = new Material(cube_ka, cube_kd, cube_ks, ns);

		mesh=cube_front.mesh;
		n=cube_front.get_n();
		m=cube_front.get_m();
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		// draw triangles for the current surface, using vertex colors
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
			}	
	    }
		
		mesh=cube_back.mesh;
		n=cube_back.get_n();
		m=cube_back.get_m();
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		// draw triangles for the current surface, using vertex colors
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
			}	
	    }
		
		mesh=cube_top.mesh;
		n=cube_top.get_n();
		m=cube_top.get_m();
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		// draw triangles for the current surface, using vertex colors
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
			}	
	    }
		
		mesh=cube_bottom.mesh;
		n=cube_bottom.get_n();
		m=cube_bottom.get_m();
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		// draw triangles for the current surface, using vertex colors
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
			}	
	    }

		mesh=cube_right.mesh;
		n=cube_right.get_n();
		m=cube_right.get_m();
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		// draw triangles for the current surface, using vertex colors
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		mesh=cube_left.mesh;
		n=cube_left.get_n();
		m=cube_left.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
	}

	/** Test case 2 **/
	void shadeTest2(boolean doSmooth){
		// the simple example scene includes one sphere and one torus
		
		DepthBuffer depth_buffer = new DepthBuffer(buff.getHeight(),buff.getWidth());
		
		AmbientLight amb_light = new AmbientLight(new ColorType (1.0f, 1.0f, 1.0f));
        
        ArrayList<InfiniteLight> inf_lights = new ArrayList<InfiniteLight>();
        
        ArrayList<PointLight> pt_lights = new ArrayList<PointLight>();
        PointLight pt_light_1 = new PointLight (new ColorType(0.0f, 1.0f, 0.0f), new Point3D (50.0f, 50.0f, 100.0f));
        PointLight pt_light_2 = new PointLight (new ColorType(0.75f, 0.25f, 1.0f), new Point3D (500.0f, 700.0f, 250.0f));
        
        // Toggle green point light
        if (Light_1)
        {
        	pt_lights.add(pt_light_1);
        }
        else
        {
        	pt_lights.remove(pt_light_1);
        }
        // Toggle purple point light
        if (Light_2)
        {
        	pt_lights.add(pt_light_2);
        }
        else
        {
        	pt_lights.remove(pt_light_2);
        }
        
        SpotLight spot_atten = new SpotLight (new ColorType(1.0f, 1.0f, 1.0f), new Point3D (280.0f, 384.0f, 0.0f), new Point3D (1.0f, 0.0f, 0.0f), 0.0f);
        float spot_atten_a0 = 0.1f;
        float spot_atten_a1 = 0.1f;
        float spot_atten_a2 = 0.1f;
        
        float spot_atten_al = 2.0f;
        
        Point3D view_vector = new Point3D((float)0.0,(float)0.0,(float)1.0);
        
        Point3D triangle_normal = new Point3D();
        
        Mesh3D mesh;
            
		int i, j, n, m;
		
		Point3D v0,v1, v2, n0, n1, n2;
		
		Point3D[] tri = {new Point3D(), new Point3D(), new Point3D()};
		
		/************/
		/** Sphere **/
        /************/
		float sphere_radius_x = (float)60.0;
		float sphere_radius_y = (float)60.0;
		float sphere_radius_z = (float)60.0;
        Ellipsoid3D sphere = new Ellipsoid3D((float)250.0, (float)220.0, (float)0.0, (float)1.5*sphere_radius_x, (float)1.5*sphere_radius_y, (float)1.5*sphere_radius_z, Nsteps, Nsteps);
        
        // Sphere material and properties
        ColorType sphere_ka;
        ColorType sphere_kd;
        ColorType sphere_ks;
        
        if (toggle_ambient)
        {
        	sphere_ka = new ColorType (0.4f, 0.4f, 0.4f);
        }
        else
        {
        	sphere_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	 sphere_kd = new ColorType (1.0f, 1.0f, 1.0f);
        }
        else
        {
        	sphere_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	sphere_ks = new ColorType (0.1f, 0.1f, 0.1f);
        }
        else
        {
        	sphere_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
        
        Material mat_sphere = new Material(sphere_ka, sphere_kd, sphere_ks, ns);
		
		mesh=sphere.mesh;
		n=sphere.get_n();
		m=sphere.get_m();
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		// draw triangles for the current surface, using vertex colors
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_sphere);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading: 
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_sphere);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
	    
		/************/
		/** Ellipsoid **/
        /************/
		float ellip_radius_x = (float)60.0;
		float ellip_radius_y = (float)50.0;
		float ellip_radius_z = (float)80.0;
        Ellipsoid3D ellipsoid = new Ellipsoid3D((float)430.0, (float)400.0, (float)0.0, (float)1.5*ellip_radius_x, (float)1.5*ellip_radius_y, (float)1.5*ellip_radius_z, Nsteps, Nsteps);
        
        // Ellipsoid material and properties
        ColorType ellip_ka;
        ColorType ellip_kd;
        ColorType ellip_ks;
        
        if (toggle_ambient)
        {
        	ellip_ka = new ColorType (0.8f, 0.8f, 0.2f);
        }
        else
        {
        	ellip_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	ellip_kd = new ColorType (0.1f, 0.1f, 0.1f);
        }
        else
        {
        	ellip_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	ellip_ks = new ColorType (0.1f, 0.1f, 0.1f);
        }
        else
        {
        	ellip_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
        
        Material mat_ellip = new Material(ellip_ka, ellip_kd, ellip_ks, ns);
		
		mesh=ellipsoid.mesh;
		n=ellipsoid.get_n();
		m=ellipsoid.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_ellip);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_ellip);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		/**************/
		/** Cylinder **/
		/**************/
		
		float cyl_radius_x = (float)60.0;
		float cyl_radius_y = (float)60.0;
		float cyl_radius_z = (float)30.0;
		float cyl_z_end = (float)100.0;
		float cyl_center_x = (float)250.0;
		float cyl_center_y = (float)600.0;
		float cyl_center_z = (float)0.0;
		
		Cylinder3D cylinder = new Cylinder3D(cyl_center_x, cyl_center_y, cyl_center_z, (float)1.5*cyl_radius_x, (float)1.5*cyl_radius_y, (float)1.5*cyl_radius_z, (float) 1.5 * cyl_z_end, Nsteps, Nsteps);
		CylinderFront cyl_front = new CylinderFront(cyl_center_x, cyl_center_y, cyl_center_z, (float)1.5*cyl_radius_x, (float)1.5*cyl_radius_y, (float) 1.5*cyl_z_end, 3, Nsteps);
		CylinderBack cyl_back = new CylinderBack(cyl_center_x, cyl_center_y, cyl_center_z, (float)1.5*cyl_radius_x, (float)1.5*cyl_radius_y, (float) 1.5*cyl_z_end, 3, Nsteps);
        
        // Cylinder material and properties
        ColorType cyl_ka;
        ColorType cyl_kd;
        ColorType cyl_ks;
        
        if (toggle_ambient)
        {
        	cyl_ka = new ColorType (0.3f, 0.0f, 0.5f);
        }
        else
        {
        	cyl_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	cyl_kd = new ColorType (0.2f, 0.2f, 0.2f);
        }
        else
        {
        	cyl_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	cyl_ks = new ColorType (0.8f, 0.8f, 0.8f);
        }
        else
        {
        	cyl_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
        
        Material mat_cyl = new Material(cyl_ka, cyl_kd, cyl_ks, ns);
		
		mesh=cylinder.mesh;
		n=cylinder.get_n();
		m=cylinder.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
			}
	    }
		
		mesh=cyl_front.mesh;
		n=cyl_front.get_n();
		m=cyl_front.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
		
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}
	    }
		
		mesh=cyl_back.mesh;
		n=cyl_back.get_n();
		m=cyl_back.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
		
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}
	    }
		
		/***********/
		/** Torus **/
		/***********/
		float torus_radius_axial = (float)50.0;
		float torus_radius = (float)25.0;
		Torus3D torus = new Torus3D((float)630.0, (float)600.0, (float)0.0,
								(float)1.5*torus_radius_axial, (float)1.5*torus_radius,
								Nsteps, Nsteps);
		
		// Torus material and properties
        ColorType torus_ka;
        ColorType torus_kd;
        ColorType torus_ks;
        
        if (toggle_ambient)
        {
        	torus_ka = new ColorType (0.0f, 0.6f, 0.7f);
        }
        else
        {
        	torus_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	torus_kd = new ColorType (1.0f, 1.0f, 1.0f);
        }
        else
        {
        	torus_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	torus_ks = new ColorType (1.0f, 1.0f, 1.0f);
        }
        else
        {
        	torus_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
		
		Material mat_torus = new Material(torus_ka, torus_kd, torus_ks, ns);

		mesh=torus.mesh;
		n=torus.get_n();
		m=torus.get_m();
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		// draw triangles for the current surface, using vertex colors
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_torus);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_torus);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		/***********/
		/** Cube **/
		/***********/
		float cube_size = (float)150.0;
		float cube_x = (float) 630.0;
		float cube_y = (float) 220.0;
		float cube_z = (float) 0.0;
		CubeFront cube_front = new CubeFront(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeBack cube_back = new CubeBack(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeTop cube_top = new CubeTop(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeBottom cube_bottom = new CubeBottom(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeRight cube_right = new CubeRight(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeLeft cube_left = new CubeLeft(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		
		// Torus material and properties
        ColorType cube_ka;
        ColorType cube_kd;
        ColorType cube_ks;
        
        if (toggle_ambient)
        {
        	cube_ka = new ColorType (0.2f, 0.7f, 0.2f);
        }
        else
        {
        	cube_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	cube_kd = new ColorType (0.5f, 0.5f, 0.5f);
        }
        else
        {
        	cube_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	cube_ks = new ColorType (0.5f, 0.5f, 0.5f);
        }
        else
        {
        	cube_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
		
		Material mat_cube = new Material(cube_ka, cube_kd, cube_ks, ns);

		mesh=cube_front.mesh;
		n=cube_front.get_n();
		m=cube_front.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		mesh=cube_back.mesh;
		n=cube_back.get_n();
		m=cube_back.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		mesh=cube_top.mesh;
		n=cube_top.get_n();
		m=cube_top.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		mesh=cube_bottom.mesh;
		n=cube_bottom.get_n();
		m=cube_bottom.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }

		mesh=cube_right.mesh;
		n=cube_right.get_n();
		m=cube_right.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		mesh=cube_left.mesh;
		n=cube_left.get_n();
		m=cube_left.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
	}
	
	/** Test case 3 **/
	void shadeTest3(boolean doSmooth){
		// the simple example scene includes one sphere and one torus
		
		DepthBuffer depth_buffer = new DepthBuffer(buff.getHeight(),buff.getWidth());
		
		AmbientLight amb_light = new AmbientLight(new ColorType (1.0f, 0.7f, 0.0f));
        
        ArrayList<InfiniteLight> inf_lights = new ArrayList<InfiniteLight>();
        InfiniteLight inf_light_1 = new InfiniteLight (new ColorType(1.0f, 1.0f, 1.0f), new Point3D(1.0f, 0.0f, 0.0f));
        InfiniteLight inf_light_2 = new InfiniteLight (new ColorType(1.0f, 0.2f, 0.2f), new Point3D(-1.0f, 1.0f, -1.0f));
        
        // Toggle white infinite light
        if (Light_1)
        {
        	inf_lights.add(inf_light_1);
        }
        else
        {
        	inf_lights.remove(inf_light_1);
        }
        
        // Toggle red infinite light
        if (Light_2)
        {
        	inf_lights.add(inf_light_2);
        }
        else
        {
        	inf_lights.remove(inf_light_2);
        }
        
        ArrayList<PointLight> pt_lights = new ArrayList<PointLight>();
        PointLight pt_light_1 = new PointLight (new ColorType(0.0f, 1.0f, 0.0f), new Point3D (450.0f, 700.0f, 20.0f));
        
        // Toggle green point light
        if (Light_3)
        {
        	pt_lights.add(pt_light_1);
        }
        else
        {
        	pt_lights.remove(pt_light_1);
        }
        
        SpotLight spot_atten = new SpotLight (new ColorType(1.0f, 1.0f, 1.0f), new Point3D (280.0f, 384.0f, 0.0f), new Point3D (1.0f, 0.0f, 0.0f), 0.0f);
        float spot_atten_a0 = 0.1f;
        float spot_atten_a1 = 0.1f;
        float spot_atten_a2 = 0.1f;
        
        float spot_atten_al = 2.0f;
        
        Point3D view_vector = new Point3D((float)0.0,(float)0.0,(float)1.0);
        
        Point3D triangle_normal = new Point3D();
        
        Mesh3D mesh;
            
		int i, j, n, m;
		
		Point3D v0,v1, v2, n0, n1, n2;
		
		Point3D[] tri = {new Point3D(), new Point3D(), new Point3D()};
		
		/************/
		/** Sphere **/
        /************/
		float sphere_radius_x = (float)60.0;
		float sphere_radius_y = (float)60.0;
		float sphere_radius_z = (float)60.0;
        Ellipsoid3D sphere = new Ellipsoid3D((float)250.0, (float)220.0, (float)0.0, (float)1.5*sphere_radius_x, (float)1.5*sphere_radius_y, (float)1.5*sphere_radius_z, Nsteps, Nsteps);
        
        // Sphere material and properties
        ColorType sphere_ka;
        ColorType sphere_kd;
        ColorType sphere_ks;
        
        if (toggle_ambient)
        {
        	sphere_ka = new ColorType (0.4f, 0.4f, 0.4f);
        }
        else
        {
        	sphere_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	 sphere_kd = new ColorType (0.4f, 0.4f, 0.4f);
        }
        else
        {
        	sphere_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	sphere_ks = new ColorType (0.8f, 0.8f, 0.8f);
        }
        else
        {
        	sphere_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
        
        Material mat_sphere = new Material(sphere_ka, sphere_kd, sphere_ks, ns);
		
		mesh=sphere.mesh;
		n=sphere.get_n();
		m=sphere.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_sphere);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading: 
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_sphere);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_sphere, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
	    
		/************/
		/** Ellipsoid **/
        /************/
		float ellip_radius_x = (float)60.0;
		float ellip_radius_y = (float)50.0;
		float ellip_radius_z = (float)80.0;
        Ellipsoid3D ellipsoid = new Ellipsoid3D((float)430.0, (float)400.0, (float)0.0, (float)1.5*ellip_radius_x, (float)1.5*ellip_radius_y, (float)1.5*ellip_radius_z, Nsteps, Nsteps);
        
        // Ellipsoid material and properties
        ColorType ellip_ka;
        ColorType ellip_kd;
        ColorType ellip_ks;
        
        if (toggle_ambient)
        {
        	ellip_ka = new ColorType (0.8f, 0.8f, 0.2f);
        }
        else
        {
        	ellip_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	ellip_kd = new ColorType (1.0f, 1.0f, 1.0f);
        }
        else
        {
        	ellip_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	ellip_ks = new ColorType (1.0f, 1.0f, 1.0f);
        }
        else
        {
        	ellip_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
        
        Material mat_ellip = new Material(ellip_ka, ellip_kd, ellip_ks, ns);
		
		mesh=ellipsoid.mesh;
		n=ellipsoid.get_n();
		m=ellipsoid.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_ellip);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_ellip);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_ellip, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_ellip, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		/**************/
		/** Cylinder **/
		/**************/
		
		float cyl_radius_x = (float)60.0;
		float cyl_radius_y = (float)60.0;
		float cyl_radius_z = (float)30.0;
		float cyl_z_end = (float)100.0;
		float cyl_center_x = (float)250.0;
		float cyl_center_y = (float)600.0;
		float cyl_center_z = (float)0.0;
		
		Cylinder3D cylinder = new Cylinder3D(cyl_center_x, cyl_center_y, cyl_center_z, (float)1.5*cyl_radius_x, (float)1.5*cyl_radius_y, (float)1.5*cyl_radius_z, (float) 1.5 * cyl_z_end, Nsteps, Nsteps);
		CylinderFront cyl_front = new CylinderFront(cyl_center_x, cyl_center_y, cyl_center_z, (float)1.5*cyl_radius_x, (float)1.5*cyl_radius_y, (float) 1.5*cyl_z_end, 3, Nsteps);
		CylinderBack cyl_back = new CylinderBack(cyl_center_x, cyl_center_y, cyl_center_z, (float)1.5*cyl_radius_x, (float)1.5*cyl_radius_y, (float) 1.5*cyl_z_end, 3, Nsteps);
        
        // Cylinder material and properties
        ColorType cyl_ka;
        ColorType cyl_kd;
        ColorType cyl_ks;
        
        if (toggle_ambient)
        {
        	cyl_ka = new ColorType (0.3f, 0.0f, 0.5f);
        }
        else
        {
        	cyl_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	cyl_kd = new ColorType (1.0f, 1.0f, 1.0f);
        }
        else
        {
        	cyl_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	cyl_ks = new ColorType (0.8f, 0.8f, 0.8f);
        }
        else
        {
        	cyl_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
        
        Material mat_cyl = new Material(cyl_ka, cyl_kd, cyl_ks, ns);
		
		mesh=cylinder.mesh;
		n=cylinder.get_n();
		m=cylinder.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					//SketchBase.drawPoint(buff, tri[0]);
					//SketchBase.drawPoint(buff, tri[1]);
					//SketchBase.drawPoint(buff, tri[2]); 
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
					//System.out.println("After sphere rendered: point (" + tri[0].x + ", " + tri[0].y + ") is " + SketchBase.get_z_at(tri[0].x, tri[0].y));
				}
			}
	    }
		
		mesh=cyl_front.mesh;
		n=cyl_front.get_n();
		m=cyl_front.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
		
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}
	    }
		
		mesh=cyl_back.mesh;
		n=cyl_back.get_n();
		m=cyl_back.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
		
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cyl);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cyl, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cyl, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}
	    }
		
		/***********/
		/** Torus **/
		/***********/
		float torus_radius_axial = (float)50.0;
		float torus_radius = (float)25.0;
		Torus3D torus = new Torus3D((float)630.0, (float)600.0, (float)0.0,
								(float)1.5*torus_radius_axial, (float)1.5*torus_radius,
								Nsteps, Nsteps);
		
		// Torus material and properties
        ColorType torus_ka;
        ColorType torus_kd;
        ColorType torus_ks;
        
        if (toggle_ambient)
        {
        	torus_ka = new ColorType (0.0f, 0.6f, 0.7f);
        }
        else
        {
        	torus_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	torus_kd = new ColorType (0.6f, 0.6f, 0.6f);
        }
        else
        {
        	torus_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	torus_ks = new ColorType (0.2f, 0.2f, 0.2f);
        }
        else
        {
        	torus_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
		
		Material mat_torus = new Material(torus_ka, torus_kd, torus_ks, ns);

		mesh=torus.mesh;
		n=torus.get_n();
		m=torus.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_torus);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * 
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 * 
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_torus);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_torus, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_torus, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		/***********/
		/** Cube **/
		/***********/
		float cube_size = (float)150.0;
		float cube_x = (float) 630.0;
		float cube_y = (float) 220.0;
		float cube_z = (float) 0.0;
		CubeFront cube_front = new CubeFront(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeBack cube_back = new CubeBack(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeTop cube_top = new CubeTop(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeBottom cube_bottom = new CubeBottom(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeRight cube_right = new CubeRight(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		CubeLeft cube_left = new CubeLeft(cube_x, cube_y, cube_z, (float) cube_size, Nsteps, Nsteps);
		
		// Torus material and properties
        ColorType cube_ka;
        ColorType cube_kd;
        ColorType cube_ks;
        
        if (toggle_ambient)
        {
        	cube_ka = new ColorType (0.2f, 0.7f, 0.2f);
        }
        else
        {
        	cube_ka = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_diffuse)
        {
        	cube_kd = new ColorType (0.5f, 0.5f, 0.5f);
        }
        else
        {
        	cube_kd = new ColorType (0.0f, 0.0f, 0.0f);
        }
        if (toggle_specular)
        {
        	cube_ks = new ColorType (0.5f, 0.5f, 0.5f);
        }
        else
        {
        	cube_ks = new ColorType (0.0f, 0.0f, 0.0f);
        }
		
		Material mat_cube = new Material(cube_ka, cube_kd, cube_ks, ns);

		mesh=cube_front.mesh;
		n=cube_front.get_n();
		m=cube_front.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		mesh=cube_back.mesh;
		n=cube_back.get_n();
		m=cube_back.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		mesh=cube_top.mesh;
		n=cube_top.get_n();
		m=cube_top.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		mesh=cube_bottom.mesh;
		n=cube_bottom.get_n();
		m=cube_bottom.get_m();
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		// draw triangles for the current surface, using vertex colors
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }

		mesh=cube_right.mesh;
		n=cube_right.get_n();
		m=cube_right.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					//SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], doSmooth);
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					//tri[2].c = tri[1].c = tri[0].c = inf_light.applyLight(mat_sphere, view_vector, triangle_normal);
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
		
		mesh=cube_left.mesh;
		n=cube_left.get_n();
		m=cube_left.get_m();
		
		mesh.rotateMesh(viewing_quaternion, viewing_center);
				
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					// Phong shading
					
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Spotlight w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;

					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j+1];
				
				triangle_normal = computeTriangleNormal(v0,v1,v2);
				
				if(view_vector.dotProduct(triangle_normal) > 0.0)  
				{	
					// flat shading
					if (renderer == 0)
					{
						n2 = n1 = n0 =  triangle_normal;
					}
					
					// Gouraud shading
					else if (renderer == 1)
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
					}
					
					// Phong shading
					else
					{
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
						/*
						 * MISSING: Phong shading implementation.
						 * Key event will properly switch to this setting,
						 * but due to the missing implementation, key 'P'
						 * will toggle to Gouraud shading instead.
						 */
					}
					
					ColorType tri_amb = apply_Amb_Light(amb_light, mat_cube);
					
					/** Apply ambient light **/
					tri[0].c = tri_amb;
					tri[1].c = tri_amb;
					tri[2].c = tri_amb;
					
					/** Apply infinite light sources **/
					ColorType tri_0_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n0);
					ColorType tri_1_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n1);
					ColorType tri_2_inf = apply_Inf_Light(amb_light, inf_lights, mat_cube, view_vector, n2);
					tri[0].c = tri[0].c.combine(tri_0_inf);
					tri[1].c = tri[1].c.combine(tri_1_inf);
					tri[2].c = tri[2].c.combine(tri_2_inf);
					
					/** Ambient light + Point Light **/
					
					ColorType tri_0_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Pt_Light(amb_light, pt_lights, mat_cube, view_vector, n2, v2);
					tri[0].c = tri[0].c.combine(tri_0_pt);
					tri[1].c = tri[1].c.combine(tri_1_pt);
					tri[2].c = tri[2].c.combine(tri_2_pt);
					
					
					/** Ambient light + Point Light w/ attenuation (radial & angular) **/
					/*
					ColorType tri_0_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
																spot_atten_al, mat_sphere, view_vector, n0, v0);
					ColorType tri_1_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n1, v1);
					ColorType tri_2_pt = apply_Spot_Light_Atten(amb_light, spot_atten, spot_atten_a0, spot_atten_a1, spot_atten_a2,
							spot_atten_al, mat_sphere, view_vector, n2, v2);
					tri[0].c = tri_amb.combine(tri_0_pt);
					tri[1].c = tri_amb.combine(tri_1_pt);
					tri[2].c = tri_amb.combine(tri_2_pt);
					*/

					tri[0].x = (int)v0.x;
					tri[0].y = (int)v0.y;
					tri[0].z = (int)v0.z;
					tri[1].x = (int)v1.x;
					tri[1].y = (int)v1.y;
					tri[1].z = (int)v1.z;
					tri[2].x = (int)v2.x;
					tri[2].y = (int)v2.y;
					tri[2].z = (int)v2.z;
					
					SketchBase.drawTriangle3D(buff, depth_buffer, tri[0], tri[1], tri[2], doSmooth);
				}
			}	
	    }
	}
	
	
	// helper method that computes the unit normal to the plane of the triangle
	// degenerate triangles yield normal that is numerically zero
	private Point3D computeTriangleNormal(Point3D v0, Point3D v1, Point3D v2)
	{
		Point3D e0 = v1.minus(v2);
		Point3D e1 = v0.minus(v2);
		Point3D norm = e0.crossProduct(e1);
		
		if(norm.magnitude()>0.000001)
			norm.normalize();
		else 	// detect degenerate triangle and set its normal to zero
			norm.set((float)0.0,(float)0.0,(float)0.0);

		return norm;
	}

	private ColorType apply_Amb_Light(AmbientLight amb_L, Material mat)
	{
		return amb_L.applyLight(mat);
	}
	
	private ColorType apply_Inf_Light(AmbientLight amb_L, ArrayList<InfiniteLight> inf_L, Material mat, Point3D v, Point3D n)
	{
		// Calculate ambient lighting applied to the material
		//ColorType Ia = amb_L.applyLight(mat);
		// Calculate I_diff and I_spec for this each Infinite Light source
		ColorType overall_Light = new ColorType(0.0f, 0.0f, 0.0f);
		for (int i = 0; i < inf_L.size(); i++)
		{
			ColorType Idiff_Ispec = inf_L.get(i).applyLight(mat, v, n);
			overall_Light.r += Idiff_Ispec.r;
			overall_Light.g += Idiff_Ispec.g;
			overall_Light.b += Idiff_Ispec.b;
		}
		// Sum up the ambient light with all other light sources
		//ColorType res = Ia.combine(overall_Light);
		ColorType res = overall_Light;
		
		res.clamp();
		
		return res;
	}
	
	private ColorType apply_Pt_Light(AmbientLight amb_L, ArrayList<PointLight> pt_L, Material mat, Point3D v, Point3D n, Point3D ps)
	{
		// Calculate ambient lighting applied to the material
		//ColorType Ia = amb_L.applyLight(mat);
		// Calculate I_diff and I_spec for each Point Light source
		ColorType overall_Light = new ColorType(0.0f, 0.0f, 0.0f);
		for (int i = 0; i < pt_L.size(); i++)
		{
			ColorType Idiff_Ispec = pt_L.get(i).applyLight(mat, v, n, ps);
			overall_Light.r += Idiff_Ispec.r;
			overall_Light.g += Idiff_Ispec.g;
			overall_Light.b += Idiff_Ispec.b;
		}
		// Sum up the ambient light with all other light sources
		//ColorType res = Ia.combine(overall_Light);
		ColorType res = overall_Light;
		
		res.clamp();
		
		return res;
	}
	
	/** Apply a point light source to the point on a surface and account for radial attenuation and angular attenuation. **/
	private ColorType apply_Spot_Light_Atten(AmbientLight amb_L, SpotLight spot_L, float a0, float a1, float a2, float al, Material mat, Point3D v, Point3D n, Point3D ps)
	{
		// Calculate I_diff and I_spec for surface point ps from the point light source pt_L
		ColorType Idiff_Ispec = spot_L.applyLight(mat, v, n, ps, a0, a1, a2, al);
		
		ColorType res = new ColorType();
		res.r = Idiff_Ispec.r;
		res.g = Idiff_Ispec.g;
		res.b = Idiff_Ispec.b;
		
		res.clamp();
		
		return res;
	}
	
	// Enable all lights in the scene
	private void reset()
	{
		Light_1 = true;
		Light_2 = true;
		Light_3 = true;
		Light_4 = true;
		toggle_ambient = true;
		toggle_diffuse = true;
		toggle_specular = true;
	}
	
}