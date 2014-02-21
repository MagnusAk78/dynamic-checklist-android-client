package com.ma.dc.data.couch;

import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class CouchViewResultKeyArray extends CouchViewResultBase {

    @SerializedName("rows")
    private CouchRowKeyArray[] rows;

    @Override
    public int getNumberOfRows() {
        return rows.length;
    }

    @Override
    public void populateListFromRows(List<JsonObject> listOfObjects) {
        for (CouchRowKeyArray row : rows) {
            listOfObjects.add(row.getValue());
        }
    }
}
