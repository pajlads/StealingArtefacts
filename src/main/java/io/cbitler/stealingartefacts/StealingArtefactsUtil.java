package io.cbitler.stealingartefacts;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.plugins.xptracker.XpTrackerService;

/**
 * Utility methods for the plugin
 */
@Slf4j
public class StealingArtefactsUtil {
    /**
     * Get the number of artefacts to next level
     * @param client The runelite client
     * @param service XPTracker service
     * @return The number of artefacts until the next level, rounded up
     */
    static int artefactsToNextLevel(Client client, XpTrackerService service) {
        double currentXp = client.getSkillExperience(Skill.THIEVING);
        double currentLevel = client.getRealSkillLevel(Skill.THIEVING);
        double goalXp = service.getEndGoalXp(Skill.THIEVING);
        double remainingXp = goalXp - currentXp;

        return (int) Math.ceil(remainingXp / (750 + (40*currentLevel)));
    }
}
