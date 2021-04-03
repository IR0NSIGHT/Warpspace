//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.schema.game.client.view.gui.shiphud;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import me.iron.WarpSpace.Mod.WarpManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.ship.ShipExternalFlightController;
import org.schema.game.client.controller.tutorial.TutorialMode;
import org.schema.game.client.controller.tutorial.states.TutorialMarker;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.data.gamemap.GameMap;
import org.schema.game.client.data.gamemap.entry.MapEntryInterface;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.client.view.gamemap.StarPosition;
import org.schema.game.client.view.gui.GuiDrawer;
import org.schema.game.client.view.gui.shiphud.newhud.IndicatorIndices;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.activities.Race;
import org.schema.game.common.controller.activities.Race.RaceState;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.rail.pickup.RailPickupCollectionManager;
import org.schema.game.common.controller.elements.rail.pickup.RailPickupUnit;
import org.schema.game.common.controller.trade.TradeNodeClient.TradeNodeMapIndication;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.fleet.FleetMember.FleetMemberMapIndication;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.TransformaleObjectTmpVars;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.server.data.Galaxy;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.SectorIndicationMode;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.ColoredInterface;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.util.WorldToScreenConverter;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;
import org.schema.schine.graphicsengine.util.timer.TimerUtil;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.Sendable;

/**
 * this is ripped code from the decompiled starmade jar.
 * It was modified to allow the neighbouring sector indicator texts to be overwritten.
 */
public class HudIndicatorOverlay extends GUIElement {
    public static List<Indication> toDrawTexts = new ObjectArrayList();
    public static List<Indication> toDrawMapTexts = new ObjectArrayList();
    public static ObjectOpenHashSet<GameMap> toDrawMapInterfaces = new ObjectOpenHashSet();
    public static ObjectOpenHashSet<StarPosition> toDrawStars = new ObjectOpenHashSet();
    public static ObjectOpenHashSet<FleetMemberMapIndication> toDrawFleet = new ObjectOpenHashSet();
    public static ObjectOpenHashSet<TradeNodeMapIndication> toDrawTradeNodes = new ObjectOpenHashSet();
    public static boolean DRAW_ALL_INFO = false;
    public static float selectColorValue;
    private static ObjectOpenHashSet<Vector3f> toDrawTextsSet = new ObjectOpenHashSet();
    private static FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
    private static FloatBuffer modelviewTmp = BufferUtils.createFloatBuffer(16);
    private final Transform[] neighborSectors;
    private final String[] neighborSectorsNames;
    private final ObjectArrayFIFOQueue<HudIndicatorOverlay.IndicationText> texts = new ObjectArrayFIFOQueue();
    private final Vector3f pTmp = new Vector3f();
    private final Vector3f middleTmp = new Vector3f();
    private final Vector3f defaultTextOnScrenIndication = new Vector3f();
    Vector3f dir = new Vector3f();
    Vector3f cross = new Vector3f();
    Vector4f tint = new Vector4f(0.0F, 1.0F, 0.0F, 1.0F);
    Vector3f lastDir = new Vector3f();
    Transform localSec = new Transform();
    private GUIOverlay indicator;
    private GUIOverlay targetOverlay;
    private GUITextOverlay textOverlay;
    private Int2IntArrayMap animap = new Int2IntArrayMap();
    private WorldToScreenConverter worldToScreenConverter;
    private long lastTimeUpdate;
    private boolean updateAnim;
    private Transformable cameraTransformable;
    private TimerUtil timerUtil;
    private SimpleTransformableSendableObject selectedEntity;
    private int screenCap;
    private Vector3i[] neighborSectorsPos;
    private Vector3i tmpSysPos = new Vector3i();
    private Transform transR = new Transform();
    private Transform neighborT = new Transform();
    private TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();
    private GUIOverlay arrow;
    private ObjectArrayFIFOQueue<HudIndicatorOverlay.IndicationText> pool = new ObjectArrayFIFOQueue();
    private boolean drawDockingAreas;
    Transform posTmp = new Transform();
    private GUIOverlay leadIndicator;
    private final Vector3f otherSecCenterTmp = new Vector3f();
    private final Matrix3f rotTmp = new Matrix3f();
    private final Vector3i pPosTmp = new Vector3i();
    private Vector3f bbTmp = new Vector3f();
    private Transform tmpTrsns = new Transform();
    private SimpleTransformableSendableObject selectedAIEntity;
    private long lastExp;
    private final Vector3f dirTmp = new Vector3f();
    private float currentDelta;

    public HudIndicatorOverlay(InputState var1) {
        super(var1);
        this.posTmp.setIdentity();
        Sprite var2 = Controller.getResLoader().getSprite("hud-target-c-4x4-gui-");
        this.indicator = new GUIOverlay(var2, var1);
        this.targetOverlay = new GUIOverlay(Controller.getResLoader().getSprite("hud-target-c-4x4-gui-"), var1);
        this.arrow = new GUIOverlay(Controller.getResLoader().getSprite("hud_pointers-c-8x8"), var1);
        this.leadIndicator = new GUIOverlay(Controller.getResLoader().getSprite("hud_pointers-c-8x8"), var1);
        this.leadIndicator.setSpriteSubIndex(33);
        this.textOverlay = new GUITextOverlay(32, 32, var1);
        this.timerUtil = new SinusTimerUtil(8.0F);
        this.worldToScreenConverter = ((GameClientState)this.getState()).getScene().getWorldToScreenConverter();
        this.neighborSectorsPos = new Vector3i[6];
        this.neighborSectors = new Transform[6];
        this.neighborSectorsNames = new String[6];

        for(int var3 = 0; var3 < this.neighborSectors.length; ++var3) {
            this.neighborSectorsPos[var3] = new Vector3i();
            this.neighborSectors[var3] = new Transform();
            this.neighborSectors[var3].setIdentity();
        }

        this.calcNeighborSectors(((GameClientState)var1).getInitialSectorPos());
    }

