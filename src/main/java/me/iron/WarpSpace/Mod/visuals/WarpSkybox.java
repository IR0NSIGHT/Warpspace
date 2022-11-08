package me.iron.WarpSpace.Mod.visuals;

import static api.common.GameClient.getClientPlayerState;
import static api.mod.StarLoader.registerListener;
import static me.iron.WarpSpace.Mod.WarpMain.instance;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.schema.schine.graphicsengine.core.Controller.getCamera;

import java.lang.reflect.Field;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.physics.KinematicCharacterControllerExt;
import org.schema.game.common.data.physics.PairCachingGhostObjectExt;
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

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.linearmath.Transform;

import api.listener.Listener;
import api.listener.events.draw.RegisterWorldDrawersEvent;
import api.utils.draw.ModWorldDrawer;
import api.utils.textures.StarLoaderTexture;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.client.WarpProcess;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;

/**
 * STARMADE MOD
 * CREATOR: Ithirahad Ivrar'kiim
 * DATE: 21.10.2021
 * TIME: 10:15
 * <br/><br/>
 * Draws a large inside-out sphere with a special shader while in warpspace.
 */

public class WarpSkybox extends ModWorldDrawer implements Shaderable {
    public static Shader shader;
    private static Mesh mesh; //sphere mesh
    private static int bubblePlaceholderTexture;
    private static final float EPSILON = 0.0000000001f;
    private static final float SCALE_FACTOR = 0.35f; //scale factor of skybox sphere = this value x sector size, also used for shader coord compensation
    private static final Vector3f zeroVel = new Vector3f();
    private static boolean skyboxIsActive = false;
    static float smoothedWarpStability = 0;

    /**
     * Time elapsed in warp. Resets when not warping.
     */
    static float time = 0;

    /**
     * This version of time increments slower when flying slower.
     * This allows noise to move by faster/slower based on flight velocity
     * without MULTIPLYING by flight speed inside the shader - as that method causes problems with acceleration/deceleration
     * and would create more and more seizure-inducing speed transitions as time goes on
     */
    static float distortedTime = 0;

    private static final Vector3f vel = new Vector3f();

    public static void registerForRegistration() { //it's like bureaucracy, but Java. I don't know if that's better or worse.
        registerListener(RegisterWorldDrawersEvent.class, new Listener<RegisterWorldDrawersEvent>() {
            @Override
            public void onEvent(RegisterWorldDrawersEvent ev) {
                ev.getModDrawables().add(new WarpSkybox());
            }
        }, instance);
    }
    private static String resourcePath = "resources/shader/";
    public static void loadResources(MeshLoader mloader, WarpMain mod) {
        try {
            mloader.loadModMesh(mod, "planet_sphere", mod.getJarResource( resourcePath+"planet_sphere.zip"), null);
            System.err.println("[MOD][WarpSpace] Successfully loaded sphere model");
            bubblePlaceholderTexture = StarLoaderTexture.newSprite(ImageIO.read(mod.getJarResource(resourcePath+"warp_sky_placeholder_tex.png")), mod, "Warp Skybox").getMaterial().getTexture().getTextureId();
            shader = Shader.newModShader(mod.getSkeleton(), "WarpShader",
                    mod.getJarResource(resourcePath+"warp.vert"),
                    mod.getJarResource(resourcePath+"warp.frag"));
        } catch (Exception ex) {
            System.err.println("[MOD][WarpSpace][ERROR] Failed to load skybox draw resources!");
        }
    }

    @Override
    public void onInit() {
        mesh = (Mesh) Controller.getResLoader().getMeshLoader().getModMesh(instance, "planet_sphere").getChilds().iterator().next();
    }

    @Override
    public void update(Timer timer) {
        skyboxIsActive = Math.abs(smoothedWarpStability) > EPSILON;

        float maxChangePerFrame = 1/(timer.getFps()*40f); //x frames to go from 0 to 1, in theory. apparently it's fuzzier than that though idk
        ManagedUsableSegmentController<?> vessel = null;
        boolean inWarp = WarpProcess.IS_IN_WARP.isTrue();

        if (!inWarp) {
            smoothedWarpStability = Math.max(0, smoothedWarpStability - (maxChangePerFrame * 10)); //quick transition to zero warp depth visual before disabling entirely (fallback smoothing)
        } else if (GameClientState.instance != null) {
            vessel = currentlyOnBoardEntity();
            //smoothly transition to desired warpdepth
            float trueDepth = (ConfigManager.ConfigEntry.killswitch_speedDrop.isTrue() ?
                    1 :
                    WarpProcess.WARP_STABILITY.getCurrentValue() / 100f);

            if (smoothedWarpStability < trueDepth) {
                smoothedWarpStability = Math.min(trueDepth, smoothedWarpStability + maxChangePerFrame);
            } else if (smoothedWarpStability > trueDepth) {
                smoothedWarpStability = Math.max(trueDepth, smoothedWarpStability - maxChangePerFrame);
            }
        }

        if (!skyboxIsActive) {
            time = 0;
            distortedTime = 0;
        } else {
            time += timer.getDelta() * 5.05F;
            if (vessel != null)
                distortedTime += timer.getDelta() * 5.05F * (vessel.getSpeedCurrent() / vessel.getMaxServerSpeed());
            else distortedTime += timer.getDelta(); //very temporary case
        }
    } //for shader

