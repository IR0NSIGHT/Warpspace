package me.iron.WarpSpace.Mod;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.12.2020
 * TIME: 13:52
 * mod-owned event
 * fired whenever an entity enters or leaves warp
 */
public class WarpJumpEvent extends Event {
    private SegmentController ship;

    private WarpJumpType type;

    private Vector3i start;

    private Vector3i end;

    public enum WarpJumpType
    {
        ENTRY,
        DROP,
        EXIT,
        INWARP,
        TRANSWARP
    }

    /**
     * constructor
     * @param ship ship
     * @param type jumptype
     * @param start start sector
     * @param end end sector
     */
    public WarpJumpEvent(SegmentController ship, WarpJumpType type, Vector3i start, Vector3i end) {
        this.ship = ship;
        this.start = start;
        this.end = end;
        this.type = type;
    }

    /**
     * get segmentcontroller that is warping
     * @return ship
     */
    public SegmentController getShip() {
        return ship;
    }

    /**
     * get type of warpevent
     * @return type
     */
    public WarpJumpType getType() {
        return type;
    }

    /**
     * get origin sector of jump
     * @return sector
     */
    public Vector3i getStart() {
        return start;
    }

    /**
     * get target sector of jump
     * @return sector
     */
    public Vector3i getEnd() {
        return end;
    }

    /**
     * cancel warpevent
     * @param pilotMessage message to be displayed to pilots for cause of failed warp
     */
    public void cancel(String pilotMessage) {
        //TODO add message and cancel event
        //display string to pilots
        ship.sendControllingPlayersServerMessage(Lng.astr(pilotMessage),ServerMessage.MESSAGE_TYPE_WARNING);
        this.canceled = true;
    }

    public String toString() {
        String s = "WarpJumpEvent," + type + "from: " +start + "to " + end + "for" + this.ship.getName() + "at " + System.currentTimeMillis();
        return s;
    }
}
