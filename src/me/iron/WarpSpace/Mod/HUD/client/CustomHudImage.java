package me.iron.WarpSpace.Mod.HUD.client;
/**
 * partly stolen from StarAPI (i think)
 */

import api.DebugFile;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector3f;
import java.awt.*;

/**
 * this class draws an image onto screen. it is a worker class for HUD_element. dont use it without HUD_element.
 */
class CustomHudImage extends org.schema.schine.graphicsengine.forms.gui.GUIElement {
    public Sprite sprite;


    /**
     * position on screen in pixels
     */
    public Vector3f position;
    public Vector3f scale;

    /**
     * the HUD element (basically image wrapper) for which this HUDImage was created
     */
    private HUD_element el;

    public CustomHudImage(InputState inputState, HUD_element el) {
        super(inputState);
        this.position = el.getPxPos(); //all HUD images of same type share the same position, scale and movestep.
        this.scale = el.getPxScale();
        this.el = el;

        if (el.enumValue.getSprite() != null) {
            this.sprite = el.enumValue.getSprite();
        }
        el.image = this; //write itself to its creator
        //AdjustToScreenSize();
        //DebugFile.log("HUDImage " + el.enumValue.name + " has pos " + position.toString()  + " HUDelement pos: " + el.getPos());
    }


    @Override
    public void cleanUp() {

    }

    @Override
    public void draw() {
        if (sprite == null) {
            if (el.enumValue.getSprite() != null) {
                this.sprite = el.enumValue.getSprite(); //this should automatically add the sprite once it was added through the graphics thread : autoupdated reference. element -> spriteenum
            } else {
                DebugFile.err("Sprite" + el.enumValue.name + " has no valid path to image");
            }
            return;
        }

        if (HUD_core.drawList.get(el.enumValue) == 1) { //draw
            //DebugFile.log("positioning and scaling");
            sprite.setPositionCenter(true);
            if (el.playShutter) {
                ShaderLibrary.scanlineShader.load();
            }
            this.setScale(el.getPxScale());
            this.setPos(el.getPxPos());
            sprite.setScale(el.getPxScale());
            sprite.setPos(el.getPxPos());
            sprite.draw();
            if (el.playShutter) {
                ShaderLibrary.scanlineShader.unload();
            }
        }


    }

    @Override
    public void onInit() {
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public float getWidth() {
        return 359;
    }

    /**
     * position in pixels on current screen.
     * @return Vector (x,y,z)
     */
    @Override
    public Vector3f getPos() {
        return position;
    }

    @Override
    public float getHeight() {
        return 252;
    }
}