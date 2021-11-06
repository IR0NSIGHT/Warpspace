package me.iron.WarpSpace.Mod;

import api.config.BlockConfig;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import api.network.packets.PacketUtil;
import api.utils.registry.UniversalRegistry;
import me.iron.WarpSpace.Mod.HUD.client.*;
import me.iron.WarpSpace.Mod.HUD.client.map.DropPointMapDrawer;
import me.iron.WarpSpace.Mod.Interdiction.InterdictionHUDUpdateLoop;
import me.iron.WarpSpace.Mod.beacon.BeaconManager;
import me.iron.WarpSpace.Mod.beacon.BeaconUpdatePacket;
import me.iron.WarpSpace.Mod.beacon.WarpBeaconAddon;
import me.iron.WarpSpace.Mod.network.PacketHUDUpdate;
import me.iron.WarpSpace.Mod.server.WarpCheckLoop;
import me.iron.WarpSpace.Mod.server.WarpJumpListener;
import me.iron.WarpSpace.Mod.taswin.WarpSpaceMap;
import me.iron.WarpSpace.Mod.visuals.BackgroundEventListener;
import org.schema.schine.resource.ResourceLoader;


/*
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.10.2020
 * TIME: 19:06
 */

/**
 * the me.iron.WarpSpace.Mod.testing.main class where the mod is run from by starloader.
 */
public class WarpMain extends StarMod {

    public static void main(String[] args) {
        System.out.println("hello space!");
    }
    public static WarpMain instance;
    public BeaconManager beaconManagerServer;
    public BeaconManager beaconManagerClient;
    public DropPointMapDrawer dropPointMapDrawer;

    @Override
    public void onEnable() {
        super.onEnable();
        BackgroundEventListener.AddListener(); //add background color listener
        instance = this;

        PacketUtil.registerPacket(PacketHUDUpdate.class);
        PacketUtil.registerPacket(BeaconUpdatePacket.class);

        WarpSpaceMap.enable(instance);
        WarpBeaconAddon.registerAddonAddEventListener();

        dropPointMapDrawer = new DropPointMapDrawer(this);
    }
    
    @Override
    public void onDisable() {
        WarpSpaceMap.disable();
    //    PersistentObjectUtil.save(this.getSkeleton());
    }
    
    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        WarpJumpListener.createListener();
    //TODO thrust    ThrustEventhandler.createListener();
        WarpCheckLoop.loop(25);
        InterdictionHUDUpdateLoop.CreateServerLoop();
        beaconManagerServer = BeaconManager.getSavedOrNew(this.getSkeleton());
        beaconManagerServer.onInit();
        DebugChatEvent.addDebugChatListener();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
    //    DebugChatEvent.addDebugChatListener();
        WarpProcessController.initMap(); //build situation map for warp processes
        SpriteList.init();
        HUD_core.initList();
        GUIeventhandler.addHUDDrawListener();
        HUD_core.HUDLoop();
        beaconManagerClient = new BeaconManager();
        beaconManagerClient.onInit();
        dropPointMapDrawer.activate();
    }

    @Override
    public void onResourceLoad(ResourceLoader loader) {
        super.onResourceLoad(loader);
        dropPointMapDrawer.loadSprite();
    }

    @Override
    public void onBlockConfigLoad(BlockConfig blockConfig) {
        super.onBlockConfigLoad(blockConfig);
        WarpBeaconAddon.registerChamberBlock();
    }

    @Override
    public void onUniversalRegistryLoad() {
        super.onUniversalRegistryLoad();
        UniversalRegistry.registerURV(UniversalRegistry.RegistryType.PLAYER_USABLE_ID,this.getSkeleton(), WarpBeaconAddon.UIDName);
    }
}
