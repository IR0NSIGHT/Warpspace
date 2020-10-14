import api.DebugFile;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.10.2020
 * TIME: 19:06
 */
public class main extends StarMod {
    public static void main(String[] args) {
        System.out.println("hello space!");
    }

    @Override
    public void onGameStart() {
        super.onGameStart();
        this.setModVersion("0.1");
        this.setModName("Warpspace");
        this.setModAuthor("IR0NSIGHT");
        DebugFile.log("Hello Space!");
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        JumpListener.createListener();
    }

}
