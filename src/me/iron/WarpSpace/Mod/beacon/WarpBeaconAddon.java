package me.iron.WarpSpace.Mod.beacon;

import api.ModPlayground;
import api.config.BlockConfig;
import api.listener.Listener;
import api.listener.events.register.RegisterAddonsEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.addon.SimpleAddOn;
import api.utils.game.SegmentControllerUtils;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.client.DebugUI;
import me.iron.WarpSpace.Mod.client.sounds.WarpSounds;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

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
        beaconChamber = BlockConfig.newChamber(mod, "Warp Beacon", rootID); //the chamber is the beacon, the addon is the toggle (button)
        beaconChamber.chamberCapacity = 0.5f;
        beaconChamber.setTextureId(ElementKeyMap.getInfo(rootID).getTextureIds());
        beaconChamber.setDescription("Shift the closest warp droppoint to this sector.");
        beaconChamber.chamberPermission = ElementInformation.CHAMBER_PERMISSION_STATION;
        BlockConfig.add(beaconChamber);

    }
    public static long addonID;
    public static void registerAddonAddEventListener() {
        StarLoader.registerListener(RegisterAddonsEvent.class, new Listener<RegisterAddonsEvent>() {
            @Override
            public void onEvent(RegisterAddonsEvent event) {
                event.addModule(new WarpBeaconAddon(event.getContainer(),WarpMain.instance));
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
        if (warpBeaconChamber == null)
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
        return 1;
    }

    @Override
    public boolean isAutoChargeOn() {
        return super.isAutoChargeOn();
    }

    @Override
    protected boolean isDeactivatableManually() {
        return false;
    }

    @Override
    public boolean onExecuteServer() {
      //  DebugUI.echo("BEACON ADDON EXECUTED ON SERVER",null);
        if (!wasActive) {
            onActivation();
        }
        wasActive = true;
        return true;
    }

    @Override
    public boolean onExecuteClient() {
        return true;
    }


    @Override
    public void onActive() {
        wasActive = true;
    }

    private boolean wasActive; //on deactivation
    @Override
    public void onInactive() { //called when?
        //ModPlayground.broadcastMessage("ON INACTIVE BEACON");
        if (wasActive) {
            onDeactivation();
        }
        wasActive = false;
    }

    /**
     * called once when addon goes inactive.
     */
    private void onDeactivation() {
    }

    /**
     * called once when addon is activated
     */
    private void onActivation() {
        //ModPlayground.broadcastMessage("TOGGLE BEACON");
       // DebugUI.echo("beacon toggle on entity"+ getSegmentController().getName()+ " was activated",null);
        if (isOnServer()) {
            //get/make beacon
            beacon = WarpMain.instance.beaconManagerServer.getBeaconByUID(getSegmentController().getUniqueIdentifier());
            if (beacon == null) {
                beacon = new BeaconObject(this.segmentController);
                WarpMain.instance.beaconManagerServer.addBeacon(beacon);
            }

            //toggle beacon
            boolean on = beacon.isActive();
            beacon.setActive(!on);
            getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Beacon is now "+(beacon.isActive()?"active":"inactive")), ServerMessage.MESSAGE_TYPE_INFO);
        }
    }

    @Override
    public String getName() {
        return "Warp Beacon Toggle";
    }

    @Override
    public void setCharge(float v) {
        super.setCharge(v);
    }

    @Override
    public void setCharges(int i) {
        super.setCharges(i);
    }

    private void deactivate() {
        this.activation = null;
    }
}
