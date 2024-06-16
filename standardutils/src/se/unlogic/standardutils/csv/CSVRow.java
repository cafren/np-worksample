package se.unlogic.standardutils.csv;


public class CSVRow {

	private final String[] cells;

	public CSVRow(String[] cells) {

		super();
		this.cells = cells;
	}

	public CSVRow(int columns) {

		super();
		this.cells = new String[columns];
	}
	
	public void setCellValue(int index, String value){
		
		cells[index] = value;
	}
	
	public String getCellValue(int index){
		
		return cells[index];
	}
	
	public String[] getCells(){
		
		return cells;
	}
	
	public int getColumnCount() {
		
		return cells.length;
	}
}
