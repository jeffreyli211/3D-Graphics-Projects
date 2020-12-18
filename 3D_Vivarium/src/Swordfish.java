/**
 * Swordfish.java - Model class to create 3D swordfish predators model to put in vivarium.
 */

import javax.media.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.gl2.GLUT;

import java.util.*;

/**
 * Name: Jeffrey Li
 * Class: CS 480
 * Programming assignment 3
 * Due date: November 3, 2020
 * Assignment objective: Develop a 3-D vivarium with polyhedral creatures moving around.
 * Requirements:
 * 		1. Construct two different creatures using polyhedral (solid) parts. Feel free to use routines provided
 * 		   with the previous assignment. You are also free to create your own basic parts, but they must be
 *		   polyhedral (solid). The creatures should have moving linkages of the basic parts. These linkages should
 *		   move back and forth in a periodic motion, as the creatures move about the vivarium.
 * 		2. Creatures and their parts are to be stored and manipulated as OpenGL display list objects.
 * 
 * 		3. Use OpenGL transforms to control the motion of each creature and its parts.
 * 
 * 		4. Creatures should face in the direction they are moving.
 * 
 * 		5. Creatures in the vivarium should react to where other creatures are and move accordingly. Your creatures
 * 		   can have a prey/predator relationship, a courtship/mating relationship, etc.
 * 
 * 		6. Your creatures live within the 3D "tank" of fixed width, height, and depth. At no point should your
 * 		   creatures go outside the 3D tank.
 * 
 */

public class Swordfish
{
  // The swordfish's main (frontal) body
  private int swfishBody_object;
  public float radius;
  // The swordfish's "sword"
  private int swfishSword_object;
  // The swordfish's pectoral fins
  private int swfishPecFinL_object;
  private int swfishPecFinR_object;
  private float pecFin_base;
  private float pecFin_height;
  // The swordfish's dorsal fin
  private int swfishDorsal_object;
  private float dorsal_base;
  private float dorsal_height;
  // The swordfish's back tail portion
  private int swfishTail_object;
  private float tail_base;
  private float tail_height;
  // The swordfish's tail fin
  private int swfishTailFin_object;
  private float tailFin_base;
  private float tailFin_height;
  
  // Characteristics of the swordfish, i.e. size and color
  public float scale;
  private float color_R;
  private float color_G;
  private float color_B;
  
  //Position of the swordfish
  public float x;
  public float y;
  public float z;
 
  // Fish's next position, also used to calculate angle of rotation to make the fish face its direction change
  private double grad_x;
  private double grad_y;
  private double grad_z;
  private float facing_angle;
  private float norm_x;		// Unit vector normal to the original ffv cross grad vector
  private float norm_y;
  private float norm_z;
 
  // Values of the fish's forward-facing unit vector
  public float ffv_x;
  public float ffv_y;
  public float ffv_z;
  
  
  // The angles of reference at which to rotate the swordfish's tail and tail fin
  private float tail_angle;
  private float fin_angle;
  /* 
   * Indicates the direction in which the swordfish is currently swaying its tail/tailfin
   * 1 = right
   * -1 = left
   */
  private int direction = 1;
  // Step counter to indicate which part of the animation step we are currently on
  private int step = 9;
  
  private static final int DEFAULT_SLICES = 36;
  public static final int DEFAULT_STACKS = 28;

  public Swordfish(float scale_, float x_, float y_, float z_)
  {
    radius = 1.0f;
    tail_base = 1.0f;		tail_height = 1.0f;
    tailFin_base = 1.0f;	tailFin_height = 0.5f;
    pecFin_base = 1.0f;		pecFin_height = 1.0f;
    dorsal_base = 1.0f;		dorsal_height = 1.0f;
    scale = scale_;
    
    Random rand = new Random();
    color_R = rand.nextFloat();	color_G = rand.nextFloat();	color_B = rand.nextFloat();
    
    x = x_;	y = y_;	z = z_;
    grad_x = 0;	grad_y = 0;	grad_z = 0;
    
    // Since the fish will always spawn facing parallel to the z-axis, its ffv is a unit vector along the z-axis (when translated to the origin).
    ffv_x = x;
    ffv_y = y;
    ffv_z = z-1;
    
    tail_angle = 0;
    fin_angle = 0;
  }

