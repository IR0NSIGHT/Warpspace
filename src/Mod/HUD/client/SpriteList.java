package Mod.HUD.client;

import Mod.WarpMain;
import api.DebugFile;
import api.element.gui.elements.GUIElement;
import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public enum SpriteList {
        CONSOLE,
    RSP_ICON
    ;


        private Sprite sprite;
        private String name;

        public static void init() {
            DebugFile.log("######################################### spritelist init called, enum has x entries: " + SpriteList.values().length);
            StarLoaderTexture.runOnGraphicsThread(() -> {
                synchronized (SpriteList.class) {
                    for (SpriteList value : SpriteList.values()) {

                        String name = value.name().toLowerCase();
                        value.name = name;
                        DebugFile.log("################################### handling sprite: " + value.getName());
                        try {
                            String path = "res/" + name + ".png";
                            DebugFile.log("####################### trying to get resourcestream -> buffered image from path: " + path);
                            InputStream is = WarpMain.class.getResourceAsStream(path);
                            DebugFile.log("inputstream null: "+ (null == is));
                            BufferedImage bi = ImageIO.read(is);
                            value.sprite = StarLoaderTexture.newSprite(bi, WarpMain.instance, "warpmain_" + name);
                        } catch (IOException e) {
                            e.printStackTrace();
                            DebugFile.log("############################ sprite failed" +e.toString());
                        }
                    };
                    DebugFile.log("########################### finished loading sprites");
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
