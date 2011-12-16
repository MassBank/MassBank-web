/*******************************************************************************
 *
 * Copyright (C) 2008 JST-BIRD MassBank
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *******************************************************************************
 *
 * SearchPage クラス
 *
 * ver 1.0.20 2011.12.16
 *
 ******************************************************************************/

import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import massbank.GetConfig;
import massbank.GetInstInfo;
import massbank.MassBankCommon;

/**
 * SearchPage クラス
 */
@SuppressWarnings("serial")
public class SearchPage extends JApplet {

	public static String baseUrl = "";

	private GetInstInfo instInfo = null;
	
	private static int PRECURSOR = -1;
	private static float TOLERANCE = 0.3f;
	public static int CUTOFF_THRESHOLD = 5;
	private static final int LEFT_PANEL_WIDTH = 430;

	private static final int TAB_ORDER_DB = 0;
	private static final int TAB_ORDER_FILE = 1;
	private static final int TAB_RESULT_DB = 0;
	private static final int TAB_VIEW_COMPARE = 0;
	private static final int TAB_VIEW_PACKAGE = 1;

	public static final String COL_LABEL_NAME = "Name";
	public static final String COL_LABEL_SCORE = "Score";
	public static final String COL_LABEL_HIT = "Hit";
	public static final String COL_LABEL_ID = "ID";
	public static final String COL_LABEL_ION = "Ion";
	public static final String COL_LABEL_CONTRIBUTOR = "Contributor";
	public static final String COL_LABEL_NO = "No.";
	public static final String COL_LABEL_ORDER = "Order";
	public static final String COL_LABEL_TYPE = "Type";
	public static final String COL_LABEL_MATCH = "Match";
	public static final String COL_LABEL_DISABLE = "Disable";
	public static final String COL_LABEL_PEAK = "Peak";
	public static final String COL_LABEL_PRECURSOR = "Precursor";

	private UserFileData[] userDataList = null;

	public static final String TABLE_QUERY_FILE = "QueryFile";
	public static final String TABLE_QUERY_DB = "QueryDb";
	public static final String TABLE_RESULT = "Result";
	
	private TableSorter fileSorter = null;						// クエリーファイルテーブルモデル
	private TableSorter querySorter = null; 					// クエリーDBテーブルモデル
	private TableSorter resultSorter = null;					// 検索結果テーブルモデル
	
	private JTable queryFileTable = null;						// クエリーユーザファイルテーブル
	private JTable queryDbTable = null;						// クエリーDBテーブル
	private JTable resultTable = null;							// 検索結果テーブル
	
	private PeakPanel queryPlot = new PeakPanel(false);		// クエリースペクトルパネル
	private PeakPanel resultPlot = new PeakPanel(false);		// 検索結果スペクトルパネル
	private PeakPanel compPlot = new PeakPanel(true);			// 比較用スペクトルパネル

	private JTabbedPane queryTabPane = new JTabbedPane();		// クエリータブペイン
	private JTabbedPane resultTabPane = new JTabbedPane();		// 検索結果タブペイン
	private JTabbedPane viewTabPane = new JTabbedPane();		// スペクトル表示タブペイン

	private JScrollPane queryFilePane = null;					// クエリーファイルペイン
	private JScrollPane resultPane = null;						// クエリーDBペイン
	private JScrollPane queryDbPane = null;					// 検索結果ペイン
	
	private JButton btnName = new JButton("Search Name");
	private JButton btnAll = new JButton("All");

	private String saveSearchName = "";

	private JButton etcPropertyButton = new JButton("Search Parameter Setting");
	
	private boolean isRecActu;			// スペクトル検索フラグ(実測スペクトル)
	private boolean isRecInteg;			// スペクトル検索フラグ(統合スペクトル)
	private boolean isDispSelected;		// Package View表示フラグ(選択レコード)
	private boolean isDispRelated;		// Package View表示フラグ(関連スペクトル)
	
	private JRadioButton tolUnit1 = new JRadioButton("unit", true);
	private JRadioButton tolUnit2 = new JRadioButton("ppm");

	private Map<String, List<String>> instGroup;							// 装置種別グループマップ
	private LinkedHashMap<String, JCheckBox> instCheck;					// 装置種別チェックボックス格納用
	private HashMap<String, Boolean> isInstCheck;							// 装置種別チェックボックス値格納用
	private LinkedHashMap<String, JCheckBox> msCheck;						// MS種別チェックボックス格納用
	private HashMap<String, Boolean> isMsCheck;							// MS種別チェックボックス値格納用
	private LinkedHashMap<String, JRadioButton> ionRadio;					// イオン種別ラジオボタン格納用
	private HashMap<String, Boolean> isIonRadio;							// イオン種別ラジオボタン値格納用
	
	private boolean isSubWindow = false;

	private JLabel hitLabel = new JLabel(" ");

	private ArrayList<String[]> nameList = new ArrayList<String[]>();

	private ArrayList nameListAll = new ArrayList();

	private String[] siteList;

	public static String[] siteNameList;

	private JPanel parentPanel2 = null;

	private PackageViewPanel pkgView = null;					// PackageViewコンポーネント
	
	private MassBankCommon mbcommon = new MassBankCommon();
	private final Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);

	private static int seaqId = 0;
	private static int seaqCompound = 0;

	public static AppletContext context = null;				// アプレットコンテキスト
	public static int initAppletWidth = 0;					// アプレット初期画面サイズ(幅)
	public static int initAppletHight = 0;					// アプレット初期画面サイズ(高さ)
	
	public static final int MAX_DISPLAY_NUM = 30;				// Package View最大表示可能件数
	
	private CookieManager cm;							// Cookie Manager
