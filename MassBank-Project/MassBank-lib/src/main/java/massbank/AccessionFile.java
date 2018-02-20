package massbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class AccessionFile {
	private final ArrayList<String> tag = new ArrayList<String>();
	
	private final ArrayList<String> subtag = new ArrayList<String>();
	
	private final ArrayList<String> value = new ArrayList<String>();
	
	private final ArrayList<Integer> lineNo = new ArrayList<Integer>();
	
	public String file;
	
	public String contributor;
		
	public AccessionFile() {}
	
	/**
	 * Parse a MassBank Record file. Parses the file based on information provided
	 * by {@link massbank.RecordFormat}. The parsers does not ensure the validity of
	 * the file according to the MassBank Record Format Specification. To validate
	 * the record use {@link #isValid}.
	 * 
	 * @param file The MassBank Record file to parse.
	 * 
	 * @return AccessionFile on successful parsing. Null if a parsing error occurs.
	 */
	public static AccessionFile getAccessionDataFromFile(File file) { 
		AccessionFile acc = new AccessionFile();
		acc.file = file.toString();
		int fileLine = 0; 
		boolean parsing = true;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"latin1"))) {
			String line;
			String tag = null;
			String subtag = null;
			String value = null;
			int lineNo = 1;
			boolean eofMarker = false;
			while ((line = br.readLine()) != null) {
				fileLine++;
				if (eofMarker) {
					parsing = false;
					DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n\ton line " + fileLine + "\n\tFile continues after end of file marker has been read.");
					continue;
				}
				if (line.startsWith(RecordFormat.EOF_MARKER)) {
					eofMarker = true;
					continue;
				}
				if (line.startsWith(RecordFormat.MULTILINE_PREFIX)) {
					lineNo++;
					if (tag != null) {
						value = line.substring(RecordFormat.MULTILINE_PREFIX.length()).trim();
						if (!RecordFormat.VALUE_REGEX.matcher(value).matches()) {
							parsing = false;
							DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n\ton line " + fileLine + "\n\tValue " + value + " contains illegal characters (must be of form \\\\S+[ ]?)+.");
							continue;
						}
					} else {
						parsing = false;
						DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n\ton line " + fileLine + "\n\tAttempting to parse multiple line value, but no tag has been read in advance.");
						continue;
					}
				} else {
					tag = null;
					subtag = null;
					value = null;
					lineNo = 1;
					try {
						tag = line.substring(0, line.indexOf(RecordFormat.TAG_SEPARATOR));
						if (!RecordFormat.TAG_REGEX.matcher(tag).matches()) {
							parsing = false;
							DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n\ton line " + fileLine + "\n\tTag " + tag + " contains illegal characters (must be of form ([A-Z,_]+|([A-Z,_]+\\\\$[A-Z,_]+)).");
							continue;
						}
						line = line.substring(line.indexOf(RecordFormat.TAG_SEPARATOR)+RecordFormat.TAG_SEPARATOR.length());
					} catch (StringIndexOutOfBoundsException e) {
						parsing = false;
						DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n\ton line " + fileLine + "\n\tMalformed line, no tag separator found.");
						continue;
					}
					if (!RecordFormat.TAGS.contains(tag)) {
						parsing = false;
						DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n\ton line " + fileLine + "\n\tUnknown tag \"" + tag + "\".");
						continue;
					}
					if (RecordFormat.allowsSubtags(tag)) {
						try {
							subtag = line.substring(0, line.indexOf(RecordFormat.SUBTAG_SEPARATOR));
							if (!RecordFormat.SUBTAG_REGEX.matcher(subtag).matches()) {
								parsing = false;
								DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n\ton line " + fileLine + "\n\tSubtag " + subtag + " contains illegal characters (must be of form [A-Z]+(_?[A-Z])*).");
								continue;
							}
							line = line.substring(line.indexOf(RecordFormat.SUBTAG_SEPARATOR)+RecordFormat.SUBTAG_SEPARATOR.length());
							// handle SOLVENT subtags for now
//							if (subtag.compareTo("SOLVENT") == 0) {
//								subtag = subtag + "_" + line.charAt(0);
//								line.substring(1);
//							}
						} catch (StringIndexOutOfBoundsException e) {
							parsing = false;
							DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n\ton line " + fileLine + "\n\tMalformed line, no subtag separator found.");
							continue;
						} 
					} else  {
						subtag = null;
					}
					value = line.trim();	
					if (!RecordFormat.VALUE_REGEX.matcher(value).matches()) {
						parsing = false;
						DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n\ton line " + fileLine + "\n\tValue " + value + " contains illegal characters (must be of form \\\\S+[ ]?)+.");
						continue;
					}
					if (line.compareTo("") == 0) {
						if (subtag != null) {
							DevLogger.printToParseLog("Parsing warning for " + file.toString() + "\n\ttag \"" + tag + RecordFormat.TAG_SEPARATOR + subtag + "\" has no value.");
						} else {
							DevLogger.printToParseLog("Parsing warning for " + file.toString() + "\n\ttag \"" + tag + "\" has no value.");
						}
					}
				}
				acc.add(tag,subtag,value,lineNo);
			}
		} catch (FileNotFoundException e) {
			parsing = false;
			DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n File " + file.toString() + " not found.");
		} catch (IOException e) {
			parsing = false;
			DevLogger.printToParseLog("Parsing error for " + file.toString() + "\n IOException on reading line from " + file.toString() + ".");
		}
		if (parsing) {
			return acc;
		} else {
			return null;
		}
	}
	
	public ArrayList<String[]> get(String tag) {
		ArrayList<String[]> list = new ArrayList<String[]>();
		for (int i = 0; i < this.tag.size(); i++) {
			if (this.tag.get(i).compareTo(tag) == 0) {
				String[] res = {this.tag.get(i),this.subtag.get(i),this.value.get(i),this.lineNo.get(i).toString()};
				list.add(res);
			}
		}
		return list;
	}

	public ArrayList<String[]> get(String tag, String subtag) {
		ArrayList<String[]> list = new ArrayList<String[]>();
		for (int i = 0; i < this.tag.size(); i++) {
			if (this.tag.get(i).compareTo(tag) == 0) {
				if (this.subtag.get(i).compareTo(subtag) == 0) {
					String[] res = {this.tag.get(i),this.subtag.get(i),this.value.get(i),this.lineNo.get(i).toString()};
					list.add(res);		
				}
			}
		}
		return list;
	}	
	
	/*
	 * Validates the parsed MassBank Record file with information provided by {@link
	 * massbank.RecordFormat}. The validation process checks (1) that all mandatory
	 * tags are present and have an assigned value, (2) that unique tags only occur
	 * once and (3) that single line tags span only one line.
	 */
	public boolean isValid() {
		boolean valid = true;
		// check if there are null values
		for (int i = 0; i < this.value.size(); i++) {
			if (this.value.get(i) == null || this.value.get(i).compareTo("") == 0) {
				if (this.subtag.get(i) != null) {
					if (RecordFormat.isMandatory(this.tag.get(i) + RecordFormat.TAG_SEPARATOR + this.subtag.get(i))) {
						valid = false;
						DevLogger.printToValidationLog("Validation error for file " + file + ": Mandatory tag \"" + this.tag.get(i) + RecordFormat.TAG_SEPARATOR + this.subtag.get(i) + "\" has no value.");
					} else {
						DevLogger.printToValidationLog("Validation warning for file " + file + ": Optional tag \"" + this.tag.get(i) + RecordFormat.TAG_SEPARATOR + this.subtag.get(i) + "\" has no value.");
					}
				} else {
					if (RecordFormat.isMandatory(this.tag.get(i))) {
						valid = false;
						DevLogger.printToValidationLog("Validation error for file " + file + ": Mandatory tag \"" + this.tag.get(i) + "\" has no value.");
					} else {
						DevLogger.printToValidationLog("Validation warning for file " + file + ": Optional tag \"" + this.tag.get(i) + "\" has no value.");
					}
				}
			}
		}
		// check if mandatory tags are present
		for (String tag : RecordFormat.TAGS) {
			if (RecordFormat.isMandatory(tag)) {
				if (tag.contains(RecordFormat.TAG_SEPARATOR)) {
					if (this.get(tag.substring(0, tag.indexOf(RecordFormat.TAG_SEPARATOR)), tag.substring(tag.indexOf(RecordFormat.TAG_SEPARATOR)+2, tag.length())).size() == 0) {
						valid = false;
						DevLogger.printToValidationLog("Validation error for file " + file + ": Mandatory tag \"" + tag + "\" is missing.");
					}
				} else {
					if (this.get(tag).size() == 0) {
						valid = false;
						DevLogger.printToValidationLog("Validation error for file " + file + ": Mandatory tag \"" + tag + "\" is missing.");
					}
				}
			}
		}
		// check if unique tags occur only once
		// check if single line tags span only one line
		for (int i = 0; i < this.tag.size(); i++) {
			String tag = this.tag.get(i);
			String subtag = this.subtag.get(i);
			ArrayList<String[]> set = new ArrayList<String[]>();
			if (RecordFormat.allowsSubtags(tag)) {
				set = this.get(tag, subtag);
				if (RecordFormat.TAGS.contains(tag + RecordFormat.TAG_SEPARATOR + subtag)) {
					if (!RecordFormat.isIterative(tag + RecordFormat.TAG_SEPARATOR + subtag)) {
						if (RecordFormat.allowsMultipleLines(tag + RecordFormat.TAG_SEPARATOR + subtag)) {
							boolean foundOne = false;
							for (String[] el : set) {
								if (Integer.parseInt(el[3]) == 1) {
									if (foundOne) {
										valid = false;
										DevLogger.printToValidationLog("Validation error for file " + file + ": Unique tag \"" + tag + RecordFormat.TAG_SEPARATOR + subtag + "\" appears multiple times.");
									} else {
										foundOne = true;
									}
								}
							}
						} else {
							if (set.size() > 1) {
								valid = false;
								DevLogger.printToValidationLog("Validation error for file " + file + ": Unique tag \"" + tag + RecordFormat.TAG_SEPARATOR + subtag + "\" appears multiple times.");
							}							
						}
					} else {
						if (RecordFormat.allowsMultipleLines(tag + RecordFormat.TAG_SEPARATOR + subtag)) {
							// TODO subtagged iterative multiline
						} else {
							for (String[] el : set) {
								if (Integer.parseInt(el[3]) != 1) {
									valid = false;
									DevLogger.printToValidationLog("Validation error for file " + file + ": Single line tag \"" + tag + RecordFormat.TAG_SEPARATOR + subtag + "\" spans multiple lines.");
								}
							}
						}
					}
				} else {
					if (!RecordFormat.isIterative(tag)) {
						if (RecordFormat.allowsMultipleLines(tag)) {
							boolean foundOne = false;
							for (String[] el : set) {
								if (Integer.parseInt(el[3]) == 1) {
									if (foundOne) {
										valid = false;
										DevLogger.printToValidationLog("Validation error for file " + file + ": Unique tag \"" + tag + RecordFormat.TAG_SEPARATOR + subtag + "\" appears multiple times.");
									} else {
										foundOne = true;
									}
								}
							}							
						} else {
							if (this.get(tag, subtag).size() > 1) {
								valid = false;
								DevLogger.printToValidationLog("Validation error for file " + file + ": Unique tag \"" + tag + RecordFormat.TAG_SEPARATOR + subtag + "\" appears multiple times.");
							}
						}
					} else {
						if (RecordFormat.allowsMultipleLines(tag)) {
							// TODO subtagged iterative multiline
						} else {
							for (String[] el : set) {
								if (Integer.parseInt(el[3]) != 1) {
									valid = false;
									DevLogger.printToValidationLog("Validation error for file " + file + ": Single line tag \"" + tag + RecordFormat.TAG_SEPARATOR + subtag + "\" spans multiple lines.");
								}
							}
						}
					}
				}
			} else {
				set = this.get(tag);
				if (!RecordFormat.isIterative(tag)) {
					if (RecordFormat.allowsMultipleLines(tag)) {
						String tmp = null;
						for (String[] el : set) {
							if (Integer.parseInt(el[3]) == 1) {
								if (tmp != null) {
									valid = false;
									DevLogger.printToValidationLog("Validation error for file " + file + ": Unique tag " + tag + " appears multiple times");
								} else {
									tmp = el[0];
								}
							}
						}						
					} else {
						if (this.get(tag).size() > 1) {
							valid = false;
							DevLogger.printToValidationLog("Validation error for file " + file + ": Unique tag \"" + tag + "\" appears multiple times.");
						}
					}
				} else {
					if (RecordFormat.allowsMultipleLines(tag)) {
						// TODO non-subtag iterative multiline
					} else {
						for (String[] el : set) {
							if (Integer.parseInt(el[3]) != 1) {
								valid = false;
								DevLogger.printToValidationLog("Validation error for file " + file + ": Single line tag \"" + tag + "\" spans multiple lines.");
							}
						}	
					} 
				}
			}
						
		}
		return valid;
	}
	
	public void add(String tag, String subtag, String value, Integer lineNo) {
		this.tag.add(tag);
		this.subtag.add(subtag);
		this.value.add(value);
		this.lineNo.add(lineNo);
	}
	
	public void persist(String contributor) throws SQLException {
		this.contributor = contributor;
		DatabaseManager db  = DatabaseManager.create();
		if (db != null) {
			db.persistAccessionFile(this);
		} else {
			DevLogger.printToDBLog("Could not persist file because no connction to the database could be established" );
		}
	}
	
