package me.iron.WarpSpace.Mod.server.config;

import api.listener.Listener;
import api.listener.events.network.ClientLoginEvent;
import api.mod.StarLoader;
import api.mod.config.FileConfiguration;
import api.network.packets.PacketUtil;
import me.iron.WarpSpace.Mod.WarpMain;
import org.schema.game.server.data.GameServerState;

/**
 * config manager class. reads, writes, corrects, defaults config values.
 */
public class ConfigManager {
    public ConfigManager(final WarpMain mod) {
        PacketUtil.registerPacket(ConfigSyncPacket.class);
        config = mod.getConfig(configName);
        for (ConfigEntry e: ConfigEntry.values()) {
            e.setValue(config.getConfigurableFloat(e.getPath(), e.defaultValue));
            if (e.isInt)
                e.value = Math.round(e.value);
            config.set(e.getPath(),e.value);
        }
        config.saveConfig();

        StarLoader.registerListener(ClientLoginEvent.class, new Listener<ClientLoginEvent>() {
            @Override
            public void onEvent(ClientLoginEvent event) {
                if(GameServerState.instance != null) {
                    PacketUtil.sendPacket(event.getServerProcessor(), new ConfigSyncPacket());
                }
            }
        }, mod);

        new ConfigDocuManager(ConfigEntry.values(), configName);
    }

    private final FileConfiguration config;
    public static final String configName = "WarpSpaceConfig";
    private static final float manySeconds = 60000000;

    public enum ConfigEntry {
        //astronaut autodrop killswitch

        killswitch_astronautDrop("killswitch_astronaut_drop", 1, 0, 1, true, true),
        killswitch_speedDrop("killswitch_speeddrop", 1, 0, 1, true, true),
        //voice announcer
        sfx_voice_enable("sfx_voice_enable", 1, 0, 20),
        sfx_voice_add_db("sfx_voice_loudness_add_dezibel", 10f, 0, 20),

        //warp sound effects
        sfx_effects_enable("sfx_effects_enable", 1, 0, 20),
        sfx_effects_add_db("sfx_effects_loudness_add_dezibel", 0f, 0, 20),

        //warp shader
        vfx_use_warp_shader("vfx_use_warp_shader", 1, 0, 1, true, false),

        //map stuff
        map_draw_droppoints_range("map_draw_droppoints_range",1,0,10),

        //######### serverside config

        //sectors l/r u/d f/b random offset of droppoint (deterministic)
        droppoint_random_offset("droppoint_random_offset_sectors",2.25f,0,Float.MAX_VALUE, false, true),

        //warp-to-rsp-ratio: how many sectors in rsp equals one sector in warp?
        warp_to_rsp_ratio("warp_to_rsp_ratio",10,2,Float.MAX_VALUE),

        //second its takes for a slow ship to drop from warp
        seconds_until_speeddrop("seconds_until_speeddrop",30,0,manySeconds, false, true),

        //meters per second required to fly in warp to not drop out
        minimum_warp_speed("minimum_warp_speed",50,0,Float.MAX_VALUE,false, true),

        //second between executing jumpdrive and jumping
        seconds_warpjump_delay("seconds_warpjump_delay",9.5f,9.5f,manySeconds,false,true),

        //warp speed chamber values
        warp_speed_no_chamber_multiplier("warp_speed_no_chamber_multiplier",1f,0,1000,false, true),
        warp_speed_chamber_lvl_1_multiplier("warp_speed_chamber_lvl_1_multiplier",1.3f,0,1000,false, true),
        warp_speed_chamber_lvl_2_multiplier("warp_speed_chamber_lvl_2_multiplier",1.6f,0,1000,false, true),
        warp_speed_chamber_lvl_3_multiplier("warp_speed_chamber_lvl_3_multiplier",2,0,1000,false, true),

        //beacon chamber
        warp_beacon_chamber_percent("warp_beacon_chamber_percent",0.25f,0,1, false, true),
        warp_beacon_disable_on_homebase("warp_beacon_disable_on_homebase",1,0,1,true,true);

        private final String path;
        public final float defaultValue;
        public final float minValue;
        public final float maxValue;
        private float value;
        private final boolean isInt;
        private final boolean overwriteClient;

        ConfigEntry(String path, float defaultValue, float min, float max) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.minValue = min;
            this.maxValue = max;
            this.isInt = false;
            this.overwriteClient = false;
        }

        ConfigEntry(String path, float defaultValue, float min, float max, boolean isInt, boolean overwriteClient) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.minValue = min;
            this.maxValue = max;
            this.isInt = isInt;
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
