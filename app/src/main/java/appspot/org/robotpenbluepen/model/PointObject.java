package appspot.org.robotpenbluepen.model;


import android.graphics.Paint;

import org.json.JSONException;
import org.json.JSONObject;

import appspot.org.robotpenbluepen.model.entity.TrailEntity;
import appspot.org.robotpenbluepen.model.entity.symbol.BatteryState;
import appspot.org.robotpenbluepen.model.entity.symbol.SceneType;

public class PointObject {
    public static final int VALUE_INCH_116_WIDTH = 18950;
    public static final int VALUE_INCH_116_HEIGHT = 11500;
    public static final int VALUE_P1_WIDTH = 17407;
    public static final int VALUE_P1_HEIGHT = 10751;
    public static final int VALUE_P7_WIDTH = 14335;
    public static final int VALUE_P7_HEIGHT = 8191;
    public static final int VALUE_ELITE_WIDTH = 14335;
    public static final int VALUE_ELITE_HEIGHT = 8191;
    public static final int VALUE_ELITE_PLUS_WIDTH = 22015;
    public static final int VALUE_ELITE_PLUS_HEIGHT = 15359;
    private float width;
    private float height;
    private float windowWidth;
    private float windowHeight;
    private SceneType sceneType;
    private DeviceType deviceType;
    public float originalX;
    public float originalY;
    private float offsetX;
    private float offsetY;
    public boolean isRoute;
    public boolean isLeave;
    public boolean isSw1;
    public boolean isSw2;
    public float pressure;
    public short pressureValue;
    public Paint paint;
    public String key;
    public int color;
    public float weight;
    public BatteryState battery;

    public PointObject() {
        this.sceneType = SceneType.NOTHING;
        this.deviceType = DeviceType.TOUCH;
        this.pressure = 1.0F;
        this.pressureValue = 0;
        this.color = 2147483647;
        this.weight = 0.0F;
        this.battery = BatteryState.NOTHING;
    }

    public PointObject(String jsonValue) {
        this.sceneType = SceneType.NOTHING;
        this.deviceType = DeviceType.TOUCH;
        this.pressure = 1.0F;
        this.pressureValue = 0;
        this.color = 2147483647;
        this.weight = 0.0F;
        this.battery = BatteryState.NOTHING;
        if (jsonValue != null && jsonValue.startsWith("{") && jsonValue.endsWith("}")) {
            try {
                JSONObject e = new JSONObject(jsonValue);
                this.copy(e);
            } catch (JSONException var3) {
                var3.printStackTrace();
            }
        }

    }

    public PointObject(TrailEntity trail) {
        this.sceneType = SceneType.NOTHING;
        this.deviceType = DeviceType.TOUCH;
        this.pressure = 1.0F;
        this.pressureValue = 0;
        this.color = 2147483647;
        this.weight = 0.0F;
        this.battery = BatteryState.NOTHING;
        if (trail != null) {
            this.key = trail.getKey();
            this.originalX = trail.getX();
            this.originalY = trail.getY();
            this.weight = trail.getWdith();
            this.color = trail.getColor();
            this.isRoute = trail.getIsDown();
            this.deviceType = trail.getDeviceType();
        }

    }

    public PointObject(SceneType type, float windowX, float windowY, float windowW, float windowH) {
        this.sceneType = SceneType.NOTHING;
        this.deviceType = DeviceType.TOUCH;
        this.pressure = 1.0F;
        this.pressureValue = 0;
        this.color = 2147483647;
        this.weight = 0.0F;
        this.battery = BatteryState.NOTHING;
        if (!type.isHorizontal()) {
            this.sceneType = type.getHorizontalType();
            this.windowWidth = windowH;
            this.windowHeight = windowW;
            this.originalX = (windowH - windowY) * this.getWidth() / windowH;
            this.originalY = windowX * this.getHeight() / windowW;
        } else {
            this.sceneType = type;
            this.windowWidth = windowW;
            this.windowHeight = windowH;
            this.originalX = windowX * (this.getWidth() / windowW);
            this.originalY = windowY * (this.getHeight() / windowH);
        }

    }

    public void setSceneType(SceneType type) {
        this.sceneType = type;
    }

    public SceneType getSceneType() {
        return this.sceneType;
    }

