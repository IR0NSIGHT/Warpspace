package me.iron.WarpSpace.Mod.beacon;

import api.ModPlayground;
import api.utils.game.SegmentControllerUtils;
import me.iron.WarpSpace.Mod.WarpManager;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.elements.VoidElementManager;
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
    public static int basePowerCost = 1000; //cost for 1 sector distance
    public static float modifier = 0.15f; //how strong the quadratic power growth is applied.

    public static int calcPowerCost(Vector3i position) {
        Vector3i naturalDropPos = WarpManager.getRealSpacePos(WarpManager.getWarpSpacePos(position));
        naturalDropPos.sub(position);
        return  (int) ((basePowerCost + modifier * Math.pow (Math.max(1,naturalDropPos.length()-1),2))*VoidElementManager.REACTOR_POWER_CAPACITY_MULTIPLIER);
    }

    //non static stuff-----------------------------------------------------------------

    //important values
    private Vector3i position = new Vector3i();
    private String UID;
    private int factionID;
    private int strength;
    private SimpleTransformableSendableObject.EntityType entityType;
    private boolean godMode; //allows to skip all checks and remain as an active beacon.
    private int powercost;
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
        this.powercost = calcPowerCost(position);
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
        this.powercost = calcPowerCost(position);
    }


    public void update() {
        if (godMode)
            return;
        if (isFlagForDelete())
            return;

        boolean existsDBorLoaded = EntityRequest.existsIdentifierWOExc(GameServerState.instance,UID);
        if (!existsDBorLoaded) {
            setFlagForDelete();
            ModPlayground.broadcastMessage("UID doesnt exist anymore.");

            return;
        }

        //test if loaded
        SegmentController beaconSC = GameServerState.instance.getSegmentControllersByName().get(UID);
        if (beaconSC != null)
            updateLoaded(beaconSC);
    }

    private void updateLoaded(SegmentController sc) {
        if (!sc.getSector(new Vector3i()).equals(position)) {
            setFlagForDelete();
            ModPlayground.broadcastMessage("position has changed, illegal");
            return;
        }

        if (sc instanceof SpaceStation) {
            ReactorElement beaconChamber = SegmentControllerUtils.getChamberFromElement((ManagedUsableSegmentController)sc,WarpBeaconAddon.beaconChamber);
            if (beaconChamber == null || !beaconChamber.isAllValid() || beaconChamber.isDamaged()){
                setFlagForDelete();
                ModPlayground.broadcastMessage("chamber invalid/damaged/null.");
                return;
            }
            //TODO test if warpBeaconAddon is active
        //    PlayerUsableInterface beaconAddon = SegmentControllerUtils.getAddon((ManagedUsableSegmentController)sc,WarpBeaconAddon.class);
        //    if (beaconAddon.)
        } else {
            setFlagForDelete();
        }

    }

    private int getBeaconStrength(SegmentController s) {
        return 1;
    }

    //getter and setter
    public void setFlagForDelete() {
        ModPlayground.broadcastMessage("FLAG DELETE beacon: " + UID);
        flagForDelete = true;
    }

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

    public int getPowerCost() {
        return powercost;
    }
}
