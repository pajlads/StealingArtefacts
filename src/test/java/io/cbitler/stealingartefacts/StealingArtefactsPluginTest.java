package io.cbitler.stealingartefacts;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class StealingArtefactsPluginTest {
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(StealingArtefactsPlugin.class);
        RuneLite.main(args);
    }
}
