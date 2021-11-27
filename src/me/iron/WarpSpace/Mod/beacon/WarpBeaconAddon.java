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
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
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
        beaconChamber.chamberPermission = ElementInformation.CHAMBER_PERMISSION_STATION;
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
    public static long addonID;
    public static void registerAddonAddEventListener() {
        StarLoader.registerListener(RegisterAddonsEvent.class, new Listener<RegisterAddonsEvent>() {
            @Override
            public void onEvent(RegisterAddonsEvent event) {
                event.addModule(new WarpBeaconAddon(event.getContainer(),WarpMain.instance));
                DebugFile.log("################################ REGISTERES WARP BEACON ADDON FOR:" + event.getSegmentController().getName() +                 event.addons
                .toString());
            }
        }, WarpMain.instance);
    }
    public static WarpBeaconAddon getAddon(SegmentController s) {
        if (!(s instanceof ManagedUsableSegmentController))
            return null;
        ManagedUsableSegmentController msc = (ManagedUsableSegmentController)s;
        PlayerUsableInterface pui = SegmentControllerUtils.getAddon(msc,WarpBeaconAddon.addonID);
        if (pui == null || !(pui instanceof WarpBeaconAddon))
            return null;
        return (WarpBeaconAddon)pui;
    }

    private BeaconObject beacon;
    public WarpBeaconAddon(ManagerContainer<?> managerContainer, StarMod starMod) {
        super(managerContainer, ElementKeyMap.REACTOR_CHAMBER_JUMP, starMod, UIDName);
        addonID = this.usableId;
    }

    @Override
    public boolean isPlayerUsable() { //called every frame (or often)
        SegmentController sc = getSegmentController();
        if (!(sc instanceof ManagedUsableSegmentController<?>))
            return false;
        ReactorElement warpBeaconChamber = SegmentControllerUtils.getChamberFromElement((ManagedUsableSegmentController<?> )sc, beaconChamber);
        if (warpBeaconChamber == null)// || !(this.segmentController instanceof SpaceStation))
            return false;
        return super.isPlayerUsable();
    }

    @Override //time in seconds required to fully charge
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
        //ModPlayground.broadcastMessage("warp beacon activated by " + this.segmentController.getName());
        beacon = new BeaconObject(this.segmentController);
        WarpMain.instance.beaconManagerServer.addBeacon(beacon);
        return true;
    }

    @Override
    public boolean onExecuteClient() {
        return true;
    }

    @Override
    public void onActive() {
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

    @Override
    public void setCharge(float v) {
        super.setCharge(v);
    }

    @Override
    public void setCharges(int i) {
        super.setCharges(i);
    }
}
