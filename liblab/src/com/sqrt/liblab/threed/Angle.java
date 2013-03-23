package com.sqrt.liblab.threed;

public class Angle {
    public static final Angle zero = new Angle(0);

    public final float degrees, radians;

    public Angle(float degrees) {
        this.degrees = degrees;
        this.radians = (float) Math.toRadians(degrees);
    }

    public Angle(Angle a) {
        this.degrees = a.degrees;
        this.radians = a.radians;
    }

    public Angle normalize(float low) {
        return new Angle(getDegrees(low));
    }

    public Angle clamp(float mag) {
        float degrees = getDegrees(-180f);
        if(degrees > mag)
            degrees = mag;
        else if(degrees < -mag)
            degrees = -mag;
        return new Angle(degrees);
    }

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

    public Angle add(Angle a) {
        return new Angle(a.degrees + degrees);
    }

    public Angle mult(float f) {
        return new Angle(degrees*f);
    }

    public Angle sub(Angle a) {
        return new Angle(degrees - a.degrees);
    }
}