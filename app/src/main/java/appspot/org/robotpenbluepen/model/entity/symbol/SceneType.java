package appspot.org.robotpenbluepen.model.entity.symbol;

import appspot.org.robotpenbluepen.model.DeviceType;

/**
 * Created by wangweiwei on 2017/1/19.
 */

public enum SceneType {
    NOTHING(0),
    ELITE_PLUS(1),
    ELITE(2),
    ELITE_PLUS_H(3),
    ELITE_H(4),
    CUSTOM(5),
    INCH_116(6),
    INCH_116_horizontal(7),
    P1(8),
    P1_H(9),
    P7(10),
    P7_H(11);

    private final int value;

    private SceneType(int value) {
        this.value = value;
    }

    public static SceneType toSceneType(int value) {
        return value >= 0 && value < values().length ? values()[value] : NOTHING;
    }

    public static SceneType getSceneType(boolean isHorizontal, DeviceType deviceType) {
        return deviceType != DeviceType.P7 && deviceType != DeviceType.ELITE ? (deviceType == DeviceType.ELITE_PLUS ? (isHorizontal ? ELITE_PLUS_H : ELITE_PLUS) : (isHorizontal ? P1_H : P1)) : (isHorizontal ? P7_H : P7);
    }

    public final int getValue() {
        return this.value;
    }

    public boolean isHorizontal() {
        return this != NOTHING && this != CUSTOM && this != ELITE_PLUS && this != ELITE && this != P1 && this != INCH_116 && this != P7;
    }

    public SceneType getHorizontalType() {
        return P7_H;

    }
}
