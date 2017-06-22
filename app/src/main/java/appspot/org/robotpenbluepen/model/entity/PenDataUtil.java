package appspot.org.robotpenbluepen.model.entity;


import appspot.org.robotpenbluepen.model.PointObject;

public class PenDataUtil {
    public static final int TOUCH_DATA_VALID_LENGTH = 8;
    public static final int SYNC_DATA_VALID_LENGTH = 5;
    public static final byte VALUE_PEN = 2;
    public static final byte VALUE_PEN_UP = 16;
    public static final byte VALUE_PEN_DOWN = 17;
    public static final byte VALUE_PEN_SWITCH_1 = 19;
    protected static float mLastX;
    protected static float mLastY;

    public PenDataUtil() {
    }

    protected static float getPressure(short value) {
        return (float) value / 800.0F;
    }

    public static boolean existPenRoute(byte[] data) {
        if (data != null && data.length >= 8) {
            for (int i = 0; i < data.length; i += 8) {
                if (isPenData(data[i]) && isPenRoute(data, i)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isTrailEnd(byte[] data, int i) {
        return data[i] == -16 | data[i] == -32 && data[i + 1] == 0 && data[i + 2] == 0 && data[i + 3] == 0 && data[i + 4] == 0;
    }

    public static boolean isPenRoute(byte[] data, int i) {
        return isPenRoute(data[i + 1]);
    }

    public static boolean isPenRoute(byte value) {
        return value == 17;
    }

    protected static boolean isPenSw1(byte[] data, int i) {
        boolean result = false;
        if (data[i + 1] == 19) {
            result = true;
        }

        return result;
    }

    protected static boolean isPenSw2(byte[] data, int i) {
        return data[i + 1] == 48;
    }

    protected static boolean isPenData(byte b1) {
        return b1 == 2;
    }

    protected static int isTrailData(byte[] data, int i) {
        return i >= data.length - 5 ? -1 : (validate(data[i]) ? (data.length > i + 10 ? (validate(data[i + 10]) && validate(data[i + 5]) ? i : isTrailData(data, i + 1)) : (data.length > i + 5 ? (validate(data[i + 5]) ? i : isTrailData(data, i + 1)) : i)) : isTrailData(data, i + 1));
    }


    static final boolean validate(byte data) {
        boolean b = (data & 240) == 240 || (data >> 5 & 7) == 7;
        return b;
    }

    public static void clearDataBuffer() {
        mLastX = 0.0F;
        mLastY = 0.0F;
    }

    public static String getFormatFirmwareVersion(int value) {
        byte[] data = new byte[]{(byte) ((-16777216 & value) >> 24), (byte) ((16711680 & value) >> 16), (byte) (('\uff00' & value) >> 8), (byte) (255 & value)};
        return getFormatVersion(data);
    }

    public static String getFormatHardwareVersion(int value) {
        byte[] data = new byte[]{(byte) ((255 & value) >> 8), (byte) (255 & value)};
        return getFormatVersion(data);
    }

    public static String getFormatVersion(byte[] data) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.length; ++i) {
            sb.append(".");
            sb.append(Integer.parseInt(toHex(data[i]), 16));
        }

        return sb.substring(1);
    }

    public static int byteToFirmwareVersion(byte[] data, int start) {
        StringBuilder sb = new StringBuilder();

        for (int i = start; i < start + 4; ++i) {
            sb.insert(0, toHex(data[i]));
        }

        return Integer.parseInt(sb.toString(), 16);
    }

    public static int byteToHardwareVersion(byte[] data, int start) {
        StringBuilder sb = new StringBuilder();

        for (int i = start; i < start + 2; ++i) {
            sb.insert(0, toHex(data[i]));
        }

        return Integer.parseInt(sb.toString(), 16);
    }

    public static String toHex(byte by) {
        return "" + "0123456789ABCDEF".charAt(by >> 4 & 15) + "0123456789ABCDEF".charAt(by & 15);
    }

    public static short byteToshort(byte[] by) {
        return byteToshort(by, 0);
    }

    public static short byteToshort(byte[] by, int offset) {
        short toshort = (short) ((by[offset + 1] & 255) << 8 | by[offset] & 255);
        return toshort;
    }

    public static int byteToInteger(byte[] by) {
        return byteToInteger(by, 0);
    }

    public static int byteToInteger(byte[] by, int start) {
        StringBuilder sb = new StringBuilder();

        for (int i = start; i < start + 4 && i < by.length; ++i) {
            sb.insert(0, String.format("%02X", new Object[]{Byte.valueOf(by[i])}));
        }

        return Integer.parseInt(sb.toString(), 16);
    }


    public static PointObject[] getGetPoint() {
        return getPoint;
    }

    public void setGetPoint(PointObject[] getPoint) {
        this.getPoint = getPoint;
    }

    public static PointObject[] getPoint;


    public static PointObject[] getPointList(byte[] data) {
        PointObject[] list = null;
        if (data != null && data.length > 0) {
            list = new PointObject[data.length / 8];
            fillPointList(list, data);

        }
        return list;
    }

    private static void fillPointList(PointObject[] list, byte[] penData) {
        byte[] byX = new byte[2];
        byte[] byY = new byte[2];
        byte[] byPressure = new byte[2];
        int index = 0;

        for (int i = 0; i < penData.length; i += 8) {
            PointObject item = null;
            if (isPenData(penData[i])) {
                byX[0] = penData[i + 2];
                byX[1] = penData[i + 3];
                byY[0] = penData[i + 4];
                byY[1] = penData[i + 5];
                byPressure[0] = penData[i + 6];
                byPressure[1] = penData[i + 7];
                short x = byteToshort(byX);
                short y = byteToshort(byY);
                boolean isRoute = false;
                boolean isLeave = penData[i + 1] == 0;
                if (!isLeave) {
                    isRoute = isPenRoute(penData[i + 1]);
                }

                double gap = !isRoute ? 2.0D : Math.sqrt(Math.pow((double) (mLastX - (float) x), 2.0D) + Math.pow((double) (mLastY - (float) y), 2.0D));
                if (gap > 1.0D) {
                    mLastX = (float) x;
                    mLastY = (float) y;
                    item = new PointObject();
                    item.originalX = (float) x;
                    item.originalY = (float) y;
                    item.isLeave = isLeave;
                    item.isRoute = isRoute;
                    item.pressureValue = byteToshort(byPressure);
                    item.pressure = getPressure(item.pressureValue);

                }
            }

            list[index] = item;
            ++index;
        }

    }
}
