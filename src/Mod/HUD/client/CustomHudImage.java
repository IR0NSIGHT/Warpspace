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

    public CustomHudImage(InputState inputState, Vector3f position, Vector3f scale, Sprite sprite) {
        super(inputState, 100, 100);
        this.position = position;
        this.scale = scale;
        this.sprite = sprite;
        DebugFile.log("########### img a custom HUD panel and was constructed");
        DebugFile.log("sprite is null: " + (Objects.isNull(this.sprite)));
    }


    @Override
    public void cleanUp() {

    }

    @Override
    public void draw() {
        if (sprite == null) {
            return;
        }
        //DebugFile.log("i was drawn yee haw");
        ShaderLibrary.scanlineShader.load();
            DebugFile.log("positioning and scaling");
            sprite.setPositionCenter(true);

            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int width = gd.getDisplayMode().getWidth();
            int height = gd.getDisplayMode().getHeight();

            float xX = position.x * width;
            float yY = position.y * height;
            //DebugFile.log("search here w:" + width + "h: " + height + "x:" + xX + "y" + yY);
            sprite.setPos(xX,yY, position.z);
            sprite.setScale(scale.x,scale.y,scale.z);
        //}
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