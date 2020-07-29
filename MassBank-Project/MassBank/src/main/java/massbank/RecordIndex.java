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

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import massbank.db.DatabaseTimestamp;
import massbank.web.SearchExecution;
import massbank.web.recordindex.RecordIndexCount;
import massbank.web.recordindex.RecordIndexCount.RecordIndexCountResult;

/**
 * 
 * This servlet generates dynamic content for the RecordIndex.
 * 
 * @author rmeier
 * @version 30-04-2019
 *
 */
@WebServlet("/RecordIndex")
public class RecordIndex extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(RecordIndex.class);
	// generated RecordIndex represent state of the database at 'timestamp'
	private DatabaseTimestamp timestamp;
	private RecordIndexCountResult result;
	String sitechartSVG;
	String instchartSVG;
	String mschartSVG;
	
	private static PieDataset createDataset(Map<String, Integer> data, Integer MAX_DISP_DATA) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		if (!data.isEmpty()) {
			Integer sum=data.values().stream().mapToInt(Integer::intValue).sum();
			
			// sort map in ascending order
			Map<String, Integer> sorted_data = data.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) 			
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
					(oldValue, newValue) -> oldValue, LinkedHashMap::new));
			
			Iterator<Map.Entry<String, Integer>> it = sorted_data.entrySet().iterator();
			int i=0, disp_data=0;
			while (it.hasNext() & i<MAX_DISP_DATA) {
			    Map.Entry<String, Integer> entry = it.next();
			    BigDecimal percent = new BigDecimal(entry.getValue() * 100 / sum );
			    String label = entry.getKey() + " : " + percent.setScale(1, RoundingMode.HALF_UP).toString() + "%";
			    dataset.setValue(label, entry.getValue());
			    disp_data=disp_data+entry.getValue();
			    i++;
			}
			// sow all remainig data as "etc"
			if (disp_data<sum) {
				BigDecimal percent = new BigDecimal((sum-disp_data) * 100 / sum );
			    String label = "etc. : " + percent.setScale(1, RoundingMode.HALF_UP).toString() + "%";
			    dataset.setValue(label, sum-disp_data);
			}
		}
		else dataset.setValue("No data", 0);
        return dataset;
    }
	
	private static JFreeChart drawDataset(PieDataset dataset, String title)
	{
		// JFreeChartオブジェクト生成
		JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
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
		plot.setDefaultSectionOutlinePaint(ChartColor.BLACK);
		// グラフラベル背景色設定
		plot.setLabelBackgroundPaint(new Color(240,255,255));
		// グラフラベルフォント設定
		plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
		// グラフラベル幅設定
		plot.setMaximumLabelWidth(0.3);
		// グラフラベル間距離設定
		plot.setLabelGap(0.05);
		// グラフ前景色透明度設定
		plot.setForegroundAlpha(0.9f);
		return chart;
	}
	
	public void init() throws ServletException {
		try {
			result = new SearchExecution(null).exec(new RecordIndexCount());
			
			// construct and show pie charts
			int MAX_DISP_DATA=10;
			PieDataset siteGraphData = createDataset(result.mapSiteToRecordCount, MAX_DISP_DATA);
			PieDataset instGraphData = createDataset(result.mapInstrumentToRecordCount, MAX_DISP_DATA);
			PieDataset msGraphData = createDataset(result.mapMsTypeToRecordCount, MAX_DISP_DATA);
						
			int siteTopNum = (siteGraphData.getItemCount() < MAX_DISP_DATA) ? siteGraphData.getItemCount() : MAX_DISP_DATA;
			int instTopNum = (instGraphData.getItemCount() < MAX_DISP_DATA) ? instGraphData.getItemCount() : MAX_DISP_DATA;
			int msTopNum = (msGraphData.getItemCount() < MAX_DISP_DATA) ? msGraphData.getItemCount() : MAX_DISP_DATA;
						
			JFreeChart sitechart = drawDataset(siteGraphData, "Contributor top " + siteTopNum);
			JFreeChart instchart = drawDataset(instGraphData, "Instrument Type top " + instTopNum);
			JFreeChart mschart = drawDataset(msGraphData, "MS Type top " + msTopNum);

			SVGGraphics2D g2 = new SVGGraphics2D(900, 350);
			Rectangle r = new Rectangle(0, 0, 900, 350);
			sitechart.draw(g2, r);
			sitechartSVG= g2.getSVGElement();
			instchart.draw(g2, r);
			instchartSVG= g2.getSVGElement();
			mschart.draw(g2, r);
			mschartSVG= g2.getSVGElement();
			
			// get the current database timestamp
			timestamp=new DatabaseTimestamp();
		} catch (SQLException | ConfigurationException e) {
			logger.error(e.getMessage());
		}		
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Preprocess request: load list of mass spectrometry information in JSP.
		try {
			if (timestamp.isOutdated()) init();
		} catch (SQLException | ConfigurationException e) {
			logger.error(e.getMessage());
		}
		
		try {
			request.setAttribute("sites", result.mapSiteToRecordCount);
			request.setAttribute("instruments", result.mapInstrumentToRecordCount);
			request.setAttribute("mstypes", result.mapMsTypeToRecordCount);
			request.setAttribute("ionmodes", result.mapIonModeToRecordCount);
			request.setAttribute("symbols", result.mapSymbolToCount);
			request.setAttribute("spectra", result.spectraCount);
			request.setAttribute("compounds", result.compoundCount);
			request.setAttribute("isomers", result.isomerCount);
			request.setAttribute("version", timestamp.getVersion());
			request.setAttribute("sitechartSVG", sitechartSVG);
			request.setAttribute("instchartSVG", instchartSVG);
			request.setAttribute("mschartSVG", mschartSVG);
			
			request.getRequestDispatcher("/RecordIndex.jsp").forward(request, response);
		} catch (Exception e) {
			throw new ServletException("Error preparing record index", e);
		}
	}
}
