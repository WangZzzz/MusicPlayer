package com.musicplayer.model;

/**
 * Created by WangZ on 2015/7/30.
 */
public class ScanFile {
    private String filePath;
    private String fileName;
    private boolean isChosen;
    private boolean isDirectory;

    public ScanFile() {
    }

    public ScanFile(String filePath, String fileName, boolean isChosen, boolean isDirectory) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.isChosen = isChosen;
        this.isDirectory = isDirectory;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean isChosen) {
        this.isChosen = isChosen;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }
}