//	private final String COOKIE_PRE = "PRE";			// Cookie情報キー（PRECURSOR）
	private final String COOKIE_TOL = "TOL";			// Cookie情報キー（TOLERANCE）
	private final String COOKIE_CUTOFF = "CUTOFF"; 	// Cookie情報キー（COOKIE_CUTOFF）
	private final String COOKIE_INST = "INST";			// Cookie情報キー（INSTRUMENT）
	private final String COOKIE_MS = "MS";				// Cookie情報キー（MS）
	private final String COOKIE_ION = "ION";			// Cookie情報キー（ION）

	private final JRadioButton dispSelected = new JRadioButton("selected", true);
	private final JRadioButton dispRelated = new JRadioButton("related");
	private final JLabel lbl2 = new JLabel("Package View display mode  : ");

	public ProgressDialog dlg;
	public String[] ps = null;
	public String param = "";

	/**
	 * メインプログラム
	 */
	public void init() {
		
		// アプレットコンテキスト取得
		context = getAppletContext();
		
		// アプレット初期画面サイズ取得
		initAppletWidth = getWidth();
		initAppletHight = getHeight();

		// 環境設定ファイルから連携サイトのURLを取得
		String confPath = getCodeBase().toString();
		confPath = confPath.replaceAll("/jsp", "");
		GetConfig conf = new GetConfig(confPath);
		siteNameList = conf.getSiteName();
		baseUrl = conf.getServerUrl();
		
		// Cookie情報ユーティリティ初期化
		cm = new CookieManager(this, "SerchApplet", 30, conf.isCookie());
		
		// Precursor m/z情報初期化
		initPreInfo();
		
		// Tolerance情報初期化
		initTolInfo();
		
		// Cutoff Threshold情報初期化
		initCutoffInfo();
		
		// 装置種別情報初期化
		instInfo = new GetInstInfo(confPath);
		initInstInfo();
		
		// MS種別情報初期化
		initMsInfo();
		
		// イオン種別情報初期化
		initIonInfo();

		
		// ウインドウ生成
		createWindow();

		// 検索中ダイアログ
		this.dlg = new ProgressDialog(getFrame());

		// ユーザーファイル読込み
		if (getParameter("file") != null) {
			loadFile(getParameter("file"));
		}
		// 他画面からのクエリ追加
		else if (getParameter("num") != null) {
			DefaultTableModel dm = (DefaultTableModel) querySorter.getTableModel();
			dm.setRowCount(0);

			int num = Integer.parseInt(getParameter("num"));
			for (int i = 0; i < num; i++) {
				String pnum = Integer.toString(i + 1);
				String id = getParameter("qid" + pnum);
				String name = getParameter("name" + pnum);
				String site = getParameter("site" + pnum);
				String[] idNameSite = new String[] { id, name, site };
				nameList.add(idNameSite);

				site = siteNameList[Integer.parseInt(site)];
				String[] idNameSite2 = new String[] { id, name, site, String.valueOf(i + 1) };
				dm.addRow(idNameSite2);
			}
		}
	}
	
	/**
	 * 強度レンジ及びマスレンジ設定
	 */
	public void setAllPlotAreaRange() {
		queryPlot.setIntensityRange(PeakPanel.INTENSITY_MAX);
		compPlot.setIntensityRange(PeakPanel.INTENSITY_MAX);
		resultPlot.setIntensityRange(PeakPanel.INTENSITY_MAX);
		PeakData qPeak = queryPlot.getPeaks(0);
		PeakData rPeak = resultPlot.getPeaks(0);
		if (qPeak == null && rPeak == null)
			return;
		double qMax = 0d;
		double rMax = 0d;
		if (qPeak != null)
			qMax = qPeak.compMaxMzPrecusor(queryPlot.getPrecursor());
		if (rPeak != null)
			rMax = rPeak.compMaxMzPrecusor(resultPlot.getPrecursor());
		if (qMax > rMax) {
			queryPlot.setPeaks(null, -1);
			setAllPlotAreaRange(queryPlot);
		} else {
			resultPlot.setPeaks(null, -1);
			setAllPlotAreaRange(resultPlot);
		}
	}

	/**
	 * 強度レンジ及びマスレンジ設定
	 * @param panel PeakPanel
	 */
	public void setAllPlotAreaRange(PeakPanel panel) {
		if (panel == queryPlot) {
			compPlot.setMass(queryPlot.getMassStart(),
					queryPlot.getMassRange(), queryPlot.getIntensityRange());
			resultPlot.setMass(queryPlot.getMassStart(), queryPlot.getMassRange(),
					queryPlot.getIntensityRange());
		}
		else if (panel == compPlot) {
			queryPlot.setMass(compPlot.getMassStart(), compPlot.getMassRange(),
					compPlot.getIntensityRange());
			resultPlot.setMass(compPlot.getMassStart(), compPlot.getMassRange(),
					compPlot.getIntensityRange());
		}
		else if (panel == resultPlot) {
			queryPlot.setMass(resultPlot.getMassStart(), resultPlot.getMassRange(),
					resultPlot.getIntensityRange());
			compPlot.setMass(resultPlot.getMassStart(), resultPlot.getMassRange(),
					resultPlot.getIntensityRange());
		}
	}

	/**
	 * 最大強度取得
	 * マスレンジ内ピーク内での最大強度取得
	 * @param start
	 * @param end
	 */
	public int getMaxIntensity(double start, double end) {
		PeakData qPaek = queryPlot.getPeaks(0);
		PeakData dPeak = resultPlot.getPeaks(0);
		int qm = 0;
		int dm = 0;
		if (qPaek != null)
			qm = qPaek.getMaxIntensity(start, end);
		if (dPeak != null)
			dm = dPeak.getMaxIntensity(start, end);
		return Math.max(qm, dm);
	}

	/**
	 * ウインドウ生成
	 */
	private void createWindow() {
		
		// ツールチップマネージャー設定
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setInitialDelay(50);
		ttm.setDismissDelay(8000);
		
		// Searchパネル
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		Border border = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				new EmptyBorder(1, 1, 1, 1));
		mainPanel.setBorder(border);
		
		// *********************************************************************
		// User File Queryタブ
		// *********************************************************************
		DefaultTableModel fileDm = new DefaultTableModel();
		fileSorter = new TableSorter(fileDm, TABLE_QUERY_FILE);
		queryFileTable = new JTable(fileSorter) {
			@Override
			public boolean isCellEditable(int row, int column) {
//				super.isCellEditable(row, column);
				// オーバーライドでセル編集を不可とする
				return false;
			}
		};
		queryFileTable.addMouseListener(new TblMouseListener());
		fileSorter.setTableHeader(queryFileTable.getTableHeader());
		queryFileTable.setRowSelectionAllowed(true);
		queryFileTable.setColumnSelectionAllowed(false);
		queryFileTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		String[] col = { COL_LABEL_NO, COL_LABEL_NAME, COL_LABEL_ID };
		((DefaultTableModel) fileSorter.getTableModel()).setColumnIdentifiers(col);
		(queryFileTable.getColumn(queryFileTable.getColumnName(0)))
				.setPreferredWidth(44);
		(queryFileTable.getColumn(queryFileTable.getColumnName(1)))
				.setPreferredWidth(LEFT_PANEL_WIDTH - 44);
		(queryFileTable.getColumn(queryFileTable.getColumnName(2)))
				.setPreferredWidth(70);
		
		ListSelectionModel lm = queryFileTable.getSelectionModel();
		lm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lm.addListSelectionListener(new LmFileListener());
		queryFilePane = new JScrollPane(queryFileTable);
		queryFilePane.addMouseListener(new PaneMouseListener());
		queryFilePane.setPreferredSize(new Dimension(300, 300));
		
		
		// *********************************************************************
		// Resultタブ
		// *********************************************************************
		DefaultTableModel resultDm = new DefaultTableModel();
		resultSorter = new TableSorter(resultDm, TABLE_RESULT);
		resultTable = new JTable(resultSorter) {
			@Override
			public String getToolTipText(MouseEvent me) {
//				super.getToolTipText(me);
				// オーバーライドでツールチップの文字列を返す
				Point pt = me.getPoint();
				int row = rowAtPoint(pt);
				if (row < 0) {
					return null;
				} else {
					int nameCol = getColumnModel().getColumnIndex(COL_LABEL_NAME);
					return " " + getValueAt(row, nameCol) + " ";
				}
			}
			@Override
			public boolean isCellEditable(int row, int column) {
//				super.isCellEditable(row, column);
				// オーバーライドでセル編集を不可とする
				return false;
			}
		};
		resultTable.addMouseListener(new TblMouseListener());
		resultSorter.setTableHeader(resultTable.getTableHeader());
		
		JPanel dbPanel = new JPanel();
		dbPanel.setLayout(new BorderLayout());
		resultPane = new JScrollPane(resultTable);
		resultPane.addMouseListener(new PaneMouseListener());
		
		resultTable.setRowSelectionAllowed(true);
		resultTable.setColumnSelectionAllowed(false);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		String[] col2 = { COL_LABEL_NAME, COL_LABEL_SCORE, COL_LABEL_HIT,
				COL_LABEL_ID, COL_LABEL_ION, COL_LABEL_CONTRIBUTOR, COL_LABEL_NO };

		resultDm.setColumnIdentifiers(col2);
		(resultTable.getColumn(resultTable.getColumnName(0)))
				.setPreferredWidth(LEFT_PANEL_WIDTH - 180);
		(resultTable.getColumn(resultTable.getColumnName(1))).setPreferredWidth(70);
		(resultTable.getColumn(resultTable.getColumnName(2))).setPreferredWidth(20);
		(resultTable.getColumn(resultTable.getColumnName(3))).setPreferredWidth(70);
		(resultTable.getColumn(resultTable.getColumnName(4))).setPreferredWidth(20);
		(resultTable.getColumn(resultTable.getColumnName(5))).setPreferredWidth(70);
		(resultTable.getColumn(resultTable.getColumnName(6))).setPreferredWidth(50);

		ListSelectionModel lm2 = resultTable.getSelectionModel();
		lm2.addListSelectionListener(new LmResultListener());
		
		resultPane.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, 200));
		dbPanel.add(resultPane, BorderLayout.CENTER);
		
		
		// *********************************************************************
		// DB Queryタブ
		// *********************************************************************
		DefaultTableModel dbDm = new DefaultTableModel();
		querySorter = new TableSorter(dbDm, TABLE_QUERY_DB);
		queryDbTable = new JTable(querySorter) {
			@Override
			public boolean isCellEditable(int row, int column) {
//				super.isCellEditable(row, column);
				// オーバーライドでセル編集を不可とする
				return false;
			}
		};
		queryDbTable.addMouseListener(new TblMouseListener());
		querySorter.setTableHeader(queryDbTable.getTableHeader());
		queryDbPane = new JScrollPane(queryDbTable);
		queryDbPane.addMouseListener(new PaneMouseListener());
		
		int h = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		queryDbPane.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, h));
		queryDbTable.setRowSelectionAllowed(true);
		queryDbTable.setColumnSelectionAllowed(false);
		queryDbTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		String[] col3 = { COL_LABEL_ID, COL_LABEL_NAME, COL_LABEL_CONTRIBUTOR, COL_LABEL_NO };
		DefaultTableModel model = (DefaultTableModel) querySorter.getTableModel();
		model.setColumnIdentifiers(col3);

		// 列幅セット
		queryDbTable.getColumn(queryDbTable.getColumnName(0))
				.setPreferredWidth(70);
		queryDbTable.getColumn(queryDbTable.getColumnName(1))
				.setPreferredWidth(LEFT_PANEL_WIDTH - 70);
		queryDbTable.getColumn(queryDbTable.getColumnName(2))
				.setPreferredWidth(70);
		queryDbTable.getColumn(queryDbTable.getColumnName(3))
				.setPreferredWidth(50);

		ListSelectionModel lm3 = queryDbTable.getSelectionModel();
		lm3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lm3.addListSelectionListener(new LmQueryDbListener());
		
		// ボタンパネル
		JPanel btnPanel = new JPanel();
		btnName.addActionListener(new BtnSearchNameListener());
		btnAll.addActionListener(new BtnAllListener());
		btnPanel.add(btnName);
		btnPanel.add(btnAll);
		
		parentPanel2 = new JPanel();
		parentPanel2.setLayout(new BoxLayout(parentPanel2, BoxLayout.PAGE_AXIS));
		parentPanel2.add(btnPanel);
		parentPanel2.add(queryDbPane);
		
		// オプションパネル
		JPanel dispModePanel = new JPanel();
		isDispSelected = dispSelected.isSelected();
		isDispRelated = dispRelated.isSelected();
		if (isDispSelected) {
			resultTable.getSelectionModel().setSelectionMode(
					ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		}
		else if (isDispRelated) {
			resultTable.getSelectionModel().setSelectionMode(
					ListSelectionModel.SINGLE_SELECTION);
		}
		Object[] retRadio = new Object[]{dispSelected, dispRelated};
		for (int i=0; i<retRadio.length; i++) {
			((JRadioButton)retRadio[i]).addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (isDispSelected != dispSelected.isSelected()
							|| isDispRelated != dispRelated.isSelected()) {
						
						isDispSelected = dispSelected.isSelected();
						isDispRelated = dispRelated.isSelected();
						
						// 結果レコード選択状態を解除
						resultTable.clearSelection();
						resultPlot.clear();
						compPlot.setPeaks(null, 1);
						resultPlot.setPeaks(null, 0);
						setAllPlotAreaRange();
						pkgView.initResultRecInfo();
						
						if (isDispSelected) {
							resultTable.getSelectionModel().setSelectionMode(
									ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						}
						else if (isDispRelated) {
							resultTable.getSelectionModel().setSelectionMode(
									ListSelectionModel.SINGLE_SELECTION);
						}
					}
				}
			});
		}
		ButtonGroup disGroup = new ButtonGroup();
		disGroup.add(dispSelected);
		disGroup.add(dispRelated);
		dispModePanel.add(lbl2);
		dispModePanel.add(dispSelected);
		dispModePanel.add(dispRelated);
		
		
		JPanel paramPanel = new JPanel();
		paramPanel.add(etcPropertyButton);
		etcPropertyButton.setMargin(new Insets(0, 10, 0, 10));
		etcPropertyButton.addActionListener(new ActionListener() {
			private ParameterSetWindow ps = null;
			public void actionPerformed(ActionEvent e) {
				// 子画面が開いていなければ生成
				if (!isSubWindow) {
					ps = new ParameterSetWindow();
				} else {
					ps.requestFocus();
				}
			}
		});
		
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		optionPanel.add(dispModePanel);
		optionPanel.add(paramPanel);
		
		// PackageView生成及び、初期化
		pkgView = new PackageViewPanel();
		pkgView.initAllRecInfo();
		
		queryTabPane.addTab("DB", parentPanel2);
		queryTabPane.setToolTipTextAt(TAB_ORDER_DB, "Query from DB.");
		queryTabPane.addTab("File", queryFilePane);
		queryTabPane.setToolTipTextAt(TAB_ORDER_FILE, "Query from user file.");
		queryTabPane.setSelectedIndex(TAB_ORDER_DB);
		queryTabPane.setFocusable(false);
		queryTabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {

				// プロットペイン初期化
				queryPlot.clear();
				compPlot.clear();
				resultPlot.clear();
				queryPlot.setPeaks(null, 0);
				compPlot.setPeaks(null, 1);
				resultPlot.setPeaks(null, 0);

				// PackageView初期化
				pkgView.initAllRecInfo();
				
				// DB Hitタブ関連初期化
				if (resultTabPane.getTabCount() > 0) {
					resultTabPane.setSelectedIndex(0);
				}
				DefaultTableModel dataModel = (DefaultTableModel) resultSorter
						.getTableModel();
				dataModel.setRowCount(0);
				hitLabel.setText(" ");

				// DBタブ、User Fileタブの選択済みレコード反映処理
				queryTabPane.update(queryTabPane.getGraphics());
				if (queryTabPane.getSelectedIndex() == TAB_ORDER_DB) {
					parentPanel2.update(parentPanel2.getGraphics());
					updateSelectQueryTable(queryDbTable);
				} else if (queryTabPane.getSelectedIndex() == TAB_ORDER_FILE) {
					queryFilePane.update(queryFilePane.getGraphics());
					updateSelectQueryTable(queryFileTable);
				}
			}
		});
		
		
		// レイアウト		
		JPanel queryPanel = new JPanel();
		queryPanel.setLayout(new BorderLayout());
		queryPanel.add(queryTabPane, BorderLayout.CENTER);
		queryPanel.add(optionPanel, BorderLayout.SOUTH);
		queryPanel.setMinimumSize(new Dimension(0, 170));

		JPanel jtp2Panel = new JPanel();
		jtp2Panel.setLayout(new BorderLayout());
		jtp2Panel.add(dbPanel, BorderLayout.CENTER);
		jtp2Panel.add(hitLabel, BorderLayout.SOUTH);
		jtp2Panel.setMinimumSize(new Dimension(0, 70));
		Color colorGreen = new Color(0, 128, 0);
		hitLabel.setForeground(colorGreen);

		resultTabPane.addTab("Result", jtp2Panel);
		resultTabPane.setToolTipTextAt(TAB_RESULT_DB, "Result of DB hit.");
		resultTabPane.setFocusable(false);
		
		queryPlot.setMinimumSize(new Dimension(0, 100));
		compPlot.setMinimumSize(new Dimension(0, 120));
		resultPlot.setMinimumSize(new Dimension(0, 100));
		int height = initAppletHight / 3;
		JSplitPane jsp_cmp2db = new JSplitPane(JSplitPane.VERTICAL_SPLIT, compPlot, resultPlot);
		JSplitPane jsp_qry2cmp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryPlot,
				jsp_cmp2db);
		jsp_cmp2db.setDividerLocation(height);
		jsp_qry2cmp.setDividerLocation(height - 25);
		jsp_qry2cmp.setMinimumSize(new Dimension(190, 0));
		
		viewTabPane.addTab("Compare View", jsp_qry2cmp);
		viewTabPane.addTab("Package View", pkgView);
		viewTabPane.setToolTipTextAt(TAB_VIEW_COMPARE, "Comparison of query and result spectrum.");
		viewTabPane.setToolTipTextAt(TAB_VIEW_PACKAGE, "Package comparison of query and result spectrum.");
		viewTabPane.setSelectedIndex(TAB_VIEW_COMPARE);
		viewTabPane.setFocusable(false);
		
		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryPanel,
				resultTabPane);
		jsp.setDividerLocation(310);
		jsp.setMinimumSize(new Dimension(180, 0));
		jsp.setOneTouchExpandable(true);
		
		JSplitPane jsp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jsp,
				viewTabPane);
		int divideSize = (int)(initAppletWidth * 0.4);
		divideSize = (divideSize >= 180) ? divideSize : 180;
		jsp2.setDividerLocation(divideSize);
		jsp2.setOneTouchExpandable(true);
		
		mainPanel.add(jsp2, BorderLayout.CENTER);
		add(mainPanel);

		queryPlot.setSearchPage(this);
		compPlot.setSearchPage(this);
		resultPlot.setSearchPage(this);
	}

	/**
	 * ファイル読み込み処理
	 * @param fileName ファイル名
	 */
	private void loadFile(String fileName) {
		seaqCompound = 0;
		seaqId = 0;
		String reqUrl = baseUrl + "jsp/SearchPage.jsp?file=" + fileName;
		ArrayList<String> lineList = new ArrayList<String>();
		
		try {
			
			URL url = new URL(reqUrl);
			URLConnection con = url.openConnection();

			// レスポンス取得
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = "";
			while ((line = in.readLine()) != null) {
				lineList.add(line);
			}
			in.close();
			
			// 1行も読み込めなかった場合
			if (lineList.size() == 0) {
				// ERROR：ファイルがありません
				JOptionPane.showMessageDialog(null, "No file.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		catch (MalformedURLException mue) {			// URL書式無効
			mue.printStackTrace();
			// ERROR：サーバーエラー
			JOptionPane.showMessageDialog(null, "Server error.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (IOException ie) {					// 入出力例外
			ie.printStackTrace();
			// ERROR：サーバーエラー
			JOptionPane.showMessageDialog(null, "Server error.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		DefaultTableModel dataModel = (DefaultTableModel) fileSorter.getTableModel();
		dataModel.setRowCount(0);

		Object[] row;
		String line = "";
		String peaksLine = "";
		UserFileData usrData = null;
		Vector<UserFileData>tmpUserDataList = new Vector<UserFileData>();
		int dataNum = 0;
		try {
			for (int i=0; i<lineList.size(); i++) {
				
				line = lineList.get(i);
				
				// コメント行読み飛ばし
				if (line.trim().startsWith("//")) {
					continue;
				}
				
				// レコード情報取得処理
				if (line.trim().indexOf(":") == -1 && line.trim().length() != 0) {
					if (usrData == null) {
						usrData = new UserFileData();
					}
					if (line.lastIndexOf(";") != -1) {
						peaksLine += line.trim();
					}
					else {
						peaksLine += line.trim() + ";";
					}
				}
				else if (line.trim().startsWith("Name:")) {
					if (usrData == null) {
						usrData = new UserFileData();
					}
					usrData.setName(line.substring(5).trim());
				}
				else if (line.trim().startsWith("ID:")) {
					if (usrData == null) {
						usrData = new UserFileData();
					}
					usrData.setId(line.substring(3).trim());
				}
				
				// レコード情報追加処理
				if (line.trim().length() == 0 || i == lineList.size()-1) {
					
					if (usrData != null) {
						
						dataNum++;
						
						// === ID ===
						if (usrData.getId().equals("")) {
							usrData.setId(createId());
						}
						
						// === 化合物名 ===
						if (usrData.getName().equals("")) {
							usrData.setName(createName());
						}
						
						if (peaksLine.length() != 0) {
							
							// ピーク情報加工(m/z昇順のm/zと強度の組み合わせ)
							double max = 0d;
							ArrayList<String> peakList = new ArrayList<String>(Arrays.asList(peaksLine.split(";")));
							for (int j = 0; j < peakList.size(); j++) {
								peakList.set(j, peakList.get(j).replaceAll("^ +", ""));
								peakList.set(j, peakList.get(j).replaceAll(" +", "\t"));
								
								// 最大強度保持
								if (max < Double.parseDouble(peakList.get(j).split("\t")[1])) {
									max = Double.parseDouble(peakList.get(j).split("\t")[1]);
								}
							}
							Collections.sort(peakList, new PeakComparator());
							
							// 強制的に強度を相対強度に変換
							for (int j = 0; j < peakList.size(); j++) {
								
								// m/z退避
								String tmpMz = peakList.get(j).split("\t")[0];
								
								// 元の強度
								String beforeVal = peakList.get(j).split("\t")[1];
								
								// 相対強度
								long tmpVal = Math.round(Double.parseDouble(beforeVal) / max * 999d);
								if (tmpVal > 999) { 
									tmpVal = 999;
								}
								if (tmpVal < 1) {
									tmpVal = 1;
								}
								String afterVal = String.valueOf(tmpVal);
								
								peakList.set(j, tmpMz + "\t" + afterVal);
							}
							usrData.setPeaks((String[])peakList.toArray(new String[peakList.size()]));
							
						}
						
						// ユーザデータ情報追加
						tmpUserDataList.add(usrData);
						
						// テーブル情報追加
						row = new Object[3];
						row[0] = String.valueOf(dataNum);
						row[1] = usrData.getName();
						row[2] = usrData.getId();
						dataModel.addRow(row);
					}
					
					usrData = null;
					peaksLine = "";
				}
			}
		}
		catch (Exception e) {
			System.out.println("Illegal file format.");
			e.printStackTrace();
			// WARNING：ファイルフォーマットが不正です
			JOptionPane.showMessageDialog(null, "Illegal file format.", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		userDataList = (UserFileData[])tmpUserDataList.toArray(new UserFileData[tmpUserDataList.size()]);
		queryTabPane.setSelectedIndex(TAB_ORDER_FILE);
	}

	/**
	 * ID生成
	 * IDを自動生成し返却する
	 * @return 化合物名
	 */
	private String createId() {
		String tmpId = "US";
		
		synchronized (this) {
			if (seaqId < Integer.MAX_VALUE) {
				seaqId++;
			} else {
				seaqId = 0;
			}
		}
		DecimalFormat df = new DecimalFormat("000000");
		return tmpId + df.format(seaqId);
	}
	
	/**
	 * 化合物名生成
	 * 化合物名を自動生成し返却する
	 * @return 化合物名
	 */
	private String createName() {
		String tmpName = "Compound_";

		synchronized (this) {
			if (seaqCompound < Integer.MAX_VALUE) {
				seaqCompound++;
			} else {
				seaqCompound = 0;
			}
		}
		DecimalFormat df = new DecimalFormat("000000");
		return tmpName + df.format(seaqCompound);
	}

	/**
	 * DB検索
	 * クエリーにヒットするピークのスペクトルをDBから検索する。
	 * @param ps ピーク情報
	 * @param precursor プリカーサー
	 * @param queryName クエリー化合物名
	 * @param queryKey クエリーレコードキー
	 */
	private void searchDb(String[] ps, String precursor, String queryName, String queryKey) {
		queryPlot.clear();
		compPlot.clear();
		resultPlot.clear();
		queryPlot.setPeaks(null, 0);
		compPlot.setPeaks(null, 1);
		resultPlot.setPeaks(null, 0);
		DefaultTableModel dataModel = (DefaultTableModel)resultSorter.getTableModel();
		dataModel.setRowCount(0);
		hitLabel.setText("");

		if (queryTabPane.getSelectedIndex() == TAB_ORDER_DB) {
			queryPlot.setSpectrumInfo(queryName, queryKey, precursor, PeakPanel.SP_TYPE_QUERY, false);	
		} else if (queryTabPane.getSelectedIndex() == TAB_ORDER_FILE) {
			queryPlot.setSpectrumInfo(queryName, queryKey, precursor, PeakPanel.SP_TYPE_QUERY, true);
		}
		
		// クエリ側スペクトルのピークがない場合
		if (ps.length == 0 || (ps.length == 1 && ps[0].split("\t")[0].equals("0") && ps[0].split("\t")[1].equals("0"))) {
			queryPlot.setNoPeak(true);
			hitLabel.setText(" 0 Hit.    ("
					+ ((PRECURSOR < 1) ? "" : "Precursor : " + PRECURSOR + ", ")
					+ "Tolerance : "
					+ TOLERANCE
					+ " "
					+ ((tolUnit1.isSelected()) ? tolUnit1.getText() : tolUnit2.getText()) + ", Cutoff threshold : "
					+ CUTOFF_THRESHOLD + ")");
			// マウスカーソルをデフォルトカーソルに
			this.setCursor(Cursor.getDefaultCursor());
			return;
		}

		// POSTデータを作成
		StringBuffer post = new StringBuffer();
		if (isRecInteg)
			post.append( "INTEG=true&" );
		else if (isRecActu)
			post.append( "INTEG=false&" );
		if (PRECURSOR > 0) {
			post.append( "PRE=" + PRECURSOR + "&");
		}
		post.append( "CUTOFF=" + CUTOFF_THRESHOLD + "&" );
		post.append( "TOLERANCE=" + TOLERANCE + "&" );
		if (tolUnit2.isSelected())
			post.append( "TOLUNIT=ppm&" );
		else
			post.append( "TOLUNIT=unit&" );
		post.append( "INST=" );
		StringBuffer instTmp = new StringBuffer();
		boolean isInstAll = true;
		for (Iterator i=isInstCheck.keySet().iterator(); i.hasNext(); ) {
			String key = (String)i.next();
			
			if ( (isInstCheck.get(key)) ) {
				if (instTmp.length() > 0) {
					instTmp.append( "," );
				}
				instTmp.append( key );
			} else {
				isInstAll = false;
			}
		}
		if (isInstAll) {
			if (instTmp.length() > 0) {
				instTmp.append( "," );
			}
			instTmp.append( "ALL" );
		}
		post.append( instTmp.toString() + "&" );
		
		post.append( "MS=" );
		StringBuffer msTmp = new StringBuffer();
		boolean isMsAll = true;
		for (Iterator i=isMsCheck.keySet().iterator(); i.hasNext(); ) {
			String key = (String)i.next();
			
			if ( (isMsCheck.get(key)) ) {
				if (msTmp.length() > 0) {
					msTmp.append( "," );
				}
				msTmp.append( key );
			} else {
				isMsAll = false;
			}
		}
		if (isMsAll) {
			if (msTmp.length() > 0) {
				msTmp.append( "," );
			}
			msTmp.append( "ALL" );
		}
		post.append( msTmp.toString() + "&" );
		
		
		if (isIonRadio.get("Posi")) {
			post.append( "ION=1&" );
		} else if (isIonRadio.get("Nega")) {
			post.append( "ION=-1&" );
		} else {
			post.append( "ION=0&");
		}
		
		post.append( "VAL=" );
		for (int i = 0; i < ps.length; i++) {
			post.append( ps[i].replace("\t", ",") + "@" );
		}

		// 画面操作を無効する
		setOperationEnbled(false);

		// 検索中ダイアログ表示する
		dlg.setVisible(true);

		this.param = post.toString();
		this.ps = ps;
		SwingWorker worker = new SwingWorker() {
			private ArrayList<String> result = null;

			public Object construct() {
				// サーブレット呼び出し-マルチスレッドでCGIを起動
				String cgiType = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_SEARCH];
				result = mbcommon.execMultiDispatcher(baseUrl, cgiType, SearchPage.this.param);
				return null;
			}

			public void finished() {
				// 画面操作無効を解除する
				setOperationEnbled(true);

				// 検索中ダイアログを非表示にする
				dlg.setVisible(false);

				int total = 0;
				if (result != null && result.size() > 0) {
					total = result.size();
					DefaultTableModel dataModel = (DefaultTableModel)resultSorter.getTableModel();

					// 検索結果をDBTableにセット
					siteList = new String[total];
					for (int i = 0; i < total; i++) {
						String line = (String) result.get(i);
						String[] item = line.split("\t");
						String id = item[0];
						String name = item[1];

						// Score, Hit
						String score = "";
						String hit = "";
						String hitScore = item[2];
						int pos = hitScore.indexOf(".");
						if (pos > 0) {
							score = "0" + hitScore.substring(pos);
							hit = hitScore.substring(0, pos);
						} else {
							score = "0";
							hit = hitScore;
						}
						Double dblScore = Double.parseDouble(score);
						Integer ihit = Integer.parseInt(hit);

						// Ion
						int iIon = Integer.parseInt(item[3]);
						String ion = "";
						if (iIon > 0) {
							ion = "P";
						} else if (iIon < 0) {
							ion = "N";
						} else {
							ion = "-";
						}

						// SiteName
						String siteName = siteNameList[Integer.parseInt(item[4])];
						siteList[i] = item[4];

						// Name, Score, Hit, ID, Ion, SiteName, No.
						Object[] rowData = { name, dblScore, ihit, id, ion, siteName, (i + 1) };
						dataModel.addRow(rowData);
					}
				}

				PeakData peak = new PeakData(SearchPage.this.ps);
				queryPlot.setPeaks(peak, 0);
				compPlot.setPeaks(peak, 0);
				resultTabPane.setSelectedIndex(0);
				setAllPlotAreaRange(queryPlot);
				SearchPage.this.setCursor(Cursor.getDefaultCursor());
				hitLabel.setText(" "
						+ total
						+ " Hit.    ("
						+ ((PRECURSOR < 1) ? "" : "Precursor : " + PRECURSOR + ", ")
						+ "Tolerance : "
						+ TOLERANCE
						+ " "
						+ ((tolUnit1.isSelected()) ? tolUnit1.getText()
								: tolUnit2.getText()) + ", Cutoff threshold : "
						+ CUTOFF_THRESHOLD + ")");
				hitLabel.setToolTipText(" "
						+ total
						+ " Hit.    ("
						+ ((PRECURSOR < 1) ? "" : "Precursor : " + PRECURSOR + ", ")
						+ "Tolerance : "
						+ TOLERANCE
						+ " "
						+ ((tolUnit1.isSelected()) ? tolUnit1.getText()
								: tolUnit2.getText()) + ", Cutoff threshold : "
						+ CUTOFF_THRESHOLD + ")");
			}
		};
		worker.start();
	}
	
	/**
	 * クエリーの選択状態を更新
	 */
	private void updateSelectQueryTable(JTable tbl) {
		
		// マウスカーソルを砂時計に
		this.setCursor(waitCursor);
		
		int selRow = tbl.getSelectedRow();
		if (selRow >= 0) {
			tbl.clearSelection();
			Color defColor = tbl.getSelectionBackground();
			tbl.setRowSelectionInterval(selRow, selRow);
			tbl.setSelectionBackground(Color.PINK);
			tbl.update(tbl.getGraphics());
			tbl.setSelectionBackground(defColor);
		}
		// マウスカーソルをデフォルトカーソルに
		this.setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * スペクトル取得
	 * DBからスペクトルを取得する
	 * @param searchName 
	 */
	private void getSpectrumForQuery(String searchName) {
		
		String param = "";
		if (!searchName.equals("")) {
			String wc = "&wc=";
			boolean wcStart = false;
			boolean wcEnd = false;
			if (searchName.substring(0, 1).equals("*")) {
				wcStart = true;
			}
			if (searchName.substring(searchName.length() - 1).equals("*")) {
				wcEnd = true;
			}

			if (wcStart) {
				if (wcEnd) {
					wc += "both";
				} else {
					wc += "start";
				}
			} else {
				if (wcEnd) {
					wc += "end";
				} else {
					wc = "";
				}
			}
			searchName = searchName.replace("*", "");
			param = "name=" + searchName + wc;
		}

		// サーブレット呼び出し-マルチスレッドでCGIを起動
		String cgiType = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GNAME];
		ArrayList<String> result = mbcommon.execMultiDispatcher(baseUrl, cgiType, param);
		DefaultTableModel dataModel = (DefaultTableModel) querySorter.getTableModel();
		dataModel.setRowCount(0);
		if (result == null || result.size() == 0) {
			return;
		}

		// ソート
		Collections.sort(result);

		nameList.clear();
		for (int i = 0; i < result.size(); i++) {
			String nameId = (String) result.get(i);
			String[] cutNameId = nameId.split("\t");
			String name = cutNameId[0];

			String id = cutNameId[1];
			String site = cutNameId[2];

			String[] cutIdNameSite = new String[] { id, name, site };
			nameList.add(cutIdNameSite);

			site = siteNameList[Integer.parseInt(site)];
			String[] idNameSite2 = new String[] { id, name, site, String.valueOf(i + 1) };

			// 取得値をテーブルにセット
			dataModel.addRow(idNameSite2);
		}
	}
	
	/**
	 * レコードページ表示
	 * @param selectIndex 選択行インデックス
	 */
	private void showRecordPage(JTable eventTbl) {
		int selRows[] = eventTbl.getSelectedRows();
		int idCol = eventTbl.getColumnModel().getColumnIndex(COL_LABEL_ID);
		int siteCol = eventTbl.getColumnModel().getColumnIndex(COL_LABEL_CONTRIBUTOR);
		
		// 選択された行の値(id)を取得
		String id = (String)eventTbl.getValueAt(selRows[0], idCol);
		String siteName = (String)eventTbl.getValueAt(selRows[0], siteCol);
		String site = "0";
		for (int i=0; i<siteNameList.length; i++) {
			if (siteName.equals(siteNameList[i])) {
				site = Integer.toString(i);
				break;
			}
		}

		// CGI呼び出し
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
		String reqUrl = baseUrl + "jsp/" + MassBankCommon.DISPATCHER_NAME
				+ "?type=" + typeName + "&id=" + id + "&site=" + site;
		try {
			context.showDocument(new URL(reqUrl), "_blank");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * レコードリスト用ポップアップ表示
	 * @param e マウスイベント
	 */
	private void recListPopup(MouseEvent e) {
		JTable tbl = null;
		JScrollPane pane = null;
		try {
			tbl = (JTable)e.getSource();
		}
		catch (ClassCastException cce) {
			pane = (JScrollPane)e.getSource();
			if (pane.equals(queryDbPane)) {
				tbl = queryDbTable;
			}
			else if (pane.equals(resultPane)) {
				tbl = resultTable;
			}
			if (pane.equals(queryFilePane)) {
				tbl = queryFileTable;
			}
		}
		int rowCnt = tbl.getSelectedRows().length;
		
		JMenuItem item1 = new JMenuItem("Show Record");
		item1.addActionListener(new PopupShowRecordListener(tbl));
		JMenuItem item2 = new JMenuItem("Multiple Display");
		item2.addActionListener(new PopupMultipleDisplayListener(tbl));
		
		// 可視設定
		if (tbl.equals(queryFileTable)) {
			item1.setEnabled(false);
			item2.setEnabled(false);
		}
		else if (rowCnt == 0) {
			item1.setEnabled(false);
			item2.setEnabled(false);
		}
		else if (rowCnt == 1) {
			item1.setEnabled(true);
			item2.setEnabled(false);
		}
		else if (rowCnt > 1) {
			item1.setEnabled(false);
			item2.setEnabled(true);
		}
		
		// ポップアップメニュー表示
		JPopupMenu popup = new JPopupMenu();
		popup.add(item1);
		if (tbl.equals(resultTable)) {
			popup.add(item2);
		}
		popup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	/**
	 * Precursor m/z情報初期化
	 */
	private void initPreInfo() {
//		// Cookie情報用リストにCookieからPrecursor状態を取得
//		ArrayList<String> valueList = cm.getCookie(COOKIE_PRE);
//		
//		// Cookieが存在する場合
//		if (valueList.size() != 0) {
//			try {
//				PRECURSOR = Integer.valueOf(valueList.get(0));
//			} catch (Exception e) {
//				// PRECURSORはデフォルト値を使用
//			}
//		} else {
			PRECURSOR = -1;
//			valueList.add(String.valueOf(PRECURSOR));
//			cm.setCookie(COOKIE_PRE, valueList);
//		}
	}
	
	/**
	 * Tolerance情報初期化
	 */
	private void initTolInfo() {
		// Cookie情報用リストにCookieからTolerance状態を取得
		ArrayList<String> valueList = cm.getCookie(COOKIE_TOL);
		
		// Cookieが存在する場合
		if (valueList.size() != 0) {
			try {
				TOLERANCE = Float.valueOf(valueList.get(0));
			} catch (Exception e) {
				// TOLERANCEはデフォルト値を使用
			}
			
			if (valueList.contains(tolUnit2.getText())) {
				tolUnit1.setSelected(false);
				tolUnit2.setSelected(true);
			} else {
				tolUnit1.setSelected(true);
				tolUnit2.setSelected(false);
			}
		} else {
			TOLERANCE = 0.3f;
			valueList.add(String.valueOf(TOLERANCE));
			if (tolUnit1.isSelected()) {
				valueList.add(tolUnit1.getText());	
			}
			else {
				valueList.add(tolUnit2.getText());
			}
			cm.setCookie(COOKIE_TOL, valueList);
		}
	}
	
	/**
	 * Cutoff Threshold情報初期化
	 */
	private void initCutoffInfo() {
		// Cookie情報用リストにCookieからCutoff Threshold状態を取得
		ArrayList<String> valueList = cm.getCookie(COOKIE_CUTOFF);
		
		// Cookieが存在する場合
		if (valueList.size() != 0) {
			try {
				CUTOFF_THRESHOLD = Integer.valueOf(valueList.get(0));
			} catch (Exception e) {
				// CUTOFF_THRESHOLDはデフォルト値を使用
			}
		} else {
			CUTOFF_THRESHOLD = 5;
			valueList.add(String.valueOf(CUTOFF_THRESHOLD));
			cm.setCookie(COOKIE_CUTOFF, valueList);
		}
	}
	
	/**
	 * 装置種別情報初期化
	 * データベースから全装置種別情報を取得して対応するチェックボックスを生成
	 * チェックボックスの選択状態は[Cookie情報]＞[デフォルト]の優先順で選択状態を初期化する
	 * チェックボックスが1つも選択されない場合は強制的に全て選択した状態で初期化する
	 */
	private void initInstInfo() {
		instCheck = new LinkedHashMap<String, JCheckBox>();
		isInstCheck = new HashMap<String, Boolean>();
		instGroup = instInfo.getTypeGroup();
		
		// Cookie情報用リストにCookieから装置種別の選択状態を取得
		ArrayList<String> valueGetList = cm.getCookie(COOKIE_INST);
		ArrayList<String> valueSetList = new ArrayList<String>();
		
		boolean checked = false;
		
		for (Iterator i=instGroup.keySet().iterator(); i.hasNext(); ) {
			String key = (String)i.next();
			
			List<String> list = instGroup.get(key);
			for ( int j = 0; j < list.size(); j++ ) {
				String val = list.get(j);
			
				JCheckBox chkBox;
				
				// Cookieが存在する場合
				if (valueGetList.size() != 0) {
					if (valueGetList.contains(val)) {
						chkBox = new JCheckBox(val, true);
						checked = true;
					} else {
						chkBox = new JCheckBox(val, false);
					}
				} else {
					if ( isDefaultInst(val) ) {	// デフォルト装置種別の場合 
						chkBox = new JCheckBox(val, true);
						checked = true;
						valueSetList.add(val);
					} else {
						chkBox = new JCheckBox(val, false);
					}
				}
				
				instCheck.put(val, chkBox);
				isInstCheck.put(val, chkBox.isSelected());
			}
		}
		
		// 装置種別がデータベースに登録されていない場合
		if (instCheck.size() == 0 && isInstCheck.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"Instrument Type is not registered in the database.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// ここまでの処理で装置種別が1つも選択されていない場合は強制的に全て選択する
		if ( !checked ) {
			for (Iterator i=instCheck.keySet().iterator(); i.hasNext();) {
				String key = (String)i.next();
				
				((JCheckBox)instCheck.get(key)).setSelected(true);
				isInstCheck.put(key, true);
				valueSetList.add(key);
			}
		}
		
		// 初回読み込み時にCookie情報が存在しない場合はCookieに設定
		if (valueGetList.size() == 0) {
			cm.setCookie(COOKIE_INST, valueSetList);
		}
	}
	
	/**
	 * MS種別情報初期化
	 * データベースから全MS種別情報を取得して対応するチェックボックスを生成
	 * チェックボックスの選択状態は[Cookie情報]＞[デフォルト]の優先順で選択状態を初期化する
	 * チェックボックスが1つも選択されない場合は強制的に全て選択した状態で初期化する
	 */
	private void initMsInfo() {
		msCheck = new LinkedHashMap<String, JCheckBox>();
		isMsCheck = new HashMap<String, Boolean>();
		
		// Cookie情報用リストにCookieからMS種別の選択状態を取得
		ArrayList<String> valueGetList = cm.getCookie(COOKIE_MS);
		ArrayList<String> valueSetList = new ArrayList<String>();
		
		boolean checked = false;
		
		List<String> list = Arrays.asList(instInfo.getMsAll());
		for ( int j=0; j<list.size(); j++ ) {
			String val = list.get(j);
		
			JCheckBox chkBox;
			
			// Cookieが存在する場合
			if (valueGetList.size() != 0) {
				if (valueGetList.contains(val)) {
					chkBox = new JCheckBox(val, true);
					checked = true;
				} else {
					chkBox = new JCheckBox(val, false);
				}
			} else {
				chkBox = new JCheckBox(val, true);
				checked = true;
				valueSetList.add(val);
			}
			
			msCheck.put(val, chkBox);
			isMsCheck.put(val, chkBox.isSelected());
		}
		
		// MS種別がデータベースに登録されていない場合
		if (msCheck.size() == 0 && isMsCheck.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"MS Type is not registered in the database.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// ここまでの処理でMS種別が1つも選択されていない場合は強制的に全て選択する
		if ( !checked ) {
			for (Iterator i=msCheck.keySet().iterator(); i.hasNext();) {
				String key = (String)i.next();
				((JCheckBox)msCheck.get(key)).setSelected(true);
				isMsCheck.put(key, true);
				valueSetList.add(key);
			}
		}
		
		// 初回読み込み時にCookie情報が存在しない場合はCookieに設定
		if (valueGetList.size() == 0) {
			cm.setCookie(COOKIE_MS, valueSetList);
		}
	}
	
	/**
	 * イオン種別情報初期化
	 */
	private void initIonInfo() {
		final String keyPosi = "Posi";
		final String keyNega = "Nega";
		final String keyBoth = "Both";
		
		ionRadio = new LinkedHashMap<String, JRadioButton>();
		isIonRadio = new HashMap<String, Boolean>();
		
		// Cookie情報用リストにCookieからイオン種別の選択状態を取得
		ArrayList<String> valueList = cm.getCookie(COOKIE_ION);
		
		JRadioButton ionPosi = new JRadioButton("Positive");
		JRadioButton ionNega = new JRadioButton("Negative");
		JRadioButton ionBoth = new JRadioButton("Both");
		
		// Cookieが存在する場合
		if (valueList.size() != 0) {
			ionPosi.setSelected(valueList.contains(keyPosi));
			ionNega.setSelected(valueList.contains(keyNega));
			ionBoth.setSelected(valueList.contains(keyBoth));
		}
		else {
			ionPosi.setSelected(true);
			ionNega.setSelected(false);
			ionBoth.setSelected(false);
			valueList.add(keyPosi);
			cm.setCookie(COOKIE_ION, valueList);
		}
		
		ionRadio.put(keyPosi, ionPosi);
		ionRadio.put(keyNega, ionNega);
		ionRadio.put(keyBoth, ionBoth);
		isIonRadio.put(keyPosi, ionPosi.isSelected());
		isIonRadio.put(keyNega, ionNega.isSelected());
		isIonRadio.put(keyBoth, ionBoth.isSelected());
	}

	/**
	 * デフォルト装置種別チェック
	 * 装置種別に"ESI"、"APPI"、"MALDI"を含むものをデフォルト装置種別とする
	 * @param inst 装置種別
	 */
	private boolean isDefaultInst(String inst) {
		
		if ( inst.indexOf("ESI") != -1 ||
			 inst.indexOf("APPI") != -1 ||
			 inst.indexOf("MALDI") != -1 ) {
			
			return true;
		}
		return false;
	}
	
	/**
	 * アプレットのフレームを取得
	 */
	protected Frame getFrame() {
		for (Container p = getParent(); p != null; p = p.getParent()) {
			if (p instanceof Frame) return (Frame)p;
		}
		return null;
	}

	/**
	 * 画面操作有効・無効設定
	 */
	private void setOperationEnbled(boolean value) {
		queryFileTable.setEnabled(value);
		queryDbTable.setEnabled(value);
		etcPropertyButton.setEnabled(value);
		btnName.setEnabled(value);
		btnAll.setEnabled(value);
		dispSelected.setEnabled(value);
		dispRelated.setEnabled(value);
		queryTabPane.setEnabled(value);
		resultTabPane.setEnabled(value);
		viewTabPane.setEnabled(value);
		lbl2.setEnabled(value);
	}

	/**
	 * ParameterSetWindowクラス
	 */
	class ParameterSetWindow extends JFrame {
		
		private final int LABEL_SIZE_L = 0;
		private final int LABEL_SIZE_M = 1;
		private final int LABEL_SIZE_S = 2;
		private final JTextField preField;
		private final JTextField tolField;
		private final JTextField cutoffField;
		private boolean isTolUnit1 = tolUnit1.isSelected();
		private boolean isTolUnit2 = tolUnit2.isSelected();
		
		/**
		 * コンストラクタ
		 */
		public ParameterSetWindow() {
			
			// ウィンドウサイズ固定
			setResizable(false);
			
			// キーリスナー登録用コンポーネントリスト
			ArrayList<Component> keyListenerList = new ArrayList<Component>();
			keyListenerList.add(this);
			
			// メインコンテナー取得
			Container container= getContentPane();
			initMainContainer(container);
			
			JPanel delimPanel;
			JPanel labelPanel;
			JPanel itemPanel;
			
			
			// Tolerance
			labelPanel = newLabelPanel("Tolerance of m/z", " Tolerance of m/z. ", LABEL_SIZE_L, 2);
			
			JPanel tolPanel = new JPanel();
			tolPanel.setLayout(new BoxLayout(tolPanel, BoxLayout.X_AXIS));
			tolField = new JTextField(String.valueOf(TOLERANCE), 5);
			tolField.setHorizontalAlignment(JTextField.RIGHT);
			keyListenerList.add(tolField);
			keyListenerList.add(tolUnit1);
			keyListenerList.add(tolUnit2);
			ButtonGroup tolGroup = new ButtonGroup();
			tolGroup.add(tolUnit1);
			tolGroup.add(tolUnit2);
			tolPanel.add(tolUnit1);
			tolPanel.add(tolUnit2);
			
			itemPanel = new JPanel();
			initItemPanel(itemPanel);
			itemPanel.add(wrappTextPanel(tolField), itemPanelGBC(0d, 0d, 0, 0, 1, 1));
			itemPanel.add(tolPanel, itemPanelGBC(0d, 0d, 1, 0, 1, 1));
			itemPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 2, 0, GridBagConstraints.REMAINDER, 1));
			
			delimPanel = new JPanel();
			initDelimPanel(delimPanel, true);
			delimPanel.add(labelPanel, delimPanelGBC(0d, 0d, 0, 0, 1, 1));
			delimPanel.add(itemPanel, delimPanelGBC(0d, 0d, 1, 0, 1, 1));
			delimPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 2, 0, GridBagConstraints.REMAINDER, 1));
			
			container.add(delimPanel, mainContainerGBC(0, 0, 1, 1));
			
			
			
			// Cutoff Thresholds
			labelPanel = newLabelPanel("Cutoff Threshold", " Cutoff threshold of intensities. ", LABEL_SIZE_L, 2);
			
			cutoffField = new JTextField(String.valueOf(CUTOFF_THRESHOLD), 5);
			cutoffField.setHorizontalAlignment(JTextField.RIGHT);
			keyListenerList.add(cutoffField);
			
			itemPanel = new JPanel();
			initItemPanel(itemPanel);
			itemPanel.add(wrappTextPanel(cutoffField), itemPanelGBC(0d, 0d, 0, 0, 1, 1));
			itemPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 1, 0, GridBagConstraints.REMAINDER, 1));
			
			delimPanel = new JPanel();
			initDelimPanel(delimPanel, true);
			delimPanel.add(labelPanel, delimPanelGBC(0d, 0d, 0, 0, 1, 1));
			delimPanel.add(itemPanel, delimPanelGBC(0d, 0d, 1, 0, 1, 1));
			delimPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 2, 0, GridBagConstraints.REMAINDER, 1));
			
			container.add(delimPanel, mainContainerGBC(0, 1, 1, 1));
			
			
			
			// Instrument Type
			labelPanel = newLabelPanel("Instrument Type", " Instrument type. ", LABEL_SIZE_L, 2);
			
			final JCheckBox chkBoxInstAll = new JCheckBox("All");
			chkBoxInstAll.setSelected(isInstAll());
			final JCheckBox chkBoxInstDefault = new JCheckBox("Default");
			chkBoxInstDefault.setSelected(isInstDefault());
			keyListenerList.add(chkBoxInstAll);
			keyListenerList.add(chkBoxInstDefault);

			itemPanel = new JPanel();
			initItemPanel(itemPanel);
			itemPanel.add(chkBoxInstAll, itemPanelGBC(0d, 0d, 0, 0, 1, 1));
			itemPanel.add(new JPanel(), itemPanelGBC(0.1d, 0d, 1, 0, 1, 1));
			itemPanel.add(chkBoxInstDefault, itemPanelGBC(0d, 0d, 2, 0, 1, 1));
			itemPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 3, 0, GridBagConstraints.REMAINDER, 0));
			
			delimPanel = new JPanel();
			initDelimPanel(delimPanel, true);
			delimPanel.add(labelPanel, delimPanelGBC(0d, 0d, 0, 0, 1, 1));
			delimPanel.add(itemPanel, delimPanelGBC(0d, 0d, 1, 0, 1, 1));
			
			JPanel instPanel = new JPanel();
			initItemPanel(instPanel);
			
			int keyNum = 0;
			boolean isSep = false;
			for (Iterator i=instGroup.keySet().iterator(); i.hasNext();) {
				String key = (String)i.next();
				
				itemPanel = new JPanel();
				initItemPanel(itemPanel);
				
				
				// 小分類単位のコンポーネント追加処理
				List<String> list = instGroup.get(key);
				int valNum = 0;
				for ( int j = 0; j < list.size(); j++ ) {
					String val = list.get(j);
					
					// セパレータ挿入
					if (keyNum != 0 && valNum == 0) {
						itemPanel.add(new JSeparator(), itemPanelGBC(0d, 0d, 0, valNum, 3, 1));
						valNum += 1;
						isSep = true;
					}
					
					JCheckBox chkBox = (JCheckBox)instCheck.get(val);
					keyListenerList.add(chkBox);
					itemPanel.add(chkBox, itemPanelGBC(0d, 0d, 1, valNum, 1, 1));
					valNum += 1;
				}
				
				
				// 大分類単位のコンポーネント追加処理
				if (valNum > 0) {
					labelPanel = newLabelPanel(key, null, LABEL_SIZE_S, 2);
					if (isSep) {
						itemPanel.add(labelPanel, itemPanelGBC(0d, 0d, 0, 1, 1, 1));
					} else {
						itemPanel.add(labelPanel, itemPanelGBC(0d, 0d, 0, 0, 1, 1));
					}
					itemPanel.add(new JPanel(), itemPanelGBC(1d, 1d, 2, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER));
					instPanel.add(itemPanel, itemPanelGBC(0d, 0d, 0, keyNum, 1, 1));
					keyNum += 1;
					isSep = false;
				}
			}
			instPanel.add(new JPanel(), itemPanelGBC(1d, 1d, 0, keyNum, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER));
			JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setPreferredSize(new Dimension(240, 250));
			scroll.getVerticalScrollBar().setUnitIncrement(60);
			scroll.setViewportView(instPanel);
			
			delimPanel.add(scroll, delimPanelGBC(0d, 0d, 1, 1, 1, 1));
			delimPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 2, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER));
			
			container.add(delimPanel, mainContainerGBC(0, 2, 1, 1));
			
			
			
			// MS Type
			labelPanel = newLabelPanel("MS Type", " MS type. ", LABEL_SIZE_L, 2);
			
			JPanel msPanel = new JPanel();
			msPanel.setLayout(new BoxLayout(msPanel, BoxLayout.X_AXIS));
			
			final JCheckBox chkBoxMsAll = new JCheckBox("All");
			chkBoxMsAll.setSelected(isMsAll());
			keyListenerList.add(chkBoxMsAll);
			msPanel.add(chkBoxMsAll);
			
			for (Iterator i=msCheck.keySet().iterator(); i.hasNext();) {
				String key = (String)i.next();
				
				JCheckBox chkBox = (JCheckBox)msCheck.get(key);
				keyListenerList.add(chkBox);
				msPanel.add(chkBox);
				msPanel.add(new JLabel(" "));
			}
			
			itemPanel = new JPanel();
			initItemPanel(itemPanel);
			itemPanel.add(msPanel, itemPanelGBC(0d, 0d, 0, 0, 1, 1));
			itemPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 1, 0, GridBagConstraints.REMAINDER, 1));

			delimPanel = new JPanel();
			initDelimPanel(delimPanel, true);
			delimPanel.add(labelPanel, delimPanelGBC(0d, 0d, 0, 0, 1, 1));
			delimPanel.add(itemPanel, delimPanelGBC(0d, 0d, 1, 0, 1, 1));
			delimPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 2, 0, GridBagConstraints.REMAINDER, 1));
			
			container.add(delimPanel, mainContainerGBC(0, 3, 1, 1));
			
			
			
			// Ion Mode
			labelPanel = newLabelPanel("Ion Mode", " Ion mode. ", LABEL_SIZE_L, 2);
			
			JPanel ionPanel = new JPanel();
			ionPanel.setLayout(new BoxLayout(ionPanel, BoxLayout.X_AXIS));
			
			ButtonGroup ionGroup = new ButtonGroup();
			for (Iterator i=ionRadio.keySet().iterator(); i.hasNext();) {
				String key = (String)i.next();
				
				JRadioButton rdoBtn = (JRadioButton)ionRadio.get(key);
				keyListenerList.add(rdoBtn);
				ionGroup.add(rdoBtn);
				ionPanel.add(rdoBtn);
			}
			
			itemPanel = new JPanel();
			initItemPanel(itemPanel);
			itemPanel.add(ionPanel, itemPanelGBC(0d, 0d, 0, 0, 1, 1));
			itemPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 1, 0, GridBagConstraints.REMAINDER, 1));

			delimPanel = new JPanel();
			initDelimPanel(delimPanel, true);
			delimPanel.add(labelPanel, delimPanelGBC(0d, 0d, 0, 0, 1, 1));
			delimPanel.add(itemPanel, delimPanelGBC(0d, 0d, 1, 0, 1, 1));
			delimPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 2, 0, GridBagConstraints.REMAINDER, 1));
			
			container.add(delimPanel, mainContainerGBC(0, 4, 1, 1));
			
			
			
			// Precursor m/z
			labelPanel = newLabelPanel("Precursor m/z", " Precursor m/z. ", LABEL_SIZE_L, 2);
			
			preField = new JTextField(((PRECURSOR < 0) ? "" : String.valueOf(PRECURSOR)), 5);
			preField.setHorizontalAlignment(JTextField.RIGHT);
			keyListenerList.add(preField);
			
			itemPanel = new JPanel();
			initItemPanel(itemPanel);
			itemPanel.add(wrappTextPanel(preField), itemPanelGBC(0d, 0d, 0, 0, 1, 1));
			itemPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 2, 0, GridBagConstraints.REMAINDER, 1));
			
			delimPanel = new JPanel();
			initDelimPanel(delimPanel, true);
			delimPanel.add(labelPanel, delimPanelGBC(0d, 0d, 0, 0, 1, 1));
			delimPanel.add(itemPanel, delimPanelGBC(0d, 0d, 1, 0, 1, 1));
			delimPanel.add(new JPanel(), itemPanelGBC(1d, 0d, 2, 0, GridBagConstraints.REMAINDER, 1));
			
			container.add(delimPanel, mainContainerGBC(0, 5, 1, 1));
			
			
			
			// ボタン
			final JButton okButton = new JButton("OK");
			keyListenerList.add(okButton);
			final JButton cancelButton = new JButton("Cancel");
			keyListenerList.add(cancelButton);
			JPanel btnPanel = new JPanel();
			btnPanel.add(okButton);
			btnPanel.add(cancelButton);
			
			container.add(btnPanel, mainContainerGBC(0, 6, 1, 1));
			
			
			// 装置種別Allチェックリスナー
			chkBoxInstAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (Iterator i=instCheck.keySet().iterator(); i.hasNext(); ) {
						String key = (String)i.next();
						if (chkBoxInstAll.isSelected()) {
							((JCheckBox)instCheck.get(key)).setSelected(true);
						} else {
							((JCheckBox)instCheck.get(key)).setSelected(false);
						}
					}
					chkBoxInstDefault.setSelected(isInstDefault());
				}
			});
			
			// 装置種別Defaultチェックリスナー
			chkBoxInstDefault.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (Iterator i=instCheck.keySet().iterator(); i.hasNext(); ) {
						String key = (String)i.next();
						if (chkBoxInstDefault.isSelected()) {
							// Allチェックをはずしてデフォルト選択
							chkBoxInstAll.setSelected(false);
							if ( isDefaultInst(key) ) {
								((JCheckBox)instCheck.get(key)).setSelected(true);
							} else {
								((JCheckBox)instCheck.get(key)).setSelected(false);	
							}
						} else {
							((JCheckBox)instCheck.get(key)).setSelected(false);
						}
					}
					chkBoxInstAll.setSelected(isInstAll());
				}
			});
			
			// 装置種別チェックリスナー
			for (Iterator i=instCheck.keySet().iterator(); i.hasNext();) {
				String key = (String)i.next();
				((JCheckBox)instCheck.get(key)).addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						chkBoxInstAll.setSelected(isInstAll());
						chkBoxInstDefault.setSelected(isInstDefault());
					}
				});
			}
			
			// MS種別Allチェックリスナー
			chkBoxMsAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (Iterator i=msCheck.keySet().iterator(); i.hasNext(); ) {
						String key = (String)i.next();
						if (chkBoxMsAll.isSelected()) {
							((JCheckBox)msCheck.get(key)).setSelected(true);
						} else {
							((JCheckBox)msCheck.get(key)).setSelected(false);
						}
					}
				}
			});
			
			// MS種別チェックリスナー
			for (Iterator i=msCheck.keySet().iterator(); i.hasNext();) {
				String key = (String)i.next();
				((JCheckBox)msCheck.get(key)).addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						chkBoxMsAll.setSelected(isMsAll());
					}
				});
			}
			
			// OKボタンリスナー
			okButton.addActionListener(new ActionListener() {
				private final Color defColor = okButton.getBackground();
				private void startProc() {
					// ボタンの色を変更
					okButton.setBackground(Color.PINK);
					okButton.update(okButton.getGraphics());
					// マウスカーソルを砂時計に
					ParameterSetWindow.this.setCursor(waitCursor);
				}
				private void endProc() {
					// マウスカーソルをデフォルトカーソルに
					if (!ParameterSetWindow.this.getCursor().equals(Cursor.getDefaultCursor())) {
						ParameterSetWindow.this.setCursor(Cursor.getDefaultCursor());
					}
					// ボタンの色を戻す
					okButton.setBackground(defColor);
				}
				public void actionPerformed(ActionEvent e) {
					
					startProc();
					
					// 入力チェック
					try {
						tolField.setText(tolField.getText().trim());
						float num = Float.parseFloat(tolField.getText());
						if (num < 0) {
							JOptionPane.showMessageDialog(null,
									"[Tolerance]  Value must be an positive numerical value.",
									"Warning",
									JOptionPane.WARNING_MESSAGE);
							endProc();
							return;
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null,
								"[Tolerance]  Value must be an numerical value.",
								"Warning",
								JOptionPane.WARNING_MESSAGE);
						endProc();
						return;
					}
					try {
						cutoffField.setText(cutoffField.getText().trim());
						int num = Integer.parseInt(cutoffField.getText());
						if (num < 0) {
							JOptionPane.showMessageDialog(null,
									"[Cutoff Threshold]  Value must be an positive integer.",
									"Warning",
									JOptionPane.WARNING_MESSAGE);
							endProc();
							return;
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null,
								"[Cutoff Threshold]  Value must be an integer.",
								"Warning",
								JOptionPane.WARNING_MESSAGE);
						endProc();
						return;
					}
					if (instCheck.size() == 0) {
						JOptionPane.showMessageDialog(null,
								"[Instrument Type]  Instrument type is not registered in the database.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
						endProc();
						return;							
					}
					if (!isInstCheck()) {
						JOptionPane.showMessageDialog(null,
								"[Instrument Type]  Select one or more checkbox.",
								"Warning",
								JOptionPane.WARNING_MESSAGE);
						endProc();
						return;	
					}
					if (msCheck.size() == 0) {
						JOptionPane.showMessageDialog(null,
								"[MS Type]  MS type is not registered in the database.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
						endProc();
						return;							
					}
					if (!isMsCheck()) {
						JOptionPane.showMessageDialog(null,
								"[MS Type]  Select one or more checkbox.",
								"Warning",
								JOptionPane.WARNING_MESSAGE);
						endProc();
						return;	
					}
					try {
						preField.setText(preField.getText().trim());
						if (!preField.getText().equals("")) {
							int num = Integer.parseInt(preField.getText());
							if (num < 1) {
								JOptionPane.showMessageDialog(null,
										"[Precursor m/z]  Value must be an integer of 1 or more.",
										"Warning",
										JOptionPane.WARNING_MESSAGE);
								endProc();
								return;								
							}
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null,
								"[Precursor m/z]  Value must be an integer of 1 or more.",
								"Warning",
								JOptionPane.WARNING_MESSAGE);
						endProc();
						return;
					}
					
					
					// 条件設定に変更があった場合
					if (isPreChange()
							|| isTolChange()
							|| isCutoffChange()
							|| isInstChange()
							|| isMsChange()
							|| isIonChange()) {
						
						preChange(true);
						tolChange(true);
						cutoffChange(true);
						instChange(true);
						msChange(true);
						ionChange(true);

						// クエリーの選択状態の更新処理
						resultPlot.setSpectrumInfo("", "", "", "", false);
						switch (queryTabPane.getSelectedIndex()) {
						case TAB_ORDER_DB :						// DBタブ選択時
							updateSelectQueryTable(queryDbTable);
							break;
						case TAB_ORDER_FILE :						// FILEタブ選択時
							updateSelectQueryTable(queryFileTable);
							break;
						}
					}
					
					endProc();

					dispose();
					isSubWindow = false;
				}
			});
			
			// Cancelボタンリスナー
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// 設定情報を戻す
					preChange(false);
					tolChange(false);
					cutoffChange(false);
					instChange(false);
					msChange(false);
					ionChange(false);
					
					dispose();
					isSubWindow = false;
				}
			});

			// キーリスナー追加
			for (int i = 0; i < keyListenerList.size(); i++) {
				keyListenerList.get(i).addKeyListener(new KeyAdapter() {
					public void keyReleased(KeyEvent e) {
						// Escキーリリース
						if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
							// 設定情報を戻す
							preChange(false);
							tolChange(false);
							cutoffChange(false);
							instChange(false);
							msChange(false);
							ionChange(false);
							
							dispose();
							isSubWindow = false;
						}
					}
				});
			}
			setTitle("Search Parameter Setting");
			pack();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((int)(d.getWidth() / 2 - getWidth() / 2),
					(int)(d.getHeight() / 2 - getHeight() / 2));
			setVisible(true);
			
			// ウィンドウリスナー追加
			addWindowListener(new WindowAdapter() {
				public void windowOpened(WindowEvent e) {
					isSubWindow = true;
				}
				public void windowClosing(WindowEvent e) {
					cancelButton.doClick();
				}
			});
		}
		
		/**
		 * Tolerance値変更確認
		 * Tolerance値が変更されていた場合はtrueを返却する。
		 * @return 変更フラグ
		 */
		private boolean isTolChange() {
			
			if (Float.parseFloat(tolField.getText()) != TOLERANCE) {
				return true;
			}
			else if (isTolUnit1 != tolUnit1.isSelected()
					|| isTolUnit2 != tolUnit2.isSelected()) {
				return true;
			}
			return false;
		}
		
		/**
		 * Tolerance値変更
		 * Tolerance値を変更した場合に確定もしくはキャンセルする。
		 * @param isChange 変更フラグ
		 */
		private void tolChange(boolean isChange) {
			
			if (isChange) {
				
				// Cookie情報用リスト
				ArrayList<String> valueList = new ArrayList<String>();
				
				TOLERANCE = Float.parseFloat(tolField.getText());
				valueList.add(String.valueOf(TOLERANCE));
				
				isTolUnit1 = tolUnit1.isSelected();
				isTolUnit2 = tolUnit2.isSelected();
				if (tolUnit2.isSelected()) {
					valueList.add(tolUnit2.getText());
				}
				else {
					valueList.add(tolUnit1.getText());
				}
				
				// Tolerance値をCookieに設定
				cm.setCookie(COOKIE_TOL, valueList);
			}
			else {
				tolUnit1.setSelected(isTolUnit1);
				tolUnit2.setSelected(isTolUnit2);
			}
		}
		
		/**
		 * Cutoff Threshold値変更確認
		 * Cutoff Threshold値が変更されていた場合はtrueを返却する。
		 * @return 変更フラグ
		 */
		private boolean isCutoffChange() {
			
			if (Integer.parseInt(cutoffField.getText()) != CUTOFF_THRESHOLD) {
				return true;
			}
			return false;
		}
		
		/**
		 * Cutoff Threshold値変更
		 * Cutoff Threshold値を変更した場合に確定もしくはキャンセルする。
		 * @param isChange 変更フラグ
		 */
		private void cutoffChange(boolean isChange) {
			
			if (isChange) {
				
				// Cookie情報用リスト
				ArrayList<String> valueList = new ArrayList<String>();
				
				CUTOFF_THRESHOLD = Integer.parseInt(cutoffField.getText());
				valueList.add(String.valueOf(CUTOFF_THRESHOLD));
				
				// Cutoff Threshold値をCookieに設定
				cm.setCookie(COOKIE_CUTOFF, valueList);
			}
		}
		
		/**
		 * 装置種別チェックボックス値チェック（All）
		 * 装置種別がAll選択状態かを返却する。
		 * @return All選択フラグ
		 */
		private boolean isInstAll() {
			
			if (instCheck.size() == 0) {
				return false;
			}
			for (Iterator j=instCheck.keySet().iterator(); j.hasNext(); ) {
				String key = (String)j.next();
				
				if ( !((JCheckBox)instCheck.get(key)).isSelected() ) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 装置種別チェックボックス値チェック（Default）
		 * 装置種別がDefault選択状態かを返却する。 
		 * @return Default選択フラグ
		 */
		private boolean isInstDefault() {
			
			if (instCheck.size() == 0) {
				return false;
			}
			for (Iterator j=instCheck.keySet().iterator(); j.hasNext(); ) {
				String key = (String)j.next();
				if ( isDefaultInst(key) ) {	// デフォルト装置種別の場合
					if ( !((JCheckBox)instCheck.get(key)).isSelected() ) {
						return false;
					}
				} else {
					if ( ((JCheckBox)instCheck.get(key)).isSelected() ) {
						return false;
					}
				}
			}
			return true;
		}
		
		/**
		 * 装置種別選択チェック
		 * 装置種別が1つでも選択されていた場合はtrueを返却する。
		 * @return 選択済みフラグ
		 */
		private boolean isInstCheck() {
			
			if (instCheck.size() == 0) {
				return false;
			}
			for (Iterator j=instCheck.keySet().iterator(); j.hasNext(); ) {
				String key = (String)j.next();
				if ( ((JCheckBox)instCheck.get(key)).isSelected() ) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * 装置種別選択値変更確認
		 * 装置種別チェックボックスの値が1つでも変更されていた場合はtrueを返却する。
		 * @return 変更フラグ
		 */
		private boolean isInstChange() {
			
			if (isInstCheck.size() == 0) {
				return false;
			}
			for (Iterator i=isInstCheck.keySet().iterator(); i.hasNext(); ) {
				String key = (String)i.next();
				boolean before = (boolean)isInstCheck.get(key);
				boolean after = ((JCheckBox)instCheck.get(key)).isSelected();
				if (before != after) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * 装置種別選択値変更
		 * チェックボックスを選択変更した場合に確定もしくはキャンセルする。
		 * @param isChange 変更フラグ
		 */
		private void instChange(boolean isChange) {
			
			// Cookie情報用リスト
			ArrayList<String> valueList = new ArrayList<String>();		
			
			if (isInstCheck.size() == 0) {
				return;
			}
			for (Iterator i=isInstCheck.keySet().iterator(); i.hasNext(); ) {
				String key = (String)i.next();
				boolean before = (boolean)isInstCheck.get(key);
				boolean after = ((JCheckBox)instCheck.get(key)).isSelected();
				if (before != after) {
					if (isChange) {
						isInstCheck.put(key, after);
					}
					else {
						((JCheckBox)instCheck.get(key)).setSelected(before);
					}
				}
				if ( ((JCheckBox)instCheck.get(key)).isSelected() ) {
					valueList.add(key);
				}
			}
			// 装置種別選択状態をCookieに設定
			if (isChange) {
				cm.setCookie(COOKIE_INST, valueList);
			}
		}
		
		/**
		 * MS種別チェックボックス値チェック（All）
		 * MS種別がAll選択状態かを返却する。
		 * @return All選択フラグ
		 */
		private boolean isMsAll() {
			
			if (msCheck.size() == 0) {
				return false;
			}
			for (Iterator j=msCheck.keySet().iterator(); j.hasNext(); ) {
				String key = (String)j.next();
				if ( !((JCheckBox)msCheck.get(key)).isSelected() ) {
					return false;
				}
			}
			return true;
		}
		
		/**
		 * MS種別選択チェック
		 * MS種別が1つでも選択されていた場合はtrueを返却する。
		 * @return 選択済みフラグ
		 */
		private boolean isMsCheck() {
			
			if (msCheck.size() == 0) {
				return false;
			}
			for (Iterator j=msCheck.keySet().iterator(); j.hasNext(); ) {
				String key = (String)j.next();
				if ( ((JCheckBox)msCheck.get(key)).isSelected() ) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * MS種別選択値変更確認
		 * MS種別チェックボックスの値が1つでも変更されていた場合はtrueを返却する。
		 * @return 変更フラグ
		 */
		private boolean isMsChange() {
			
			if (isMsCheck.size() == 0) {
				return false;
			}
			for (Iterator i=isMsCheck.keySet().iterator(); i.hasNext(); ) {
				String key = (String)i.next();
				boolean before = (boolean)isMsCheck.get(key);
				boolean after = ((JCheckBox)msCheck.get(key)).isSelected();
				if (before != after) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * MS種別選択値変更
		 * チェックボックスを選択変更した場合に確定もしくはキャンセルする。
		 * @param isChange 変更フラグ
		 */
		private void msChange(boolean isChange) {
			
			// Cookie情報用リスト
			ArrayList<String> valueList = new ArrayList<String>();		
			
			if (isMsCheck.size() == 0) {
				return;
			}
			for (Iterator i=isMsCheck.keySet().iterator(); i.hasNext(); ) {
				String key = (String)i.next();
				boolean before = (boolean)isMsCheck.get(key);
				boolean after = ((JCheckBox)msCheck.get(key)).isSelected();
				if (before != after) {
					if (isChange) {
						isMsCheck.put(key, after);
					}
					else {
						((JCheckBox)msCheck.get(key)).setSelected(before);
					}
				}
				if ( ((JCheckBox)msCheck.get(key)).isSelected() ) {
					valueList.add(key);
				}
			}
			// MS種別選択状態をCookieに設定
			if (isChange) {
				cm.setCookie(COOKIE_MS, valueList);
			}
		}
		
		/**
		 * イオン種別選択値変更確認
		 * イオン種別ラジオボタンの値が変更されていた場合はtrueを返却する。
		 * @return 変更フラグ
		 */
		private boolean isIonChange() {
			
			for (Iterator i=isIonRadio.keySet().iterator(); i.hasNext(); ) {
				String key = (String)i.next();
				
				boolean before = (boolean)isIonRadio.get(key);
				boolean after = ((JRadioButton)ionRadio.get(key)).isSelected();
				
				if (before != after) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * イオン種別選択値変更
		 * ラジオボタンを選択変更した場合に確定もしくはキャンセルする。
		 * @param isChange 変更フラグ
		 */
		private void ionChange(boolean isChange) {
			
			// Cookie情報用リスト
			ArrayList<String> valueList = new ArrayList<String>();		
			
			for (Iterator i=isIonRadio.keySet().iterator(); i.hasNext(); ) {
				String key = (String)i.next();
				
				boolean before = (boolean)isIonRadio.get(key);
				boolean after = ((JRadioButton)ionRadio.get(key)).isSelected();
				
				if (before != after) {
					if (isChange) {
						isIonRadio.put(key, after);
					}
					else {
						((JRadioButton)ionRadio.get(key)).setSelected(before);
					}
				}
				if ( ((JRadioButton)ionRadio.get(key)).isSelected() ) {
					valueList.add(key);
				}
			}
			// イオン種別選択状態をCookieに設定
			if (isChange) {
				cm.setCookie(COOKIE_ION, valueList);
			}
		}
		
		/**
		 * Precursor m/z値変更確認
		 * Precursor m/z値が変更されていた場合はtrueを返却する。
		 * @return 変更フラグ
		 */
		private boolean isPreChange() {
			
			if (preField.getText().equals("")) {
				if (PRECURSOR != -1) {
					return true;
				}
			}
			else if (Integer.parseInt(preField.getText()) != PRECURSOR) {
				return true;
			}
			return false;
		}
		
		/**
		 * Precursor m/z値変更
		 * Precursor m/z値を変更した場合に確定もしくはキャンセルする。
		 * @param isChange 変更フラグ
		 */
		private void preChange(boolean isChange) {
			
			if (isChange) {
				
//				// Cookie情報用リスト
//				ArrayList<String> valueList = new ArrayList<String>();
				
				if (preField.getText().equals("")) {
					PRECURSOR = -1;
				}
				else {
					PRECURSOR = Integer.parseInt(preField.getText());
				}
//				valueList.add(String.valueOf(PRECURSOR));
				
//				// Precursor m/z値をCookieに設定
//				cm.setCookie(COOKIE_PRE, valueList);
			}
		}
		
		/**
		 * メインコンテナーの初期化
		 * @param c コンテナー
		 */
		private void initMainContainer(Container c) {
			c.setLayout(new GridBagLayout());
		}

		/**
		 * メインコンテナー用レイアウト制約
		 * @param x 水平方向セル位置
		 * @param y 垂直方向セル位置
		 * @param w 水平方向の1行のセル数
		 * @param h 垂直方向の1行のセル数
		 * @return レイアウト制約
		 */
		private GridBagConstraints mainContainerGBC(int x, int y, int w, int h) {
			
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.weightx = 1.0d;
			gbc.weighty = 1.0d;
			gbc.insets = new Insets(15, 15, 0, 15);
			
			gbc.gridx = x;
			gbc.gridy = y;
			gbc.gridwidth = w;
			gbc.gridheight = h;
			
			return gbc;
		}
		
		/**
		 * 条件区切り用のパネルの初期化
		 * 
		 * @param p
		 * @param isBorder
		 * @return 条件区切り用パネル
		 */
		private void initDelimPanel(JPanel p, boolean isBorder) {
			
			p.setLayout(new GridBagLayout());
			
			if (isBorder) {
				Border border = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
						new EmptyBorder(3, 3, 3, 3));
				p.setBorder(border);
			}
		}
		
		/**
		 * 条件区切り用のパネルのレイアウト制約
		 * 
		 * @parma wx 水平方向拡大倍率
		 * @parma wy 垂直方向拡大倍率
		 * @param x 水平方向セル位置
		 * @param y 垂直方向セル位置
		 * @param w 水平方向の1行のセル数
		 * @param h 垂直方向の1行のセル数
		 * @return レイアウト制約
		 */
		private GridBagConstraints delimPanelGBC(double wx, double wy, int x, int y, int w, int h) {
			
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.weightx = wx;
			gbc.weighty = wy;
			gbc.insets = new Insets(2, 2, 2, 2);
			
			gbc.gridx = x;
			gbc.gridy = y;
			gbc.gridwidth = w;
			gbc.gridheight = h;
			
			return gbc;
		}
		
		/**
		 * ラベルパネル生成（レイアウト調整用）
		 * 
		 * JLabelを直接コンポーネントにaddすると位置の微調整ができないため、
		 * 微調整を行いJPanelでラッピングして返却する。
		 * また、ラベルサイズの統一のために使用することを推奨する。
		 * 
		 * @param label ラベル文字列（HTMLタグ内に表示する文字列）
		 * @param tooltip ツールチップ文字列（HTMLタグ内に表示する文字列）
		 * @param size ラベルサイズ
		 * @param labelIndent ラベル左インデント幅（半角スペースの数）
		 * @return ラベルをラッピングしたパネル
		 */
		private JPanel newLabelPanel(String label, String tooltip, int size, int labelIndent) {
			// ラベル生成
			for (int i=0; i<labelIndent; i++) {
				label = " " + label;
			}
			JLabel l = new JLabel(label);
			
			switch (size) {
				case LABEL_SIZE_L:
					l.setPreferredSize(new Dimension(110, 20));
					l.setMinimumSize(new Dimension(110, 20));				
					break;
				case LABEL_SIZE_M:
					l.setPreferredSize(new Dimension(85, 20));
					l.setMinimumSize(new Dimension(85, 20));				
					break;
				case LABEL_SIZE_S:
					l.setPreferredSize(new Dimension(45, 20));
					l.setMinimumSize(new Dimension(45, 20));				
					break;				
				default:
					break;
			}
			
			if (tooltip != null) {
				l.setToolTipText(tooltip);
			}
			
			
			// パネルセット
			JPanel p = new JPanel();
			p.setLayout(new GridBagLayout());
			
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 0.0d;
			gbc.weighty = 0.0d;
			gbc.insets = new Insets(2, 2, 2, 2);
			
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			
			p.add(l, gbc);
			
			return p;
		}
		
		/**
		 * テキストフィールドラッピング（レイアウト調整用）
		 * 
		 * JTextFieldをaddする場合に位置の微調整を行いたい場合に使用する。
		 * 
		 * @return テキストフィールドをラッピングしたパネル
		 */
		private JPanel wrappTextPanel(JTextField t) {
			
			// パネルセット
			JPanel p = new JPanel();
			p.setLayout(new GridBagLayout());
			
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 0.0d;
			gbc.weighty = 0.0d;
			gbc.insets = new Insets(2, 2, 2, 2);
			
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			
			p.add(t, gbc);
			
			return p;
		}
		
		/**
		 * アイテムパネルの初期化
		 * @return アイテムパネル
		 */
		private void initItemPanel(JPanel p) {
			p.setLayout(new GridBagLayout());
		}
		
		/**
		 * アイテムパネルのレイアウト制約
		 * @parma wx 水平方向拡大倍率
		 * @parma wy 垂直方向拡大倍率
		 * @param x 水平方向セル位置
		 * @param y 垂直方向セル位置
		 * @param w 水平方向の1行のセル数
		 * @param h 垂直方向の1行のセル数
		 * @return レイアウト制約
		 */
		private GridBagConstraints itemPanelGBC(double wx, double wy, int x, int y, int w, int h) {
			
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.weightx = wx;
			gbc.weighty = wy;
			gbc.insets = new Insets(2, 2, 2, 2);
			
			gbc.gridx = x;
			gbc.gridy = y;
			gbc.gridwidth = w;
			gbc.gridheight = h;
			
			return gbc;
		}	
	}
	
	/**
	 * Fileタブのテーブルリストモデルリスナークラス
	 * SerarchPageのインナークラス。
	 */
	class LmFileListener implements ListSelectionListener {
		
		/**
		 * バリューチェンジイベント
		 * @see javax.swing.event.ListSelectionListener#valueChanged(java.swing.event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent le) {
			
			if (le.getValueIsAdjusting()) {
				return;
			}
			
			final int selRow = queryFileTable.getSelectedRow();
			if (selRow < 0) {
				// CompareView初期化
				queryPlot.clear();
				compPlot.clear();
				resultPlot.clear();
				queryPlot.setPeaks(null, 0);
				compPlot.setPeaks(null, 1);
				resultPlot.setPeaks(null, 0);

				// PackageView初期化
				pkgView.initAllRecInfo();
				
				DefaultTableModel dm = (DefaultTableModel)resultSorter.getTableModel();
				dm.setRowCount(0);
				hitLabel.setText(" ");
				return;
			}
			
			// マウスカーソルを砂時計に
			SearchPage.this.setCursor(waitCursor);

			// PackageView表示ファイルデータ設定
			int noCol = queryFileTable.getColumnModel().getColumnIndex(COL_LABEL_NO);
			int nameCol = queryFileTable.getColumnModel().getColumnIndex(COL_LABEL_NAME);
			int idCol = queryFileTable.getColumnModel().getColumnIndex(COL_LABEL_ID);
			
			// ピーク情報
			String[] peaks = userDataList[selRow].getPeaks();
				
			PackageRecData recData = new PackageRecData();
			
			// == クエリーレコードフラグ ===
			recData.setQueryRecord(true);
			
			// === 統合レコードフラグ ===
			recData.setIntegRecord(false);
			
			// === ID ===
			recData.setId((String)queryFileTable.getValueAt(selRow, idCol));
			
			// === スコア ===
			recData.setScore(" -");
			
			// === サイト ===
			recData.setSite("");
			
			// === 化合物名 ===
			recData.setName((String)queryFileTable.getValueAt(selRow, nameCol));
			
			// === プリカーサー ===
			recData.setPrecursor("");
			
			// === ピーク数 ===
			int num = peaks.length;
			if (num == 1) {
				if (peaks[0].split("\t")[0].equals("0") && peaks[0].split("\t")[1].equals("0")) {
					num = 0;
				}
			}
			recData.setPeakNum( num );
			
			for (int i=0; i < recData.getPeakNum(); i++ ) {
				// === m/z ===
				recData.setMz( i, peaks[i].split("\t")[0] );
				
				// === 強度 ===
				recData.setIntensity(i, peaks[i].split("\t")[1] );
			}
			
			// === ピーク色 ===
			recData.setPeakColorType(PackageRecData.COLOR_TYPE_BLACK);
			
			// レコード情報追加
			pkgView.addQueryRecInfo(recData);
			pkgView.addRecInfoAfter(true, recData.getId(), PackageSpecData.SORT_KEY_NONE);
			
			
			// DB検索
			String name = (String)queryFileTable.getValueAt(selRow, nameCol);
			String key = String.valueOf(queryFileTable.getValueAt(selRow, noCol));
			searchDb(userDataList[selRow].getPeaks(), "", name, key);
		}
	}
	
	/**
	 * Resultタブのテーブルリストモデルリスナークラス
	 * SerarchPageのインナークラス。
	 */
	class LmResultListener implements ListSelectionListener {
		
		/**
		 * バリューチェンジイベント
		 * @see javax.swing.event.ListSelectionListener#valueChanged(java.swing.event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent le) {
			
			if (le.getValueIsAdjusting()) {
				return;
			}
			
			int[] selRows = resultTable.getSelectedRows();
			if (selRows.length < 1) {
				// CompareView初期化
				resultPlot.clear();
				compPlot.setPeaks(null, 1);
				resultPlot.setPeaks(null, 0);
				setAllPlotAreaRange();
				
				// PackageView初期化
				pkgView.initResultRecInfo();
				return;
			}
			
			// マウスカーソルを砂時計に
			SearchPage.this.setCursor(waitCursor);
			
			int idCol = resultTable.getColumnModel().getColumnIndex(COL_LABEL_ID);
			int nameCol = resultTable.getColumnModel().getColumnIndex(COL_LABEL_NAME);
			int siteNameCol = resultTable.getColumnModel().getColumnIndex(COL_LABEL_CONTRIBUTOR);
			int scoreCol = resultTable.getColumnModel().getColumnIndex(COL_LABEL_SCORE);
			
			String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GSDATA];
			String id;
			String name;
			String relation;
			int ion;
			String score;
			String siteName;
			String site = "0";
			PackageRecData recData = null;

			if (isIonRadio.get("Posi")) {
				ion = 1;
			} else if (isIonRadio.get("Nega")) {
				ion = -1;
			} else {
				ion = 0;
			}
			
			if (isDispSelected) {
				
				// Compare View用データクラス初期化
				if (selRows.length > 1) {
					// 2件以上選択時は表示できない
					resultPlot.clear();
					compPlot.setPeaks(null, 1);
					resultPlot.setPeaks(null, 0);
					setAllPlotAreaRange();
				}
				if (selRows.length > MAX_DISPLAY_NUM) {
					JOptionPane.showMessageDialog(
							null,
							"Cannot display more than " + MAX_DISPLAY_NUM + " spectra in Package View.",
							"Warning",
							JOptionPane.WARNING_MESSAGE);
					SearchPage.this.setCursor(Cursor.getDefaultCursor());
					return;
				}
				
				// PackageView表示データ設定
				boolean recChangeFlag = true;
				PeakData peak = null;
				for (int i=0; i<selRows.length; i++) {
					
					id = (String)resultTable.getValueAt(selRows[i], idCol);
					name = (String)resultTable.getValueAt(selRows[i], nameCol);
					relation = "false";
					score = String.valueOf(resultTable.getValueAt(selRows[i], scoreCol));
					siteName = (String)resultTable.getValueAt(selRows[i], siteNameCol);
					for (int j=0; j<siteNameList.length; j++) {
						if (siteName.equals(siteNameList[j])) {
							site = Integer.toString(j);
							break;
						}
					}
					
					String reqUrl = baseUrl + "jsp/"
							+ MassBankCommon.DISPATCHER_NAME + "?type="
							+ typeName + "&id=" + id + "&site=" + site + "&relation=" + relation + "&ion=" + ion;
					
					String line = "";
					String findStr;
					try {
						URL url = new URL( reqUrl );
						URLConnection con = url.openConnection();
						
						// レスポンス取得
						BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
						
						// レスポンス格納
						String result;
						while ( (result = in.readLine()) != null ) {
							if ( !result.equals("") ) {		// スペース行を読み飛ばす
								line = result;
								break;
							}
						}
						in.close();
					}
					catch (IOException iex) {
						iex.printStackTrace();
						SearchPage.this.setCursor(Cursor.getDefaultCursor());
					}
					
						
					recData = new PackageRecData();
					
					// === 化合物名 ===
					findStr = "name=";
					recData.setName(line, findStr);
					
					// === プリカーサー ===
					findStr = "precursor=";
					recData.setPrecursor(line, findStr);
					
					// === ID ===
					findStr = "id=";
					recData.setId(line, findStr);
					
					// === リザルトレコードフラグ ===
					recData.setResultRecord(true);
					
					// === 統合レコードフラグ ===
					recData.setIntegRecord(recData.getName());
					
					// === スコア ===
					recData.setScore(score);
					
					// === サイト ===
					recData.setSite(site);
					
					// レコード情報以降の文字列を削除し、ピーク情報(m/z、強度)のみを残し
					// ピーク情報のみを切り出す。
					if (line.indexOf("::") > 0) {
						line = line.substring(0, line.indexOf("::"));
					}
					String[] tmpPeak = line.split("\t\t");
					
					// === ピーク数 ===
					int num = tmpPeak.length;
					if (num == 1) {
						if (tmpPeak[0].split("\t")[0].equals("0") && tmpPeak[0].split("\t")[1].equals("0")) {
							num = 0;
						}
					}
					recData.setPeakNum( num );
					
					for (int j = 0; j < recData.getPeakNum(); j++ ) {
						// === m/z ===
						recData.setMz( j, tmpPeak[j].split("\t")[0] );
						
						// === 強度 ===
						recData.setIntensity(j, tmpPeak[j].split("\t")[1] );
					}
					
					// === ピーク色 ===
					recData.setPeakColorType(PackageRecData.COLOR_TYPE_BLACK);
					
					// レコード情報追加
					pkgView.addResultRecInfo(recData, recChangeFlag);
					recChangeFlag = false;
					
					// Compare View用データクラス生成
					if (selRows.length == 1) {
						peak = new PeakData(tmpPeak);
						resultPlot.clear();
						resultPlot.setPeaks(peak, 0);
						resultPlot.setSpectrumInfo(name, id, recData.getPrecursor(), PeakPanel.SP_TYPE_RESULT, false);
						compPlot.setPeaks(peak, 1);
						setAllPlotAreaRange();
						compPlot.setTolerance(String.valueOf(TOLERANCE), tolUnit1.isSelected());
					}
				}
				
				pkgView.setTolerance(String.valueOf(TOLERANCE), tolUnit1.isSelected());
				pkgView.addRecInfoAfter(
						false,
						(String)resultTable.getValueAt(resultTable.getSelectionModel().getLeadSelectionIndex(),
						idCol),
						PackageSpecData.SORT_KEY_SCORE);
			}
			else if (isDispRelated) {
				id = (String)resultTable.getValueAt(selRows[0], idCol);
				name = (String)resultTable.getValueAt(selRows[0], nameCol);
				relation = "true";
				siteName = (String)resultTable.getValueAt(selRows[0], siteNameCol);
				for (int i = 0; i < siteNameList.length; i++) {
					if (siteName.equals(siteNameList[i])) {
						site = Integer.toString(i);
						break;
					}
				}
				String reqUrl = baseUrl + "jsp/"
						+ MassBankCommon.DISPATCHER_NAME + "?type="
						+ typeName + "&id=" + id + "&site=" + site + "&relation=" + relation + "&ion=" + ion;
				String precursor = "";
				PeakData peak = null;
				try {
					URL url = new URL(reqUrl);
					URLConnection con = url.openConnection();
					String line = "";
					String findStr;
					boolean recChangeFlag = true;
					BufferedReader in = new BufferedReader(
							new InputStreamReader(con.getInputStream()));
					
					// PackageView表示データ設定
					while ((line = in.readLine()) != null) {
						if (line.equals("")) {		// スペース行を読み飛ばす
							continue;
						}
						
						recData = new PackageRecData();
						
						// === 化合物名 ===
						findStr = "name=";
						recData.setName(line, findStr);
						
						// === プリカーサー ===
						findStr = "precursor=";
						recData.setPrecursor(line, findStr);
						
						// === ID ===
						findStr = "id=";
						recData.setId(line, findStr);
						
						// === 統合レコードフラグ ===
						recData.setIntegRecord(recData.getName());
						
						// === スコア ===
						recData.setScore("");
						
						// === サイト ===
						recData.setSite(site);
						
						// レコード情報以降の文字列を削除し、ピーク情報(m/z、強度)のみを残し
						// ピーク情報のみを切り出す。
						if (line.indexOf("::") > 0) {
							line = line.substring(0, line.indexOf("::"));
						}
						String[] tmpPeak = line.split("\t\t");
						
						// === ピーク数 ===
						int num = tmpPeak.length;
						if (num == 1) {
							if (tmpPeak[0].split("\t")[0].equals("0") && tmpPeak[0].split("\t")[1].equals("0")) {
								num = 0;
							}
						}
						recData.setPeakNum( num );
						
						for (int j = 0; j < recData.getPeakNum(); j++ ) {
							// === m/z ===
							recData.setMz( j, tmpPeak[j].split("\t")[0] );
							
							// === 強度 ===
							recData.setIntensity(j, tmpPeak[j].split("\t")[1] );
						}
						
						// === ピーク色 ===
						recData.setPeakColorType(PackageRecData.COLOR_TYPE_BLACK);
						
						// レコード情報追加
						pkgView.addResultRecInfo(recData, recChangeFlag);
						recChangeFlag = false;
						
						// Compare View用データクラス生成
						if (id.equals(recData.getId())) {
							peak = new PeakData(tmpPeak);
							precursor = recData.getPrecursor();
						}
					}
					in.close();
				}
				catch (Exception ex) {
					ex.printStackTrace();
					SearchPage.this.setCursor(Cursor.getDefaultCursor());
				}
				
				pkgView.setTolerance(String.valueOf(TOLERANCE), tolUnit1.isSelected());
				pkgView.addRecInfoAfter(false, id, PackageSpecData.SORT_KEY_NAME);
				
				resultPlot.clear();
				resultPlot.setPeaks(peak, 0);
				resultPlot.setSpectrumInfo(name, id, precursor, PeakPanel.SP_TYPE_RESULT, false);
				compPlot.setPeaks(peak, 1);

				setAllPlotAreaRange();
				compPlot.setTolerance(String.valueOf(TOLERANCE), tolUnit1.isSelected());
			}

			// 構造式画像のファイル名を取得する
			id = recData.getId();
			site = recData.getSite();
			String temp = recData.getName();
			String[] items = temp.split(";");
			name = URLEncoder.encode(items[0]);
			String getUrl = baseUrl + "jsp/GetCompoudInfo.jsp?name=" + name + "&site=" + site + "&id=" + id;
			String gifMFileName = "";
			String gifSFileName = "";
			String formula = "";
			String emass = "";
			try {
				URL url = new URL(getUrl);
				URLConnection con = url.openConnection();
				BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
				String line = "";
				while ( (line = in.readLine()) != null ) {
					if ( line.indexOf("GIF:") >= 0 ) {
						gifMFileName = line.replace("GIF:", "");
					}
					else if ( line.indexOf("GIF_SMALL:") >= 0 ) {
						gifSFileName = line.replace("GIF_SMALL:", "");
					}
					else if ( line.indexOf("FORMULA:") >= 0 ) {
						formula = line.replace("FORMULA:", "");
					}
					else if ( line.indexOf("EXACT_MASS:") >= 0 ) {
						emass = line.replace("EXACT_MASS:", "");
					}
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			resultPlot.loadStructGif(gifMFileName, gifSFileName);
			resultPlot.setCompoundInfo(formula, emass);

			SearchPage.this.setCursor(Cursor.getDefaultCursor());
		}
	}
	
	/**
	 * クエリDBタブのテーブルリストモデルリスナークラス
	 * SerarchPageのインナークラス。
	 */
	class LmQueryDbListener implements ListSelectionListener {
		
		/**
		 * バリューチェンジイベント
		 * @see javax.swing.event.ListSelectionListener#valueChanged(java.swing.event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent le) {
			
			if (le.getValueIsAdjusting()) {
				return;
			}
			
			final int selRow = queryDbTable.getSelectedRow();
			if (selRow < 0) {
				// CompareView初期化
				queryPlot.clear();
				compPlot.clear();
				resultPlot.clear();
				queryPlot.setPeaks(null, 0);
				compPlot.setPeaks(null, 1);
				resultPlot.setPeaks(null, 0);

				// PackageView初期化
				pkgView.initAllRecInfo();
				
				DefaultTableModel dm = (DefaultTableModel)resultSorter.getTableModel();
				dm.setRowCount(0);
				hitLabel.setText(" ");
				return;
			}
			
			// マウスカーソルを砂時計に
			SearchPage.this.setCursor(waitCursor);
			
			int idCol = queryDbTable.getColumnModel().getColumnIndex(COL_LABEL_ID);
			int nameCol = queryDbTable.getColumnModel().getColumnIndex(COL_LABEL_NAME);
			
			// nameListからのレコード情報取得用インデックス特定
			int nameListIndex = -1;
			if (!querySorter.isSorting()) {
				// ソート無しの場合
				nameListIndex = selRow;
			} else {
				// ソート有りの場合
				String tmpId = (String) queryDbTable.getValueAt(selRow, idCol);
				for (int i = 0; i < nameList.size(); i++) {
					if (nameList.get(i)[0].equals(tmpId)) {
						nameListIndex = i;
						break;
					}
				}
			}
			String idName[] = (String[]) nameList.get(nameListIndex);
			String id = idName[0];
			String site = idName[2];
			
			
			// PackageView表示クエリーデータ設定
			PackageRecData recData = new PackageRecData();
			
			// == クエリーレコードフラグ ===
			recData.setQueryRecord(true);
			
			// === ID ===
			recData.setId(id);
			
			// === スコア ===
			recData.setScore(" -");
			
			// === サイト ===
			recData.setSite(site);
			
			String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GSDATA];
			String reqUrl = baseUrl + "jsp/" + MassBankCommon.DISPATCHER_NAME
					+ "?type=" + typeName + "&id=" + id + "&site=" + site + "&relation=false";
			
			String line = "";
			String findStr;
			try {
				URL url = new URL( reqUrl );
				URLConnection con = url.openConnection();
				
				// レスポンス取得
				BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
				
				// レスポンス格納
				String result;
				while ( (result = in.readLine()) != null ) {
					// スペース行を読み飛ばす
					if ( !result.equals("") ) {
						line = result;
						break;
					}
				}
				in.close();
			}
			catch (IOException iex) {
				iex.printStackTrace();
				SearchPage.this.setCursor(Cursor.getDefaultCursor());
			}
			
			// === 化合物名 ===
			findStr = "name=";
			recData.setName(line, findStr);
			
			// === 統合レコードフラグ ===
			recData.setIntegRecord(recData.getName());
			
			// === プリカーサー ===
			findStr = "precursor=";
			recData.setPrecursor(line, findStr);
			
			// レコード情報以降の文字列を削除し、ピーク情報(m/z、強度)のみを残し
			// ピーク情報のみを切り出す。
			if (line.indexOf("::") > 0) {
				line = line.substring(0, line.indexOf("::"));
			}
			String[] tmpPeak = line.split("\t\t");
			
			// === ピーク数 ===
			int num = tmpPeak.length;
			if (num == 1) {
				if (tmpPeak[0].split("\t")[0].equals("0") && tmpPeak[0].split("\t")[1].equals("0")) {
					num = 0;
				}
			}
			recData.setPeakNum( num );
			
			for (int i = 0; i < recData.getPeakNum(); i++ ) {
				// === m/z ===
				recData.setMz( i, tmpPeak[i].split("\t")[0] );
				
				// === 強度 ===
				recData.setIntensity(i, tmpPeak[i].split("\t")[1] );
			}
			
			// === ピーク色 ===
			recData.setPeakColorType(PackageRecData.COLOR_TYPE_BLACK);
			
			// レコード情報追加
			pkgView.addQueryRecInfo(recData);
			pkgView.addRecInfoAfter(true, id, PackageSpecData.SORT_KEY_NONE);
			
			
			// DB検索
			String name = (String)queryDbTable.getValueAt(selRow, nameCol);
			String key = (String)queryDbTable.getValueAt(selRow, idCol);
			searchDb(tmpPeak, recData.getPrecursor(), name, key);
		}
	}

	/**
	 * テーブルマウスリスナークラス
	 * SerarchPageのインナークラス。
	 */
	class TblMouseListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			super.mouseClicked(e);
			
			// 左クリックの場合
			if (SwingUtilities.isLeftMouseButton(e)) {

				JTable tbl = (JTable)e.getSource();
				
				if (e.getClickCount() == 2 && !tbl.equals(queryFileTable)) {
					showRecordPage(tbl);
				}
				else if (e.getClickCount() == 1) {
					
					if (e.isShiftDown() || e.isControlDown()) {
						return;
					}
					
					int selRow[] = tbl.getSelectedRows();
					int idCol = tbl.getColumnModel().getColumnIndex(COL_LABEL_ID);
					
					// Package View テーブル再選択処理
					String id = (String)tbl.getValueAt(selRow[0], idCol);
					if (tbl.equals(resultTable)) {
						pkgView.setTblSelection(false, id);
					}
					else {
						pkgView.setTblSelection(true, id);
					}
				}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			
			// 右リリースの場合
			if (SwingUtilities.isRightMouseButton(e)) {
				
				recListPopup(e);
			}
		}
	}
	
	/**
	 * ペインマウスリスナー
	 * SearchPageのインナークラス
	 */
	class PaneMouseListener extends MouseAdapter {
		
		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			
			// 右リリースの場合
			if (SwingUtilities.isRightMouseButton(e)) {
				recListPopup(e);
			}
		}
	}
	
	/**
	 * Search Nameボタンリスナークラス
	 * SerarchPageのインナークラス。
	 */
	class BtnSearchNameListener implements ActionListener {
		
		/**
		 * アクションイベント
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			String inputStr = (String)JOptionPane.showInputDialog(null,
					"Please input the Name.", "Search Name",
					JOptionPane.PLAIN_MESSAGE, null, null, saveSearchName);

			// キャンセルボタン、×ボタン、Escキー押下時
			if (inputStr == null) {
				return;
			}

			String searchName = inputStr.trim();
			// ボタンの色を変更
			JButton btn = btnName;
			Color defColor = btn.getBackground();
			btn.setBackground(Color.PINK);
			btn.update(btn.getGraphics());

			// マウスカーソルを砂時計に
			SearchPage.this.setCursor(waitCursor);
			
			// プロットペイン初期化
			queryPlot.clear();
			compPlot.clear();
			resultPlot.clear();
			queryPlot.setPeaks(null, 0);
			compPlot.setPeaks(null, 1);
			resultPlot.setPeaks(null, 0);

			// PackageView初期化
			pkgView.initAllRecInfo();
			
			// DB Hitタブ関連初期化
			if (resultTabPane.getTabCount() > 0) {
				resultTabPane.setSelectedIndex(0);
			}
			DefaultTableModel dm1 = (DefaultTableModel) resultSorter.getTableModel();
			dm1.setRowCount(0);
			hitLabel.setText(" ");

			if (searchName.equals("")) {
				// DBタブ関連初期化
				DefaultTableModel dataModel = (DefaultTableModel) querySorter.getTableModel();
				dataModel.setRowCount(0);
				SearchPage.this.setCursor(Cursor.getDefaultCursor());
				btn.setBackground(defColor);
				return;
			}

			saveSearchName = searchName;

			// スペクトル取得
			getSpectrumForQuery(searchName);

			// マウスカーソルをデフォルトカーソルに
			SearchPage.this.setCursor(Cursor.getDefaultCursor());

			// ボタンの色を戻す
			btn.setBackground(defColor);
		}
	}

	/**
	 * Allボタンリスナークラス
	 * SerarchPageのインナークラス。
	 */
	class BtnAllListener implements ActionListener {
		
		/**
		 * アクションイベント
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			// ボタンの色を変更
			JButton btn = btnAll;
			Color defColor = btn.getBackground();
			btn.setBackground(Color.PINK);
			btn.update(btn.getGraphics());

			// マウスカーソルを砂時計に
			SearchPage.this.setCursor(waitCursor);

			// 未検索の場合
			if (nameListAll.size() == 0) {
				// スペクトル取得
				getSpectrumForQuery("");
				nameListAll = new ArrayList(nameList);
			}
			// 既検索の場合
			else {
				// プロットペイン初期化
				queryPlot.clear();
				compPlot.clear();
				resultPlot.clear();
				queryPlot.setPeaks(null, 0);
				compPlot.setPeaks(null, 1);
				resultPlot.setPeaks(null, 0);

				// PackageView初期化
				pkgView.initAllRecInfo();
				
				DefaultTableModel dm = (DefaultTableModel)resultSorter.getTableModel();
				dm.setRowCount(0);
				hitLabel.setText(" ");
				nameList = new ArrayList(nameListAll);
				try {
					DefaultTableModel dataModel = (DefaultTableModel) querySorter.getTableModel();
					queryDbTable.clearSelection();
					dataModel.setRowCount(0);
					for (int i = 0; i < nameListAll.size(); i++) {
						String[] item = (String[]) nameListAll.get(i);
						String id = item[0];
						String name = item[1];
						String site = siteNameList[Integer.parseInt(item[2])];
						String[] idNameSite = new String[] { id, name, site, String.valueOf(i + 1) };
						dataModel.addRow(idNameSite);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			// マウスカーソルをデフォルトカーソルに
			SearchPage.this.setCursor(Cursor.getDefaultCursor());

			// ボタンの色を戻す
			btn.setBackground(defColor);
		}
	}

	/**
	 * ポップアップメニューShow Recordリスナークラス
	 * SerarchPageのインナークラス。
	 */
	class PopupShowRecordListener implements ActionListener {
		
		private JTable eventTbl;	// イベント発生テーブル
		
		/**
		 * コンストラクタ
		 * @param eventTbl イベントが発生したテーブル
		 */
		public PopupShowRecordListener(JTable eventTbl) {
			this.eventTbl = eventTbl;
		}
		
		/**
		 * アクションイベント
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			showRecordPage(eventTbl);
		}
	}

	/**
	 * ポップアップメニューMultiple Displayリスナークラス
	 * SerarchPageのインナークラス。
	 */
	class PopupMultipleDisplayListener implements ActionListener {
		
		private JTable eventTbl;	// イベント発生テーブル
		
		/**
		 * コンストラクタ
		 * @param eventTbl イベントが発生したテーブル
		 */
		public PopupMultipleDisplayListener(JTable eventTbl) {
			this.eventTbl = eventTbl;
		}
		
		/**
		 * アクションイベント
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			// 選択された行のインデックスを取得
			int selRows[] = eventTbl.getSelectedRows();

			// CGI呼び出し
			try {
				String reqUrl = baseUrl + "jsp/Display.jsp";
				String param = "";

				int idCol = eventTbl.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_ID);
				int nameCol = eventTbl.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_NAME);
				int ionCol = eventTbl.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_ION);
				int siteCol = eventTbl.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_CONTRIBUTOR);
				for (int i = 0; i < selRows.length; i++) {
					int row = selRows[i];
					String name = (String)eventTbl.getValueAt(row, nameCol);
					String id = (String)eventTbl.getValueAt(row, idCol);
					String formula = "";
					String mass = "";
					String ion = (String)eventTbl.getValueAt(row, ionCol);
					name = URLEncoder.encode(name);
					String siteName = (String)eventTbl.getValueAt(row, siteCol);
					String site = "0";
					for (int j = 0; j < siteNameList.length; j++) {
						if (siteName.equals(siteNameList[j])) {
							site = Integer.toString(j);
							break;
						}
					}
					param += "id=" + name + "\t" + id + "\t" + formula + "\t" + mass + "\t"	+ ion + "\t" + site + "&";
				}
				param = param.substring(0, param.length() - 1);

				URL url = new URL(reqUrl);
				URLConnection con = url.openConnection();
				con.setDoOutput(true);
				PrintStream out = new PrintStream(con.getOutputStream());
				out.print(param);
				out.close();
				String line;
				String filename = "";
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				while ((line = in.readLine()) != null) {
					filename += line;
				}
				in.close();

				reqUrl += "?type=Multiple Display&" + "name=" + filename;
				context.showDocument(new URL(reqUrl), "_blank");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * ピークコンパレータ
	 * SearchPageのインナークラス。
	 * m/zの昇順ソートを行う。
	 */
	class PeakComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			String mz1 = String.valueOf(o1).split("\t")[0];
			String mz2 = String.valueOf(o2).split("\t")[0];
			return Double.valueOf(mz1).compareTo(Double.valueOf(mz2));
		}
	}
}
