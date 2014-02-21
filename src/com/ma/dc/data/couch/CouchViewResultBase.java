package com.ma.dc.data.couch;

import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

abstract class CouchViewResultBase {

    @SerializedName("total_rows")
    private Integer totalRows;

    @SerializedName("offset")
    private Integer offset;

    public Integer getTotalNumberOfRows() {
        return totalRows;
    }

    public abstract int getNumberOfRows();

    public final Integer getOffset() {
        return offset;
    }

    public abstract void populateListFromRows(List<JsonObject> listOfObjects);
}
