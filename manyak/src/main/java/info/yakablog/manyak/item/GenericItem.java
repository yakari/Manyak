package info.yakablog.manyak.item;

import android.graphics.Bitmap;

/**
 * Created by krnl7365 on 03/03/14.
 */
public class GenericItem {
    private long id;

    private String name;

    private String vignetteURL;

    private Bitmap vignette;

    private String description;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVignetteURL() {
        return vignetteURL;
    }

    public void setVignetteURL(String vignetteURL) {
        this.vignetteURL = vignetteURL;
    }

    public Bitmap getVignette() {
        return vignette;
    }

    public void setVignette(Bitmap vignette) {
        this.vignette = vignette;
    }
}
