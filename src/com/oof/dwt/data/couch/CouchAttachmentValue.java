package com.oof.dwt.data.couch;

public class CouchAttachmentValue {
    private String content_type;
    private int revpos;
    private String digest;
    private int length;
    private boolean stub;
    
    public String getContent_type() {
        return content_type;
    }
    public int getRevpos() {
        return revpos;
    }
    public String getDigest() {
        return digest;
    }
    public int getLength() {
        return length;
    }
    public boolean isStub() {
        return stub;
    }
}
