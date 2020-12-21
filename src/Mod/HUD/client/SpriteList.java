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
    CONSOLE_HUD1024,
    CONSOLE_HUD1024_SCREEN,
    CONSOLE_HUD1024_BOTTOM,
    RSP_ICON,
    WARP_ICON,
    ICON_OUTLINE_RSP_TRAVEL,    //green lower bottom
    ICON_OUTLINE_WARP_TRAVEL, //green up

    ICON_OUTLINE_RSP_INACTIVE, //greyed out lower bottom
    ICON_OUTLINE_WARP_INACTIVE, //grey top

    ICON_OUTLINE_RSP_BLOCKED,   //red lower
    ICON_OUTLINE_WARP_BLOCKED,   //red up
    ICON_OUTLINE_SECTOR_LOCKED_UP,  //red up and down
    ICON_OUTLINE_SECTOR_LOCKED_DOWN,

    ICON_OUTLINE_TO_RSP,    //yellow drop sign
    ICON_OUTLINE_TO_WARP    //yellow up sign
    ;


        private Sprite sprite;
        private String name;

        public static void init() {
            DebugFile.log("######################################### spritelist init called, enum has x entries: " + SpriteList.values().length);
            StarLoaderTexture.runOnGraphicsThread(() -> {
                synchronized (SpriteList.class) {
                    long duration = System.currentTimeMillis();
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
                    duration = (System.currentTimeMillis() - duration);
                    DebugFile.log("########################### finished loading sprites after " + duration + "millis");
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
