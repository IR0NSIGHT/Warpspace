package me.iron.WarpSpace.Mod.HUD.client;
/**
 * partly stolen from StarAPI (i think)
 */

import api.DebugFile;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector3f;
import java.awt.*;

class CustomHudImage extends org.schema.schine.graphicsengine.forms.gui.GUIElement {
    public Sprite sprite;


    public Vector3f position;

    public Vector3f scale;

    /**
     * the HUD element (basically image wrapper) for which this HUDImage was created
     */
    private HUD_element el;

    public CustomHudImage(InputState inputState, Vector3f position, Vector3f scale, HUD_element el) {
        super(inputState);
        this.position = new Vector3f(position);
        this.screenPos = new Vector3f(position);
        this.scale = new Vector3f(scale);
        this.el = el;
        if (el.enumValue.getSprite() != null) {
            this.sprite = el.enumValue.getSprite();
        }
        el.image = this; //write itself to its creator
        AdjustToScreenSize();
        DebugFile.log("HUDImage " + el.enumValue.name + " has pos " + position.toString() + " screenpos " + screenPos.toString() + " HUDelement pos: " + el.pos);
    }


    @Override
    public void cleanUp() {

    }

    private GraphicsDevice gd;
    private Vector3f screenRes = new Vector3f();
    private Vector3f screenPos;
    private Vector3f desiredPos = new Vector3f(); //used for moving sprites smoothly
    private Vector3f moveStep = new Vector3f(1,1,0); //vector which is added onto screenpos per time unit. used for moving sprites smoothly
    private long lastMovement = 0; //timestamp when last movement happened.
    private Vector3f screenScale = new Vector3f(1,1,1);
    private boolean playShutter = false;
    private int screenResUpdate = 0;
    @Override
    public void draw() {
        if (sprite != null) {
            if (screenResUpdate % 30 == 0) {
                screenResUpdate = 0;
                playShutter = el.playShutter;
            //    AdjustToScreenSize();
            }
            screenResUpdate += 1;

            if (HUD_core.drawList.get(el.enumValue) == 1) { //draw
                //DebugFile.log("positioning and scaling");
                sprite.setPositionCenter(true);

                if (playShutter) {
                    ShaderLibrary.scanlineShader.load();
                }
                doMovement();
                sprite.setPos(screenPos.x,screenPos.y,screenPos.z);
                sprite.setScale(screenScale.x,screenScale.y,screenScale.z); //!scale uses the smaller dimension (screenheight) as a multiplier so different formats dont stretch the image

                sprite.draw();
                if (playShutter) {
                    ShaderLibrary.scanlineShader.unload();
                }
            }
        } else {
            if (el.enumValue.getSprite() != null) {
                this.sprite = el.enumValue.getSprite(); //this should automatically add the sprite once it was added through the graphics thread : autoupdated reference. element -> spriteenum
            } else {
                DebugFile.err("Sprite" + el.enumValue.name + " has no valid path to image");
            }
        }
    }

    private void AdjustToScreenSize() {
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        screenRes.x = gd.getDisplayMode().getWidth();
        screenRes.y = gd.getDisplayMode().getHeight();

        screenPos.x = position.x * screenRes.x;
        screenPos.y = position.y * screenRes.y;
        screenPos.z = position.z * screenRes.z;

        screenScale.x = scale.x * screenRes.y;
        screenScale.y = scale.y * screenRes.y;
        screenScale.z = scale.z * screenRes.y;
    }
    @Override
    public void onInit() {
    }

