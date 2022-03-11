package me.iron.WarpSpace.Mod.visuals;

import api.listener.Listener;
import api.listener.events.draw.RegisterWorldDrawersEvent;
import api.utils.draw.ModWorldDrawer;
import api.utils.textures.StarLoaderTexture;
import com.bulletphysics.linearmath.Transform;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import org.lwjgl.opengl.GL11;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.resource.MeshLoader;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3f;

import java.util.Set;

import static api.common.GameClient.getClientPlayerState;
import static api.mod.StarLoader.registerListener;
import static me.iron.WarpSpace.Mod.WarpMain.*;
import static org.lwjgl.opengl.GL11.*;
import static org.schema.schine.graphicsengine.core.Controller.getCamera;

/**
 * STARMADE MOD
 * CREATOR: Ithirahad Ivrar'kiim
 * DATE: 21.10.2021
 * TIME: 10:15
 *
 * Draws a large inside-out sphere with a special shader while in warpspace.
 */

public class WarpSkybox extends ModWorldDrawer implements Shaderable {
    public static Shader shader;
    private static Mesh mesh; //sphere mesh
    private static int bubblePlaceholderTexture;
    private static final float SCALE_FACTOR = 0.35f; //scale factor of skybox sphere = this value x sector size, also used for shader coord compensation
    static float time = 0;
    private static final Vector3f vel = new Vector3f();

    public static void registerForRegistration() { //it's like bureaucracy, but Java. I don't know if that's better or worse.
        registerListener(RegisterWorldDrawersEvent.class, new Listener<RegisterWorldDrawersEvent>() {
            @Override
            public void onEvent(RegisterWorldDrawersEvent ev) {
                ev.getModDrawables().add(new WarpSkybox());
            }
        }, instance);
    }

    public static void loadResources(MeshLoader mloader, WarpMain mod) {
        try {
            mloader.loadModMesh(mod, "planet_sphere", mod.getJarResource("me/iron/WarpSpace/Mod/res/planet_sphere.zip"), null);
            System.err.println("[MOD][WarpSpace] Successfully loaded sphere model");
            bubblePlaceholderTexture = StarLoaderTexture.newSprite(ImageIO.read(mod.getJarResource("me/iron/WarpSpace/Mod/res/warp_sky_placeholder_tex.png")), mod, "Warp Skybox").getMaterial().getTexture().getTextureId();
            shader = Shader.newModShader(mod.getSkeleton(), "WarpShader",
                    mod.getJarResource("me/iron/WarpSpace/Mod/res/warp.vert"),
                    mod.getJarResource("me/iron/WarpSpace/Mod/res/warp.frag"));
        } catch(Exception ex){
            System.err.println("[MOD][WarpSpace][ERROR] Failed to load skybox draw resources!");
            ex.printStackTrace();
        }
    }

    @Override
    public void onInit() {
        mesh = (Mesh) Controller.getResLoader().getMeshLoader().getModMesh(instance, "planet_sphere").getChilds().iterator().next();
        //This is the way that the base game makes the tube shader; trying this first to see what happens
    }

    @Override
    public void update(Timer timer) {
        time += timer.getDelta()*10.1F;
    } //for shader

    @Override
    public void updateShader(DrawableScene drawableScene) {
        GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0); //??? Shader does not use textures but maybe this is needed for some reason
    }

    @Override
    public void updateShaderParameters(Shader shader) {
        GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, bubblePlaceholderTexture); //derpface; shouldn't matter though
        GlUtil.updateShaderFloat(shader, "timeBasis", time); //speed of effect 'animation'
        if(GameClientState.instance != null){
            ManagedUsableSegmentController<?> vessel = currentlyOnBoardEntity();
            if(vessel != null && vessel.getPhysicsObject() != null) {
                //synchronized (vessel) { //useless? produced nullpointer, guessed it might be non threadsafe cause
                GlUtil.updateShaderVector3f(shader, "flightVel", vessel.getPhysicsObject().getLinearVelocity(vel));
                GlUtil.updateShaderFloat(shader, "maxSpeed", vessel.getMaxServerSpeed());
                GlUtil.updateShaderVector3f(shader,"vesselOrigin",vessel.getClientTransform().origin);
                //GlUtil.updateShaderFloat(shader, "warpDepth", someWeirdWSStateInformationIDK);
                //}
            }
        }
        //TODO: Darken/lighten over time when entering/leaving
    }

    @Override
    public void preCameraPrepare() { //Basically background draw; everything else except skybox draws on top of this stuff
        if (!WarpManager.IsInWarp(getClientPlayerState().getCurrentSector())) return;
        float sectorSize = GameClientState.instance.getSectorSize();
        float scale = SCALE_FACTOR * sectorSize;
        scale *= -0.35f;
        scale *= 1.5f; //idk

        Transform tr = new Transform();
        tr.setIdentity();

        Vector3f drawPosition = getCamera().getOffsetPos(new Vector3f()); //TODO: This should be the draw position

        GlUtil.glEnable(GL_DEPTH_TEST); //??????????????????
        GlUtil.glDepthMask(true); //???????

        Vector3f modelScale = new Vector3f(scale,scale,scale);

        shader.setShaderInterface(this);
        shader.load();

        tr.origin.set(drawPosition);
        mesh.loadVBO(true);

        GlUtil.glPushMatrix();
        GlUtil.glMultMatrix(tr);

        GlUtil.scaleModelview(modelScale.x, modelScale.y, modelScale.z); //lol, why is there no scaleModelview(vector3f)

        mesh.renderVBO();
        GlUtil.glPopMatrix();
        mesh.unloadVBO(true);
        shader.unload();
    }

    @Override
    public void cleanUp() {
        //mesh.cleanUp();
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public void onExit() {

    }

    public static ManagedUsableSegmentController<?> currentlyOnBoardEntity() {
        PlayerState player = getClientPlayerState();
        Set<ControllerStateUnit> units = player.getControllerState().getUnits();
        if (units.isEmpty()) {
            //TODO: look for what they're on board of too
            GameClientState.instance.getCurrentGravitySources();

            return null;
        } else {
            ControllerStateUnit unit = units.iterator().next();
            if(unit.playerControllable instanceof ManagedUsableSegmentController)
            return (ManagedUsableSegmentController<?>) unit.playerControllable;
            else return null;
        }
    }
}
