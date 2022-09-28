package me.iron.WarpSpace.Mod;

import api.listener.fastevents.FastListenerCommon;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.StarRunnable;
import api.utils.draw.ModWorldDrawer;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 10.10.2021
 * TIME: 12:46
 * drawer that changes HUD stuff in the HUDIndicator overlay before every frame is drawn.
 * overwrite onRun() for your custom methods to run timed.
 */
public class TimedRunnable {
    private int timeout;
    private int runs;
    /**
     * will create a runnable that runs once every x millis.
     * @param timeout in millis
     * @param
     */
    public TimedRunnable(final int timeout, StarMod mod, final int runAmount)  {
        this.timeout = timeout;
        this.runs = runAmount;
        new StarRunnable(){
            private long lastRun = System.currentTimeMillis();
            @Override
            public void run() {
                if (System.currentTimeMillis() > lastRun + timeout) {
                    lastRun = System.currentTimeMillis();
                    onRun();
                    if (canStop()) {
                        runs--;
                        if (runs<=0)
                            cancel();
                    }

                }
            }
        }.runTimer(mod,1);
    }

    private boolean canStop() {
        return (runs>=0);
    }

    /**
     * overwrite this method for custom stuff happening.
     */
    public void onRun() {

    }

    public int getTimeout() {
        return timeout;
    }
}
