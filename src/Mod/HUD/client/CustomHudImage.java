package Mod.HUD.client;

import api.DebugFile;
import api.element.gui.elements.GUIElement;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.Objects;

class CustomHudImage extends GUIElement {
    public Sprite sprite;


    public Vector3f position;

    public Vector3f scale;

    private HUD_element el;

    public CustomHudImage(InputState inputState, Vector3f position, Vector3f scale, HUD_element el) {
        super(inputState, 100, 100);
        this.position = position;
        this.scale = scale;
        this.el = el;
        if (el.enumValue.getSprite() != null) {
            this.sprite = el.enumValue.getSprite();
        } else {
            //run a loop until a valid sprite was found
        }

        DebugFile.log("########### img a custom HUD panel and was constructed");
        DebugFile.log("sprite is null: " + (Objects.isNull(this.sprite)));
    }


    @Override
    public void cleanUp() {

    }

    private GraphicsDevice gd;
    private Vector3f screenRes = new Vector3f();
    private Vector3f screenPos = new Vector3f(1,1,1);
    private Vector3f screenScale = new Vector3f(1,1,1);
    private boolean playShutter = false;
    private int screenResUpdate = 0;
    @Override
    public void draw() {
        if (sprite != null) {
            if (HUD_core.drawList.get(el.enumValue) == 1) { //draw
                //DebugFile.log("positioning and scaling");
                sprite.setPositionCenter(true);


                if (screenResUpdate % 30 == 0) {
                //    DebugFile.log("###########updating screen resolution");
                    screenResUpdate = 0;
                    gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                    screenRes.x = gd.getDisplayMode().getWidth();
                    screenRes.y = gd.getDisplayMode().getHeight();

                    screenPos.x = position.x * screenRes.x;
                    screenPos.y = position.y * screenRes.y;
                    screenPos.z = position.z * screenRes.z;

                    screenScale.x = scale.x * screenRes.y;
                    screenScale.y = scale.y * screenRes.y;
                    screenScale.z = scale.z * screenRes.y;

                    playShutter = el.playShutter;
                    if (el.enumValue.equals(SpriteList.CONSOLE_HUD1024)) {
                        DebugFile.log("console is pos " + screenPos + " scale " + scale);
                    }
                //    DebugFile.log("original scale is: " + scale.toString());

                }
                screenResUpdate += 1;
            //    DebugFile.log("screen res is: " + screenRes.toString() + " position is: " + screenPos.toString() + " scale is: " + screenScale.toString());
                if (playShutter) {
                    ShaderLibrary.scanlineShader.load();
                }
                sprite.setPos(screenPos.x,screenPos.y,screenPos.z);
                sprite.setScale(screenScale.x,screenScale.y,screenScale.z); //!scale uses the smaller dimension (screenheight) as a multiplier so different formats dont stretch the image

                sprite.draw();
                if (playShutter) {
                    ShaderLibrary.scanlineShader.unload();
                }
            }
        } else {
            DebugFile.log("sprite is null");
            if (el.enumValue.getSprite() != null) {
                DebugFile.log("elements sprite was detected");
                this.sprite = el.enumValue.getSprite(); //this should automatically add the sprite once it was added through the graphics thread : autoupdated reference. element -> spriteenum
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

    @Override
    public float getHeight() {
        return 252;
    }
}