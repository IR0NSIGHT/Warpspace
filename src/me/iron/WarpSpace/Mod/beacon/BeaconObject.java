package me.iron.WarpSpace.Mod.beacon;

import api.utils.game.SegmentControllerUtils;
import me.iron.WarpSpace.Mod.WarpManager;
import me.iron.WarpSpace.Mod.client.sounds.VoiceAnnouncer;
import me.iron.WarpSpace.Mod.client.sounds.WarpSounds;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.EntityRequest;
import org.schema.game.server.data.GameServerState;

import java.io.Serializable;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 25.10.2021
 * TIME: 12:11
 * represents the segmentcontroller that is the beacon
 */
public class BeaconObject implements Serializable {
    //important values
    private final String UID;
    private SimpleTransformableSendableObject.EntityType entityType;

    //runtime values (get updated from server to client)
    private Vector3i position = new Vector3i();
    private int factionID;
    private int strength;
    private boolean godMode; //allows to skip all checks and remain as an active beacon.
    private boolean active; //state of beacon. toggled from outside
    private boolean flagForDelete;
    private boolean loaded;

    //display values
    private String name;

    /** update with values from dummy object and fire any events connected to runtime values.
     *
     * @param dummy
     */
    public void synchFromDummy(BeaconObject dummy) {
        this.setPosition(dummy.position);
        this.setFactionID(dummy.factionID);
        this.setStrength(dummy.strength);
        this.setGodMode(dummy.godMode);
        this.setActive(dummy.active);
        if (dummy.isFlagForDelete())
            setFlagForDelete();
        this.setLoaded(dummy.loaded);
        this.setName(dummy.name);
    }

    public BeaconObject(SegmentController s) {
        this.position = s.getSector(position);
        this.loaded = true;
        this.UID = s.getUniqueIdentifier();
        this.name = s.getName();

        this.factionID = s.getFactionId();

        this.strength = getBeaconStrength(s);
        this.entityType = s.getType();
    }

    void update() {
        if (GameServerState.instance == null)
            return;

        if (godMode)
            return;

        boolean existsDBorLoaded = EntityRequest.existsIdentifierWOExc(GameServerState.instance,UID);
        if (!existsDBorLoaded) {
            setFlagForDelete();
            return;
        }

        //test if loaded
        SegmentController beaconSC = GameServerState.instance.getSegmentControllersByName().get(UID);
        if (beaconSC != null && beaconSC.isFullyLoadedWithDock())
            updateLoaded(beaconSC);
    }

    //will attempt to execute the beacon module of this segementcontroller. used when objects are loaded in.
    void activateAddon(SegmentController s) {
        WarpBeaconAddon addon = WarpBeaconAddon.getAddon(s);
        if (addon == null || !addon.isPlayerUsable())
            return;
        addon.setCharges(1);
        addon.executeModule();
        addon.sendChargeUpdate();
    }

    private void updateLoaded(SegmentController sc) {
        if (!sc.getSector(new Vector3i()).equals(position)) {
            setFlagForDelete();
            //DebugUI.echo("DELETE BEACON: WRONG POSITION FOR BEACON",null);
            return;
        }
        boolean isHB = (sc instanceof SpaceStation && sc.isHomeBase());
        if (sc instanceof ManagedUsableSegmentController) {
            ManagedUsableSegmentController msc = (ManagedUsableSegmentController)sc;
            ReactorElement beaconChamber = SegmentControllerUtils.getChamberFromElement(msc,WarpBeaconAddon.beaconChamber);
            //beaconchamber is null after loading. -> is that an issue that gets solved by waiting for th chamber to be loaded in?
            if (beaconChamber == null) {
                setFlagForDelete();
                return;
            }
            if (sc.isCoreOverheating() || isHB || beaconChamber.isDamagedRec()) {
                setFlagForDelete();
                return;
            }
            WarpBeaconAddon addon = WarpBeaconAddon.getAddon(msc);
            if (addon == null) //seems to sometimes just randomly be null?
                return;
           //    //TODO playerusable for unmanned craft too?

        }

    }

    private int getBeaconStrength(SegmentController s) {
        return 1;
    }

    //getter and setter
    public void setFlagForDelete() {
        flagForDelete = true;
    } //TODO only delete if beacon chamber is gone

    public boolean isFlagForDelete() {
        return flagForDelete;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public Vector3i getPosition() {
        return position;
    }

    public boolean isGodMode() {
        return godMode;
    }

    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }

    public String getUID() {
        return UID;
    }

    public boolean isActive() {
        return active;
    }

    public void setFactionID(int id) {
        this.factionID = id;
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            if (GameClientState.instance != null) {
                Vector3i ownWarpPos = WarpManager.getWarpSpacePos(GameClientState.instance.getPlayer().getCurrentSector());
                Vector3i beaconPos = WarpManager.getWarpSpacePos(getPosition());
                if (ownWarpPos.equals(beaconPos)) ;
                    /*WarpSounds.instance.queueSound(
                            (active? WarpSounds.SoundEntry.beacon_activated: WarpSounds.SoundEntry.beacon_deactivated), VoiceAnnouncer.queueID
                    );

                     */
            }
        }
        this.active = active;
    }

    public void setPosition(Vector3i position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "BeaconObject{" +
                "position=" + position +
                ", factionID=" + factionID +
                ", strength=" + strength +
                ", godMode=" + godMode +
                ", active=" + active +
                ", name='" + name + '\'' +
                ", flagForDelete=" + flagForDelete +
                ", loaded=" + loaded +
                '}';
    }
}