//	public static void main (String[] args) throws IOException, SQLException, InterruptedException {
//		System.setOut(new PrintStream(new FileOutputStream("/Users/laptop/Desktop/output.txt")));
//		new RecordFormat();
//		new DevLogger();			
////		Path dir = FileSystems.getDefault().getPath("/Users/laptop/Desktop/UFZ/MBRecords/newFolder/");
//		Path dir = FileSystems.getDefault().getPath("/Users/laptop/Desktop/UFZ/MBRecordsEU/");
//		DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
////		ArrayList<Thread> threadPool = new ArrayList<Thread>();
//		int parseFail = 0;
//		int validFail = 0;
//		int persiFail = 0;
//		for (Path path : stream) {
//			path = Paths.get(path.toString(), "/recdata/");
//			if (path.toFile().isDirectory()) {
//				DirectoryStream<Path> stream2 = Files.newDirectoryStream(path);
//				for (Path path2 : stream2) {
//					File file = path2.toFile();
////					System.out.println(file.toString());
////					Thread thread = new Thread(new Runnable() {
////						@Override
////						public void run() {
//							AccessionFile acc = AccessionFile.getAccessionDataFromFile(file);
//							if (acc != null) {
//								boolean valid = acc.isValid();
//								if (valid) {							
//									try {
//										acc.persist();
//									} catch (Exception e) {
//										persiFail++;
//									}
//								} else {
//									validFail++;
//								}
//							} else {
//								parseFail++;
//							}
////						}	
////					});
////					threadPool.add(thread);
////					if (threadPool.size() >= 100) {
////						for (Thread t : threadPool) {
////							t.join();
////						}
////						threadPool.clear();
////					}
////					thread.start();
//				}
//				stream2.close();
//			}
//		}
//		stream.close();
//		System.out.println(parseFail);
//		System.out.println(validFail);
//		System.out.println(persiFail);
//	}		
}
