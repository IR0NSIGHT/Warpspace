package me.iron.WarpSpace.Mod.client;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.SectorIndicationMode;
import org.schema.schine.input.InputState;

import com.bulletphysics.linearmath.Transform;

import me.iron.WarpSpace.Mod.WarpManager;

public class WarpHudIndicatorOverlay extends HudIndicatorOverlay {
    Vector3i[] neighborSectorsPos = new Vector3i[7];
    String[] neighborSectorsNames = new String[7];
    Transform[] neighborSectors = new Transform[7];

    HudIndicatorOverlay original;
    public WarpHudIndicatorOverlay(InputState inputState, HudIndicatorOverlay original) {
        super(inputState);
        this.original = original;
    }

    @Override
    public void onInit() {
        super.onInit();

        neighborSectorsPos = new Vector3i[7];
        neighborSectorsNames = new String[7];
        neighborSectors = new Transform[7];

        for (int i = 0; i < 7; i++) {
            neighborSectors[i] = new Transform();
            neighborSectors[i].setIdentity();

            neighborSectorsNames[i] = "";
            neighborSectorsPos[i] = new Vector3i();
        }
    }

    int i = 0;
    boolean inWarp;

    @Override
    public void draw() {
        if (!inWarp) {
            original.draw();
            return;
        }

        i++;
        if (i > 100) {
            i = 0;
            calcNeighborSectors();
        }
        this.drawSectorIndicators = false;
        super.draw();
        this.drawSectorIndicators = true;
        PlayerState player = ((GameClientState) getState()).getPlayer();
        Transform neighborT = new Transform();
        neighborT.setIdentity();

        if (player != null
                && !player.isInTutorial()
                && !player.isInPersonalSector()
                && !player.isInTestSector()) {
            if (EngineSettings.SECTOR_INDICATION_MODE.getCurrentState() != SectorIndicationMode.OFF && drawSectorIndicators) {
                for (int i = 0; i < 6; i++) {
                    neighborT.set(neighborSectors[i]);
                    drawFor(neighborT, neighborSectorsNames[i], -100 - i, EngineSettings.SECTOR_INDICATION_MODE.getCurrentState() == SectorIndicationMode.INDICATION_AND_ARROW, true);
                }
            }
        }
    }

    @Override
    public void onSectorChange() {
        super.onSectorChange();
        inWarp = WarpManager.getInstance().isInWarp(GameClientState.instance.getPlayer().getCurrentSector());
        original.onSectorChange();
        calcNeighborSectors();
    }

    private Vector3f getNeighbourDrawPosition(Vector3i rspSector, Vector3i ownSector) {
        Vector3i warpSector = WarpManager.getInstance().getWarpSpacePos(rspSector);
        Vector3i ownWarpSector = WarpManager.getInstance().getWarpSpacePos(ownSector);
        warpSector.sub(ownWarpSector);
        Vector3f diff = warpSector.toVector3f();
        WarpManager.getInstance().sectorsToMeter(diff);
        Vector3f origin = WarpManager.getInstance().getWarpOrigin(rspSector);
        origin.add(diff);
        return origin;
    }

    private void calcNeighborSectors() {
        Vector3i playerWarpSector = GameClientState.instance.getPlayer().getCurrentSector();
        Vector3i neighbourWarpSector = new Vector3i(playerWarpSector);

        Vector3i playerRspSector = WarpManager.getInstance().getRealSpacePosPrecise(
                neighbourWarpSector,
                WarpManager.getInstance().getClientTransformOrigin()
        );
        Vector3i neighbourRspSector = new Vector3i(playerRspSector);

        for (int i = 0; i < 6; i++) {
            Vector3i d = Element.DIRECTIONSi[i];

            //set rsp sector
            neighbourRspSector.set(playerRspSector);
            neighbourRspSector.add(d);




            //create stellar position for cross sector drawing
            WarpManager.StellarPosition warpPosition = WarpManager.getInstance().getWarpSpacePosPrecise(neighbourRspSector);
            Vector3f drawPosition = warpPosition.getPositionAdjustedFor(playerWarpSector);

            //set warp sector
            neighbourWarpSector.set(warpPosition.getSector());

            //neighborSectorsPos[i].set(neighbourWarpSector); //What is this used for?

            neighborSectorsPos[i].set(neighbourRspSector);
            try {
                neighborSectorsNames[i] = Lng.str("Warp %s", neighbourRspSector.toString());
            } catch(Exception ignored) {
                neighborSectorsNames[i] = "";
            }
            neighborSectors[i].origin.set(drawPosition);
        }

        neighborSectorsPos[6].set(playerRspSector);
        neighborSectors[6].origin.set(WarpManager.getInstance().getWarpOrigin(playerRspSector));
        neighborSectorsNames[6] = "drop " + playerRspSector.toString();
    }
}
