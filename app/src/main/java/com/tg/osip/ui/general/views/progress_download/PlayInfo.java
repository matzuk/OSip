package com.tg.osip.ui.general.views.progress_download;

/**
 * @author e.matsyuk
 */
public class PlayInfo {

    private int id;
    private String path;
    private String tgPath;

    public PlayInfo(int id, String path, String tgPath) {
        this.path = path;
        this.id = id;
        this.tgPath = tgPath;
    }

    public String getPath() {
        return path;
    }

    public int getId() {
        return id;
    }

    public String getTgPath() {
        return tgPath;
    }

    @Override
    public String toString() {
        return "PlayInfo{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", tgPath='" + tgPath + '\'' +
                '}';
    }
}
