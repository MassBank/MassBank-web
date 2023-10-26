/*

 * Slightly modified version of the com.ibatis.common.jdbc.ScriptRunner class
 * from the iBATIS Apache project. Only removed dependency on Resource class
 * and a constructor
 * GPSHansl, 06.08.2015: regex for delimiter, rearrange comment/delimiter detection, remove some ide warnings.
 */

/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package massbank;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Tool to run database scripts
 */
public class ScriptRunner {
	private static final Logger logger = LogManager.getLogger(ScriptRunner.class);

    private static final String DEFAULT_DELIMITER = ";";
    /**
     * regex to detect delimiter.
     * ignores spaces, allows delimiter in comment, allows an equals-sign
     */
    public static final Pattern delimP = Pattern.compile("^\\s*(--)?\\s*delimiter\\s*=?\\s*([^\\s]+)+\\s*.*$", Pattern.CASE_INSENSITIVE);

    private final Connection connection;

    private final boolean stopOnError;

    private String delimiter = DEFAULT_DELIMITER;
    private boolean fullLineDelimiter = false;

    /**
     * Default constructor
     */
    public ScriptRunner(Connection connection, boolean stopOnError) {
        this.connection = connection;
        this.stopOnError = stopOnError;
    }

    public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
        this.delimiter = delimiter;
        this.fullLineDelimiter = fullLineDelimiter;
    }

    /**
     * Runs an SQL script (read in using the Reader parameter)
     *
     * @param reader - the source of the script
     */
    public void runScript(Reader reader) throws IOException, SQLException {
        runScript(connection, reader);
    }

    /**
     * Runs an SQL script (read in using the Reader parameter) using the
     * connection passed in
     *
     * @param conn - the connection to use for the script
     * @param reader - the source of the script
     * @throws SQLException if any SQL errors occur
     * @throws IOException if there is an error reading from the Reader
     */
    private void runScript(Connection conn, Reader reader) throws IOException, SQLException {
        StringBuffer command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(reader);
            String line;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }
                String trimmedLine = line.trim();
                final Matcher delimMatch = delimP.matcher(trimmedLine);
                if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("//")) {
                    // Do nothing
                } else if (delimMatch.matches()) {
                    setDelimiter(delimMatch.group(2), false);
                } else if (trimmedLine.startsWith("--")) {
                    logger.trace(trimmedLine);
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("--")) {
                    // Do nothing
                } else if (!fullLineDelimiter
                        && trimmedLine.endsWith(getDelimiter())
                        || fullLineDelimiter
                        && trimmedLine.equals(getDelimiter())) {
                    command.append(line.substring(0, line
                            .lastIndexOf(getDelimiter())));
                    command.append(" ");
                    this.execCommand(conn, command, lineReader);
                    command = null;
                } else {
                    command.append(line);
                    command.append("\n");
                }
            }
            if (command != null) {
                this.execCommand(conn, command, lineReader);
            }
        }
        catch (IOException e) {
            throw new IOException(String.format("Error executing '%s': %s", command, e.getMessage()), e);
        }
    }

    private void execCommand(Connection conn, StringBuffer command,
                             LineNumberReader lineReader) throws SQLException {
        Statement statement = conn.createStatement();

        logger.trace(command);

        boolean hasResults = false;
        try {
            hasResults = statement.execute(command.toString());
        } catch (SQLException e) {
            final String errText = String.format("Error executing '%s' (line %d): %s",
                    command, lineReader.getLineNumber(), e.getMessage());
            logger.error(errText);
            if (stopOnError) {
                throw new SQLException(errText, e);
            }
        }

        ResultSet rs = statement.getResultSet();
        if (hasResults && rs != null) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                String name = md.getColumnLabel(i);
                logger.trace(name + "\t");
            }
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    String value = rs.getString(i);
                    logger.trace(value + "\t");
                }
            }
        }

        statement.close();
    }

    private String getDelimiter() {
        return delimiter;
    }
}
