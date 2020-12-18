

import javax.media.opengl.*;
import com.jogamp.opengl.util.*;
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

public class Vivarium
{
  private Tank tank;
  //Arraylist to track which prey are alive
  private ArrayList<Fish> prey;
  private Fish fish1;
  private Fish fish2;
  private Fish fish3;
  private Fish fish4;
  //Arraylist to track the predators
  private ArrayList<Swordfish> predators;
  private Swordfish swordfish1;
  private Swordfish swordfish2;

  public Vivarium()
  {
    tank = new Tank( 15.0f, 15.0f, 15.0f );
    prey = new ArrayList<Fish>();
    fish1 = new Fish(1.2f, 0.5f, 0.5f, -0.5f);
    fish2 = new Fish(1.2f, 0.5f, -0.5f, 0.5f);
    fish3 = new Fish(1.2f, -0.5f, -0.5f, 0.5f);
    fish4 = new Fish(1.2f, -0.5f, 0.5f, -0.5f);
    prey.add(fish1);
    prey.add(fish2);
    prey.add(fish3);
    prey.add(fish4);
    predators = new ArrayList<Swordfish>();
    swordfish1 = new Swordfish(0.8f, -5.0f, -1.5f, 0.5f);
    swordfish2 = new Swordfish(0.8f, 3.0f, 0.5f, -1.0f);
    predators.add(swordfish1);
    predators.add(swordfish2);
  }
  
  public boolean collision(Fish fish, Swordfish swfish)
  {
	  // Euclidean distance between a specified fish and a specified swordfish.
	  double distance = Math.sqrt(Math.pow((fish.x - swfish.x), 2) + Math.pow((fish.y - swfish.y), 2) + Math.pow((fish.z - swfish.z), 2));
	  
	  // Distance between the two are shorter than their combined radii => collision.
	  if (distance < ((fish.radius * 0.3f * fish.scale) + (swfish.radius * 0.55f * swfish.scale)))
	  {
		  return true;
	  }
	  // No collision
	  else
	  {
		  return false;
	  }
  }

  public void init( GL2 gl )
  {
    tank.init( gl );
    fish1.init( gl );
    fish2.init( gl );
    fish3.init( gl );
    fish4.init( gl );
    swordfish1.init( gl );
    swordfish2.init( gl );
  }

