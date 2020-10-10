package io.cbitler.stealingartefacts;

import lombok.Getter;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;

/**
 * Enum representing the current state of the minigame for the player
 */
@Getter
public enum StealingArtefactsState {
    NO_TASK(""),
    NORTHERN("Northern house", ObjectID.DRAWERS_27771, 0, new WorldPoint(1768, 3750, 0)),
    SOUTHEASTERN("South-Eastern house", ObjectID.DRAWERS_27772, 1, new WorldPoint(1775, 3733, 1)),
    SOUTHERN("Southern house", ObjectID.DRAWERS_27773, 1, new WorldPoint(1765, 3732, 1)),
    SOUTHWESTERN("South-Western house", ObjectID.DRAWERS_27774, 1, new WorldPoint(1749, 3734, 1)),
    WESTERN("Western house", ObjectID.DRAWERS_27775, 1, new WorldPoint(1750, 3749, 1)),
    NORTHWESTERN("North-Western house", ObjectID.DRAWERS_27776, 1, new WorldPoint(1750, 3761, 1)),
    FAILURE("Artefact Failed - Return to Captain Khaled"),
    DELIVER_ARTEFACT("Deliver the Artefact to Captain Khaled");


    private final String target;
    private final int drawerId;
    private final int drawerPlane;
    private final WorldPoint hintLocation;

    /**
     * Create a state pointing at a specific house
     * @param target The string to show in the overlay as the target
     * @param drawerId The game object ID of the drawer
     * @param drawerPlane The game object ID of the
     * @param hintLocation The hint location for the house
     */
    StealingArtefactsState(String target, int drawerId, int drawerPlane, WorldPoint hintLocation) {
        this.target = target;
        this.drawerId = drawerId;
        this.drawerPlane = drawerPlane;
        this.hintLocation = hintLocation;
    }

    /**
     * Create a state for states that aren't a specific house
     * @param target The string to show in the overlay as the target
     */
    StealingArtefactsState(String target) {
        this.target = target;
        drawerId = -1;
        drawerPlane = -1;
        hintLocation = null;
    }
}
