package io.cbitler.stealingartefacts;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.client.plugins.xptracker.XpTrackerService;
import net.runelite.client.ui.overlay.OverlayManager;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

@PluginDescriptor(
        name = "Stealing Artefacts",
        description = "Show the current house for stealing artefacts",
        tags = {}
)
@PluginDependency(XpTrackerPlugin.class)
@Slf4j
public class StealingArtefactsPlugin extends Plugin {
    public static final WorldPoint EAST_GUARD_POS = new WorldPoint(1777, 3746, 0);
    public static final WorldPoint SOUTHEAST_GUARD_POS = new WorldPoint(1780, 3731, 0);
    public static final WorldPoint CAPTAIN_KHALED_ROUGH_POS = new WorldPoint(1845, 3751, 0);
    private final AtomicReference<GameState> gameState = new AtomicReference<>();
    public @Nullable StealingArtefactsState taskState = null;
    public HashSet<TileObject> markedObjects = new HashSet<>();
    public HashSet<NPC> markedNPCs = new HashSet<>();
    public boolean eastGuardLured = false;
    public boolean southEastGuardLured = false;
    public NPC captainKhaled = null;

    /**
     * Stores the last calculated number of artefacts to reach your xp goal
     */
    public int artefactsToGoal = -1;
    private @Inject OverlayManager overlayManager;
    private @Inject StealingArtefactsOverlay overlay;
    private @Inject StealingArtefactsHouseOverlay houseOverlay;
    private @Inject StealingArtefactsPatrolOverlay patrolOverlay;
    private @Inject Client client;
    private @Inject ClientThread clientThread;
    private @Inject XpTrackerService xpTrackerService;

    /**
     * Set to true when the plugin needs to check game objects to overlay
     * Only necessary when the player has just logged in
     */
    private boolean revalidateObjects = false;

    /**
     * Handle plugin startup
     */
    @Override
    protected void startUp() {
        this.overlayManager.add(overlay);
        this.overlayManager.add(houseOverlay);
        this.overlayManager.add(patrolOverlay);

        if (client.getGameState() == GameState.LOGGED_IN) {
            clientThread.invokeLater(() -> {
                updateTaskVarbit(client.getVarbitValue(Constants.STEALING_ARTEFACTS_VARBIT));
            });
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

        if (isPlayerInPisc()) {
            // NOTE: This is not technically 100% safe
            client.clearHintArrow();
        }

        gameState.lazySet(null);
    }

    /**
     * Specify our config
     *
     * @param configManager The Runelite Config manager
     * @return The configuration for this plugin
     */
    @Provides
    StealingArtefactsConfig providesConfig(ConfigManager configManager) {
        return configManager.getConfig(StealingArtefactsConfig.class);
    }


    @Subscribe
    public void onGameStateChanged(GameStateChanged e) {
        var newState = e.getGameState();
        if (newState == GameState.LOADING) {
            this.markedObjects.clear();
            if (isPlayerInPisc()) {
                revalidateObjects = true;
            }
            return;
        }

        var previousState = gameState.getAndSet(newState);
        if (newState == previousState) {
            return;
        }

        if (newState != GameState.HOPPING && newState != GameState.LOGGED_IN) {
            reset();
        }

        if (newState == GameState.LOGGED_IN) {
            this.markedObjects.clear();
            if (this.isPlayerInPisc()) {
                this.revalidateObjects = true;
            }
        }
    }

    /**
     * On game tick, update the hint arrow if necessary
     *
     * @param event The GameTick event
     */
    @Subscribe
    public void onGameTick(GameTick event) {
        if (revalidateObjects) {
            var tiles = client.getScene().getTiles();
            for (var outerTiles : tiles) {
                for (var lineOfTiles : outerTiles) {
                    for (var tile : lineOfTiles) {
                        if (tile != null) {
                            for (var object : tile.getGameObjects()) {
                                if (shouldMarkObject(object)) {
                                    markedObjects.add(object);
                                }
                            }
                        }
                    }
                }
            }

            revalidateObjects = false;
        }
    }

    /**
     * Check if we should mark a game object when it spawns
     *
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
     *
     * @param event The GameObjectDespawned event
     */
    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if (event.getGameObject() != null) {
            markedObjects.remove(event.getGameObject());
        }
    }

