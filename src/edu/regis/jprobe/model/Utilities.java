///////////////////////////////////////////////////////////////////////////////////
//
//    Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2007  James Di Vincenzo
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
///////////////////////////////////////////////////////////////////////////////////

package edu.regis.jprobe.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import edu.regis.jprobe.jni.OSSystemInfo;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author jdivince
 *
 *         This class houses the various utilities used by the system.
 */
public class Utilities {

	public static final long SEC_IN_MIN = 60;
	public static final long SEC_IN_HOUR = SEC_IN_MIN * 60;
	public static final long SEC_IN_DAY = SEC_IN_HOUR * 24;
	public static final long NANOS_PER_MILLI = 1000000;
	public static final long MILLIS_PER_SEC = 1000;
	public static final long NANOS_PER_SECOND = NANOS_PER_MILLI * MILLIS_PER_SEC;
	public static final long ONE_KB = 1024;
	public static final long ONE_MB = ONE_KB * ONE_KB;
	public static final long ONE_GB = ONE_MB * ONE_KB;
	public static final long ONE_TB = ONE_GB * ONE_KB;
	public static final String WINDOW_TITLE = "Java VM Probe 3.1";
	public static final String VERSION_HEADING = "[" + WINDOW_TITLE + "]";
	public static final String JAR_NAME = "JProbe.jar";
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	private static final int MAX_MESSAGES = 1000;

	public static boolean debug = false;
	private static List<String> debugMsgs = new ArrayList<String>();
	private static int msgSeqNum = 0;

	static {
		String val = System.getProperty("edu.regis.jprobe.debug");
		if (val == null) {
			val = System.getenv("jprobe_debug");
		}
		debug = "true".equalsIgnoreCase(val);
	}

	/**
	 * This method will return the current date offset by adding the specified
	 * number of days.
	 * 
	 * @param days
	 *            int, days to add to the current date
	 * @return String, formatted date.
	 */
	public static String getDate(int days) {

		return getDateTime("MM/dd/yyyy", days);
	}

	/**
	 * This method will return the current date and time
	 * 
	 * @return String, formatted date and time
	 */
	public static String getDateTime() {

		return getDateTime("MM/dd/yyyy - HH:mm:ss");
	}

	/**
	 * This method will return the current date and time
	 * 
	 * @return String, formatted date and time
	 */
	public static String getDateTime(String pattern) {

		return getDateTime(pattern, 0);
	}

	/**
	 * This method will return the current year offset by adding the specified
	 * number of years.
	 * 
	 * @param years
	 *            int, number of years to offset.
	 * @return String, formatted year
	 */
	public static String getYear(int years) {

		return getDateTime("yyyy", years * 366);
	}

	public static String getHostName() {

		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostName = "localhost";
		}

