package me.iron.WarpSpace.Mod.HUD.client;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
//100% stolen from star-api
//FIXME sort out dependecies


import org.schema.schine.input.InputState;

public class WarpGUIElement extends org.schema.schine.graphicsengine.forms.gui.GUIElement {
    private int width;
    private int height;
    private int posX = 0;
    private int posY = 0;

    public WarpGUIElement(InputState inputState, int width, int height) {
        super(inputState);
        this.width = width;
        this.height = height;
    }

    public WarpGUIElement(InputState inputState, int width, int height, int posX, int posY) {
        super(inputState);
        this.width = width;
        this.height = height;
        this.posX = posX;
        this.posY = posY;
    }


    public float getWidth() {
        return 0.0F;
    }

    public float getHeight() {
        return 0.0F;
    }

    public void cleanUp() {
    }

    public void draw() {
    }

    public void drawAt(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
        this.draw();
    }

    public void onInit() {
    }
}
