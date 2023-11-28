package io.cbitler.stealingartefacts;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

/**
 * Overlay to highlight the patrol-people in port pisc
 */
public class StealingArtefactsPatrolOverlay extends Overlay {
    public static final Color CLICKBOX_BORDER = Color.YELLOW;
    public static final Color CLICKBOX_FILL_COLOR = new Color(255, 0, 0, 50);

    public static final Color CLICKBOX_FILL_COLOR_LURED = new Color(0, 255, 0, 50);

    private final StealingArtefactsPlugin plugin;
    private final StealingArtefactsConfig config;
    private final Client client;

    @Inject
    StealingArtefactsPatrolOverlay(Client client, StealingArtefactsPlugin plugin, StealingArtefactsConfig config) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Overlay the patrol-people on the same plane as the player
     *
     * @param graphics The graphics to draw the overlay with
     * @return null, use OverlayUtil to draw overlay
     */
    @Override
    public Dimension render(Graphics2D graphics) {
        Point mousePosition = client.getMouseCanvasPosition();
        if (config.highlightPatrols()) {
            for (NPC actor : plugin.markedNPCs) {
                if ((actor.getId() == Constants.PATROL_ID_MAX) && plugin.isGuardLured(actor) && config.highlightGuardLures()) {
                    OverlayUtil.renderHoverableArea(graphics, actor.getConvexHull(),
                            mousePosition, CLICKBOX_FILL_COLOR_LURED, CLICKBOX_BORDER, CLICKBOX_BORDER);
                } else {
                    OverlayUtil.renderHoverableArea(graphics, actor.getConvexHull(),
                            mousePosition, CLICKBOX_FILL_COLOR, CLICKBOX_BORDER, CLICKBOX_BORDER);
                }
            }
        }

        return null;
    }
}
