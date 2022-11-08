package me.iron.WarpSpace.Mod.server.config;

import java.util.LinkedList;

import api.mod.config.FileConfiguration;
import me.iron.WarpSpace.Mod.WarpMain;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.10.2022
 * TIME: 08:55
 */
public class ConfigDocuManager {
    public static final String docuConfigSuffix = "_DOCUMENTATION";
    public ConfigDocuManager(ConfigManager.ConfigEntry[] entries, String configName) {
        FileConfiguration config = new FileConfiguration(WarpMain.instance,configName+docuConfigSuffix,new LinkedList<String>(), new LinkedList<String>());
        String separator = "\n//\t";
        config.set("a_info","If you messed up your config, just delete the line or the whole config. it will be replaced with a fresh, default one on restart.");
        for (ConfigManager.ConfigEntry e: entries) {
            config.set(e.getPath(),separator+getDescription(e) +separator+ getRangeString(e)+separator+getSidingString(e));
        }
        config.saveConfig();
    }

    public String getDescription(ConfigManager.ConfigEntry entry) {
        switch (entry) {
            case droppoint_random_offset:
                return "natural droppoints are randomly shifted up to x sectors.";
            case killswitch_speedDrop:
                return "killswitch to disable automated speed-drop from going slow in warp.";
            case killswitch_astronautDrop:
                return "killswitch to disable automated astronaut drop. disable the feature if people receive crashes + nullpointers when in warp.";
            case map_draw_droppoints_range:
                return "draw the droppoints (blue spiral) of all warpsectors with distance < x on the map in realspace.";
            case minimum_warp_speed:
                return "you need to fly x meter/second in warpspace, otherwise you loose stability and will drop out.";
            case seconds_until_speeddrop:
                return "If you fly to slow in warpspace, you will drop out after x seconds.";
            case seconds_warpjump_delay:
                return "If you execute your jumpdrive, it will take x seconds until the actual jump happens.";
            case sfx_effects_enable:
                return "Enable sound effects.";
            case sfx_effects_add_db:
                return "Additional loudness for soundeffects in decibel.";
            case sfx_voice_enable:
                return "Enable the voice announcer.";
            case sfx_voice_add_db:
                return "Additional loudness for the voiceannouncer in decibel.";
            case vfx_use_warp_shader:
                return "Enable the colorful shader in warpspace. Disable if your GPU can't handle it.";
            case warp_beacon_chamber_percent:
                return "The beacon chamber will take up x*100 percent of the reactor tree.";
            case warp_beacon_disable_on_homebase:
                return "Disable warp beacons on homebase stations.";
            case warp_speed_chamber_lvl_1_multiplier:
                return "The speed in warp will be increased by factor x when using a 'warp flight chamber' level 1.";
            case warp_speed_chamber_lvl_2_multiplier:
                return "The speed in warp will be increased by factor x when using a 'warp flight chamber' level 2.";
            case warp_speed_chamber_lvl_3_multiplier:
                return "The speed in warp will be increased by factor x when using a 'warp flight chamber' level 3.";
            case warp_speed_no_chamber_multiplier:
                return "The speed in warp will be increased by factor x when using no warp flight chamber.";
            case warp_to_rsp_ratio:
                return "Travelling 1 sector in warpspace, means travelling x sectors in realspace. Droppoints will be spaced x sectors apart.";
            default:
                return "No documentation available";
        }
    }

    String getRangeString(ConfigManager.ConfigEntry entry) {
        return String.format("default %s min %s max %s ", getRoundedString(entry.defaultValue), getRoundedString(entry.minValue), getRoundedString(entry.maxValue));
    }

    String getRoundedString(float value) {
        return String.format("%.2f", value);
    }

    String getSidingString(ConfigManager.ConfigEntry e) {
        return e.isOverwriteClient()?"[SERVER]":"[CLIENT]";
    }


}
