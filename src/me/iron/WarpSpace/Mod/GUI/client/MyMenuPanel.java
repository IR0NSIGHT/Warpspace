package me.iron.WarpSpace.Mod.GUI.client;

import api.utils.gui.GUIMenuPanel;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIInnerTextbox;
import org.schema.schine.input.InputState;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.04.2021
 * TIME: 21:23
 */

/**
 * this is the window that shows up as a GUI Panel. It holds multiple tabs and is created through a MyControlManager class.
 */
public class MyMenuPanel extends GUIMenuPanel {

    public MyMenuPanel(InputState inputState) {
        super(inputState, "MyMenuPanel", 800, 500);
    }

    @Override
    public void recreateTabs() {
        guiWindow.clearTabs();

        GUIContentPane firstTab = guiWindow.addTab("FIRST TAB");
        firstTab.setTextBoxHeightLast(500);
        GUIInnerTextbox textBox1 = firstTab.addNewTextBox(500);
        GUITextOverlay textOverlay = new GUITextOverlay(32, 32, FontLibrary.getBoldArial12White(), this.getState());

        textOverlay.setText(new ArrayList<Object>(Arrays.asList("London", "Tokyo")));
        //Do whatever
        textBox1.getContent().attach(textOverlay);

        GUIContentPane secondTab = guiWindow.addTab("SECOND TAB");
        secondTab.setTextBoxHeightLast(500);
        //Do whatever
    }
}

