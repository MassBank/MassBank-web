package massbank.web.instrument;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import massbank.GetConfig;
import massbank.web.Database;
import massbank.web.SearchFunction;

public class InstrumentSearch implements SearchFunction {
	
	public void getParameters(HttpServletRequest request) {
		
	}
	
	public ArrayList<String> search(Connection connection) {
		ArrayList<String> resList = new ArrayList<String>();
		
		String sqlInst = "select AC_INSTRUMENT, AC_INSTRUMENT_TYPE "
				+ "from INSTRUMENT "
				+ "group by AC_INSTRUMENT, AC_INSTRUMENT_TYPE"; 
		String sqlMs = "select distinct AC_MASS_SPECTROMETRY_MS_TYPE "
				+ "from RECORD";
		
		resList.add("INSTRUMENT_INFORMATION");
		try {
			PreparedStatement stmnt = connection.prepareStatement(sqlInst.toUpperCase());
			ResultSet res = stmnt.executeQuery();
			int instNo = 1;
			while(res.next()) {
				resList.add(instNo + "\t" + res.getString("AC_INSTRUMENT_TYPE") + "\t" + res.getString("AC_INSTRUMENT"));
				instNo++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		resList.add("MS_INFORMATION");
		try {
			PreparedStatement stmnt = connection.prepareStatement(sqlMs.toUpperCase());
			ResultSet res = stmnt.executeQuery();
			while(res.next()) {
				resList.add(res.getString("AC_MASS_SPECTROMETRY_MS_TYPE"));
			} 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resList;
	}
}
