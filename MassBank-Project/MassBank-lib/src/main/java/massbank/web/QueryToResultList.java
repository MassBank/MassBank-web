package massbank.web;

import java.util.HashMap;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.ex.ConfigurationException;

import massbank.ResultList;
import massbank.ResultRecord;

public class QueryToResultList {

	public static ResultList toResultList(List<String> allLine, HttpServletRequest request) throws ConfigurationException {
		// Result information record generation (結果情報レコード生成)
		ResultList list = new ResultList();
		ResultRecord record;
		int nodeGroup = -1;
		HashMap<String, Integer> nodeCount = new HashMap<String, Integer>();
		String[] fields;
		for (int i = 0; i < allLine.size(); i++) {
			fields = allLine.get(i).split("\t");
			record = new ResultRecord();
			record.setInfo(fields[0]);
			record.setId(fields[1]);
			record.setIon(fields[2]);
			record.setFormula(fields[3]);
			record.setEmass(fields[4]);
			record.setContributor("0");
			// Node group setting (ノードグループ設定)
			if (!nodeCount.containsKey(record.getName())) {
				nodeGroup++;
				nodeCount.put(record.getName(), nodeGroup);
				record.setNodeGroup(nodeGroup);
			} else {
				record.setNodeGroup(nodeCount.get(record.getName()));
			}
			list.addRecord(record);
		}

		// Get sort key (ソートキー取得)
		String sortKey = ResultList.SORT_KEY_NAME;
		if (request.getParameter("sortKey") != null) {
//			if (request.getParameter("sortKey").compareTo(ResultList.SORT_KEY_FORMULA) == 0) {
//				sortKey = ResultList.SORT_KEY_FORMULA;
//			} else if (request.getParameter("sortKey").compareTo(ResultList.SORT_KEY_EMASS) == 0) {
//				sortKey = ResultList.SORT_KEY_EMASS;
//			} else if (request.getParameter("sortKey").compareTo(ResultList.SORT_KEY_ID) == 0) {
//				sortKey = ResultList.SORT_KEY_ID;
//			}
			sortKey = request.getParameter("sortKey");
		}

		// Acquire sort action (ソートアクション取得)
		int sortAction = ResultList.SORT_ACTION_ASC;
		if (request.getParameter("sortAction") != null) {
//			if (request.getParameter("sortAction").compareTo(String.valueOf(ResultList.SORT_ACTION_DESC)) == 0) {
//				sortAction = ResultList.SORT_ACTION_DESC;
//			}
			sortAction = Integer.parseInt(request.getParameter("sortAction"));
		}
		
		// Record sort (レコードソート)
		list.sortList(sortKey, sortAction);
		
		return list;
	}
	public static ResultList toResultList(ResultRecord[] records, HttpServletRequest request) throws ConfigurationException {
		// Result information record generation (結果情報レコード生成)
		ResultList list = new ResultList();
		ResultRecord record;
		int nodeGroup = -1;
		HashMap<String, Integer> nodeCount = new HashMap<String, Integer>();
		for (int recordIndex = 0; recordIndex < records.length; recordIndex++) {
			record = records[recordIndex];
			record.setContributor("0");
			// Node group setting (ノードグループ設定)
			if (!nodeCount.containsKey(record.getName())) {
				nodeGroup++;
				nodeCount.put(record.getName(), nodeGroup);
				record.setNodeGroup(nodeGroup);
			} else {
				record.setNodeGroup(nodeCount.get(record.getName()));
			}
			list.addRecord(record);
		}

		// Get sort key (ソートキー取得)
		String sortKey = ResultList.SORT_KEY_NAME;
		if (request.getParameter("sortKey") != null) {
//			if (request.getParameter("sortKey").compareTo(ResultList.SORT_KEY_FORMULA) == 0) {
//				sortKey = ResultList.SORT_KEY_FORMULA;
//			} else if (request.getParameter("sortKey").compareTo(ResultList.SORT_KEY_EMASS) == 0) {
//				sortKey = ResultList.SORT_KEY_EMASS;
//			} else if (request.getParameter("sortKey").compareTo(ResultList.SORT_KEY_ID) == 0) {
//				sortKey = ResultList.SORT_KEY_ID;
//			}
			sortKey = request.getParameter("sortKey");
			if(sortKey.equals(ResultList.SORT_NOT)) sortKey	= ResultList.SORT_KEY_NAME;
		}

		// Acquire sort action (ソートアクション取得)
		int sortAction = ResultList.SORT_ACTION_ASC;
		if (request.getParameter("sortAction") != null) {
//			if (request.getParameter("sortAction").compareTo(String.valueOf(ResultList.SORT_ACTION_DESC)) == 0) {
//				sortAction = ResultList.SORT_ACTION_DESC;
//			}
			sortAction = Integer.parseInt(request.getParameter("sortAction"));
		}
		
		// Record sort (レコードソート)
		list.sortList(sortKey, sortAction);
		return list;
	}
}