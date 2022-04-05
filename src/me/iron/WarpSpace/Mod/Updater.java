package me.iron.WarpSpace.Mod;

import api.mod.config.PersistentObjectUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Updater {
    private VersionTag jarVersion;
    private boolean updateOnVersionChange = true;
    public Updater(String version) {
        String[] vArr = version.split("\\.",4);
        jarVersion = new VersionTag(Integer.parseInt(vArr[0]),
                Integer.parseInt(vArr[1]),
                Integer.parseInt(vArr[2]));
        System.out.println("WarpSpace JAR has version:"+jarVersion);
        System.out.println("WarpSpace installed has version:"+getSavedVersion());
    }

    public void runUpdate() {
        if (updateOnVersionChange && !jarVersion.equals(getSavedVersion())) {
            System.out.println("################ RUNNING UPDATE FOR WARPSPACE AFTER VERSION CHANGED FROM "+ getSavedVersion() + " TO " + jarVersion);
            executeUpdate();
            writeVersion(jarVersion);
        }
    }

    private void writeVersion(VersionTag tag) {
        PersistentObjectUtil.removeAllObjects(WarpMain.instance.getSkeleton(), VersionTag.class);
        PersistentObjectUtil.addObject(WarpMain.instance.getSkeleton(), tag);
        PersistentObjectUtil.save(WarpMain.instance.getSkeleton());
    }

    public VersionTag getSavedVersion() {
        try {
            ArrayList<Object> os = PersistentObjectUtil.getObjects(WarpMain.instance.getSkeleton(), VersionTag.class);
            if (os.isEmpty()) {
                return null;
            }
            return  (VersionTag)os.get(0);
        } catch (Exception ex) {
            return null;
        }

    }

    private void executeUpdate() {
        //wipe "resources" directory, will be recreated afterwards by other classes
        String folderPath = WarpMain.instance.getSkeleton().getResourcesFolder().getPath().replace("\\","/")+"/resources/"; //in moddata
        File resources = new File(folderPath);
        if (!resources.exists())
            return;

        deleteDirectoryRecursive(resources);
    }

    private boolean deleteDirectoryRecursive(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectoryRecursive(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    class VersionTag {
        private int[] version = new int[3];
        public VersionTag(int major, int minor, int patch) {
            version[0] = major; version[1] = minor; version[2] = patch;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VersionTag that = (VersionTag) o;
            return Arrays.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(version);
        }

        @Override
        public String toString() {
            return "VersionTag{" +
                    "version=" + version[0]+"."+version[1]+"."+version[2] +
                    '}';
        }
    }
}
