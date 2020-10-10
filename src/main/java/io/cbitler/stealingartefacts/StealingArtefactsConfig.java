package io.cbitler.stealingartefacts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("stealingartefacts")
public interface StealingArtefactsConfig extends Config {
    String GROUP_NAME = "StealingArtefacts";
    String CURRENT_STATE_KEY = "currentState";
    @ConfigItem(
            keyName = CURRENT_STATE_KEY,
            name = "",
            description ="",
            hidden = true
    )
    default int currentHouse() {
        return -1;
    }
}
