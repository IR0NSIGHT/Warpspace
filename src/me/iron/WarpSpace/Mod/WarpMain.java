package me.iron.WarpSpace.Mod;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.ModStarter;
import api.mod.StarMod;
import api.network.packets.PacketUtil;
import me.iron.WarpSpace.Mod.HUD.client.*;
import me.iron.WarpSpace.Mod.Interdiction.InterdictionHUDUpdateLoop;
import me.iron.WarpSpace.Mod.network.PacketHUDUpdate;
import me.iron.WarpSpace.Mod.server.WarpCheckLoop;
import me.iron.WarpSpace.Mod.server.WarpJumpListener;
import me.iron.WarpSpace.Mod.taswin.WarpSpaceMap;
import me.iron.WarpSpace.Mod.visuals.BackgroundEventListener;
import me.iron.WarpSpace.Mod.visuals.WarpSkybox;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.resource.MeshLoader;
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
    public static StarMod instance;

    @Override
    public void onEnable() {
        super.onEnable();
        BackgroundEventListener.AddListener(); //add background color listener
        instance = this;
        WarpSkybox.instantiate();
        PacketUtil.registerPacket(PacketHUDUpdate.class);
        
        WarpSpaceMap.enable(instance);
    }

    @Override
    public void onResourceLoad(ResourceLoader loader) {
        WarpSkybox.loadResources(loader.getMeshLoader(), this);
    }

    @Override
    public void onDisable() {
        WarpSpaceMap.disable();
    }
    
    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        WarpJumpListener.createListener();
    //TODO thrust    ThrustEventhandler.createListener();
        WarpCheckLoop.loop(25);
        InterdictionHUDUpdateLoop.CreateServerLoop();
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
    }

}
