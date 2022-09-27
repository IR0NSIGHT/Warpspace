package me.iron.WarpSpace.Mod.server.config;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.network.ClientLoginEvent;
import api.mod.StarLoader;
import api.mod.config.FileConfiguration;
import api.network.packets.PacketUtil;
import me.iron.WarpSpace.Mod.WarpMain;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;

/**
 * config manager class. reads, writes, corrects, defaults config values.
 */
public class ConfigManager {
    public ConfigManager(final WarpMain mod) {
        PacketUtil.registerPacket(ConfigSyncPaket.class);
        config = mod.getConfig(configName);
        for (ConfigEntry e: ConfigEntry.values()) {
            e.setValue(config.getConfigurableFloat(e.getPath(), e.defaultValue));
            if (e.isBool)
                e.value = e.value<0.5f?0:1;
            config.set(e.getPath(),e.value);
        }
        config.set("help","deleted values will be set to their default value on next loading. values that go below/above limits will be capped to limit.");
        config.saveConfig();

        StarLoader.registerListener(ClientLoginEvent.class, new Listener<ClientLoginEvent>() {
            @Override
            public void onEvent(ClientLoginEvent event) {
                if(GameServerState.instance != null) {
                    PacketUtil.sendPacket(event.getServerProcessor(), new ConfigSyncPaket());
                }
            }
        }, mod);
    }

    private final FileConfiguration config;
    public static final String configName = "WarpSpaceConfig";
    public enum ConfigEntry {
        //voice announcer
        sfx_voice_enable("sfx_voice_enable",1,0,20),
        sfx_voice_add_db("sfx_voice_loudness_add_dezibel",10f,0,20),

        //warp sound effects
        sfx_effects_enable("sfx_effects_enable",1,0,20),
        sfx_effects_add_db("sfx_effects_loudness_add_dezibel",0f,0,20),

        //warp shader
        vfx_use_warp_shader("vfx_use_warp_shader",1,0,1, true, false),

        //map stuff
        map_draw_droppoints_range("map_draw_droppoints_range",1,0,10),

        //######### serverside config

        //sectors l/r u/d f/b random offset of droppoint (deterministic)
        droppoint_random_offset("droppoint_random_offset_sectors",2.25f,0,Float.MAX_VALUE, false, true),

        //second its takes for a slow ship to drop from warp
        seconds_until_speeddrop("seconds_until_speeddrop",30,0,60000000, false, true),

        //warp speed chamber values
        warp_speed_no_chamber_multiplier("warp_speed_no_chamber_multiplier",1f,0,1000,false, true),
        warp_speed_chamber_lvl_1_multiplier("warp_speed_chamber_lvl_1_multiplier",1.3f,0,1000,false, true),
        warp_speed_chamber_lvl_2_multiplier("warp_speed_chamber_lvl_2_multiplier",1.6f,0,1000,false, true),
        warp_speed_chamber_lvl_3_multiplier("warp_speed_chamber_lvl_3_multiplier",2,0,1000,false, true),

        //beacon chamber
        warp_beacon_chamber_percent("warp_beacon_chamber_percent",0.25f,0,1, false, true);


        private final String path;
        private final float defaultValue;
        private final float minValue;
        private final float maxValue;
        private float value;
        private final boolean isBool;
        private final boolean overwriteClient;

        ConfigEntry(String path, float defaultValue, float min, float max) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.minValue = min;
            this.maxValue = max;
            this.isBool = false;
            this.overwriteClient = false;
        }

        ConfigEntry(String path, float defaultValue, float min, float max, boolean isBool, boolean overwriteClient) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.minValue = min;
            this.maxValue = max;
            this.isBool = isBool;
            this.overwriteClient = overwriteClient;
        }

        public String getPath() {
            return path;
        }
        public float getValue(){
            return value;
        }
        public boolean isOverwriteClient() {
            return overwriteClient;
        }
        public boolean isTrue() {
            return value>0.5f;//almost c++ style
        }
        public void setValue(float val) {
            System.out.println("set config setting " + this.path+ " to " + val);
            this.value = Math.max(minValue, Math.min(maxValue, val));
        }
        public static ConfigEntry getEntryByPath(String path) {
            for (ConfigEntry c : ConfigEntry.values()) {
                if (c.path.equals(path))
                        return c;
            }
            throw new EnumConstantNotPresentException(ConfigEntry.class, path);
        }
    }
}

