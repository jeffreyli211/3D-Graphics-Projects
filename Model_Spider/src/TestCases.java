import java.util.HashMap;
import java.util.Map;

/**
 * This class contains and cycles through 5 different test case poses for the spider model, along with one base idle pose:
 * 
 * idle : The base test case, where the spider is in a natural, idle position.
 * 
 * dead : The spider is posed as though it were no longer alive, with its legs contracted in and its body upside down.
 * 
 * attack : Posed in a defensive position, as if a predator were approaching it.
 * 
 * crawl : Posed as though the spider were taking a step forward, with its left side treading forward.
 * 
 * flatten : Posed with all of its legs extended outward, as if it was laying flat.
 * 
 * spiderman : Posed to mimic the iconic Marvel Spiderman logo. For reference: https://i.pinimg.com/736x/c3/5f/e1/c35fe1ba4d6980feedf98f12c5770b08.jpg
 */

public class TestCases extends CyclicIterator<Map<String, Angled>> {

  Map<String, Angled> idle() {
    return this.idle;
  }

  private final Map<String, Angled> idle;

  @SuppressWarnings("unchecked")
  TestCases() {
    this.idle = new HashMap<String, Angled>();
    final Map<String, Angled> dead = new HashMap<String, Angled>();
    final Map<String, Angled> attack = new HashMap<String, Angled>();
    final Map<String, Angled> crawl = new HashMap<String, Angled>();
    final Map<String, Angled> flatten = new HashMap<String, Angled>();
    final Map<String, Angled> spiderman = new HashMap<String, Angled>();

    super.add(idle, dead, attack, crawl, flatten, spiderman);
    
    // the idle test case
    idle.put(Model_Spider.TOP_LEVEL_NAME, new BaseAngled(20, 30, 0));
    idle.put(Model_Spider.ABDOMEN_BRIDGE_NAME, new BaseAngled(195, 0, 0));
    idle.put(Model_Spider.ABDOMEN_NAME, new BaseAngled(0, 0, 0));
    idle.put(Model_Spider.THORAX_BRIDGE_NAME, new BaseAngled(0, 0, 0));
    idle.put(Model_Spider.THORAX_NAME, new BaseAngled(0, 0, 0));
    
    idle.put(Model_Spider.L_BACK_BODY_NAME, new BaseAngled(-115, -45, -45));
    idle.put(Model_Spider.L_BACK_INNERMID_NAME, new BaseAngled(0, -40, 0));
    idle.put(Model_Spider.L_BACK_OUTERMID_NAME, new BaseAngled(0, -45, 0));
    idle.put(Model_Spider.L_BACK_TIP_NAME, new BaseAngled(0, 45, 0));
    idle.put(Model_Spider.R_BACK_BODY_NAME, new BaseAngled(-115, 45, -135));
    idle.put(Model_Spider.R_BACK_INNERMID_NAME, new BaseAngled(0, -40, 0));
    idle.put(Model_Spider.R_BACK_OUTERMID_NAME, new BaseAngled(0, -45, 0));
    idle.put(Model_Spider.R_BACK_TIP_NAME, new BaseAngled(0, 45, 0));
    
    idle.put(Model_Spider.L_RING_BODY_NAME, new BaseAngled(-85, -45, -5));
    idle.put(Model_Spider.L_RING_INNERMID_NAME, new BaseAngled(0, -50, 0));
    idle.put(Model_Spider.L_RING_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    idle.put(Model_Spider.L_RING_TIP_NAME, new BaseAngled(0, 40, 0));
    idle.put(Model_Spider.R_RING_BODY_NAME, new BaseAngled(-85, 45, -175));
    idle.put(Model_Spider.R_RING_INNERMID_NAME, new BaseAngled(0, -50, 0));
    idle.put(Model_Spider.R_RING_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    idle.put(Model_Spider.R_RING_TIP_NAME, new BaseAngled(0, 40, 0));
    
    idle.put(Model_Spider.L_MIDDLE_BODY_NAME, new BaseAngled(-75, -45, 15));
    idle.put(Model_Spider.L_MIDDLE_INNERMID_NAME, new BaseAngled(0, -50, 0));
    idle.put(Model_Spider.L_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    idle.put(Model_Spider.L_MIDDLE_TIP_NAME, new BaseAngled(0, 40, 0));
    idle.put(Model_Spider.R_MIDDLE_BODY_NAME, new BaseAngled(-75, 45, 165));
    idle.put(Model_Spider.R_MIDDLE_INNERMID_NAME, new BaseAngled(0, -50, 0));
    idle.put(Model_Spider.R_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    idle.put(Model_Spider.R_MIDDLE_TIP_NAME, new BaseAngled(0, 40, 0));
    
    idle.put(Model_Spider.L_INDEX_BODY_NAME, new BaseAngled(-55, -45, 50));
    idle.put(Model_Spider.L_INDEX_INNERMID_NAME, new BaseAngled(0, -40, 0));
    idle.put(Model_Spider.L_INDEX_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    idle.put(Model_Spider.L_INDEX_TIP_NAME, new BaseAngled(0, 40, 0));
    idle.put(Model_Spider.R_INDEX_BODY_NAME, new BaseAngled(-55, 45, 130));
    idle.put(Model_Spider.R_INDEX_INNERMID_NAME, new BaseAngled(0, -40, 0));
    idle.put(Model_Spider.R_INDEX_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    idle.put(Model_Spider.R_INDEX_TIP_NAME, new BaseAngled(0, 40, 0));
    
    // The dead test case
    dead.put(Model_Spider.TOP_LEVEL_NAME, new BaseAngled(20, 30, 0));
    dead.put(Model_Spider.ABDOMEN_BRIDGE_NAME, new BaseAngled(195, 0, 0));
    dead.put(Model_Spider.ABDOMEN_NAME, new BaseAngled(0, 0, 0));
    dead.put(Model_Spider.THORAX_BRIDGE_NAME, new BaseAngled(0, 0, 180));
    dead.put(Model_Spider.THORAX_NAME, new BaseAngled(0, 0, 0));
    
    dead.put(Model_Spider.L_BACK_BODY_NAME, new BaseAngled(-115, -100, -45));
    dead.put(Model_Spider.L_BACK_INNERMID_NAME, new BaseAngled(0, -70, 0));
    dead.put(Model_Spider.L_BACK_OUTERMID_NAME, new BaseAngled(0, -80, 0));
    dead.put(Model_Spider.L_BACK_TIP_NAME, new BaseAngled(0, -90, 0));
    dead.put(Model_Spider.R_BACK_BODY_NAME, new BaseAngled(-115, 100, -135));
    dead.put(Model_Spider.R_BACK_INNERMID_NAME, new BaseAngled(0, -70, 0));
    dead.put(Model_Spider.R_BACK_OUTERMID_NAME, new BaseAngled(0, -80, 0));
    dead.put(Model_Spider.R_BACK_TIP_NAME, new BaseAngled(0, -90, 0));
    
    dead.put(Model_Spider.L_RING_BODY_NAME, new BaseAngled(-85, -100, -5));
    dead.put(Model_Spider.L_RING_INNERMID_NAME, new BaseAngled(0, -70, 0));
    dead.put(Model_Spider.L_RING_OUTERMID_NAME, new BaseAngled(0, -80, 0));
    dead.put(Model_Spider.L_RING_TIP_NAME, new BaseAngled(0, -90, 0));
    dead.put(Model_Spider.R_RING_BODY_NAME, new BaseAngled(-85, 100, -175));
    dead.put(Model_Spider.R_RING_INNERMID_NAME, new BaseAngled(0, -70, 0));
    dead.put(Model_Spider.R_RING_OUTERMID_NAME, new BaseAngled(0, -80, 0));
    dead.put(Model_Spider.R_RING_TIP_NAME, new BaseAngled(0, -90, 0));
    
    dead.put(Model_Spider.L_MIDDLE_BODY_NAME, new BaseAngled(-75, -100, 15));
    dead.put(Model_Spider.L_MIDDLE_INNERMID_NAME, new BaseAngled(0, -70, 0));
    dead.put(Model_Spider.L_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -80, 0));
    dead.put(Model_Spider.L_MIDDLE_TIP_NAME, new BaseAngled(0, -90, 0));
    dead.put(Model_Spider.R_MIDDLE_BODY_NAME, new BaseAngled(-75, 100, 165));
    dead.put(Model_Spider.R_MIDDLE_INNERMID_NAME, new BaseAngled(0, -70, 0));
    dead.put(Model_Spider.R_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -80, 0));
    dead.put(Model_Spider.R_MIDDLE_TIP_NAME, new BaseAngled(0, -90, 0));
    
    dead.put(Model_Spider.L_INDEX_BODY_NAME, new BaseAngled(-55, -100, 50));
    dead.put(Model_Spider.L_INDEX_INNERMID_NAME, new BaseAngled(0, -70, 0));
    dead.put(Model_Spider.L_INDEX_OUTERMID_NAME, new BaseAngled(0, -80, 0));
    dead.put(Model_Spider.L_INDEX_TIP_NAME, new BaseAngled(0, -90, 0));
    dead.put(Model_Spider.R_INDEX_BODY_NAME, new BaseAngled(-55, 100, 130));
    dead.put(Model_Spider.R_INDEX_INNERMID_NAME, new BaseAngled(0, -70, 0));
    dead.put(Model_Spider.R_INDEX_OUTERMID_NAME, new BaseAngled(0, -80, 0));
    dead.put(Model_Spider.R_INDEX_TIP_NAME, new BaseAngled(0, -90, 0));
    
    // The attack test case
    attack.put(Model_Spider.TOP_LEVEL_NAME, new BaseAngled(20, 30, 0));
    attack.put(Model_Spider.ABDOMEN_BRIDGE_NAME, new BaseAngled(195, 0, 0));
    attack.put(Model_Spider.ABDOMEN_NAME, new BaseAngled(0, 0, 0));
    attack.put(Model_Spider.THORAX_BRIDGE_NAME, new BaseAngled(-20, 0, 0));
    attack.put(Model_Spider.THORAX_NAME, new BaseAngled(-15, 0, 0));
    
    attack.put(Model_Spider.L_BACK_BODY_NAME, new BaseAngled(-115, -45, -45));
    attack.put(Model_Spider.L_BACK_INNERMID_NAME, new BaseAngled(0, -40, 0));
    attack.put(Model_Spider.L_BACK_OUTERMID_NAME, new BaseAngled(0, -45, 0));
    attack.put(Model_Spider.L_BACK_TIP_NAME, new BaseAngled(0, 45, 0));
    attack.put(Model_Spider.R_BACK_BODY_NAME, new BaseAngled(-115, 45, -135));
    attack.put(Model_Spider.R_BACK_INNERMID_NAME, new BaseAngled(0, -40, 0));
    attack.put(Model_Spider.R_BACK_OUTERMID_NAME, new BaseAngled(0, -45, 0));
    attack.put(Model_Spider.R_BACK_TIP_NAME, new BaseAngled(0, 45, 0));
    
    attack.put(Model_Spider.L_RING_BODY_NAME, new BaseAngled(-85, -55, -15));
    attack.put(Model_Spider.L_RING_INNERMID_NAME, new BaseAngled(0, -50, 0));
    attack.put(Model_Spider.L_RING_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    attack.put(Model_Spider.L_RING_TIP_NAME, new BaseAngled(0, 40, 0));
    attack.put(Model_Spider.R_RING_BODY_NAME, new BaseAngled(-85, 55, -165));
    attack.put(Model_Spider.R_RING_INNERMID_NAME, new BaseAngled(0, -50, 0));
    attack.put(Model_Spider.R_RING_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    attack.put(Model_Spider.R_RING_TIP_NAME, new BaseAngled(0, 40, 0));
    
    attack.put(Model_Spider.L_MIDDLE_BODY_NAME, new BaseAngled(-75, -45, 45));
    attack.put(Model_Spider.L_MIDDLE_INNERMID_NAME, new BaseAngled(0, -35, 0));
    attack.put(Model_Spider.L_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    attack.put(Model_Spider.L_MIDDLE_TIP_NAME, new BaseAngled(0, 40, 0));
    attack.put(Model_Spider.R_MIDDLE_BODY_NAME, new BaseAngled(-75, 45, 135));
    attack.put(Model_Spider.R_MIDDLE_INNERMID_NAME, new BaseAngled(0, -35, 0));
    attack.put(Model_Spider.R_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    attack.put(Model_Spider.R_MIDDLE_TIP_NAME, new BaseAngled(0, 40, 0));
    
    attack.put(Model_Spider.L_INDEX_BODY_NAME, new BaseAngled(-55, -25, 80));
    attack.put(Model_Spider.L_INDEX_INNERMID_NAME, new BaseAngled(0, -15, 0));
    attack.put(Model_Spider.L_INDEX_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    attack.put(Model_Spider.L_INDEX_TIP_NAME, new BaseAngled(0, 40, 0));
    attack.put(Model_Spider.R_INDEX_BODY_NAME, new BaseAngled(-55, 25, 100));
    attack.put(Model_Spider.R_INDEX_INNERMID_NAME, new BaseAngled(0, -15, 0));
    attack.put(Model_Spider.R_INDEX_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    attack.put(Model_Spider.R_INDEX_TIP_NAME, new BaseAngled(0, 40, 0));
    
    // The crawl test case
    crawl.put(Model_Spider.TOP_LEVEL_NAME, new BaseAngled(20, 30, 0));
    crawl.put(Model_Spider.ABDOMEN_BRIDGE_NAME, new BaseAngled(195, 0, 0));
    crawl.put(Model_Spider.ABDOMEN_NAME, new BaseAngled(0, 0, 0));
    crawl.put(Model_Spider.THORAX_BRIDGE_NAME, new BaseAngled(0, 0, 0));
    crawl.put(Model_Spider.THORAX_NAME, new BaseAngled(0, -5, 0));
    
    crawl.put(Model_Spider.L_BACK_BODY_NAME, new BaseAngled(-115, -45, -75));
    crawl.put(Model_Spider.L_BACK_INNERMID_NAME, new BaseAngled(0, -52, 0));
    crawl.put(Model_Spider.L_BACK_OUTERMID_NAME, new BaseAngled(0, -45, 0));
    crawl.put(Model_Spider.L_BACK_TIP_NAME, new BaseAngled(0, 53, 0));
    crawl.put(Model_Spider.R_BACK_BODY_NAME, new BaseAngled(-115, 45, -165));
    crawl.put(Model_Spider.R_BACK_INNERMID_NAME, new BaseAngled(0, -40, 0));
    crawl.put(Model_Spider.R_BACK_OUTERMID_NAME, new BaseAngled(0, -45, 0));
    crawl.put(Model_Spider.R_BACK_TIP_NAME, new BaseAngled(0, 45, 0));
    
    crawl.put(Model_Spider.L_RING_BODY_NAME, new BaseAngled(-85, -45, -35));
    crawl.put(Model_Spider.L_RING_INNERMID_NAME, new BaseAngled(0, -62, 0));
    crawl.put(Model_Spider.L_RING_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    crawl.put(Model_Spider.L_RING_TIP_NAME, new BaseAngled(0, 46, 0));
    crawl.put(Model_Spider.R_RING_BODY_NAME, new BaseAngled(-85, 45, -205));
    crawl.put(Model_Spider.R_RING_INNERMID_NAME, new BaseAngled(0, -50, 0));
    crawl.put(Model_Spider.R_RING_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    crawl.put(Model_Spider.R_RING_TIP_NAME, new BaseAngled(0, 40, 0));
    
    crawl.put(Model_Spider.L_MIDDLE_BODY_NAME, new BaseAngled(-75, -45, -15));
    crawl.put(Model_Spider.L_MIDDLE_INNERMID_NAME, new BaseAngled(0, -57, 0));
    crawl.put(Model_Spider.L_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    crawl.put(Model_Spider.L_MIDDLE_TIP_NAME, new BaseAngled(0, 44, 0));
    crawl.put(Model_Spider.R_MIDDLE_BODY_NAME, new BaseAngled(-75, 45, 135));
    crawl.put(Model_Spider.R_MIDDLE_INNERMID_NAME, new BaseAngled(0, -50, 0));
    crawl.put(Model_Spider.R_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    crawl.put(Model_Spider.R_MIDDLE_TIP_NAME, new BaseAngled(0, 40, 0));
    
    crawl.put(Model_Spider.L_INDEX_BODY_NAME, new BaseAngled(-55, -45, 20));
    crawl.put(Model_Spider.L_INDEX_INNERMID_NAME, new BaseAngled(0, -42, 0));
    crawl.put(Model_Spider.L_INDEX_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    crawl.put(Model_Spider.L_INDEX_TIP_NAME, new BaseAngled(0, 42, 0));
    crawl.put(Model_Spider.R_INDEX_BODY_NAME, new BaseAngled(-55, 45, 100));
    crawl.put(Model_Spider.R_INDEX_INNERMID_NAME, new BaseAngled(0, -40, 0));
    crawl.put(Model_Spider.R_INDEX_OUTERMID_NAME, new BaseAngled(0, -40, 0));
    crawl.put(Model_Spider.R_INDEX_TIP_NAME, new BaseAngled(0, 40, 0));
    
    // flatten test case (legs are extended outward)
    flatten.put(Model_Spider.TOP_LEVEL_NAME, new BaseAngled(20, 30, 0));
    flatten.put(Model_Spider.ABDOMEN_BRIDGE_NAME, new BaseAngled(195, 0, 0));
    flatten.put(Model_Spider.ABDOMEN_NAME, new BaseAngled(0, 0, 0));
    flatten.put(Model_Spider.THORAX_BRIDGE_NAME, new BaseAngled(0, 0, 0));
    flatten.put(Model_Spider.THORAX_NAME, new BaseAngled(0, 0, 0));
    
    flatten.put(Model_Spider.L_BACK_BODY_NAME, new BaseAngled(-115, -80, -45));
    flatten.put(Model_Spider.L_BACK_INNERMID_NAME, new BaseAngled(0, -10, 0));
    flatten.put(Model_Spider.L_BACK_OUTERMID_NAME, new BaseAngled(0, -14, 0));
    flatten.put(Model_Spider.L_BACK_TIP_NAME, new BaseAngled(0, 19, 0));
    flatten.put(Model_Spider.R_BACK_BODY_NAME, new BaseAngled(-115, 80, -135));
    flatten.put(Model_Spider.R_BACK_INNERMID_NAME, new BaseAngled(0, -10, 0));
    flatten.put(Model_Spider.R_BACK_OUTERMID_NAME, new BaseAngled(0, -14, 0));
    flatten.put(Model_Spider.R_BACK_TIP_NAME, new BaseAngled(0, 19, 0));
    
    flatten.put(Model_Spider.L_RING_BODY_NAME, new BaseAngled(-85, -80, -5));
    flatten.put(Model_Spider.L_RING_INNERMID_NAME, new BaseAngled(0, -10, 0));
    flatten.put(Model_Spider.L_RING_OUTERMID_NAME, new BaseAngled(0, -14, 0));
    flatten.put(Model_Spider.L_RING_TIP_NAME, new BaseAngled(0, 19, 0));
    flatten.put(Model_Spider.R_RING_BODY_NAME, new BaseAngled(-85, 80, -175));
    flatten.put(Model_Spider.R_RING_INNERMID_NAME, new BaseAngled(0, -10, 0));
    flatten.put(Model_Spider.R_RING_OUTERMID_NAME, new BaseAngled(0, -14, 0));
    flatten.put(Model_Spider.R_RING_TIP_NAME, new BaseAngled(0, 19, 0));
    
    flatten.put(Model_Spider.L_MIDDLE_BODY_NAME, new BaseAngled(-75, -80, 15));
    flatten.put(Model_Spider.L_MIDDLE_INNERMID_NAME, new BaseAngled(0, -10, 0));
    flatten.put(Model_Spider.L_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -14, 0));
    flatten.put(Model_Spider.L_MIDDLE_TIP_NAME, new BaseAngled(0, 19, 0));
    flatten.put(Model_Spider.R_MIDDLE_BODY_NAME, new BaseAngled(-75, 80, 165));
    flatten.put(Model_Spider.R_MIDDLE_INNERMID_NAME, new BaseAngled(0, -10, 0));
    flatten.put(Model_Spider.R_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -14, 0));
    flatten.put(Model_Spider.R_MIDDLE_TIP_NAME, new BaseAngled(0, 19, 0));
    
    flatten.put(Model_Spider.L_INDEX_BODY_NAME, new BaseAngled(-55, -80, 50));
    flatten.put(Model_Spider.L_INDEX_INNERMID_NAME, new BaseAngled(0, -10, 0));
    flatten.put(Model_Spider.L_INDEX_OUTERMID_NAME, new BaseAngled(0, -14, 0));
    flatten.put(Model_Spider.L_INDEX_TIP_NAME, new BaseAngled(0, 19, 0));
    flatten.put(Model_Spider.R_INDEX_BODY_NAME, new BaseAngled(-55, 80, 130));
    flatten.put(Model_Spider.R_INDEX_INNERMID_NAME, new BaseAngled(0, -10, 0));
    flatten.put(Model_Spider.R_INDEX_OUTERMID_NAME, new BaseAngled(0, -14, 0));
    flatten.put(Model_Spider.R_INDEX_TIP_NAME, new BaseAngled(0, 19, 0));
    
    // spiderman test case (mimics the spiderman logo)
    spiderman.put(Model_Spider.TOP_LEVEL_NAME, new BaseAngled(20, 30, 0));
    spiderman.put(Model_Spider.ABDOMEN_BRIDGE_NAME, new BaseAngled(195, 0, 0));
    spiderman.put(Model_Spider.ABDOMEN_NAME, new BaseAngled(0, 0, 0));
    spiderman.put(Model_Spider.THORAX_BRIDGE_NAME, new BaseAngled(-90, 0, 150));
    spiderman.put(Model_Spider.THORAX_NAME, new BaseAngled(0, 0, 0));
    
    spiderman.put(Model_Spider.L_BACK_BODY_NAME, new BaseAngled(-100, -55, -75));
    spiderman.put(Model_Spider.L_BACK_INNERMID_NAME, new BaseAngled(0, -48, 0));
    spiderman.put(Model_Spider.L_BACK_OUTERMID_NAME, new BaseAngled(0, -42, 0));
    spiderman.put(Model_Spider.L_BACK_TIP_NAME, new BaseAngled(0, -22, 0));
    spiderman.put(Model_Spider.R_BACK_BODY_NAME, new BaseAngled(-100, 55, -105));
    spiderman.put(Model_Spider.R_BACK_INNERMID_NAME, new BaseAngled(0, -48, 0));
    spiderman.put(Model_Spider.R_BACK_OUTERMID_NAME, new BaseAngled(0, -42, 0));
    spiderman.put(Model_Spider.R_BACK_TIP_NAME, new BaseAngled(0, -22, 0));
    
    spiderman.put(Model_Spider.L_RING_BODY_NAME, new BaseAngled(-84, -43, -35));
    spiderman.put(Model_Spider.L_RING_INNERMID_NAME, new BaseAngled(0, -68, 0));
    spiderman.put(Model_Spider.L_RING_OUTERMID_NAME, new BaseAngled(0, -52, 0));
    spiderman.put(Model_Spider.L_RING_TIP_NAME, new BaseAngled(0, -16, 0));
    spiderman.put(Model_Spider.R_RING_BODY_NAME, new BaseAngled(-84, 43, -145));
    spiderman.put(Model_Spider.R_RING_INNERMID_NAME, new BaseAngled(0, -68, 0));
    spiderman.put(Model_Spider.R_RING_OUTERMID_NAME, new BaseAngled(0, -52, 0));
    spiderman.put(Model_Spider.R_RING_TIP_NAME, new BaseAngled(0, -16, 0));
    
    spiderman.put(Model_Spider.L_MIDDLE_BODY_NAME, new BaseAngled(-75, -35, 45));
    spiderman.put(Model_Spider.L_MIDDLE_INNERMID_NAME, new BaseAngled(0, -70, 0));
    spiderman.put(Model_Spider.L_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -48, 0));
    spiderman.put(Model_Spider.L_MIDDLE_TIP_NAME, new BaseAngled(0, -34, 0));
    spiderman.put(Model_Spider.R_MIDDLE_BODY_NAME, new BaseAngled(-75, 35, 135));
    spiderman.put(Model_Spider.R_MIDDLE_INNERMID_NAME, new BaseAngled(0, -70, 0));
    spiderman.put(Model_Spider.R_MIDDLE_OUTERMID_NAME, new BaseAngled(0, -48, 0));
    spiderman.put(Model_Spider.R_MIDDLE_TIP_NAME, new BaseAngled(0, -34, 0));
    
    spiderman.put(Model_Spider.L_INDEX_BODY_NAME, new BaseAngled(-40, -25, 80));
    spiderman.put(Model_Spider.L_INDEX_INNERMID_NAME, new BaseAngled(0, -60, 0));
    spiderman.put(Model_Spider.L_INDEX_OUTERMID_NAME, new BaseAngled(0, -44, 0));
    spiderman.put(Model_Spider.L_INDEX_TIP_NAME, new BaseAngled(0, -10, 0));
    spiderman.put(Model_Spider.R_INDEX_BODY_NAME, new BaseAngled(-40, 25, 100));
    spiderman.put(Model_Spider.R_INDEX_INNERMID_NAME, new BaseAngled(0, -60, 0));
    spiderman.put(Model_Spider.R_INDEX_OUTERMID_NAME, new BaseAngled(0, -44, 0));
    spiderman.put(Model_Spider.R_INDEX_TIP_NAME, new BaseAngled(0, -10, 0));
  }
}
