package io.cbitler.stealingartefacts;

import net.runelite.client.ui.FontManager;
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
    private final StealingArtefactsConfig config;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    private StealingArtefactsOverlay(StealingArtefactsPlugin plugin, StealingArtefactsConfig config) {
        setPosition(OverlayPosition.TOP_LEFT);
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
        if (!plugin.isPlayerInPisc()) {
            return null;
        }

        panelComponent.getChildren().clear();
        String title = "Stealing Artefacts";
        var currentTaskState = plugin.taskState.get();
        String targetLine = getTargetMessage(currentTaskState);

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

        if (config.highlightGuardLures()) {
            panelComponent.getChildren().add(LineComponent.builder().build());
            panelComponent.getChildren().add(LineComponent.builder().left("Guard Lures:").build());

            String eastGuardLured = plugin.eastGuardLured ? "\u2713" : "\u2717";
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Eastern Guard")
                    .right(eastGuardLured)
                    .rightFont(FontManager.getDefaultFont())
                    .rightColor(plugin.eastGuardLured ? Color.GREEN : Color.RED)
                    .build());

            String southEastGuardLured = plugin.southEastGuardLured ? "\u2713" : "\u2717";
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("South-East Guard")
                    .right(southEastGuardLured)
                    .rightFont(FontManager.getDefaultFont())
                    .rightColor(plugin.southEastGuardLured ? Color.GREEN : Color.RED)
                    .build());
        }

        panelComponent.setPreferredSize(new Dimension(200, 0));
        return panelComponent.render(graphics);
    }

    /**
     * Get the target message based on the state
     *
     * @param state The current minigame state
     * @return The string to display as the target, either 'None' or the target message
     */
    private String getTargetMessage(StealingArtefactsState state) {
        if (state == null) {
            return "None";
        }

        return state.getTarget();
    }
}
