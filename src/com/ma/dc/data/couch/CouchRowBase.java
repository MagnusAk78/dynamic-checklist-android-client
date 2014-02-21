package com.ma.dc.data.couch;

import com.google.gson.JsonObject;

abstract class CouchRowBase {
    protected String id;

    protected JsonObject value;

    public JsonObject getValue() {
        return value;
    }
}
