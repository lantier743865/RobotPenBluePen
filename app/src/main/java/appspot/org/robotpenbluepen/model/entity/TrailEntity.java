package appspot.org.robotpenbluepen.model.entity;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;

import appspot.org.robotpenbluepen.model.DeviceType;

public class TrailEntity implements Parcelable, Serializable {
    private static final long serialVersionUID = 1L;
    public static final Creator<TrailEntity> CREATOR = new Creator() {
        public TrailEntity createFromParcel(Parcel source) {
            return new TrailEntity(source);
        }

        public TrailEntity[] newArray(int size) {
            return new TrailEntity[size];
        }
    };

    private String key;
    private float x;
    private float y;
    private float wdith;
    private int color;
    private boolean isDown;

    public TrailEntity() {
    }

    protected TrailEntity(Parcel in) {
        this.key = in.readString();
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.wdith = in.readFloat();
    }

    public TrailEntity(String key, float x, float y, float wdith) {
        this.key = key;
        this.x = x;
        this.y = y;
        this.wdith = wdith;
    }

    public DeviceType getDeviceType() {
        return !TextUtils.isEmpty(this.key)?DeviceType.toDeviceType(this.key):DeviceType.TOUCH;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeFloat(this.x);
        dest.writeFloat(this.y);
        dest.writeFloat(this.wdith);
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWdith() {
        return this.wdith;
    }

    public void setWdith(float wdith) {
        this.wdith = wdith;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean getIsDown() {
        return this.isDown;
    }

    public void setIsDown(boolean isDown) {
        this.isDown = isDown;
    }
}
