package appspot.org.robotpenbluepen.model.entity.symbol;

/**
 * Created by wangweiwei on 2017/1/19.
 */
public enum BatteryState {
    NOTHING(0),
    LOW(1),
    GOOD(2);

    private final int value;

    private BatteryState(int value) {
        this.value = value;
    }

    public static BatteryState toBatteryState(int value) {
        return value >= 0 && value < values().length?values()[value]:NOTHING;
    }

    public final int getValue() {
        return this.value;
    }
}