    @Override
    public void updateShader(DrawableScene drawableScene) {
        GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0); //??? Shader does not use textures but maybe this is needed for some reason
    }

    @Override
    public void updateShaderParameters(Shader shader) {
        GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, bubblePlaceholderTexture);
        GlUtil.updateShaderFloat(shader, "timeBasis", time); //speed of effect 'animation'
        if (GameClientState.instance != null) {
            ManagedUsableSegmentController<?> vessel = currentlyOnBoardEntity();
            if (vessel != null && vessel.getPhysicsObject() != null) {
                CollisionObject cob = vessel.getDockingController().getAbsoluteMother().getPhysicsDataContainer().getObject(); //I'll refrain from making any corny puns here

                GlUtil.updateShaderVector3f(shader, "flightVel", cob != null ? ((RigidBody) cob).getLinearVelocity(vel) : zeroVel);
                GlUtil.updateShaderFloat(shader, "maxSpeed", vessel.getMaxServerSpeed());
                GlUtil.updateShaderVector3f(shader, "vesselOrigin", vessel.getClientTransform().origin);
                GlUtil.updateShaderFloat(shader, "distortedTime", distortedTime);
                GlUtil.updateShaderFloat(shader, "warpDepth", smoothedWarpStability);
                //}
            }
        }
    }

    @Override
    public void preCameraPrepare() { //Basically background draw; everything else except skybox draws on top of this stuff
        if (!skyboxIsActive) return;
        float sectorSize = GameClientState.instance.getSectorSize();
        float scale = SCALE_FACTOR * sectorSize;
        scale *= -0.35f;
        scale *= 1.5f; //idk

        Transform tr = new Transform();
        tr.setIdentity();

        Vector3f drawPosition = getCamera().getOffsetPos(new Vector3f()); //TODO: This should be the draw position

        GlUtil.glEnable(GL_DEPTH_TEST);
        GlUtil.glDepthMask(true);

        Vector3f modelScale = new Vector3f(scale, scale, scale);

        if (ConfigManager.ConfigEntry.vfx_use_warp_shader.isTrue()) {
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
        if (units.isEmpty()) return null;
        else {
            ControllerStateUnit unit = units.iterator().next();
            if (unit.playerControllable instanceof ManagedUsableSegmentController)
                return (ManagedUsableSegmentController<?>) unit.playerControllable;

            else {
                try {
                    KinematicCharacterControllerExt characterController = GameClientState.instance.getPlayer().getAssingedPlayerCharacter().getCharacterController();
                    PairCachingGhostObject spook = (PairCachingGhostObject) readPrivateField(KinematicCharacterController.class, "ghostObject", characterController);
                    if (spook instanceof PairCachingGhostObjectExt) { //covers null condition too
                        SegmentController attached = (SegmentController) (readPrivateField(PairCachingGhostObjectExt.class, "attached", spook));
                        if (attached != null) return (ManagedUsableSegmentController<?>) attached;
                    }
                } //TODO: This is cursed. Is there really not any better way to get the segmentcontroller a player is attached to?
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }
    }

    public static Object readPrivateField(Class cls, String fieldName, Object targetInstance) { //lifted from RRS
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object rtn = field.get(targetInstance);
            field.setAccessible(false);
            return rtn;
        } catch (IllegalAccessException e) {
            System.err.println("[MOD][Resources ReSourced] ERROR: Could not access target field \"" + targetInstance.getClass().getName() + "." + fieldName + "\".");
            e.printStackTrace();
            return null;
        } catch (NoSuchFieldException e) {
            System.err.println("[MOD][Resources ReSourced] ERROR: Provided field name \"" + targetInstance.getClass().getName() + "." + fieldName + "\" does not correspond to any extant field in the target's superclasses.");
            e.printStackTrace();
            return null;
        }
    }
}
