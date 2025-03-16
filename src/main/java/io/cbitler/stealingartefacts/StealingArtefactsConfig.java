package io.cbitler.stealingartefacts;

import lombok.AllArgsConstructor;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("stealingartefacts")
public interface StealingArtefactsConfig extends Config {
    String GROUP_NAME = "StealingArtefacts";
    String CURRENT_STATE_KEY = "currentState";

    String HIGHLIGHT_PATROLS = "highlightPatrols";

    String HIGHLIGHT_LADDERS = "highlightLadders";

    String HIGHLIGHT_GUARD_LURES = "highlightGuardLures";

    String SHOW_TO_NEXT_LEVEL = "showToNextLevel";

    @ConfigItem(
            keyName = CURRENT_STATE_KEY,
            name = "",
            description ="",
            hidden = true
    )
    default int currentHouse() {
        return -1;
    }

    @ConfigItem(
            keyName = HIGHLIGHT_PATROLS,
            name = "Highlight Patrols",
            description = "Whether or not to highlight patrols",
            position = 3
    )
    default boolean highlightPatrols() { return true; }

    @ConfigItem(
            keyName = HIGHLIGHT_LADDERS,
            name = "Highlight House Ladders",
            description = "Whether or not to highlight house ladders",
            position = 1
    )
    default boolean highlightLadders() { return true; }

    @ConfigItem(
            keyName = HIGHLIGHT_GUARD_LURES,
            name = "Highlight Lured Guards",
            description = "Whether or not to highlight guards when lured/positioned correctly",
            position = 2
    )
    default boolean highlightGuardLures() { return true; }

    @ConfigItem(
            keyName = "highlightKhaledTaskless",
            name = "Highlight Khaled without task",
            description = "This option will highlight Khaled when you don't have a current target",
            position = 4
    )
    default boolean highlightKhaledTaskless() {
        return true;
    }

    @ConfigSection(
            name = "Overlay",
            description = "Controls the overlay display preferences",
            position = 10,
            closedByDefault = false
    )
    String overlaySection = "Overlay";

    @ConfigItem(
            keyName = "showOverlay",
            name = "Show overlay",
            description = "Uncheck this to hide the overlay",
            section = overlaySection,
            position = 1
    )
    default boolean showOverlay() {
        return true;
    }

    @ConfigItem(
            keyName = SHOW_TO_NEXT_LEVEL,
            name = "Show artefacts to next level",
            description = "Whether or not to show artefacts to next level",
            section = overlaySection,
            position = 2
    )
    default boolean showToNextLevel() { return true; }

    @AllArgsConstructor
    enum OverlayShowGuardLure {
        Always("Always"),
        Never("Never"),
        OnlyUnlured("Only unlured");

        final String displayText;

        @Override
        public String toString() {
            return this.displayText;
        }
    };

    @ConfigItem(
            keyName = "overlayShowGuardLures",
            name = "Show guard lures",
            description = "Uncheck this to hide the guard lure portion in the overlay",
            section = overlaySection,
            position = 3
    )
    default OverlayShowGuardLure overlayShowGuardLures() {
        return OverlayShowGuardLure.Always;
    }
}