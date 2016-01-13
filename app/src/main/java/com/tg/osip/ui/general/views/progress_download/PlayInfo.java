package com.tg.osip.ui.general.views.progress_download;

/**
 * @author e.matsyuk
 */
public class PlayInfo {

    private int id;
    private String path;

    public PlayInfo(int id, String path) {
        this.path = path;
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "PlayInfo{" +
                "id=" + id +
                ", path='" + path + '\'' +
                '}';
    }

}
