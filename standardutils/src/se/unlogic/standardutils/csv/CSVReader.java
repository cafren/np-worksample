package se.unlogic.standardutils.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

	public static List<CSVRow> readFile(InputStream inputStream, String delimiter, String encoding) throws IOException {

		try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, encoding))){
			
			List<CSVRow> rows = new ArrayList<CSVRow>();
			
			String line;
			
			while(bufferedReader.ready()) {
				
				line = bufferedReader.readLine();
				
				String[] cells = line.split(delimiter);
				
				rows.add(new CSVRow(cells));
			}
			
			return rows;
		}
	}
}