  public void init( GL2 gl )
  {
	GLUT glut = new GLUT();
	
	/** Create the body of the swordfish **/
    swfishBody_object = gl.glGenLists(1);
    gl.glNewList(swfishBody_object, GL2.GL_COMPILE );		// Establish the main swordfish body matrix
	   	gl.glPushMatrix();									// Save the initial transformation matrix that forms the main swordfish body
	   	gl.glScalef(0.3f, 0.55f, 1.0f);
	    glut.glutSolidSphere(radius, DEFAULT_SLICES, DEFAULT_STACKS);
	    gl.glPushMatrix();									// Save the initial transformation matrix that forms the face
    	gl.glTranslatef(0.0f, 0.0f, -0.8f);
    	gl.glScalef(0.6f, 0.6f, -0.5f);
    	glut.glutSolidCone(1.0f, 1.0f, DEFAULT_SLICES, DEFAULT_STACKS);
    	gl.glPopMatrix();									// Restore the initial transformation matrix that forms the face
	    gl.glPopMatrix();									// Restore the initial transformation matrix that forms the main swordfish body
    gl.glEndList();
    
    /** Create the swordfish's "sword" beak **/
    swfishSword_object = gl.glGenLists(1);
    gl.glNewList(swfishSword_object, GL2.GL_COMPILE);
	    gl.glPushMatrix();									// Save the initial transformation matrix that forms the "sword"
		gl.glTranslatef(0.0f, 0.1f, -0.8f);
		gl.glRotatef(-10.0f, 1.0f, 0.0f, 0.0f);
		gl.glScalef(0.08f, 0.10f, -1.25f);
		glut.glutSolidCone(1.0f, 1.5f, DEFAULT_SLICES, DEFAULT_STACKS);
		gl.glPopMatrix();									// Restore the initial transformation matrix that forms the "sword"
    gl.glEndList();

    /** Create the dorsal fin **/
    swfishDorsal_object = gl.glGenLists(1);
    gl.glNewList(swfishDorsal_object, GL2.GL_COMPILE);
    	gl.glPushMatrix();
    	gl.glTranslatef(0.0f, 0.4f, 0.0f);
    	gl.glRotatef(30.0f, 1.0f, 0.0f, 0.0f);
    	gl.glScalef(0.1f, 1.0f, 0.2f);
    	gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
    	glut.glutSolidCone(dorsal_base, dorsal_height, DEFAULT_SLICES, DEFAULT_STACKS);
    	gl.glPopMatrix();
    gl.glEndList();
    
    /** Create the left pectoral fin **/
    swfishPecFinL_object = gl.glGenLists(1);
    gl.glNewList(swfishPecFinL_object, GL2.GL_COMPILE);
    	gl.glPushMatrix();
    	gl.glTranslatef(-0.12f, -0.05f, 0.1f);
    	gl.glTranslatef(0.0f, 0.0f, -0.25f);
    	gl.glRotated(-25.0f, 0.0f, 0.0f, 1.0f);
    	gl.glRotated(70.0f, 1.0f, 0.0f, 0.0f);
    	gl.glTranslatef(0.0f, 0.0f, 0.25f);
    	gl.glScalef(0.02f, 0.15f, 0.8f);
    	glut.glutSolidCone(pecFin_base, pecFin_height, DEFAULT_SLICES, DEFAULT_STACKS);
    	gl.glPopMatrix();
    gl.glEndList();
    
    /** Right pectoral fin **/
    swfishPecFinR_object = gl.glGenLists(1);
    gl.glNewList(swfishPecFinR_object, GL2.GL_COMPILE);
    	gl.glPushMatrix();
    	gl.glTranslatef(0.12f, -0.05f, 0.1f);
    	gl.glTranslatef(0.0f, 0.0f, -0.25f);
    	gl.glRotated(25.0f, 0.0f, 0.0f, 1.0f);
    	gl.glRotated(70.0f, 1.0f, 0.0f, 0.0f);
    	gl.glTranslatef(0.0f, 0.0f, 0.25f);
    	gl.glScalef(0.02f, 0.15f, 0.8f);
    	glut.glutSolidCone(pecFin_base, pecFin_height, DEFAULT_SLICES, DEFAULT_STACKS);
    	gl.glPopMatrix();
    gl.glEndList();
    
    /** Create the tail **/
    swfishTail_object = gl.glGenLists(1);
    gl.glNewList(swfishTail_object, GL2.GL_COMPILE );		// Establish the fish tail matrix
    	gl.glPushMatrix();								// Save the initial transformation matrix that forms the fish tail
    	gl.glTranslatef(0.0f, 0.0f, radius-0.5f);
    	//gl.glScalef(0.16f, 0.25f, 1.0f);
    	gl.glScalef(0.26f, 0.48f, 2.0f);
    	glut.glutSolidCone(tail_base, tail_height, DEFAULT_SLICES, DEFAULT_STACKS);
    	gl.glPopMatrix();								// Restore the initial transformation matrix that forms the fish tail
    gl.glEndList();
    
    /** Create the tail fin **/
    swfishTailFin_object = gl.glGenLists(1);
    gl.glNewList(swfishTailFin_object, GL2.GL_COMPILE);	// Establish the tail fin matrix
    	gl.glPushMatrix();								// Save the initial transformation matrix that forms the tail fin
    	gl.glTranslatef(0.0f, 0.0f, (2 * tail_height) + 0.6f);
    	gl.glScalef(0.03f, 0.8f, -1.0f);
    	gl.glRotatef(-5.0f, 1.0f, 0.0f, 0.0f);
    	glut.glutSolidCone(tailFin_base, tailFin_height, DEFAULT_SLICES, DEFAULT_STACKS);
    	gl.glPopMatrix();								// Restore the initial transformation matrix that forms the tail fin
    gl.glEndList();
  }
  
