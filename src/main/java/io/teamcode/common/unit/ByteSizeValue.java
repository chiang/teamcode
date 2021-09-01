package io.teamcode.common.unit;

import io.teamcode.common.Strings;

import java.io.Serializable;

public class ByteSizeValue implements Serializable {

    private long size;

    private ByteSizeUnit sizeUnit;

    private ByteSizeValue() {

    }

    public ByteSizeValue(long bytes) {
        this(bytes, ByteSizeUnit.BYTES);
    }

    public ByteSizeValue(long size, ByteSizeUnit sizeUnit) {
        this.size = size;
        this.sizeUnit = sizeUnit;
    }

    public int bytesAsInt() throws IllegalArgumentException {
        long bytes = bytes();
        if (bytes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("size [" + toString() + "] is bigger than max int");
        }
        return (int) bytes;
    }

    public long bytes() {
        return sizeUnit.toBytes(size);
    }

    public long getBytes() {
        return bytes();
    }

    public long kb() {
        return sizeUnit.toKB(size);
    }

    public long getKb() {
        return kb();
    }

    public long mb() {
        return sizeUnit.toMB(size);
    }

    public long getMb() {
        return mb();
    }

    public long gb() {
        return sizeUnit.toGB(size);
    }

    public long getGb() {
        return gb();
    }

    public double kbFrac() {
        return ((double) bytes()) / ByteSizeUnit.C1;
    }

    public double getKbFrac() {
        return kbFrac();
    }

    public double mbFrac() {
        return ((double) bytes()) / ByteSizeUnit.C2;
    }

    public double getMbFrac() {
        return mbFrac();
    }

    public double gbFrac() {
        return ((double) bytes()) / ByteSizeUnit.C3;
    }

    public double getGbFrac() {
        return gbFrac();
    }

    @Override
    public String toString() {
        long bytes = bytes();
        double value = bytes;
        String suffix = " b";
        if (bytes >= ByteSizeUnit.C3) {
            value = gbFrac();
            suffix = " gb";
        } else if (bytes >= ByteSizeUnit.C2) {
            value = mbFrac();
            suffix = " mb";
        } else if (bytes >= ByteSizeUnit.C1) {
            value = kbFrac();
            suffix = " kb";
        }
        return Strings.format1Decimals(value, suffix);
    }

    public static ByteSizeValue parseBytesSizeValue(String sValue) {
        return parseBytesSizeValue(sValue, null);
    }

    public static ByteSizeValue parseBytesSizeValue(String sValue, ByteSizeValue defaultValue) {
        if (sValue == null) {
            return defaultValue;
        }
        long bytes;
        try {
            if (sValue.endsWith("k") || sValue.endsWith("K")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 2)) * ByteSizeUnit.C1);
            } else if (sValue.endsWith("kb")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 3)) * ByteSizeUnit.C1);
            } else if (sValue.endsWith("m") || sValue.endsWith("M")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 2)) * ByteSizeUnit.C2);
            } else if (sValue.endsWith("mb")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 3)) * ByteSizeUnit.C2);
            } else if (sValue.endsWith("g") || sValue.endsWith("G")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 2)) * ByteSizeUnit.C3);
            } else if (sValue.endsWith("gb")) {
                bytes = (long) (Double.parseDouble(sValue.substring(0, sValue.length() - 3)) * ByteSizeUnit.C3);
            } else if (sValue.endsWith("b")) {
                bytes = Long.parseLong(sValue.substring(0, sValue.length() - 2));
            } else {
                bytes = Long.parseLong(sValue);
            }
        } catch (NumberFormatException e) {
            throw e;//FIXME throw new e("Failed to parse [" + sValue + "]", e);
        }
        return new ByteSizeValue(bytes, ByteSizeUnit.BYTES);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteSizeValue sizeValue = (ByteSizeValue) o;

        if (size != sizeValue.size) return false;
        if (sizeUnit != sizeValue.sizeUnit) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (size ^ (size >>> 32));
        result = 31 * result + (sizeUnit != null ? sizeUnit.hashCode() : 0);
        return result;
    }
}