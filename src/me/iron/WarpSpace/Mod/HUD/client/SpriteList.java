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

    ARROW_TO_RSP,
    ARROW_TO_RSP_BLOCKED,
    ARROW_TO_RSP_JUMP,

    ARROW_TO_WARP,
    ARROW_TO_WARP_BLOCKED,
    ARROW_TO_WARP_JUMP,

    BORDER,
    SPIRAL,
    SPIRAL_BLOCKED,
    PEARL,
    PEARL_BLOCKED
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

            synchronized (SpriteList.class) {
                for (SpriteList value : SpriteList.values()) {
                    String name = value.name().toLowerCase();
                    value.name = name;
                    try {
                        String path = "me/iron/WarpSpace/Mod/res/" + name + ".png"; //console.png
                        InputStream is = WarpMain.instance.getJarResource(path);
                        if (is == null) {
                            continue;
                        }
                        BufferedImage bi = ImageIO.read(is);
                        value.sprite = StarLoaderTexture.newSprite(bi, WarpMain.instance, "warpmain_" + name);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
            }
        }
    }
