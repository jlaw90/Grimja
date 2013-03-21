package com.sqrt.liblab;

public abstract class LabEntry {
    public final LabFile container;
    private String name;

    protected LabEntry(LabFile container, String name) {
        this.container = container;
        setName(name);
    }

    public final String getName() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public final void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}