package massbank;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;

import massbank.admin.Validator2;

public class DevLoadData {
	
	public DevLoadData() throws IOException {
		System.out.println("RecordFormat");
		new RecordFormat();
		System.out.println("DevLogger");
		new DevLogger();
		System.out.println("init_db");
		try {
			DatabaseManager.init_db();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String contributor;
		Path dir = FileSystems.getDefault().getPath("/var/www/html/MassBank/DB");
		System.out.println(dir);
		DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
		for (Path contributorPath : stream) {
			if (!contributorPath.toFile().isDirectory()) continue;
			if (contributorPath.endsWith(".git")) continue;
			if (contributorPath.endsWith(".scripts")) continue;
			contributorPath = Paths.get(contributorPath.toString());
			contributor = contributorPath.getFileName().toString();
			DirectoryStream<Path> stream2 = Files.newDirectoryStream(contributorPath);
			for (Path recordPath : stream2) {
				File recordFile = recordPath.toFile();
				System.out.println(recordFile.toString());
				String recordAsString	= FileUtils.readFileToString(recordFile, StandardCharsets.UTF_8);
				
//				boolean mod	= false;
//				String[] sa	= recordAsString.split("\n");
//				for(int i = 0; i < sa.length; i++) {
//					if(!sa[i].startsWith("CH$COMPOUND_CLASS: "))
//						continue;
//					if(sa[i].endsWith(";")) {
//						sa[i]	= sa[i].substring(0, sa[i].length() - 1);
//						mod = true;
//					}
//					if(sa[i].contains("\"")) {
//						sa[i]	= sa[i].replaceAll("\"", "");
//						mod = true;
//					}
//				}
//				if(mod) {
//					FileUtils.writeStringToFile(recordFile, String.join("\n", sa), StandardCharsets.UTF_8, false);
//					System.out.println("### " + recordFile);
//				}
				
				Record record = Validator2.validate(recordAsString, contributor);
//				AccessionFile acc = AccessionFile.getAccessionDataFromFile(recordFile, contributor);
				if (record == null) {
					System.out.println("Error reading and validating record " + recordFile.getName());
					continue;
//					throw new IllegalArgumentException("Error reading and validating record " + recordFile.getName());
				}
				
//				boolean valid = acc.isValid();
//				if (valid) {							
					try {
						record.persist();
					} catch (Exception e) {
						e.printStackTrace();
					}
//				}
			}
			stream2.close();
		}
		stream.close();
	}
	
	public static void main(String[] args) throws IOException {
		new DevLoadData();
	}
	
}
