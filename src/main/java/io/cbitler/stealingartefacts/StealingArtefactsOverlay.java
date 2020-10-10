package io.cbitler.stealingartefacts;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

/**
 * Overlay to show the user's current target in the minigame
 */
public class StealingArtefactsOverlay extends Overlay {
    private final StealingArtefactsPlugin plugin;
    private final Client client;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    private StealingArtefactsOverlay(Client client, StealingArtefactsPlugin plugin, StealingArtefactsConfig config) {
        setPosition(OverlayPosition.TOP_LEFT);
        this.client = client;
        this.plugin = plugin;
    }

    /**
     * Draw the overlay in the top left corner, but only if the user is in the area of the minigame
     * @param graphics The graphics object to draw the overlay with
     * @return The rendered panel, or null if they aren't in the minigame area
     */
    @Override
    public Dimension render(Graphics2D graphics) {
        if (client.getLocalPlayer() != null && plugin.isInPisc(client.getLocalPlayer().getWorldLocation())) {
            panelComponent.getChildren().clear();
            String title = "Stealing Artefacts";
            String targetLine = getTargetMessage(plugin.currentState);
            panelComponent.getChildren().add(TitleComponent.builder().text(title).color(Color.YELLOW).build());
            panelComponent.getChildren().add(LineComponent.builder().left("Current Target:").build());
            panelComponent.getChildren().add(LineComponent.builder().left(targetLine).build());
            panelComponent.setPreferredSize(new Dimension(200, 0));
            return panelComponent.render(graphics);
        }
        return null;
    }

    /**
     * Get the target message based on the state
     * @param state The current minigame state
     * @return The string to display as the target, either 'None' or the target message
     */
    private String getTargetMessage(StealingArtefactsState state) {
        if (state == null || state == StealingArtefactsState.NO_TASK) {
            return "None";
        } else {
            return state.getTarget();
        }
    }
}