    public static void getColor(SimpleTransformableSendableObject var0, Vector4f var1, boolean var2, GameClientState var3) {
        if (var2) {
            var1.x = 0.3F + selectColorValue;
            var1.y = 0.3F + selectColorValue;
            var1.z = 0.3F + selectColorValue;
        } else {
            RType var4 = null;
            ArrayList var5 = null;
            float var6 = var2 ? selectColorValue : 0.0F;
            if (var0 instanceof PlayerCharacter && !(var5 = ((PlayerCharacter)var0).getAttachedPlayers()).isEmpty()) {
                var4 = ((PlayerState)var5.get(0)).getRelation(var3.getPlayer());
            }

            List var7;
            if (var0 instanceof PlayerControllable && !(var7 = ((PlayerControllable)var0).getAttachedPlayers()).isEmpty()) {
                var4 = ((PlayerState)var7.get(0)).getRelation(var3.getPlayer());
            }

            if (var4 == null) {
                var4 = var3.getPlayer().getRelation(var0.getFactionId());
            }

            var0.getRelationColor(var4, var3.getPlayer().getFactionId() != 0 && var3.getPlayer().getFactionId() == var0.getFactionId(), var1, var6, selectColorValue);
            if (var0 instanceof SegmentController && ((SegmentController)var0).railController.isDockedAndExecuted()) {
                if (var3.getCurrentPlayerObject() != null && ((SegmentController)var0).railController.previous.rail.getSegmentController() == var3.getCurrentPlayerObject() && !var3.getGlobalGameControlManager().getIngameControlManager().isInBuildMode()) {
                    var1.set(0.0F, 0.0F, 0.0F, 0.0F);
                } else {
                    var1.x = Math.max(var1.x - 0.2F, 0.01F);
                    var1.y = Math.max(var1.y - 0.2F, 0.01F);
                    var1.z = Math.max(var1.z + 0.2F, 0.01F);
                }
            }

            if (var0 instanceof SegmentController && ((SegmentController)var0).getDockingController().isDocked()) {
                if (var3.getCurrentPlayerObject() != null && ((SegmentController)var0).getDockingController().getDockedOn().to.getSegment().getSegmentController() == var3.getCurrentPlayerObject() && !var3.getGlobalGameControlManager().getIngameControlManager().isInBuildMode()) {
                    var1.set(0.0F, 0.0F, 0.0F, 0.0F);
                    return;
                }

                var1.x = Math.max(var1.x - 0.2F, 0.01F);
                var1.y = Math.max(var1.y - 0.2F, 0.01F);
                var1.z = Math.max(var1.z + 0.2F, 0.01F);
            }

        }
    }

    public static float getRotation(SimpleTransformableSendableObject var0, boolean var1, GameClientState var2) {
        return var0 instanceof SegmentController && ((SegmentController)var0).railController.isDockedAndExecuted() ? 0.7853982F : 0.0F;
    }

    public static float getScale(SimpleTransformableSendableObject var0, boolean var1, GameClientState var2) {
        return var0 instanceof SegmentController && ((SegmentController)var0).railController.isDockedAndExecuted() ? 0.5F : 1.0F;
    }

    //!! renamed the variables, not sure if all names are correct.
    private void calcNeighborSectors(Vector3i center) {
        int sectorSize = (int)((GameClientState)this.getState()).getSectorSize();
        Vector3i neighbour = new Vector3i();
        Vector3f var4 = new Vector3f(); //global position?
        neighbour.set(center);

        for(int i = 0; i < 6; ++i) {
            this.neighborSectorsPos[i].set(center);
            neighbour.set(center);
            Vector3i directionNeighbour = Element.DIRECTIONSi[i];
            neighbour.add(directionNeighbour);
            this.neighborSectorsPos[i].set(neighbour);
            var4.set((float)(directionNeighbour.x * sectorSize), (float)(directionNeighbour.y * sectorSize), (float)(directionNeighbour.z * sectorSize));
            String neighbourName = neighbour.toString();
            if (WarpManager.IsInWarp(neighbour)) {
                neighbourName = WarpManager.GetRealSpacePos(neighbour).toString();
            };
            this.neighborSectorsNames[i] = StringTools.format(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHIPHUD_HUDINDICATOROVERLAY_3, new Object[]{neighbourName});
            this.neighborSectors[i].origin.set(var4);
        }

    }

    private boolean calcSecPos(int var1, Transform var2) {
        Vector3i var3 = this.neighborSectorsPos[var1];
        this.calcWaypointSecPos(var3, var2);
        return ((GameClientState)this.getState()).getController().getClientGameData().getNearestToWayPoint() != null ? var3.equals(((GameClientState)this.getState()).getController().getClientGameData().getNearestToWayPoint()) : false;
    }

    private void calcWaypointSecPos(Vector3i var1, Transform var2) {
        StellarSystem.getPosFromSector(var1, this.tmpSysPos);
        this.pPosTmp.set(var1);
        GameClientState var4 = (GameClientState)this.getState();
        this.pPosTmp.sub(var4.getPlayer().getCurrentSector());
        var2.setIdentity();
        float var3 = var4.getGameState().getRotationProgession();
        this.otherSecCenterTmp.set((float)this.pPosTmp.x * var4.getSectorSize(), (float)this.pPosTmp.y * var4.getSectorSize(), (float)this.pPosTmp.z * var4.getSectorSize());
        this.rotTmp.rotX(6.2831855F * var3);
        Sendable var5;
        if ((var5 = (Sendable)var4.getLocalAndRemoteObjectContainer().getLocalObjects().get(var4.getCurrentSectorId())) != null && var5 instanceof RemoteSector && ((RemoteSector)var5).getType() == SectorType.PLANET) {
            this.rotTmp.invert();
            (new Vector3f()).add(this.otherSecCenterTmp);
            TransformTools.rotateAroundPoint(this.bbTmp, this.rotTmp, var2, new Transform());
            var2.origin.add(this.otherSecCenterTmp);
        } else {
            var2.origin.set(this.otherSecCenterTmp);
        }
    }

