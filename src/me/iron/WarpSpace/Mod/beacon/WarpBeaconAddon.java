package me.iron.WarpSpace.Mod.beacon;

import api.DebugFile;
import api.ModPlayground;
import api.config.BlockConfig;
import api.listener.Listener;
import api.listener.events.register.RegisterAddonsEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.addon.SimpleAddOn;
import api.utils.game.SegmentControllerUtils;
import me.iron.WarpSpace.Mod.WarpMain;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.SingleModuleActivation;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.data.GameServerState;

import java.util.Arrays;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 26.10.2021
 * TIME: 19:31
 */
public class WarpBeaconAddon extends SimpleAddOn {
    private  float powerCost = 10000;
    public static final String UIDName = "WARP_BEACON_SIMPLE";
    public static ElementInformation beaconChamber;
    public static void registerChamberBlock() {
        //short rootID = (short) ElementKeyMap.getInfo().chamberRoot;
         StarMod mod = WarpMain.instance;
         short rootID = ElementKeyMap.REACTOR_CHAMBER_JUMP;
        beaconChamber = BlockConfig.newChamber(mod, "Warp Beacon", rootID);
        beaconChamber.chamberCapacity = 0.5f;
        beaconChamber.setTextureId(ElementKeyMap.getInfo(rootID).getTextureIds());
        beaconChamber.setDescription("Shift the closest warp droppoint to this sector.");
        BlockConfig.add(beaconChamber);
        short moddedBlockID = beaconChamber.id;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < ElementKeyMap.infoArray.length; i++) {
            ElementInformation ei = ElementKeyMap.infoArray[i];
            out.append("idx:"+i+"\t\t"+(ei==null?"NULL":ei.toString())).append("\n");
        }
        DebugFile.log("\n\n\n\n"+out.toString()+"\n\n\n\n");
        ElementInformation ei = ElementKeyMap.getInfo(beaconChamber.id);

    }
    public static void registerAddonAddEventListener() {
        StarLoader.registerListener(RegisterAddonsEvent.class, new Listener<RegisterAddonsEvent>() {
            @Override
            public void onEvent(RegisterAddonsEvent event) {
                event.addModule(new WarpBeaconAddon(event.getContainer(),WarpMain.instance));
            }
        }, WarpMain.instance);
    }

    private BeaconObject beacon;
    public WarpBeaconAddon(ManagerContainer<?> managerContainer, StarMod starMod) {
        super(managerContainer, ElementKeyMap.REACTOR_CHAMBER_JUMP, starMod, UIDName);
    }

    @Override
    public boolean isPlayerUsable() { //called every frame (or often)
        ReactorElement warpBeaconChamber = SegmentControllerUtils.getChamberFromElement(getManagerUsableSegmentController(), beaconChamber);
        if (warpBeaconChamber == null)// || !(this.segmentController instanceof SpaceStation))
            return false;
        return super.isPlayerUsable();
    }

    @Override
    public float getChargeRateFull() { //in seconds
        return 3;
    }

    @Override
    public double getPowerConsumedPerSecondResting() {
        return 0;
    }

    @Override
    public double getPowerConsumedPerSecondCharging() {
        return 0;
    }

    @Override
    public float getDuration() {
        return -1;
    }

    @Override
    public boolean isAutoChargeOn() {
        return super.isAutoChargeOn();
    }

    @Override
    protected boolean isDeactivatableManually() {
        return true;
    }

    @Override
    public boolean onExecuteServer() {
        activation = new SingleModuleActivation();
        activation.startTime = System.currentTimeMillis();
        if (GameServerState.instance == null)
            return true;
        ModPlayground.broadcastMessage("warp beacon activated by " + this.segmentController.getName());
        beacon = new BeaconObject(this.segmentController);
        WarpMain.instance.beaconManagerServer.addBeacon(beacon);
        return true;
    }

    @Override
    public boolean onExecuteClient() {
        return true;
    }

    private int timer;
    @Override
    public void onActive() { //called every frame
        if (timer++%100==0 && beacon != null)
            beacon.update();
    }

    @Override
    public void onInactive() { //called when?
       if (beacon != null) {
           beacon.setFlagForDelete();
           WarpMain.instance.beaconManagerServer.updateBeacon(beacon);
           beacon = null;
       }
    }

    @Override
    public String getName() {
        return "Warp Beacon";
    }
}
