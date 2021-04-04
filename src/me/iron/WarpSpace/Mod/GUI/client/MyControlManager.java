package me.iron.WarpSpace.Mod.GUI.client;

import api.DebugFile;
import api.utils.gui.GUIControlManager;
import org.schema.game.client.data.GameClientState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.04.2021
 * TIME: 21:25
 */
public class MyControlManager extends GUIControlManager {

    public MyControlManager(GameClientState clientState) {
        super(clientState);
        DebugFile.log("MyControlManager instance created.");
    }

    @Override
    public MyMenuPanel createMenuPanel() {
        return new MyMenuPanel(getState());
    }
}

