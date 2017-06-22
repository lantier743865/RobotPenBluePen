package appspot.org.robotpenbluepen.model;


import android.text.TextUtils;

public enum DeviceType {
    TOUCH(0),
    P7(1),
    ELITE(2),
    ELITE_PLUS(3),
    P1(4);

    private final int value;

    private DeviceType(int value) {
        this.value = value;
    }

    public static DeviceType getBleType(int type) {
        return type == 2?ELITE:(type == 3?ELITE_PLUS:P7);
    }

    public static DeviceType toDeviceType(String deviceIdent) {
        if(!TextUtils.isEmpty(deviceIdent)) {
            DeviceType[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                DeviceType type = var1[var3];
                if(deviceIdent.startsWith(type.getDeviceIdent())) {
                    return type;
                }
            }
        }

        return TOUCH;
    }

    public static DeviceType toDeviceType(int value) {
        return value >= 0 && value < values().length?values()[value]:TOUCH;
    }

    public final int getValue() {
        return this.value;
    }

    public boolean isBleDevice() {
        return this == P7 || this == ELITE || this == ELITE_PLUS;
    }

    public String getDeviceIdent() {
        return this == P1?"P1_":(this == P7?"P7_":(this == ELITE?"ELITE_":(this == ELITE_PLUS?"ELITE_PLUS_":"TOUCH_")));
    }
}
