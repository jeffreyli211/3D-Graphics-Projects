import java.util.Arrays;
import java.util.Collections;
import java.util.List;

  /**
   * A spider leg which has a body joint, an inner middle joint, and outer middle joint and an outermost tip joint.
   * This class is a modified form of @author Jeffrey Finkelstein's Finger class.
   */

  public class Leg {
    /** The list of all the joints in this leg. */
	private final List<Component> joints;
	/** The outermost joint of the leg. */
	private final Component tipJoint;
	/** The outer middle joint of the leg. */
	private final Component outerMidJoint;
	/** The inner middle joint of the leg. */
	private final Component innerMidJoint;
	/** The body joint of the leg. */
	private final Component bodyJoint;

    /**
     * Instantiates this leg with the four specified joints.
     * 
     * @param bodyJoint
     *          The body joint of this leg.
     * @param innerMidJoint
     *          The inner middle joint of this leg.
     * @param outerMidJoint
     *          The outer middle joint of this leg.
     * @param tipjoint
     * 			The outermost joint of this leg.
     */
    public Leg(final Component bodyJoint, final Component innerMidJoint,
      final Component outerMidJoint, final Component tipJoint) {
      this.bodyJoint = bodyJoint;
      this.innerMidJoint = innerMidJoint;
      this.outerMidJoint = outerMidJoint;
      this.tipJoint = tipJoint;

      this.joints = Collections.unmodifiableList(Arrays.asList(this.bodyJoint,
    		  this.innerMidJoint, this.outerMidJoint, this.tipJoint));
    }

    /**
     * Gets an unmodifiable view of the list of the joints of this leg.
     * 
     * @return An unmodifiable view of the list of the joints of this leg.
     */
    List<Component> joints() {
      return this.joints;
    }
    
    /**
     * Gets the body joint of this leg.
     * 
     * @return The body joint of this leg.
     */
    Component bodyJoint() {
        return this.bodyJoint;
      }
    
    /**
     * Gets the outermost joint of this leg.
     * 
     * @return The distal joint of this leg.
     */
    Component tipJoint() {
      return this.tipJoint;
    }

    /**
     * Gets the outer middle joint of this leg.
     * 
     * @return The outer middle joint of this leg.
     */
    Component outerMidJoint() {
      return this.outerMidJoint;
    }

    /**
     * Gets the inner middle joint of this leg.
     * 
     * @return The inner middle joint of this leg.
     */
    Component innerMidJoint() {
        return this.innerMidJoint;
      }
  }