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

    private final StealingArtefactsPlugin plugin;

    @Inject
    StealingArtefactsPatrolOverlay(StealingArtefactsPlugin plugin) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.plugin = plugin;
    }

    /**
     * Overlay the patrol-people on the same plane as the player
     * @param graphics The graphics to draw the overlay with
     * @return null, use OverlayUtil to draw overlay
     */
    @Override
    public Dimension render(Graphics2D graphics) {
        for (NPC actor : plugin.markedNPCs) {
            renderPoly(graphics, CLICKBOX_FILL_COLOR, actor.getConvexHull());
        }

        return null;
    }

    private void renderPoly(Graphics2D graphics, Color color, Shape polygon)
    {
        if (polygon != null)
        {
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(2));
            graphics.draw(polygon);
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
            graphics.fill(polygon);
        }
    }
}