    private void drawStarMarker() {
        GameClientState var1;
        if ((var1 = (GameClientState)this.getState()).getCurrentClientSystem() != null) {
            Galaxy var2 = var1.getCurrentGalaxy();
            VoidSystem var3;
            Vector3i var4 = (var3 = var1.getCurrentClientSystem()).getPos();
            Vector3i var5 = var1.getPlayer().getCurrentSector();
            Vector3i var6 = VoidSystem.getSunSectorPosAbs(var2, var4, new Vector3i());
            float var10 = var2.getSystemSunIntensity(var4);
            float var7 = var2.getSunDistance(var5);
            float var8 = var1.getGameState().sunMinIntensityDamageRange;
            if (var2.getSystemTypeAt(var1.getPlayer().getCurrentSystem()) == SectorType.SUN && (var3.isHeatDamage(var5, var10, var7, var8) || var3.getDistanceIntensity(var10, var7) < var8 * 2.0F)) {
                Transform var9;
                (var9 = new Transform()).setIdentity();
                this.calcWaypointSecPos(var6, var9);
                this.tint.set(0.25F + selectColorValue, 0.0F, 0.0F, selectColorValue);
                this.textOverlay.setColor(0.25F + selectColorValue, 0.0F, 0.0F, 0.25F + selectColorValue);
                this.drawFor(var9, Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHIPHUD_HUDINDICATOROVERLAY_5, -50, true, true);
                this.textOverlay.setColor(Color.white);
            }
        }

    }

    public float cap(float var1, int var2, int var3) {
        return Math.min((float)var3, Math.max((float)var2, var1));
    }

    public void cleanUp() {
    }

