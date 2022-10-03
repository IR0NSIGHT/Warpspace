package me.iron.WarpSpace.Mod;

import api.ModPlayground;
import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.09.2022
 * TIME: 14:45
 */
public class InWarpRunnable extends TimedRunnable{
    private int entityId;
    private String entityName;
    private SimpleTransformableSendableObject entity;
    static final int countdownMax = (int) ConfigManager.ConfigEntry.seconds_until_speeddrop.getValue()*1000;
    private float countdown_millis = countdownMax; //initial start value

    public InWarpRunnable(SimpleTransformableSendableObject entity) {
        super(1000,WarpMain.instance, -1);
        this.entityId = entity.getId();
        this.entity = entity;
        this.entityName = entity.getName();
    }

    @Override
    public void onRun() {
        super.onRun();
        updateEntity();
        if (entity != null) {
            //skip attached players
            if (entity instanceof AbstractCharacter && entity.getGravity().source != null) {
                return;
            }

            //skip docked ships
            if (entity instanceof Ship && !((Ship)entity).railController.isRoot()) {
                ModPlayground.broadcastMessage("skip docked entity " + entity.getName());
                return; //Object is docked to something else
            }

            //update object
            if (WarpManager.isInWarp(entity)) {
                updateWarp();
            } else {
                updateRSP();
            }
        } else {
            WarpEntityManager.RemoveWarpEntity(entityId);
            ModPlayground.broadcastMessage("CANCEL " + entity.getName());
        }
    }

    private void updateEntity() {
        if (entity != null && entity.isMarkedForDeleteVolatile())
            entity = null;
    }

    private void updateWarp() {
        //update value for synching
        int stability = (int)((100* countdown_millis)/countdownMax);
        WarpProcess.setProcess(entity,WarpProcess.WARP_STABILITY,stability);
        ModPlayground.broadcastMessage(" stability: " + stability+ " countdown: " + countdown_millis);

        if (entity.getSpeedCurrent() < WarpManager.minimumSpeed) {
            //ship is to slow, dropping out of warp!
            countdown_millis -= getTimeout(); //runs once a second
        } else {
            if (countdown_millis < countdownMax) {
                countdown_millis += 2*getTimeout();
            }
        }

        if (countdown_millis > countdownMax) { //essentially caps the countdown to max_val, while allowing a start buffer of extra seconds
            countdown_millis -= getTimeout();
        }
        if (countdown_millis <= 0) {
            //drop entity out of warp.
            ModPlayground.broadcastMessage("invoke speeddrop, countdown:" + countdown_millis);
            WarpJumpManager.invokeDrop(0,entity,false, false);
        }

    }

    @Override
    public void doStop() {
        super.doStop();
        ModPlayground.broadcastMessage("HALT WARP LOOP " + entityName);
    }

    private void updateRSP() {
        //set to full, so that entering warp starts with f
        WarpProcess.setProcess(entity,WarpProcess.WARP_STABILITY,100);
        countdown_millis = countdownMax;
    }
}