  /** 
   * Updates facing_angle and the normal vector to allow for rotations of the swordfish to face in its moving direction.
   * Also updates the ffv to allow for the next change of rotation to be calculated correctly.
   */
  public void change_facing_dir()
  {
	  // Adjust the ffv to be coming out from the origin
	  ffv_x -= x;
	  ffv_y -= y;
	  ffv_z -= z;
	  
	  // Magnitude of the new ffv
	  float new_ffv_mag = (float) Math.sqrt(Math.abs((float) Math.pow(grad_x, 2) + (float) Math.pow(grad_y, 2) + (float) Math.pow(grad_z, 2)));
	  // Convert the new ffv into a unit vector
	  float new_ffv_x = (float) grad_x/new_ffv_mag;
	  float new_ffv_y = (float) grad_y/new_ffv_mag;
	  float new_ffv_z = (float) grad_z/new_ffv_mag;
	  
	  // Cross product of the original ffv with the new ffv
	  float old_cross_new_x = (ffv_y * new_ffv_z) - (ffv_z * new_ffv_y);
	  float old_cross_new_y = - ((ffv_x * new_ffv_z) - (ffv_z * new_ffv_x));
	  float old_cross_new_z = (ffv_x * new_ffv_y) - (ffv_y * new_ffv_x);
	  
	  // Update the normal vector that will be used to rotate the swordfish
	  norm_x = old_cross_new_x;
	  norm_y = old_cross_new_y;
	  norm_z = old_cross_new_z;
	  
	  float dot_prod = (ffv_x * new_ffv_x) + (ffv_y * new_ffv_y) + (ffv_z * new_ffv_z);
	  
	  // Update the angle of rotation that the swordfish will make to face in its moving direction
	  facing_angle = (float) Math.toDegrees(Math.acos(dot_prod));
	  
	  // Revert the ffv to its original, then update it.
	  ffv_x += x + grad_x;
	  ffv_y += y + grad_y;
	  ffv_z += z + grad_z;
  }
  
