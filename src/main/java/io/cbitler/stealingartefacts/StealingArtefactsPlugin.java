package io.cbitler.stealingartefacts;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.client.plugins.xptracker.XpTrackerService;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

@PluginDescriptor(
        name = "Stealing Artefacts",
        description = "Show the current house for stealing artefacts",
        tags = {}
)
@PluginDependency(XpTrackerPlugin.class)
@Slf4j
public class StealingArtefactsPlugin extends Plugin {
    @Inject
    private OverlayManager overlayManager;

    @Inject
    ConfigManager configManager;

    @Inject
    private StealingArtefactsOverlay overlay;

    @Inject
    private StealingArtefactsHouseOverlay houseOverlay;

    @Inject
    private StealingArtefactsPatrolOverlay patrolOverlay;

    @Inject
    private Client client;

    @Inject
    private XpTrackerService xpTrackerService;

    public HashSet<GameObject> markedObjects = new HashSet<>();

    public HashSet<NPC> markedNPCs = new HashSet<>();

    public StealingArtefactsState currentState;

    public boolean highlightPatrols = true;

    public boolean showArtefactsToNextLevel = true;

    public static final WorldPoint EAST_GUARD_POS = new WorldPoint(1777, 3746,0);
    public static final WorldPoint SOUTHEAST_GUARD_POS = new WorldPoint(1780, 3731,0);
    public boolean eastGuardLured = false;

    public boolean southEastGuardLured = false;

    public NPC captainKhaled;

    public int artefactsToGoal = -1;

