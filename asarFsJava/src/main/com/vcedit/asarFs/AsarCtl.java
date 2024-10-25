package com.vcedit.asarFs;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class AsarCtl {
	static class AsarItem {
		int start = 0;
		int len = 0;
	}

	AsarFile asarFile = null;
	JSONObject asarObj = null;
	Map<String, String[]> mapDir = new HashMap<>();
	Map<String, AsarItem> mapFile = new HashMap<>();
	Map<String, String> mapLink = new HashMap<>();

	public boolean isAsarExist() {
		return (asarObj != null);
	}

	public JSONObject getAsarObj() {
		return asarObj;
	}

	void ergObj(JSONObject obj, String path, int rootOffset) {
		try {
			if (!obj.has("files")) {
				return;
			}
			JSONObject fileObj = obj.getJSONObject("files");
			Iterator<String> itKeys = fileObj.keys();
			List<String> lstDir = new ArrayList<>();
			while (itKeys.hasNext()) {
				String key = itKeys.next();
				String keyLower = key.toLowerCase();
				String subPath = path + (path.equals("") ? "" : "/") + keyLower;
				//String subPath = path + "/" + key;
				try {
					JSONObject child = fileObj.getJSONObject(key);
					// is directory
					if (child.has("files")) {
						ergObj(child, subPath, rootOffset);
						lstDir.add(key);
						continue;
					}
					if (child.has("link")) {
						try {
							String str = child.getString("link").replaceAll("[\\\\/]+", "/");
							mapLink.put(subPath, str);
							lstDir.add(key);
						} catch (Exception ignored) { }
						continue;
					}
					// is file
					try {
						AsarItem it = new AsarItem();
						it.start = rootOffset + child.getInt("offset");
						it.len = child.getInt("size");
						mapFile.put(subPath, it);
						lstDir.add(key);
					} catch (Exception ignored) { }
				} catch (Exception ignored) { }
			}
			String[] arrDir = new String[lstDir.size()];
			lstDir.toArray(arrDir);
			mapDir.put(path, arrDir);
		} catch (Exception ignored) { }
	}

	void loadStream(InputStream input) {
		asarObj = null;
		mapFile = new HashMap<>();
		mapDir = new HashMap<>();
		mapLink = new HashMap<>();
		try {
			final int headLen = 16;
			final int posSig = 0;
			final int posJsonData = 12;

			int fileSize = input.available();

			byte[] arrHead = new byte[headLen];
			input.read(arrHead, 0, headLen);

			ByteBuffer bufHead = ByteBuffer.wrap(arrHead);
			bufHead.order(ByteOrder.LITTLE_ENDIAN);
			int sigHead = bufHead.getInt(posSig);
			if (sigHead != 0x4) {
				return;
			}

			int jsonLen = bufHead.getInt(posJsonData);
			if (jsonLen > fileSize) {
				return;
			}
			int rootOffset = headLen + jsonLen;
			// 4 byte align
			rootOffset = rootOffset + ((4 - (rootOffset % 4)) % 4);

			byte[] arrJsonData = new byte[jsonLen];
			input.read(arrJsonData);
			String str = new String(arrJsonData, StandardCharsets.UTF_8);

			asarObj = new JSONObject(str);
			ergObj(asarObj, "", rootOffset);

		} catch (Exception ignored) { }

		try {
			input.close();
		} catch (Exception ignored) {}
	}
	
	static String formatPath(String path) {
		return path.trim().toLowerCase().replaceAll("[\\\\/]+", "/").replaceAll("[/]+$", "");
	}

	public String[] dirList(String path) {
		path = formatPath(path);
		if (!mapDir.containsKey(path)) {
			return new String[0];
		}
		return mapDir.get(path);
	}

	public String fileType(String path) {
		path = formatPath(path);
		if (mapDir.containsKey(path)) {
			return "dir";
		}
		if (mapFile.containsKey(path)) {
			return "file";
		}
		if (mapLink.containsKey(path)) {
			return "link";
		}
		return "";
	}

	public boolean isDirExist(String path) {
		path = formatPath(path);
		if (mapDir.containsKey(path)) {
			return true;
		}
		return false;
	}

	public boolean isFileExist(String path) {
		path = formatPath(path);
		if (mapFile.containsKey(path)) {
			return true;
		}
		return false;
	}

	public boolean isLinkExist(String path) {
		path = formatPath(path);
		if (mapLink.containsKey(path)) {
			return true;
		}
		return false;
	}

	public String getLinkPath(String path) {
		path = formatPath(path);
		if (mapLink.containsKey(path)) {
			return mapLink.get(path);
		}
		return "";
	}

	public long fileSize(String path) {
		if (!mapFile.containsKey(path)) {
			return 0;
		}
		AsarItem it = mapFile.get(path);
		return it.len;
	}

	public void load(AsarFile _file) {
		asarFile = _file;
		try {
			InputStream input = asarFile.getStream();
			if (input == null) {
				return;
			}
			loadStream(input);
		} catch (Exception ignored) { }
	}

	public InputStream getFileStream(String path) {
		if (asarObj == null) {
			return null;
		}

		if (!mapFile.containsKey(path)) {
			return null;
		}

		AsarItem it = mapFile.get(path);

		try {
			InputStream input = asarFile.getStream();
			return new SplitInputStream(input, it.start, it.len);
		} catch (Exception ignored) {}
		return null;
	}
}
