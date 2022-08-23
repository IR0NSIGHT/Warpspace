package me.iron.WarpSpace.Mod;

import api.config.BlockConfig;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

import static org.schema.game.common.data.element.ElementKeyMap.descriptionTranslations;
import static org.schema.game.common.data.element.ElementKeyMap.nameTranslations;

/**
 * BY: Ithirahad
 * Aug. 23 2022
 */

public class WSElementInfoManager {
    public static void onBlockConfigLoad(WarpMain instance, BlockConfig blockConfig) {
        ElementInformation JUMP_BASE = ElementKeyMap.getInfo(67); //I wonder what that ID got repurposed from
        ElementInformation JUMP_DIST_1 = ElementKeyMap.getInfo(119);
        ElementInformation JUMP_DIST_2 = ElementKeyMap.getInfo(118);
        ElementInformation JUMP_DIST_3 = ElementKeyMap.getInfo(117);
        nameTranslations.remove(JUMP_BASE.id);
        descriptionTranslations.remove(JUMP_BASE.id);
        JUMP_BASE.description = "If server settings require it, this chamber will enable the Warp Drive on your ship.\r\nOtherwise, it simply acts as a hub for Warp Drive upgrade chambers.";
        nameTranslations.remove(JUMP_DIST_1.id);
        nameTranslations.remove(JUMP_DIST_2.id);
        nameTranslations.remove(JUMP_DIST_3.id);
        JUMP_BASE.name = "Warp Drive Base";
        JUMP_DIST_1.name = "Warp Flight Speed 1";
        JUMP_DIST_2.name = "Warp Flight Speed 2";
        JUMP_DIST_3.name = "Warp Flight Speed 3";
    }
}
