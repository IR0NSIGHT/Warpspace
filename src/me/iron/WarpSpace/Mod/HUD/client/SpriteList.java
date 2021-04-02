package me.iron.WarpSpace.Mod.HUD.client;
/**
 * provided by Jake
 * thanks jake!
 * modified by ironsight
 */

import me.iron.WarpSpace.Mod.WarpMain;
import api.DebugFile;
import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
    ICON_OUTLINE_TO_WARP,    //yellow up sign

    INFO_RIGHT              //right info box for inhibitor
    ;


        public Sprite sprite;
        public String name;

        public static void init() { //TODO add java 7 method instead of lambda (j8)
            StarLoaderTexture.runOnGraphicsThread(new SpriteLoader());
        }
        public Sprite getSprite() {
            return sprite;
        }
        public String getName() {
            return "warpmain_" + name;
        }
    }

    class SpriteLoader implements Runnable {
        @Override
        public void run() {
            DebugFile.log(" spriteloader running");

            synchronized (SpriteList.class) {
                for (SpriteList value : SpriteList.values()) {
                    String name = value.name().toLowerCase();
                    value.name = name;
                    try {
                        String path = "me/iron/WarpSpace/Mod/res/" + name + ".png"; //console.png
                        InputStream is = WarpMain.instance.getJarResource(path);
                        if (is == null) {
                            DebugFile.err("spritelist initialization could not get a valid path for image " + name + " at path: " + path);
                            continue;
                        }
                        BufferedImage bi = ImageIO.read(is);
                        DebugFile.log(" spritelist loaded: " + path + " for image " + value.name);
                        value.sprite = StarLoaderTexture.newSprite(bi, WarpMain.instance, "warpmain_" + name);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
            }
        }
    }
