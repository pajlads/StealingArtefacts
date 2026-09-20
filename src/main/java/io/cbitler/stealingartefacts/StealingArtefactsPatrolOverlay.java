package io.cbitler.stealingartefacts;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Overlay to highlight the patrol-people in port pisc
 */
public class StealingArtefactsPatrolOverlay extends Overlay {
    public static final Color CLICKBOX_BORDER = Color.YELLOW;
    public static final Color CLICKBOX_FILL_COLOR = new Color(255, 0, 0, 50);

    public static final Color CLICKBOX_FILL_COLOR_LURED = new Color(0, 255, 0, 50);

    private static final int DIRECTION_ARROW_SIZE = 24;
    private static final int CAMERA_UNITS_PER_ORIENTATION_UNIT = 8;
    private static final int CAMERA_HALF_TURN = 8192;

    private final StealingArtefactsPlugin plugin;
    private final StealingArtefactsConfig config;
    private final Client client;

    @Inject
    StealingArtefactsPatrolOverlay(Client client, StealingArtefactsPlugin plugin, StealingArtefactsConfig config,
                                   SpriteManager spriteManager) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        spriteManager.getSpriteAsync(SpriteID.Arrow.YELLOW_UP, 0, sprite -> directionArrow = sprite);
    }

    private volatile BufferedImage directionArrow;

    /**
     * Overlay the patrol-people on the same plane as the player
     * @param graphics The graphics to draw the overlay with
     * @return null, use OverlayUtil to draw overlay
     */
    @Override
    public Dimension render(Graphics2D graphics) {
        Point mousePosition = client.getMouseCanvasPosition();
        for (NPC actor : plugin.markedNPCs) {
            if (actor.getWorldLocation().getPlane() != client.getPlane()) {
                continue;
            }

            if (config.highlightPatrols()) {
                if ((actor.getId() == Constants.PATROL_ID_MAX) && plugin.isGuardLured(actor) && config.highlightGuardLures()) {
                    OverlayUtil.renderHoverableArea(graphics, actor.getConvexHull(),
                            mousePosition, CLICKBOX_FILL_COLOR_LURED, CLICKBOX_BORDER, CLICKBOX_BORDER);
                } else {
                    OverlayUtil.renderHoverableArea(graphics, actor.getConvexHull(),
                            mousePosition, CLICKBOX_FILL_COLOR, CLICKBOX_BORDER, CLICKBOX_BORDER);
                }
            }

            if (config.showPatrolFacingDirection()) {
                renderFacingDirection(graphics, actor);
            }

        }

        return null;
    }

    private void renderFacingDirection(Graphics2D graphics, NPC actor) {
        BufferedImage arrow = directionArrow;
        if (arrow == null) {
            return;
        }

        LocalPoint actorLocation = actor.getLocalLocation();
        if (actorLocation == null) {
            return;
        }

        Point canvasPoint = Perspective.localToCanvas(client, actorLocation, client.getPlane());
        if (canvasPoint == null) {
            return;
        }

        Graphics2D arrowGraphics = (Graphics2D) graphics.create();
        try {
            double scaleX = (double) DIRECTION_ARROW_SIZE / arrow.getWidth();
            double scaleY = (double) DIRECTION_ARROW_SIZE / arrow.getHeight();
            AffineTransform transform = new AffineTransform();
            transform.translate(canvasPoint.getX(), canvasPoint.getY());
            transform.rotate(spriteRotation(actor.getCurrentOrientation(), client.getCameraYaw()));
            transform.scale(scaleX, scaleY);
            transform.translate(-arrow.getWidth() / 2.0, -arrow.getHeight() / 2.0);

            arrowGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            arrowGraphics.drawImage(arrow, transform, null);
        } finally {
            arrowGraphics.dispose();
        }
    }

    static double spriteRotation(int orientation, int cameraYaw) {
        int orientationInCameraUnits = (orientation & 2047) * CAMERA_UNITS_PER_ORIENTATION_UNIT;
        int relativeAngle = orientationInCameraUnits + (cameraYaw & 16383) - CAMERA_HALF_TURN;
        return relativeAngle * Perspective.UNIT14;
    }
}
