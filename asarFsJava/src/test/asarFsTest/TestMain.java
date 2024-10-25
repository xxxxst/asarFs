package asarFsTest;

import com.google.gson.Gson;
import com.vcedit.asarFs.AsarFile;
import com.vcedit.asarFs.AsarFileStatus;
import com.vcedit.asarFs.AsarFs;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMain {
	static Gson gson = new Gson();
	static int testCount = 0;
	static int errCount = 0;
	static float asarMtime = 0;
	
	static void existsSyncTest(boolean expect, String path) {
		testEqual("[existsSync() err] " + path, expect, AsarFs.existsSync(path));
	}

	static void statSyncTest(AsarFileStatus expect, String path) {
		testObjEqual("[statSync() err] " + path, expect, AsarFs.statSync(path), new StatusCompare());
	}

	static void lstatSyncTest(AsarFileStatus expect, String path) {
		testObjEqual("[lstatSync() err] " + path, expect, AsarFs.lstatSync(path), new StatusCompare());
	}

	static void readdirSyncTest(String[] expect, String path) {
		testArrSortEqual("[readdirSync() err] " + path, expect, AsarFs.readdirSync(path));
	}

	static void readFileStringSyncTest(String expect, String path) {
		testEqual("[readFileStringSync() err] " + path, expect, AsarFs.readFileStringSync(path));
	}

	static void formatPathTest(String expect, String path) {
		testEqual("[formatPath() err] " + path, expect, AsarFs.formatPath(path));
	}

	static void streamExistTest(boolean expect, String path) {
		testEqual("[streamExistTest() err] " + path, expect, AsarFs.getFileStream(path) != null);
	}

    public static void main(String[] args) {
		try {
			File file = new File("./test/app.asar");
			asarMtime = file.lastModified();
		} catch(Exception ignored) { }

		// AsarFs.cacheTime = 0;
		
		// AsarFs.getAsarFileInfo("./test/app.asar/ccc.asar");
		// System.out.println(AsarFs.readFileStringSync("./test/app.asar/ccc.asar"));
		// if (true) {
		// 	return;
		// }

        log("test start");
		System.out.println("");

		formatPathTest("", "");
		formatPathTest("/", "/");
		formatPathTest("", "./.");
		formatPathTest("", "../");
		formatPathTest("/", "/..");
		formatPathTest("/", "/.");
		formatPathTest("/aaa", "/aaa");
		formatPathTest("/aaa/ccc/ggg/ddd.txt", "/aaa\\/bbb/../ccc/fff/../ggg/./\\ddd.txt");
        formatPathTest("ccc/ddd.txt", "aaa/bbb/../ .. /ccc/./ddd.txt/");
        formatPathTest("aaa/bbb/ccc/ddd.txt", "../aaa/bbb// ccc / . /./ddd.txt/.");
        formatPathTest("aaa/ /bbb/ccc/ddd.txt", "./aaa/ /bbb/\\\\ccc/././ddd.txt/.");
        formatPathTest("aaa/ /bbb/ddd", "./aaa/ /bbb/\\\\ccc/ .. / . /ddd/");
        formatPathTest("ccc/ddd.txt", ".\\/aaa/bbb\\/../../../ccc/././ddd.txt");

		existsSyncTest(false, "");
		existsSyncTest(true, "./test/");
		existsSyncTest(false, "./test/app/unknown.txt");
		existsSyncTest(true, "./test/app.asar");
		existsSyncTest(true, "./test/app.asar/\\bbb.txt");
		existsSyncTest(true, "./test/app.asar/\\bbbLink.txt");
		existsSyncTest(true, "./test/app.asar/\\bbblink.txt");
		existsSyncTest(false, "./test/app.asar/unknown.txt");
		existsSyncTest(false, "./test/app.asar/ccc/unknown/");
		existsSyncTest(false, "./test/app.asar/ccc/unknown/unknown.txt");
		existsSyncTest(false, "./test/\\app.asar/ccc/unknown.txt");
		existsSyncTest(false, "./test/\\app.asar/cccLink/unknown.txt");
		existsSyncTest(true, "./test/app.asar/bbb.asar\\size");
		existsSyncTest(true, "./test/app.asar/ccc.asar\\ddd/\\");
		existsSyncTest(true, "./test/app.asar/ccc.asar\\eee.txt");
		existsSyncTest(true, "./test/app.asar/ccc/ddd\\\\/fff/hhh.txt");
		existsSyncTest(true, "./test/app.asar/cccLink/ddd\\\\/fff/hhh.txt");
		existsSyncTest(true, "./test/app.asar/ccclink/ddd\\\\/fff/hhh.txt");
		existsSyncTest(true, "./test/app.asar/cccLink/dddLink\\\\/gggLink.txt");
		existsSyncTest(false, "./test/appVirtual.asar/cccLink/dddLink\\\\/gggLink.txt");
		existsSyncTest(false, "@resource/test/appVirtual.asar/cccLink/dddLink\\\\/gggLink.txt");

		statSyncTest(null, "");
		statSyncTest(stPhyFile("./test/"), "./test/");
		statSyncTest(null, "./test/unknown.txt");
		statSyncTest(stDire(), "./test/app.asar");
		statSyncTest(stFile(2), "./test/app.asar/\\bbb.txt");
		statSyncTest(stFile(2), "./test/app.asar/\\bbbLink.txt");
		statSyncTest(null, "./test/app.asar/unknown.txt");
		statSyncTest(null, "./test/app.asar/ccc/unknown/");
		statSyncTest(null, "./test/app.asar/ccc/unknown/unknown.txt");
		statSyncTest(null, "./test/\\app.asar/ccc/unknown.txt");
		statSyncTest(null, "./test/\\app.asar/cccLink/unknown.txt");
		statSyncTest(stDire(), "./test/app.asar/bbb.asar\\size");
		statSyncTest(stDire(), "./test/app.asar/ccc.asar\\ddd/\\");
		statSyncTest(stFile(8), "./test/app.asar/ccc/ddd\\\\/fff/hhh.txt");
		statSyncTest(stFile(8), "./test/app.asar/cccLink/ddd\\\\/fff/hhh.txt");
		statSyncTest(stFile(8), "./test/app.asar/ccclink/ddd\\\\/fff/hhh.txt");
		statSyncTest(stFile(7), "./test/app.asar/cccLink/dddLink\\\\//gggLink.txt");
		statSyncTest(null, "./test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");
		statSyncTest(null, "@resource/test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");

		lstatSyncTest(null, "");
		lstatSyncTest(stPhyFile("./test/"), "./test/");
		lstatSyncTest(null, "./test/unknown.txt");
		lstatSyncTest(stDire(), "./test/app.asar");
		lstatSyncTest(stFile(2), "./test/app.asar/\\bbb.txt");
		lstatSyncTest(stLink(), "./test/app.asar/\\bbbLink.txt");
		lstatSyncTest(null, "./test/app.asar/unknown.txt");
		lstatSyncTest(null, "./test/app.asar/ccc/unknown/");
		lstatSyncTest(null, "./test/app.asar/ccc/unknown/unknown.txt");
		lstatSyncTest(null, "./test/\\app.asar/ccc/unknown.txt");
		lstatSyncTest(null, "./test/\\app.asar/cccLink/unknown.txt");
		lstatSyncTest(stDire(), "./test/app.asar/bbb.asar\\size");
		lstatSyncTest(stDire(), "./test/app.asar/ccc.asar\\ddd/\\");
		lstatSyncTest(stFile(8), "./test/app.asar/ccc/ddd\\\\/fff/hhh.txt");
		lstatSyncTest(stFile(8), "./test/app.asar/cccLink/ddd\\\\/fff/hhh.txt");
		lstatSyncTest(stFile(8), "./test/app.asar/ccclink/ddd\\\\/fff/hhh.txt");
		lstatSyncTest(stLink(), "./test/app.asar/cccLink/dddLink\\\\//gggLink.txt");
		lstatSyncTest(null, "./test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");
		lstatSyncTest(null, "@resource/test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");
		
		readdirSyncTest(new String[0], "");
		readdirSyncTest(readdirSync("./test/"), "./test/");
		readdirSyncTest(new String[0], "./test/unknown.txt");
		readdirSyncTest(new String[] { "Aaa", "AaaLink", "bbb.asar", "ccc", "cccLink", "files", "bbb.txt", "bbbLink.txt", "ccc.asar" }, "./test/app.asar");
		readdirSyncTest(new String[0], "./test/app.asar/\\bbb.txt");
		readdirSyncTest(new String[0], "./test/app.asar/\\bbbLink.txt");
		readdirSyncTest(new String[0], "./test/app.asar/unknown.txt");
		readdirSyncTest(new String[0], "./test/app.asar/ccc/unknown/");
		readdirSyncTest(new String[0], "./test/app.asar/ccc/unknown/unknown.txt");
		readdirSyncTest(new String[0], "./test/\\app.asar/ccc/unknown.txt");
		readdirSyncTest(new String[0], "./test/\\app.asar/cccLink/unknown.txt");
		readdirSyncTest(new String[] { ".gitkeep", "bbb2.txt" }, "./test/app.asar/bbb.asar\\size");
		readdirSyncTest(new String[] { "fff", "ggg.txt", "gggLink.txt" }, "./test/app.asar/ccc.asar\\ddd/\\");
		readdirSyncTest(new String[0], "./test/app.asar/ccc/ddd\\\\/fff/hhh.txt");
		readdirSyncTest(new String[0], "./test/app.asar/cccLink/ddd\\\\/fff/hhh.txt");
		readdirSyncTest(new String[0], "./test/app.asar/ccclink/ddd\\\\/fff/hhh.txt");
		readdirSyncTest(new String[0], "./test/app.asar/cccLink/dddLink\\\\//gggLink.txt");
		readdirSyncTest(new String[0], "./test/appVirtual.asar/bbb.asar\\size");
		readdirSyncTest(new String[0], "@resource/test/appVirtual.asar/bbb.asar\\size");
		
		readFileStringSyncTest("", "");
		readFileStringSyncTest("", "./test/");
		readFileStringSyncTest("", "./test/unknown.txt");
		// readFileStringSyncTest("", "./test/app.asar");
		readFileStringSyncTest("bb", "./test/app.asar/\\bbb.txt");
		readFileStringSyncTest("bb", "./test/app.asar/\\bbbLink.txt");
		readFileStringSyncTest("", "./test/app.asar/unknown.txt");
		readFileStringSyncTest("", "./test/app.asar/ccc/unknown/");
		readFileStringSyncTest("", "./test/app.asar/ccc/unknown/unknown.txt");
		readFileStringSyncTest("", "./test/\\app.asar/ccc/unknown.txt");
		readFileStringSyncTest("", "./test/\\app.asar/cccLink/unknown.txt");
		readFileStringSyncTest("", "./test/app.asar/bbb.asar\\size");
		readFileStringSyncTest("", "./test/app.asar/ccc.asar\\ddd/\\");
		readFileStringSyncTest("eee", "./test/app.asar/ccc.asar\\eee.txt");
		readFileStringSyncTest("hhhhhhhh", "./test/app.asar/ccc/ddd\\\\/fff/hhh.txt");
		readFileStringSyncTest("hhhhhhhh", "./test/app.asar/cccLink/ddd\\\\/fff/hhh.txt");
		readFileStringSyncTest("hhhhhhhh", "./test/app.asar/ccclink/ddd\\\\/fff/hhh.txt");
		readFileStringSyncTest("ggggggg", "./test/app.asar/cccLink/dddLink\\\\//gggLink.txt");
		readFileStringSyncTest("", "./test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");
		readFileStringSyncTest("", "@resource/test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");

		streamExistTest(false, "");
		streamExistTest(false, "./test/");
		streamExistTest(false, "./test/unknown.txt");
		streamExistTest(true, "./test/app.asar");
		streamExistTest(true, "./test/app.asar/\\bbb.txt");
		streamExistTest(true, "./test/app.asar/\\bbb.txt");
		streamExistTest(false, "./test/app.asar/ccc.asar\\ddd/\\");
		streamExistTest(true, "./test/app.asar/ccc.asar\\eee.txt");
		streamExistTest(true, "./test/app.asar/ccc.asar");

		AsarFile asarFile = new AsarFile() {
			@Override
			public InputStream getStream() {
				try {
					return new FileInputStream("./test/app.asar");
				} catch(Exception ignored) { }
				return null;
			}
		};
		asarFile.mtimeMs = asarMtime;
		
		Map<String, AsarFile> mapVirtualAsarFile = new HashMap<>();
		mapVirtualAsarFile.put("./test/appVirtual.asar", asarFile);
		mapVirtualAsarFile.put("@resource/test/appVirtual.asar", asarFile);
		AsarFs.setVirtualAsarFile(mapVirtualAsarFile);
		
		existsSyncTest(true, "./test/app.asar/cccLink/dddLink\\\\/gggLink.txt");
		existsSyncTest(true, "./test/appVirtual.asar/cccLink/dddLink\\\\/gggLink.txt");
		existsSyncTest(true, "@resource/test/appVirtual.asar/cccLink/dddLink\\\\/gggLink.txt");
		
		statSyncTest(stFile(7), "./test/app.asar/cccLink/dddLink\\\\//gggLink.txt");
		statSyncTest(stFile(7), "./test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");
		statSyncTest(stFile(7), "@resource/test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");
		
		lstatSyncTest(stLink(), "./test/app.asar/cccLink/dddLink\\\\//gggLink.txt");
		lstatSyncTest(stLink(), "./test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");
		lstatSyncTest(stLink(), "@resource/test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");
		
		readdirSyncTest(new String[] { ".gitkeep", "bbb2.txt" }, "./test/app.asar/bbb.asar\\size");
		readdirSyncTest(new String[] { ".gitkeep", "bbb2.txt" }, "./test/appVirtual.asar/bbb.asar\\size");
		readdirSyncTest(new String[] { ".gitkeep", "bbb2.txt" }, "@resource/test/appVirtual.asar/bbb.asar\\size");
		
		readFileStringSyncTest("ggggggg", "./test/app.asar/cccLink/dddLink\\\\//gggLink.txt");
		readFileStringSyncTest("ggggggg", "./test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");
		readFileStringSyncTest("ggggggg", "@resource/test/appVirtual.asar/cccLink/dddLink\\\\//gggLink.txt");

        log("test count: ", testCount, ", success: ", (testCount - errCount), ", failed: ", errCount);
        log("test end");
	}

	static class StatusCompare extends ObjCompare<AsarFileStatus> {
		public boolean compare(AsarFileStatus a, AsarFileStatus b) {
			if (a == null || b == null) {
				return a == b;
			}
			if (a.isFile != b.isFile) { return false; }
			if (a.isDirectory != b.isDirectory) { return false; }
			if (a.isSymbolicLink != b.isSymbolicLink) { return false; }
			if (a.size != b.size) { return false; }

			float floatLimit = 0.0000001f;
			if (Math.abs(a.atimeMs - b.atimeMs) > floatLimit) { return false; }
			if (Math.abs(a.mtimeMs - b.mtimeMs) > floatLimit) { return false; }
			if (Math.abs(a.ctimeMs - b.ctimeMs) > floatLimit) { return false; }

			return true;
		}
	}

	static AsarFileStatus stPhyFile(String path) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				return null;
			}
			return createStatus(file.isFile(), file.isDirectory(), false, file.isFile() ? file.length() : 0, file.lastModified());
		} catch(Exception ignored) { }
		return null;
	}

	static AsarFileStatus stDire() {
		return createStatus(false, true, false, 0, asarMtime);
	}

	static AsarFileStatus stFile(long size) {
		return createStatus(true, false, false, size, asarMtime);
	}

	static AsarFileStatus stLink() {
		return createStatus(false, false, true, 0, asarMtime);
	}

	static String[] readdirSync(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return new String[0];
		}
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

	static AsarFileStatus createStatus(boolean isFile, boolean isDirectory, boolean isSymbolicLink, long size, float time) {
		AsarFileStatus status = new AsarFileStatus();
		status.isFile = isFile;
		status.isDirectory = isDirectory;
		status.isSymbolicLink = isSymbolicLink;
		status.size = size;

		status.atimeMs = status.mtimeMs = status.ctimeMs = time;
		return status;
	}

	static <T> void testEqual(String tag, T expect, T input) {
		++testCount;
		if (input.equals(expect)) {
			return;
		}
		++errCount;
		if (tag.equals("")) {
			tag = "[equal err]";
		}
		log(tag);
		log("    expect: ", expect);
		log("    actual: ", input);
		System.out.println("");
	}
	
	static <T> void testArrSortEqual(String tag, T[] expect, T[] input) {
		++testCount;

		boolean isSame = true;
		if (expect == null || input == null) {
			if (expect == input) {
				return;
			}
			isSame = false;
		}
		if (isSame && expect.length == input.length) {
			Arrays.sort(expect);
			Arrays.sort(input);
			boolean isOk = true;
			for (int i = 0; i < expect.length; ++i) {
				if (!expect[i].equals(input[i])) {
					isOk = false;
					break;
				}
			}
			if (isOk) {
				return;
			}
		}
		++errCount;
		if (tag.equals("")) {
			tag = "[equal err]";
		}
		log(tag);
		log("    expect: ", gson.toJson(expect));
		log("    actual: ", gson.toJson(input));
		System.out.println("");
	}

	static abstract class ObjCompare<T> {
		public abstract boolean compare(T a, T b);
	}
	
	static <T> void testObjEqual(String tag, T expect, T input, ObjCompare<T> compareIns) {
		++testCount;
		if (compareIns.compare(expect, input)) {
			return;
		}
		++errCount;
		if (tag.equals("")) {
			tag = "[equal err]";
		}
		log(tag);
		log("    expect: ", gson.toJson(expect));
		log("    actual: ", gson.toJson(input));
		System.out.println("");
	}

	static void log(Object ...arrObj) {
		String str = "";
		for (int i = 0; i < arrObj.length; ++i) {
			str += arrObj[i];
		}
		System.out.println(str);
	}

	static void logObj(Object ...arrObj) {
		for (int i = 0; i < arrObj.length; ++i) {
			String str = gson.toJson(arrObj[i]);
			if (str.equals("\"\"")) {
				str = "";
			}
			System.out.println(str);
		}
	}
}
