package com.sqrt.liblab.threed;

/**
 * Represents an angle for rotation
 */
public class Angle {
    /**
     * No rotation (0 degrees)
     */
    public static final Angle zero = new Angle(0);

    /**
     * The angle in degrees
     */
    public final float degrees;

    /**
     * Constructs a new Angle with the specified angle...
     * @param degrees the angle in degrees
     */
    public Angle(float degrees) {
        this.degrees = degrees;
    }

    /**
     * Copies an angle
     * @param a the angle to copy
     */
    public Angle(Angle a) {
        this.degrees = a.degrees;
    }

    /**
     * Normalizes this angle to the specified central angle and returns it
     * @param low the central angle
     * @return the result
     */
    public Angle normalize(float low) {
        return new Angle(getDegrees(low));
    }

    /**
     * Clamps this angle to a range within the specified magnitude
     * @param mag the magnitude
     * @return the clamped angle
     */
    public Angle clamp(float mag) {
        float degrees = getDegrees(-180f);
        if(degrees > mag)
            degrees = mag;
        else if(degrees < -mag)
            degrees = -mag;
        return new Angle(degrees);
    }

    /**
     * Returns this angle normalized around the specified central point
     * @param low the center point of this angle
     * @return the result
     */
    public float getDegrees(float low) {
        float degrees = this.degrees;
        if (degrees >= low + 360.f) {
            float x = (float) Math.floor((degrees - low) / 360f);
            degrees -= 360.f * x;
        } else if (degrees < low) {
            float x = (float) Math.floor((degrees - low) / 360.f);
            degrees -= 360.f * x;
        }
        return degrees;
    }

    /**
     * Adds this angle to the specified angle and returns the result
     * @param a the angle to add
     * @return the result of the addition
     */
    public Angle add(Angle a) {
        return new Angle(a.degrees + degrees);
    }

    /**
     * Multiplies this angle against the specified float value and returns the result
     * @param f the multiplication factor
     * @return the result
     */
    public Angle mult(float f) {
        return new Angle(degrees*f);
    }

    /**
     * Subtracts the specified angle from this angle and returns the result
     * @param a the angle to subtract
     * @return the result
     */
    public Angle sub(Angle a) {
        return new Angle(degrees - a.degrees);
    }
}