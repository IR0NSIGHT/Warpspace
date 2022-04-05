package me.iron.WarpSpace.Mod.client.sounds;

import api.utils.StarRunnable;
import api.utils.sound.AudioUtils;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.client.WarpProcess;
import org.apache.commons.io.IOUtils;
import org.schema.schine.graphicsengine.core.Controller;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 18.12.2021
 * TIME: 19:28
 */
public class WarpSounds {
    public static WarpSounds instance;
    private HashMap<String, SoundQueue> uid_to_queues = new HashMap<>();
    public static void main(String[] args) {
        new WarpSounds();
    }
    public WarpSounds() {
        instance = this;
        installSounds();

        String path = WarpMain.instance.getSkeleton().getResourcesFolder().getPath().replace("\\","/")+"/resources/sounds/"; //in moddata

          File f;
          for (int i = 0; i< SoundEntry.values().length; i++) {
              f = new File(path + SoundEntry.values()[i].getSoundName()+".wav");
              if (f.exists()) {
                  addSound(SoundEntry.values()[i].getSoundName(), f);
                  SoundEntry soundEntry = SoundEntry.values()[i];
                  try {
                      AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                      AudioFormat format = ais.getFormat();
                      long frames = ais.getFrameLength();
                      double durationInSeconds = (frames+0.0)/format.getFrameRate();
                      soundEntry.setDuration((long)(durationInSeconds*1000));
                  } catch (UnsupportedAudioFileException | IOException e) {
                      e.printStackTrace();
                  }
              } else {
                  new FileNotFoundException("warp sounds file " + SoundEntry.values()[i].getSoundName()).printStackTrace();
              }
          }
        initLoop();
        initEventSounds();
    }

