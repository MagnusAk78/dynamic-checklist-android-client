package com.ma.dc.data.couch;

import com.google.gson.annotations.SerializedName;

public class CouchObjMeasurement extends CouchObjBase {

    @SerializedName("type_of_obj")
    private String type_of_obj;

    @SerializedName("checkpoint")
    private String checkpointId;

    @SerializedName("value")
    private Integer value;

    @SerializedName("tag")
    private String tag;

    @SerializedName("date")
    private Long date;

    public CouchObjMeasurement(final String checkpointId, final int value, final String tag, final long timestamp) {
        this.type_of_obj = "measurement";
        this.value = value;
        this.date = timestamp;
        this.checkpointId = checkpointId;
        this.tag = tag;
    }

    public String getType() {
        return type_of_obj;
    }

    public String getCheckpointId() {
        return checkpointId;
    }

    public Long getDate() {
        return date;
    }

    public Integer getValue() {
        return value;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CouchObjMeasurement(");
        sb.append("checkpointId: ");
        sb.append(checkpointId);
        sb.append(", value: ");
        sb.append(value);
        sb.append(", date: ");
        sb.append(date);
        sb.append(", tag: ");
        sb.append(tag);
        sb.append(")");
        return sb.toString();
    }
}
