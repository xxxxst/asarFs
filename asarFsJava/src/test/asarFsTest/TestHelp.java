package asarFsTest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class TestHelp {
	static Gson gson = new Gson();

	public static void log(Object ...arrObj) {
		String str = "";
		for (int i = 0; i < arrObj.length; ++i) {
			str += arrObj[i];
		}
		System.out.println(str);
	}

	public static void logObj(Object ...arrObj) {
		for (int i = 0; i < arrObj.length; ++i) {
			String str = gson.toJson(arrObj[i]);
			if (str.equals("\"\"")) {
				str = "";
			}
			System.out.println(str);
		}
	}

	public static byte readConsoleChar() {
		try {
			System.in.read();
			System.in.skip(System.in.available());
		} catch(Exception e){ }

		return 0;
	}
	
	public static boolean isDirExist(String path) {
		try {
			File file = new File(path);
			return file.exists() && file.isDirectory();
		} catch (Exception ignored) { }
		return  false;
	}
	
	public static boolean isFileExist(String path) {
		try {
			File file = new File(path);
			return file.exists() && !file.isDirectory();
		} catch (Exception ignored) { }
		return false;
	}

	public static String[] readdirSync(String path) {
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

	public static byte[] readFileByte(InputStream is) {
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

		byte[] arrByte = buf.toByteArray();

		return arrByte;
	}

	public static String readFileString(InputStream is) {
		String rst="";

		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[10240];

		try {
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buf.write(data, 0, nRead);
			}
		} catch (Exception ignored) {}

		rst = new String(buf.toByteArray(), StandardCharsets.UTF_8);

		try {
			is.close();
		} catch (Exception ignored){ }
		return rst;
	}
}
