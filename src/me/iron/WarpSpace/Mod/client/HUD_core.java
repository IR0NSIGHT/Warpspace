package me.iron.WarpSpace.Mod.client;

import api.listener.Listener;
import api.listener.events.gui.HudCreateEvent;
import api.mod.StarLoader;
import me.iron.WarpSpace.Mod.TimedRunnable;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import api.utils.StarRunnable;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.12.2020
 * TIME: 19:23
 */
public class HUD_core {

    public static List<HUD_element> elementList = new ArrayList<>();
    public static HashMap<SpriteList, Integer> drawList = new HashMap<>();
    /**
     * some general HUD element placements to use a position references. any element built with these will move + scale with them
     */
    public static HUD_element console = new HUD_element(
            new Vector3f((float)1700/1920,(float)1000/1080,0f),
            new Vector3f((float)0.75/1080,(float)0.75/1080,(float)1/1080),
            new Vector3f( 1,1,1),
            SpriteList.PEARL,
            HUD_element.ElementType.BACKGROUND);

    /**
     * init method for HUD stuff.
     */
    public static void HUDLoop() {
        new StarRunnable() {
            PlayerState player = GameClientState.instance.getPlayer();

            int tenthSeconds = 0;
            long lastTime = System.currentTimeMillis();
            @Override
            public void run() {
                if (player == null || player.getCurrentSector() == null) { //nullpointer check to avoid drawing before player spawns.
                    player = GameClientState.instance.getPlayer();
                } else {
                    if (GameServerState.isShutdown()) {
                        cancel();
                    }
                    UpdateHUD();

                    SimpleTransformableSendableObject playerShip = player.getFirstControlledTransformableWOExc();

                    //turn of HUD if player is not controlling a ship
                    if (null == playerShip || !playerShip.isSegmentController() || GameClientState.instance.isInAnyBuildMode()) {
                        for (HUD_element.ElementType type: HUD_element.ElementType.values()) {
                            HUDElementController.drawType(type,0);
                        }
                        return;
                    }

                    //not server situation dependent, 100% passive
                    HUDElementController.drawType(HUD_element.ElementType.BACKGROUND,1);
                    HUDElementController.drawElement(SpriteList.SPIRAL,true);

                    //situation dependend HUD, imperative
                    if (WarpProcess.IS_IN_WARP.isTrue()) {
                        HUDElementController.drawElement(SpriteList.PEARL,true);
                        HUDElementController.drawElement(SpriteList.ARROW_TO_RSP,true);

                    } else {
                        HUDElementController.drawElement(SpriteList.ARROW_TO_WARP,true);
                        HUDElementController.clearType(HUD_element.ElementType.PEARL);
                    }

                    if (WarpProcess.IS_IN_WARP.isTrue() && (WarpProcess.WARP_STABILITY.getCurrentValue() < 100) && (WarpProcess.WARP_STABILITY.getPreviousValue()>WarpProcess.WARP_STABILITY.getCurrentValue()) && ((tenthSeconds % 8) <= 4)) {
                        //do blinking drop icon
                        HUDElementController.drawElement(SpriteList.ARROW_TO_RSP_JUMP,true);
                    }

                    if (WarpProcess.JUMPENTRY.isTrue() && ((tenthSeconds % 8) <= 4)) {
                        HUDElementController.drawElement(SpriteList.ARROW_TO_WARP_JUMP,true);
                    }

                    if (WarpProcess.WARPSECTORBLOCKED.isTrue()) {
                        if (WarpProcess.IS_IN_WARP.isTrue()) { //sector locked down
                            HUDElementController.drawElement(SpriteList.PEARL_BLOCKED,true);
                            HUDElementController.drawElement(SpriteList.SPIRAL_BLOCKED,true);

                        } else { //no jump upwards
                            HUDElementController.drawElement(SpriteList.ARROW_TO_WARP_BLOCKED,true);
                        }
                    }

                    if (WarpProcess.RSPSECTORBLOCKED.isTrue()) {
                        if (WarpProcess.IS_IN_WARP.isTrue()) {
                            HUDElementController.drawElement(SpriteList.ARROW_TO_RSP_BLOCKED,true);
                        } else {
                            HUDElementController.drawElement(SpriteList.SPIRAL_BLOCKED,true);
                        }
                    }
                }

                //precise timer handling (not super precise but better than serverticks)
                if (System.currentTimeMillis() - lastTime > 100) {
                    //0.1 second passed
                    tenthSeconds++;
                    lastTime = System.currentTimeMillis();
                }
                if (tenthSeconds > 1000) {
                    tenthSeconds = 0;
                }
            }
        }.runTimer(WarpMain.instance,1);

        StarLoader.registerListener(HudCreateEvent.class, new Listener<HudCreateEvent>() {
            @Override
            public void onEvent(HudCreateEvent hudCreateEvent) {
                new TimedRunnable(500,WarpMain.instance, -1){
                    @Override
                    public void onRun() {
                        updateVanillaHUD();
                    }
                };
                initRadarSectorGUI();
            }
        }, WarpMain.instance);


    }