  public void update( GL gl, double delta_x, double delta_y, double delta_z )
  {
	  animateUpdate( gl );
	  /** First, update the step in direction that the swordfish will take **/
	  if (x <= -(7.5f - (0.3f * scale))) {
		  grad_x = -(7.5f - (0.3f * scale)) - (float) delta_x;
	  }
	  else if (x >= (7.5f - (0.3f * scale))) {
		  grad_x = 7.5f - (0.3f * scale) - (float) delta_x;
	  }
	  else {
		  grad_x = delta_x;
	  }
	  
	  if (y <= -(7.5f - (0.55f * scale))) {
		  grad_y = -(7.5f - (0.55f * scale)) - (float) delta_y;
	  }
	  else if (y >= (7.5f - (0.55f * scale))) {
		  grad_y = 7.5f - (0.55f * scale) - (float) delta_y;
	  }
	  else {
		  grad_y = delta_y;
	  }
	  
	  if (z <= -(7.5f - (1.0f * scale))) {
		  grad_z = -(7.5f - (1.0f * scale)) - (float) delta_z;
	  }
	  else if (z >= (7.5f - (1.0f * scale))) {
		  grad_z = 7.5f - (1.0f * scale) - (float) delta_z;
	  }
	  else {
		  grad_z = delta_z;
	  }
	  
	  /** Find the angle of rotation and the normal unit vector from the old ffv to the new ffv **/
	  change_facing_dir();
	  
	  /** Update the position of the fish to the new position **/
	  if (x <= -(7.5f - (0.3f * scale))) {
		  x = -(7.5f - (0.3f * scale) + (float) delta_x);
	  }
	  else if (x >= (7.5f - (0.3f * scale))) {
		  x = 7.5f - (0.3f * scale) + (float) delta_x;
	  }
	  else {
		  x += delta_x;
	  }
	  
	  if (y <= -(7.5f - (0.55f * scale))) {
		  y = -(7.5f - (0.55f * scale) + (float) delta_y);
	  }
	  else if (y >= (7.5f - (0.55f * scale))) {
		  y = 7.5f - (0.55f * scale) + (float) delta_y;
	  }
	  else {
		  y += delta_y;
	  }
	  
	  if (z <= -(7.5f - (1.0f * scale))) {
		  z = -(7.5f - (1.0f * scale) + (float) delta_z);
	  }
	  else if (z >= (7.5f - (1.0f * scale))) {
		  z = 7.5f - (1.0f * scale) + (float) delta_z;
	  }
	  else {
		  z += delta_z;
	  }
  }

  public void animateUpdate( GL gl )
  {
	  // Right direction
	  if (direction == 1)
	  {
		  if (step < 17)
		  {
			  tail_angle += 1;
			  fin_angle += 0.5;
			  step++;
		  }
		  else			// On final step of the right direction, start swaying left
		  {
			  tail_angle -= 1;
			  fin_angle -= 0.5;
			  step--;
			  direction = -1;	// Switch the direction
		  }
	  }
	  // Left direction
	  else
	  {
		  if (step > 1)
		  {
			  tail_angle -= 1;
			  fin_angle -= 0.5;
			  step--;
		  }
		  else			// On final step of the left direction, start swaying right
		  {
			  tail_angle += 1;
			  fin_angle += 0.5;
			  step++;
			  direction = 1;
		  }
	  }
  }

  public void draw( GL2 gl )
  {
    gl.glPushMatrix();
    gl.glPushAttrib( GL2.GL_CURRENT_BIT );
    
    // Rotates swordfish to face its moving direction.
    gl.glTranslatef(x, y, z);
    gl.glRotated(facing_angle, norm_x, norm_y, norm_z);
    gl.glTranslatef(-x, -y, -z);
    
    // Fish swims to its next position
    gl.glTranslatef(x, y, z);
    
    gl.glScalef(scale, scale, scale);
    gl.glColor3f(color_R, color_G, color_B);
    
    
    // Create the swordfish's body and attach its fins and sword
    gl.glCallList( swfishBody_object );
    gl.glCallList(swfishSword_object);
    gl.glCallList(swfishDorsal_object);
    gl.glCallList(swfishPecFinL_object);
    gl.glCallList(swfishPecFinR_object);
    
    gl.glTranslatef(0.0f, 0.0f, radius-0.5f);
    gl.glRotatef(tail_angle, 0.0f, 1.0f, 0.0f);			// Animate the tail of the swordfish to sway left and right
    gl.glTranslatef(0.0f, 0.0f, -(radius-0.5f));
    gl.glCallList(swfishTail_object);		// Create tail
    
    gl.glTranslatef(0.0f, 0.0f, (2 * tail_height) + 0.6f - tailFin_height);
    gl.glRotatef(fin_angle, 0.0f, 1.0f, 0.0f);			// Animate the tail fin of the swordfish to slightly sway left and right
    gl.glTranslatef(0.0f, 0.0f, -((2 * tail_height) + 0.6f - tailFin_height));
    gl.glCallList(swfishTailFin_object);	// Create tail fin
    
    gl.glPopAttrib();
    gl.glPopMatrix();
  }
}
