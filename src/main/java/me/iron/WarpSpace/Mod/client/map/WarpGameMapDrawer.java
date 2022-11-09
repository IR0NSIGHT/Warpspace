package me.iron.WarpSpace.Mod.client.map;


import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.Galaxy;

import api.DebugFile;
import api.listener.events.world.GalaxyInstantiateEvent;
import api.mod.StarLoader;
import me.iron.WarpSpace.Mod.WarpManager;

public class WarpGameMapDrawer extends GameMapDrawer {
    public WarpGameMapDrawer(GameClientState gameClientState) {
        super(gameClientState);
    }

    @Override
    public void draw() {
        DebugFile.log("player sector by map: "+getPlayerSector());
        DebugFile.log("gamemap pos current system: " + getGameMapPosition().getCurrentSysPos());
        super.draw();
    }

    @Override
    public Vector3i getPlayerSector() {
        try {
            return WarpManager.getInstance().getRealSpacePosPrecise(
                    GameClientState.instance.getPlayer().getCurrentSector(),
                    WarpManager.getInstance().getClientTransformOrigin()
            );
        } catch (NullPointerException ex) {
            return new Vector3i();
        }

    }

    @Override
    public void drawGalaxy() {
        Vector3i sector = WarpManager.getInstance().getRealSpacePosPrecise(
                GameClientState.instance.getPlayer().getCurrentSector(),
                WarpManager.getInstance().getClientTransformOrigin()
        );

        Galaxy g = getCurrentGalaxy(sector, GameClientState.instance.getPlayer());
        this.drawGalaxy(g);

        Galaxy.USE_GALAXY = false;
        super.drawGalaxy();
        Galaxy.USE_GALAXY = true;
    }

    public Galaxy getCurrentGalaxy(Vector3i sector, PlayerState player) {
        //help vars
        Vector3i tmpStellar = new Vector3i();
        Vector3i tmpGalaxy = new Vector3i();

        //get system and galaxy pos
        Vector3i sysPos = VoidSystem.getContainingSystem(
                (player.isInTutorial() || player.isInPersonalSector() || player.isInTestSector())
                        ? new Vector3i(0, 0, 0) : sector, tmpStellar);
        Vector3i galaxyPos = Galaxy.getContainingGalaxyFromSystemPos(sysPos, tmpGalaxy);

        long seed = GameClientState.instance.getGameState().getUniverseSeed() + galaxyPos.hashCode();
        assert (seed != 0);

        Galaxy galaxy = new Galaxy(seed, new Vector3i(galaxyPos));
        ///INSERTED CODE
        GalaxyInstantiateEvent e = new GalaxyInstantiateEvent(galaxy, sysPos);
        StarLoader.fireEvent(GalaxyInstantiateEvent.class, e, true);
        galaxy = e.getGalaxy();
        ///
        galaxy.generate();

        return galaxy;
    }
}