    /**
     * initialize the list of hud elements, add all entries into the drawList.
     */
    public static void initList() {
        elementList.add(new HUD_element(console,SpriteList.BORDER, HUD_element.ElementType.BACKGROUND));

        elementList.add(new HUD_element(console, SpriteList.SPIRAL, HUD_element.ElementType.SPIRAL));
        elementList.add(new HUD_element(console, SpriteList.SPIRAL_BLOCKED, HUD_element.ElementType.SPIRAL));


        elementList.add(new HUD_element(console, SpriteList.PEARL, HUD_element.ElementType.PEARL));
        elementList.add(new HUD_element(console, SpriteList.PEARL_BLOCKED, HUD_element.ElementType.PEARL));

        elementList.add(new HUD_element(console, SpriteList.ARROW_TO_RSP, HUD_element.ElementType.ARROW));
        elementList.add(new HUD_element(console, SpriteList.ARROW_TO_RSP_JUMP, HUD_element.ElementType.ARROW));
        elementList.add(new HUD_element(console, SpriteList.ARROW_TO_RSP_BLOCKED, HUD_element.ElementType.ARROW));
        elementList.add(new HUD_element(console, SpriteList.ARROW_TO_WARP, HUD_element.ElementType.ARROW));
        elementList.add(new HUD_element(console, SpriteList.ARROW_TO_WARP_BLOCKED, HUD_element.ElementType.ARROW));
        elementList.add(new HUD_element(console, SpriteList.ARROW_TO_WARP_JUMP, HUD_element.ElementType.ARROW));


        for (HUD_element e : elementList) {
            drawList.put(e.enumValue,0);
        }
    }

    /**
     * overwrite custom radar if necessary
     */
    public static void UpdateHUD() {
        if (GameClientState.instance == null)
            return;
        if (GameClientState.instance.getPlayer().getCurrentSector().length()<5000 || WarpManager.isInWarp(GameClientState.instance.getPlayer().getCurrentSector()))
            initRadarSectorGUI();
    }

    /**
     * update neighbour sector names.
     */
    private static void updateVanillaHUD() {
        if (GameClientState.instance == null || GameClientState.instance.getPlayer() == null)
            return;

        if (!WarpManager.isInWarp(GameClientState.instance.getPlayer().getCurrentSector()))
            return;

        HudIndicatorOverlay overlay = GameClientState.instance.getWorldDrawer().getGuiDrawer().getHud().getIndicator();
        for (int i = 0; i < overlay.neighborSectorsNames.length; i++) {
            overlay.neighborSectorsNames[i] = "[WARP]\n"+ WarpManager.getRealSpacePos(overlay.neighborSectorsPos[i]);
        }

    }

    /**
     * replaces the text of GUIText under radar so that "warp" is shown when in warp.
     */
    public static void initRadarSectorGUI() {
        try {
            GUITextOverlay sectorPosGUI = GameClientState.instance.getWorldDrawer().getGuiDrawer().getHud().getRadar().getLocation();
            sectorPosGUI.setTextSimple(new Object(){
                @Override
                public String toString() {
                    try {
                        Vector3i sector = GameClientState.instance.getPlayer().getCurrentSector();
                        boolean inWarp = WarpManager.isInWarp(sector);

                        //im funny
                        if (sector.equals(69,69,69))
                            return "nice.";
                        Vector3i drop =  WarpJumpManager.getDropPoint(sector);
                        boolean isBeacon = WarpJumpManager.isDroppointShifted(sector);
                        return inWarp?"[WARP]\n"+(isBeacon?"B ":"")+drop.toStringPure():sector.toStringPure();
                    } catch (Exception e) {
                        return "error";
                    }
                }
            });
        }catch (NullPointerException ignored) {

        }
    }
}
