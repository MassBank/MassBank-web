package massbank;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DevLoadData {
	
	public DevLoadData() throws IOException {
		new RecordFormat();
		new DevLogger();
		String contributor;
		Path dir = FileSystems.getDefault().getPath("/home/rene/GIT/MassBank-data");
//		Path dir = FileSystems.getDefault().getPath("/vagrant/MassBank_Records/");
		DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
		for (Path path : stream) {
			if (path.endsWith(".git")) continue;
			if (path.endsWith(".scripts")) continue;
			path = Paths.get(path.toString());
			contributor = path.getFileName().toString();
			if (path.toFile().isDirectory()) {
				DirectoryStream<Path> stream2 = Files.newDirectoryStream(path);
				for (Path path2 : stream2) {
					File file = path2.toFile();
					System.out.println(file.toString());
					AccessionFile acc = AccessionFile.getAccessionDataFromFile(file);
					if (acc != null) {
						boolean valid = acc.isValid();
						if (valid) {							
							try {
								acc.persist(contributor);
							} catch (Exception e) {	
							}
						}
					}
//					break;
				}
				stream2.close();
			}
		}
		stream.close();
	}
	
	public static void main(String[] args) throws IOException {
		new DevLoadData();
	}
	
}
