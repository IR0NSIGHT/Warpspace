package Mod.HUD.client;

import Mod.WarpMain;
import api.DebugFile;
import api.element.gui.elements.GUIElement;
import api.utils.StarRunnable;
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

    public CustomHudImage(InputState inputState, Vector3f position, Vector3f scale, final HUD_element el) {
        super(inputState, 100, 100);
        this.position = position;
        this.scale = scale;
        if (el.sprite != null) {
            this.sprite = sprite;
        } else {
            //run a loop until a valid sprite was found
        }

        DebugFile.log("########### img a custom HUD panel and was constructed");
        DebugFile.log("sprite is null: " + (Objects.isNull(this.sprite)));
    }


    @Override
    public void cleanUp() {

    }

    @Override
    public void draw() {
        ShaderLibrary.scanlineShader.load();
        if (sprite != null) {
            sprite.draw();
            DebugFile.log("positioning and scaling");
            sprite.setPositionCenter(true);
            sprite.setPos(0,0,0);
            sprite.setScale(1,1,1);
        } else {

        }
        ShaderLibrary.scanlineShader.unload();

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