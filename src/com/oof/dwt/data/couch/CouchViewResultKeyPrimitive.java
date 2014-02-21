package com.oof.dwt.data.couch;

import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class CouchViewResultKeyPrimitive extends CouchViewResultBase {

    @SerializedName("rows")
    private CouchRowKeyPrimitive[] rows;

    @Override
    public int getNumberOfRows() {
        return rows.length;
    }

    @Override
    public void populateListFromRows(List<JsonObject> listOfObjects) {
        for (CouchRowKeyPrimitive row : rows) {
            listOfObjects.add(row.getValue());
        }
    }
}
