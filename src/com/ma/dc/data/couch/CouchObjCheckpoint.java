package com.ma.dc.data.couch;

import com.google.gson.annotations.SerializedName;

public class CouchObjCheckpoint extends CouchObjBase {

    @SerializedName("type_of_obj")
    private String type_of_obj;

    @SerializedName("checkpoint_name")
    private String checkpointName;

    @SerializedName("description")
    private String description;
    
    @SerializedName("active")
    private Boolean active;

    @SerializedName("updates")
    private Integer updates;
    
    @SerializedName("time_period")
    private String timePeriod;
    
    @SerializedName("start_time")
    private Integer startTime;
    
    @SerializedName("start_day")
    private Integer startDay;
    
    @SerializedName("include_weekends")
    private Boolean includeWeekends;
    
    @SerializedName("order_nr")
    private Integer orderNr;
    
    @SerializedName("error_tag_1")
    private String errorTag1;
    
    @SerializedName("error_tag_2")
    private String errorTag2;
    
    @SerializedName("error_tag_3")
    private String errorTag3;
    
    @SerializedName("error_tag_4")
    private String errorTag4;
    
    @SerializedName("action_tag_1")
    private String actionTag1;
    
    @SerializedName("action_tag_2")
    private String actionTag2;
    
    @SerializedName("action_tag_3")
    private String actionTag3;
    
    @SerializedName("action_tag_4")
    private String actionTag4;

    public String getType() {
        return type_of_obj;
    }

    public String getName() {
        return checkpointName;
    }

    public String getDescription() {
        return description;
    }
    
    public Boolean getActive() {
        return active;
    }

    public Integer getUpdates() {
        return updates;
    }

    public String getTimePeriod() {
        return timePeriod;
    }
    
    public Integer getStartTime() {
        return startTime;
    }
    
    public Integer getStartDay() {
        return startDay;
    }
    
    public Boolean getIncludeWeekends() {
        return includeWeekends;
    }
    
    public Integer getOrderNr() {
        return orderNr;
    }
    
    public String getErrorTag1() {
        return errorTag1;
    }
    
    public String getErrorTag2() {
        return errorTag2;
    }
    
    public String getErrorTag3() {
        return errorTag3;
    }
    
    public String getErrorTag4() {
        return errorTag4;
    }
    
    public String getActionTag1() {
        return actionTag1;
    }
    
    public String getActionTag2() {
        return actionTag2;
    }
    
    public String getActionTag3() {
        return actionTag3;
    }
    
    public String getActionTag4() {
        return actionTag4;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CouchObjCheckpoint(");
        sb.append("checkpointName: ");
        sb.append(checkpointName);
        sb.append(", description: ");
        sb.append(description);
        sb.append(", active: ");
        sb.append(active);
        sb.append(", updates: ");
        sb.append(updates);
        sb.append(", timePeriod: ");
        sb.append(timePeriod);
        sb.append(", startTime: ");
        sb.append(startTime);
        sb.append(", startDay: ");
        sb.append(startDay);
        sb.append(", includeWeekends: ");
        sb.append(includeWeekends);
        sb.append(", orderNr: ");
        sb.append(orderNr);
        sb.append(")");
        return sb.toString();
    }
}