  public void update( GL2 gl )
  {
    tank.update( gl );
    /** Update position of each fish in the vivarium. **/
    for (int i = 0; i < prey.size(); i++)
    {
    	Fish curr_prey = prey.get(i);
    	// If the prey hasn't been eaten yet, account for its gradient.
    	if (!curr_prey.collided) {
			double grad_x = 0;
			double grad_y = 0;
			double grad_z = 0;
			// Calculate the gradient of each prey to determine the best step for the prey to take to avoid the predators.
			for (int j = 0; j < predators.size(); j++)
			{
				Swordfish curr_pred = predators.get(j);
				
				// Gaussian calculation for the gradient of potential functions for each fish repulsed by its surrounding predators.
				double norm_sq = Math.pow(curr_prey.x - curr_pred.x,2) + Math.pow(curr_prey.y - curr_pred.y,2) + Math.pow(curr_prey.z - curr_pred.z,2);
				grad_x += (2 * (curr_prey.x - curr_pred.x) * 0.2) /  (Math.pow(norm_sq + 0.75, 2));	
				grad_y += (2 * (curr_prey.y - curr_pred.y) * 0.2) /  (Math.pow(norm_sq + 0.75, 2));	
				grad_z += (2 * (curr_prey.z - curr_pred.z) * 0.2) /  (Math.pow(norm_sq + 0.75, 2));	
			}
			
			// Gaussian calculation for the prey's potential repulsion from the walls of the vivarium tank.
			grad_x += (2 * (curr_prey.x - (-7.5)) * 0.15) / (Math.pow(Math.pow(curr_prey.x - (-7.5), 2) + 0.75, 2));
			grad_x += (2 * (curr_prey.x - 7.5) * 0.15) / (Math.pow(Math.pow(curr_prey.x - 7.5, 2) + 0.75, 2));
			grad_y += (2 * (curr_prey.y - (-7.5)) * 0.15) / (Math.pow(Math.pow(curr_prey.y - (-7.5), 2) + 0.75, 2));
			grad_y += (2 * (curr_prey.y - 7.5) * 0.15) / (Math.pow(Math.pow(curr_prey.y - 7.5, 2) + 0.75, 2));
			grad_z += (2 * (curr_prey.z - (-7.5)) * 0.15) / (Math.pow(Math.pow(curr_prey.z - (-7.5), 2) + 0.75, 2));
			grad_z += (2 * (curr_prey.z - 7.5) * 0.15) / (Math.pow(Math.pow(curr_prey.z - 7.5, 2) + 0.75, 2));

			prey.get(i).update( gl , grad_x, grad_y, grad_z);
    	}
    }
    /** Update the position of each swordfish in the vivarium. **/
    for (int i = 0; i < predators.size(); i++)
    {
    	Swordfish curr_pred = predators.get(i);
    	double grad_x = 0;
    	double grad_y = 0;
    	double grad_z = 0;
    	// Calculate and sum up the gradients of each prey and their predators to determine which step the prey should take to avoid the predators.
    	for (int j = 0; j < prey.size(); j++)
    	{
    		Fish curr_prey = prey.get(j);
    		// Gaussian calculation for the gradient of potential functions for each swordfish attracted to its surrounding prey.
    		double norm_sq = Math.pow(curr_pred.x - curr_prey.x,2) + Math.pow(curr_pred.y - curr_prey.y,2) + Math.pow(curr_pred.z - curr_prey.z,2);
    		grad_x += -(2 * (curr_pred.x - curr_prey.x) * 0.17) /  (Math.pow(norm_sq + 0.75, 2));	
    		grad_y += -(2 * (curr_pred.y - curr_prey.y) * 0.17) /  (Math.pow(norm_sq + 0.75, 2));
    		grad_z += -(2 * (curr_pred.z - curr_prey.z) * 0.17) /  (Math.pow(norm_sq + 0.75, 2));	
    		
    	}
    	// Gaussian calculation for the predator's potential repulsion from the walls of the vivarium tank.
    	grad_x += (2 * (curr_pred.x - (-7.5)) * 0.15) / (Math.pow(Math.pow(curr_pred.x - (-7.5), 2) + 0.75, 2));
    	grad_x += (2 * (curr_pred.x - 7.5) * 0.15) / (Math.pow(Math.pow(curr_pred.x - 7.5, 2) + 0.75, 2));
    	grad_y += (2 * (curr_pred.y - (-7.5)) * 0.15) / (Math.pow(Math.pow(curr_pred.y - (-7.5) + 0.75, 2), 2));
    	grad_y += (2 * (curr_pred.y - 7.5) * 0.15) / (Math.pow(Math.pow(curr_pred.y - 7.5, 2) + 0.75, 2));
    	grad_z += (2 * (curr_pred.z - (-7.5)) * 0.15) / (Math.pow(Math.pow(curr_pred.z - (-7.5), 2) + 0.75, 2));
    	grad_z += (2 * (curr_pred.z - 7.5) * 0.15) / (Math.pow(Math.pow(curr_pred.z - 7.5, 2) + 0.75, 2));
    	
    	curr_pred.update( gl , grad_x, grad_y, grad_z);
    	
    	// After a predator has moved, check if a prey has been eaten by a predator.
    	// If so, remove the prey from the tracked arraylist.
    	for (int j = 0; j < prey.size(); j++)
        {
        	Fish curr_prey = prey.get(j);
        	if (collision(curr_prey, curr_pred))
        	{
        		prey.get(j).collided = true;
        		prey.remove(curr_prey);
        	}
        }
    }
  }

  public void draw( GL2 gl )
  {
    tank.draw( gl );
    /** Draw each fish in the vivarium, unless they have been eaten already. **/
    if (!fish1.collided)
    {
    	fish1.draw(gl);
    }
    if (!fish2.collided)
    {
    	fish2.draw(gl);
    }
    if (!fish3.collided)
    {
    	fish3.draw(gl);
    }
    if (!fish4.collided)
    {
    	fish4.draw(gl);
    }
    /** Draw each swordfish in the vivarium. **/
    for (int i = 0; i < predators.size(); i++) {
    	predators.get(i).draw(gl);
    }
  }
}
