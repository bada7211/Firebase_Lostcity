package com.example.firebasetest;

public class Room {
    String Name;
    String State;

    public Room(String name, String state) {
        this.Name = name;
        this.State = state;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        this.State = state;
    }

}
