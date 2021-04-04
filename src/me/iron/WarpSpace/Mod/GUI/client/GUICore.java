package me.iron.WarpSpace.Mod.GUI.client;

import api.DebugFile;
import api.common.GameClient;
import api.mod.StarMod;
import api.utils.StarRunnable;
import api.utils.gui.ControlManagerHandler;
import me.iron.WarpSpace.Mod.WarpMain;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.04.2021
 * TIME: 21:26
 */
public class GUICore {
    public static boolean allowGUI = false;
    public static MyControlManager RegisterGUI(StarMod instance) {
        DebugFile.log("registering new GUI control manager");
        MyControlManager controlManager = new MyControlManager(GameClient.getClientState());
        ControlManagerHandler.registerNewControlManager(instance.getSkeleton(), controlManager);
        return controlManager;
    }

    public static void CreateGUILoop() {
        DebugFile.log("creating new GUI loop for control manager");
        new StarRunnable() {
            long lastUpdate = System.currentTimeMillis();
            boolean active = false;
            MyControlManager manager;
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastUpdate >= 2000 && GUICore.allowGUI) {
                    lastUpdate = System.currentTimeMillis();
                    active = !active;

                    if (manager == null) {
                        manager = RegisterGUI(WarpMain.instance);
                    }
                    manager.setActive(active);
                    DebugFile.log("setting control manager to active: " + active);
                }
            }
        }.runTimer(WarpMain.instance,1);
    }
}
