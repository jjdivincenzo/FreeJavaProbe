package edu.regis.jprobe.model;

import java.util.StringTokenizer;

public class JavaVersion implements Comparable<JavaVersion> {

	private int major = 1;
	private int minor = 1;
	private int rev = 0;
	private String versionString;

	public static final JavaVersion java1_1 = new JavaVersion(1, 1, 0);
	public static final JavaVersion java1_2 = new JavaVersion(1, 2, 0);
	public static final JavaVersion java1_3 = new JavaVersion(1, 3, 0);
	public static final JavaVersion java1_4 = new JavaVersion(1, 4, 0);
	public static final JavaVersion java1_5 = new JavaVersion(1, 5, 0);
	public static final JavaVersion java1_6 = new JavaVersion(1, 6, 0);
	public static final JavaVersion java1_7 = new JavaVersion(1, 7, 0);
	public static final JavaVersion java1_8 = new JavaVersion(1, 8, 0);
	public static final JavaVersion java1_9 = new JavaVersion(1, 9, 0);

	public JavaVersion() {
		this(System.getProperty("java.version"));
	}

	public JavaVersion(int major, int minor, int rev) {

		this.major = major;
		this.minor = minor;
		this.rev = rev;
		versionString = major + "." + minor + "_" + rev;

	}

	public JavaVersion(String versionString) {

		this.versionString = versionString;
		String ver = versionString.replace("_", ".");
		StringTokenizer st = new StringTokenizer(ver, ".");
		int tokens = st.countTokens();

		int[] values = new int[tokens];
		int i = 0;

		while (st.hasMoreTokens()) {
			values[i++] = parse(st.nextToken());
		}

		if (tokens > 0) {
			major = values[0];
		}

		if (tokens > 1) {
			minor = values[1];
		}

		if (tokens > 2) {
			rev = values[2];
		}

	}

	private int parse(String val) {

		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Override
	public int compareTo(JavaVersion other) {

		if (this.major > other.major)
			return 1;
		if (this.major < other.major)
			return -1;
		if (this.minor > other.minor)
			return 1;
		if (this.minor < other.minor)
			return -1;
		if (this.rev > other.rev)
			return 1;
		if (this.rev < other.rev)
			return -1;

		return 0;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof JavaVersion) {
			JavaVersion other = (JavaVersion) obj;
			if (this.major != other.major || this.minor != other.minor || this.rev != other.rev) {

				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (major * 33) + (minor * 17) + (rev * 7);
	}

	@Override
	public String toString() {
		return "Major(" + major + ") Minor(" + minor + ") Revision(" + rev + ") Original(" + versionString + ")";
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getRev() {
		return rev;
	}

	public String getVersionString() {
		return versionString;
	}

}
