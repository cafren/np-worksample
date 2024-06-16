package se.unlogic.standardutils.streams;

import java.io.IOException;
import java.io.InputStream;

import se.unlogic.standardutils.io.BinarySizeFormater;

public class SizeLimitedInputStream extends InputStream {

	private final InputStream originalInputStream;
	
	private final long maxAllowedSize;
	
	private long readBytes;

	public SizeLimitedInputStream(InputStream originalInputStream, long maxAllowedSize) {
        
		this.originalInputStream = originalInputStream;
        
		this.maxAllowedSize = maxAllowedSize;
    }

	@Override
	public int read() throws IOException {

		int i = originalInputStream.read();
		
		if (i >= 0) {
			incrementReadBytes(1);
		}
		return i;
	}

	@Override
	public int read(byte b[]) throws IOException {

		return read(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {

		int i = originalInputStream.read(b, off, len);
		
		if (i >= 0) {
			incrementReadBytes(i);
		}
		
		return i;
	}

	private void incrementReadBytes(int size) throws InputStreamSizeLimitExceeded {

		readBytes += size;
		
		if (readBytes > maxAllowedSize) {
			
			throw new InputStreamSizeLimitExceeded("InputStream exceeded maximum allowed size of " + BinarySizeFormater.getFormatedSize(maxAllowedSize), maxAllowedSize);
		}
	}

	@Override
	public long skip(long n) throws IOException {

		return originalInputStream.skip(n);
	}

	@Override
	public int available() throws IOException {

		return originalInputStream.available();
	}

	@Override
	public void close() throws IOException {

		originalInputStream.close();
	}

	@Override
	public synchronized void mark(int readlimit) {

		originalInputStream.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {

		originalInputStream.reset();
	}

	@Override
	public boolean markSupported() {

		return originalInputStream.markSupported();
	}

}
