import api.DebugFile;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import api.network.packets.PacketUtil;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.10.2020
 * TIME: 19:06
 */

/**
 * the main class where the mod is run from by starloader.
 */
public class main extends StarMod {
    /**
     * empty method required for jar to build correctly. prints "hello space" if run.
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("hello space!");
    }
    public static StarMod instance;
    /**
     *  sets mod information like author, name and version.
     */
    @Override
    public void onGameStart() {
        super.onGameStart();
        this.setModVersion("0.4");
        this.setModName("WarpSpace");
        this.setModAuthor("IR0NSIGHT");
      //  this.addDependency("StarAPI");
   //     this.setServerSide(true); //needs client for packet receiving
        DebugFile.log("WarpSpace info set.",this);
   //     DebugFile.log(this.toString());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        DebugFile.log("enabled.",this);
        PacketUtil.registerPacket(PacketSCUpdateWarp.class);
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        DebugFile.log("WarpSpace creating listeners at server creation",this);
        JumpListener.createListener();
        CheeseCatchLoop.createLoop();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
        DebugFile.log("calling static method to register to GUI draw listener",main.instance);
        GUIeventhandler.addHUDDrawListener();
    }
}