    /**
     * will add sounds and install soundfiles to the client if they dont already exist.
     */
    private void installSounds() {
        String folderPath = WarpMain.instance.getSkeleton().getResourcesFolder().getPath().replace("\\","/")+"/resources/sounds/"; //in moddata
        File dir = new File(folderPath);
        if (!dir.exists())
            dir.mkdirs();

        File file;
        String name;
        String path;
        for (int i = 0; i< SoundEntry.values().length; i++) {
            name = SoundEntry.values()[i].getSoundName();
            path = folderPath + name +".wav";
            //System.out.println("trying to load sound '"+name+"' at :"+path);
            file = new File(".",path);
            if (!file.exists()) {
                try {
                    //install sound files to client
                    String jarPath = "me/iron/WarpSpace/Mod/res/sounds/" + SoundEntry.values()[i].getSoundName()+".wav";

                    InputStream source = WarpMain.instance.getSkeleton().getClassLoader().getResourceAsStream(jarPath);

                    File targetFile = new File(".",path);
                    targetFile.createNewFile();


                    FileOutputStream outStream = new FileOutputStream(targetFile);

                    IOUtils.copy(source,outStream);

                    source.close();
                    outStream.close();

                    file = new File(path);
                    if (!file.exists()) {
                        new FileNotFoundException().printStackTrace();
                    } else {
                        System.out.println("installed file at " + file.getCanonicalPath());
                    }
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            assert file.exists():"installation of warpspace soundfiles failed";
            addSound(name,file);

        }
    }

    /**
     *
     * @param name name of sound
     * @param file
     */
    private void addSound(String name, File file) {
        Controller.getAudioManager().addSound(name,file);
    }

    public void initEventSounds() {
        VoiceAnnouncer vc = new VoiceAnnouncer();


        EngineSounds es = new EngineSounds();
        WarpProcess.JUMPENTRY.addListener(es);
        WarpProcess.JUMPEXIT.addListener(es);
        WarpProcess.HAS_JUMPED.addListener(es);
        WarpProcess.JUMPDROP.addListener(es);
    }

    public void queueSound(SoundEntry e, String queueID) {
        queueSound(new SoundInstance(e),queueID);
    }

    public void queueSound(SoundInstance s, String queueID) {
        SoundQueue queue;
        if (!uid_to_queues.containsKey(queueID)) {
            queue = new SoundQueue(queueID,false,true);
            uid_to_queues.put(queueID,queue);
        } else {
            queue = uid_to_queues.get(queueID);
        }
        queue.add(s);
    }

    public void playSound(SoundEntry s) {
        String name = s.soundName;
        AudioUtils.clientPlaySound(name, dbToVolume(s.standardVolume),1);
    }

    /**
     * @param s
     * @param volume Dezibel increase, use negative for decrease.
     * @param pitch pitch from 0 to 1
     */
    public void playSound(SoundEntry s, float volume, float pitch) {
        AudioUtils.clientPlaySound(s.soundName, volume, pitch);
    }

    public void playSound(SoundInstance sound) {
        if (sound.getVolume() == 0)
            return;
        playSound(sound.soundEntry, sound.volume, sound.pitch);
    }

    public static float dbToVolume(float db) {
        return (float)Math.pow(10,db/10);
    }

    private void initLoop() {
        new StarRunnable(){
            @Override
            public void run() {
                //update all soundqueues
                if (!uid_to_queues.isEmpty()) {
                    for (SoundQueue q: uid_to_queues.values()) {
                        q.update(System.currentTimeMillis());
                    }
                }
            }
        }.runTimer(WarpMain.instance,1);
    }
    public enum SoundEntry {
        //voices
        voice_activate("voice_activate",1f),
        voice_activated("voice_activated",1f),
        voice_active("voice_active",1f),
        voice_beacon("voice_beacon",1f),
        voice_critical("voice_critical",1f),
        voice_deactivate("voice_deactivate",1f),
        voice_deactivated("voice_deactivated",1f),
        voice_destination("voice_destination",1f),
        voice_detected("voice_detected",1f),
        voice_disengage("voice_disengage",1f),
        voice_disengaged("voice_disengaged",1f),
        voice_drive("voice_drive",1f),
        voice_droppoint("voice_drop-point",1f),
        voice_engage("voice_engage",1f),
        voice_engaged("voice_engaged",1f),
        voice_high("voice_high",1f),
        voice_inactive("voice_inactive",1f),
        voice_inhibitor("voice_inhibitor",1f),
        voice_interdictor("voice_interdictor",1f),
        voice_low("voice_low",1f),
        voice_nav("voice_nav",1f),
        voice_reached("voice_reached",1f),
        voice_signature("voice_signature",1f),
        voice_stability("voice_stability",1f),
        voice_target("voice_target",1f),
        voice_warp("voice_warp",1f),
        voice_warpdrive("voice_warpdrive",1f),

        //SFX / Effects
        drive_charge_up("drive_charge_up",1f),
        warning_beep("warning_beep",1f),
        warp_boom("warp_boom",1f),
        warpambient("warpambient",1f);

        SoundEntry(String path, float standardVolume) {
            this.soundName = path;
            this.standardVolume = standardVolume;
        }
        private final String soundName;
        private float standardVolume = 1;
        private long duration;

        /**
         * in millis
         * @return
         */
        public long getDuration() {
            return duration;
        }

        public void setDuration(long millis) {
            this.duration = millis;
        }

        public String getSoundName() {
            return soundName;
        }

        @Override
        public String toString() {
            return "Sound{" +
                    "enum='"+this.name() + '\'' +
                    ", soundName='" + soundName + '\'' +
                    ", duration=" + duration +
                    '}';
        }
    }

    static class SoundInstance {
        SoundEntry soundEntry;
        float pitch;
        private float volume;

        public SoundInstance(SoundEntry e, float db, float pitch) {
            this.soundEntry = e;
            this.pitch = pitch;
            this.setVolume(dbToVolume(soundEntry.standardVolume+db));
        }

        public SoundInstance(SoundEntry soundEntry) {
            this.soundEntry = soundEntry;
            pitch = 1;
            volume = dbToVolume(soundEntry.standardVolume);
        }

        public float getVolume() {
            return volume;
        }
        public void setVolume(float v) {
            volume = v;
        }
    }

    //queue that will play one sound after the other, starting when the sound has ended. allows control like pause, stop, clear, add
    static class SoundQueue {
        private String id;
        private boolean loop;
        private long soundStarted;
        private boolean active;
        private LinkedList<SoundInstance> queue = new LinkedList<>();
        private SoundInstance currentlyPlaying;

        public SoundQueue(String id, boolean loop, boolean active) {
            this.id = id;
            this.loop = loop;
            this.active = active;
        }

        /**
         * update soundqueue, will auto play next in line if previous was done, or nothing is currently played.
         * @param timeMillis
         * @return
         */
        public SoundInstance update(long timeMillis) {
            boolean finishedCurrent = (currentlyPlaying == null || timeMillis>= soundStarted +currentlyPlaying.soundEntry.getDuration());
            if (!finishedCurrent)
                return currentlyPlaying;

            //did current sound finished playing?
            if (active && !queue.isEmpty()) {
                assert currentlyPlaying == null || currentlyPlaying.equals(queue.getFirst());
                if (loop)
                    queue.add(queue.getFirst()); //is loop->add to end of queue after playing
                if (currentlyPlaying != null)
                    queue.removeFirst();                 //remove song that was finished

                //are more sonds queued?
                if (!queue.isEmpty()) {
                    //play next sound
                    currentlyPlaying = queue.getFirst();
                    WarpSounds.instance.playSound(currentlyPlaying);
                    soundStarted = timeMillis; //update time
                } else {
                    currentlyPlaying = null;
                    soundStarted = -1;
                    //TODO delete flag?
                }
            }
            return currentlyPlaying;
        }

        /**
         * set queue to active: will play new sounds after last one is done
         */
        public void setActive() {
            this.active = true;
        }

        public void clear() {
            queue.clear();
        }

        /**
         * pause queue, will stop playing after current sound is done.
         */
        public void pause() {
            active = false;
        }

        /**
         * stop queue, will hard stop playing sound
         */
        public void stop() {
            pause();
            //TODO hard stop currentPlaying
        }
        public void add(SoundInstance s) {
            queue.add(s);
        }

    }
}
