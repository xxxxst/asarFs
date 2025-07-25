package com.vcedit.asarFs;

import java.io.IOException;
import java.io.InputStream;

public class SplitInputStream extends InputStream {
	public InputStream input = null;
	public int inputPos = 0;
	public int start = 0;
	public int length = 0;

	public SplitInputStream(InputStream _input, int _start, int _length) {
		input = _input;
		start = _start;
		length = _length;
		try {
			reset();
		} catch (Exception ignored) { }
	}

	@Override
	public void close() throws IOException {
		input.close();
	}

	@Override
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

	@Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (inputPos >= start + length) {
			return -1;
		}
		if (inputPos + len > start + length) {
			len = start + length - inputPos;
		}
		if (len <= 0) {
			return -1;
		}

		try {
			int rst = input.read(b, off, len);
			inputPos += len;
			return rst;
		} catch (Exception ex) {
			inputPos += len;
			throw ex;
		}
	}

	@Override
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

	@Override
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

	@Override
	public int available() throws IOException {
		int len = input.available();
		len = Math.min(len, start + length - inputPos);
		return len;
	}
}
