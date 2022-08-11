package me.iron.WarpSpace.Mod.server;

import me.iron.WarpSpace.Mod.WarpMain;

/**
 * not used. intended for allowing a mod config later on.
 * //TODO use
 */
public class ConfigManager {
    public static String configName = "WarpSpace_Client";
    public enum entries {
        warpdrop_warningsound_loudness("warpdrop_warningsound_loudness");

        private final String path;
        private entries(String path) {
            this.path = path;
        }



        public String getPath() {
            return path;
        }

    }
    public static void WriteToConfig() {
     //   WarpMain.instance.getConfig(configName).
    //    WarpMain.instance.getConfig(configName).saveDefault("base_interdict_strength: 100");
        int level = WarpMain.instance.getConfig(configName).getInt(entries.warpdrop_warningsound_loudness.getPath());
    }

    public static void ControlConfig() {
        int level = WarpMain.instance.getConfig(configName).getInt(entries.warpdrop_warningsound_loudness.getPath());
        WarpMain.instance.getConfig(configName).saveDefault(entries.warpdrop_warningsound_loudness.getPath() + ": " + (Math.max(100,(Math.min(0, level)))));
    }
}

