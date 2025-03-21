package io.cbitler.stealingartefacts;

import net.runelite.api.Client;
import net.runelite.client.plugins.xptracker.XpTrackerService;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

/**
 * Overlay to show the user's current target in the minigame
 */
public class StealingArtefactsOverlay extends OverlayPanel {
    private final StealingArtefactsPlugin plugin;
    private final Client client;
    private final StealingArtefactsConfig config;

    @Inject
    private StealingArtefactsOverlay(Client client, StealingArtefactsPlugin plugin, StealingArtefactsConfig config) {
        setPosition(OverlayPosition.TOP_LEFT);

        panelComponent.setPreferredSize(new Dimension(200, 0));

        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Draw the overlay in the top left corner, but only if the user is in the area of the minigame
     *
     * @param graphics The graphics object to draw the overlay with
     * @return The rendered panel, or null if they aren't in the minigame area
     */
    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showOverlay()) {
            return null;
        }

        var localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return null;
        }

        if (!plugin.isInPisc(localPlayer.getWorldLocation())) {
            return null;
        }

        panelComponent.getChildren().clear();
        String title = "Stealing Artefacts";
        String targetLine = getTargetMessage(plugin.currentState);

        // Title
        panelComponent.getChildren().add(TitleComponent.builder().text(title).color(Color.YELLOW).build());

        // Target
        panelComponent.getChildren().add(LineComponent.builder().left("Current Target:").build());
        panelComponent.getChildren().add(LineComponent.builder().left(targetLine).build());

        // Artefacts to goal
        if (plugin.artefactsToGoal > 0 && config.showToNextLevel()) {
            panelComponent.getChildren().add(LineComponent.builder().build());
            panelComponent.getChildren().add(LineComponent.builder().left("Artefacts until goal:").build());
            panelComponent.getChildren().add(LineComponent.builder().left(String.valueOf(plugin.artefactsToGoal)).build());
        }

        this.addGuardSection();

        return super.render(graphics);
    }

    private void addGuardSection() {
        var overlayShowGuardLures = config.overlayShowGuardLures();

        if (overlayShowGuardLures == StealingArtefactsConfig.OverlayShowGuardLure.Never) {
            return;
        }

        var isAnyGuardUnlured = !plugin.southEastGuardLured || !plugin.eastGuardLured;

        if (isAnyGuardUnlured || overlayShowGuardLures == StealingArtefactsConfig.OverlayShowGuardLure.Always) {
            panelComponent.getChildren().add(LineComponent.builder().build());
            panelComponent.getChildren().add(LineComponent.builder().left("Guard Lures:").build());

            if (!plugin.eastGuardLured || overlayShowGuardLures == StealingArtefactsConfig.OverlayShowGuardLure.Always) {
                String eastGuardLured = plugin.eastGuardLured ? "\u2713" : "\u2717";
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Eastern Guard")
                        .right(eastGuardLured)
                        .rightFont(FontManager.getDefaultFont())
                        .rightColor(plugin.eastGuardLured ? Color.GREEN : Color.RED)
                        .build());
            }

            if (!plugin.southEastGuardLured || overlayShowGuardLures == StealingArtefactsConfig.OverlayShowGuardLure.Always) {
                String southEastGuardLured = plugin.southEastGuardLured ? "\u2713" : "\u2717";
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("South-East Guard")
                        .right(southEastGuardLured)
                        .rightFont(FontManager.getDefaultFont())
                        .rightColor(plugin.southEastGuardLured ? Color.GREEN : Color.RED)
                        .build());
            }
        }
    }

    /**
     * Get the target message based on the state
     *
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
