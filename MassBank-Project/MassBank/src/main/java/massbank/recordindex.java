/*******************************************************************************
 * Copyright (C) 2017 MassBank consortium
 * 
 * This file is part of MassBank.
 * 
 * MassBank is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * package massbank;
 * 
 ******************************************************************************/
package massbank;

import javax.servlet.http.HttpServlet;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import massbank.web.SearchExecution;
import massbank.web.recordindex.RecordIndexCount;
import massbank.web.recordindex.RecordIndexCount.RecordIndexCountResult;

@WebServlet("/RecordIndex2")
public class recordindex extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static PieDataset createDataset(Map<String, Integer> data) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		Integer sum=data.values().stream().mapToInt(Integer::intValue).sum();
		System.out.println(sum);
		for (Entry<String, Integer> entry : data.entrySet())
		{
			
			BigDecimal percent = new BigDecimal(entry.getValue() / (sum * 100));
			String label = entry.getKey() + " : " + percent.setScale(1, RoundingMode.HALF_UP).toString() + "%";
			dataset.setValue(label, percent);
	
			System.out.println(entry.getKey() + "/" + entry.getValue());
			System.out.println(label + percent.toString());
		}
        return dataset;
    }
	

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Preprocess request: load list of mass spectrometry information in JSP.
		try {
			RecordIndexCountResult result = new SearchExecution(request).exec(new RecordIndexCount());
			Map<String, Integer> sites = result.mapSiteToRecordCount;
			Map<String, Integer> instruments = result.mapInstrumentToRecordCount;
			Map<String, Integer> mstypes = result.mapMsTypeToRecordCount;
			Map<String, Integer> mergedtypes = result.mapMergedToCount;
			Map<String, Integer> ionmodes = result.mapIonModeToRecordCount;
			Map<String, Integer> symbols = result.mapSymbolToCount;
			
	        request.setAttribute("sites", sites);
	        request.setAttribute("instruments", instruments);
	        request.setAttribute("mstypes", mstypes);
	        request.setAttribute("mergedtypes", mergedtypes);
	        request.setAttribute("ionmodes", ionmodes);
	        request.setAttribute("symbols", symbols);

	    	//---------------------------
	    	// construct and show pie charts
	    	//---------------------------
	        	// グラフデータセットオブジェクト
	    	DefaultPieDataset siteGraphData = new DefaultPieDataset();
	    	DefaultPieDataset instGraphData = new DefaultPieDataset();
	    	DefaultPieDataset msGraphData = new DefaultPieDataset();
	    	
	    	// グラフデータ表示件数
	    	final int MAX_DISP_DATA = 10;
	    	Set<String> keys = null;
	    	String key = null;
	    	int val = 0;
	    	BigDecimal percent = null;
	    	String label = null;
	    	int totalSiteNum=sites.values().stream().mapToInt(Integer::intValue).sum();
	    	int totalInstNum=instruments.values().stream().mapToInt(Integer::intValue).sum();
	    	int totalMsNum=mstypes.values().stream().mapToInt(Integer::intValue).sum();
	    	
	    	// Contributor
	    	if (totalSiteNum > 0) {
	    		keys = sites.keySet();
	    		int siteNum = 0;
	    		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
	    			key = (String)iterator.next();
	    			val = sites.get(key);
	    			percent = new BigDecimal(String.valueOf(val / totalSiteNum * 100));
	    			label = key + " : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
	    			siteNum++;
	    			siteGraphData.setValue(label, val);
	    		}
	    		siteGraphData.sortByValues(SortOrder.DESCENDING);
	    		createDataset(sites);
	    		
	    		// グラフデータが多い場合は表示データを省略
	    		long etcVal = 0;
	    		int siteCount = siteGraphData.getItemCount();
	    		ArrayList<Comparable> etcList = new ArrayList<Comparable>();
	    		if ( siteCount > MAX_DISP_DATA) {
	    			for (int index=MAX_DISP_DATA; index<siteCount; index++) {
	    				etcList.add(siteGraphData.getKey(index));
	    			}
	    			for (Comparable etcKey:etcList) {
	    				etcVal += siteGraphData.getValue(etcKey).longValue();
	    				siteGraphData.remove(etcKey);
	    			}
	    			percent = new BigDecimal(String.valueOf(etcVal / totalSiteNum * 100));
	    			label = "etc. : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
	    			siteGraphData.setValue(label, etcVal);
	    		}
	    	}
	    	else {
	    		siteGraphData.setValue("No Contributor Data", 0);
	    	}
	    	
	    	// Instrument Typeグラフデータセット
	    	if (totalInstNum > 0) {
	    		keys = instruments.keySet();
	    		int instNum = 0;
	    		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
	    			key = (String)iterator.next();
	    			val = instruments.get(key);
	    			percent = new BigDecimal(String.valueOf(val / totalInstNum * 100));
	    			label = key + " : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
	    			instNum++;
	    			instGraphData.setValue(label, val);
	    		}
	    		instGraphData.sortByValues(SortOrder.DESCENDING);
	    		
	    		// グラフデータが多い場合は表示データを省略
	    		long etcVal = 0;
	    		int instCount = instGraphData.getItemCount();
	    		ArrayList<Comparable> etcList = new ArrayList<Comparable>();
	    		if ( instCount > MAX_DISP_DATA) {
	    			for (int index=MAX_DISP_DATA; index<instCount; index++) {
	    				etcList.add(instGraphData.getKey(index));
	    			}
	    			for (Comparable etcKey:etcList) {
	    				etcVal += instGraphData.getValue(etcKey).longValue();
	    				instGraphData.remove(etcKey);
	    			}
	    			percent = new BigDecimal(String.valueOf(etcVal / totalInstNum * 100));
	    			label = "etc. : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
	    			instGraphData.setValue(label, etcVal);
	    		}
	    	}
	    	else {
	    		instGraphData.setValue("No Instrument Type Data", 0);
	    	}
	    	
	    	
	    	// MS Typeグラフデータセット
	    	if (totalMsNum > 0) {
	    		keys = mstypes.keySet();
	    		int msNum = 0;
	    		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
	    			key = (String)iterator.next();
	    			val = mstypes.get(key);
	    			percent = new BigDecimal(String.valueOf(val / totalMsNum * 100));
	    			label = key + " : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
	    			msNum++;
	    			msGraphData.setValue(label, val);
	    		}
	    		msGraphData.sortByValues(SortOrder.DESCENDING);
	    		
	    		// グラフデータが多い場合は表示データを省略
	    		long etcVal = 0;
	    		int msCount = msGraphData.getItemCount();
	    		ArrayList<Comparable> etcList = new ArrayList<Comparable>();
	    		if ( msCount > MAX_DISP_DATA) {
	    			for (int index=MAX_DISP_DATA; index<msCount; index++) {
	    				etcList.add(msGraphData.getKey(index));
	    			}
	    			for (Comparable etcKey:etcList) {
	    				etcVal += msGraphData.getValue(etcKey).longValue();
	    				msGraphData.remove(etcKey);
	    			}
	    			percent = new BigDecimal(String.valueOf(etcVal / totalMsNum * 100));
	    			label = "etc. : " + percent.setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
	    			msGraphData.setValue(label, etcVal);
	    		}
	    	}
	    	else {
	    		msGraphData.setValue("No MS Type Data", 0);
	    	}
	    	
	    		// グラフ一括生成＆出力
	    	/*try {
	    		LinkedHashMap<String, DefaultPieDataset> graphDataMap = new LinkedHashMap<String, DefaultPieDataset>(2);
	    		int siteTopNum = (siteGraphData.getItemCount() < MAX_DISP_DATA) ? siteGraphData.getItemCount() : MAX_DISP_DATA;
	    		int instTopNum = (instGraphData.getItemCount() < MAX_DISP_DATA) ? instGraphData.getItemCount() : MAX_DISP_DATA;
	    		int msTopNum = (msGraphData.getItemCount() < MAX_DISP_DATA) ? msGraphData.getItemCount() : MAX_DISP_DATA;
	    		graphDataMap.put("Contributor  top " + siteTopNum, siteGraphData);
	    		graphDataMap.put("Instrument Type  top " + instTopNum, instGraphData);
	    		graphDataMap.put("MS Type  top " + msTopNum, msGraphData);
	    		DefaultPieDataset data = null;
	    		String fileName = null;
	    		String filePath = null;
	    		
	    		keys = graphDataMap.keySet();
	    		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
	    			key = (String)iterator.next();
	    			
	    			// グラフ用データ取得
	    			data = graphDataMap.get(key);
	    			
	    			// JFreeChartオブジェクト生成
	    			JFreeChart chart = ChartFactory.createPieChart(key, data, true, true, false);
	    			
	    			// グラフ全体背景色設定
	    			chart.setBackgroundPaint(new ChartColor(242,246,255));
	    			
	    			// グラフ全体境界線設定
	    			chart.setBorderVisible(true);
	    			
	    			// グラフエリアパディング設定
	    			chart.setPadding(new RectangleInsets(10,10,10,10));
	    			
	    			// グラフタイトルフォント設定
	    			chart.getTitle().setFont(new Font("Arial", Font.BOLD, 22));
	    			
	    			// 凡例の表示位置設定
	    			LegendTitle legend = chart.getLegend();
	    			legend.setPosition(RectangleEdge.RIGHT);
	    			
	    			// 凡例パディング設定
	    			legend.setPadding(10,10,10,10);
	    			
	    			// 凡例マージン設定
	    			legend.setMargin(0,10,0,5);
	    			
	    			// 凡例表示位置上寄せ
	    			legend.setVerticalAlignment(VerticalAlignment.TOP);
	    			
	    			// グラフの描画領域を取得
	    			PiePlot plot = (PiePlot)chart.getPlot();
	    			
	    			// グラフの楕円表示を許可する
	    			plot.setCircular(true);
	    			
	    			// グラフ境界線色設定
	    			plot.setBaseSectionOutlinePaint(ChartColor.BLACK);
	    			
	    			// グラフラベル背景色設定
	    			plot.setLabelBackgroundPaint(new Color(240,255,255));
	    			
	    			// グラフラベルフォント設定
	    			plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
	    			
	    			// グラフラベル幅設定
	    			plot.setMaximumLabelWidth(0.3);
	    			
	    			// グラフラベル間距離設定
	    			plot.setLabelGap(0.05);
	    			
	    			// グラフ背景色透明度設定
//	    			plot.setBackgroundAlpha(0.9f);
	    			
	    			// グラフ前景色透明度設定
	    			plot.setForegroundAlpha(0.9f);
	    			
	    			// グラフのファイル出力
	    			fileName = "massbank_" + key + "_Graph.jpg";
	    			fileName = Pattern.compile("[ ]*top[ ]*[0-9]*").matcher(fileName).replaceAll("");
	    			
	    			filePath = Config.get().TOMCAT_TEMP_PATH(context) + fileName;
	    			BufferedOutputStream outStream = null;
	    			try {
	    				outStream = new BufferedOutputStream(new FileOutputStream(filePath));
	    				ChartUtilities.writeChartAsJPEG(outStream, chart, 900, 350);
	    			}
	    			catch ( IOException ie ) {
	    				ie.printStackTrace();
	    			}
	    			finally {
	    				if ( outStream != null ) {
	    					try {
	    						outStream.flush();
	    						outStream.close();
	    					} catch (IOException ie) {
	    						ie.printStackTrace();
	    					}
	    				}
	    			}
	    			
	    			// グラフの表示
	    			out.println( "<tr>" );
	    			out.println( "<td>" );
	    			//out.println( "<img src=\"" + Config.get().BASE_URL() + "temp/" + fileName + "\" alt=\"\" border=\"0\">" );
	    			out.println( "<img src=\"" + Config.get().TOMCAT_TEMP_URL() + fileName + "\" alt=\"\" border=\"0\">" );
	    			out.println( "</td>" );
	    			out.println( "</tr>" );
	    		}
	    	}
	    	catch ( NoClassDefFoundError nc ) {	// for linux...(a transitional program)
	    		// "java.lang.NoClassDefFoundError: Could not initialize class sun.awt.X11GraphicsEnvironment" by ChartFactory.createPieChart
	    		// Linuxでグラフを描画しようとした際に上記のエラーが発生する環境ではグラフを表示しない（暫定対処）
	    		out.println( "<tr><td>Graph can not be rendered by the influence of environment on the server....</td></tr>" );
	    		Logger.getLogger("global").warning("Graph can not be rendered by the influence of environment on the server....");
	    		nc.printStackTrace();
	    	}
	    	out.println( "</table>" );*/
	        
	        
	        request.getRequestDispatcher("/RecordIndex2.jsp").forward(request, response);
		} catch (Exception e) {
			throw new ServletException("Cannot obtain Instrument Information from DB", e);
        }
     }

}
