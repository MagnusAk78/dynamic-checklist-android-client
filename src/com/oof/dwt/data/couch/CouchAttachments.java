package com.oof.dwt.data.couch;

import java.util.Map;
import java.util.Set;


public class CouchAttachments {
    
    Map<String, CouchAttachmentValue> keyValueMap;
    
    public Set<String> getKeys() {
        return keyValueMap.keySet();
    }
}
