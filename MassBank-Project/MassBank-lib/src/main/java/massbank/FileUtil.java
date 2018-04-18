package massbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.ex.ConfigurationException;

public class FileUtil {
	public static void writeToFile(String svg, String file){
		try {
			PrintWriter pw = new PrintWriter(file);
			pw.println(svg);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static List<String> readFromFile(File file){
		List<String> list	= new ArrayList<String>();
		BufferedReader br = null;
		try {
			// read
			br = new BufferedReader(new FileReader(file));
			String currentLine;
			while ((currentLine = br.readLine()) != null)
				list.add(currentLine);

		} catch (IOException e) {
			// read error
			e.printStackTrace();
		} finally {
			// close file
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return list;
	}
	public static boolean existsFile(String databaseName, String accession) throws ConfigurationException{
		File file	= FileUtil.getFile(databaseName, accession);
		return file.exists();
	}
	public static File getFile(String databaseName, String accession) throws ConfigurationException{
		// http://localhost/MassBank/DB/annotation/MassBank/XXX00001.txt
		File file	= new File(Config.getInstance().get_DataRootPath() + databaseName + File.separator + accession + ".txt");
		return file;
	}
}
