package me.iron.WarpSpace.Mod.HUD.client.glossar;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 06.12.2021
 * TIME: 18:04
 */
public class GlossarEntry {
    private String title;
    private String content;
    public GlossarEntry(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
