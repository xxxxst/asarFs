package com.vcedit.asarFs;

/** file status */
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
