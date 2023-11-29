package io.cbitler.stealingartefacts;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import org.jetbrains.annotations.Nullable;

/**
 * Enum representing the current state of the minigame for the player
 */
@Getter
@RequiredArgsConstructor
public enum StealingArtefactsState {
    NO_TASK("None"),
    NORTHERN("Northern house", ObjectID.DRAWERS_27771, ObjectID.LADDER_27634, 0, new WorldPoint(1768, 3750, 0), new WorldPoint(0, 0, 0)),
    SOUTHEASTERN("South-Eastern house", ObjectID.DRAWERS_27772, ObjectID.LADDER_27634, 1, new WorldPoint(1775, 3733, 1), new WorldPoint(1776, 3730, 0)),
    SOUTHERN("Southern house", ObjectID.DRAWERS_27773, ObjectID.LADDER_27634, 1, new WorldPoint(1765, 3732, 1), new WorldPoint(1768, 3733, 0)),
    SOUTHWESTERN("South-Western house", ObjectID.DRAWERS_27774, ObjectID.LADDER_27634, 1, new WorldPoint(1749, 3734, 1), new WorldPoint(1749, 3730, 0)),
    WESTERN("Western house", ObjectID.DRAWERS_27775, ObjectID.LADDER_27634, 1, new WorldPoint(1750, 3749, 1), new WorldPoint(1751, 3751, 0)),
    NORTHWESTERN("North-Western house", ObjectID.DRAWERS_27776, ObjectID.LADDER_27634, 1, new WorldPoint(1750, 3761, 1), new WorldPoint(1750, 3756, 0)),
    FAILURE("Artefact Failed - Return to Captain Khaled"),
    DELIVER_ARTEFACT("Deliver the Artefact to Captain Khaled");


    private final String target;
    private final int drawerId;
    private final int ladderId;
    private final int drawerPlane;
    private final @Nullable WorldPoint hintLocation;
    private final @Nullable WorldPoint ladderLocation;

    /**
     * Create a state for states that aren't a specific house
     *
     * @param target The string to show in the overlay as the target
     */
    StealingArtefactsState(@NonNull String target) {
        this.target = target;
        drawerId = -1;
        ladderId = -1;
        drawerPlane = -1;
        hintLocation = null;
        ladderLocation = null;
    }
}
