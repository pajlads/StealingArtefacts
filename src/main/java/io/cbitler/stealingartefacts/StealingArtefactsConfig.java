package io.cbitler.stealingartefacts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("stealingartefacts")
public interface StealingArtefactsConfig extends Config {
    String GROUP_NAME = "StealingArtefacts";

    @ConfigItem(
        keyName = "highlightPatrols",
        name = "Highlight Patrols",
        description = "Whether or not to highlight patrols"
    )
    default boolean highlightPatrols() {
        return true;
    }

    @ConfigItem(
        keyName = "highlightLadders",
        name = "Highlight House Ladders",
        description = "Whether or not to highlight house ladders"
    )
    default boolean highlightLadders() {
        return true;
    }

    @ConfigItem(
        keyName = "highlightGuardLures",
        name = "Highlight Lured Guards",
        description = "Whether or not to highlight guards when lured/positioned correctly"
    )
    default boolean highlightGuardLures() {
        return true;
    }

    @ConfigItem(
        keyName = "showToNextLevel",
        name = "Show artefacts to next level",
        description = "Whether or not to show artefacts to next level"
    )
    default boolean showToNextLevel() {
        return true;
    }
}
