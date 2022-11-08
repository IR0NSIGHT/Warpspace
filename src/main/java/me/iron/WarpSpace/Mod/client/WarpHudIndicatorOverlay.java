package me.iron.WarpSpace.Mod.client;

import java.util.concurrent.TimeUnit;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

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

    public WarpHudIndicatorOverlay(InputState inputState) {
        super(inputState);
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
        i++;
        if (i > 100) {
            i = 0;
            calcNeighborSectors();
        }
        this.drawSectorIndicators = false;
        this.drawWaypoints = false;
        super.draw();
        this.drawSectorIndicators = true;
        this.drawWaypoints = true;

        drawNeighbourSectors(neighborSectors, neighborSectorsNames);
        drawWaypoint(
                ((GameClientState) getState()).getController().getClientGameData().getRspWaypoint(),
                GameClientState.instance.getPlayer().getCurrentSector(),
                WarpManager.getInstance().getClientTransformOrigin()
        );
    }

    void drawWaypoint(Vector3i waypointRspSector, Vector3i playerWarpSector, Vector3f playerWarpOrigin) {
        if (waypointRspSector != null & drawWaypoints) {
            WarpManager.StellarPosition p = WarpManager.getInstance().getWarpSpacePosition(waypointRspSector);
            Vector4f tint = new Vector4f();
            tint.set(0.1f + selectColorValue, 0.8f + selectColorValue, 0.6f + selectColorValue, 0.4f + selectColorValue);
            Transform t = new Transform();
            t.setIdentity();
            t.origin.set(p.getPositionAdjustedFor(playerWarpSector));

            Vector3f toWaypoint = p.getFromTo(playerWarpSector, playerWarpOrigin);
            float metersToWp = toWaypoint.length();///WarpManager.getInstance().getScale();
            float currentSpeedAligned;
            Vector3f velocity = new Vector3f();
            GameClientState.instance.getPlayer().getFirstControlledTransformableWOExc().getLinearVelocity(velocity);
            toWaypoint.normalize();
            currentSpeedAligned = velocity.dot(toWaypoint);
            String text = Lng.str(
                    "Waypoint" +
                            waypointRspSector.toString() + "\ntime: " + (currentSpeedAligned > 0.1f ? formatETA((int) (metersToWp / currentSpeedAligned)) : -1)
                    //v = s/t <=> t = s/v
            );
            drawFor(t, text, -300, true, true);
        }
    }

    String formatETA(int seconds) {
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    void drawNeighbourSectors(Transform[] neighborSectors, String[] neighborSectorsNames) {
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
        calcNeighborSectors();
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
            WarpManager.StellarPosition warpPosition = WarpManager.getInstance().getWarpSpacePosition(neighbourRspSector);
            Vector3f drawPosition = warpPosition.getPositionAdjustedFor(playerWarpSector);

            //set warp sector
            neighbourWarpSector.set(warpPosition.getSector());

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
