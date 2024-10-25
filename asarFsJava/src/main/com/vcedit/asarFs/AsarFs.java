package com.vcedit.asarFs;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public final class AsarFs {
	/** asar header cache time(ms), default is 30 minutes */
	public static long cacheTimeMs = 30 * 60 * 1000;

	static long lastCheckTime = 0;

	static class AsarFileItem {
		public long visitTime = 0;
		public String path = "";
		public AsarCtl ctl = null;

		public String physicsPath = "";
		public int physicsStart = 0;
		public long physicsLen = 0;
		public float physicsMtimeMs = 0;
	}

	static class AsarFileInfo {
		public AsarFileItem asarFile = null;

		public String subPath = "";
		public AsarFileStatus subFileStatus = null;
	}
	
	static Map<String, AsarFileItem> mapFileItem = new HashMap<>();
	static Map<String, AsarFile> mapVirtualAsarFile = new HashMap<>();

	static Pattern regFormatPath = null;

	private AsarFs() {

	}

	/**
	 * format path,<br>
	 *     ./.                                  =&gt; "",<br>
	 *     ../                                  =&gt; "",<br>
	 *     /aaa                                 =&gt; /aaa,<br>
	 *     ./aaa/ /bbb/\\\\ccc/ .. / . /ddd/    =&gt; aaa/ /bbb/ddd
	 * @param path path
	 * @return format path
	*/
	public static String formatPath(String path) {
		path = path.trim().toLowerCase();

		if (path.equals("")) {
			return path;
		}
		String[] arr = path.split("[\\\\/]+", -1);
		StringBuilder sb = new StringBuilder();
		int ignoreCount = 0;
		for (int i = arr.length - 1; i >= 0; --i) {
			String str = arr[i].trim();
			if (str.equals("")) {
				if (i == 0) {
					sb.insert(0, "/");
					continue;
				}
				if (sb.length() > 0) {
					sb.insert(0, "/");
				}
				sb.insert(0, arr[i]);
				continue;
			}
			if (str.equals(".")) {
				continue;
			}
			if (str.equals("..")) {
				++ignoreCount;
				continue;
			}
			if (ignoreCount > 0) {
				--ignoreCount;
				continue;
			}
			if (sb.length() > 0) {
				sb.insert(0, "/");
			}
			sb.insert(0, str);
		}
		return sb.toString();
	}
	
	static void addToCache(AsarFileItem it) {
		if (cacheTimeMs <= 0) {
			return;
		}
		it.visitTime = new Date().getTime();
		mapFileItem.put(it.path, it);
	}
	
	static void clearCacheByTime() {
		long time = new Date().getTime();

		Iterator<Entry<String, AsarFileItem>> itr = mapFileItem.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, AsarFileItem> ent = itr.next();
			String path = ent.getKey();
			AsarFileItem it = ent.getValue();
			
			if (time - it.visitTime > cacheTimeMs) {
				itr.remove();
			}
		}
	}
	
	static AsarFileItem getFromCache(String path) {
		if (!mapFileItem.containsKey(path)) {
			return null;
		}
		AsarFileItem it = mapFileItem.get(path);

		long time = new Date().getTime();
		long checkTime = cacheTimeMs;
		it.visitTime = time;

		if (lastCheckTime > time) {
			lastCheckTime = time;
		}

		if (time - lastCheckTime < checkTime) {
			return it;
		}
		lastCheckTime = time;
		clearCacheByTime();

		return it;
	}
	
	static AsarFileItem loadAsarFromAsarFileMd(AsarFile asarFile, String path) {
		AsarCtl ctl = new AsarCtl();
		ctl.load(asarFile);
		if (!ctl.isAsarExist()) {
			return null;
		}

		AsarFileItem it = new AsarFileItem();
		it.ctl = ctl;
		it.path = path;
		it.physicsMtimeMs = asarFile.mtimeMs;
		addToCache(it);

		return it;
	}

	static AsarFileItem loadAsarFromPath(String path) {
		File file = new File(path);
		if (!file.exists() || file.isDirectory()) {
			return null;
		}

		final String pathTmp = path;

		AsarFile asarFile = new AsarFile() {
			@Override
			public InputStream getStream() {
				try {
					return new FileInputStream(pathTmp);
				} catch (Exception ignored) { }
				return null;
			}
		};

		AsarFileItem it = loadAsarFromAsarFileMd(asarFile, path);
		it.physicsPath = path;
		it.physicsLen = file.length();
		it.physicsMtimeMs = file.lastModified();
		
		return it;
	}

	static AsarFileItem loadAsarFromVirtual(String path) {
		if (!mapVirtualAsarFile.containsKey(path)) {
			return null;
		}
		AsarFile asarFile = mapVirtualAsarFile.get(path);

		AsarFileItem it = loadAsarFromAsarFileMd(asarFile, path);
		return it;
	}

	static AsarFileItem loadAsarFromAsar(AsarFileItem asarItem, String subPath) {
		String[] arr = subPath.split("/");

		if (!asarItem.ctl.isFileExist(subPath)) {
			return null;
		}

		final AsarFileItem asarItemTmp = asarItem;
		final String subPathTmp = subPath;

		AsarFile asarFile = new AsarFile() {
			@Override
			public InputStream getStream() {
				return asarItemTmp.ctl.getFileStream(subPathTmp);
			}
		};

		String path = asarItem.path + "/" + subPath;
		AsarFileItem it = loadAsarFromAsarFileMd(asarFile, path);
		it.physicsPath = asarItem.physicsPath;
		it.physicsLen = asarItem.physicsLen;
		it.physicsMtimeMs = asarItem.physicsMtimeMs;
		
		return it;
	}

	static boolean isAsarFile(String path) {
		String strTagAsar = ".asar";
		int idx = path.lastIndexOf(strTagAsar);
		return idx >= 0 && idx == path.length() - strTagAsar.length();
	}

	static String addPath(String path, String subPath) {
		if (!path.equals("")) {
			path += "/";
		}
		path += subPath;
		return path;
	}
	
	static String jonArr(String[] arr, String separator, int start) {
		return jonArr(arr, separator, start, -1);
	}

	static String jonArr(String[] arr, String separator, int start, int len) {
		int end = start + len;
		if (len < 0) {
			end = arr.length;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; ++i) {
			if (i != start) {
				sb.append(separator);
			}
			sb.append(arr[i]);
		}
		return sb.toString();
	}
	
	public static AsarFileInfo getAsarFileInfo(String path) {
		return getAsarFileInfo(path, false);
	}

	public static AsarFileInfo getAsarFileInfo(String path, boolean isKeepLastAsarFile) {
		path = formatPath(path);
		String[] arr = path.split("/", -1);
		if (arr.length <= 0) {
			return null;
		}

		String fullPath = "";
		String physicsPath = "";
		String asarSubPath = "";
		AsarFileItem asarTmp = null;
		JSONObject asarObj = null;

		int asarStartIdx = -1;

		// find physics asar file
		for (int i = 0; i < arr.length; ++i) {
			String str = arr[i];
			fullPath = addPath(fullPath, str);
			physicsPath = addPath(physicsPath, str);
			if (!isAsarFile(str)) {
				continue;
			}
			AsarFileItem tmp = getFromCache(fullPath);
			if (tmp != null) {
				asarTmp = tmp;
				asarStartIdx = i + 1;
				break;
			}

			File file = new File(physicsPath);
			if (!file.exists()) {
				asarTmp = loadAsarFromVirtual(physicsPath);
				if (asarTmp != null) {
					asarStartIdx = i + 1;
					break;
				}
				return null;
			}
			if (file.isDirectory()) {
				continue;
			}
			asarTmp = loadAsarFromPath(physicsPath);
			if (asarTmp != null) {
				asarStartIdx = i + 1;
				break;
			}
		}
		if (asarTmp == null) {
			return null;
		}

		// find last asar file from asar file
		int lastAsarStartIdx = asarStartIdx;
		HashMap<String, String> mapLinkPath = new HashMap<>();
		boolean isLastLink = false;
		for (int i = asarStartIdx; i < arr.length; ++i) {
			String str = arr[i];
			fullPath = addPath(fullPath, str);
			asarSubPath = addPath(asarSubPath, str);
			
			if (asarTmp.ctl.isLinkExist(asarSubPath)) {
				if (!isLastLink) {
					if (i == arr.length - 1) {
						isLastLink = true;
					} else {
						isLastLink = false;
					}
				}
				String linkPath = asarTmp.ctl.getLinkPath(asarSubPath);
				linkPath = formatPath(linkPath);
				if (mapLinkPath.containsKey(linkPath)) {
					return null;
				}
				mapLinkPath.put(linkPath, "");
				String subPath = linkPath + "/" + jonArr(arr, "/", i + 1);
				subPath = formatPath(subPath);
				if (subPath.equals("")) {
					break;
				}
				arr = subPath.split("/", -1);
				i = -1;
				fullPath = asarTmp.path;
				asarSubPath = "";
				lastAsarStartIdx = 0;
				continue;
			}

			if (!isAsarFile(str)) {
				continue;
			}
			
			AsarFileItem tmp = getFromCache(fullPath);
			if (tmp != null) {
				if (isKeepLastAsarFile && i == (arr.length - 1)) {
					break;
				}
				asarTmp = tmp;
				asarSubPath = "";
				lastAsarStartIdx = i + 1;
				continue;
			}
			String type = asarTmp.ctl.fileType(asarSubPath);
			if (type.equals("dir")) {
				continue;
			}
			if (!type.equals("file")) {
				return null;
			}
			if (isKeepLastAsarFile && i == (arr.length - 1)) {
				break;
			}
			asarTmp = loadAsarFromAsar(asarTmp, asarSubPath);
			if (asarTmp == null) {
				return null;
			}
			asarSubPath = "";
			lastAsarStartIdx = i + 1;
		}

		if (asarTmp == null){
			return null;
		}

		AsarFileStatus status = new AsarFileStatus();
		AsarFileInfo info = new AsarFileInfo();
		info.asarFile = asarTmp;
		info.subFileStatus = status;
		status.mtimeMs = asarTmp.physicsMtimeMs;
		status.atimeMs = status.ctimeMs = status.mtimeMs;

		if (lastAsarStartIdx == arr.length) {
			status.isDirectory = true;
			status.isSymbolicLink = isLastLink;
			return info;
		}

		asarSubPath = jonArr(arr, "/", lastAsarStartIdx);

		String type = asarTmp.ctl.fileType(asarSubPath);
		status.isDirectory = type.equals("dir");
		status.isFile = type.equals("file");
		status.isSymbolicLink = isLastLink;

		if (type == "" || type == "link") {
			return null;
		}

		if (status.isFile) {
			status.size = asarTmp.ctl.fileSize(asarSubPath);
		}

		info.subPath = asarSubPath;
		return info;
	}
	
	/**
	 * map embed asar file or stream to virtual path, priority lower than physical location
	 * @param virtualAsarFile virtual asar file map
	*/
	public static void setVirtualAsarFile(Map<String, AsarFile> virtualAsarFile) {
		mapVirtualAsarFile = new HashMap<>();
		mapFileItem = new HashMap<>();

		if (virtualAsarFile == null) {
			return;
		}

		Iterator<Entry<String, AsarFile>> itr = virtualAsarFile.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, AsarFile> ent = itr.next();
			String key = formatPath(ent.getKey());
			AsarFile value = ent.getValue();
			mapVirtualAsarFile.put(key, value);
		}
	}

	/**
	 * check if the file exists
	 * @param path file path
	 * @return returns true if the path exists, false otherwise
	*/
	public static boolean existsSync(String path) {
		path = formatPath(path);
		if (!isAsarFile(path)) {
			File file = new File(path);
			if (file.exists()) {
				return true;
			}
		}
		AsarFileInfo info = getAsarFileInfo(path);
		if (info == null) {
			return false;
		}
		return true;
	}

	static AsarFileStatus baseStatSync(String path) {
		path = formatPath(path);
		if (!isAsarFile(path)) {
			File file = new File(path);
			if (file.exists()) {
				AsarFileStatus status = new AsarFileStatus();
				status.isFile = file.isFile();
				status.isDirectory = file.isDirectory();
				status.size = status.isFile ? file.length() : 0;
				status.mtimeMs = file.lastModified();
				status.atimeMs = status.ctimeMs = status.mtimeMs;
				return status;
			}
		}
		AsarFileInfo info = getAsarFileInfo(path);
		if (info == null) {
			return null;
		}
		return info.subFileStatus;
	}

	/**
	 * retrieves file status for the path
	 * @param path file path
	 * @return if file not exist, return null,
	 *     symbolic link be recognized as normal file or directory,<br>
	 *     "isSymbolicLink" is alwayse false
	*/
	public static AsarFileStatus statSync(String path) {
		AsarFileStatus status = baseStatSync(path);
		if (status == null) {
			return status;
		}
		status.isSymbolicLink = false;
		return status;
	}

	/**
	 * retrieves file status for the symbolic link referred to by path
	 * @param path file path
	 * @return if file not exist, return null,<br>
	 *     if is symbolic link, attribute "isFile" "isDirectory" is false, "size" is 0
	*/
	public static AsarFileStatus lstatSync(String path) {
		AsarFileStatus status = baseStatSync(path);
		if (status == null) {
			return status;
		}
		if (status.isSymbolicLink) {
			status.isDirectory = false;
			status.isFile = false;
			status.size = 0;
		}
		return status;
	}

	/**
	 * reads the contents of the directory
	 * @param path file path
	 * @return if direcotry not exist, return empty array
	*/
	public static String[] readdirSync(String path) {
		path = formatPath(path);
		if (!isAsarFile(path)) {
			File file = new File(path);
			if (file.exists()) {
				if (!file.isDirectory()) {
					return new String[0];
				}
				List<String> lstName = new ArrayList<>();
				for (File it : file.listFiles()) {
					lstName.add(it.getName());
				}
				String[] rst = new String[lstName.size()];
				lstName.toArray(rst);
				return rst;
			}
		}
		AsarFileInfo info = getAsarFileInfo(path);
		if (info == null) {
			return new String[0];
		}
		AsarFileStatus status = info.subFileStatus;
		if (!status.isDirectory) {
			return new String[0];
		}
		return info.asarFile.ctl.dirList(info.subPath);
	}

	/**
	 * return file stream
	 * @param path file path
	 * @return if file not exist or is directory, return null
	*/
	public static InputStream getFileStream(String path) {
		path = formatPath(path);
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory()) {
				return null;
			}
			try {
				return new FileInputStream(path);
			} catch (Exception ignored) {
				return null;
			}
		}
		AsarFileInfo info = getAsarFileInfo(path, true);
		if (info == null) {
			return null;
		}
		return info.asarFile.ctl.getFileStream(info.subPath);
	}

	/**
	 * read file as utf8 string
	 * @param path file path
	 * @return if file not exist or is directory, return empty string
	*/
	public static String readFileStringSync(String path) {
		return readFileStringSync(path, StandardCharsets.UTF_8);
	}

	/**
	 * read file as string by charset
	 * @param path file path
	 * @param charset charset
	 * @return if file not exist or is directory, return empty string
	*/
	public static String readFileStringSync(String path, Charset charset) {
		byte[] buf = readFileByteSync(path);
		return new String(buf, charset);
	}

	/**
	 * read bin file
	 * @param path file path
	 * @return if file not exist or is directory, return empty array
	*/
	public static byte[] readFileByteSync(String path) {
		InputStream is = getFileStream(path);
		if (is == null) {
			return new byte[0];
		}
		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[10240];

		try {
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buf.write(data, 0, nRead);
			}
		} catch (Exception ignored) {}

		try {
			is.close();
		} catch (Exception ignored) {}

		return buf.toByteArray();
	}
}
