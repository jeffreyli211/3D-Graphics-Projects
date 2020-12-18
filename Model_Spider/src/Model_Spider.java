/**
 * Model_Spider.java - driver for the spider model simulation
 */

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;//for new version of gl
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;//for new version of gl
import com.jogamp.opengl.util.gl2.GLUT;//for new version of gl

public class Model_Spider extends JFrame implements GLEventListener, KeyListener,
    MouseListener, MouseMotionListener {

  /** The color for components which are selected for rotation. */
  public static final FloatColor ACTIVE_COLOR = FloatColor.RED;
  /** The radius of the abdomen. */
  public static final double ABDOMEN_RADIUS = 0.8;
  /** The default width of the created window. */
  public static final int DEFAULT_WINDOW_HEIGHT = 800;
  /** The default height of the created window. */
  public static final int DEFAULT_WINDOW_WIDTH = 800;
  /** The radius of each joint which comprises the leg. */
  public static final double LEG_RADIUS = 0.05;
  /** The radius of the thorax. */
  public static final double THORAX_RADIUS = 0.5;
  /** The color for components which are not selected for rotation. */
  public static final FloatColor INACTIVE_COLOR = FloatColor.ORANGE;
  /** The initial position of the top level component in the scene. */
  public static final Point3D INITIAL_POSITION = new Point3D(0, 0, -0.5);
  /** The height of the tip joint on each of the legs. */
  public static final double TIP_JOINT_HEIGHT = 0.1;
  /** The height of the inner middle joint on each of the legs. */
  public static final double INNERMID_JOINT_HEIGHT = 0.4;
  /** The height of the outer middle joint on each of the legs. */
  public static final double OUTERMID_JOINT_HEIGHT = 0.7;
  /** The height of the body joint on each of the legs. */
  public static final double BODY_JOINT_HEIGHT = 0.3;
  /** The angle by which to rotate the joint on user request to rotate. */
  public static final double ROTATION_ANGLE = 2.0;
  /** Randomly generated serial version UID. */
  private static final long serialVersionUID = -7060944143920496524L;

  /**
   * Runs the spider simulation in a single JFrame.
   * 
   * @param args
   *          This parameter is ignored.
   */
  public static void main(final String[] args) {
    new Model_Spider().animator.start();
  }

  /**
   * The animator which controls the framerate at which the canvas is animated.
   */
  final FPSAnimator animator;
  /** The canvas on which we draw the scene. */
  private final GLCanvas canvas;
  /** The capabilities of the canvas. */
  private final GLCapabilities capabilities = new GLCapabilities(null);
  /** The legs on the spider to be modeled. */
  private final Leg[] legs;
  /** The OpenGL utility object. */
  private final GLU glu = new GLU();
  /** The OpenGL utility toolkit object. */
  private final GLUT glut = new GLUT();
  /** The main body to be modeled (the part of the spider body where the legs connect). */
  private final Component thorax;
  private final Component thorax_bridge;
  /** The abdomen to be modeled. */
  private final Component abdomen;
  private final Component abdomen_bridge;
  /** The last x and y coordinates of the mouse press. */
  private int last_x = 0, last_y = 0;
  /** Whether the world is being rotated. */
  private boolean rotate_world = false;
  /** The axis around which to rotate the selected joints. */
  private Axis selectedAxis = Axis.X;
  /** The set of components which are currently selected for rotation. */
  private final Set<Component> selectedComponents = new HashSet<Component>(36);
  /**
   * The set of legs which have been selected for rotation.
   * 
   * Selecting a joint will only affect the joints in this set of selected
   * legs.
   **/
  private final Set<Leg> selectedLegs = new HashSet<Leg>(8);
  /** Whether the state of the model has been changed. */
  private boolean stateChanged = true;
  /**
   * The top level component in the scene which controls the positioning and
   * rotation of everything in the scene.
   */
  private final Component topLevelComponent;
  /** The quaternion which controls the rotation of the world. */
  private Quaternion viewing_quaternion = new Quaternion();
  /** The set of all components. */
  private final List<Component> components;
  
  /** Legs on the left side of the spider's body (from a head-on POV) */
  public static String L_INDEX_BODY_NAME = "left index body";
  public static String L_INDEX_INNERMID_NAME = "left index innermid";
  public static String L_INDEX_OUTERMID_NAME = "left index outermid";
  public static String L_INDEX_TIP_NAME = "left index tip";
  
  public static String L_MIDDLE_BODY_NAME = "left middle body";
  public static String L_MIDDLE_INNERMID_NAME = "left middle innermid";
  public static String L_MIDDLE_OUTERMID_NAME = "left middle outermid";
  public static String L_MIDDLE_TIP_NAME = "left middle tip";
  
  public static String L_RING_BODY_NAME = "left ring body";
  public static String L_RING_INNERMID_NAME = "left ring innermid";
  public static String L_RING_OUTERMID_NAME = "left ring outermid";
  public static String L_RING_TIP_NAME = "left ring tip";
  
  public static String L_BACK_BODY_NAME = "left back body";
  public static String L_BACK_INNERMID_NAME = "left back innermid";
  public static String L_BACK_OUTERMID_NAME = "left back outermid";
  public static String L_BACK_TIP_NAME = "left back tip";
  
  /** Legs on the right side of the spider's body (from a head-on POV) */
  public static String R_INDEX_BODY_NAME = "right index body";
  public static String R_INDEX_INNERMID_NAME = "right index innermid";
  public static String R_INDEX_OUTERMID_NAME = "right index outermid";
  public static String R_INDEX_TIP_NAME = "right index tip";
  
  public static String R_MIDDLE_BODY_NAME = "right middle body";
  public static String R_MIDDLE_INNERMID_NAME = "right middle innermid";
  public static String R_MIDDLE_OUTERMID_NAME = "right middle outermid";
  public static String R_MIDDLE_TIP_NAME = "right middle tip";
  
  public static String R_RING_BODY_NAME = "right ring body";
  public static String R_RING_INNERMID_NAME = "right ring innermid";
  public static String R_RING_OUTERMID_NAME = "right ring outermid";
  public static String R_RING_TIP_NAME = "right ring tip";
  
  public static String R_BACK_BODY_NAME = "right back body";
  public static String R_BACK_INNERMID_NAME = "right back innermid";
  public static String R_BACK_OUTERMID_NAME = "right back outermid";
  public static String R_BACK_TIP_NAME = "right back tip";
  
  /** 
   * The main parts of the spider's body.
   * The bridges connect the body parts together without gaps and acts as a point of rotation.
   */
  public static String THORAX_NAME = "thorax";
  public static String THORAX_BRIDGE_NAME = "thorax brigde";
  public static String ABDOMEN_NAME = "abdomen";
  public static String ABDOMEN_BRIDGE_NAME = "abdomen_bridge";
  public static String TOP_LEVEL_NAME = "top level";
  
  /**
   * Initializes the necessary OpenGL objects and adds a canvas to this JFrame.
   */
  public Model_Spider() {
    this.capabilities.setDoubleBuffered(true);

    this.canvas = new GLCanvas(this.capabilities);
    this.canvas.addGLEventListener(this);
    this.canvas.addMouseListener(this);
    this.canvas.addMouseMotionListener(this);
    this.canvas.addKeyListener(this);
    // this is true by default, but we just add this line to be explicit
    this.canvas.setAutoSwapBufferMode(true);
    this.getContentPane().add(this.canvas);

    // refresh the scene at 60 frames per second
    this.animator = new FPSAnimator(this.canvas, 60);

    this.setTitle("CS480/CS680 : Spider Simulator");
    this.setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
    
    // all tip joints
    final Component L_tip1 = new Component(new Point3D(0, 0,
    	OUTERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        TIP_JOINT_HEIGHT, this.glut), L_BACK_TIP_NAME);
    final Component L_tip2 = new Component(new Point3D(0, 0,
    	OUTERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        TIP_JOINT_HEIGHT, this.glut), L_RING_TIP_NAME);
    final Component L_tip3 = new Component(new Point3D(0, 0,
    	OUTERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        TIP_JOINT_HEIGHT, this.glut), L_MIDDLE_TIP_NAME);
    final Component L_tip4 = new Component(new Point3D(0, 0,
    	OUTERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        TIP_JOINT_HEIGHT, this.glut), L_INDEX_TIP_NAME);
    final Component R_tip1 = new Component(new Point3D(0, 0,
    	OUTERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        TIP_JOINT_HEIGHT, this.glut), R_BACK_TIP_NAME);
    final Component R_tip2 = new Component(new Point3D(0, 0,
    	OUTERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        TIP_JOINT_HEIGHT, this.glut), R_RING_TIP_NAME);
    final Component R_tip3 = new Component(new Point3D(0, 0,
    	OUTERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        TIP_JOINT_HEIGHT, this.glut), R_MIDDLE_TIP_NAME);
    final Component R_tip4 = new Component(new Point3D(0, 0,
    	OUTERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        TIP_JOINT_HEIGHT, this.glut), R_INDEX_TIP_NAME);
    
    // all outer middle joints
    final Component L_outermid1 = new Component(new Point3D(0, 0,
        INNERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        OUTERMID_JOINT_HEIGHT, this.glut), L_BACK_OUTERMID_NAME);
    final Component L_outermid2 = new Component(new Point3D(0, 0,
        INNERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        OUTERMID_JOINT_HEIGHT, this.glut), L_RING_OUTERMID_NAME);
    final Component L_outermid3 = new Component(new Point3D(0, 0,
        INNERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        OUTERMID_JOINT_HEIGHT, this.glut), L_MIDDLE_OUTERMID_NAME);
    final Component L_outermid4 = new Component(new Point3D(0, 0,
        INNERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        OUTERMID_JOINT_HEIGHT, this.glut), L_INDEX_OUTERMID_NAME);
    final Component R_outermid1 = new Component(new Point3D(0, 0,
        INNERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        OUTERMID_JOINT_HEIGHT, this.glut), R_BACK_OUTERMID_NAME);
    final Component R_outermid2 = new Component(new Point3D(0, 0,
        INNERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        OUTERMID_JOINT_HEIGHT, this.glut), R_RING_OUTERMID_NAME);
    final Component R_outermid3 = new Component(new Point3D(0, 0,
        INNERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        OUTERMID_JOINT_HEIGHT, this.glut), R_MIDDLE_OUTERMID_NAME);
    final Component R_outermid4 = new Component(new Point3D(0, 0,
        INNERMID_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        OUTERMID_JOINT_HEIGHT, this.glut), R_INDEX_OUTERMID_NAME);
    
    // all inner middle joints
    final Component L_innermid1 = new Component(new Point3D(0, 0,
        BODY_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        INNERMID_JOINT_HEIGHT, this.glut), L_BACK_INNERMID_NAME);
    final Component L_innermid2 = new Component(new Point3D(0, 0,
        BODY_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        INNERMID_JOINT_HEIGHT, this.glut), L_RING_INNERMID_NAME);
    final Component L_innermid3 = new Component(new Point3D(0, 0,
        BODY_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        INNERMID_JOINT_HEIGHT, this.glut), L_MIDDLE_INNERMID_NAME);
    final Component L_innermid4 = new Component(new Point3D(0, 0,
        BODY_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        INNERMID_JOINT_HEIGHT, this.glut), L_INDEX_INNERMID_NAME);
    final Component R_innermid1 = new Component(new Point3D(0, 0,
        BODY_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        INNERMID_JOINT_HEIGHT, this.glut), R_BACK_INNERMID_NAME);
    final Component R_innermid2 = new Component(new Point3D(0, 0,
        BODY_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        INNERMID_JOINT_HEIGHT, this.glut), R_RING_INNERMID_NAME);
    final Component R_innermid3 = new Component(new Point3D(0, 0,
        BODY_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        INNERMID_JOINT_HEIGHT, this.glut), R_MIDDLE_INNERMID_NAME);
    final Component R_innermid4 = new Component(new Point3D(0, 0,
        BODY_JOINT_HEIGHT), new RoundedCylinder(LEG_RADIUS,
        INNERMID_JOINT_HEIGHT, this.glut), R_INDEX_INNERMID_NAME);
    
    // all body joints, displaced by varied rotation and position from the thorax
    final Component L_body1 = new Component(new Point3D(-(THORAX_RADIUS-0.15), 0, THORAX_RADIUS-0.35),
    	new RoundedCylinder(LEG_RADIUS, BODY_JOINT_HEIGHT, this.glut), L_BACK_BODY_NAME);
    final Component L_body2 = new Component(new Point3D(-(THORAX_RADIUS-0.05), 0, THORAX_RADIUS-0.15),
        new RoundedCylinder(LEG_RADIUS, BODY_JOINT_HEIGHT, this.glut), L_RING_BODY_NAME);
    final Component L_body3 = new Component(new Point3D(-(THORAX_RADIUS-0.05), 0, THORAX_RADIUS+0.1),
        new RoundedCylinder(LEG_RADIUS, BODY_JOINT_HEIGHT, this.glut), L_MIDDLE_BODY_NAME);
    final Component L_body4 = new Component(new Point3D(-(THORAX_RADIUS-0.1), 0, THORAX_RADIUS+0.3),
        new RoundedCylinder(LEG_RADIUS, BODY_JOINT_HEIGHT, this.glut), L_INDEX_BODY_NAME);
    final Component R_body1 = new Component(new Point3D(THORAX_RADIUS-0.15, 0, THORAX_RADIUS-0.35),
    	new RoundedCylinder(LEG_RADIUS, BODY_JOINT_HEIGHT, this.glut), R_BACK_BODY_NAME);
    final Component R_body2 = new Component(new Point3D(THORAX_RADIUS-0.05, 0, THORAX_RADIUS-0.15),
        new RoundedCylinder(LEG_RADIUS, BODY_JOINT_HEIGHT, this.glut), R_RING_BODY_NAME);
    final Component R_body3 = new Component(new Point3D(THORAX_RADIUS-0.05, 0, THORAX_RADIUS+0.1),
        new RoundedCylinder(LEG_RADIUS, BODY_JOINT_HEIGHT, this.glut), R_MIDDLE_BODY_NAME);
    final Component R_body4 = new Component(new Point3D(THORAX_RADIUS-0.1, 0, THORAX_RADIUS+0.3),
        new RoundedCylinder(LEG_RADIUS, BODY_JOINT_HEIGHT, this.glut), R_INDEX_BODY_NAME);

    // put together the legs for easier selection by keyboard input later on
    this.legs = new Leg[] { new Leg(L_body1, L_innermid1, L_outermid1, L_tip1),
    	new Leg(L_body2, L_innermid2, L_outermid2, L_tip2),
    	new Leg(L_body3, L_innermid3, L_outermid3, L_tip3),
    	new Leg(L_body4, L_innermid4, L_outermid4, L_tip4),
    	new Leg(R_body1, R_innermid1, R_outermid1, R_tip1), 
    	new Leg(R_body2, R_innermid2, R_outermid2, R_tip2),
    	new Leg(R_body3, R_innermid3, R_outermid3, R_tip3),
    	new Leg(R_body4, R_innermid4, R_outermid4, R_tip4)};

    // the thorax, which models the main body joint
    this.thorax_bridge = new Component (new Point3D(0, 0, 0), new RoundedCylinder(0.15, THORAX_RADIUS, this.glut), THORAX_BRIDGE_NAME);
    this.thorax = new Component(new Point3D(0, 0, 0), new Body(
        THORAX_RADIUS, this.glut), THORAX_NAME);
    
    // the abdomen, which models the large back body portion of the spider.
    this.abdomen_bridge = new Component(new Point3D(0, 0, 0),
            new RoundedCylinder(0.15, ABDOMEN_RADIUS, this.glut),
            ABDOMEN_BRIDGE_NAME);
    this.abdomen = new Component(new Point3D(0, 0, 0), new Body(
            ABDOMEN_RADIUS, this.glut), ABDOMEN_NAME);

    // the top level component which provides an initial position and rotation
    // to the scene (but does not cause anything to be drawn)
    this.topLevelComponent = new Component(INITIAL_POSITION, TOP_LEVEL_NAME);

    this.topLevelComponent.addChild(this.thorax_bridge);
    // the thorax bridge and thorax are considered to be one moving piece.
    this.thorax_bridge.addChild(this.thorax);
    // connect the abdomen to the thorax
    this.thorax_bridge.addChild(this.abdomen_bridge);
    // the abdomen bridge and abdomen are considered to be one moving piece.
    this.abdomen_bridge.addChild(this.abdomen);
    // connect all legs to the thorax
    this.thorax.addChildren(L_body1, L_body2, L_body3, L_body4, R_body1, R_body2, R_body3, R_body4);
    L_body1.addChild(L_innermid1); L_innermid1.addChild(L_outermid1); L_outermid1.addChild(L_tip1);
    L_body2.addChild(L_innermid2); L_innermid2.addChild(L_outermid2); L_outermid2.addChild(L_tip2);
    L_body3.addChild(L_innermid3); L_innermid3.addChild(L_outermid3); L_outermid3.addChild(L_tip3);
    L_body4.addChild(L_innermid4); L_innermid4.addChild(L_outermid4); L_outermid4.addChild(L_tip4);
    R_body1.addChild(R_innermid1); R_innermid1.addChild(R_outermid1); R_outermid1.addChild(R_tip1);
    R_body2.addChild(R_innermid2); R_innermid2.addChild(R_outermid2); R_outermid2.addChild(R_tip2);
    R_body3.addChild(R_innermid3); R_innermid3.addChild(R_outermid3); R_outermid3.addChild(R_tip3);
    R_body4.addChild(R_innermid4); R_innermid4.addChild(R_outermid4); R_outermid4.addChild(R_tip4);

    // default angle of spider
    this.topLevelComponent.rotate(Axis.Y, 30);
    this.topLevelComponent.rotate(Axis.X, 20);
    
    // rotate the abdomen to be slightly tilted over the main thorax
    this.abdomen_bridge.rotate(Axis.X, 195);
    
    // rotate and bend the legs to be at a spider's idle position
    L_body1.rotate(Axis.X, -115);
    L_body2.rotate(Axis.X, -85);
    L_body3.rotate(Axis.X, -75);
    L_body4.rotate(Axis.X, -55);
    R_body1.rotate(Axis.X, -115);
    R_body2.rotate(Axis.X, -85);
    R_body3.rotate(Axis.X, -75); 
    R_body4.rotate(Axis.X, -55); 
    
    L_body1.rotate(Axis.Y, -45);
    L_body2.rotate(Axis.Y, -45);
    L_body3.rotate(Axis.Y, -45);
    L_body4.rotate(Axis.Y, -45);
    R_body1.rotate(Axis.Y, 45);
    R_body2.rotate(Axis.Y, 45);
    R_body3.rotate(Axis.Y, 45);
    R_body4.rotate(Axis.Y, 45);
    
    L_body1.rotate(Axis.Z, -45);
    L_innermid1.rotate(Axis.Y, -40);
    L_outermid1.rotate(Axis.Y, -45);
    L_tip1.rotate(Axis.Y, 45);
    R_body1.rotate(Axis.Z, -135);
    R_innermid1.rotate(Axis.Y, -40);
    R_outermid1.rotate(Axis.Y, -45);
    R_tip1.rotate(Axis.Y, 45);
    
    L_body2.rotate(Axis.Z, -5);
    L_innermid2.rotate(Axis.Y, -50);
    L_outermid2.rotate(Axis.Y, -40);
    L_tip2.rotate(Axis.Y, 40);
    R_body2.rotate(Axis.Z, -175);
    R_innermid2.rotate(Axis.Y, -50);
    R_outermid2.rotate(Axis.Y, -40);
    R_tip2.rotate(Axis.Y, 40);
    
    L_body3.rotate(Axis.Z, 15);
    L_innermid3.rotate(Axis.Y, -50);
    L_outermid3.rotate(Axis.Y, -40);
    L_tip3.rotate(Axis.Y, 40);
    R_body3.rotate(Axis.Z, 165);
    R_innermid3.rotate(Axis.Y, -50);
    R_outermid3.rotate(Axis.Y, -40);
    R_tip3.rotate(Axis.Y, 40);
    
    L_body4.rotate(Axis.Z, 50);
    L_innermid4.rotate(Axis.Y, -40);
    L_outermid4.rotate(Axis.Y, -40);
    L_tip4.rotate(Axis.Y, 40);
    R_body4.rotate(Axis.Z, 130);
    R_innermid4.rotate(Axis.Y, -40);
    R_outermid4.rotate(Axis.Y, -40);
    R_tip4.rotate(Axis.Y, 40);
    
    // set rotation limits for the thorax/thorax bridge
    this.thorax.setXPositiveExtent(0);
    this.thorax.setXNegativeExtent(-15);
    this.thorax.setYPositiveExtent(5);
    this.thorax.setYNegativeExtent(-5);
    this.thorax.setZPositiveExtent(0);
    this.thorax.setZNegativeExtent(0);
    this.thorax_bridge.setXPositiveExtent(360);
    this.thorax_bridge.setXNegativeExtent(-360);
    this.thorax_bridge.setYPositiveExtent(360);
    this.thorax_bridge.setYNegativeExtent(-360);
    this.thorax_bridge.setZPositiveExtent(180);
    this.thorax_bridge.setZNegativeExtent(-180);
    
    // set rotation limits for the abdomen/abdomen bridge
    this.abdomen.setXPositiveExtent(0);
    this.abdomen.setXNegativeExtent(0);
    this.abdomen.setYPositiveExtent(0);
    this.abdomen.setYNegativeExtent(0);
    this.abdomen.setZPositiveExtent(0);
    this.abdomen.setZNegativeExtent(0);
    this.abdomen_bridge.setXPositiveExtent(195);
    this.abdomen_bridge.setXNegativeExtent(180);
    this.abdomen_bridge.setYPositiveExtent(5);
    this.abdomen_bridge.setYNegativeExtent(-5);
    this.abdomen_bridge.setZPositiveExtent(0);
    this.abdomen_bridge.setZNegativeExtent(0);
    
    // set rotation limits for the legs
    // Only the body joints should be able to rotate about the X- and Z-axis
    L_body1.setXPositiveExtent(-100); L_body1.setXNegativeExtent(-130);
    R_body1.setXPositiveExtent(-100); R_body1.setXNegativeExtent(-130);
    L_body1.setZPositiveExtent(-15); L_body1.setZNegativeExtent(-75);
    R_body1.setZPositiveExtent(-105); R_body1.setZNegativeExtent(-165);
    
    L_body2.setXPositiveExtent(-70); L_body2.setXNegativeExtent(-100);
    R_body2.setXPositiveExtent(-70); R_body2.setXNegativeExtent(-100);
    L_body2.setZPositiveExtent(25); L_body2.setZNegativeExtent(-35);
    R_body2.setZPositiveExtent(-145); R_body2.setZNegativeExtent(-205);
    
    L_body3.setXPositiveExtent(-60); L_body3.setXNegativeExtent(-90);
    R_body3.setXPositiveExtent(-60); R_body3.setXNegativeExtent(-90);
    L_body3.setZPositiveExtent(45); L_body3.setZNegativeExtent(-15);
    R_body3.setZPositiveExtent(195); R_body3.setZNegativeExtent(135);
    
    L_body4.setXPositiveExtent(-40); L_body4.setXNegativeExtent(-70);
    R_body4.setXPositiveExtent(-40); R_body4.setXNegativeExtent(-70);
    L_body4.setZPositiveExtent(80); L_body4.setZNegativeExtent(20);
    R_body4.setZPositiveExtent(160); R_body4.setZNegativeExtent(100);
    
    // Y-extents
    for (final Component L_bodyJoint : Arrays.asList(L_body1, L_body2, L_body3, L_body4)) {
    	L_bodyJoint.setYPositiveExtent(-25);
    	L_bodyJoint.setYNegativeExtent(-100);
    }
    
    for (final Component R_bodyJoint : Arrays.asList(R_body1, R_body2, R_body3, R_body4)) {
    	R_bodyJoint.setYPositiveExtent(100);
    	R_bodyJoint.setYNegativeExtent(25);
    }
    
    for (final Component L_IMidJoint : Arrays.asList(L_innermid1, L_innermid2, L_innermid3, L_innermid4)) {
    	L_IMidJoint.setXPositiveExtent(0);
    	L_IMidJoint.setXNegativeExtent(0);
    	L_IMidJoint.setYPositiveExtent(0);
    	L_IMidJoint.setYNegativeExtent(-70);
    	L_IMidJoint.setZPositiveExtent(0);
    	L_IMidJoint.setZNegativeExtent(0);
    }
    
    for (final Component L_OMidJoint : Arrays.asList(L_outermid1, L_outermid2, L_outermid3, L_outermid4)) {
    	L_OMidJoint.setXPositiveExtent(0);
    	L_OMidJoint.setXNegativeExtent(0);
    	L_OMidJoint.setYPositiveExtent(0);
    	L_OMidJoint.setYNegativeExtent(-80);
    	L_OMidJoint.setZPositiveExtent(0);
    	L_OMidJoint.setZNegativeExtent(0);
    }
    
    for (final Component L_tipJoint : Arrays.asList(L_tip1, L_tip2, L_tip3, L_tip4)) {
    	L_tipJoint.setXPositiveExtent(0);
    	L_tipJoint.setXNegativeExtent(0);
    	L_tipJoint.setYPositiveExtent(90);
    	L_tipJoint.setYNegativeExtent(-90);
    	L_tipJoint.setZPositiveExtent(0);
    	L_tipJoint.setZNegativeExtent(0);
    }
    
    for (final Component R_IMidJoint : Arrays.asList(R_innermid1, R_innermid2, R_innermid3, R_innermid4)) {
    	R_IMidJoint.setXPositiveExtent(0);
    	R_IMidJoint.setXNegativeExtent(0);
    	R_IMidJoint.setYPositiveExtent(0);
    	R_IMidJoint.setYNegativeExtent(-70);
    	R_IMidJoint.setZPositiveExtent(0);
    	R_IMidJoint.setZNegativeExtent(0);
    }
    
    for (final Component R_OMidJoint : Arrays.asList(R_outermid1, R_outermid2, R_outermid3, R_outermid4)) {
    	R_OMidJoint.setXPositiveExtent(0);
    	R_OMidJoint.setXNegativeExtent(0);
    	R_OMidJoint.setYPositiveExtent(0);
    	R_OMidJoint.setYNegativeExtent(-80);
    	R_OMidJoint.setZPositiveExtent(0);
    	R_OMidJoint.setZNegativeExtent(0);
    }
    
    for (final Component R_tipJoint : Arrays.asList(R_tip1, R_tip2, R_tip3, R_tip4)) {
    	R_tipJoint.setXPositiveExtent(0);
    	R_tipJoint.setXNegativeExtent(0);
    	R_tipJoint.setYPositiveExtent(90);
    	R_tipJoint.setYNegativeExtent(-90);
    	R_tipJoint.setZPositiveExtent(0);
    	R_tipJoint.setZNegativeExtent(0);
    }
    
    // create the list of all the components for debugging purposes
    this.components = Arrays.asList(L_body1, L_body2, L_body3, L_body4,
    		L_innermid1, L_innermid2, L_innermid3, L_innermid4,
    		L_outermid1, L_outermid2, L_outermid3, L_outermid4,
    		L_tip1, L_tip2, L_tip3, L_tip4,
    		R_body1, R_body2, R_body3, R_body4,
    		R_innermid1, R_innermid2, R_innermid3, R_innermid4,
    		R_outermid1, R_outermid2, R_outermid3, R_outermid4,
    		R_tip1, R_tip2, R_tip3, R_tip4, this.thorax, this.thorax_bridge, this.abdomen, this.abdomen_bridge);
  }

  /**
   * Redisplays the scene containing the spider model.
   * 
   * @param drawable
   *          The OpenGL drawable object with which to create OpenGL models.
   */
  public void display(final GLAutoDrawable drawable) {
    final GL2 gl = (GL2)drawable.getGL();

    // clear the display
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    // from here on affect the model view
    gl.glMatrixMode(GL2.GL_MODELVIEW);

    // start with the identity matrix initially
    gl.glLoadIdentity();

    // rotate the world by the appropriate rotation quaternion
    gl.glMultMatrixf(this.viewing_quaternion.toMatrix(), 0);

	// update the position of the components which need to be updated
    // TODO only need to update the selected and JUST deselected components
    if (this.stateChanged) {
      this.topLevelComponent.update(gl);
      this.stateChanged = false;
    }

    // redraw the components
    this.topLevelComponent.draw(gl);
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param drawable
   *          This parameter is ignored.
   * @param modeChanged
   *          This parameter is ignored.
   * @param deviceChanged
   *          This parameter is ignored.
   */
  public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
      boolean deviceChanged) {
    // intentionally unimplemented
  }

  /**
   * Initializes the scene and model.
   * 
   * @param drawable
   *          {@inheritDoc}
   */
  public void init(final GLAutoDrawable drawable) {
    final GL2 gl = (GL2)drawable.getGL();

    // perform any initialization needed by the spider model
    this.topLevelComponent.initialize(gl);

    // initially draw the scene
    this.topLevelComponent.update(gl);

    // set up for shaded display of the spider
    final float light0_position[] = { 1, 1, 1, 0 };
    final float light0_ambient_color[] = { 0.25f, 0.25f, 0.25f, 1 };
    final float light0_diffuse_color[] = { 1, 1, 1, 1 };

    gl.glPolygonMode(GL.GL_FRONT, GL2.GL_FILL);
    gl.glEnable(GL2.GL_COLOR_MATERIAL);
    gl.glColorMaterial(GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);

    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    gl.glShadeModel(GL2.GL_SMOOTH);

    // set up the light source
    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0_position, 0);
    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light0_ambient_color, 0);
    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light0_diffuse_color, 0);

    // turn lighting and depth buffering on
    gl.glEnable(GL2.GL_LIGHTING);
    gl.glEnable(GL2.GL_LIGHT0);
    gl.glEnable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_NORMALIZE);
  }

  /**
   * Interprets key presses according to the following scheme:
   * 
   * up-arrow, down-arrow: increase/decrease rotation angle
   * 
   * @param key
   *          The key press event object.
   */
  public void keyPressed(final KeyEvent key) {
    switch (key.getKeyCode()) {
    case KeyEvent.VK_KP_UP:
    case KeyEvent.VK_UP:
      for (final Component component : this.selectedComponents) {
    	  // If the component is on a right side body joint, and we are rotating about the Y- or Z- axis,
    	  // we need to the reverse the rotation in order to allow the sides to mirror each other.
    	  if (component.name().contains("right") && component.name().contains("body") && this.selectedAxis == Axis.Y) {
    		  component.rotate(this.selectedAxis, -ROTATION_ANGLE);
    	  }
    	  else if (component.name().contains("right") && component.name().contains("body") && this.selectedAxis == Axis.Z) {
    		  component.rotate(this.selectedAxis, -ROTATION_ANGLE);
    	  }
    	  else {
    		  component.rotate(this.selectedAxis, ROTATION_ANGLE);
    	  }
      }
      this.stateChanged = true;
      break;
    case KeyEvent.VK_KP_DOWN:
    case KeyEvent.VK_DOWN:
      for (final Component component : this.selectedComponents) {
    	  if (component.name().contains("right") && component.name().contains("body") && this.selectedAxis == Axis.Y) {
    		  component.rotate(this.selectedAxis, ROTATION_ANGLE);
    	  }
    	  else if (component.name().contains("right") && component.name().contains("body") && this.selectedAxis == Axis.Z) {
    		  component.rotate(this.selectedAxis, ROTATION_ANGLE);
    	  }
    	  else {
    		  component.rotate(this.selectedAxis, -ROTATION_ANGLE);
    	  }
      }
      this.stateChanged = true;
      break;
    default:
      break;
    }
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param key
   *          This parameter is ignored.
   */
  public void keyReleased(final KeyEvent key) {
    // intentionally unimplemented
  }

  private final TestCases testCases = new TestCases();

  private void setModelState(final Map<String, Angled> state) {
	this.abdomen_bridge.setAngles(state.get(ABDOMEN_BRIDGE_NAME));
    this.thorax_bridge.setAngles(state.get(THORAX_BRIDGE_NAME));
    this.abdomen.setAngles(state.get(ABDOMEN_NAME));
    this.thorax.setAngles(state.get(THORAX_NAME));
    
    this.legs[0].bodyJoint().setAngles(state.get(L_BACK_BODY_NAME));
    this.legs[0].innerMidJoint().setAngles(state.get(L_BACK_INNERMID_NAME));
    this.legs[0].outerMidJoint().setAngles(state.get(L_BACK_OUTERMID_NAME));
    this.legs[0].tipJoint().setAngles(state.get(L_BACK_TIP_NAME));
    this.legs[1].bodyJoint().setAngles(state.get(L_RING_BODY_NAME));
    this.legs[1].innerMidJoint().setAngles(state.get(L_RING_INNERMID_NAME));
    this.legs[1].outerMidJoint().setAngles(state.get(L_RING_OUTERMID_NAME));
    this.legs[1].tipJoint().setAngles(state.get(L_RING_TIP_NAME));
    this.legs[2].bodyJoint().setAngles(state.get(L_MIDDLE_BODY_NAME));
    this.legs[2].innerMidJoint().setAngles(state.get(L_MIDDLE_INNERMID_NAME));
    this.legs[2].outerMidJoint().setAngles(state.get(L_MIDDLE_OUTERMID_NAME));
    this.legs[2].tipJoint().setAngles(state.get(L_MIDDLE_TIP_NAME));
    this.legs[3].bodyJoint().setAngles(state.get(L_INDEX_BODY_NAME));
    this.legs[3].innerMidJoint().setAngles(state.get(L_INDEX_INNERMID_NAME));
    this.legs[3].outerMidJoint().setAngles(state.get(L_INDEX_OUTERMID_NAME));
    this.legs[3].tipJoint().setAngles(state.get(L_INDEX_TIP_NAME));
    
    this.legs[4].bodyJoint().setAngles(state.get(R_BACK_BODY_NAME));
    this.legs[4].innerMidJoint().setAngles(state.get(R_BACK_INNERMID_NAME));
    this.legs[4].outerMidJoint().setAngles(state.get(R_BACK_OUTERMID_NAME));
    this.legs[4].tipJoint().setAngles(state.get(R_BACK_TIP_NAME));
    this.legs[5].bodyJoint().setAngles(state.get(R_RING_BODY_NAME));
    this.legs[5].innerMidJoint().setAngles(state.get(R_RING_INNERMID_NAME));
    this.legs[5].outerMidJoint().setAngles(state.get(R_RING_OUTERMID_NAME));
    this.legs[5].tipJoint().setAngles(state.get(R_RING_TIP_NAME));
    this.legs[6].bodyJoint().setAngles(state.get(R_MIDDLE_BODY_NAME));
    this.legs[6].innerMidJoint().setAngles(state.get(R_MIDDLE_INNERMID_NAME));
    this.legs[6].outerMidJoint().setAngles(state.get(R_MIDDLE_OUTERMID_NAME));
    this.legs[6].tipJoint().setAngles(state.get(R_MIDDLE_TIP_NAME));
    this.legs[7].bodyJoint().setAngles(state.get(R_INDEX_BODY_NAME));
    this.legs[7].innerMidJoint().setAngles(state.get(R_INDEX_INNERMID_NAME));
    this.legs[7].outerMidJoint().setAngles(state.get(R_INDEX_OUTERMID_NAME));
    this.legs[7].tipJoint().setAngles(state.get(R_INDEX_TIP_NAME));
    
    this.stateChanged = true;
  }

  /**
   * Interprets typed keys according to the following scheme:
   * 
   * 1 : toggle the left back leg active in rotation
   * 
   * 2 : toggle the left ring (2nd to back) leg active in rotation
   * 
   * 3 : toggle the left middle (2nd to front) leg active in rotation
   * 
   * 4 : toggle the left index (front) leg active in rotation
   * 
   * 5 : toggle the thorax active in rotation
   * 
   * 6 : toggle the abdomen active in rotation
   * 
   * 7 : toggle the right back leg active in rotation
   * 
   * 8 : toggle the right ring (2nd to back) leg active in rotation
   * 
   * 9 : toggle the right middle (2nd to front) leg active in rotation
   * 
   * 0 : toggle the right index (front) leg active in rotation
   * 
   * X : use the X axis rotation at the active joint(s)
   * 
   * Y : use the Y axis rotation at the active joint(s)
   * 
   * Z : use the Z axis rotation at the active joint(s)
   * 
   * A : select the body joint that connects to the thorax
   * 
   * S : select the inner middle joint
   * 
   * D : select the outer middle joint
   * 
   * F : select the outer most tip joint
   * 
   * C : resets the spider to the idle pose
   * 
   * T : set the spider's pose to the next test case
   * 
   * R : resets the view to the initial rotation
   * 
   * K : prints the angles of the leg joints for debugging purposes
   * 
   * Q, Esc : exits the program
   * 
   */

  public void keyTyped(final KeyEvent key) {
    switch (key.getKeyChar()) {
    case 'Q':
    case 'q':
    case KeyEvent.VK_ESCAPE:
      new Thread() {
        @Override
        public void run() {
          Model_Spider.this.animator.stop();
        }
      }.start();
      System.exit(0);
      break;

    // print the angles of the components
    case 'K':
    case 'k':
      printJoints();
      break;

    // resets to the idle stance
    case 'C':
    case 'c':
      this.setModelState(this.testCases.idle());
      break;

    // set the pose of the spider to the next test case
    case 'T':
    case 't':
      this.setModelState(this.testCases.next());
      break;

    // set the viewing quaternion to 0 rotation
    case 'R':
    case 'r':
      this.viewing_quaternion.reset();
      break;

    // Toggle which leg(s) are affected by the current rotation
    case '1':
      toggleSelection(this.legs[0]);
      break;
    case '2':
      toggleSelection(this.legs[1]);
      break;
    case '3':
      toggleSelection(this.legs[2]);
      break;
    case '4':
      toggleSelection(this.legs[3]);
      break;
        
    case '7':
      toggleSelection(this.legs[4]);
      break;
      
    case '8':
        toggleSelection(this.legs[5]);
        break;
        
    case '9':
        toggleSelection(this.legs[6]);
        break;
        
    case '0':
        toggleSelection(this.legs[7]);
        break;
        
    case '5':
        toggleSelection(this.thorax_bridge);
        toggleSelection(this.thorax);
        break;
        
    case '6':
    	toggleSelection(this.abdomen_bridge);
        toggleSelection(this.abdomen);
        break;

    // toggle which joints are affected by the current rotation
    case 'F':
    case 'f':
      for (final Leg leg : this.selectedLegs) {
        toggleSelection(leg.tipJoint());
      }
      break;
    case 'D':
    case 'd':
      for (final Leg leg : this.selectedLegs) {
        toggleSelection(leg.outerMidJoint());
      }
      break;
    case 'S':
    case 's':
    	for (final Leg leg : this.selectedLegs) {
            toggleSelection(leg.innerMidJoint());
          }
          break;
    case 'A':
    case 'a':
      for (final Leg leg : this.selectedLegs) {
        toggleSelection(leg.bodyJoint());
      }
      break;
      
    // change the axis of rotation at current active joint
    case 'X':
    case 'x':
      this.selectedAxis = Axis.X;
      break;
    case 'Y':
    case 'y':
      this.selectedAxis = Axis.Y;
      break;
    case 'Z':
    case 'z':
      this.selectedAxis = Axis.Z;
      break;
    default:
      break;
    }
  }

  /**
   * Prints the joints on the System.out print stream.
   */
  private void printJoints() {
    this.printJoints(System.out);
  }

  /**
   * Prints the joints on the specified PrintStream.
   * 
   * @param printStream
   *          The stream on which to print each of the components.
   */
  private void printJoints(final PrintStream printStream) {
    for (final Component component : this.components) {
      printStream.println(component);
    }
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param mouse
   *          This parameter is ignored.
   */
  public void mouseClicked(MouseEvent mouse) {
    // intentionally unimplemented
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
		final double magnitude = Math.sqrt(dx * dx + dy * dy);
		final float[] axis = magnitude == 0 ? new float[]{1,0,0}: // avoid dividing by 0
			new float[] { (float) (dy / magnitude),(float) (dx / magnitude), 0 };
	
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
	}
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param mouse
   *          This parameter is ignored.
   */
  public void mouseEntered(MouseEvent mouse) {
    // intentionally unimplemented
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param mouse
   *          This parameter is ignored.
   */
  public void mouseExited(MouseEvent mouse) {
    // intentionally unimplemented
  }

  /**
   * This method is intentionally unimplemented.
   * 
   * @param mouse
   *          This parameter is ignored.
   */
  public void mouseMoved(MouseEvent mouse) {
    // intentionally unimplemented
  }

  /**
   * Starts rotating the world if the left mouse button was released.
   * 
   * @param mouse
   *          The mouse press event object.
   */
  public void mousePressed(final MouseEvent mouse) {
    if (mouse.getButton() == MouseEvent.BUTTON1) {
      this.last_x = mouse.getX();
      this.last_y = mouse.getY();
      this.rotate_world = true;
    }
  }

  /**
   * Stops rotating the world if the left mouse button was released.
   * 
   * @param mouse
   *          The mouse release event object.
   */
  public void mouseReleased(final MouseEvent mouse) {
    if (mouse.getButton() == MouseEvent.BUTTON1) {
      this.rotate_world = false;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @param drawable
   *          {@inheritDoc}
   * @param x
   *          {@inheritDoc}
   * @param y
   *          {@inheritDoc}
   * @param width
   *          {@inheritDoc}
   * @param height
   *          {@inheritDoc}
   */
  public void reshape(final GLAutoDrawable drawable, final int x, final int y,
      final int width, final int height) {
    final GL2 gl = (GL2)drawable.getGL();

    // prevent division by zero by ensuring window has height 1 at least
    final int newHeight = Math.max(1, height);

    // compute the aspect ratio
    final double ratio = (double) width / newHeight;

    // reset the projection coordinate system before modifying it
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();

    // set the viewport to be the entire window
    gl.glViewport(0, 0, width, newHeight);

    // set the clipping volume
    this.glu.gluPerspective(25, ratio, 0.1, 100);

    // camera positioned at (0,0,6), look at point (0,0,0), up vector (0,1,0)
    this.glu.gluLookAt(0, 0, 12, 0, 0, 0, 0, 1, 0);

    // switch back to model coordinate system
    gl.glMatrixMode(GL2.GL_MODELVIEW);
  }

  private void toggleSelection(final Component component) {
    if (this.selectedComponents.contains(component)) {
      this.selectedComponents.remove(component);
      component.setColor(INACTIVE_COLOR);
    } else {
      this.selectedComponents.add(component);
      component.setColor(ACTIVE_COLOR);
    }
    this.stateChanged = true;
  }

  private void toggleSelection(final Leg leg) {
    if (this.selectedLegs.contains(leg)) {
      this.selectedLegs.remove(leg);
      this.selectedComponents.removeAll(leg.joints());
      for (final Component joint : leg.joints()) {
        joint.setColor(INACTIVE_COLOR);
      }
    } else {
      this.selectedLegs.add(leg);
    }
    this.stateChanged = true;
  }

@Override
public void dispose(GLAutoDrawable drawable) {
	// TODO Auto-generated method stub
	
}
}
