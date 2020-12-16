package Mod.HUD.client;

import Mod.WarpMain;
import api.element.gui.elements.GUIElement;
import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.imageio.ImageIO;
import java.io.IOException;
    public enum SpriteList {
        FLOWER,
        ;

        private Sprite sprite;
        private String name;

        public static void init() {
            StarLoaderTexture.runOnGraphicsThread(() -> {
                synchronized (SpriteList.class) {
                    for (SpriteList value : SpriteList.values()) {
                        String name = value.name().toLowerCase();
                        value.name = name;
                        try {
                            value.sprite = StarLoaderTexture.newSprite(ImageIO.read(WarpMain.class.getResourceAsStream("res/" + name + ".png")), WarpMain.instance, "extraeffects_" + name);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        public Sprite getSprite() {
            return sprite;
        }

        public String getName() {
            return "extraeffects_" + name;
        }
    }