    public void setDeviceType(DeviceType value) {
        this.deviceType = value;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public void setWindowWidth(float value) {
        this.windowWidth = value;
    }

    public float getWindowWidth() {
        return this.windowWidth;
    }

    public void setWindowHeight(float value) {
        this.windowHeight = value;
    }

    public float getWindowHeight() {
        return this.windowHeight;
    }

    public void setCustomScene(short width, short height) {
        this.setCustomScene((float) width, (float) height, 0.0F, 0.0F);
    }

    public void setCustomScene(float width, float height, float offsetX, float offsetY) {
        this.sceneType = SceneType.CUSTOM;
        this.width = width;
        this.height = height;
        this.setOffset(offsetX, offsetY);
    }

    public void setOffset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public float getOffsetX() {
        return this.offsetX;
    }

    public float getOffsetY() {
        return this.offsetY;
    }

    public float getWidth() {
        float w = getWidth(this.sceneType);
        return w > 0.0F ? w : this.width;
    }

    public float getHeight() {
        float h = getHeight(this.sceneType);
        return h > 0.0F ? h : this.height;
    }

    public static float getWidth(SceneType type) {
        return 8191.0F;
    }

    public static float getHeight(SceneType type) {
        return 14335.0F;
    }

    public float getWindowX() {
        return this.getWindowX(this.windowWidth);
    }

    public float getWindowX(float showWidth) {
        float x;
        if (this.sceneType.isHorizontal()) {
            if (this.deviceType == DeviceType.ELITE) {
                x = this.getWidth() - this.originalX;
            } else {
                x = this.originalX;
            }
        } else if (this.deviceType == DeviceType.ELITE) {
            x = this.getWidth() - this.originalY;
        } else {
            x = this.originalY;
        }

        float value = x + this.offsetX;
        if (value < 0.0F) {
            value = 0.0F;
        } else if (value > this.getWidth()) {
            value = this.getWidth();
        }

        if (showWidth > 0.0F) {
            value *= showWidth / this.getWidth();
        }

        return value;
    }

    public float getWindowY() {
        return this.getWindowY(this.windowHeight);
    }

    public float getWindowY(float showHeight) {
        float y;
        if (this.sceneType.isHorizontal()) {
            if (this.deviceType == DeviceType.ELITE) {
                y = this.getHeight() - this.originalY;
            } else {
                y = this.originalY;
            }
        } else if (this.deviceType == DeviceType.ELITE) {
            y = this.originalX;
        } else {
            y = this.getHeight() - this.originalX;
        }

        float value = y + this.offsetY;
        if (value < 0.0F) {
            value = 0.0F;
        } else if (value > this.getHeight()) {
            value = this.getHeight();
        }

        if (showHeight > 0.0F) {
            value *= showHeight / this.getHeight();
        }

        return value;
    }

    public String toString() {
        return "isRoute:" + this.isRoute + ", isSw1:" + this.isSw1 + ", battery:" + this.battery + " x:" + this.originalX + " ,y:" + this.originalY + " sceneType:" + this.sceneType + "  sceneX:" + this.getWindowX() +" pressureValue:"+pressureValue+ "  ,sceneY:" + this.getWindowY();
    }

    public void copy(JSONObject obj) throws JSONException {
        if (obj != null) {
            this.sceneType = SceneType.toSceneType(obj.getInt("sceneType"));
            this.width = (float) obj.getDouble("width");
            this.height = (float) obj.getDouble("height");
            this.offsetX = (float) ((short) obj.getInt("offsetX"));
            this.offsetY = (float) ((short) obj.getInt("offsetY"));
            this.originalX = (float) obj.getDouble("originalX");
            this.originalY = (float) obj.getDouble("originalY");
            this.isRoute = obj.getInt("isRoute") > 0;
            this.isSw1 = obj.getInt("isSw1") > 0;
            this.battery = BatteryState.toBatteryState(obj.getInt("battery"));
        }

    }

    public String toJsonString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("\"sceneType\":" + Integer.toString(this.sceneType.getValue()) + ",");
        result.append("\"width\":" + Float.toString(this.width) + ",");
        result.append("\"height\":" + Float.toString(this.height) + ",");
        result.append("\"offsetX\":" + Float.toString(this.offsetX) + ",");
        result.append("\"offsetY\":" + Float.toString(this.offsetY) + ",");
        result.append("\"originalX\":" + Float.toString(this.originalX) + ",");
        result.append("\"originalY\":" + Float.toString(this.originalY) + ",");
        result.append("\"isRoute\":" + (this.isRoute ? "1" : "0") + ",");
        result.append("\"isSw1\":" + (this.isSw1 ? "1" : "0") + ",");
        result.append("\"battery\":" + Integer.toString(this.battery.getValue()));
        result.append("}");
        return result.toString();
    }
}
