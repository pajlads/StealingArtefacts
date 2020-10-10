package io.cbitler.stealingartefacts;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

/**
 * Overly for drawers in the minigame
 */
public class StealingArtefactsHouseOverlay extends Overlay {
    public static final Color CLICKBOX_BORDER = Color.YELLOW;
    public static final Color CLICKBOX_HOVER_BORDER = Color.YELLOW;
    public static final Color CLICKBOX_FILL_COLOR = new Color(0, 255, 0, 50);

    private final Client client;
    private final StealingArtefactsPlugin plugin;

    @Inject
    StealingArtefactsHouseOverlay(Client client, StealingArtefactsPlugin plugin) {
        super(plugin);
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
    }

    /**
     * Draw an overlay on the drawers for the minigame where applicable
     * @param graphics The graphics to draw the overlay with
     * @return null, use OverlayUtil to render hoverable area
     */
    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.currentState == null) {
            return null;
        }

        Point mousePosition = client.getMouseCanvasPosition();
        for (GameObject markedObject : plugin.markedObjects) {
            if (client.getPlane() == plugin.currentState.getDrawerPlane()) {
                OverlayUtil.renderHoverableArea(graphics, markedObject.getClickbox(), mousePosition,
                        CLICKBOX_FILL_COLOR, CLICKBOX_BORDER, CLICKBOX_HOVER_BORDER);
            }
        }

        return null;
    }
}
