package massbank.web.instrument;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import massbank.DatabaseManager;
import massbank.web.SearchFunction;

public class InstrumentSearch implements SearchFunction<List<String>> {

	public void getParameters(HttpServletRequest request) {

	}

	public List<String> search(DatabaseManager databaseManager) {
		ArrayList<String> resList = new ArrayList<String>();

		String sqlInst = "SELECT AC_INSTRUMENT, AC_INSTRUMENT_TYPE FROM INSTRUMENT "
				+ "GROUP BY AC_INSTRUMENT, AC_INSTRUMENT_TYPE";
		String sqlMs = "SELECT DISTINCT AC_MASS_SPECTROMETRY_MS_TYPE FROM RECORD";

		resList.add("INSTRUMENT_INFORMATION");
		try {
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement(sqlInst);
			ResultSet res = stmnt.executeQuery();
			int instNo = 1;
			while (res.next()) {
				resList.add(
						instNo + "\t" + res.getString("AC_INSTRUMENT_TYPE") + "\t" + res.getString("AC_INSTRUMENT"));
				instNo++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		resList.add("MS_INFORMATION");
		try {
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement(sqlMs);
			ResultSet res = stmnt.executeQuery();
			while (res.next()) {
				resList.add(res.getString("AC_MASS_SPECTROMETRY_MS_TYPE"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resList;
	}
}
