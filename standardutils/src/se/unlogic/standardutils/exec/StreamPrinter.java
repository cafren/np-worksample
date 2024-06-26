/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import se.unlogic.standardutils.readwrite.ReadWriteUtils;



/**
 * A simple buffered {@link StreamHandler} implementation that prints the input from the {@link InputStream} to the given {@link OutputStream}. If no {@link OutputStream} is given it defaults to System.out.<p>
 * 
 * This implementation inputStream based on the {@link PrintWriter}, {@link InputStreamReader} and {@link BufferedReader} classes.
 * 
 * @author Robert "Unlogic" Olofsson (unlogic@unlogic.se)
 *
 */
public class StreamPrinter extends Thread implements StreamHandler {

	private InputStream inputStream;
	private String prefix;
	private OutputStream outputStream;

	private boolean terminated = false;
	
	public StreamPrinter(String prefix, OutputStream os) {
		super();
		this.outputStream = os;
		this.prefix = prefix;
	}
	
	public StreamPrinter(OutputStream os) {
		super();
		this.outputStream = os;
	}

	public StreamPrinter() {}

	@Override
	public boolean isTerminated(){
		
		return terminated;
	}
	
	@Override
	public void terminate(){
		
		this.terminated = true;
	}
	
	@Override
	public void run() {
		
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		
		try {
			PrintWriter pw = null;

			if (outputStream != null) {
				pw = new PrintWriter(outputStream);
			}else{
				pw = new PrintWriter(System.out);
			}

			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);

			String line = null;

			if(prefix != null){

				while (!terminated && (line = bufferedReader.readLine()) != null) {

					pw.println(prefix + line);
				}

			}else{

				while (!terminated && (line = bufferedReader.readLine()) != null) {

					pw.println(line);
				}
			}

			if (pw != null) {
				pw.flush();
			}
			
		} catch (IOException e) {
						
			throw new RuntimeException(e);
			
		}finally{
			ReadWriteUtils.closeReader(bufferedReader);
			ReadWriteUtils.closeReader(inputStreamReader);
			
			this.terminated = true;
		}
	}

	@Override
	public void handleStream(InputStream is) {
		this.inputStream = is;
		this.start();
	}

	public String getPrefix() {
		return prefix;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public boolean isFinished() {

		return !this.isAlive();
	}

	@Override
	public void awaitFinish() {

		try {
			this.join();
			
		} catch (InterruptedException e) {

			throw new RuntimeException(e);
		}
	}
}
