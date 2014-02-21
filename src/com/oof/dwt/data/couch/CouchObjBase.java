package com.oof.dwt.data.couch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public abstract class CouchObjBase {

    @SerializedName("_id")
    private String _id;

    @SerializedName("_rev")
    private String _rev;
    
    @SerializedName("_attachments")
    private Map<String, CouchAttachmentValue> attachments;
    
    private transient Map<String, byte[]> attachmentsRawData = new HashMap<String, byte[]>(1);

    public String getId() {
        return _id;
    }

    public String getRev() {
        return _rev;
    }
    
    public int getNrOfAttachments() {
        if(attachments != null) {
            return attachments.size();
        }
        return 0;
    }
    
    public List<String> getAttachmentFilenames() {
        if(getNrOfAttachments() > 0) {
            List<String> list = new ArrayList<String>(attachments.size());
            list.addAll(attachments.keySet());
            return list;
        }
        return null;
    }
    
    public CouchAttachmentValue getAttachmentInfo(String attachmentFilename) {
        if(getNrOfAttachments() > 0) {
            return attachments.get(attachmentFilename);
        }
        return null;
    }
    
    public void addAttachmentRawData(String filename, byte[] data) {
        attachmentsRawData.put(filename, data);
    }
    
    public byte[] getAttachmentRawData(String filename) {
        return attachmentsRawData.get(filename);
    }
}