package com.codepath.gogreen.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by anyazhang on 7/14/17.
 */
@ParseClassName("Action")
public class Action extends ParseObject {
    public long uid;
    public String actionType;
    public String subType;
    public double magnitude;
    public double points;

    public String getSubType() {
        return this.getString("subType");
//        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
        this.put("subType", subType);
    }

    public long getUid() {
        return this.getLong("uid");

//        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
        this.put("uid", uid);
    }

    public String getActionType() {
        return this.getString("actionType");

//        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
        this.put("actionType", actionType);
    }

    public double getMagnitude() {
        return this.getDouble("magnitude");
//        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
        this.put("magnitude", magnitude);
    }

    public double getPoints() {

        return this.getDouble("points");
//        return points;
    }

    public void setPoints(double points) {
        this.points = points;
        this.put("points", points);
    }
}