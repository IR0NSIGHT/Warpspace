package me.iron.WarpSpace.Mod.client;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 20.12.2020
 * TIME: 23:24
 */
public class HUDElementController {
    /**
     * will enable the given element
     * @param element Spritelist element = image
     * @param clean disable other elements of this type
     */
    public static void drawElement(SpriteList element, boolean clean) { //TODO is this used?
        if (clean) {
            //clean elements of same type
            //TODO write good find method
            for (HUD_element el: HUD_core.elementList) {
                if (el.enumValue == null) {
                    continue;
                }
                if (el.enumValue.equals(element)) {
                    clearType(el.type);
                }
            }
        }
        HUD_core.drawList.put(element,1);
    }

    /**
     * will set all elements of given type to ON: 1 or OFF: 2
     * @param type Elementtype
     * @param value value 0,1
     */
    public static void drawType(HUD_element.ElementType type, int value) {
        for (HUD_element el: HUD_core.elementList) {
            if (el.type == null) {
                continue;
            }
            if (el.type.equals(type)) {
                HUD_core.drawList.put(el.enumValue, value);
            }
        }
    }

    /**
     * will clear all elements of this type from screen
     * @param type ElementType to clear.
     */
    public static void clearType(HUD_element.ElementType type) {
        drawType(type,0);
    }
}