    /**
     * Handle plugin startup
     */
    @Override
    protected void startUp() {
        this.overlayManager.add(overlay);
        this.overlayManager.add(houseOverlay);
        this.overlayManager.add(patrolOverlay);
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            loadConfig();
        }
    }

    /**
     * Handle shutdown clean up
     */
    @Override
    protected void shutDown() {
        this.overlayManager.remove(overlay);
        this.overlayManager.remove(houseOverlay);
        this.overlayManager.remove(patrolOverlay);

        if (client.getLocalPlayer() != null && isInPisc(client.getLocalPlayer().getWorldLocation())) {
            client.clearHintArrow();
        }
    }

    /**
     * Specify our config
     * @param configManager The Runelite Config manager
     * @return The configuration for this plugin
     */
    @Provides
    StealingArtefactsConfig providesConfig(ConfigManager configManager) {
        return configManager.getConfig(StealingArtefactsConfig.class);
    }


    @Subscribe
    public void onGameStateChanged(GameStateChanged e) {
        if (e.getGameState() == GameState.LOGGING_IN || e.getGameState() == GameState.LOGIN_SCREEN || e.getGameState() == GameState.HOPPING) {
            markedNPCs.clear();
            markedObjects.clear();
        }
        if (e.getGameState() == GameState.LOGGED_IN && !(isInPisc(client.getLocalPlayer().getWorldLocation()))) {
            markedNPCs.clear();
            markedObjects.clear();
        }
    }
    @Subscribe
    public void onConfigChanged(ConfigChanged c) {
        if (!c.getGroup().equalsIgnoreCase(StealingArtefactsConfig.GROUP_NAME)) {
            return;
        }
        handleRefreshCurrentHouse();
    }

    /**
     * Configs are username-local, so load if we change username
     * @param e The UsernameChanged event
     */
    @Subscribe
    public void onUsernameChanged(UsernameChanged e)
    {
        if (client.getGameState() == GameState.LOGIN_SCREEN || client.getGameState() == GameState.LOGGED_IN) {
            loadConfig();
        }
    }

    /**
     * On game tick, update the hint arrow if necessary
     * @param event The GameTick event
     */
    @Subscribe
    public void onGameTick(GameTick event) {
        if (client.getLocalPlayer() != null && isInPisc(client.getLocalPlayer().getWorldLocation())) {
            if (currentState != null && client.getLocalPlayer() != null) {
                if (currentState == StealingArtefactsState.FAILURE || currentState == StealingArtefactsState.DELIVER_ARTEFACT) {
                    if (captainKhaled != null) {
                        client.setHintArrow(captainKhaled);
                    }
                } else if (currentState != StealingArtefactsState.NO_TASK) {
                    WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();
                    if (playerPos.distanceTo(currentState.getHintLocation()) > 3) {
                        client.setHintArrow(currentState.getHintLocation());
                    }
                }
            }
        } else {
            if (client.getHintArrowPoint() != null) {
                if (isInPisc(client.getHintArrowPoint())) {
                    client.clearHintArrow();
                }
            }
        }
    }

    /**
     * Check if we should mark a game object when it spawns
     * @param event The GameObjectSpawnedEvent
     */
    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if (shouldMarkObject(event.getGameObject())) {
            markedObjects.add(event.getGameObject());
        }
    }

    /**
     * Remove a game object if we currently have it marked when it despawns
     * @param event The GameObjectDespawned event
     */
    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if (event.getGameObject() != null && !markedObjects.isEmpty()) {
            markedObjects.remove(event.getGameObject());
        }
    }

    /**
     * Capture npc spawns for Captain Khaled and Patrolmen/women
     * @param event The NPCSpawned event
     */
    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc().getId() == NpcID.CAPTAIN_KHALED_6972) {
            captainKhaled = event.getNpc();
        }
        if (event.getNpc().getId() >= Constants.PATROL_ID_MIN && event.getNpc().getId() <= Constants.PATROL_ID_MAX) {
            markedNPCs.add(event.getNpc());
            log.debug("NPC Spawned: {} Index: {}", event.getNpc().getName(), event.getNpc().getId());
        }
    }

    /**
     * Handle NPCs despawning (Captain Khaled, Patrolmen/women)
     * @param event The NPCDespawned event
     */
    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        if (event.getNpc() == captainKhaled) {
            captainKhaled = null;
            if (currentState == StealingArtefactsState.DELIVER_ARTEFACT || currentState == StealingArtefactsState.FAILURE) {
                client.clearHintArrow();
            }
        }

        log.debug("NPC Spawned: {} Index: {}", event.getNpc().getName(), event.getNpc().getId());
        markedNPCs.remove(event.getNpc());
    }

    /**
     * Handle the stealing artefacts varbit change
     * TODO: Change to use Varbit in Runelite API once added
     * @param event The VarbitChanged event
     */
    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        StealingArtefactsState state = StealingArtefactsState.values()[client.getVarbitValue(Constants.STEALING_ARTEFACTS_VARBIT)];
        if (state != null) {
            updateState(state);
        }

        if ((state == StealingArtefactsState.DELIVER_ARTEFACT || state == StealingArtefactsState.FAILURE)) {
            if (captainKhaled != null) {
                client.setHintArrow(captainKhaled);
            } else {
                if (client.getLocalPlayer() != null && isInPisc(client.getLocalPlayer().getWorldLocation())) {
                    client.clearHintArrow();
                }
            }
            markedObjects.clear();
        } else if (state == StealingArtefactsState.NO_TASK) {
            if (client.getLocalPlayer() != null && isInPisc(client.getLocalPlayer().getWorldLocation())) {
                client.clearHintArrow();
            }
            markedObjects.clear();
        } else {
            client.setHintArrow(state.getHintLocation());
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged e) {
        if (client.getLocalPlayer() != null && isInPisc(client.getLocalPlayer().getWorldLocation())) {
            artefactsToGoal = StealingArtefactsUtil.artefactsToNextLevel(client, xpTrackerService);
            log.debug("Artefacts to goal: {}", artefactsToGoal);
        }
    }

    private void handleRefreshCurrentHouse() {
        final String stateGroup = StealingArtefactsConfig.GROUP_NAME + "." + client.getUsername();
        String configStateText = configManager.getConfiguration(stateGroup, StealingArtefactsConfig.CURRENT_STATE_KEY);
        if (configStateText != null) {
            int configState = Integer.parseInt(configStateText);
            if (configState != -1) {
                currentState = StealingArtefactsState.values()[configState];
            }

            if (client.getLocalPlayer() != null && isInPisc(client.getLocalPlayer().getWorldLocation())) {
                client.clearHintArrow();
                if (currentState == StealingArtefactsState.DELIVER_ARTEFACT || currentState == StealingArtefactsState.FAILURE) {
                    if (captainKhaled != null) {
                        client.setHintArrow(captainKhaled);
                    }
                    markedObjects.clear();
                } else if (currentState != StealingArtefactsState.NO_TASK) {
                    client.setHintArrow(currentState.getHintLocation());
                }
            }
        }
    }
    /**
     * Load the user config
     */
    public void loadConfig() {
        handleRefreshCurrentHouse();
    }

    /**
     * Check if we should be marking this object - for both drawers and ladders
     * @param object The game object to check
     * @return True if we should mark it, false if we shouldn't.
     */
    public boolean shouldMarkObject(GameObject object) {
        boolean shouldMark = false;
        if (currentState != null && currentState.getDrawerId() != -1) {
            shouldMark  = object.getId() == currentState.getDrawerId();
        }
        if (currentState != null && currentState.getLadderId() != -1 && (object.getWorldLocation().distanceTo(currentState.getLadderLocation()) == 0)) {
            shouldMark  = object.getId() == currentState.getLadderId();
        }
        return shouldMark;
    }

    /**
     * Update the state in the plugin and the config
     * @param state The state to set
     */
    private void updateState(StealingArtefactsState state) {
        final String stateGroup = StealingArtefactsConfig.GROUP_NAME + "." + client.getUsername();

        if (state == currentState) {
            return;
        }

        if (state == null) {
            currentState = null;
            configManager.unsetConfiguration(stateGroup, StealingArtefactsConfig.CURRENT_STATE_KEY);
        } else {
            currentState = state;
            configManager.setConfiguration(stateGroup, StealingArtefactsConfig.CURRENT_STATE_KEY, state.ordinal());
        }
    }

    /**
     * Check if the user is in Port Pisc
     * @param position The user's position in the world
     * @return True if they are in Port Pisc, otherwise false
     */
    public boolean isInPisc(WorldPoint position) {
        if (position.getX() >= 1739 && position.getX() <= 1860) {
            return position.getY() >= 3675 && position.getY() <= 3803;
        }

        return false;
    }

    /**
     * Check if the applicable guards is facing the lured direction
     * @param guard The NPC object to check
     * @return True if guard is facing correct position, otherwise false
     */
    public boolean isGuardLured(NPC guard) {
        boolean isLured = false;
        int eastGuardDistance = guard.getWorldLocation().distanceTo(EAST_GUARD_POS);
        int southEastGuardDistance = guard.getWorldLocation().distanceTo(SOUTHEAST_GUARD_POS);

        if (eastGuardDistance == 0 && guard.getCurrentOrientation() == Constants.SOUTH) {
            isLured = true;
            eastGuardLured = true;
        } else if (eastGuardDistance == 0) {
            eastGuardLured = false;
        }

        if (southEastGuardDistance == 0 && guard.getCurrentOrientation() == Constants.WEST) {
            isLured = true;
            southEastGuardLured = true;
        } else if (southEastGuardDistance == 0) {
            southEastGuardLured = false;
        }

        return isLured;
    }
}
