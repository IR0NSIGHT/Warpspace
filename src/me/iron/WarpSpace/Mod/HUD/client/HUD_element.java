package me.iron.WarpSpace.Mod.HUD.client;

import api.DebugFile;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 20.02.2021
 * TIME: 15:14
 */
class HUD_element {
    private Vector3f pos = new Vector3f(0,0,0); //positon in % of screen
    private Vector3f scale = new Vector3f(0,0,0); //scale in % of screen
    private Vector3f pxPos = new Vector3f(0,0,0); //position in px
    private Vector3f pxScale = new Vector3f(0,0,0); //scale on current screen.
    private Vector3f moveStep = new Vector3f(0,0,0); //used for moving the image, synched vector
    private HUD_element mother = null; //parent object which this element is attached to: uses its position and scale.
    private TextElement textElement = null; //TODO make 100% sure textelements work here.
    private Vector3f textElementOffset = new Vector3f(0,0,0); //in percent of screen
    private String text = "";
    private Vector3f textElementPxPos = new Vector3f(0,0,0); //absolute position of textelement
    private boolean drawCondition = true;

    private List<HUD_element> children = new ArrayList<HUD_element>();
    public SpriteList enumValue;
    public CustomHudImage image;
    public boolean playShutter = false;
    public ElementType type;

    public enum ElementType {
        LOWER_BAR,
        UPPER_Bar,
        INDICATOR, //warp or rsp
        BACKGROUND,
        INFO_LEFT,
        INFO_RIGHT
    }

    /**
     * creates a HUD element
     *
     * @param pos       position in % on Full HD screen.
     * @param scale     scale in % on full hd screen
     * @param moveStep
     * @param enumValue
     * @param type
     */
    public HUD_element(Vector3f pos, Vector3f scale, Vector3f moveStep, SpriteList enumValue, ElementType type) {
        this.enumValue = enumValue;
        this.type = type;
        this.pos = pos;
        this.scale = scale;
        this.moveStep = moveStep;
        setScale(scale);
        setPos(pos);
        //DebugFile.log("created" + this.toString());
    }

    /**
     * @param reference what position/scale group this belongs to. used for moving & scaling.
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
        //DebugFile.log("created " + this.toString());
    }

    public HUD_element(HUD_element reference, Vector3f textElementOffset, String text) {
        this.mother = reference;
        this.mother.addChild(this);
        setScale(scale);
        this.textElementOffset = ScreenHelper.pixelPosToRelPos(textElementOffset,false);
        this.text = text;
        setPos(mother.getPos());
        //DebugFile.log("HUD-Text_element: " + this.toString());
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
                ", text=" + text +
                ", textElPxPos" + textElementPxPos +
                '}';
    }

    /**
     * set new position with time it takes to reach that pos
     *
     * @param relPos   target position relative in % of screen. f.e. center = 0.5,0.5,0
     * @param lerpTime time to fullfill movement
     */
    public void SetPos(Vector3f relPos, float lerpTime) {
        setPos(relPos);
    }

    /**
     * returns copy of position
     *
     * @return
     */
    public Vector3f getPos() {
        return new Vector3f(pos);
    }

    /**
     * sets position to new value
     *
     * @param pos
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
     * returns copy of scale (% of screen)
     *
     * @return
     */
    public Vector3f getScale() {
        return new Vector3f(scale);
    }

    /**
     * sets scale to new values.
     *
     * @param scale
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
     *
     * @param pxPos
     */
    public void setPxPos(Vector3f pxPos) {
        this.pxPos = new Vector3f(pxPos);
    }

    /**
     * returns copy of scale of image on current screen. !not size of image in pixel, but abstract scale
     *
     * @return
     */
    public Vector3f getPxScale() {
        return new Vector3f(pxScale);
    }

    /**
     * sets value for scale on current screen.
     *
     * @param pxScale
     */
    public void setPxScale(Vector3f pxScale) {
        this.pxScale = new Vector3f(pxScale);
    }

    /**
     * get children objects that are attached to this HUDelement.
     * @return
     */
    public List<HUD_element> getChildren() {
        return children;
    }

    /**
     * add a child element which is moved and scaled with this HUD element
     * @param child
     */
    public void addChild(HUD_element child) {
        this.children.add(child);
    }

    /**
     * return the attached textelement, null if doesnt exist
     * @return
     */
    public TextElement getTextElement() {
        return textElement;
    }

    /**
     * set textelement to be attached to this HUD element.
     * @param textElement
     */
    public void setTextElement(TextElement textElement) {
        this.textElement = textElement;
        textElement.parent = this;
    }

    /**
     * offset textelement from
     * @param offset
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
     * set draw condition for this hud element
     * @param drawCondition
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

    /**
     * get text of attached textelement
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * set text of attached textelement
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }
}
