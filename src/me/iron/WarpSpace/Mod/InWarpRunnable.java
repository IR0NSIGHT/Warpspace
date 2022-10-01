package me.iron.WarpSpace.Mod;

import api.ModPlayground;
import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

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
    final int countdownMax = (int) ConfigManager.ConfigEntry.seconds_until_speeddrop.getValue();
    float countdown = countdownMax; //initial start value

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
            if (entity instanceof SegmentController)
                return;

            if (entity instanceof AbstractCharacter) {
/*                //its an AI dude or a player character
                if (entity.getGravity() != null) {  //character is attached to something else
                    ModPlayground.broadcastMessage(""+entity.getName() + " is attached to " + entity.getGravity().source.getName());
                    return;
                } else {
                    ModPlayground.broadcastMessage("free astronaut " + entity);
                }*/
            }
            ModPlayground.broadcastMessage("Run " + entity.getName());

            if (entity instanceof Ship && !((Ship)entity).railController.isRoot()) {
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
        ModPlayground.broadcastMessage(" stability: " + 100*(countdown/countdownMax));

        //update value for synching
        WarpProcess.setProcess(entity,WarpProcess.WARP_STABILITY,(int)((100*countdown)/countdownMax));

        if (entity.getSpeedCurrent() < WarpManager.minimumSpeed) {
            //ship is to slow, dropping out of warp!
            countdown -= getTimeout(); //runs once a second
        } else {
            if (countdown < countdownMax) {
                countdown += 2*getTimeout();
            }
        }

        if (countdown > countdownMax) { //essentially caps the countdown to max_val, while allowing a start buffer of extra seconds
            countdown -= getTimeout();
        }
        if (countdown <= 0) {
            //drop entity out of warp.
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
    }
}
