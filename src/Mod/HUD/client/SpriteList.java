package Mod.HUD.client;

import Mod.WarpMain;
import api.DebugFile;
import api.element.gui.elements.GUIElement;
import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.imageio.ImageIO;
import java.io.IOException;
    public enum SpriteList {
        FLOWER;

        private Sprite sprite;
        private String name;

        public static void init() {
            DebugFile.log("######################################### spritelist init called, enum has x entries: " + SpriteList.values().length);
            StarLoaderTexture.runOnGraphicsThread(() -> {
                synchronized (SpriteList.class) {
                    for (SpriteList value : SpriteList.values()) {
                        DebugFile.log("################################### handling sprite: " + value.getName());
                     //   String name = value.name().toLowerCase();
                    //    value.name = name;

                     /*   try {
                            value.sprite = StarLoaderTexture.newSprite(ImageIO.read(WarpMain.class.getResourceAsStream("res/" + name + ".png")), WarpMain.instance, "warpmain_" + name);
                        } catch (IOException e) {
                            e.printStackTrace();
                            DebugFile.log("############################ sprite failed" +e.toString());
                        }*/
                    }
                }
            });
        }

        public Sprite getSprite() {
            return sprite;
        }

        public String getName() {
            return "warpmain_" + name;
        }
    }
