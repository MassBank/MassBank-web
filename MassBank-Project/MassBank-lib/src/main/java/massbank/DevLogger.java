/**
 * 
 */
package massbank;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.sql.Timestamp;

/**
 * For Development Only
 * 
 * @author laptop
 *
 */
public class DevLogger {
	
	private static PrintStream parse = null;
	private static PrintStream validate = null;
	private static PrintStream db = null;
	
	public DevLogger() {
		try {
			parse = new PrintStream(new FileOutputStream("./parseLog.txt",true));
			validate = new PrintStream(new FileOutputStream("./validateLog.txt",true));
			db = new PrintStream(new FileOutputStream("./dbLog.txt",true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void printToParseLog(String s) {
		print(s, parse);
		
	}
	
	public static void printToValidationLog(String s) {
		print(s, validate);
	}
	
	public static void printToDBLog(String s) {
		print(s, db);
	}
	
	private static void print(String s, PrintStream ps) {
		ps.print(new Timestamp(System.currentTimeMillis()));
		ps.print("\n\t");
		ps.print(s);
		ps.print("\n");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DevLogger logger = new DevLogger();
		printToParseLog("testString");

	}

}
