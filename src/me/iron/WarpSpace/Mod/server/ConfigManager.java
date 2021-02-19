package me.iron.WarpSpace.Mod.server;

import me.iron.WarpSpace.Mod.HUD.client.WarpProcessController;
import me.iron.WarpSpace.Mod.WarpMain;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    public static String configName = "WarpSpaceConfig.yml";
    public enum entries {
        warpdrop_warningsound_loudness("warpdrop_warningsound_loudness");

        private final String path;
        private static Map map = new HashMap<>();
        private entries(String path) {
            this.path = path;
        }

        static { //map enum value to int keys for reconstruction int -> enumvalue
            for (WarpProcessController.WarpProcess s: WarpProcessController.WarpProcess.values()) {
                map.put(s.getValue(),s);
            }
        }

        public static WarpProcessController.WarpProcess valueOf(int k) {
            return (WarpProcessController.WarpProcess) map.get(k);
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

