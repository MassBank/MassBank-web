package massbank;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
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
import org.jfree.graphics2d.svg.SVGUtils;
import org.openscience.cdk.depict.DepictionGenerator;

import massbank.web.SearchExecution;
import massbank.web.recordindex.RecordIndexCount;
import massbank.web.recordindex.RecordIndexCount.RecordIndexCountResult;

/**
 * @author rmeier
 * @version 0.2, 30-07-2018
 * This class is called from command line to create a new temporary
 * database <i>tmpdbName</i>, fill it with all records found in <i>DataRootPath</i>
 * and move the new database to <i>dbName</i>. For each Record a svg showing the
 * molecular formula is created in <i>DataRootPath</i>/figure.
 * 
 */
public class RefreshDatabase {
	private static final Logger logger = LogManager.getLogger(RefreshDatabase.class);
	
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
	
	public static void main(String[] args) throws FileNotFoundException, SQLException, ConfigurationException, IOException {
		try {
			logger.trace("Creating a new database \""+ Config.get().tmpdbName() +"\" and initialize a MassBank database scheme.");
			DatabaseManager.init_db(Config.get().tmpdbName());
			
			logger.trace("Creating a DatabaseManager for \"" + Config.get().tmpdbName() + "\".");
			DatabaseManager db  = new DatabaseManager(Config.get().tmpdbName());
			
			logger.trace("Creating a temporary directory.");
			Path tmp = Files.createTempDirectory(null);
			tmp.toFile().deleteOnExit();
			
			logger.info("Opening DataRootPath \"" + Config.get().DataRootPath() + "\" and iterate over content.");
			DirectoryStream<Path> path = Files.newDirectoryStream(FileSystems.getDefault().getPath(Config.get().DataRootPath()));
			for (Path contributorPath : path) {
				if (!Files.isDirectory(contributorPath)) continue;
				if (contributorPath.endsWith(".git")) continue;
				if (contributorPath.endsWith(".scripts")) continue;
				if (contributorPath.endsWith("figure")) continue;
				
				String contributor = contributorPath.getFileName().toString();
				logger.trace("Opening contributor path \"" + contributor + "\" and iterate over content.");
				DirectoryStream<Path> path2 = Files.newDirectoryStream(contributorPath);
				for (Path recordPath : path2) {
					logger.info("Validating \"" + recordPath + "\".");
					String recordAsString	= FileUtils.readFileToString(recordPath.toFile(), StandardCharsets.UTF_8);
					Record record = Validator.validate(recordAsString, contributor);
					if (record == null) {
						logger.error("Error reading and validating record \"" + recordPath.toString() + "\".");
						continue;
					}
					logger.trace("Writing record \"" + record.ACCESSION() + "\" to database.");
					db.persistAccessionFile(record);
					logger.trace("Creating svg figure for record\"" + record.ACCESSION() + "\".");
					// create formula images					
					DepictionGenerator dg = new DepictionGenerator().withAtomColors().withZoom(3);
					dg.depict(record.CH_IUPAC1()).writeTo(Config.get().DataRootPath()+"/figure/"+record.ACCESSION()+".svg");
				}
				path2.close();
			}
			path.close();
			
			logger.trace("Moving new database to MassBank database.");
			DatabaseManager.move_temp_db_to_main_massbank();
			
			RecordIndexCountResult result = new SearchExecution(null).exec(new RecordIndexCount());
			Map<String, Integer> sites = result.mapSiteToRecordCount;
			Map<String, Integer> instruments = result.mapInstrumentToRecordCount;
			Map<String, Integer> mstypes = result.mapMsTypeToRecordCount;
			
			// construct and show pie charts
			int MAX_DISP_DATA=10;
			PieDataset siteGraphData = createDataset(sites, MAX_DISP_DATA);
			PieDataset instGraphData = createDataset(instruments, MAX_DISP_DATA);
			PieDataset msGraphData = createDataset(mstypes, MAX_DISP_DATA);
			
			int siteTopNum = (siteGraphData.getItemCount() < MAX_DISP_DATA) ? siteGraphData.getItemCount() : MAX_DISP_DATA;
			int instTopNum = (instGraphData.getItemCount() < MAX_DISP_DATA) ? instGraphData.getItemCount() : MAX_DISP_DATA;
			int msTopNum = (msGraphData.getItemCount() < MAX_DISP_DATA) ? msGraphData.getItemCount() : MAX_DISP_DATA;
			
			JFreeChart sitechart = drawDataset(siteGraphData, "Contributor  top " + siteTopNum);
			JFreeChart instchart = drawDataset(instGraphData, "Instrument Type  top " + instTopNum);
			JFreeChart mschart = drawDataset(msGraphData, "MS Type  top " + msTopNum);
			
	        SVGGraphics2D g2 = new SVGGraphics2D(900, 350);
	        Rectangle r = new Rectangle(0, 0, 900, 350);
	        sitechart.draw(g2, r);
	        File f = new File(Config.get().DataRootPath()+ "/figure/" + "massbank_Contributor_Graph.svg");
	        SVGUtils.writeToSVG(f, g2.getSVGElement());
			
	        instchart.draw(g2, r);
	        f = new File(Config.get().DataRootPath()+ "/figure/" + "massbank_Instrument_Type_Graph.svg");
	        SVGUtils.writeToSVG(f, g2.getSVGElement());
	        
	        mschart.draw(g2, r);
	        f = new File(Config.get().DataRootPath()+ "/figure/" + "massbank_MS_Type_Graph.svg");
	        SVGUtils.writeToSVG(f, g2.getSVGElement());
		}
		catch (Exception e) {
			logger.fatal(e);
			System.exit(1);
		}
	}
}
