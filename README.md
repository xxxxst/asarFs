# asarFs

.asar file system for java/android, asar file analysis as directory,

[https://github.com/xxxxst/asarFs](https://github.com/xxxxst/asarFs)

# Useage

```java
import com.vcedit.asarFs.AsarFile;
import com.vcedit.asarFs.AsarFileStatus;
import com.vcedit.asarFs.AsarFs;

String path = "./path/../file.asar/embed.asar/file.txt";

// check if the file exists
boolean isExist = AsarFs.existsSync(path);

// can also visit physics file
boolean isExist = AsarFs.existsSync("./physics.txt");

// retrieves file status for the path, if file not exist, return null
AsarFileStatus status = AsarFs.statSync(path);

// retrieves file status for the symbolic link referred to by path
AsarFileStatus status = AsarFs.lstatSync(path);

// reads the contents of the directory
String[] files = AsarFs.readdirSync(path);

// read file as file stream, if file not exist or is directory, return null
InputStream is = AsarFs.getFileStream(path);

// read file as utf8 string, if file not exist or is directory, return empty string
String strContent = AsarFs.readFileStringSync(path);

// read file as string by charset
String strContent = AsarFs.readFileStringSync(path, StandardCharsets.UTF_8);

// read bin file
byte[] bytes = AsarFs.readFileByteSync(path);

// asar header cache time(ms), default is 30 minutes
AsarFs.cacheTimeMs = 30 * 60 * 1000;

// map embed asar file or stream to virtual path, priority lower than physical location
AsarFile asarFileMd = new AsarFile() {
    @Override
    public InputStream getStream() {
        try {
            File file = new File(path);
            if (file.exists()) {
                return new FileInputStream(appPathTmp);
            }
            
            AssetManager am = myActivity.getAssets();
            return am.open("embed/path/file.asar");
        } catch (Exception ignored) { }
        return null;
    }
};
Map<String, AsarFile> mapVirtualAsarFile = new HashMap<>();
// map to physics path, priority lower than physical location
// mapVirtualAsarFile.put("./path/file.asar", asarFileMd);
mapVirtualAsarFile.put("@res/path/file.asar", asarFileMd);
AsarFs.setVirtualAsarFile(mapVirtualAsarFile);

// visit virtual path
boolean isExist = AsarFs.existsSync("@res/path/file.asar");

/** asar file status */
public class AsarFileStatus {
	/** is file */
	public boolean isFile = false;
	/** is directory */
	public boolean isDirectory = false;
	/** is symbolic link, need use lstatSync() if want to test symbolic link */
	public boolean isSymbolicLink = false;

	/** file size, directory and symbolic link size is 0 */
	public long size = 0;

	/** access time(ms), same to mtimeMs */
	public float atimeMs = 0;
	/** modify time(ms), same to physics file modify time */
	public float mtimeMs = 0;
	/** create time(ms), same to mtimeMs */
	public float ctimeMs = 0;
}

/** asar file model */
public abstract class AsarFile {
	/** modify time(ms) */
	public float mtimeMs = 0;
	/**
	 * return asar file stream
	 * @return asar file stream
	*/
	public abstract InputStream getStream();
}

```

# Dependencies

- org.json.jar (Android already build-in)

# Build

```bat
.\build.bat
```

# Run test

```bat
.\run.bat test

@REM rebuild and run test
.\run.bat
```

# License

MIT

[LICENSE](./LICENSE)