    /**
     * will change screenpos of sprite.
     * @param pos new position
     * @param lerpTime time to reach destination pos in ticks
     */
    public void setScreenPos (Vector3f pos, int lerpTime) {
        DebugFile.log("SetScreenPos #######################################");
        DebugFile.log("setting new screen pos for img of" + el.enumValue.name + "to " + pos.toString());
        if (lerpTime <= 0) {
            screenPos = new Vector3f(pos); //directly overwrite screenpos, no movement
            return;
        }
        //get distance between nowPos and desiredPos: destination minus now = direction to destination
        Vector3f dir = new Vector3f(pos); //will be direction towards new Pos
        Vector3f b = new Vector3f(screenPos); //negative screenpos
        b.negate();
        dir.add(b);
        if (dir.length() <= 0) {
            return;
        }
        //get speed necessary to move in lerptime
        float moveSpeed = dir.length()/lerpTime;
        //get vector to add per tick
        dir.normalize();
        dir.scale(moveSpeed);
        DebugFile.log(el.enumValue.name + "from " + screenPos + " to " + pos + "= dist" + dir.length() + " in time: " + lerpTime + " => movespeed: " + moveSpeed + " movestep" + moveStep);

        this.moveStep = dir;
        this.desiredPos = pos;
        lastMovement = System.currentTimeMillis();
    }

    /**
     * will handle movement towards desired screenposition
     */
    private void doMovement() {
        //move towards desired pos
        //if closer than one movement step, just overwrite screenpos.
        pingpong();
        //dont move if sprite is already at wanted position.
    //    if (desiredPos == null || screenPos == desiredPos || desiredPos.equals(new Vector3f(0,0,0))) {
    //        return;
    //    }

        //get time since last movement
        if (lastMovement == 0) {
            lastMovement = System.currentTimeMillis();
        }
        long timeSinceLastMove = System.currentTimeMillis() - lastMovement;

        lastMovement = System.currentTimeMillis(); //in millis

        //scale direction vector
        Vector3f step = new Vector3f(moveStep);
        step.scale(timeSinceLastMove);
   //    //get distance to destination
   //    Vector3f dir = new Vector3f(desiredPos);
   //    Vector3f nowPos = new Vector3f(screenPos);

   //    nowPos.negate(); dir.add(nowPos); //dir = vector from screenPos to desiredpos
   //    if (dir.length() <= 0) {
   //        return;
   //    }
   //    if (el.enumValue.name == SpriteList.CONSOLE_HUD1024.name) {
   //        DebugFile.log("########## moving "+ el.enumValue.name + " time since last movement " + timeSinceLastMove + " with moveStep scaled: " + dir.toString() + " dist: " + dir.length() + "desiredPos: " + desiredPos);
   //    }
   //    if (step.length() > dir.length()) {
   //        //step would be longer than distance => overshoot
   //        if (el.enumValue.name == SpriteList.CONSOLE_HUD1024.name) {
   //            DebugFile.log("teleporting to avoid overshooting");
   //        }

   //        screenPos = new Vector3f(desiredPos);
   //        desiredPos = null;
   //        return;
   //    }
        screenPos.add(step); //else: add step onto screenpos = move one step closer.
    }

    private void pingpong() {
        //check if near an edge.
        //512 x 512
        //left side
        Vector3f a = new Vector3f(screenPos);
        boolean left = false;
        boolean right = false;
        boolean top = false;
        boolean bottom = false;
        float randomMulti = (float) (0.9 + Math.random() * 0.2); //0.9 .. 1.1
        if (a.x <= 256 || a.x >= (1920 - 256)) {
            //touching left or right border, reflect on x axis
            moveStep = new Vector3f(
                    -1 * moveStep.x,
                    Math.min( randomMulti * moveStep.y, 1.5f),
                    0);
            DebugFile.log("movestep after collision: " + moveStep.toString() + " at pos " + screenPos.toString());
        }
        if (a.y <= 256 || a.y >= (1080 - 256)) {
            //touching up or down border
            moveStep = new Vector3f(
                    Math.min( randomMulti * moveStep.x,1.5f),
                    -1 * moveStep.y,
                    0);
            DebugFile.log("movestep after collision: " + moveStep.toString() + " at pos " + screenPos.toString());
        }
        screenPos = new Vector3f(
                Math.max(Math.min(screenPos.x,1920 - 256),256),
                Math.max(Math.min(screenPos.y,1080 - 256),256),
                0
        );
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
    public Vector3f getPos() {
        if (screenPos.equals(new Vector3f())) {
            return null;
        }
        return screenPos;
    }

    @Override
    public float getHeight() {
        return 252;
    }
}