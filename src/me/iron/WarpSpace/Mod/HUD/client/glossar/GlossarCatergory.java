package me.iron.WarpSpace.Mod.HUD.client.glossar;

import java.util.ArrayList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 06.12.2021
 * TIME: 18:04
 */
public class GlossarCatergory {
    private String name;
    private ArrayList<GlossarEntry> entries = new ArrayList<>();

    public GlossarCatergory(String topicName) {
        this.name = topicName;
    }

    public void addEntry(GlossarEntry e) {
        entries.add(e);
    }

    public void removeEntry(GlossarEntry e) {
        entries.remove(e);
    }

    public void clearEntries() {
        entries.clear();
    }

    public ArrayList<GlossarEntry> getEntries() {
        return entries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
