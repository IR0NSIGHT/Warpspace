package me.iron.WarpSpace.Mod.client;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 20.02.2021
 * TIME: 15:14
 * This is the element which serves as a container for an image (sprite) and a text element. it defines position, size etc.
 */
class HUD_element {
    private Vector3f pos; //positon in % of screen
    private Vector3f scale; //scale in % of screen
    private Vector3f pxPos; //position in px
    private Vector3f pxScale; //scale on current screen.
    private Vector3f moveStep; //used for moving the image, synched vector
    private HUD_element mother = null; //parent object which this element is attached to: uses its position and scale.
    private TextElement textElement = null;
    private Vector3f textElementOffset = new Vector3f(0,0,0); //in percent of screen
    //TODO refactor into easier inheritance class -> mother updates scale and pos, class only defines offset.
    //TODO add lerp movement
    private Vector3f textElementPxPos; //absolute position of textelement
    private boolean drawCondition = true;

    /**
     * attached children that get scaled and moved with the original.
     */
    private List<HUD_element> children = new ArrayList<HUD_element>();
    public SpriteList enumValue;
    public CustomHudImage image;
    public boolean playShutter = false;
    public ElementType type;

    /**
     * type to group elements.
     */
    public enum ElementType {
        ARROW,
        PEARL,
        SPIRAL, //warp or rsp
        BACKGROUND,
        INFO_LEFT,
        INFO_RIGHT
    }

    /**
     * creates a HUD element
     *
     * @param pos       position in % on Full HD screen.
     * @param scale     scale in % on full hd screen
     * @param moveStep x
     * @param enumValue x
     * @param type x
     */
    //TODO is this still needed?
    public HUD_element(Vector3f pos, Vector3f scale, Vector3f moveStep, SpriteList enumValue, ElementType type) {
        this.enumValue = enumValue;
        this.type = type;
        this.pos = pos;
        this.scale = scale;
        this.moveStep = moveStep;
        setScale(scale);
        setPos(pos);
    }

    /**
     * @param reference what position or scale group this belongs to. used for moving and scaling.
     * @param image     image to use (from spritelist)
     * @param type      up, down, background, etc, used for collectivley drawing or disabling.
     */
    public HUD_element(HUD_element reference, SpriteList image, ElementType type) {
        this.enumValue = image;
        this.type = type;
        this.pos = reference.getPos();
        this.scale = reference.getScale();
        this.moveStep = reference.getMoveStep();
        this.mother = reference;
        this.mother.addChild(this);
        setScale(scale);
        setPos(pos);
    }

    @Override
    public String toString() {
        return "HUD_element{" +
                "pos=" + pos +
                ", scale=" + scale +
                ", pxPos=" + pxPos +
                ", pxScale=" + pxScale +
                ", moveStep=" + moveStep +
                ", mother=" + mother +
                ", enumValue=" + enumValue +
                ", image=" + image +
                ", playShutter=" + playShutter +
                ", type=" + type +
                '}';
    }

    /**
     * set new position with time it takes to reach that pos !!doesnt lerp atm, planned feature
     * @param relPos   target position relative in % of screen. f.e. center = 0.5,0.5,0
     * @param lerpTime time to fullfill movement
     */
    public void SetPos(Vector3f relPos, float lerpTime) {
        setPos(relPos);
    }

    /**
     * returns copy of position
     * @return position
     */
    public Vector3f getPos() {
        return new Vector3f(pos);
    }

    /**
     * sets position to new value, is inhertied from attached children.
     * @param pos position
     */
    public void setPos(Vector3f pos) {
        this.pos.set(pos);
        this.setPxPos(ScreenHelper.relPosToPixelPos(pos));
        for (HUD_element child : children) {
            child.setPos(pos);
        }
        //change Text element pos
        Vector3f offset = getPxPos();
        offset.add(ScreenHelper.relPosToPixelPos(textElementOffset));
        textElementPxPos = offset;
    }

    /**
     * returns copy of scale (% of screen) (screen or orgininal sprite?) //TODO findout
     * @return scale
     */
    public Vector3f getScale() {
        return new Vector3f(scale);
    }

    /**
     * sets scale to new values.
     * @param scale scale in percent of original image (i think?) //TODO findout
     */
    public void setScale(Vector3f scale) {
        this.scale.set(scale);
        this.setPxScale(ScreenHelper.relPosToPixelPos(scale,true));
        for (HUD_element child: children) {
            child.setScale(scale);
        }
    }

    public Vector3f getMoveStep() {
        return moveStep;
    }

    /**
     * not used yet. intended for lerped movement.
     * @param moveStep x
     */
    public void setMoveStep(Vector3f moveStep) {
        this.moveStep = moveStep;
        //no synch to children necessary bc they inherit position anyways
    }

    /**
     * returns clone of position in pixels on screen.
     *
     * @return pixel pos (x,y,z)
     */
    public Vector3f getPxPos() {
        return new Vector3f(pxPos);
    }

    /**
     * sets pixelposition on screen to this value.
     * @param pxPos absolute position on screen
     */
    public void setPxPos(Vector3f pxPos) {
        this.pxPos = new Vector3f(pxPos);
    }

    /**
     * returns copy of scale of image on current screen. !not size of image in pixel, but abstract scale !!IDK what this is anymore. marked with todo
     * @return ?
     */
    //TODO what does this do, what does this mean
    public Vector3f getPxScale() {
        return new Vector3f(pxScale);
    }

    /**
     * sets value for scale on current screen.
     * @param pxScale scale in pixels
     */
    //is this actual scale after doing all inheriting stuff etc?
    public void setPxScale(Vector3f pxScale) {
        this.pxScale = new Vector3f(pxScale);
    }

    /**
     * get children objects that are attached to this HUDelement.
     * @return list of attached children
     */
    public List<HUD_element> getChildren() {
        return children;
    }

    /**
     * add a child element which is moved and scaled with this HUD element
     * @param child HUD element child to be attached
     */
    public void addChild(HUD_element child) {
        this.children.add(child);
    }

    /**
     * return the attached textelement, null if doesnt exist
     * @return textelement
     */
    public TextElement getTextElement() {
        return textElement;
    }

    /**
     * set textelement to be attached to this HUD element.
     * @param textElement element which should be attached.
     */
    public void setTextElement(TextElement textElement) {
        this.textElement = textElement;
        textElement.parent = this;
    }

    /**
     * offset textelement from
     * @param offset vector3f offset from attached position.
     * @param absolute use absolute (true) or relative (false)
     */
    public void setTextElementOffset(Vector3f offset, boolean absolute) {
        if (absolute) {
            textElementOffset = ScreenHelper.pixelPosToRelPos(offset,false);
        } else {
            textElementOffset = new Vector3f(offset);
        }
        setPos(pos); //update other positions
    }

    public Vector3f getTextElementOffset() {
        if (textElementOffset == null) {
            return new Vector3f(0,0,0);
        };
        return textElementOffset;
    }
    /**
     * check if draw condition is met
     * @return true or false
     */
    public boolean isDrawCondition() {
        return drawCondition;
    }

    /**
     * set draw condition for this hud element. doesnt work
     * @param drawCondition condition on which to draw element.
     */
    public void setDrawCondition(boolean drawCondition) {
        this.drawCondition = drawCondition;
    }

    public Vector3f getTextElementPxPos() {
        return new Vector3f(textElementPxPos);
    }

    public void setTextElementPxPos(Vector3f textElementPxPos) {
        this.textElementPxPos.set(textElementPxPos);
    }
}
