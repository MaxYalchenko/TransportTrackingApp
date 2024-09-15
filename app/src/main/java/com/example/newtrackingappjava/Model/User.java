package com.example.newtrackingappjava.Model;

import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable {
    private String uid, email, transportName;
    private int fuelCost;
    private HashMap<String, User> acceptList;
    private boolean online;

    public User(String uid, String email, String transportName, int fuelCost) {
        this.uid = uid;
        this.email = email;
        this.transportName = transportName;
        this.fuelCost = fuelCost;
        acceptList = new HashMap<>();
    }

    public User(){

    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getTransportName() {
        return transportName;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
    }
    public int getFuelCost() {
        return fuelCost;
    }

    public void setFuelCost(int fuelCost) {
        this.fuelCost = fuelCost;
    }

    public HashMap<String, User> getAcceptList() {
        return acceptList;
    }

    public void setAcceptList(HashMap<String, User> acceptList) {
        this.acceptList = acceptList;
    }

    public boolean isOnline(){
        return online;
    }
    public void setOnline(boolean online){
        this.online = online;
    }

}
