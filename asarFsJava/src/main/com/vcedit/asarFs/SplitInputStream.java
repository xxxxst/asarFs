package com.vcedit.asarFs;

import java.io.IOException;
import java.io.InputStream;

class SplitInputStream extends InputStream {
	InputStream input = null;
	int inputPos = 0;
	int start = 0;
	int length = 0;

	public SplitInputStream(InputStream _input, int _start, int _length) {
		input = _input;
		start = _start;
		length = _length;
		try {
			reset();
		} catch (Exception ignored) { }
	}

	public void close() throws IOException {
		input.close();
	}

	public int read() throws IOException {
		if (inputPos >= start + length) {
			return -1;
		}

		try {
			int rst = input.read();
			++inputPos;
			return rst;
		} catch (Exception ex) {
			++inputPos;
			throw ex;
		}
	}

	public synchronized void reset() throws IOException {
		inputPos = start;
		if (input.markSupported()) {
			input.reset();
			//return;
		}

		//input.reset();
		long skipNum = start;
		while (skipNum > 0) {
			long num = input.skip(skipNum);
			skipNum -= num;
		}
	}

	public long skip(long num) throws IOException {
		if (inputPos + num >= start + length) {
			num = (start + length - inputPos - 1);
		}
		if (num <= 0) {
			return 0;
		}
		long rst = input.skip(num);
		inputPos += rst;
		return rst;
	}

	public int available() throws IOException {
		int len = input.available();
		len = Math.min(len, start + length - inputPos);
		return len;
	}
}
