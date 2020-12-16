package Mod.HUD.client;

import api.element.gui.elements.GUIElement;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.InputState;

class CustomHudImage extends GUIElement {
    Sprite sprite;
    public CustomHudImage(InputState inputState) {
        super(inputState);
    }


    @Override
    public void cleanUp() {

    }

    @Override
    public void draw() {
        ShaderLibrary.scanlineShader.load();
        if(sprite != null) {
            sprite.draw();
        }else{
            sprite = SpriteList.FLOWER.getSprite();
            sprite.setPositionCenter(true);
            sprite.setPos(500,500, 0);
            sprite.setScale(10,10,1);
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
        return 50;
    }

    @Override
    public float getHeight() {
        return 50;
    }
}