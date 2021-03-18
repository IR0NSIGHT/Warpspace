package me.iron.WarpSpace.Mod;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import api.network.packets.PacketUtil;
import me.iron.WarpSpace.Mod.HUD.client.GUIeventhandler;
import me.iron.WarpSpace.Mod.HUD.client.HUD_core;
import me.iron.WarpSpace.Mod.HUD.client.SpriteList;
import me.iron.WarpSpace.Mod.HUD.client.WarpProcessController;
import me.iron.WarpSpace.Mod.network.PacketHUDUpdate;
import me.iron.WarpSpace.Mod.server.WarpCheckLoop;
import me.iron.WarpSpace.Mod.server.WarpJumpEventHandler;
import me.iron.WarpSpace.Mod.server.WarpJumpListener;
import me.iron.WarpSpace.Mod.network.PacketSCUpdateWarp;
import me.iron.WarpSpace.Mod.taswin.WarpSpaceMap;

/**
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
        instance = this;
        PacketUtil.registerPacket(PacketSCUpdateWarp.class);
        PacketUtil.registerPacket(PacketHUDUpdate.class);
        
        WarpSpaceMap.enable(instance);
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
        WarpCheckLoop.loop(25); //TODO use a frequency from a config
        WarpJumpEventHandler.createServerListener();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
        //DebugChatEvent.addDebugChatListener();
        WarpProcessController.initMap(); //build situation map for warp processes
        SpriteList.init();
        HUD_core.initList();
        GUIeventhandler.addHUDDrawListener();
        HUD_core.HUDLoop();

    }

}
