package massbank.web.export;

import java.util.ArrayList;
import java.util.List;

import massbank.Record;

public class RecordExporter {
	public static enum ExportFormat {
		MSP,
		THERMO;
	}
	public static String[] recordExport(ExportFormat format, Record... records){
		List<String> list	= new ArrayList<String>();
		
		for(Record record : records) {
			switch (format) {
			case MSP:{
				list.addAll(recordToMsp(record));
				break;
			}
			case THERMO: {
				list.addAll(recordToThermo(record));
				break;
			}
			default:
				throw new IllegalArgumentException("Unknown Export-Format '" + format + "'!");
			}
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	public static List<String> recordToMsp(Record record){
		List<String> list	= new ArrayList<String>();
		
		// TODO
		
		return list;
	}
	public static List<String> recordToThermo(Record record){
		List<String> list	= new ArrayList<String>();
		
		// TODO
		
		return list;
	}
}
