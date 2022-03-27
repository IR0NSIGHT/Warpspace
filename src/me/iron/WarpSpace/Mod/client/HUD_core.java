package me.iron.WarpSpace.Mod.client;

import api.listener.Listener;
import api.listener.events.gui.HudCreateEvent;
import api.mod.StarLoader;
import me.iron.WarpSpace.Mod.TimedRunnable;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import api.utils.StarRunnable;
import me.iron.WarpSpace.Mod.client.sounds.WarpSounds;
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

    public static List<HUD_element> elementList = new ArrayList(); //TODO get rid of compiler warning for raw usage
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

    public static WarpProcessController.WarpProcess playerWarpState = WarpProcessController.WarpProcess.TRAVEL;

    private static boolean isDropping = false;
    private static boolean isEntry;
    private static boolean isExit;
    private static boolean isRSPSectorBlocked;
    private static boolean isWarpSectorBlocked;
    private static Vector3f noInhibition = new Vector3f((float)1622/1920,(float)928/1080,0.01f);
    private static Vector3f onInhibition = new Vector3f((float) 1460/1920,(float) 928/1080,0f);


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
                /**
                 * this method checks for static variables like "is in warp" and decides what elements to draw on the HUD and which to disable.
                 */
                //    UpdateSituation(); //TODO make 100% event based? -> new package from server triggers GUI update
                if (player == null || player.getCurrentSector() == null) { //nullpointer check to avoid drawing before player spawns.
                    // DebugFile.log("playerstate is null or playersector is null");
                    player = GameClientState.instance.getPlayer();
                } else {
                    if (GameServerState.isShutdown()) {
                        cancel();
                    }
                    SimpleTransformableSendableObject playerShip = player.getFirstControlledTransformableWOExc();

                    //turn of HUD if player is not controlling a ship
                    if (null == playerShip || !playerShip.isSegmentController() || GameClientState.instance.isInAnyBuildMode()) {
                        for (HUD_element.ElementType type: HUD_element.ElementType.values()) {
                            HUDElementController.drawType(type,0);
                        }
                        return;
                    }


                    //draw decision making method

                    //not server situation dependent, 100% passive
                    HUDElementController.drawType(HUD_element.ElementType.BACKGROUND,1);
                    HUDElementController.drawElement(SpriteList.SPIRAL,true);

                    boolean isInWarp = WarpManager.IsInWarp(player.getCurrentSector());
                    if (isInWarp) {
                        HUDElementController.drawElement(SpriteList.PEARL,true);
                        HUDElementController.drawElement(SpriteList.ARROW_TO_RSP,true);

                    } else {
                        isDropping = false;
                        HUDElementController.drawElement(SpriteList.ARROW_TO_WARP,true);
                        HUDElementController.clearType(HUD_element.ElementType.PEARL);
                    }

                    //TODO make prettier check for processes
                    if ((isDropping || isExit) && ((tenthSeconds % 8) <= 4)) {
                        //do blinking drop icon
                        HUDElementController.drawElement(SpriteList.ARROW_TO_RSP_JUMP,true);
                    }

                    if (isEntry && ((tenthSeconds % 8) <= 4)) {
                        HUDElementController.drawElement(SpriteList.ARROW_TO_WARP_JUMP,true);
                    }

                    if (isWarpSectorBlocked) {
                        if (isInWarp) { //sector locked down
                            HUDElementController.drawElement(SpriteList.PEARL_BLOCKED,true);
                            HUDElementController.drawElement(SpriteList.SPIRAL_BLOCKED,true);

                        } else { //no jump upwards
                            HUDElementController.drawElement(SpriteList.ARROW_TO_WARP_BLOCKED,true);
                        }
                    }

                    if (isRSPSectorBlocked) {
                        if (isInWarp) {
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
                new TimedRunnable(500,WarpMain.instance){
                    @Override
                    public void onRun() {
                        updateVanillaHUD();
                    }
                };
                initRadarSectorGUI();
            }
        }, WarpMain.instance);


    }

    //TODO maybe split up in placement + available sprites?
    //TODO move to json
    //TODO get rid of elementlist, directly put into drawlist.
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
     * is called by me.iron.WarpSpace.Mod.network.PacketHUDUpdate, used to transfer information from the server to the client about what a player is currently doing related to warp.
     * Sets the received info to WarpProcessController to the ProcessMap, allows additional info in processarray. dependent on what process is updated.
     * @param value x
     * @param process x
     * @param processArray x
     */
    public static void HUD_processPacket(WarpProcessController.WarpProcess process, Integer value, List<String> processArray) {
        //TODO add method to get more precise data like time till warpdrop/jump etc.
        playerWarpState = process;
        WarpProcessController.WarpProcessMap.put(process, value);
        UpdateSituation();
    }


    /**
     * update player situation fields from WarpProcessMap
     */
    private static void UpdateSituation() {
        boolean dropOld = isDropping,
        exitOld = isExit,
        entryOld = isEntry,
        rspBlocked = isRSPSectorBlocked,
        warpBlocked = isWarpSectorBlocked;

        //DebugFile.log("updating warp situation from WarpProcessMap: ");

        //FIXME gives false positive on velocity close to 50: 65m/s -> fix for now: test clientside speed.
        isDropping = ( WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPDROP) == 1);

        isExit = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPEXIT) == 1);

        isEntry = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.JUMPENTRY) == 1);

        isRSPSectorBlocked = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.RSPSECTORBLOCKED) == 1);

        isWarpSectorBlocked = (WarpProcessController.WarpProcessMap.get(WarpProcessController.WarpProcess.WARPSECTORBLOCKED) == 1);

        //todo build listener/event system
        if (!dropOld && isDropping && WarpManager.IsInWarp(GameClientState.instance.getPlayer().getCurrentSector())
        && GameClientState.instance.getPlayer().getFirstControlledTransformableWOExc().getSpeedCurrent() <  WarpManager.minimumSpeed) { //now dropping
            WarpSounds.instance.queueSound(WarpSounds.Sound.dropping);
        }

        if (!exitOld && isExit) { //now exit
            WarpSounds.instance.queueSound(WarpSounds.Sound.warping);
        }

        if (!entryOld && isEntry) { //now warping
            WarpSounds.instance.queueSound(WarpSounds.Sound.warping);
        }

        if (!rspBlocked && isRSPSectorBlocked) { //now rps blocked
            WarpSounds.instance.queueSound(WarpSounds.Sound.inhibitor_detected);

        }

        if (!warpBlocked && isWarpSectorBlocked) { //now warp blocked
            WarpSounds.instance.queueSound(WarpSounds.Sound.inhibitor_detected);
        }

        if ((warpBlocked && !isWarpSectorBlocked) || (rspBlocked && !isRSPSectorBlocked)) { //not inhibited anymore.
            //TODO "inhibitor gone/free to jump again" sound
        }




        if (GameClientState.instance == null)
            return;
        if (GameClientState.instance.getPlayer().getCurrentSector().length()<5000 || WarpManager.IsInWarp(GameClientState.instance.getPlayer().getCurrentSector()))
            initRadarSectorGUI(); //TODO once derp fixed the damn buildsector overwriting warpspace.
    }

    /**
     * update neighbour sector names.
     */
    private static void updateVanillaHUD() {
        if (GameClientState.instance == null || GameClientState.instance.getPlayer() == null)
            return;

        if (!WarpManager.IsInWarp(GameClientState.instance.getPlayer().getCurrentSector()))
            return;

        HudIndicatorOverlay overlay = GameClientState.instance.getWorldDrawer().getGuiDrawer().getHud().getIndicator();
        for (int i = 0; i < overlay.neighborSectorsNames.length; i++) {
            overlay.neighborSectorsNames[i] = "[WARP]\n"+ WarpManager.getRealSpacePos(overlay.neighborSectorsPos[i]);
        }

        //Radar.location ==> sector coord HUD under radar
    }

    /**
     * replaces the text of GUIText under radar so that "warp" is shown when in warp.
     */
    public static void initRadarSectorGUI() {
        GUITextOverlay sectorPosGUI = GameClientState.instance.getWorldDrawer().getGuiDrawer().getHud().getRadar().getLocation();
        sectorPosGUI.setTextSimple(new Object(){
            @Override
            public String toString() {
                try {
                    Vector3i sector = GameClientState.instance.getPlayer().getCurrentSector();
                    boolean inWarp = WarpManager.IsInWarp(sector);

                    //im funny
                    if (sector.equals(69,69,69))
                        return "nice.";
                    Vector3i drop =  WarpJumpManager.getDropPoint(sector);
                    return inWarp?"[WARP]\n"+drop.toStringPure():sector.toStringPure();
                } catch (Exception e) {
                    return "error";
                }
            }
        }); //.getText().clear();
    }
}
