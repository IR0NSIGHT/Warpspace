package me.iron.WarpSpace.Mod.server;

import api.mod.config.FileConfiguration;
import me.iron.WarpSpace.Mod.WarpMain;
import org.luaj.vm2.ast.Str;

/**
 * not used. intended for allowing a mod config later on.
 * //TODO use
 */
public class ConfigManager {
    public ConfigManager(WarpMain mod) {
        config = mod.getConfig(configName);
        for (ConfigEntry e: ConfigEntry.values()) {
            e.value = config.getConfigurableFloat(e.getPath(),(float)e.defaultValue);
        }
        config.saveConfig();
    }

    FileConfiguration config;
    public static String configName = "WarpSpaceConfig";
    public enum ConfigEntry {
        sfx_voice_enable("sfx_voice_enable",1),
        sfx_voice_loudness("sfx_voice_loudness_dezibel",10f),

        sfx_effects_enable("sfx_effects_enable",1),
        sfx_effects_loudness("sfx_effects_loudness_dezibel",0f);
        private final String path;
        private final float defaultValue;
        private float value;
        ConfigEntry(String path, float defaultValue) {
            this.path = path; this.defaultValue = defaultValue;
        }
        public String getPath() {
            return path;
        }
        public float getValue(){
            return value;
        }
        public boolean isTrue() {
            return value>0.5f; //==1 but jankier
        }
    }


 /*   public static void WriteToConfig() {
     //   WarpMain.instance.getConfig(configName).
    //    WarpMain.instance.getConfig(configName).saveDefault("base_interdict_strength: 100");
        int level = WarpMain.instance.getConfig(configName).getInt(entries.warpdrop_warningsound_loudness.getPath());
    }

    public static void ControlConfig() {
        int level = WarpMain.instance.getConfig(configName).getInt(entries.warpdrop_warningsound_loudness.getPath());
        WarpMain.instance.getConfig(configName).saveDefault(entries.warpdrop_warningsound_loudness.getPath() + ": " + (Math.max(100,(Math.min(0, level)))));
    } */
}

