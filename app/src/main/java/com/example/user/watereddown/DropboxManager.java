package com.example.user.watereddown;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.util.ArrayList;
import java.util.List;

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
        this.client = new DbxClientV2(config, ACCESS_TOKEN);
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

    public List<DBxFile> getFullListOfFiles(String path) throws com.dropbox.core.DbxException{
        List<DBxFile> files = new ArrayList<>();
        ListFolderResult result = getListOfFiles(path);
        DBxFile f;

        if (result.getEntries().size()>0){
            while(true){
                for(Metadata metadata : result.getEntries()){
                    if (metadata instanceof FileMetadata){
                        f = new DBxFile(
                                ((FileMetadata)metadata).getName(),
                                ((FileMetadata) metadata).getId(),
                                ((FileMetadata)metadata).getSize());
                        files.add(f);
                    }
//                System.out.println(metadata.getPathLower());
                }

                if (!result.getHasMore())
                    break;
            }
        }

        return files;
    }

}
