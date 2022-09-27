package me.iron.WarpSpace.Mod.server;

import api.mod.config.FileConfiguration;
import me.iron.WarpSpace.Mod.WarpMain;

/**
 * config manager class. reads, writes, corrects, defaults config values.
 */
public class ConfigManager {
    public ConfigManager(WarpMain mod) {
        config = mod.getConfig(configName);
        for (ConfigEntry e: ConfigEntry.values()) {
            e.value = Math.max(e.minValue, Math.min(e.maxValue,config.getConfigurableFloat(e.getPath(), e.defaultValue)));
            if (e.isBool)
                e.value = e.value<0.5f?0:1;
            config.set(e.getPath(),e.value);
        }
        config.set("help","deleted values will be set to their default value on next loading. values that go below/above limits will be capped to limit.");
        config.saveConfig();
    }

    FileConfiguration config;
    public static String configName = "WarpSpaceConfig";
    public enum ConfigEntry {
        //voice announcer
        sfx_voice_enable("sfx_voice_enable",1,0,20),
        sfx_voice_loudness("sfx_voice_loudness_dezibel",10f,0,20),

        //warp sound effects
        sfx_effects_enable("sfx_effects_enable",1,0,20),
        sfx_effects_loudness("sfx_effects_loudness_dezibel",0f,0,20),

        //warp shader
        vfx_use_warp_shader("vfx_use_warp_shader",1,0,1, true);

        private final String path;
        private final float defaultValue;
        private final float minValue;
        private final float maxValue;
        private float value;
        private final boolean isBool;

        ConfigEntry(String path, float defaultValue, float min, float max) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.minValue = min;
            this.maxValue = max;
            this.isBool = false;
        }

        ConfigEntry(String path, float defaultValue, float min, float max, boolean isBool) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.minValue = min;
            this.maxValue = max;
            this.isBool = isBool;
        }

        public String getPath() {
            return path;
        }
        public float getValue(){
            return value;
        }
        public boolean isTrue() {
            return value>0.5f;//almost c++ style
        }
    }
}

