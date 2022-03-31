//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.schema.schine.sound.pcode;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.util.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.File;
import java.util.Iterator;
import javax.sound.sampled.AudioFormat;
import javax.vecmath.Vector3f;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.physics.Physical;
import org.schema.schine.sound.AudioEntity;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJavaSound;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SoundManager {
    private static final int MAX_PLAYER_PER_HUNDRED_MILLI = 4;
    private static final long RESET_SOUND_TIME = 50L;
    private static final float DEFAULT_SOUND_RADIUS = 50.0F;
    public static float musicVolume;
    public static float soundVolume;
    private static SoundSystem sndSystem;
    private static boolean loaded;
    public static boolean errorGotten;
    private final ObjectArrayList<AudioEntity> currentEntities = new ObjectArrayList();
    private final Vector3f linVelo = new Vector3f();
    private SoundPool soundPoolSounds = new SoundPool();
    private SoundPool soundPoolStreaming = new SoundPool();
    private SoundPool soundPoolMusic = new SoundPool();
    private int latestSoundID = 0;
    private boolean recalc;
    private boolean soundVolumeChanged;
    private float bgMusicVolume = 0.18F;
    private Object2ObjectOpenHashMap<String, SoundManager.PlayedCheck> playedMap = new Object2ObjectOpenHashMap();

    public SoundManager() {
        musicVolume = ((Integer)EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10.0F;
        soundVolume = ((Integer)EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10.0F;
    }

    public void addMusic(String var1, File var2) {
        this.soundPoolMusic.addSound(var1, var2);
    }

    public void addSound(String var1, File var2) {
        this.soundPoolSounds.addSound(var1, var2);
    }

    public void addStreaming(String var1, File var2) {
        this.soundPoolStreaming.addSound(var1, var2);
    }

    public ObjectArrayList<AudioEntity> getCurrentEntities() {
        return this.currentEntities;
    }

    public float getMusicVolume() {
        return EngineSettings.S_SOUND_ENABLED.isOn() ? musicVolume : 0.0F;
    }

    public void setMusicVolume(float var1) {
        musicVolume = var1;
        this.setSoundVolumeChanged(true);
    }

    public float getSoundVolume() {
        return EngineSettings.S_SOUND_ENABLED.isOn() ? soundVolume : 0.0F;
    }

    public void setSoundVolume(float var1) {
        soundVolume = var1;
        this.setSoundVolumeChanged(true);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isRecalc() {
        return this.recalc;
    }

    public void setRecalc(boolean var1) {
        this.recalc = var1;
    }

    public void loadSoundSettings() {
        this.soundPoolStreaming.isGetRandomSound = false;
        if (!loaded && EngineSettings.S_SOUND_SYS_ENABLED.isOn()) {
            this.tryToSetLibraryAndCodecs();
        }

    }

    public void onCleanUp() {
        if (loaded) {
            System.err.println("[AUDIO] Cleaning up sound system");
            sndSystem.cleanup();
        }

    }

    public void onSoundOptionsChanged() {
        if (!loaded && (this.getSoundVolume() != 0.0F || this.getMusicVolume() != 0.0F)) {
            this.tryToSetLibraryAndCodecs();
        }

        if (loaded) {
            musicVolume = ((Integer)EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10.0F;
            if (this.getMusicVolume() == 0.0F || !EngineSettings.S_SOUND_ENABLED.isOn()) {
                System.err.println("[CLIENT][SOUND] Sound Disabled");
                sndSystem.stop("bm");
                return;
            }

            System.err.println("[CLIENT][SOUND] Sound Volume Changed to " + this.getMusicVolume() + " (current background lvl: " + this.bgMusicVolume + ")");
            sndSystem.setVolume("bm", this.bgMusicVolume * this.getMusicVolume());
        }

    }

    public void playBackgroundMusic(String var1, float var2) {
        SoundPoolEntry var3;
        if ((var3 = this.soundPoolSounds.get(var1)) != null && var2 > 0.0F) {
            sndSystem.backgroundMusic("bm", var3.soundUrl, var3.soundName, true);
            this.bgMusicVolume = var2;
            musicVolume = ((Integer)EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10.0F;
            System.err.println("BACKGROUND SOUND: " + this.bgMusicVolume * musicVolume + "; " + this.bgMusicVolume + "; " + musicVolume);
            sndSystem.setVolume("bm", this.bgMusicVolume * musicVolume);
        }

    }

    public void playSound(AudioEntity var1, String var2, float var3, float var4) {
        this.playSound(var1, var2, var3, var4, 50.0F);
    }

    public void playSound(AudioEntity var1, String var2, float var3, float var4, float var5) {
        if (loaded && this.getSoundVolume() != 0.0F) {
            SoundPoolEntry var6 = this.soundPoolSounds.get(var2);
            var3 *= this.getSoundVolume();
            if (var6 != null && var3 > 0.0F) {
                var2 = var1.getUniqueIdentifier();
                sndSystem.newSource(false, var2, var6.soundUrl, var6.soundName, true, var1.getWorldTransformOnClient().origin.x, var1.getWorldTransformOnClient().origin.y, var1.getWorldTransformOnClient().origin.z, 2, var5);
                sndSystem.setPitch(var2, var4);
                sndSystem.setVolume(var2, Math.min(1.0F, var3));
                sndSystem.play(var2);
            } else {
                System.err.println("[SOUND] WARNING: sound not found: " + var2);
            }
        }
    }

    public void playSound(String var1, float var2, float var3, float var4, float var5, float var6) {
        this.playSound(var1, var2, var3, var4, var5, var6, 50.0F);
    }

    public void playSound(String var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        if (loaded && this.getSoundVolume() != 0.0F) {
            var5 *= this.getSoundVolume();
            SoundManager.PlayedCheck var8;
            if ((var8 = (SoundManager.PlayedCheck)this.playedMap.get(var1)) != null) {
                if (var8.first < 0L) {
                    var8.first = System.currentTimeMillis();
                }

                if (System.currentTimeMillis() - var8.first > 100L) {
                    var8.first = System.currentTimeMillis();
                    var8.playedCount = 0;
                }

                if (var8.playedCount > 4) {
                    return;
                }

                var8.playedCount++;
            } else {
                (var8 = new SoundManager.PlayedCheck()).first = System.currentTimeMillis();
                var8.playedCount++;
                this.playedMap.put(var1, var8);
            }

            SoundPoolEntry var9;
            if ((var9 = this.soundPoolSounds.get(var1)) != null && var5 > 0.0F) {
                this.latestSoundID = (this.latestSoundID + 1) % 256;
                var1 = "sound_" + this.latestSoundID;
                if (sndSystem.playing(var1)) {
                    sndSystem.setLooping(var1, false);
                }

                sndSystem.newSource(false, var1, var9.soundUrl, var9.soundName, false, var2, var3, var4, 2, var7);
                sndSystem.setPitch(var1, var6);
                sndSystem.setVolume(var1, Math.min(1.0F, var5));
                sndSystem.setLooping(var1, false);
                sndSystem.play(var1);
            } else {
                System.err.println("[SOUND] WARNING: sound not found: " + var1);
            }
        }
    }

    /**
     *
     * @param soundName
     * @param volume 0..1, but not capped! choose carefully!
     * @param pitch
     */
    public void playSoundFX(String soundName, float volume, float pitch) {
        if (loaded && this.getSoundVolume() != 0.0F && volume > 0) {
            SoundPoolEntry var5 = this.soundPoolSounds.get(soundName);
            volume *= this.getSoundVolume();
            if (var5 != null) {
                this.latestSoundID = (this.latestSoundID + 1) % 256;
                String var4 = "sound_" + this.latestSoundID;
                sndSystem.newSource(false, var4, var5.soundUrl, var5.soundName, false, 0.0F, 0.0F, 0.0F, 0, 0.0F);

                volume *= 0.25F;
                sndSystem.setPitch(var4, pitch);
                sndSystem.setVolume(var4, volume);
                sndSystem.play(var4);
                System.out.println("playing sound "+var4 +" at volume "+  sndSystem.getVolume(var4) + " intended: " + volume );
            }
        }
    }

    public void playStreaming(String var1, float var2, float var3, float var4, float var5, float var6) {
        if (loaded && (this.getSoundVolume() != 0.0F || var1 == null)) {
            String var8 = "streaming";
            if (sndSystem.playing("streaming")) {
                sndSystem.stop("streaming");
            }

            if (var1 != null) {
                SoundPoolEntry var7;
                if ((var7 = this.soundPoolStreaming.get(var1)) != null && var5 > 0.0F) {
                    if (sndSystem.playing("bm")) {
                        sndSystem.stop("bm");
                    }

                    sndSystem.newStreamingSource(true, var8, var7.soundUrl, var7.soundName, false, var2, var3, var4, 2, 64.0F);
                    sndSystem.setVolume(var8, 0.5F * this.getSoundVolume());
                    sndSystem.play(var8);
                }

            }
        }
    }

    public void setListener(Transformable var1, float var2) {
        if (loaded && this.getSoundVolume() != 0.0F) {
            if (var1 != null) {
                sndSystem.setListenerPosition(var1.getWorldTransform().origin.x, var1.getWorldTransform().origin.y, var1.getWorldTransform().origin.z);
                Vector3f var5 = GlUtil.getForwardVector(new Vector3f(), var1.getWorldTransform());
                Vector3f var3 = GlUtil.getUpVector(new Vector3f(), var1.getWorldTransform());
                sndSystem.setListenerOrientation(var5.x, var5.y, var5.z, var3.x, var3.y, var3.z);
                CollisionObject var4;
                if (var1 instanceof Physical && (var4 = ((Physical)var1).getPhysicsDataContainer().getObject()) != null && var4 instanceof RigidBody) {
                    ((RigidBody)var4).getLinearVelocity(this.linVelo);
                    sndSystem.setListenerVelocity(this.linVelo.x, this.linVelo.y, this.linVelo.z);
                }

            }
        }
    }

    public void startAllEntitySounds() {
        if (loaded && this.getSoundVolume() != 0.0F) {
            Iterator var1 = this.getCurrentEntities().iterator();

            while(var1.hasNext()) {
                AudioEntity var2 = (AudioEntity)var1.next();
                this.playSound(var2, var2.getOutsideSound(), var2.getOutsideSoundVolume(), var2.getOutsideSoundPitch(), var2.getSoundRadius());
                if (var2.isOwnPlayerInside()) {
                    Controller.getAudioManager().switchSoundInside(var2, var2.getInsideSoundVolume(), var2.getInsideSoundPitch());
                }
            }

        }
    }

    public void startEntitySound(AudioEntity var1) {
        if (loaded && this.getSoundVolume() != 0.0F) {
            this.playSound(var1, var1.getOutsideSound(), var1.getOutsideSoundVolume(), var1.getOutsideSoundPitch());
            if (var1.isOwnPlayerInside()) {
                Controller.getAudioManager().switchSoundInside(var1, var1.getInsideSoundVolume(), var1.getInsideSoundPitch());
            }

        }
    }

    public void stopAllEntitySounds() {
        if (loaded && this.getSoundVolume() != 0.0F) {
            Iterator var1 = this.getCurrentEntities().iterator();

            while(var1.hasNext()) {
                AudioEntity var2 = (AudioEntity)var1.next();
                sndSystem.stop(var2.getUniqueIdentifier());
            }

        }
    }

    public void stopBackgroundMusic() {
        if (loaded && this.getSoundVolume() != 0.0F) {
            sndSystem.stop("bm");
        }
    }

    public void stopEntitySound(AudioEntity var1) {
        if (loaded && this.getSoundVolume() != 0.0F) {
            sndSystem.stop(var1.getUniqueIdentifier());
        }
    }

    public void switchSound(AudioEntity var1, String var2, float var3, float var4) {
        if (loaded && this.getSoundVolume() != 0.0F) {
            sndSystem.stop(var1.getUniqueIdentifier());
            this.playSound(var1, var2, var3, var4);
            if (!this.getCurrentEntities().contains(var1)) {
                this.getCurrentEntities().add(var1);
            }

        }
    }

    public void switchSoundInside(AudioEntity var1, float var2, float var3) {
        this.switchSound(var1, var1.getInsideSound(), var1.getInsideSoundVolume(), var1.getInsideSoundPitch());
    }

    public void switchSoundOutside(AudioEntity var1, float var2, float var3) {
        this.switchSound(var1, var1.getOutsideSound(), var1.getOutsideSoundVolume(), var1.getOutsideSoundPitch());
    }

    private void tryToSetLibraryAndCodecs() {
        try {
            float var1 = this.getSoundVolume();
            float var2 = this.getMusicVolume();
            this.setMusicVolume(0.0F);
            this.setMusicVolume(0.0F);
            if (EngineSettings.USE_OPEN_AL_SOUND.isOn()) {
                SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
            } else {
                SoundSystemConfig.addLibrary(LibraryJavaSound.class);
            }

            SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
            SoundSystemConfig.setCodec("wav", CodecWav.class);
            SoundSystemConfig.setLogger(new SoundManager.SndLog());
            sndSystem = new SoundSystem();
            this.setMusicVolume(var1);
            this.setMusicVolume(var2);
        } catch (Throwable var3) {
            var3.printStackTrace();
            System.err.println("error linking with the LibraryJavaSound plug-in");
        }

        loaded = true;
    }

    public void update(Timer var1) {
        if (loaded && (this.getSoundVolume() != 0.0F || this.isSoundVolumeChanged())) {
            Iterator var2 = this.playedMap.values().iterator();

            while(var2.hasNext()) {
                SoundManager.PlayedCheck var3 = (SoundManager.PlayedCheck)var2.next();
                if (var1.lastUpdate - var3.first > 50L) {
                    var3.playedCount = 0;
                    var3.first = -1L;
                }
            }

            AudioEntity var7;
            if (this.isSoundVolumeChanged()) {
                if (this.getSoundVolume() == 0.0F) {
                    sndSystem.setMasterVolume(0.0F);
                } else {
                    sndSystem.setMasterVolume(this.getSoundVolume());
                }

                var2 = this.getCurrentEntities().iterator();

                while(var2.hasNext()) {
                    var7 = (AudioEntity)var2.next();
                    System.err.println("[SOUND] adapting sound of " + var7.getUniqueIdentifier() + "; volume: " + this.getSoundVolume());
                    sndSystem.stop(var7.getUniqueIdentifier());
                    if (this.getSoundVolume() > 0.0F) {
                        this.playSound(var7, var7.getOutsideSound(), var7.getOutsideSoundVolume(), var7.getOutsideSoundPitch());
                        if (var7.isOwnPlayerInside()) {
                            Controller.getAudioManager().switchSoundInside(var7, var7.getInsideSoundVolume(), var7.getInsideSoundPitch());
                        }
                    }
                }

                this.onSoundOptionsChanged();
                this.setSoundVolumeChanged(false);
            }

            var2 = this.getCurrentEntities().iterator();

            while(var2.hasNext()) {
                var7 = (AudioEntity)var2.next();
                sndSystem.setPosition(var7.getUniqueIdentifier(), var7.getWorldTransformOnClient().origin.x, var7.getWorldTransformOnClient().origin.y, var7.getWorldTransformOnClient().origin.z);
                CollisionObject var5;
                if ((var5 = var7.getPhysicsDataContainer().getObject()) != null && var5 instanceof RigidBody) {
                    ((RigidBody)var5).getLinearVelocity(this.linVelo);
                    sndSystem.setVelocity(var7.getUniqueIdentifier(), this.linVelo.x, this.linVelo.y, this.linVelo.z);
                }
            }

            SoundSystemException var6;
            if ((var6 = SoundSystem.getLastException()) != null) {
                try {
                    GLFrame.processErrorDialogException(var6, (StateInterface)null);
                    throw var6;
                } catch (SoundSystemException var4) {
                    var4.printStackTrace();
                }
            }

        }
    }

    public boolean isSoundVolumeChanged() {
        return this.soundVolumeChanged;
    }

    public void setSoundVolumeChanged(boolean var1) {
        System.err.println("[SOUND] flag settings changed!");
        this.soundVolumeChanged = var1;
    }

    public void feedRaw(String var1, byte[] var2) {
        sndSystem.feedRawAudioData(var1, var2);
    }

    public void rawDataStream(AudioFormat var1, boolean var2, String var3, float var4, float var5, float var6, int var7, float var8) {
        sndSystem.rawDataStream(var1, var2, var3, var4, var5, var6, var7, var8);
    }

    public void play(String var1) {
        sndSystem.play(var1);
    }

    public void closeStream(String var1) {
        sndSystem.stop(var1);
        sndSystem.removeSource(var1);
    }

    static {
        musicVolume = ((Integer)EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10.0F;
        soundVolume = ((Integer)EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10.0F;
        loaded = false;
    }

    class PlayedCheck {
        private long first;
        private int playedCount;

        private PlayedCheck() {
            this.first = -1L;
        }
    }

    class SndLog extends SoundSystemLogger {
        private SndLog() {
        }

        public boolean errorCheck(boolean var1, String var2, String var3, int var4) {
            return super.errorCheck(var1, var2, var3, var4);
        }

        public void errorMessage(String var1, String var2, int var3) {
            super.errorMessage(var1, var2, var3);
            if ((var1 == null || !var1.contains("Unable to initialize OpenAL.  Probable cause: OpenAL not supported")) && (var2 == null || !var2.contains("Unable to initialize OpenAL.  Probable cause: OpenAL not supported"))) {
                System.err.println("[SOUND] ERROR NOT CRITICAL. LEAVING SOUND SYS ON");
            } else {
                EngineSettings.S_SOUND_SYS_ENABLED.setCurrentState(false);
                SoundManager.errorGotten = true;
                System.err.println("[SOUND] ERROR CRITICAL. TURNING SOUND SYS OFF");
            }
        }

        public void importantMessage(String var1, int var2) {
            super.importantMessage(var1, var2);
        }

        public void message(String var1, int var2) {
            super.message(var1, var2);
        }

        public void printExceptionMessage(Exception var1, int var2) {
            super.printExceptionMessage(var1, var2);
        }

        public void printStackTrace(Exception var1, int var2) {
            super.printStackTrace(var1, var2);
        }
    }
}
