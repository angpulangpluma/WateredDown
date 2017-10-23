package com.example.user.watereddown;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.users.FullAccount;

/**
 * Created by User on 10/23/2017.
 */

public class DropboxManager {

    private String ACCESS_TOKEN;
    private String ID;

    private DbxClientV2 client;
    public DropboxManager(String tken, String id){
        this.ACCESS_TOKEN = tken;
        this.ID = id;
    }

    public void init(){
        DbxRequestConfig config = new DbxRequestConfig(ID, "en_US");
        client = new DbxClientV2(config, ACCESS_TOKEN);
    }

    public FullAccount getAccount() throws com.dropbox.core.DbxException{
        return client.users().getCurrentAccount();
    }

    public void close(){
        this.client = null;
    }

    public ListFolderResult getListOfFiles(String path) throws com.dropbox.core.DbxException{
        return client.files().listFolder(path);
    }

}
