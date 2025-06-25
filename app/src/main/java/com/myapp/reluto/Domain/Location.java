package com.myapp.reluto.Domain;

public class Location {
    private int id;
    private String loc;

    public Location() {
    }

    @Override
    public String toString() {
        return loc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }
}
