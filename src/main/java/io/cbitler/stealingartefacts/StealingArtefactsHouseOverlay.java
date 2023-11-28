package io.cbitler.stealingartefacts;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.api.ObjectID;
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

    private final StealingArtefactsConfig config;

    @Inject
    StealingArtefactsHouseOverlay(Client client, StealingArtefactsPlugin plugin, StealingArtefactsConfig config) {
        super(plugin);
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Draw an overlay on the drawers and ladders for the minigame where applicable
     *
     * @param graphics The graphics to draw the overlay with
     * @return null, use OverlayUtil to render hoverable area
     */
    @Override
    public Dimension render(Graphics2D graphics) {
        var currentTaskState = plugin.taskState.get();
        if (currentTaskState == null) {
            return null;
        }

        Point mousePosition = client.getMouseCanvasPosition();
        for (var object : plugin.markedObjects) {
            if (object.getId() == ObjectID.LADDER_27634 && !(config.highlightLadders())) {
                continue;
            }
            if (client.getPlane() == object.getWorldLocation().getPlane()) {
                OverlayUtil.renderHoverableArea(graphics, object.getClickbox(), mousePosition, CLICKBOX_FILL_COLOR,
                        CLICKBOX_BORDER, CLICKBOX_HOVER_BORDER);
            }
        }

        return null;
    }
}
