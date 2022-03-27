package me.iron.WarpSpace.Mod.beacon;

import api.DebugFile;
import api.ModPlayground;
import api.utils.game.SegmentControllerUtils;
import me.iron.WarpSpace.Mod.client.DebugUI;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
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
    private Vector3i position = new Vector3i();
    private final String UID;
    private int factionID;
    private int strength;
    private SimpleTransformableSendableObject.EntityType entityType;
    private boolean godMode; //allows to skip all checks and remain as an active beacon.
    private boolean active; //state of beacon. toggled from outside

    //display values
    private String name;
    private String factionName;

    //runtime values
    transient private boolean flagForDelete;
    transient private boolean loaded;

    public BeaconObject(SegmentController s) {
        this.position = s.getSector(position);
        this.loaded = true;
        this.UID = s.getUniqueIdentifier();
        this.name = s.getName();

        this.factionID = s.getFactionId();
        if (factionID != 0)
            this.factionName = s.getFaction().getName();

        this.strength = getBeaconStrength(s);
        this.entityType = s.getType();
    }

    public BeaconObject(Vector3i position, boolean loaded, String UID, int factionID, int strength, SimpleTransformableSendableObject.EntityType entityType, boolean godMode, String name, String factionName) {
        this.position = position;
        this.loaded = loaded;
        this.UID = UID;
        this.factionID = factionID;
        this.strength = strength;
        this.entityType = entityType;
        this.godMode = godMode;
        this.name = name;
        this.factionName = factionName;
    }

    void update() {
        if (GameServerState.instance == null)
            return;

        if (godMode)
            return;

        boolean existsDBorLoaded = EntityRequest.existsIdentifierWOExc(GameServerState.instance,UID);
        if (!existsDBorLoaded) {
            DebugUI.echo("DELETE BEACON: UNLOADED+NOT EXIST IN DB",null);
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
            DebugUI.echo("DELETE BEACON: WRONG POSITION FOR BEACON",null);
            return;
        }
        boolean isHB = (sc instanceof SpaceStation && sc.isHomeBase());
        if (sc instanceof ManagedUsableSegmentController) {
            ManagedUsableSegmentController msc = (ManagedUsableSegmentController)sc;
            ReactorElement beaconChamber = SegmentControllerUtils.getChamberFromElement(msc,WarpBeaconAddon.beaconChamber);
            //beaconchamber is null after loading. -> is that an issue that gets solved by waiting for th chamber to be loaded in?
            if (beaconChamber == null) {
                setFlagForDelete();
                DebugUI.echo("DELETE BEACON: CHAMBER IS NULL",null);

                //    ModPlayground.broadcastMessage("doesnt have chamber.");
                return;
            }
            if (sc.isCoreOverheating() || isHB || beaconChamber.isDamagedRec()) {
                DebugUI.echo("DELETE BEACON: HB/CORE-OVERHEAT/CHAMBER DAMAGED",null);
                setFlagForDelete();
                return;
            }
            WarpBeaconAddon addon = WarpBeaconAddon.getAddon(msc);
            if (addon == null) //seems to sometimes just randomly be null?
                return;
            if (!addon.isActive() || !addon.isPlayerUsable()) {
                //TODO playerusable for unmanned craft too?
                setFlagForDelete();
                DebugUI.echo("DELETE BEACON: ADDON INACTIVE/NOT PLAYERUSABLE",null);
            }
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

    public String getFactionName() {
        return factionName;
    }

    public void setFactionName(String factionName) {
        this.factionName = factionName;
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

    public void setActive(boolean active) {
        this.active = active;
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
