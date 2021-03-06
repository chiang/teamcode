package io.teamcode.common.unit;

import io.teamcode.common.Strings;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class TimeValue implements Serializable {

	private static final long serialVersionUID = -4100352375203101843L;

	static final long C0 = 1L;
	static final long C1 = C0 * 1000L;
	static final long C2 = C1 * 1000L;
	static final long C3 = C2 * 1000L;
	static final long C4 = C3 * 60L;
	static final long C5 = C4 * 60L;
	static final long C6 = C5 * 24L;

	private long duration;

	private TimeUnit timeUnit;

	private TimeValue() {

	}

	public TimeValue(long millis) {
		this(millis, TimeUnit.MILLISECONDS);
	}

	public TimeValue(long duration, TimeUnit timeUnit) {
		this.duration = duration;
		this.timeUnit = timeUnit;
	}

	public static TimeValue timeValueMillis(long millis) {
		return new TimeValue(millis, TimeUnit.MILLISECONDS);
	}

	public static TimeValue timeValueMinutes(long minutes) {
		return new TimeValue(minutes, TimeUnit.MINUTES);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		TimeValue timeValue = (TimeValue) o;

		if (duration != timeValue.duration)
			return false;
		if (timeUnit != timeValue.timeUnit)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (duration ^ (duration >>> 32));
		result = 31 * result + (timeUnit != null ? timeUnit.hashCode() : 0);
		return result;
	}

	public long nanos() {
		return timeUnit.toNanos(duration);
	}

	public long getNanos() {
		return nanos();
	}

	public long millis() {
		return timeUnit.toMillis(duration);
	}
	
	public long seconds() {
        return timeUnit.toSeconds(duration);
    }

	public long days() {
		return timeUnit.toDays(duration);
	}

	public long getDays() {
		return days();
	}

	public double microsFrac() {
		return ((double) nanos()) / C1;
	}

	public double getMicrosFrac() {
		return microsFrac();
	}

	public double millisFrac() {
		return ((double) nanos()) / C2;
	}

	public double getMillisFrac() {
		return millisFrac();
	}

	public double secondsFrac() {
		return ((double) nanos()) / C3;
	}

	public double getSecondsFrac() {
		return secondsFrac();
	}

	public double minutesFrac() {
		return ((double) nanos()) / C4;
	}

	public double getMinutesFrac() {
		return minutesFrac();
	}

	public double hoursFrac() {
		return ((double) nanos()) / C5;
	}

	public double getHoursFrac() {
		return hoursFrac();
	}

	public double daysFrac() {
		return ((double) nanos()) / C6;
	}

	public double getDaysFrac() {
		return daysFrac();
	}

	public static TimeValue timeValueSeconds(long seconds) {
		return new TimeValue(seconds, TimeUnit.SECONDS);
	}

	public static TimeValue parseTimeValue(String sValue, TimeValue defaultValue) {
		if (sValue == null) {
			return defaultValue;
		}
		try {
			long millis;
			if (sValue.endsWith("S")) {
				millis = Long
						.parseLong(sValue.substring(0, sValue.length() - 1));
			} else if (sValue.endsWith("ms")) {
				millis = (long) (Double.parseDouble(sValue.substring(0,
						sValue.length() - "ms".length())));
			} else if (sValue.endsWith("s")) {
				millis = (long) (Double.parseDouble(sValue.substring(0,
						sValue.length() - 1)) * 1000);
			} else if (sValue.endsWith("m")) {
				millis = (long) (Double.parseDouble(sValue.substring(0,
						sValue.length() - 1)) * 60 * 1000);
			} else if (sValue.endsWith("H") || sValue.endsWith("h")) {
				millis = (long) (Double.parseDouble(sValue.substring(0,
						sValue.length() - 1)) * 60 * 60 * 1000);
			} else if (sValue.endsWith("d")) {
				millis = (long) (Double.parseDouble(sValue.substring(0,
						sValue.length() - 1)) * 24 * 60 * 60 * 1000);
			} else if (sValue.endsWith("w")) {
				millis = (long) (Double.parseDouble(sValue.substring(0,
						sValue.length() - 1))
						* 7 * 24 * 60 * 60 * 1000);
			} else {
				millis = Long.parseLong(sValue);
			}
			return new TimeValue(millis, TimeUnit.MILLISECONDS);
		} catch (NumberFormatException e) {
			//TODO throw new ScavaParseException("Failed to parse [" + sValue + "]", e);
			throw e;
		}
	}

	@Override
	public String toString() {
		if (duration < 0) {
			return Long.toString(duration);
		}
		long nanos = nanos();
		if (nanos == 0) {
			return "0s";
		}
		double value = nanos;
		String suffix = "nanos";
		if (nanos >= C6) {
			value = daysFrac();
			suffix = "d";
		} else if (nanos >= C5) {
			value = hoursFrac();
			suffix = "h";
		} else if (nanos >= C4) {
			value = minutesFrac();
			suffix = "m";
		} else if (nanos >= C3) {
			value = secondsFrac();
			suffix = "s";
		} else if (nanos >= C2) {
			value = millisFrac();
			suffix = "ms";
		} else if (nanos >= C1) {
			value = microsFrac();
			suffix = "micros";
		}
		return Strings.format1Decimals(value, suffix);
	}

}
