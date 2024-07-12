package com.vcedit.asarFs;

import java.io.InputStream;

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
