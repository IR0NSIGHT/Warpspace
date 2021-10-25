package me.iron.WarpSpace.Mod;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import api.mod.config.PersistentObjectUtil;
import api.network.packets.PacketUtil;
import me.iron.WarpSpace.Mod.HUD.client.*;
import me.iron.WarpSpace.Mod.HUD.client.map.DropPointMapDrawer;
import me.iron.WarpSpace.Mod.Interdiction.InterdictionHUDUpdateLoop;
import me.iron.WarpSpace.Mod.beacon.BeaconManager;
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
    public BeaconManager beaconManager;
    @Override
    public void onEnable() {
        super.onEnable();
        BackgroundEventListener.AddListener(); //add background color listener
        instance = this;
        PacketUtil.registerPacket(PacketHUDUpdate.class);
        
        WarpSpaceMap.enable(instance);
    }
    
    @Override
    public void onDisable() {
        WarpSpaceMap.disable();
        PersistentObjectUtil.save(this.getSkeleton());
    }
    
    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        WarpJumpListener.createListener();
    //TODO thrust    ThrustEventhandler.createListener();
        WarpCheckLoop.loop(25);
        InterdictionHUDUpdateLoop.CreateServerLoop();
        beaconManager = BeaconManager.getSavedOrNew(this.getSkeleton());
        beaconManager.onInit();
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
        beaconManager = new BeaconManager();
    }

    @Override
    public void onResourceLoad(ResourceLoader loader) {
        super.onResourceLoad(loader);
        new DropPointMapDrawer(this).loadSprite();
    }
}
