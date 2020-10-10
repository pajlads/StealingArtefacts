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

    private final Client client;
    private final StealingArtefactsPlugin plugin;

    @Inject
    StealingArtefactsPatrolOverlay(Client client, StealingArtefactsPlugin plugin) {
        super(plugin);
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
    }

    /**
     * Overlay the patrol-people on the same plane as the player
     * @param graphics The graphiics to draw the overlay with
     * @return null, use OverlayUtil to draw overlay
     */
    @Override
    public Dimension render(Graphics2D graphics) {
        Point mousePosition = client.getMouseCanvasPosition();
        for (NPC actor : plugin.markedNPCs.values()) {
            OverlayUtil.renderHoverableArea(graphics, getTile(actor), mousePosition,
                    CLICKBOX_FILL_COLOR, CLICKBOX_BORDER, CLICKBOX_BORDER);
        }

        return null;
    }

    private Polygon getTile(NPC npc) {
        int size = npc.getComposition().getSize();
        LocalPoint lp = npc.getLocalLocation();

        int x = lp.getX() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);
        int y = lp.getY() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);

        return Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));
    }
}
