package me.iron.WarpSpace.Mod;

import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.09.2022
 * TIME: 14:45
 */
public class InWarpRunnable extends TimedRunnable{
    private final int entityId;
    private SimpleTransformableSendableObject entity;
    static final int countdownMax = (int) ConfigManager.ConfigEntry.seconds_until_speeddrop.getValue()*1000;
    private float countdown_millis = countdownMax; //initial start value

    public InWarpRunnable(SimpleTransformableSendableObject entity) {
        super(1000,WarpMain.instance, -1);
        this.entityId = entity.getId();
        this.entity = entity;
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
                return; //Object is docked to something else
            }

            //update object
            if (WarpManager.getInstance().isInWarp(entity)) {
                updateWarp();
            } else {
                updateRSP();
            }
        } else {
            WarpEntityManager.RemoveWarpEntity(entityId);
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

        if (entity.getSpeedCurrent() < ConfigManager.ConfigEntry.minimum_warp_speed.getValue()) {
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
            System.out.println("dropping entity" + entity.getUniqueIdentifier() + " type " + entity.getClass().getName() +" CAUSE: SPEEDDROP");
            WarpJumpManager.invokeDrop(0,entity,false, false);
        }
    }

    @Override
    public void doStop() {
        super.doStop();
    }

    private void updateRSP() {
        //set to full, so that entering warp starts with f
        WarpProcess.setProcess(entity,WarpProcess.WARP_STABILITY,100);
        countdown_millis = countdownMax;
    }
}
