package io.cbitler.stealingartefacts;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;

/**
 * Overlay to highlight Captain Khaled
 */
public class StealingArtefactsKhaledOverlay extends Overlay {
    private static final Color MISSING_TASK_BORDER = Color.YELLOW;
    private static final Color MISSING_TASK_BORDER_HOVER = Color.ORANGE;
    private static final Color MISSING_TASK_FILL = new Color(255, 0, 0, 50);

    private final StealingArtefactsPlugin plugin;
    private final StealingArtefactsConfig config;
    private final Client client;

    @Inject
    StealingArtefactsKhaledOverlay(Client client, StealingArtefactsPlugin plugin, StealingArtefactsConfig config) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        var fillColor = this.fillColor();
        if (fillColor == null) {
            return null;
        }

        if (plugin.captainKhaled == null) {
            return null;
        }

        if (!this.config.highlightKhaledTaskless()) {
            return null;
        }

        Point mousePosition = client.getMouseCanvasPosition();

        OverlayUtil.renderHoverableArea(graphics, plugin.captainKhaled.getConvexHull(), mousePosition, fillColor, MISSING_TASK_BORDER, MISSING_TASK_BORDER_HOVER);

        return null;
    }

    @Nullable
    private Color fillColor() {
        switch (plugin.currentState) {
            case NO_TASK:
            case FAILURE:
                return MISSING_TASK_FILL;

            default:
                return null;
        }
    }
}