		return hostName;
	}

	/**
	 * This method will return the current date and time as a timestamp.
	 * 
	 * @return String, formatted date and time
	 */
	public static String getTimeStamp() {

		return getDateTime("MMddyyyyHHmmssSSS", 0);
	}

	/**
	 * This method will return the current date and time
	 * 
	 * @return String, formatted date and time
	 */
	public static String getDateTime(String pattern, int days) {

		GregorianCalendar target = new GregorianCalendar();
		SimpleDateFormat output = new SimpleDateFormat();
		target.add(Calendar.DAY_OF_MONTH, days);
		Date date = target.getTime();
		output.applyPattern(pattern);
		return output.format(date);
	}

	/**
	 * This method will format a date and return it as a string
	 * 
	 * @return String, formatted date and time
	 */
	public static String formatDateTime(Date date, String pattern) {

		SimpleDateFormat output = new SimpleDateFormat();
		output.applyPattern(pattern);
		return output.format(date);
	}

	public static String formatElapsedTime(long secondsValue) {

		long hours = secondsValue / SEC_IN_HOUR;
		long remainder = secondsValue % SEC_IN_HOUR;
		long minutes = remainder / SEC_IN_MIN;
		long seconds = remainder % SEC_IN_MIN;

		Object obj[] = new Object[3];
		obj[0] = new Long(hours);
		obj[1] = new Long(minutes);
		obj[2] = new Long(seconds);
		return String.format("%02d:%02d:%02d", obj);

	}

	public static String formatCPUTime(long millis) {

		long secondsValue = millis / MILLIS_PER_SEC;
		long milliValue = millis % MILLIS_PER_SEC;
		long hours = secondsValue / SEC_IN_HOUR;
		long remainder = secondsValue % SEC_IN_HOUR;
		long minutes = remainder / SEC_IN_MIN;
		long seconds = remainder % SEC_IN_MIN;

		Object obj[] = new Object[4];
		obj[0] = new Long(hours);
		obj[1] = new Long(minutes);
		obj[2] = new Long(seconds);
		obj[3] = new Long(milliValue);
		return String.format("%02d:%02d:%02d.%02d", obj);

	}

	public static String formatTimeStamp(long millis) {

		Date date = new Date(millis);
		SimpleDateFormat output = new SimpleDateFormat();
		output.applyPattern("HH:mm:ss.SSS");
		return output.format(date);

	}

	public static String formatTimeStamp(long millis, String pattern) {

		Date date = new Date(millis);
		SimpleDateFormat output = new SimpleDateFormat();
		output.applyPattern(pattern);
		return output.format(date);

	}

	public static String splitToLength(String in, int len, int max) {

		StringBuilder sb = new StringBuilder();

		int inLen = in.length();
		if (inLen <= len) {
			return in;
		}
		int idx = 0;
		int curr = 0;

		while (idx < inLen) {
			char c = in.charAt(idx++);
			if (curr++ > len) {
				if (!Character.isLetterOrDigit(c) || curr > max) {
					sb.append("\n");
					curr = 0;
				}
			}
			sb.append(c);

		}

		return sb.toString();

	}

	public static void debugMsg(String msg) {

		String ts = getDateTime("HH:mm:ss.SSS");
		if (debug) {
			long cpuid = OSSystemInfo.getThreadCPUId();
			String opener = "";

			if (cpuid >= 0) {
				opener = "(" + cpuid + ")";
			}
			String out = opener + ts + " [" + Thread.currentThread().getName() + ":" + Thread.currentThread().getId()
					+ "] - " + msg;
			System.out.println(out);
		}

		debugMsgs.add("(" + msgSeqNum++ + ") - [" + ts + "] - " + msg);

		if (debugMsgs.size() > MAX_MESSAGES) {
			debugMsgs.remove(0);
		}

	}

	public static byte[] encrypt(byte[] in) {

		byte[] out = new byte[in.length];

		for (int i = 0; i < in.length; i++) {
			int temp = in[i];
			temp ^= 255;
			out[i] = (byte) temp;
		}

		return out;

	}

	public static boolean sleep(long time) {

		return sleep(time, 0);
	}

	public static boolean sleep(long milli, int nanos) {

		try {
			Thread.sleep(milli, nanos);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return true;
		}
		return false;
	}

	public static String format(long val) {
		return format(val, false);
	}

	public static String format(long val, boolean inKB) {
		Object obj[] = new Object[1];
		String postfix = "";
		if (val > ONE_MB && inKB) {
			val = val / ONE_KB;
			postfix = "K";
		}
		obj[0] = new Long(val);
		return String.format("%,d" + postfix, obj);
	}

	public static String formatBytes(long val) {

		if (val < ONE_KB) {
			return format(val);
		}

		if (val < (ONE_MB * 10)) {
			return format(val / ONE_KB) + "K";
		}

		if (val < (ONE_GB * 10)) {
			return format(val / ONE_MB) + "M";
		}

		if (val < (ONE_TB * 10)) {
			return format(val / ONE_GB) + "G";
		}

		return format(val / ONE_TB) + "T";
	}

	public static String format(double val, int precision) {
		Object obj[] = new Object[1];
		obj[0] = new Double(val);
		String format = "%,." + precision + "f";
		return String.format(format, obj);
	}

	public static Color invertColor(Color c) {

		return new Color(c.getRed() ^ 255, c.getGreen() ^ 255, c.getBlue() ^ 255);
	}

	public static String formatException(Throwable t, Object by) {

		StringBuilder sb = new StringBuilder();

		sb.append("Diagnostic Data for Exception ").append(t.getClass().getName()).append("\n");
		if (by != null) {
			sb.append("\tDetected by: " + by.getClass().getName() + "\n");
		}
		sb.append("\tThread: " + Thread.currentThread().getName() + " - [" + Thread.currentThread().getId() + "]\n");
		sb.append("\tLocalized Description: " + t.getLocalizedMessage() + "\n");
		sb.append("\tDescription: " + t.getMessage() + "\n");

		sb.append(formatThrowable(t));

		return sb.toString();
	}

	private static String formatThrowable(Throwable t) {

		StringBuilder sb = new StringBuilder();
		sb.append("\tStackTrace\n");
		int offset = 0;
		StackTraceElement[] ste = t.getStackTrace();

		for (int i = 0; i < ste.length; i++) {
			String steStr = (offset == 0 ? " " : "-");

			sb.append("\t\t(" + steStr + offset++ + ") - " + ste[i].toString() + "\n");

		}
		if (t.getCause() != null) {
			sb.append("\tCause: " + t.getCause().getClass().getCanonicalName() + " - " + t.getCause().getMessage()
					+ "\n");
			sb.append(formatThrowable(t.getCause()));
		}

		return sb.toString();

	}

	public static String getBitMask(long lval, int numCPUs, boolean group) {
		String mask = getBitMask(lval, group);
		while (mask.length() < numCPUs) {
			mask = "0" + mask;
		}
		return mask;
	}

	public static String getBitMask(long lval, boolean group) {
		// byte[] vals = new byte[8];
		String mask = Long.toBinaryString(lval);

		if (!group) {
			return mask;
		}
		char[] bits = mask.toCharArray();
		int len = bits.length;

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < len; i++) {
			sb.append(bits[i]);
			if ((i + 1) % 4 == 0) {
				sb.append(" ");
			}
		}
		return sb.toString();

	}

	@Deprecated
	public static String getBitMask(byte lval) {
		byte val = lval;

		String maskStr = "";

		if ((val & 0x0080) == 128) {
			maskStr += "1";
		} else {
			maskStr += "0";
		}

		if ((val & 0x0040) == 64) {
			maskStr += "1";
		} else {
			maskStr += "0";
		}

		if ((val & 0x0020) == 32) {
			maskStr += "1";
		} else {
			maskStr += "0";
		}

		if ((val & 0x0010) == 16) {
			maskStr += "1";
		} else {
			maskStr += "0";
		}
		// maskStr += " ";
		if ((val & 0x0008) == 8) {
			maskStr += "1";
		} else {
			maskStr += "0";
		}

		if ((val & 0x0004) == 4) {
			maskStr += "1";
		} else {
			maskStr += "0";
		}

		if ((val & 0x0002) == 2) {
			maskStr += "1";
		} else {
			maskStr += "0";
		}

		if ((val & 0x0001) == 1) {
			maskStr += "1";
		} else {
			maskStr += "0";
		}
		return maskStr;
	}

	public static String formatStackTrace() {

		StringBuilder excString = new StringBuilder();

		excString.append("Thread Stack Dump\n");

		excString.append("\tRequesting Thread: ");
		excString.append(Thread.currentThread().getName());
		excString.append(" - [");
		excString.append(Thread.currentThread().getId());
		excString.append("]\n");

		Map<Thread, StackTraceElement[]> stackMap = Thread.getAllStackTraces();
		// Set keys = new HashSet();
		Set<Thread> keys = stackMap.keySet();
		Iterator<Thread> iter = keys.iterator();

		// Loop thru the map
		while (iter.hasNext()) {
			Thread thd = iter.next();
			State st = thd.getState();
			String state = st.toString();
			String type = thd.isDaemon() ? "Deamon" : "Non-Deamon";
			long tid = thd.getId();

			excString.append("\n\tStack Trace for ");
			excString.append(type);
			excString.append(" Thread [");
			excString.append(thd.getName());
			excString.append(":ID=");
			excString.append(thd.getId());
			excString.append("] State(");
			excString.append(state);
			excString.append(") Priority(");
			excString.append(thd.getPriority());
			excString.append(")\n");

			StackTraceElement ste[] = stackMap.get(thd);
			int offset = 0;
			for (int i = 0; i < ste.length; i++) {
				StringBuilder steStr = new StringBuilder();
				steStr.append("\t\t(");
				steStr.append((offset == 0 ? " " : "-"));
				steStr.append(offset++);
				steStr.append(") - ");
				steStr.append(ste[i].toString());
				excString.append(steStr.toString());
				excString.append("\n");
			}

			if (state.equals("BLOCKED")) {
				excString.append("\n\t\t*** ");
				excString.append(getBlockedInfo(tid));
				excString.append(" ***\n");
			}

		}
		return excString.toString();

	}

	public static String formatStackTrace(StackTraceElement[] ste) {

		StringBuilder excString = new StringBuilder();
		int offset = 0;
		for (int i = 0; i < ste.length; i++) {
			StringBuilder steStr = new StringBuilder();
			steStr.append("\t(");
			steStr.append((offset == 0 ? " " : "-"));
			steStr.append(offset++);
			steStr.append(") - ");
			steStr.append(ste[i].toString());
			excString.append(steStr.toString());
			excString.append("\n");
		}
		return excString.toString();

	}

	public static String getBlockedInfo(long threadId) {

		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		ThreadInfo ti = threadBean.getThreadInfo(threadId, Integer.MAX_VALUE);
		long id = ti.getLockOwnerId();
		String lockName = ti.getLockName();
		String lockOwner = ti.getLockOwnerName();

		return "Thread is waiting on lock(" + lockName + ") Owned by " + lockOwner + ":" + id;
	}

	public static void enableDebug(boolean val) {
		debug = val;
	}

	/**
	 * Sets the location of the client component in the center of the parent
	 * 
	 * @param parent
	 * @param child
	 * @param frameWidth
	 * @param frameHeight
	 */
	public static void centerRelativeToParent(Component parent, Component child, int frameWidth, int frameHeight) {
		centerRelativeToParent(parent, child, frameWidth, frameHeight, 0);

	}

	/**
	 * Sets the location of the client component in the center of the parent
	 * 
	 * @param parent
	 * @param child
	 * @param frameWidth
	 * @param frameHeight
	 */
	public static void centerRelativeToParent(Component parent, Component child, int frameWidth, int frameHeight,
			int offset) {

		if (parent != null) {
			Point p = parent.getLocation();
			Dimension pd = parent.getSize();
			int x = p.x + ((pd.width - frameWidth) / 2);
			int y = p.y + ((pd.height - frameHeight) / 2);
			x += offset;
			y += offset;
			child.setLocation(x, y);
		} else {
			Dimension d = new Dimension();
			d = Toolkit.getDefaultToolkit().getScreenSize();
			child.setLocation((((int) d.getWidth() / 2) - (frameWidth / 2)),
					(((int) d.getHeight() / 2) - (frameHeight / 2)));
		}

	}

	public static String toStringFormatter(Object obj) {
		return toStringFormatter(obj, null, null, false);
	}

	public static String toStringFormatter(Object obj, String[] excludes, String heading, boolean showStatic) {

		StringBuilder sb = new StringBuilder();

		Class<?> me = obj.getClass();
		sb.append("\n");

		if (heading == null) {
			sb.append("Member Variables for ").append(me.getName());
		} else {
			sb.append(heading);
		}
		sb.append("\n");

		/*
		 * This will ensure we also get parent class member variables...
		 */
		while (me != null) {

			formatFields(me, sb, obj, excludes, showStatic);
			me = me.getSuperclass();
		}

		return sb.toString();

	}

	private static void formatFields(Class<?> clazz, StringBuilder sb, Object obj, String[] excludes,
			boolean showStatic) {

		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()) && !showStatic) {
				continue;
			}
			boolean excludeField = false;
			if (excludes != null) {
				for (String exclude : excludes) {
					if (field.getName().equals(exclude)) {
						excludeField = true;
						break;
					}
				}
			}
			if (excludeField) {
				continue;
			}
			sb.append("\t");
			sb.append(padToLength(field.getName(), 35, '.'));
			sb.append(": ");
			try {
				field.setAccessible(true);
				Object f = field.get(obj);
				if (field.getType().isArray()) {
					if (f == null) {
						sb.append("null");
					} else {
						int len = Array.getLength(f);
						for (int i = 0; i < len; i++) {
							sb.append(" [").append(i).append("] ");
							Object occ = Array.get(f, i);
							sb.append((occ == null ? "null" : occ.toString()));
						}
					}

				} else {
					sb.append((f == null ? "null" : f.toString()));
				}
			} catch (Exception e) {
				sb.append("Exception Getting Object, Error is ");
				sb.append(e.getMessage());
				sb.append(", Exception is ");
				sb.append(e.getClass().getName());
			}
			sb.append("\n");

		}
	}

	/**
	 * Simple method to pad a string to a specified length
	 * 
	 * @param src
	 * @param len
	 * @param pad
	 * @return
	 */
	private static String padToLength(String src, int len, char pad) {

		if (src.length() >= len) {
			return src;
		}
		int diff = len - src.length();

		StringBuilder ret = new StringBuilder(src);

		for (int i = 0; i < diff; i++) {
			ret.append(pad);
		}
		return ret.toString();
	}

	public static void showInterfaces(String[] args) throws Exception {

		boolean activeOnly = false;
		boolean showLoopback = true;
		boolean showIP4 = true;
		boolean showIP6 = true;
		System.out.println("[IPInfo] Network Interface Info Display Utility V1.0.0");
		// System.out.println("\tShowing ");

		for (String opts : args) {

			if ("/h".equals(opts)) {
				System.out.println("\nIPInfo Help:");
				System.out.println("\tUsage:");
				System.out.println("\t\tIPInfo <options>");
				System.out.println("\tOptions (1 or more, Must be separated with a space):");
				System.out.println("\t\t/a  - Show Only Enabled (Active) Interfaces");
				System.out.println("\t\t/nl - Do Not Show Loopback");
				System.out.println("\t\t/n4 - Do Not Show IPV4 Addresses");
				System.out.println("\t\t/n6 - Do Not Show IPV6 Addresses");
				System.out.println("\t\t/h  - Show This Help");
				return;
			}

			if ("/a".equals(opts)) {
				activeOnly = true;
			}
			if ("/nl".equals(opts)) {
				showLoopback = false;
			}
			if ("/n4".equals(opts)) {
				showIP4 = false;
			}
			if ("/n6".equals(opts)) {
				showIP6 = false;
			}
		}

		// System.out.println("\n\tOptions Selected:");
		if (activeOnly) {
			System.out.println("\tEnabled Interfaces");
		}
		if (!showLoopback) {
			System.out.println("\t\tDo Not Display Loopback");
		}
		if (!showIP4) {
			System.out.println("\t\tDo Not Display IP V4 Addresses");
		}
		if (!showIP6) {
			System.out.println("\t\tDo Not Display IP V6 Addresses");
		}
		Enumeration<NetworkInterface> inter = NetworkInterface.getNetworkInterfaces();

		while (inter.hasMoreElements()) {
			NetworkInterface ni = inter.nextElement();

			if (activeOnly) {
				if (!ni.isUp()) {
					continue;
				}
				if (ni.isVirtual()) {
					continue;
				}

			}

			if (!showLoopback && ni.isLoopback()) {
				continue;
			}
			byte[] mac = ni.getHardwareAddress();

			String macadr = "Unknown mac address";
			if (mac != null)
				macadr = encodeStr(mac, "-");
			System.out.println("\nInterface: " + ni.getName() + " (" + ni.getDisplayName() + ")");
			String status = (ni.isUp() ? "Enabled" : "Disabled");
			boolean isLoop = ni.isLoopback();
			String netType = "Virtual";
			if (ni.getParent() == null)
				netType = "Physical";
			if (mac != null)
				System.out.println("\t" + netType + " Address: " + macadr + "");
			System.out.println("\tStatus: " + status);
			System.out.println("\tMax MTU Size: " + ni.getMTU());
			System.out.println("\tSupports Multicast: " + ni.supportsMulticast());
			if (isLoop)
				System.out.println("\tLoopback Interface");
			Enumeration<InetAddress> addr = ni.getInetAddresses();
			while (addr.hasMoreElements()) {
				InetAddress ia = addr.nextElement();

				String type = null;
				if (ia instanceof Inet6Address) {
					type = "IPV6";
					if (!showIP6) {
						continue;
					}
				} else {
					type = "IPV4";
					if (!showIP4) {
						continue;
					}
				}
				System.out.println("\t" + type + " Address: " + ia.toString());

			}
		}
	}

	public static String encodeStr(byte[] bytes, String delim) {

		StringBuffer encodedStr = new StringBuffer();

		for (int i = 0; i < bytes.length; i++) {
			int newInt = (bytes[i] & 0xf0) + (bytes[i] & 0x0f);

			String currByteStr = Integer.toHexString(newInt);
			if (currByteStr.length() == 1) {

				encodedStr.append("0");
			}

			encodedStr.append(currByteStr);
			if (i < bytes.length - 1)
				encodedStr.append(delim);
		}
		return encodedStr.toString();
	}

	public static byte[] compress(byte[] content) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
		gzipOutputStream.write(content);
		gzipOutputStream.close();

		// System.out.printf("Compression ratio %f\n", (1.0f *
		// content.length/byteArrayOutputStream.size()));
		return byteArrayOutputStream.toByteArray();
	}

	public static byte[] decompress(byte[] contentBytes) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		copyStream(new GZIPInputStream(new ByteArrayInputStream(contentBytes)), out);

		return out.toByteArray();
	}

	public static long copyStream(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * This method will obtain the process id of the JVM based on the JVM
	 * instance name
	 * 
	 * @return The process id.
	 */
	public static int getProcessID() {

		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		String jvmName = runtimeBean.getName();

		int idx = jvmName.indexOf("@");

		return Integer.parseInt(jvmName.substring(0, idx));

	}

	/**
	 * Return a list of debug messages
	 * 
	 * @return
	 */
	public static List<String> getDebugMessages() {
		return debugMsgs;
	}

	/**
	 * Return a list of debug messages
	 * 
	 * @return
	 */
	public static void clearDebugMessages() {
		debugMsgs.clear();
	}

	/**
	 * Utility method to determine if the OS is Microsoft Windows.
	 * 
	 * @return is the OS is Windows, false if not.
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	/**
	 * Returns a List of InetAddresses eligable for Multicast
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<InetAddress> getMulitcastAddress() throws IOException {

		List<InetAddress> addrs = new ArrayList<InetAddress>();

		Enumeration<NetworkInterface> inter = NetworkInterface.getNetworkInterfaces();

		while (inter.hasMoreElements()) {
			NetworkInterface ni = inter.nextElement();

			if (!ni.supportsMulticast() || !ni.isUp()) {
				continue;
			}

			Enumeration<InetAddress> addr = ni.getInetAddresses();
			while (addr.hasMoreElements()) {
				InetAddress ia = addr.nextElement();

				// if (ia instanceof Inet4Address) {

				debugMsg("Adding Interface " + ia.toString() + " to List");
				addrs.add(ia);

				// }
			}

		}

		return addrs;

	}

	public static OperatingSystemMXBean getSunOSBean() {

		OperatingSystemMXBean osmb = null;
		MBeanServer mbsc = ManagementFactory.getPlatformMBeanServer();

		try {
			ObjectName objectname = new ObjectName("java.lang:type=OperatingSystem");
			if (osmb == null && mbsc.isInstanceOf(objectname, "com.sun.management.OperatingSystemMXBean"))
				osmb = ManagementFactory.newPlatformMXBeanProxy(mbsc, "java.lang:type=OperatingSystem",
						OperatingSystemMXBean.class);
		} catch (Exception e) {
			Logger.getLogger().logException(e, null);
			return null;
		}

		return osmb;
	}
}
