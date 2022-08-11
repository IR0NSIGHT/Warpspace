package me.iron.WarpSpace.Mod.event.fastevents;

import org.schema.game.common.controller.SegmentController;

public interface WarpSpeedCalculateListener {
    public float getWarpSpeed(SegmentController sc, final int chamberLevel, float speed);
}
