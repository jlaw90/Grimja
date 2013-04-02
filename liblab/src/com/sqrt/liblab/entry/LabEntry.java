package com.sqrt.liblab.entry;

import com.sqrt.liblab.LabFile;

/**
 * Represents an entry contained within a <code>LabFile</code>
 */
public abstract class LabEntry {
    /**
     * The container of this entry
     */
    public final LabFile container;
    private String name;

    /**
     * The only constructor.  Enforces model objects to provide a container and name
     * @param container the container of this entry
     * @param name the name of this entry
     */
    protected LabEntry(LabFile container, String name) {
        this.container = container;
        setName(name);
    }

    /**
     * Returns the name of this entry
     * @return this entry's name
     */
    public final String getName() {
        return name;
    }

    /**
     * Renames this entry
     * @param name the new name for this entry
     */
    public final void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}