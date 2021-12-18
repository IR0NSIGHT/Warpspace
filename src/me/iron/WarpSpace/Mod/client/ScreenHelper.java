package me.iron.WarpSpace.Mod.client;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 20.02.2021
 * TIME: 13:38
 */

import api.DebugFile;

import javax.vecmath.Vector3f;
import java.awt.*;

/**
 * helper class that provides methods for scaling screenposititons according to resolution,  etc.
 */
public class ScreenHelper {
    /**
     * adjust given position in % of screen to current resolution. center: (0.5,0.5,0) -- pxPos (1920/2,1080/2,0) (assuming current screen is full HD).
     * creates new vector.
     * @param pos relative position in percent on screen
     * @return position in absolute pixels
     */
    public static Vector3f relPosToPixelPos(Vector3f pos) {
        return relPosToPixelPos(pos,false);
    }

    /**
     * get pixel position from relative screen positon, use only screenwidth to scale
     * @param pos relative position in percent on screen
     * @param onlyWidth only use screenwidht to scale pos vector
     * @return position in absolute pixels
     */
    public static Vector3f relPosToPixelPos(Vector3f pos, boolean onlyWidth) {
        Vector3f screenRes = getCurrentScreenResolution();
        Vector3f screenPos = new Vector3f(pos);
        if (onlyWidth) {
            screenRes = new Vector3f(screenRes.y,screenRes.y,0);
        };
        scaleMultiply(screenPos, screenRes); //mutate screenpos

        return screenPos; //TODO write
    }

    /**
     * get relative position on screen from pixel position
     * @param pos pixel position
     * @param onlyWidth use only width for x and y(for quadratic scaling on resolutionchange)
     * @return relative screenpos in percent
     */
    public static Vector3f pixelPosToRelPos(Vector3f pos, boolean onlyWidth) {
        Vector3f screenRes = getCurrentScreenResolution();
        Vector3f screenPos = new Vector3f(pos);
        if (onlyWidth) {
            screenRes = new Vector3f(screenRes.y,screenRes.y,0);
        };
        scaleDivide(screenPos, screenRes); //mutate screenpos

        return screenPos; //TODO write
    }

    /**
     * get the resolution in pixels of currently used screen.
     * @return Vector3f (screenwidht, screenheight, 0)
     */
    public static Vector3f getCurrentScreenResolution() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd == null) {
            DebugFile.log("graphics device is null");
            return null;
        }
        Vector3f screenRes = new Vector3f(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight(),0); //resolution of current screen
        return screenRes;
    }

    /**
     * multiplies every position of a with corresponding pos of b: (a.x * b.x, a.y * b.y, a. z * b.z)
     * @param a Vector to be mutated
     * @param b vector which is used to mutate a
     */
    public static void scaleMultiply(Vector3f a, Vector3f b) {
        Vector3f aClone = new Vector3f(a);
        a.set(a.x * b.x,a.y * b.y,a.z);
    }

    public static void scaleDivide(Vector3f a, Vector3f b) {
        Vector3f aClone = new Vector3f(a);
        a.set(a.x / b.x,a.y / b.y,a.z);
    }

    /**
     * get the vector from position a to position b
     * @param a point a
     * @param b point b
     * @return new vector a--b
     */
    public static Vector3f getDirection(Vector3f a, Vector3f b) {
        //b minus a
        Vector3f sub = new Vector3f(a);
        sub.negate();
        Vector3f direction = new Vector3f(b); direction.add(sub);
        return direction;
    }

    /**
     * returns distance between two points
     * @param a point a
     * @param b point b
     * @return euclidean distance as float
     */
    public static float getDistance (Vector3f a, Vector3f b) {
        return getDirection(a,b).length();
    }
}