    public void draw() {
        if (this.lastTimeUpdate + 50L < System.currentTimeMillis()) {
            this.updateAnim = true;
            this.lastTimeUpdate = System.currentTimeMillis();
        } else {
            this.updateAnim = false;
        }

        RemoteSector var1;
        if ((var1 = ((GameClientState)this.getState()).getCurrentRemoteSector()) == null || !var1.isNoIndicationsClient()) {
            this.drawDockingAreas = false;
            Vector3i var3;
            Ship var12;
            SegmentPiece var13;
            if (((GameClientState)this.getState()).getShip() != null && (var3 = (var12 = ((GameClientState)this.getState()).getShip()).getSlotAssignment().get(((GameClientState)this.getState()).getPlayer().getCurrentShipControllerSlot())) != null && (var13 = var12.getSegmentBuffer().getPointUnsave(var3)) != null && var13.getType() == 663) {
                this.drawDockingAreas = true;
            }

            PlayerInteractionControlManager var14 = ((GameClientState)this.getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
            this.selectedEntity = var14.getSelectedEntity();
            this.selectedAIEntity = var14.getSelectedAITarget();
            this.cameraTransformable = null;
            if (Controller.getCamera().getViewable() instanceof FixedViewer) {
                FixedViewer var2 = (FixedViewer)Controller.getCamera().getViewable();
                this.cameraTransformable = var2.getEntity();
            }

            TutorialMode var15;
            int var16;
            if ((var15 = ((GameClientState)this.getState()).getController().getTutorialMode()) != null && var15.markers != null) {
                Transform var22;
                (var22 = new Transform()).setIdentity();

                for(var16 = 0; var16 < var15.markers.size(); ++var16) {
                    TutorialMarker var30;
                    if ((var30 = (TutorialMarker)var15.markers.get(var16)).absolute != null) {
                        var22.origin.set(var30.absolute);
                    } else if (var30.context != null) {
                        var30.context.getAbsoluteElementWorldPositionShifted(var30.where, var22.origin);
                    } else if (var15.currentContext != null) {
                        var15.currentContext.getAbsoluteElementWorldPositionShifted(var30.where, var22.origin);
                    } else {
                        var22.origin.set((float)var30.where.x, (float)var30.where.y, (float)var30.where.z);
                    }

                    this.tint.set(0.0F + selectColorValue, 1.0F - selectColorValue, selectColorValue, 0.99F);
                    this.drawFor(var22, var30.markerText, -1000 - var16, true, true);
                }

            } else {
                PlayerState var19;
                if ((var19 = ((GameClientState)this.getState()).getPlayer()) != null && !var19.isInTutorial() && !var19.isInPersonalSector() && !var19.isInTestSector() && EngineSettings.SECTOR_INDICATION_MODE.getCurrentState() != SectorIndicationMode.OFF) {
                    for(var16 = 0; var16 < this.neighborSectors.length; ++var16) {
                        assert this.neighborSectorsNames[var16] != null;

                        this.neighborT.set(this.neighborSectors[var16]);
                        this.calcSecPos(var16, this.neighborT);
                        this.tint.set(0.4F, 0.4F, 0.4F, 0.4F);
                        this.drawFor(this.neighborT, this.neighborSectorsNames[var16], -100 - var16, EngineSettings.SECTOR_INDICATION_MODE.getCurrentState() == SectorIndicationMode.INDICATION_AND_ARROW, true);
                    }
                }

                Vector3i waypointPos;
                if ((waypointPos = ((GameClientState)this.getState()).getController().getClientGameData().getWaypoint()) != null) {
                    this.tint.set(0.1F + selectColorValue, 0.8F + selectColorValue, 0.6F + selectColorValue, 0.4F + selectColorValue);
                    Transform waypointTransform;
                    (waypointTransform = new Transform()).setIdentity();
                    this.calcWaypointSecPos(new Vector3i(waypointPos), waypointTransform); //waypoint code
                    if (WarpManager.IsInWarp(waypointPos)) {
                      waypointPos = WarpManager.GetRealSpacePos(waypointPos);
                    }
                    this.drawFor(waypointTransform, StringTools.format(" waypoint " + waypointPos.toString(), new Object[]{waypointPos.toString()}), -300, true, true);
                }

                this.drawStarMarker();
                if (((GameClientState)this.getState()).getRaceManager().isInRunningRace(((GameClientState)this.getState()).getPlayer())) {
                    Race var24;
                    RaceState var7 = (var24 = ((GameClientState)this.getState()).getRaceManager().getRace(((GameClientState)this.getState()).getPlayer())).getRaceState(((GameClientState)this.getState()).getPlayer());
                    if (!var24.isFinished()) {
                        String var20;
                        if (var7.isActive()) {
                            this.tint.set(0.9F + selectColorValue, 0.3F + selectColorValue, 0.1F + selectColorValue, 1.0F);
                            this.drawFor(var7.totalDistance, "[Race] Gate #" + (var7.currentGate + 1) + "/" + (var24.getTotalGates() - 1), -300, true, true);
                            var20 = StringTools.formatRaceTime(System.currentTimeMillis() - var24.raceStart);
                            ((GameClientState)this.getState()).getController().showBigTitleMessage("RACE_RANK", StringTools.format(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHIPHUD_HUDINDICATOROVERLAY_0, new Object[]{var7.currentRank, var20}), 0.0F);
                        } else if (var7.getFinishedTime() > 0L && System.currentTimeMillis() - var7.getFinishedTime() > 30000L) {
                            var20 = StringTools.formatRaceTime(var7.getFinishedTime() - var24.raceStart);
                            ((GameClientState)this.getState()).getController().showBigTitleMessage("RACE_RANK", StringTools.format(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHIPHUD_HUDINDICATOROVERLAY_1, new Object[]{var7.currentRank, var20}), 0.0F);
                        }
                    }
                }

                GlUtil.glPushMatrix();
                Iterator var26 = ((GameClientState)this.getState()).getCurrentSectorEntities().values().iterator();

                while(true) {
                    SimpleTransformableSendableObject var8;
                    ManagerContainer var23;
                    SimpleTransformableSendableObject var31;
                    do {
                        do {
                            do {
                                do {
                                    if (!var26.hasNext()) {
                                        GlUtil.glPopMatrix();
                                        GlUtil.glPushMatrix();
                                        Sprite.startDraw(this.arrow.getSprite());
                                        var26 = ((GameClientState)this.getState()).getCurrentSectorEntities().values().iterator();

                                        while(var26.hasNext()) {
                                            if ((var31 = (SimpleTransformableSendableObject)var26.next()) != this.cameraTransformable) {
                                                this.drawFor(var31);
                                            }
                                        }

                                        Sprite.endDraw(this.arrow.getSprite());
                                        GlUtil.glPopMatrix();

                                        while(!this.texts.isEmpty()) {
                                            HudIndicatorOverlay.IndicationText var28 = (HudIndicatorOverlay.IndicationText)this.texts.dequeue();
                                            this.textOverlay.getPos().set(var28.v);
                                            this.textOverlay.getText().set(0, var28.s0);
                                            this.textOverlay.getText().set(1, var28.s1);
                                            this.textOverlay.draw();
                                            this.pool.enqueue(var28);
                                        }

                                        try {
                                            toDrawTextsSet.clear();

                                            for(int var29 = 0; var29 < toDrawTexts.size(); ++var29) {
                                                this.drawString((Indication)toDrawTexts.get(var29), Controller.getCamera(), true, ((Indication)toDrawTexts.get(var29)).getDist(), this.worldToScreenConverter);
                                            }

                                            return;
                                        } catch (IndexOutOfBoundsException var11) {
                                            long var32 = ((GameClientState)this.getState()).getUpdateTime();
                                            if (this.lastExp - var32 > 20000L) {
                                                System.err.println("[CLIENT][HUD] OOB INDEX");
                                                this.lastExp = var32;
                                            }

                                            return;
                                        }
                                    }
                                } while((var31 = (SimpleTransformableSendableObject)var26.next()) == this.cameraTransformable);

                                var8 = var31;
                            } while(!(var31 instanceof ManagedSegmentController));
                        } while(!this.drawDockingAreas);
                    } while((var23 = ((ManagedSegmentController)var31).getManagerContainer()).getSegmentController() instanceof Ship && ((Ship)var23.getSegmentController()).isJammingFor(((GameClientState)this.getState()).getCurrentPlayerObject()));

                    GlUtil.glPushMatrix();
                    this.tint.set(0.89F, 0.64F, 0.77F, 0.7F);
                    List var25;
                    if (!(var25 = ((RailPickupCollectionManager)var23.getRailPickup().getCollectionManager()).getElementCollections()).isEmpty()) {
                        LongArrayList var17;
                        RailPickupUnit var27;
                        int var21 = (var17 = (var27 = (RailPickupUnit)var25.get(0)).getNeighboringCollection()).size();

                        for(int var5 = 0; var5 < var21; ++var5) {
                            long var9 = var17.getLong(var5);
                            if (var27.isActive(var9)) {
                                ElementCollection.getPosFromIndex(var9, this.posTmp.origin);
                                Vector3f var10000 = this.posTmp.origin;
                                var10000.x -= 16.0F;
                                var10000 = this.posTmp.origin;
                                var10000.y -= 16.0F;
                                var10000 = this.posTmp.origin;
                                var10000.z -= 16.0F;
                                var8.getWorldTransformOnClient().transform(this.posTmp.origin);
                                String var6;
                                if (var8 == this.selectedEntity) {
                                    var6 = var8.getRealName() + Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHIPHUD_HUDINDICATOROVERLAY_2;
                                } else {
                                    var6 = "";
                                }

                                this.drawFor(this.posTmp, var6, -2000 - var8.getId() - var5 * 100000, false, var8 == this.selectedEntity || var8 == this.selectedAIEntity);
                            }
                        }
                    }

                    GlUtil.glPopMatrix();
                }
            }
        }
    }

    public void onInit() {
        this.indicator.onInit();
        this.textOverlay = new GUITextOverlay(32, 32, FontLibrary.getBoldArial12White(), this.getState());
        this.textOverlay.setColor(Color.white);
        this.textOverlay.setText(new ArrayList());
        this.textOverlay.getText().add("");
        this.textOverlay.getText().add("");
        this.textOverlay.setPos(2.0F, 2.0F, 0.0F);
        this.textOverlay.onInit();
    }

    public void doOrientation() {
    }

    public float getHeight() {
        return (float)GLFrame.getHeight();
    }

    public float getWidth() {
        return (float)GLFrame.getWidth();
    }

    public boolean isPositionCenter() {
        return false;
    }

    public void drawFor(SimpleTransformableSendableObject var1) {
        GameClientState var2 = (GameClientState)this.getState();
        if (Controller.getCamera() != null) {
            if (!(var1 instanceof Ship) || !((Ship)var1).isJammingFor(((GameClientState)this.getState()).getCurrentPlayerObject())) {
                if (((GameClientState)this.getState()).getPlayer() == null || !((GameClientState)this.getState()).getPlayer().isInTutorial() || var1 instanceof AICreature) {
                    if (!(var1 instanceof AbstractCharacter) || ((AbstractCharacter)var1).getAttachedPlayers().isEmpty() || !((AbstractOwnerState)((AbstractCharacter)var1).getAttachedPlayers().get(0)).isInvisibilityMode()) {
                        if (var2.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager().isDisplayed(var1)) {
                            boolean var3 = this.getExternalShipMan().isTreeActive() && this.getExternalShipMan().getAquire().isTargetMode() && this.getExternalShipMan().getAquire().getTarget() == var1;
                            RType var4 = var2.getPlayer().getRelation(var1.getFactionId());
                            getColor(var1, this.tint, var1 == this.selectedEntity, var2);
                            if (var1 == this.selectedAIEntity) {
                                this.tint.set(0.71F, 0.32F, 0.53F, 1.0F);
                            }

                            float var5 = getScale(var1, var1 == this.selectedEntity, var2);
                            float var6 = getRotation(var1, var1 == this.selectedEntity, var2);
                            if (this.tint.lengthSquared() != 0.0F) {
                                Vector3f var7 = this.worldToScreenConverter.getMiddleOfScreen(this.middleTmp);
                                PlayerCharacter var8;
                                Vector3f var16;
                                if ((var8 = ((GameClientState)this.getState()).getCharacter()) == null || var8.getGravity() == null || var8.getGravity().source == null || var8.getGravity().source != var1 && (!(var1 instanceof SegmentController) || !((SegmentController)var1).railController.isInAnyRailRelationWith(var8.getGravity().source))) {
                                    var16 = var1.getWorldTransformOnClientCenterOfMass(this.tmpTrsns).origin;
                                } else {
                                    var16 = var1.getWorldTransformOnClient().origin;
                                }

                                this.dir.set(var16);
                                if (var2.getCurrentPlayerObject() != null) {
                                    this.dir.sub(var2.getCurrentPlayerObject().getWorldTransform().origin);
                                } else {
                                    this.dir.sub(Controller.getCamera().getPos());
                                }

                                float var9 = this.dir.length();
                                if ((var3 || var1 == this.selectedEntity || var1 == this.selectedAIEntity || var9 <= var1.getIndicatorMaxDistance(var4)) && var9 >= 1.0F && !var1.isHidden()) {
                                    if (var3) {
                                        this.tint.set(1.0F, 1.0F, 0.2F, 1.0F);
                                    }

                                    if (!var3 && var1 != this.selectedEntity && var1 != this.selectedAIEntity) {
                                        this.tint.w = Math.min(1.0F, var1.getIndicatorMaxDistance(var4) / (var9 * 10.0F));
                                    } else {
                                        this.tint.w = 1.0F;
                                    }

                                    var3 = false;
                                    Vector4f var10000;
                                    if (var1 == this.selectedEntity && var1 instanceof SegmentController && ((GameClientState)this.getState()).getPlayer() != null && ((GameClientState)this.getState()).getPlayer().getFirstControlledTransformableWOExc() instanceof ManagedUsableSegmentController) {
                                        ManagedUsableSegmentController var14 = (ManagedUsableSegmentController)((GameClientState)this.getState()).getPlayer().getFirstControlledTransformableWOExc();
                                        float var10 = 0.0F;
                                        Iterator var11 = ((GameClientState)this.getState()).getPlayer().getControllerState().getUnits().iterator();

                                        while(var11.hasNext()) {
                                            ControllerStateUnit var12;
                                            if ((var12 = (ControllerStateUnit)var11.next()).playerControllable == var14) {
                                                var14.getManagerContainer().getSelectedWeaponRange(var12);
                                                var10 = var14.getManagerContainer().getSelectedWeaponSpeed(var12);
                                                break;
                                            }
                                        }

                                        SegmentController var17 = (SegmentController)var1;
                                        this.dirTmp.sub(var17.getPhysicsDataContainer().getCurrentPhysicsTransform().origin, var17.getPhysicsDataContainer().lastTransform.origin);
                                        Vector3f var15;
                                        if (var17.railController.getRoot() instanceof Ship && var17.railController.getRoot().getPhysicsDataContainer().getObject() instanceof RigidBody && var10 > 0.0F && (var15 = ((RigidBody)((Ship)var17.railController.getRoot()).getPhysicsDataContainer().getObject()).getLinearVelocity(new Vector3f())).lengthSquared() > 0.0F) {
                                            new Vector3f();
                                            Vector3f var13;
                                            (var13 = Vector3fTools.predictPoint(var16, var15, var10, Controller.getCamera().getPos())).add(Controller.getCamera().getPos());
                                            this.dir.set(var13);
                                            if (var2.getCurrentPlayerObject() != null) {
                                                this.dir.sub(var2.getCurrentPlayerObject().getWorldTransform().origin);
                                            } else {
                                                this.dir.sub(Controller.getCamera().getPos());
                                            }

                                            var9 = this.dir.length();
                                            var10000 = this.tint;
                                            var10000.x += 0.5F;
                                            var10000 = this.tint;
                                            var10000.y -= 0.3F;
                                            var10000 = this.tint;
                                            var10000.z -= 0.2F;
                                            this.putOnScreen(var13, var1, var1.getId() - 11111111, var7, var6, var5, var9, false, true);
                                            var3 = true;
                                        }
                                    }

                                    if (var3) {
                                        var10000 = this.tint;
                                        var10000.x -= 0.2F;
                                        var10000 = this.tint;
                                        var10000.y -= 0.3F;
                                        var10000 = this.tint;
                                        var10000.z += 0.4F;
                                        var10000 = this.tint;
                                        var10000.w *= 0.5F;
                                    }

                                    this.putOnScreen(var16, var1, var1.getId(), var7, var6, var5, var9, true, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void putOnScreen(Vector3f var1, SimpleTransformableSendableObject var2, int var3, Vector3f var4, float var5, float var6, float var7, boolean var8, boolean var9) {
        var1 = this.worldToScreenConverter.convert(var1, this.pTmp, true);
        (var1 = new Vector3f(var1)).sub(var4);
        int var15;
        if (var1.length() > (float)this.screenCap) {
            var1.normalize();
            var1.scale((float)this.screenCap);
            var1.add(var4);
            this.arrow.getSprite().setTint(this.tint);
            var15 = IndicatorIndices.getCIndex(var2);
            this.arrow.setSpriteSubIndex(var15);
            this.arrow.getSprite().setSelectedMultiSprite(var15);
            this.arrow.getPos().set((float)((int)var1.x), (float)((int)var1.y), (float)((int)var1.z));
            Vector3f var14;
            (var14 = new Vector3f(this.arrow.getPos())).sub(var4);
            var14.scale(-1.0F);
            var14.normalize();
            float var11 = Vector3fTools.getFullRange2DAngleFast(new Vector3f(0.0F, 1.0F, 0.0F), var14);
            modelview.rewind();
            Controller.modelviewMatrix.store(modelview);
            float var13 = FastMath.sinFast(var11);
            var5 = FastMath.cosFast(var11);
            modelview.put(0, var5);
            modelview.put(4, -var13);
            modelview.put(8, 0.0F);
            modelview.put(1, var13);
            modelview.put(5, var5);
            modelview.put(9, 0.0F);
            modelview.put(2, 0.0F);
            modelview.put(6, 0.0F);
            modelview.put(10, 1.0F);
            modelview.put(12, (float)((int)(Controller.modelviewMatrix.m30 + var1.x)));
            modelview.put(13, (float)((int)(Controller.modelviewMatrix.m31 + var1.y)));
            modelview.put(14, (float)((int)(Controller.modelviewMatrix.m32 + var1.z)));
            modelview.rewind();
            GL11.glLoadMatrix(modelview);
            this.arrow.getSprite().setScale(var6, var6, var6);
            Sprite.doDraw(this.arrow.getSprite());
            this.arrow.getSprite().getTransform().basis.setIdentity();
            if (!var8 || !DRAW_ALL_INFO && var2 != this.selectedEntity) {
                if (var8 && var2 == this.selectedAIEntity) {
                    this.textOverlay.getPos().set(var1);
                    this.textOverlay.getText().set(0, var2.toNiceString());
                    this.textOverlay.getText().set(1, Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHIPHUD_HUDINDICATOROVERLAY_7);
                }
            } else {
                this.textOverlay.getPos().set(var1);
                this.textOverlay.getText().set(0, var2.toNiceString());
                this.textOverlay.getText().set(1, (int)var7 + "m");
            }

            if (!((GameClientState)this.getState()).isInWarp() || !this.animap.containsKey(var3)) {
                this.animap.put(var3, 1);
            }

        } else {
            this.arrow.getSprite().setTint(this.tint);
            var1.add(var4);
            if (!this.animap.containsKey(var3)) {
                this.animap.put(var3, 1);
            }

            if (var9) {
                this.arrow.setSpriteSubIndex(25);
                this.arrow.getSprite().setSelectedMultiSprite(25);
            } else {
                var15 = this.animap.get(var3);
                this.arrow.setSpriteSubIndex(var15 + 8);
                this.arrow.getSprite().setSelectedMultiSprite(var15 + 8);
                if (this.updateAnim && var15 < 15) {
                    this.animap.put(var3, Math.min(15, var15 + 1));
                }
            }

            modelview.rewind();
            Controller.modelviewMatrix.store(modelview);
            float var16 = FastMath.sinFast(var5);
            var5 = FastMath.cosFast(var5);
            modelview.put(0, var5);
            modelview.put(4, -var16);
            modelview.put(8, 0.0F);
            modelview.put(1, var16);
            modelview.put(5, var5);
            modelview.put(9, 0.0F);
            modelview.put(2, 0.0F);
            modelview.put(6, 0.0F);
            modelview.put(10, 1.0F);
            modelview.put(12, Controller.modelviewMatrix.m30 + var1.x);
            modelview.put(13, Controller.modelviewMatrix.m31 + var1.y);
            modelview.put(14, Controller.modelviewMatrix.m32 + var1.z);
            modelview.rewind();
            GL11.glLoadMatrix(modelview);
            this.arrow.getSprite().setScale(var6, var6, var6);
            Sprite.doDraw(this.arrow.getSprite());
            this.arrow.getPos().set((float)((int)var1.x), (float)((int)var1.y), (float)((int)var1.z));
            HudIndicatorOverlay.IndicationText var10;
            if (var8 && (DRAW_ALL_INFO || var2 == this.selectedEntity)) {
                if (this.pool.isEmpty()) {
                    var10 = new HudIndicatorOverlay.IndicationText(var2.toNiceString(), (int)var7 + "m", var1);
                } else {
                    (var10 = (HudIndicatorOverlay.IndicationText)this.pool.dequeue()).s0 = var2.toNiceString();
                    var10.s1 = (int)var7 + "m";
                    var10.v.set(var1);
                    if (this.selectedEntity.getSectorId() != ((GameClientState)this.getState()).getPlayer().getCurrentSectorId() && this.selectedEntity.getWorldTransform().origin.equals(new Vector3f(0.0F, 0.0F, 0.0F))) {
                        var10.v.add(new Vector3f(0.0F, 25.0F, 0.0F));
                    }
                }

                this.texts.enqueue(var10);
            } else if (var8 && var2 == this.selectedAIEntity) {
                if (this.pool.isEmpty()) {
                    var10 = new HudIndicatorOverlay.IndicationText(var2.toNiceString(), (int)var7 + "m", var1);
                } else {
                    (var10 = (HudIndicatorOverlay.IndicationText)this.pool.dequeue()).s0 = var2.toNiceString();
                    var10.s1 = Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHIPHUD_HUDINDICATOROVERLAY_6;
                    var10.v.set(var1);
                    if (this.selectedAIEntity.getSectorId() != ((GameClientState)this.getState()).getPlayer().getCurrentSectorId() && this.selectedAIEntity.getWorldTransform().origin.equals(new Vector3f(0.0F, 0.0F, 0.0F))) {
                        var10.v.add(new Vector3f(0.0F, 25.0F, 0.0F));
                    }
                }

                this.texts.enqueue(var10);
            }

            if (var8 && var2 instanceof SegmentController && ((SegmentController)var2).getCoreTimerStarted() > 0L) {
                long var17 = ((SegmentController)var2).getCoreTimerDuration() - (System.currentTimeMillis() - ((SegmentController)var2).getCoreTimerStarted());
                String var12 = Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHIPHUD_HUDINDICATOROVERLAY_8;
                this.texts.enqueue(new HudIndicatorOverlay.IndicationText(StringTools.format(Lng.ORG_SCHEMA_GAME_CLIENT_VIEW_GUI_SHIPHUD_HUDINDICATOROVERLAY_9, new Object[]{var17 / 1000L, var12}), "", var1));
            }

        }
    }

    public void drawFor(Transform var1, String var2, int var3, boolean var4, boolean var5) {
        if (Controller.getCamera() != null) {
            this.dir.set(var1.origin);
            this.dir.sub(Controller.getCamera().getPos());
            float var6 = this.dir.length();
            Vector3f var7 = this.worldToScreenConverter.getMiddleOfScreen(new Vector3f());
            Vector3f var8 = this.worldToScreenConverter.convert(var1.origin, new Vector3f(), true);
            (var8 = new Vector3f(var8)).sub(var7);
            this.indicator.getSprite().setTint(this.tint);
            this.targetOverlay.getSprite().setTint(this.tint);
            this.arrow.getSprite().setTint(this.tint);
            if (var4 && var8.length() > (float)this.screenCap) {
                var8.normalize();
                var8.scale((float)this.screenCap);
                var8.add(var7);
                this.arrow.setSpriteSubIndex(0);
                this.arrow.getPos().set((float)((int)var8.x), (float)((int)var8.y), (float)((int)var8.z));
                Vector3f var11;
                (var11 = new Vector3f(this.arrow.getPos())).sub(var7);
                var11.scale(-1.0F);
                var11.normalize();
                float var9 = Vector3fTools.getFullRange2DAngleFast(new Vector3f(0.0F, 1.0F, 0.0F), var11);
                this.arrow.setRot(0.0F, 0.0F, 57.295776F * var9);
                this.arrow.draw();
                if (!((GameClientState)this.getState()).isInWarp() || !this.animap.containsKey(var3)) {
                    this.animap.put(var3, 1);
                }

            } else {
                this.arrow.setRot(0.0F, 0.0F, 0.0F);
                var8.add(var7);
                if (!this.animap.containsKey(var3)) {
                    this.animap.put(var3, 1);
                }

                int var10 = this.animap.get(var3);
                if (GuiDrawer.isNewHud()) {
                    this.arrow.setSpriteSubIndex(var10 + 8);
                    this.arrow.getSprite().setSelectedMultiSprite(var10 + 8);
                } else {
                    this.arrow.setSpriteSubIndex(var10);
                    this.arrow.getSprite().setSelectedMultiSprite(var10);
                }

                if (this.updateAnim) {
                    this.animap.put(var3, Math.min(15, var10 + 1));
                }

                this.arrow.getPos().set((float)((int)var8.x), (float)((int)var8.y), (float)((int)var8.z));
                this.arrow.draw();
                this.textOverlay.getPos().set((float)((int)var8.x), (float)((int)var8.y), (float)((int)var8.z));
                this.textOverlay.getText().set(0, var2);
                if (var5) {
                    if (var6 > 1000.0F) {
                        this.textOverlay.getText().set(1, StringTools.formatPointZero(var6 / 1000.0F) + "km");
                    } else {
                        this.textOverlay.getText().set(1, (int)var6 + "m");
                    }
                } else {
                    this.textOverlay.getText().set(1, "");
                }

                this.textOverlay.draw();
            }
        }
    }

    public void drawMapIndications() {
        if (((GameClientState)this.getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
            GameMapDrawer var1 = ((GameClientState)this.getState()).getWorldDrawer().getGameMapDrawer();

            for(int var2 = 0; var2 < toDrawMapTexts.size(); ++var2) {
                this.drawString((Indication)toDrawMapTexts.get(var2), var1.getCamera(), false, 4000.0F, var1.getWorldToScreenConverter());
            }

            toDrawMapTexts.clear();
            Iterator var7 = toDrawMapInterfaces.iterator();

            while(var7.hasNext()) {
                GameMap var3 = (GameMap)var7.next();
                Vector3i var4 = var1.getGameMapPosition().get(new Vector3i());

                for(int var5 = 0; var5 < var3.getEntries().length; ++var5) {
                    MapEntryInterface var6;
                    if ((var6 = var3.getEntries()[var5]).isDrawIndication() && var6.canDraw() && var1.getGameMapPosition().getCurrentSysPos().equals(var3.getPos()) && var6.include(var1.getFilter(), var4)) {
                        this.drawString(var6.getIndication(var3.getPos()), var1.getCamera(), false, 4000.0F, var1.getWorldToScreenConverter());
                    }
                }
            }

            Vector3f var8 = new Vector3f(0.0F, -20.0F, 0.0F);
            Iterator var9 = toDrawStars.iterator();

            while(var9.hasNext()) {
                StarPosition var11 = (StarPosition)var9.next();
                this.drawString(var11.getIndication(((GameClientState)this.getState()).getCurrentGalaxy()), var1.getCamera(), false, 4000.0F, var1.getWorldToScreenConverter(), var8);
            }

            toDrawStars.clear();
            Vector3i var10 = new Vector3i();
            Iterator var12 = toDrawFleet.iterator();

            while(var12.hasNext()) {
                FleetMemberMapIndication var13;
                if ((var13 = (FleetMemberMapIndication)var12.next()).isDrawIndication() && var13.canDraw()) {
                    this.drawString(var13.getIndication(var10), var1.getCamera(), false, 4000.0F, var1.getWorldToScreenConverter());
                }
            }

            toDrawFleet.clear();
            var12 = toDrawTradeNodes.iterator();

            while(var12.hasNext()) {
                TradeNodeMapIndication var14;
                if ((var14 = (TradeNodeMapIndication)var12.next()).isDrawIndication() && var14.canDraw()) {
                    this.drawString(var14.getIndication(var10), var1.getCamera(), false, 4000.0F, var1.getWorldToScreenConverter());
                }
            }

            toDrawTradeNodes.clear();
        }

    }

    public void drawString(Indication var1, Camera var2, boolean var3, float var4, WorldToScreenConverter var5) {
        this.drawString(var1, var2, var3, var4, var5, this.defaultTextOnScrenIndication);
    }

    public void drawString(Indication var1, Camera var2, boolean var3, float var4, WorldToScreenConverter var5, Vector3f var6) {
        GameClientState var7 = (GameClientState)this.getState();
        this.dir.set(var1.getCurrentTransform().origin);
        if (var3 && var7.getCurrentPlayerObject() != null && !(var7.getCurrentPlayerObject() instanceof SegmentController)) {
            this.dir.sub(var7.getCurrentPlayerObject().getWorldTransform().origin);
        } else {
            this.dir.sub(var2.getPos());
        }

        if (this.dir.length() <= var4) {
            Vector3f var9 = var5.getMiddleOfScreen(new Vector3f());
            Vector3f var8 = var5.convert(var1.getCurrentTransform().origin, new Vector3f(), true, var2);
            (var8 = new Vector3f(var8)).sub(var9);
            this.targetOverlay.getSprite().setTint(this.tint);
            var8.add(var9);
            var8.add(var6);
            this.textOverlay.getPos().set((float)((int)var8.x), (float)((int)var8.y), (float)((int)var8.z));
            this.textOverlay.getText().set(0, var1.getText());
            this.textOverlay.getText().set(1, "");
            this.textOverlay.setScale(var1.scaleIndication(), var1.scaleIndication(), var1.scaleIndication());
            if (var1.getText() instanceof ColoredInterface) {
                this.textOverlay.setColor(new Vector4f(((ColoredInterface)var1.getText()).getColor()));
            }

            this.textOverlay.draw();
            this.textOverlay.getColor().r = 1.0F;
            this.textOverlay.getColor().g = 1.0F;
            this.textOverlay.getColor().b = 1.0F;
            this.textOverlay.getColor().a = 1.0F;
            this.textOverlay.setScale(1.0F, 1.0F, 1.0F);
        }
    }

    public ShipExternalFlightController getExternalShipMan() {
        return ((GameClientState)this.getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController();
    }

    public void onSectorChange() {
        GameClientState var1 = (GameClientState)this.getState();
        IntIterator var2 = this.animap.keySet().iterator();
        IntArrayList var3 = new IntArrayList();

        while(var2.hasNext()) {
            int var4 = (Integer)var2.next();
            if (!var1.getCurrentSectorEntities().containsKey(var4)) {
                var3.add(var4);
            }
        }

        Iterator var6 = var3.iterator();

        while(var6.hasNext()) {
            int var5 = (Integer)var6.next();
            this.animap.remove(var5);
        }

        this.calcNeighborSectors(var1.getPlayer().getCurrentSector());
    }

    public void update(Timer var1) {
        this.currentDelta = var1.getDelta();
        selectColorValue = 0.5F + this.timerUtil.getTime() * 0.5F;
        this.screenCap = (int)Math.min((float)GLFrame.getHeight() / 2.4F, (float)GLFrame.getWidth() / 2.4F);
        this.timerUtil.update(var1);

        try {
            for(int var2 = 0; var2 < toDrawTexts.size(); ++var2) {
                ((Indication)toDrawTexts.get(var2)).update(var1);
                if (!((Indication)toDrawTexts.get(var2)).isAlive()) {
                    toDrawTexts.remove(var2);
                    --var2;
                }
            }

        } catch (IndexOutOfBoundsException var3) {
        }
    }

    class IndicationText {
        public final Vector3f v = new Vector3f();
        public String s0;
        public String s1;

        public IndicationText(String var2, String var3, Vector3f var4) {
            this.s0 = var2;
            this.s1 = var3;
            this.v.set(var4);
        }
    }
}
