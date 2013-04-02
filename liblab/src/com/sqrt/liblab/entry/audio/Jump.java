package com.sqrt.liblab.entry.audio;

/**
 * A jump from one region to another
 */
public class Jump {
    /**
     * Unknown, but most likely used to enable or disable jumps from LUA
     */
    public int hookId;
    /**
     * How long it takes to fade from one region to the other? (I guess)
     */
    public int fadeDelay;
    /**
     * The target region for this jump
     */
    public Region target;
}