    /**
     * Capture npc spawns for Captain Khaled and Patrolmen/women
     *
     * @param event The NPCSpawned event
     */
    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc().getId() == NpcID.CAPTAIN_KHALED_6972) {
            captainKhaled = event.getNpc();
            if (taskState == StealingArtefactsState.DELIVER_ARTEFACT || taskState == StealingArtefactsState.FAILURE) {
                client.setHintArrow(captainKhaled);
            }
        }
        if (event.getNpc().getId() >= Constants.PATROL_ID_MIN && event.getNpc().getId() <= Constants.PATROL_ID_MAX) {
            markedNPCs.add(event.getNpc());
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged npcChanged) {
        markedNPCs.remove(npcChanged.getNpc());

        if (npcChanged.getNpc().getId() == NpcID.CAPTAIN_KHALED_6972) {
            captainKhaled = npcChanged.getNpc();
        }
        if (npcChanged.getNpc().getId() >= Constants.PATROL_ID_MIN && npcChanged.getNpc().getId() <= Constants.PATROL_ID_MAX) {
            markedNPCs.add(npcChanged.getNpc());
        }
    }

    /**
     * Handle NPCs despawning (Captain Khaled, Patrolmen/women)
     *
     * @param event The NPCDespawned event
     */
    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        var npc = event.getNpc();

        if (npc == captainKhaled) {
            if ((taskState == StealingArtefactsState.DELIVER_ARTEFACT || taskState == StealingArtefactsState.FAILURE) && isPlayerInPisc()) {
                // Player is in pisc & the current task state is to talk to Khaled
                // Since we can't highlight the NPC, highlight the NPCs rough position
                client.setHintArrow(CAPTAIN_KHALED_ROUGH_POS);
            } else if (client.getHintArrowNpc() == captainKhaled) {
                // Clear the hint arrow if it was previously pointing at Captain Khaled
                client.clearHintArrow();
            }

            captainKhaled = null;
        } else {
            markedNPCs.remove(npc);
        }
    }

    private void reset() {
        markedNPCs.clear();
        markedObjects.clear();
    }

    private void updateTaskVarbit(int value) {
        var newState = StealingArtefactsState.values()[value];
        taskState = newState;

        var localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            log.debug("local player not loaded yet");
            return;
        }

        switch (newState) {
            case NO_TASK:
                if (isInPisc(localPlayer.getWorldLocation())) {
                    client.clearHintArrow();
                }
                reset();
                break;

            case NORTHERN:
            case SOUTHEASTERN:
            case SOUTHERN:
            case SOUTHWESTERN:
            case WESTERN:
            case NORTHWESTERN:
                if (isInPisc(localPlayer.getWorldLocation())) {
                    var hintLocation = newState.getHintLocation();
                    if (hintLocation != null) {
                        client.setHintArrow(newState.getHintLocation());
                    } else {
                        log.warn("Invalid hint location from {}", newState);
                    }
                } else {
                    log.debug("Player not in pisc, don't update anything");
                }
                break;

            case FAILURE:
            case DELIVER_ARTEFACT:
                if (isInPisc(localPlayer.getWorldLocation())) {
                    if (captainKhaled != null) {
                        client.setHintArrow(captainKhaled);
                    } else {
                        client.setHintArrow(CAPTAIN_KHALED_ROUGH_POS);
                    }
                } else {
                    log.debug("Player not in pisc, don't update anything");
                }
                break;
        }
    }

    /**
     * Handle the stealing artefacts varbit change
     *
     * @param event The VarbitChanged event
     */
    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == Constants.STEALING_ARTEFACTS_VARBIT) {
            updateTaskVarbit(event.getValue());
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged e) {
        if (e.getSkill() != Skill.THIEVING) {
            return;
        }

        var localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }

        if (!isInPisc(localPlayer.getWorldLocation())) {
            return;
        }

        artefactsToGoal = StealingArtefactsUtil.artefactsToNextLevel(client, xpTrackerService);
    }

    /**
     * Check if we should be marking this object - for both drawers and ladders
     *
     * @param object The game object to check
     * @return True if we should mark it, false if we shouldn't.
     */
    public boolean shouldMarkObject(@Nullable GameObject object) {
        if (object == null) {
            return false;
        }
        if (taskState == null) {
            return false;
        }

        boolean shouldMark = false;
        if (taskState.getDrawerId() != -1) {
            shouldMark = object.getId() == taskState.getDrawerId();
        }
        if (taskState.getLadderId() != -1 && (object.getWorldLocation().distanceTo(taskState.getLadderLocation()) == 0)) {
            shouldMark = object.getId() == taskState.getLadderId();
        }
        return shouldMark;
    }

    /**
     * Check if the user is in Port Pisc
     *
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
     * Check if the current player is in Port Pisc
     *
     * @return false if the player not in pisc, or if the player does not exist yet, otherwise true
     */
    public boolean isPlayerInPisc() {
        var localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return false;
        }
        return isInPisc(localPlayer.getWorldLocation());
    }

    /**
     * Check if the applicable guards is facing the lured direction
     *
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
