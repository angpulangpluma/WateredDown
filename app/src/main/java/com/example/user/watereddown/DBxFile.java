package com.example.user.watereddown;

/**
 * Created by User on 10/23/2017.
 */

public class DBxFile {

    private String fname;
    private String fid;
    private long fsize;

    public DBxFile(){ }

    public DBxFile(String name, String id, long size){
        this.fname = name;
        this.fid = id;
        this.fsize = size;
    }

    public String getDBxFileName(){ return this.fname; }

    public String getDBxFileId(){ return this.fid; }

    public long getDBxFileSize() { return this.fsize; }

    public void setDBxFileName(String name) { this.fname = name; }

    public void setDBxFileId(String id) { this.fid = id; }

    public void setDBcFileSize(long size) { this.fsize = size; }
}
