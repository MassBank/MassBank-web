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
 * スペクトル一括表示 クラス
 *
 * ver 1.0.7 2011.08.10
 *
 ******************************************************************************/

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import massbank.MassBankCommon;

/**
 * スペクトル一括表示 クラス
 */
@SuppressWarnings("serial")
public class PackageViewPanel extends JPanel {
	
	private final PackageSpecData specData;				// スペクトル情報データクラス(blank final変数)
	
	private final int MARGIN = 15;						// 余白
	private final int INTENSITY_RANGE_MAX = 1000;			// 最大強度
	private final int MASS_RANGE_MIN = 5;					// 最小マスレンジ
	
	private int width = 0;									// 画面幅
	private int height = 0;								// 画面高
	
	private int maxMoveXPoint = 0;							// x軸最大可動値
	private int maxMoveYPoint = 0;							// y軸最大可動値
	private int minMoveXPoint = 0;							// x軸最小可動値
	private int minMoveYPoint = 0;							// y軸最小可動値	
	private int moveXPoint = 0;							// x軸可動値
	private int moveYPoint = 0;							// y軸可動値
	private int tmpMoveXPoint = -1;						// 現在のx軸可動値(退避用)
	private int tmpMoveYPoint = -1;						// 現在のy軸可動値(退避用)

	private double massStart = 0;
	private double massRange = 0;
	private int massRangeMax = 0;
	private int intensityRange = 0;
	
	private Point cursorPoint = null;						// マウスカーソルポイント
	
	private boolean underDrag = false;					// ドラッグ中フラグ
	private Point fromPos = null;							// ドラッグ開始ポイント
	private Point toPos = null;							// ドラッグ終了ポイント
	private boolean isInitRate = false;					// 初期倍率フラグ(true:未拡大、false:拡大中)
	
	private Timer animationTimer = null;					// 拡大処理用タイマーオブジェクト
	private Timer moveTimer = null;						// グラフ可動用タイマーオブジェクト
	
	private GridBagConstraints gbc = null;					// レイアウト制約オブジェクト
	
	private JButton leftMostBtn = null;					// スペクトル移動ボタン(最左)
	private JButton leftBtn = null;						// スペクトル移動ボタン(左)
	private JButton rightBtn = null;						// スペクトル移動ボタン(右)
	private JButton rightMostBtn = null;					// スペクトル移動ボタン(最右)
	
	private JButton xAxisDown = null;						// x軸グラフ可動ボタン(Down)
	private JButton xAxisUp = null;						// x軸グラフ可動ボタン(Up)
	private JButton yAxisUp = null;						// y軸グラフ可動ボタン(Up)
	private JButton yAxisDown = null;						// y軸グラフ可動ボタン(Down)
	private JToggleButton mzDisp = null;					// show all m/zボタン
	private JToggleButton mzMatchDisp = null;				// show match m/zボタン
	private JToggleButton chgColor = null;					// change colorボタン
	private JToggleButton flat = null;						// flatボタン
	private JButton topAngleBtn = null;					// オートアングルボタン(top)
	private JButton sideAngleBtn = null;					// オートアングルボタン(side)

	private JLabel statusKeyLbl = null;						// ステータスラベル(キー)
	private JLabel statusValLbl = null;						// ステータスラベル(値)
	
	private final Color[] colorTable = new Color[]{
			new Color(51, 102, 153),	// #336699
			new Color(0, 102, 102),		// #006666
			new Color(0, 102, 0), 		// #006600
			new Color(0, 153, 0), 		// #009900
			new Color(102, 153, 0),		// #669900
			new Color(153, 153, 51), 	// #999933			
			new Color(204, 153, 0),		// #CC9900
			new Color(255, 153, 51),	// #FF9933
			new Color(255, 102, 0),		// #FF6600
			new Color(255, 102, 102),	// #FF6666
			new Color(204, 51, 102),	// #CC3366
			new Color(204, 51, 51),		// #CC3333
			new Color(153, 0, 0),		// #990000
			new Color(102, 0, 0),		// #660000
			new Color(51, 0, 51),		// #330033
			new Color(51, 0, 102),		// #330066
			new Color(51, 0, 153),		// #330099
			new Color(51, 51, 153),		// #333399
			new Color(0, 51, 153),		// #003399
			new Color(0, 102, 153)		// #006699
			};												// Colorテーブル(WebセーフカラーOnly)
	
	private int recNum = 0;								// レコード数
	private TableSorter recSorter = null;					// レコードリストテーブルモデル
	private JTable recTable = null;						// レコードリストテーブル
	private boolean initDispFlag = false;					// 初期表示フラグ
	
	public static final String QUERY_RECORD = "Query";
	public static final String RESULT_RECORD = "Result";
	public static final String INTEGRATE_RECORD = " / MERGED SPECTRUM";
	
	public static final String TABLE_RECORD_LIST = "RecordList";
	
	private float tolVal = 0.3f;			// Tolerance入力値
	private boolean tolUnit = true;		// Tolerance単位選択値（true：unit、false：ppm）
	private int pressRowIndex = -1;		// プレス時の行インデックス
	private int releaseRowIndex = -1;		// リリース時の行インデックス
	private int dragRowIndex = -1;			// ドラッグ時の行インデックス
	private boolean isDragCancel = false;	// ドラッグキャンセルフラグ
	
	/**
	 * コンストラクタ
	 */
	public PackageViewPanel() {
		
		specData = new PackageSpecData();
		
		// 各レンジ初期化
		initRange(true);
		
		// コンポーネントの配置
		initComponentLayout();
	}
	
	/**
	 * レコード情報初期化(全レコード)
	 */
	public void initAllRecInfo() {
		specData.initAllData();
		recNum = specData.getRecNum();
		chgColor.setSelected(false);
		flat.setSelected(false);
		allBtnCtrl(false);
		setMassRangeMax();
		setTblData(false, "");
		initRange(true);
	}
	
	/**
	 * レコード情報初期化(検索結果レコードのみ)
	 */
	public void initResultRecInfo() {
		specData.initResultData();
		recNum = specData.getRecNum();
		flat.setSelected(false);
		allBtnCtrl(true);
		setMassRangeMax();
		if (recNum > 0) {
			setTblData(true, specData.getRecInfo(0).getId());
		}
		initRange(true);
	}
	
	/**
	 * クエリーレコード情報追加
	 * @param recData レコード情報
	 */
	public void addQueryRecInfo(PackageRecData recData) {
		specData.initAllData();
		specData.addRecInfo(recData);
	}
	
	/**
	 * 検索結果レコード情報追加
	 * @param recData レコード情報
	 * @param changeRecFlag 検索結果レコード情報変更フラグ
	 * @param id 選択レコードのID
	 */
	public void addResultRecInfo(PackageRecData recData, boolean changeRecFlag) {
		if (changeRecFlag) {
			specData.initResultData();
		}
		specData.addRecInfo(recData);
	}
	
	/**
	 * レコード情報追加後処理
	 * レコード情報追加後に必ず呼ぶ。
	 * @param isQuery クエリーレコード情報追加後フラグ(true：クエリー追加後、false：結果追加後)
	 * @param selectedId 選択レコードID
	 * @param sortKey ソートキーとするカラム名
	 */
	public void addRecInfoAfter(boolean isQuery, String selectedId, int sortKey) {
		recNum = specData.getRecNum();
		if (recNum == 0) {
			return;
		}
		if (isQuery) {
			allBtnCtrl(true);
		}
		else {
			specData.setMatchPeakInfo(tolVal, tolUnit);
			specData.sortRecInfo(sortKey);
			mzDisp.setSelected(false);
			mzMatchDisp.setSelected(true);
		}
		setMassRangeMax();
		setTblData(isQuery, selectedId);
		initRange(true);
	}
	
	/**
	 * レコードリストテーブルのレコード選択状態設定
	 * @param selectionQuery クエリーレコードフラグ(true：クエリーレコード、false：クエリーレコード以外)
	 * @param selectionId 選択レコードID
	 */
	public void setTblSelection(boolean selectionQuery, String selectionId) {
		int idCol = recTable.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_ID);
		int typeCol = recTable.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_TYPE);
		// 既に選択されていれば処理しない
		String selectedQuery = String.valueOf(recTable.getValueAt(recTable.getSelectedRow(), typeCol));
		String selectedId = String.valueOf(recTable.getValueAt(recTable.getSelectedRow(), idCol));

		if ((selectionQuery == selectedQuery.equals(QUERY_RECORD)
				|| selectionQuery == selectedQuery.equals(QUERY_RECORD + INTEGRATE_RECORD))
				&& selectionId.equals(selectedId)) {
			return;
		}
		
		// 選択処理
		for (int i=0; i<recTable.getRowCount(); i++) {
			if ((selectionQuery == recTable.getValueAt(i, typeCol).equals(QUERY_RECORD)
					|| selectionQuery == recTable.getValueAt(i, typeCol).equals(QUERY_RECORD + INTEGRATE_RECORD))
					&& selectionId.equals(recTable.getValueAt(i, idCol))) {
				recTable.setRowSelectionInterval(i, i);
				break;
			}
		}
	}
	
	/**
	 * Tolerance入力値設定
	 * @param val tolerance値
	 * @param unit Tolerance単位（true：unit、false：ppm）
	 */
	public void setTolerance(String val, boolean unit) {
		this.tolVal = Float.parseFloat(val);
		this.tolUnit = unit;
	}
	
	/**
	 * massRangeMax設定
	 * レコード数に変更があった場合に呼ぶと、
	 * 現在のレコード情報で一番大きいm/zを持つスペクトルのm/zが表示できるように
	 * スペクトルのマスレンジを設定する。
	 */
	private void setMassRangeMax() {
		massRangeMax = 0;
		PackageRecData recData = null;
		int tmpMassRange = 0;
		for (int i=0; i<recNum; i++) {
			recData = specData.getRecInfo(i);
			// m/zの最大値を100の位(整数第2位)で切り上げた値をレンジとする
			tmpMassRange = new BigDecimal(
					String.valueOf(recData.compMaxMzPrecusor())).setScale(-2, BigDecimal.ROUND_UP).intValue();
			// m/zの最大値が100で割り切れる場合はレンジを+100する
			if (recData.compMaxMzPrecusor() % 100d == 0d) {
				tmpMassRange += 100;
			}
			if (massRangeMax < tmpMassRange) {
				massRangeMax = tmpMassRange;
			}
		}
	}
	
	/**
	 * 各レンジ初期化
	 * @param initAngle アングル初期化フラグ（true：初期化、false：保持）
	 */
	private void initRange(boolean initAngle) {

		isInitRate = true;
		massRange = massRangeMax;
		massStart = 0;
		intensityRange = INTENSITY_RANGE_MAX;
		initDispFlag = initAngle;
		
		this.repaint();
	}
	
	/**
	 * コンポーネント配置
	 * コンポーネントのレイアウトを指定して配置する。
	 */
	private void initComponentLayout() {
		
		// マウスカーソル設定
		PackageViewPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		// レイアウト指定
		setLayout(new BorderLayout());
		GridBagLayout gbl = new GridBagLayout();
		
		// スペクトル表示ペイン
		SpectrumPlotPane plotPane = new SpectrumPlotPane();
		plotPane.setMinimumSize(new Dimension(0, 180));
		plotPane.repaint();
		gbc = new GridBagConstraints();						// レイアウト制約初期化
		gbc.fill = GridBagConstraints.BOTH;					// 垂直、水平サイズの変更を許可
		gbc.weightx = 1;									// 余分の水平スペースを分配
		gbc.weighty = 1;									// 余分の垂直スペースを分配
		gbc.gridwidth = GridBagConstraints.REMAINDER;		// 列最後のコンポーネントに指定
		gbl.setConstraints(plotPane, gbc);
		
		// ボタンペイン
		ButtonPane btnPane = new ButtonPane();
		btnPane.setMinimumSize(new Dimension(0, 44));
		btnPane.setLayout( new FlowLayout(FlowLayout.LEFT, 0, 0) );
		gbc = new GridBagConstraints();						// レイアウト制約初期化
		gbc.fill = GridBagConstraints.HORIZONTAL;			// 水平サイズの変更のみを許可
		gbc.weightx = 1;									// 余分の水平スペースを分配
		gbc.weighty = 0;									// 余分の垂直スペースを分配しない
		gbc.gridwidth = GridBagConstraints.REMAINDER;		// 列最後のコンポーネントに指定
		gbl.setConstraints(btnPane, gbc);
		
		// ステータスラベル
		statusKeyLbl = new JLabel(" ");
		gbc = new GridBagConstraints();						// レイアウト制約初期化
		gbc.fill = GridBagConstraints.HORIZONTAL;			// 水平サイズの変更のみを許可
		gbc.weightx = 0;									// 余分の水平スペースを分配しない
		gbc.weighty = 0;									// 余分の垂直スペースを分配しない
		gbc.gridheight = GridBagConstraints.REMAINDER;		// 行最後のコンポーネントに指定
		gbc.insets = new Insets(4, 4, 4, 4);				// 外側パディング指定
		gbl.setConstraints(statusKeyLbl, gbc);
		statusValLbl = new JLabel(" ");
		statusValLbl.setForeground(new Color(0, 139, 139));
		gbc = new GridBagConstraints();						// レイアウト制約初期化
		gbc.fill = GridBagConstraints.HORIZONTAL;			// 水平サイズの変更のみを許可
		gbc.weightx = 1;									// 余分の水平スペースを分配
		gbc.weighty = 0;									// 余分の垂直スペースを分配しない
		gbc.gridwidth = GridBagConstraints.REMAINDER;		// 列最後のコンポーネントに指定
		gbc.gridheight = GridBagConstraints.REMAINDER;		// 行最後のコンポーネントに指定
		gbc.insets = new Insets(4, 4, 4, 4);				// 外側パディング指定
		gbl.setConstraints(statusValLbl, gbc);
		
		// 表示パネル追加
		JPanel dispPanel = new JPanel();
		dispPanel.setLayout(gbl);
		dispPanel.add(plotPane);
		dispPanel.add(btnPane);
		dispPanel.add(statusKeyLbl);
		dispPanel.add(statusValLbl);	
		add(dispPanel);
		
		// レコードリストペイン追加
		recTable = createRecListTable();
		JScrollPane recListPane = new JScrollPane(recTable);
		recListPane.addMouseListener(new PaneMouseListener());
		recListPane.setMinimumSize(new Dimension(0, 100));
		
		// PackageViewPanelペイン追加
		JSplitPane pkgPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dispPanel, recListPane);
		pkgPane.setMinimumSize(new Dimension(350, 0));
		pkgPane.setDividerLocation((int)(SearchPage.initAppletHight * 0.7));
		pkgPane.setOneTouchExpandable(true);
		add(pkgPane, BorderLayout.CENTER);
	}
	
	/**
	 * レコードリストテーブル作成
	 * @return レコードリストテーブル
	 */
	private JTable createRecListTable() {
		
		recSorter = new TableSorter(new DefaultTableModel(), specData);
		JTable t = new JTable(recSorter) {
			@Override
			public boolean isCellEditable(int row, int column) {
				super.isCellEditable(row, column);
				// セル編集を不可とする
				return false;
			}
			
			@Override
			public void setValueAt(Object value, int row, int col) {
				super.setValueAt(value, row, col);
				// チェックボックスが編集された場合に再描画を行う
				if (recTable.getColumnName(col).equals(SearchPage.COL_LABEL_DISABLE)) {
					PackageRecData recData = specData.getRecInfo(row);
					recData.setDisable(Boolean.parseBoolean(String.valueOf(recTable.getValueAt(row, col))));
					specData.setMatchPeakInfo(tolVal, tolUnit);
					PackageViewPanel.this.repaint();
					
					int hitCol = recTable.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_HIT);
					for (int i=0; i<recTable.getRowCount(); i++) {
						// 編集されたレコードによってHitカラムの値をアップデートする
						setValueAt(specData.getRecInfo(i).getHitPeakNum(), i, hitCol);
					}
				}
			}
			
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				Graphics2D g2 = (Graphics2D)g;
				Rectangle2D area = new Rectangle2D.Float();
				
				// ドラッグ時にドラッグ先塗りつぶし表示
				if (!isSortStatus() && dragRowIndex >= 0) {
					
					g2.setPaint(Color.RED);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
					
					area.setRect(0, (dragRowIndex * getRowHeight()), getWidth(), getRowHeight());
					g2.fill(area);
				}
				
				// change colorの場合はオーダーカラムの行を塗りつぶし表示
				if (chgColor.isSelected()) {
					
					for (int i=0; i<recTable.getRowCount(); i++) {
						// 非表示の場合は色づけしない
						if (Boolean.parseBoolean(String.valueOf(getValueAt(i, getColumnModel().getColumnIndex(SearchPage.COL_LABEL_DISABLE))))) {
							continue;
						}
						g2.setPaint(getColor(i));
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
						
						area.setRect(getCellRect(i, getColumnModel().getColumnIndex(SearchPage.COL_LABEL_ORDER), false));
						g2.fill(area);
					}
				}
			}
		};
		recSorter.setTableHeader(t.getTableHeader());
		t.setMinimumSize(new Dimension(400, 400));
		t.setRowSelectionAllowed(true);
		t.setColumnSelectionAllowed(false);
		t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		t.addMouseListener(new TblMouseListener());						// リスナー追加
		t.addMouseMotionListener(new TblMouseMotionListener());			// リスナー追加
		t.addKeyListener(new TblKeyListener());							// キーリスナー追加
		t.setDefaultRenderer(Object.class, new TblRenderer());			// オリジナルレンダラー
		ListSelectionModel lm = t.getSelectionModel();
		lm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lm.addListSelectionListener(new LmSelectionListener());

		
		// カラムセット
		String[] columnLabel = {
				SearchPage.COL_LABEL_ORDER, SearchPage.COL_LABEL_TYPE, SearchPage.COL_LABEL_NAME,
				SearchPage.COL_LABEL_MATCH, SearchPage.COL_LABEL_ID, SearchPage.COL_LABEL_DISABLE, 
				SearchPage.COL_LABEL_CONTRIBUTOR, SearchPage.COL_LABEL_SCORE, SearchPage.COL_LABEL_HIT,
				SearchPage.COL_LABEL_PEAK, SearchPage.COL_LABEL_PRECURSOR };
		DefaultTableModel model = (DefaultTableModel)recSorter.getTableModel();
		model.setColumnIdentifiers(columnLabel);

		// 列幅セット
		t.getColumn(t.getColumnName(0)).setPreferredWidth(36);
		t.getColumn(t.getColumnName(1)).setPreferredWidth(75);
		t.getColumn(t.getColumnName(2)).setPreferredWidth(360);
		t.getColumn(t.getColumnName(3)).setPreferredWidth(36);
		t.getColumn(t.getColumnName(4)).setPreferredWidth(70);
		t.getColumn(t.getColumnName(5)).setPreferredWidth(47);
		t.getColumn(t.getColumnName(6)).setPreferredWidth(70);
		t.getColumn(t.getColumnName(7)).setPreferredWidth(70);
		t.getColumn(t.getColumnName(8)).setPreferredWidth(20);
		t.getColumn(t.getColumnName(9)).setPreferredWidth(36);
		t.getColumn(t.getColumnName(10)).setPreferredWidth(58);

		return t;
	}
	
	/**
	 * レコードリストテーブルデータ設定
	 * @param selectedQuery クエリーレコード選択フラグ(true：クエリー追加後、false：結果追加後)
	 * @param id 選択レコードのID
	 */
	private void setTblData(boolean selectedQuery, String selectedId) {
		
		// ソート解除
		for (int i=0; i<recTable.getColumnCount(); i++) {
			recSorter.setSortingStatus(i, TableSorter.NOT_SORTED);
		}
		
		DefaultTableModel dataModel = (DefaultTableModel)recSorter.getTableModel();
		dataModel.setRowCount(0);
		
		if (recNum == 0) {
			return;
		}
		
		PackageRecData recData = null;
		Object[] recordData;
		int number = recNum;
		for (int i=0; i<recNum; i++) {
			recData = specData.getRecInfo(i);
			
			// レコード情報生成
			recordData = new Object[dataModel.getColumnCount()];
			recordData[0] = String.valueOf(number--);
			if (recData.isQueryRecord()) {
				recordData[1] = QUERY_RECORD;
			}
			else if (recData.isResultRecord()) {
				recordData[1] = RESULT_RECORD;
			}
			if (recData.isIntegRecord()) {
				recordData[1] = recordData[1] + INTEGRATE_RECORD;
			}
			recordData[2] = recData.getName();
			recordData[3] = recData.getMatchPeakNum();
			recordData[4] = recData.getId();
			recordData[5] = new Boolean(recData.isDisable());
			if (!recData.getSite().equals("")) {
				recordData[6] = SearchPage.siteNameList[Integer.parseInt(recData.getSite())];
			}
			else {
				recordData[6] = "";
			}
			recordData[7] = recData.getScore();
			recordData[8] = recData.getHitPeakNum();
			recordData[9] = String.valueOf(recData.getPeakNum());
			recordData[10] = recData.getPrecursor();
			
			// テーブルへのレコード追加
			dataModel.addRow(recordData);
			
			if (selectedQuery == recData.isQueryRecord() && selectedId.equals(recData.getId())) {
				// クエリーDBタブ、DB Hitタブでの選択レコードを初期選択
				recTable.setRowSelectionInterval(i, i);
			}
		}
	}
	
	/**
	 * レコードリストテーブル表示状態一括設定
	 * @param disable 表示状態(true：表示、false：非表示)
	 */
	private void setTblDispStatus(boolean disable) {
    	if (recTable.getRowCount() > 0) {
    		int disableCol = recTable.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_DISABLE);
			for (int i=0; i<recTable.getRowCount(); i++) {
				recTable.setValueAt(new Boolean(disable), i, disableCol);
			}
    	}
	}
	
	/**
	 * レコードリストテーブルソート状態取得
	 * レコードリストテーブルのいずれかのカラムがソートされているかを取得する。
	 * @return ソート状態(true：ソート中、fale：デフォルト)
	 */
	private boolean isSortStatus() {
    	if (recTable.getRowCount() > 0) {
			int sortStatus = TableSorter.NOT_SORTED;
			for (int i=0; i<recTable.getColumnCount(); i++) {
				sortStatus = recSorter.getSortingStatus(i);
				if (sortStatus != TableSorter.NOT_SORTED) {
					return true;
				}
			}
    	}
    	return false;
	}
	
	/**
	 * レコードリスト用ポップアップ表示
	 * @param e マウスイベント
	 */
	private void recListPopup(MouseEvent e) {
		final int selRow = recTable.getSelectedRow();
		final int rowCnt = recTable.getRowCount();
		
		JMenuItem item1 = new JMenuItem("Show Record");
		item1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showRecordPage(selRow);
			}
		});
		int siteCol = recTable.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_CONTRIBUTOR);
		if (selRow == -1 || String.valueOf(recTable.getValueAt(selRow, siteCol)).equals("")) {
			item1.setEnabled(false);
		}
		
		JSeparator sep = new JSeparator();

		JMenuItem item2 = new JMenuItem("All Disable");
		item2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTblDispStatus(true);
			}
		});
		if (rowCnt == 0) {
			item2.setEnabled(false);
		}
		
		JMenuItem item3 = new JMenuItem("All Enable");
		item3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTblDispStatus(false);
			}
		});
		if (rowCnt == 0) {
			item3.setEnabled(false);
		}
		
		// ポップアップ表示
		JPopupMenu showRecPopup = new JPopupMenu();
		showRecPopup.add(item1);
		showRecPopup.add(sep);
		showRecPopup.add(item2);
		showRecPopup.add(item3);
		showRecPopup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	/**
	 * m/zの表示用フォーマット
	 * 画面表示用にm/zの桁数を合わせて返却する
	 * @param mass フォーマット対象のm/z
	 * @param isForce 桁数強制統一フラグ（true:0埋めと切捨てを行う、false:切捨てのみ行う）
	 * @return フォーマット後のm/z
	 */
	private String formatMass(double mass, boolean isForce) {
		final int ZERO_DIGIT = 4;
		String massStr = String.valueOf(mass);
		if (isForce) {
			// 強制的に全ての桁を統一する（0埋めと切捨てを行う）
			if (massStr.indexOf(".") == -1) {
				massStr += ".0000";
			}
			else {
				if (massStr.indexOf(".") != -1) {
					String [] tmpMzStr = massStr.split("\\.");
					if (tmpMzStr[1].length() <= ZERO_DIGIT) {
						int addZeroCnt = ZERO_DIGIT - tmpMzStr[1].length();
						for (int j=0; j<addZeroCnt; j++) {
							massStr += "0";
						}
					}
					else {
						if (tmpMzStr[1].length() > ZERO_DIGIT) {
							massStr = tmpMzStr[0] + "." + tmpMzStr[1].substring(0, ZERO_DIGIT);
						}
					}
				}
			}
		}
		else {
			// 桁を超える場合のみ桁を統一する（切捨てのみ行う）
			if (massStr.indexOf(".") != -1) {
				String [] tmpMzStr = massStr.split("\\.");
				if (tmpMzStr[1].length() > ZERO_DIGIT) {
					massStr = tmpMzStr[0] + "." + tmpMzStr[1].substring(0, ZERO_DIGIT);
				}
			}
		}
		return massStr;
	}
	
	/**
	 * Colorテーブルからの色取得
	 * Colorテーブルから色を取得して返却。
	 * Colorテーブル内の色を使いまわす。
	 * @param index インデックス
	 * @return Colorオブジェクト
	 */
	private Color getColor(int index) {
		return colorTable[index % colorTable.length];
	}

	/**
	 * 全ボタン有効無効制御
	 * @param enable 有効無効
	 */
	private void allBtnCtrl(boolean enable) {
		
		if (enable) {
			// スペクトル移動ボタン
			if (!isInitRate) {
				leftMostBtn.setEnabled(true);
				leftBtn.setEnabled(true);
				rightBtn.setEnabled(true);
				rightMostBtn.setEnabled(true);
			}
			else {
				leftMostBtn.setEnabled(false);
				leftBtn.setEnabled(false);
				rightBtn.setEnabled(false);
				rightMostBtn.setEnabled(false);
			}
		}
		else {
			leftMostBtn.setEnabled(false);
			leftBtn.setEnabled(false);
			rightBtn.setEnabled(false);
			rightMostBtn.setEnabled(false);
			mzDisp.setSelected(false);
		}
		
		mzDisp.setEnabled(enable);
		mzMatchDisp.setEnabled(enable);
		if (recNum <= 1) {
			mzMatchDisp.setSelected(false);
		}
		chgColor.setEnabled(enable);
		xAxisUp.setEnabled(enable);
		xAxisDown.setEnabled(enable);
		yAxisUp.setEnabled(enable);
		yAxisDown.setEnabled(enable);
		flat.setEnabled(enable);
		topAngleBtn.setEnabled(enable);
		sideAngleBtn.setEnabled(enable);
	}
	
	/**
	 * レコードページ表示
	 * @param selectIndex 選択行インデックス
	 */
	private void showRecordPage(int selectIndex) {
		String id = specData.getRecInfo(selectIndex).getId();
		String site = specData.getRecInfo(selectIndex).getSite();

		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
		String reqUrl = SearchPage.baseUrl + "jsp/" + MassBankCommon.DISPATCHER_NAME
				+ "?type=" + typeName + "&id=" + id + "&site=" + site;
		try {
			SearchPage.context.showDocument(new URL(reqUrl), "_blank");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * スペクトル表示ペイン
	 * PackageViewPanelのインナークラス
	 */
	class SpectrumPlotPane extends JPanel implements MouseListener, MouseMotionListener {
				
		private long lastClickedTime = 0;									// 最後にクリックした時間
		
		private boolean isDrag = false;									// ドラッグイベント有効無効フラグ
		private int[] dragArea = null;									// ドラッグ範囲保持用
		
		private final String LT = "LeftTop";								// 左上を表す定数
		private final String LB = "LeftBottom";							// 左下を表す定数
		private final String RT = "RightTop";								// 右上を表す定数
		private final String RB = "RightBottom";							// 右下を表す定数
		
		private HashMap<String, Point> prevGraphArea = 
			new HashMap<String, Point>(4);									// 一番手前のグラフエリア範囲保持用
		
		private final Color gridColor1 = Color.LIGHT_GRAY;					// グリッドメイン色
		private final Color gridColor2 = new Color(235, 235, 235);		// グリッドサブ色
		private final Color graphColorBack = new Color(248, 248, 253);	// グラフ背面色
		private final Color graphColorSide = new Color(240, 240, 255);	// グラフ側面色
		private final Color graphColorBottom = new Color(250, 250, 250);	// グラフ底面色
		
		private final Color selectRecColor = new Color(153, 51, 255);		// レコード選択色
		private final Color onCursorColor = Color.BLUE;					// カーソル上色
		
		private double xscale = 0d;											// x軸方向拡大率
		private double yscale = 0d;											// y軸方向拡大率
		
		private int marginTop = MARGIN;									// 上部余白
		private int marginRight = MARGIN;									// 右部余白
		
		private JPopupMenu contextPopup = null;							// コンテキストポップアップメニュー
		private JPopupMenu selectPopup = null;				 				// ピーク選択ポップアップメニュー
		
		private ArrayList<String> overCursorMz = null;						// カーソル上ピークm/zリスト
		
		
		/**
		 * デフォルトコンストラクタ
		 */
		public SpectrumPlotPane() {
			cursorPoint = new Point();
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		/**
		 * x軸目盛り幅計算
		 * @param range マスレンジ
		 */
		private int stepCalc(int range) {
			
			if (range < 10) {
				return 1;
			}
			if (range < 20) {
				return 2;
			}
			if (range < 50) {
				return 5;
			}
			if (range < 100) {
				return 10;
			}
			if (range < 250) {
				return 25;
			}
			if (range < 500) {
				return 50;
			}
			return 100;
		}
		
		/**
		 * グラフのアングルに関する設定
		 */
		private void setAngleProperty() {
			
			// 初期表示アングル設定
			if (initDispFlag) {
				if (recNum <= 1 ) {
					maxMoveXPoint = 0;
					minMoveXPoint = 0;
					moveXPoint = 0;
					maxMoveYPoint = 0;
					minMoveYPoint = 0;
					moveYPoint = 0;
					return;
				}
				
				// x軸初期可動値
				if (width == 0) {
					moveXPoint = 0;
				}
				else {
					moveXPoint = (int)((width - (MARGIN * 2)) * 0.2) / (recNum - 1);
				}
				
				// y軸初期可動値
				if (height == 0) {
					moveYPoint = 0;
				}
				else {
					moveYPoint = (int)((height - (MARGIN * 2)) * 0.8) / (recNum - 1);
				}
				
				initDispFlag = false;
			}
			
			
			// x軸最大及び最小可動値の設定
			if (recNum > 1) {
				maxMoveXPoint = (int)((width - (MARGIN * 2)) * 0.8) / (recNum - 1);
				if (0 > maxMoveXPoint) {
					maxMoveXPoint = 0;
				}
			
				minMoveXPoint = (int)((width - (MARGIN * 2)) * 0.1) / (recNum - 1);
				if (0 > minMoveXPoint) {
					minMoveXPoint = 0;
				}
			}
			else {
				maxMoveXPoint = 0;
				minMoveXPoint = 0;
			}
				
			// x軸可動値の範囲チェック
			if (moveXPoint > maxMoveXPoint) {
				moveXPoint = maxMoveXPoint;
			}
			else if (moveXPoint < minMoveXPoint) {
				if (moveTimer == null
						|| (!moveTimer.isRunning() && !flat.isSelected())) {
					moveXPoint = minMoveXPoint;
				}
			}

			// y軸最大及び最小可動値の設定
			if (recNum > 1) {
				maxMoveYPoint = (int)((height - (MARGIN * 2)) * 0.9) / (recNum - 1);
				if (0 > maxMoveYPoint) {
					maxMoveYPoint = 0;
				}
				
				minMoveYPoint = (int)((height - (MARGIN * 2)) * 0.2) / (recNum - 1);
				if (0 > minMoveYPoint) {
					minMoveYPoint = 0;
				}
			}
			else {
				maxMoveYPoint = 0;
				minMoveYPoint = 0;
			}
				
			// y軸可動値の範囲チェック
			if (moveYPoint > maxMoveYPoint) {
				moveYPoint = maxMoveYPoint;
			}
			else if (moveYPoint < minMoveYPoint) {
				if (moveTimer == null
						|| (!moveTimer.isRunning() && !flat.isSelected())) {
					moveYPoint = minMoveYPoint;
				}
			}
		}
		
		/**
		 * ステータスラベル文字列設定
		 */
		private void setStatusLabel() {
			if (specData.getSelectedPeakNum() != 0) {
				StringBuffer sb = new StringBuffer();
				Iterator<Double> ite = specData.getSelectedPeakList().iterator();
				while (ite.hasNext()) {
					sb.append(String.valueOf(ite.next()) + ",  ");
				}
				statusKeyLbl.setText("Selected m/z :");
				statusValLbl.setText(sb.toString().substring(0, sb.length()-3));
			}
			else {
				statusKeyLbl.setText(" ");
				statusValLbl.setText(" ");
			}
		}
		
		/**
		 * ペイントコンポーネント
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		public void paintComponent(Graphics g) {
			
			super.paintComponent(g);
			
			final FontUIResource font1 = new FontUIResource(
					g.getFont().getFamily(),g.getFont().getStyle(),14);	// フォント1
			final FontUIResource font2 = new FontUIResource(
					g.getFont().getFamily(),g.getFont().getStyle(),9);	// フォント2
			final FontUIResource font3 = new FontUIResource(
					g.getFont().getFamily(),g.getFont().getStyle(),12);	// フォント3
			
			// 画面サイズ再設定
			width = getWidth();
			height = getHeight();
			
			// グラフの可動に関する設定の再設定（画面リサイズに対応するための処理）
			setAngleProperty();
			
			// 描画エリア全体の背景描画
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
			
			// x軸方向拡大率の算出
//			xscale = (width - 2.0f * MARGIN) / massRange;
			xscale = (width - (2.0d * MARGIN) - ((recNum-1) * moveXPoint)) / massRange;
			if (xscale < 0d) {
				xscale = 0d;
			}
			
			// y軸方向拡大率の算出
//			float yscale = (height - (float)(MARGIN +marginTop) ) / intensityRange;
			yscale = (height - (double)(MARGIN + marginTop + (recNum-1) * moveYPoint) ) / intensityRange;
			if (yscale < 0d) {
				yscale = 0d;
			}
			
			int tmpMarginTop = 0;
			int tmpMarginRight = 0;
			
			int baseX = 0;			// x軸とy軸の交点のx座標
			int baseY = 0;			// x軸とy軸の交点のy座標
			int nextBaseX = 0;		// 次レコードのx軸とy軸の交点のx座標
			int nextBaseY = 0;		// 次レコードのx軸とy軸の交点のy座標
			
			int loopCount = ((recNum != 0) ? recNum : (recNum + 1));
			
			// グラフ背景色を描画
			int[] sideXPlots = new int[4];		// グラフ側面用x軸
			int[] sideYPlots = new int[4];		// グラフ側面用y軸
			int[] backXPlots = new int[4];		// グラフ背面用x軸
			int[] backYPlots = new int[4];		// グラフ背面用y軸
			int[] bottomXPlots = new int[4];	// グラフ底面用x軸
			int[] bottomYPlots = new int[4];	// グラフ底面用y軸
			for (int i=0; i<loopCount; i++) {
				
				baseX = MARGIN + (loopCount-1-i) * moveXPoint;
				baseY = height - MARGIN - (loopCount-1-i) * moveYPoint;

				nextBaseX = baseX - moveXPoint;
				nextBaseY = baseY + moveYPoint;
				
				tmpMarginTop = marginTop + (i * moveYPoint);
				tmpMarginRight = marginRight + (i * moveXPoint);
				if (tmpMarginTop > baseY) {
					tmpMarginTop = baseY;
				}
				if (width - tmpMarginRight < baseX) {
					tmpMarginRight = (width - baseX > 0) ? (width - baseX) : 0;
				}
				
				if ( i == 0 ) {
					sideXPlots[0] = baseX;
					sideYPlots[0] = tmpMarginTop;
					sideXPlots[1] = baseX;
					sideYPlots[1] = baseY;
					
					backXPlots[0] = baseX;
					backYPlots[0] = tmpMarginTop;
					backXPlots[1] = width - tmpMarginRight;
					backYPlots[1] = tmpMarginTop;
					backXPlots[2] = width - tmpMarginRight;
					backYPlots[2] = baseY;
					backXPlots[3] = baseX;
					backYPlots[3] = baseY;
					
					bottomXPlots[0] = baseX;
					bottomYPlots[0] = baseY;
					bottomXPlots[1] = ((width - tmpMarginRight > baseX) ? (width - tmpMarginRight) : baseX);
					bottomYPlots[1] = baseY;
				}
				if ( i == (loopCount-1) ) {
					sideXPlots[2] = baseX;
					sideYPlots[2] = baseY;
					sideXPlots[3] = baseX;
					sideYPlots[3] = tmpMarginTop;
					
					bottomXPlots[2] = width - tmpMarginRight;
					bottomYPlots[2] = baseY;
					bottomXPlots[3] = baseX;
					bottomYPlots[3] = baseY;
				}
			}
			g.setColor(graphColorBack);
			g.fillPolygon(backXPlots, backYPlots, 4);		// 背面
			
			g.setColor(graphColorSide);
			g.fillPolygon(sideXPlots, sideYPlots, 4);		// 側面
			
			g.setColor(graphColorBottom);
			g.fillPolygon(bottomXPlots, bottomYPlots, 4);	// 底面
			
			
			
			// グラフグリッド描画
			g.setFont(font2);
			for (int i=0; i<loopCount; i++) {
 				g.setColor(Color.LIGHT_GRAY);
				
				baseX = MARGIN + (loopCount-1-i) * moveXPoint;
				baseY = height - MARGIN - (loopCount-1-i) * moveYPoint;
				
				nextBaseX = baseX - moveXPoint;
				nextBaseY = baseY + moveYPoint;
				
				tmpMarginTop = marginTop + (i * moveYPoint);
				tmpMarginRight = marginRight + (i * moveXPoint);
				if (tmpMarginTop > baseY) {
					tmpMarginTop = baseY;
				}
				if (width - tmpMarginRight < baseX) {
					tmpMarginRight = (width - baseX > 0) ? (width - baseX) : 0;
				}
				
				int step = stepCalc((int)massRange);
				int start = (step - (int)massStart % step) % step;
				
				// x軸
				for (int j=start; j<(int)massRange; j+=step) {
					g.setColor(gridColor2);
					// x軸を基準にグラフ背面y軸グリッド描画
					if ( i == 0 ) {
						g.drawLine(baseX + (int)(j * xscale),
								   baseY,
								   baseX + (int)(j * xscale),
								   tmpMarginTop);
					}
					
					// x軸を基準にグラフ底面z軸グリッド描画
					if ( i != (loopCount-1) ) {
						g.drawLine(baseX + (int)(j * xscale),
								   baseY,
								   nextBaseX + (int)(j * xscale),
								   nextBaseY);
					}
					
					g.setColor(gridColor1);
					// グラフ基準x軸目盛り描画
					g.drawLine(baseX + (int)(j * xscale),
							   baseY,
							   baseX + (int)(j * xscale),
							   baseY + 2);
					
					// グラフ基準x軸へのm/z文字列描画
					if (i == (loopCount-1)) {
						g.drawString(formatMass(j + massStart, true),
									 baseX + (int)(j * xscale) - 5,
									 height - 1);
					}
				}
				
				// y軸
				for (int j=0; j<=intensityRange; j += intensityRange / 5) {
					g.setColor(gridColor2);
					// y軸を基準にグラフ背面x軸グリッド描画
					if ( i == 0 ) {
						g.drawLine(baseX,
								   baseY - (int)(j * yscale),
								   width - tmpMarginRight,
								   baseY - (int)(j * yscale));
					}
					
					// y軸を基準にグラフ側面z軸グリッド描画
					if ( i != (loopCount-1) ) {
						g.drawLine(baseX,
								   baseY - (int)(j * yscale),
								   nextBaseX,
								   nextBaseY - (int)(j * yscale));
					}
					
					g.setColor(gridColor1);
					// グラフ基準y軸目盛り描画
					g.drawLine(baseX - 2,
							   baseY - (int)(j * yscale),
							   baseX,
							   baseY - (int)(j * yscale));
					
					// グラフ基準y軸への強度文字列描画
					if (i == (loopCount-1)) {
						g.drawString(String.valueOf(j),	
									 0,
									 baseY - (int)(j * yscale));
					}
				}
				
				
				// 主要グリッド描画
				g.setColor(gridColor1);
				if ( i != (loopCount-1) ) {
					g.drawLine(baseX,
							   baseY,
							   nextBaseX,
							   nextBaseY);									// z軸
				}
				if (recTable != null && i == recTable.getSelectedRow()) {
					g.setColor(selectRecColor.darker());
				}
				else {
					g.setColor(gridColor1);
				}
				g.drawLine(baseX, tmpMarginTop, baseX, baseY);				// y軸
				g.drawLine(baseX, baseY, width - tmpMarginRight, baseY);	// x軸
				
				
				// 一番手前のグラフエリアの範囲を保持
				if (i == (recNum-1)) {
					prevGraphArea.put(LB, new Point(baseX, baseY));
					prevGraphArea.put(LT, new Point(baseX, tmpMarginTop));
					prevGraphArea.put(RT, new Point(width - tmpMarginRight, tmpMarginTop));
					prevGraphArea.put(RB, new Point(width - tmpMarginRight, baseY));					
				}
				
				// マウスでドラッグした領域を黄色の立体で囲む
				if (i == (recNum-1) && underDrag) {
					
					int xpos = Math.min(fromPos.x, toPos.x);
					int dragWidth = Math.abs(fromPos.x - toPos.x);
					
					
					// ドラッグ範囲を立体で描画
					g.setXORMode(Color.WHITE);
					g.setColor(Color.YELLOW);
					g.fillPolygon(
							new int[]{xpos+dragWidth, xpos+dragWidth, xpos, xpos},
							new int[]{tmpMarginTop, baseY, baseY, tmpMarginTop},
							4);	// 正面
					g.setColor(new Color(255, 255, 90));
					g.fillPolygon(
							new int[]{xpos+dragWidth, xpos+dragWidth+((recNum-1)*moveXPoint), xpos+((recNum-1)*moveXPoint), xpos},
							new int[]{tmpMarginTop, tmpMarginTop-((recNum-1)*moveYPoint), tmpMarginTop-((recNum-1)*moveYPoint), tmpMarginTop},
							4);	// 天面
					g.setColor(new Color(225, 225, 0));
					g.fillPolygon(
							new int[]{xpos+dragWidth, xpos+dragWidth+((recNum-1)*moveXPoint), xpos+dragWidth+((recNum-1)*moveXPoint), xpos+dragWidth},
							new int[]{tmpMarginTop, tmpMarginTop-((recNum-1)*moveYPoint), baseY-((recNum-1)*moveYPoint), baseY},
							4);	// 側面
					
					// 通常見えない立体の境界線を描画
					g.setPaintMode();
					g.setColor(new Color(200, 200, 0));
					g.drawLine(
							xpos+((recNum-1)*moveXPoint),
							tmpMarginTop-((recNum-1)*moveYPoint),
							xpos+((recNum-1)*moveXPoint),
							baseY-((recNum-1)*moveYPoint)
							);	// y軸
					g.drawLine(
							xpos+((recNum-1)*moveXPoint), 
							baseY-((recNum-1)*moveYPoint),
							xpos, 
							baseY
							);	// z軸
					g.drawLine(
							xpos+((recNum-1)*moveXPoint), 
							baseY-((recNum-1)*moveYPoint), 
							xpos+dragWidth+((recNum-1)*moveXPoint), 
							baseY-((recNum-1)*moveYPoint)
							);	// x軸
					
					// 立体の境界線を描画
					g.setColor(new Color(150, 150, 0));
					g.drawPolygon(
							new int[]{xpos + dragWidth,xpos+dragWidth, xpos, xpos},
							new int[]{tmpMarginTop, baseY, baseY, tmpMarginTop},
							4);	// 正面
					g.drawPolygon(
							new int[]{xpos+dragWidth, xpos+dragWidth+((recNum-1)*moveXPoint), xpos+((recNum-1)*moveXPoint), xpos},
							new int[]{tmpMarginTop, tmpMarginTop-((recNum-1)*moveYPoint), tmpMarginTop-((recNum-1)*moveYPoint), tmpMarginTop},
							4);	// 天面
					g.drawPolygon(
							new int[]{xpos+dragWidth, xpos+dragWidth+((recNum-1)*moveXPoint), xpos+dragWidth+((recNum-1)*moveXPoint), xpos+dragWidth},
							new int[]{tmpMarginTop, tmpMarginTop-((recNum-1)*moveYPoint), baseY-((recNum-1)*moveYPoint), baseY},
							4);	// 側面
					
					// ドラッグ範囲を保持
					dragArea = new int[]{xpos, xpos + dragWidth};
				}
			}
			
			
			PackageRecData recData = null;
			overCursorMz = new ArrayList<String>();
			
			// カーソルライン表示処理
			ArrayList<ArrayList<String>> cursorLineXInfo = new ArrayList<ArrayList<String>>();		// レコード毎のカーソルラインx座標情報
			for (int i=0; i<recNum; i++) {
				
				recData = specData.getRecInfo(i);
				
				baseX = MARGIN + (recNum-1-i) * moveXPoint;
				baseY = height - MARGIN - (recNum-1-i) * moveYPoint;

				tmpMarginTop = marginTop + (i * moveYPoint);
				tmpMarginRight = marginRight + (i * moveXPoint);
				if (tmpMarginTop > baseY) {
					tmpMarginTop = baseY;
				}
				if (width - tmpMarginRight < baseX) {
					tmpMarginRight = (width - baseX > 0) ? (width - baseX) : 0;
				}
				
				int step = stepCalc((int)massRange);
				int start = (step - (int)massStart % step) % step;
				
				// ピークがない場合
				if ( recData.getPeakNum() == 0 ) {
					continue;
				}
				
				int end, its, x, y, w;
				double mz;
				start = recData.getIndex(massStart);
				end = recData.getIndex(massStart + massRange);
				for (int j=start; j<end; j++) {
					boolean isSelectedLine = false;
					mz = recData.getMz(j);
					its = recData.getIntensity(j);
					
					x = baseX + (int)((mz - massStart) * xscale) - (int)Math.floor(xscale / 8);
					y = baseY - (int)(its * yscale);
					w = (int)(xscale / 8);
					
					// 描画パラメータ（幅）調整
					if (w < 2) {
						w = 2;
					}
					else if (w < 3) {
						w = 3;
					}
					
					// y軸より左側には描画しないように調整
					if(baseX > x) {
						w = (w - (baseX - x) > 0) ? (w - (baseX - x)) : 1;
						x = baseX + 1;
					}
					
					// 選択済ピークであるかを判定
					if (specData.getSelectedPeakNum() != 0) {
						Iterator<Double> ite = specData.getSelectedPeakList().iterator();
						while (ite.hasNext()) {
							if (String.valueOf(mz).equals(String.valueOf(ite.next()))) {
								isSelectedLine = true;
								break;
							}
						}
						// カーソルライン描画
						if ( isSelectedLine ) {
							g.setColor(Color.CYAN.darker().darker());
							g.drawLine(
									x,
									baseY,
									x + (i * moveXPoint),
									baseY - (i * moveYPoint)
									);											// カーソルピークより奥
							g.drawLine(
									x,
									baseY,
									x - ((recNum - 1 - i) * moveXPoint),
									baseY + ((recNum - 1 - i) * moveYPoint)
									);											// カーソルピークより手前
							g.drawLine(
									x - ((recNum - 1 - i) * moveXPoint),
									baseY + ((recNum - 1 - i) * moveYPoint),
									x - ((recNum - 1 - i) * moveXPoint) - 2,
									baseY + ((recNum - 1 - i) * moveYPoint) + 6
									);											// カーソルライン目印
							g.drawLine(
									x - ((recNum - 1 - i) * moveXPoint),
									baseY + ((recNum - 1 - i) * moveYPoint),
									x - ((recNum - 1 - i) * moveXPoint) + 2,
									baseY + ((recNum - 1 - i) * moveYPoint) + 6
									);											// カーソルライン目印
						}
					}
					
					
					// 拡大処理中は処理しない
					if (animationTimer == null || !animationTimer.isRunning()) {
						
						// カーソルの場所（X/Y座標）がPeakの描画エリアに含まれている場合				
						if ((cursorPoint.x >= x 
								&& cursorPoint.x <= (x + w) 
								&& cursorPoint.y >= y
								&& cursorPoint.y <= baseY)) {
							
							ArrayList<String> cursorLineX = new ArrayList<String>();	// レコード毎のカーソルラインx座標
							for (int k=0; k<recNum; k++) {
								
								if (k <= i) {
									cursorLineX.add(String.valueOf(x + ((i - k) * moveXPoint)));
								}
								else {
									cursorLineX.add(String.valueOf(x - ((k - i) * moveXPoint)));
								}
							}
							cursorLineXInfo.add(cursorLineX);
							
							// カーソルライン描画
							if ( !isSelectedLine ) {
								// 選択済ピークでない場合に描画
								g.setColor(onCursorColor.darker());
								g.drawLine(
										x,
										baseY,
										x + (i * moveXPoint),
										baseY - (i * moveYPoint)
										);											// カーソルピークより奥
								g.drawLine(
										x,
										baseY,
										x - ((recNum - 1 - i) * moveXPoint),
										baseY + ((recNum - 1 - i) * moveYPoint)
										);											// カーソルピークより手前
								g.drawLine(
										x - ((recNum - 1 - i) * moveXPoint),
										baseY + ((recNum - 1 - i) * moveYPoint),
										x - ((recNum - 1 - i) * moveXPoint) - 2,
										baseY + ((recNum - 1 - i) * moveYPoint) + 6
										);											// カーソルライン目印
								g.drawLine(
										x - ((recNum - 1 - i) * moveXPoint),
										baseY + ((recNum - 1 - i) * moveYPoint),
										x - ((recNum - 1 - i) * moveXPoint) + 2,
										baseY + ((recNum - 1 - i) * moveYPoint) + 6
										);											// カーソルライン目印
							}
							
							overCursorMz.add(String.valueOf(mz));
						}
					}
				}	// end for
			}	// end for
			
			
			// ピーク描画
			for (int i=0; i<recNum; i++) {
				
				recData = specData.getRecInfo(i);
				
				if (recData.isDisable()) {
					continue;
				}
				
				baseX = MARGIN + (recNum-1-i) * moveXPoint;
				baseY = height - MARGIN - (recNum-1-i) * moveYPoint;

				nextBaseX = baseX - moveXPoint;
				nextBaseY = baseY + moveYPoint;
				
				tmpMarginTop = marginTop + (i * moveYPoint);
				tmpMarginRight = marginRight + (i * moveXPoint);
				if (tmpMarginTop > baseY) {
					tmpMarginTop = baseY;
				}
				if (width - tmpMarginRight < baseX) {
					tmpMarginRight = (width - baseX > 0) ? (width - baseX) : 0;
				}
				
				int step = stepCalc((int)massRange);
				int start = (step - (int)massStart % step) % step;
				
				// ピークがない場合
				if ( recData.getPeakNum() == 0 ) {
					continue;
				}
				
				
				int end, its, x, y, w, h;
				double mz;
				start = recData.getIndex(massStart);
				end = recData.getIndex(massStart + massRange);
				for (int j=start; j<end; j++) {
					boolean isSelectedLine = false;
					boolean isOnLine = false;						// マウスカーソルライン上ピークフラグ
					boolean isDragArea = false;					// ドラッグ範囲内フラグ
					
					mz = recData.getMz(j);
					its = recData.getIntensity(j);
					
//					x = MARGIN + (int) ((peak - massStart) * xscale) - (int) Math.floor(xscale / 8);
					x = baseX + (int)((mz - massStart) * xscale) - (int)Math.floor(xscale / 8);
					y = baseY - (int)(its * yscale);
					w = (int)(xscale / 8);
					h = (int)(its * yscale);
					
					// 描画パラメータ（高さ、位置）調整
					if (h == 0) {
						y -= 1;
						h = 1;
					}
					// 描画パラメータ（幅）調整
					if (w < 2) {
						w = 2;
					}
					else if (w < 3) {
						w = 3;
					}
					
					// y軸より左側には描画しないように調整
					if(baseX > x) {
						w = (w - (baseX - x) > 0) ? (w - (baseX - x)) : 1;
						x = baseX + 1;
					}
					
					
					boolean inPeakArea = false;
					
					// 拡大処理中は処理しない
					if (animationTimer == null || !animationTimer.isRunning()) {
						// マウスカーソルラインの場所（X/Y座標）がPeakの描画エリアに含まれているかを判定	
						for (int k=0; k<cursorLineXInfo.size(); k++) {
							
							if (Integer.parseInt(String.valueOf(cursorLineXInfo.get(k).get(i))) == x) {
								inPeakArea = true;
								break;
							}
						}
						
						// さらに、エリア内の場合はカーソル上Peakのm/zと同じかを判定
						if (inPeakArea) {
							for (int k=0; k<overCursorMz.size(); k++) {
								if (Double.parseDouble(overCursorMz.get(k)) == mz) {
									isOnLine = true;
									break;
								}
							}
						}
					}
					
					// 選択済ピークm/zであるかの判定
					if (specData.getSelectedPeakNum() != 0) {
						Iterator<Double> ite = specData.getSelectedPeakList().iterator();
						while (ite.hasNext()) {
							if (String.valueOf(mz).equals(String.valueOf(ite.next()))) {
								isSelectedLine = true;
								break;
							}
						}
					}
					
					// ドラッグ範囲の中にPeakが含まれているかを判定
					if ( underDrag ) {
						if ( dragArea[0] + ((recNum-1-i) * moveXPoint) <= (x + w) 
								&& dragArea[1] + ((recNum-1-i) * moveXPoint) >= x) {
							
							isDragArea = true;
						}
					}
					
					
					// ピーク描画
					g.setPaintMode();
					if (isSelectedLine) {
						g.setColor(Color.CYAN.darker());						// ラインピーク選択時の色
					}
					else if (isOnLine) {
						g.setColor(onCursorColor);								// ライン上の色
					}
					else if ( isDragArea ) {									// ドラッグ範囲内の色
						if (!chgColor.isSelected() && recData.getPeakColor(j).equals(Color.RED)) {
							g.setColor(Color.MAGENTA);
						}
						else if (!chgColor.isSelected() && recData.getPeakColor(j).equals(Color.MAGENTA)) {
							g.setColor(Color.RED);
						}
						else {
							g.setColor(Color.BLUE);
						}
					}
					else if (chgColor.isSelected()) {
						g.setColor(getColor(i));								// オートカラー変更時の色
					}
					else if (y < tmpMarginTop) {
						g.setColor(Color.GRAY);									// 通常時の色(ピークがグラフ内におさまっていない場合)
					}
					else {
						g.setColor(recData.getPeakColor(j));					// 通常時の色
					}
					// ピークの描画でピークの強度が高いためにグラフ内におさまりきらない場合は、
					// グラフ内に描画できる強度まで描画する。
					if (y >= tmpMarginTop) {
						g.fill3DRect(x, y, w, h, true);
					}
					else {
						g.fill3DRect(x, tmpMarginTop, w, baseY - tmpMarginTop, true);
					}
					
					
					// m/z値を描画
					g.setFont(font2);
					boolean isMzDraw = false;
					if ( isSelectedLine ) {
						g.setColor(Color.CYAN.darker());
						if ( isOnLine ) {
							g.setFont(font1);
						}
						isMzDraw = true;
					}
					else if ( isOnLine ) {
						g.setFont(font1);
						g.setColor(onCursorColor);
						isMzDraw = true;
					}
					else if ( mzDisp.isSelected() ) {
						g.setFont(font2);
						if (!isDragArea) {
							if (its > intensityRange * 0.4 ) {
								g.setColor(Color.RED);
							}
							else if (!chgColor.isSelected()){
								g.setColor(Color.BLACK);
							}
						}
						isMzDraw = true;
					}
					else if (mzMatchDisp.isSelected()) {
						if (recData.getPeakColorType(j) != PackageRecData.COLOR_TYPE_BLACK) {
							if (!chgColor.isSelected() && !isDragArea) {
								g.setFont(font2);
								g.setColor(recData.getPeakColor(j));
							}
							isMzDraw = true;
						}
					}
					else if (its > intensityRange * 0.4) {
						g.setFont(font2);
						if (!chgColor.isSelected()) {
							g.setColor(Color.BLACK);
						}
						isMzDraw = true;
					}
					
					// グラフ内に描画できる場合のみ描画
					if (isMzDraw && y >= tmpMarginTop) {
						g.drawString(formatMass(mz, false), x, y);
					}
					
					
					// ピークの強度表示
					if (isOnLine || isSelectedLine) {
						// 強度の描画位置がグラフ内(現在の最大強度以下)の場合
						if (y >= tmpMarginTop) {
							g.setColor(onCursorColor);
							if ( isSelectedLine ) {
								g.setColor(Color.CYAN.darker());
							}
							g.drawLine(
									baseX + 4, 
									y, 
									baseX - 4, 
									y);					// 強度目盛り
							g.setColor(Color.GRAY);
							g.setFont(font2);
							if (isOnLine) {
								g.setColor(Color.DARK_GRAY);
								g.setFont(font3);
							}
							g.drawString(String.valueOf(recData.getIntensity(j)),
									baseX + 5,
									y - 1);				// 強度
						}
					}
				}	// end for
				
				// プリカーサーm/zに三角マーク付け
				if ( recData != null
						&& !recData.getPrecursor().equals("") ) {
					
					double pre = Double.parseDouble(recData.getPrecursor());
					int preX = baseX + (int)((pre - massStart) * xscale) - (int)Math.floor(xscale / 8);

					// プリカーサーm/zがグラフ内の場合のみ描画
					if ( preX >= baseX 
							&& preX <= width - tmpMarginRight ) {
						
						// プリカーサー三角の描画角度をグラフの可動にあわせて変更
						int maxGraphWidth = width - (MARGIN * 2);
						int nowGraphWidth = width - tmpMarginRight - baseX;
						int maxMovePreX = 7;
						int movePreX = 0;
						for (int j=1; j<maxMovePreX; j++) {
							if ((maxGraphWidth / maxMovePreX * j) > nowGraphWidth) {
								movePreX = maxMovePreX - j;
								break;
							}
						}
						
						int[] xp = { preX, (preX + 6 - movePreX), (preX - 6 - movePreX) };
						int[] yp = { baseY, (baseY + 5), (baseY + 5) };
						g.setColor( Color.RED );
						g.fillPolygon( xp, yp, 3 );
					}
				}
			}	// end for
		}
		

		/**
		 * マウスプレスイベント
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			
			if (animationTimer != null && animationTimer.isRunning()) {
				return;
			}
			else if (moveTimer != null && moveTimer.isRunning()) {
				return;
			}
			
			if (recNum == 0) {
				return;
			}
			
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				
				// 一番手前のグラフの左端、右端におさまる範囲内でのイベントの場合
				if (e.getPoint().x >= prevGraphArea.get(LT).x
						&& e.getPoint().x <= prevGraphArea.get(RT).x) {
					
					isDrag = true;
				}
				else {
					isDrag = false;
				}
				
				// ドラッグイベント有効の場合
				if ( isDrag ) {
					if(animationTimer != null && animationTimer.isRunning()) {
						return;
					}
					fromPos = toPos = e.getPoint();
				}
			}
			
			// 右ボタンの場合
			if ( !SwingUtilities.isRightMouseButton(e) ) {
				// ソートメニュー表示時対応
				if (contextPopup != null && !contextPopup.isVisible()) {
					cursorPoint = e.getPoint();
					PackageViewPanel.this.repaint();
				}
				
			}
		}
		
		/**
		 * マウスドラッグイベント
		 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
		 */
		public void mouseDragged(MouseEvent e) {

			if (animationTimer != null && animationTimer.isRunning()) {
				return;
			}
			else if (moveTimer != null && moveTimer.isRunning()) {
				return;
			}
			
			if (recNum == 0) {
				return;
			}
			
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				
				// ドラッグイベント有効の場合
				if ( isDrag ) {

					cursorPoint = new Point();
					if(animationTimer != null && animationTimer.isRunning()) {
						return;
					}
					underDrag = true;
					toPos = e.getPoint();
					PackageViewPanel.this.repaint();
				}
			}
		}

		/**
		 * マウスリリースイベント
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			
			if (animationTimer != null && animationTimer.isRunning()) {
				return;
			}
			else if (moveTimer != null && moveTimer.isRunning()) {
				return;
			}
			
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				
				if (recNum == 0) {
					return;
				}
				
				// ドラッグイベント有効の場合
				if ( isDrag ) {
				
					if (!underDrag || (animationTimer != null && animationTimer.isRunning())) {
						return;
					}
					underDrag = false;
					if ((fromPos != null) && (toPos != null)) {
						
						if (Math.min(fromPos.x, toPos.x) < 0) {
							massStart = Math.max(0, massStart - massRange / 3);
						}
						else if (Math.max(fromPos.x, toPos.x) > getWidth()) {
							massStart = Math.min(massRangeMax - massRange, massStart + massRange / 3);
						}
						else {
							if (specData != null) {
								// マウスカーソル設定
								PackageViewPanel.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
								
								isInitRate = false;
								
								// スペクトル移動ボタンの使用を制限
								leftMostBtn.setEnabled(true);
								leftBtn.setEnabled(true);
								rightBtn.setEnabled(true);
								rightMostBtn.setEnabled(true);
								
								animationTimer = new Timer(30,
												  new AnimationTimer(Math.abs(fromPos.x - toPos.x),
												  Math.min(fromPos.x, toPos.x)));
								animationTimer.start();
							}
							else {
								fromPos = toPos = null;
								PackageViewPanel.this.repaint();
							}
						}
					}
					// 拡大処理後のマウスカーソルポジション表示
					cursorPoint = e.getPoint();
				}
			}
			// 右ボタンの場合
			else if ( SwingUtilities.isRightMouseButton(e) ) {
				
				if (!underDrag) {
					
					// ポップアップメニューインスタンス生成
					contextPopup = new JPopupMenu();
					
					JMenuItem item1 = null;
					item1 = new JMenuItem("Peak Search");
					item1.setActionCommand("search");
					item1.addActionListener(new ContextPopupListener());
					item1.setEnabled(false);
					contextPopup.add(item1);
					
					JMenuItem item2 = null;
					item2 = new JMenuItem("Select Reset");
					item2.setActionCommand("reset");
					item2.addActionListener(new ContextPopupListener());
					item2.setEnabled(false);
					contextPopup.add(item2);
					
					if (specData.getSelectedPeakNum() != 0) {
						item1.setEnabled(true);
						item2.setEnabled(true);
					}
				
					// ポップアップメニュー表示
					contextPopup.show( e.getComponent(), e.getX(), e.getY() );
				}
			}
		}

		/**
		 * マウスクリックイベント
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			
			if (animationTimer != null && animationTimer.isRunning()) {
				return;
			}
			else if (moveTimer != null && moveTimer.isRunning()) {
				return;
			}
			
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				
				if (recNum == 0) {
					return;
				}
				
				// クリック間隔算出
				long interSec = (e.getWhen() - lastClickedTime);
				lastClickedTime = e.getWhen();
				
				// ダブルクリックの場合（クリック間隔280ミリ秒以内）
				if(interSec <= 280){
					
					// 拡大処理
					fromPos = toPos = null;
					initRange(false);
					
					// スペクトル移動ボタンの使用を制限
					leftMostBtn.setEnabled(false);
					leftBtn.setEnabled(false);
					rightBtn.setEnabled(false);
					rightMostBtn.setEnabled(false);
				}
				// シングルクリックの場合（クリック間隔281ミリ秒以上）
				else {
					
					Point clickPoint = e.getPoint();			// クリックポイント
					
					int tmpMarginTop = 0;
					int tmpMarginRight = 0;
					
					int baseX = 0;			// x軸とy軸の交点のx座標
					int baseY = 0;			// x軸とy軸の交点のy座標

					PackageRecData recData = null;
					
					TreeSet<Double> tmpClickMzList = new TreeSet<Double>();
					
					for (int i=0; i<recNum; i++) {
						
						recData = specData.getRecInfo(i);
						
						baseX = MARGIN + (recNum-1-i) * moveXPoint;
						baseY = height - MARGIN - (recNum-1-i) * moveYPoint;
						
						tmpMarginTop = marginTop + (i * moveYPoint);
						tmpMarginRight = marginRight + (i * moveXPoint);
						if (tmpMarginTop > baseY) {
							tmpMarginTop = baseY;
						}
						if (width - tmpMarginRight < baseX) {
							tmpMarginRight = (width - baseX > 0) ? (width - baseX) : 0;
						}
						
						int step = stepCalc((int)massRange);
						int start = (step - (int)massStart % step) % step;
						
						// ピークがない場合
						if ( recData.getPeakNum() == 0 ) {
							continue;
						}
						
						int end, its, x, y, w;
						double mz;
						start = recData.getIndex(massStart);
						end = recData.getIndex(massStart + massRange);
						for (int j=start; j<end; j++) {
							mz = recData.getMz(j);
							its = recData.getIntensity(j);
							
							x = baseX + (int)((mz - massStart) * xscale) - (int)Math.floor(xscale / 8);
							y = baseY - (int)(its * yscale);
							w = (int)(xscale / 8);
							
							// 描画パラメータ（幅）調整
							if (w < 2) {
								w = 2;
							}
							else if (w < 3) {
								w = 3;
							}
							
							// y軸より左側には描画しないように調整
							if(baseX > x) {
								w = (w - (baseX - x) > 0) ? (w - (baseX - x)) : 1;
								x = baseX + 1;
							}
							
							// クリックした場所が（X/Y座標）がPeakの描画エリアに含まれている場合
							if (clickPoint.x >= x 
									&& clickPoint.x <= (x + w) 
									&& clickPoint.y >= y
									&& clickPoint.y <= baseY) {
								
								tmpClickMzList.add(mz);
							}
						}	// end for
					}	// end for
					
					
					// クリックポイントにPeakが1つの場合
					if (tmpClickMzList.size() == 1) {
						
						String mz = String.valueOf(tmpClickMzList.iterator().next());
						
						if (!specData.containsSelectedPeak(mz)) {
							if (specData.getSelectedPeakNum() < MassBankCommon.PEAK_SEARCH_PARAM_NUM) {
								// 選択済ピークの保持
								specData.addSelectedPeakList(mz);
								// 選択状態を設定
								for (int i=0; i<recNum; i++) {
									recData = specData.getRecInfo(i);
									recData.setSelectPeak(mz, true);
								}
							}
							else {
								JOptionPane.showMessageDialog(
										SpectrumPlotPane.this,
										" m/z of " + MassBankCommon.PEAK_SEARCH_PARAM_NUM + " peak or more cannot be selected.&nbsp;",
										"Warning",
										JOptionPane.WARNING_MESSAGE);
							}
						}
						else {
							// 選択済ピークの保持解除
							specData.removeSelectedPeakList(mz);
							// 選択状態を解除
							for (int i=0; i<recNum; i++) {
								recData = specData.getRecInfo(i);
								recData.setSelectPeak(mz, false);
							}
						}
						setStatusLabel();
						PackageViewPanel.this.repaint();
					}
					// クリックポイントにPeakが2つ以上の場合
					else if (tmpClickMzList.size() >= 2) {
						// ポップアップメニューインスタンス生成
						selectPopup = new JPopupMenu();
						JMenuItem item = null;
						
						Iterator<Double> ite = tmpClickMzList.iterator();
						while (ite.hasNext()) {
							String mz = String.valueOf(ite.next());
							item = new JMenuItem(mz);
							selectPopup.add(item);
							item.addActionListener(new SelectMZPopupListener(mz));
							
							if (specData.getSelectedPeakNum() >= MassBankCommon.PEAK_SEARCH_PARAM_NUM
									&& !specData.containsSelectedPeak(mz)) {
								// Peak選択数がMAXの場合、選択済みPeak以外は選択不可を設定
								item.setEnabled(false);
							}
						}
						
						// ポップアップメニュー表示
						selectPopup.show( e.getComponent(), e.getX(), e.getY() );
					}
				}
			}
		}
		
		/**
		 * マウスエンターイベント
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}
		
		/**
		 * マウスイグジットイベント
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}
		
		/**
		 * マウスムーブイベント
		 * @see java.awt.event.MouseMotionListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseMoved(MouseEvent e) {
			
			if (animationTimer != null && animationTimer.isRunning()) {
				return;
			}
			else if (moveTimer != null && moveTimer.isRunning()) {
				return;
			}
			
			if (recNum == 0) {
				return;
			}
			
			// ポップアップメニューが表示されている時はイベントを処理しない
			if ((contextPopup == null || (contextPopup != null && !contextPopup.isVisible()))
					&& (selectPopup == null || (selectPopup != null && !selectPopup.isVisible()))) {
				
				// マウスカーソルポイント
				cursorPoint = e.getPoint();
				PackageViewPanel.this.repaint();
			}
		}
		
		/**
		 * 拡大処理をアニメーション化するクラス
		 * PlotPaneのインナークラス
		 */
		class AnimationTimer implements ActionListener {
			private final int LOOP = 15;
			private int loopCoef;
			private int toX;
			private int fromX;
			private double tmpMassStart;
			private double tmpMassRange;
			private int tmpIntensityRange;
			private int movex;

			/**
			 * コンストラクタ
			 * @param from ドラッグ開始位置
			 * @param to ドラッグ終了位置
			 */
			public AnimationTimer(int from, int to) {
				loopCoef = 0;
				toX = to;
				fromX = from;
				movex = 0 + MARGIN;
				// 目的拡大率を算出
//				float xs = (getWidth() - 2.0f * MARGIN) / massRange;
				double xs = (getWidth() - (2.0d * MARGIN) - ((recNum-1) * moveXPoint)) / massRange;
				tmpMassStart = massStart + ((toX - MARGIN) / xs);
				
				tmpMassRange = 10 * (fromX / (10 * xs));
				if (tmpMassRange < MASS_RANGE_MIN) {
					tmpMassRange = MASS_RANGE_MIN;
				}

				// Intensityのレンジを設定
				if (massRange <= massRangeMax) {
					int maxIntensity = 0;
					double start = Math.max(tmpMassStart, 0.0d);
					// 全てのレコード内から強度の最大値を算出
					PackageRecData recData = null;
					for ( int i=0; i<recNum; i++ ) {
						recData = specData.getRecInfo(i);
						if (maxIntensity < recData.getMaxIntensity(start, start + tmpMassRange)) {
							maxIntensity = recData.getMaxIntensity(start, start + tmpMassRange);
						}
					}
					// 50単位に変換してスケールを決定
					tmpIntensityRange = (int)((1.0d + maxIntensity / 50.0d) * 50.0d);
					if(tmpIntensityRange > INTENSITY_RANGE_MAX) {
						tmpIntensityRange = INTENSITY_RANGE_MAX;
					}
				}
			}

			/**
			 * アクションイベント
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				int xpos = (movex + toX) / 2;
				if (Math.abs(massStart - tmpMassStart) <= 2
						&& Math.abs(massRange - tmpMassRange) <= 2
						&& Math.abs(intensityRange - tmpIntensityRange) <= 2) {
					
					xpos = toX;
					
					massStart = tmpMassStart;
					massRange = tmpMassRange;
					intensityRange = tmpIntensityRange;
					animationTimer.stop();
					// マウスカーソル設定
					PackageViewPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				else {
					
					loopCoef++;
					massStart = massStart
							+ (((tmpMassStart + massStart) / 2 - massStart)
									* loopCoef / LOOP);
					massRange = massRange
							+ (((tmpMassRange + massRange) / 2 - massRange)
									* loopCoef / LOOP);
					intensityRange = intensityRange
							+ ((tmpIntensityRange - intensityRange)
									* loopCoef / LOOP);

					if (loopCoef >= LOOP) {
						movex = xpos;
						loopCoef = 0;
					}
				}
				PackageViewPanel.this.repaint();
			}
		}
		
		/**
		 * ピーク選択ポップアップメニューリスナークラス
		 * PlotPaneのインナークラス
		 */
		class SelectMZPopupListener implements ActionListener {
			
			private String mz = "";			// m/z
			
			/**
			 * コンストラクタ
			 * @param mz m/z
			 */
			public SelectMZPopupListener(String mz) {
				this.mz = mz;
			}
			
			/**
			 * アクションイベント
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				
				PackageRecData recData = null;
				
				if (!specData.containsSelectedPeak(mz)) {
					// 選択済ピークの保持
					specData.addSelectedPeakList(mz);
					// 選択状態を設定
					for (int i=0; i<recNum; i++) {
						recData = specData.getRecInfo(i);
						recData.setSelectPeak(mz, true);
					}
				}
				else {
					// 選択済ピークの保持解除
					specData.removeSelectedPeakList(mz);
					// 選択状態を解除
					for (int i=0; i<recNum; i++) {
						recData = specData.getRecInfo(i);
						recData.setSelectPeak(mz, false);
					}
				}
				
				setStatusLabel();
				
				// PlotPaneカーソルポイント初期化
				cursorPoint = new Point();
				
				PackageViewPanel.this.repaint();
			}
		}
		
		/**
		 * コンテキストポップアップメニューリスナークラス
		 * PlotPaneのインナークラス
		 */
		class ContextPopupListener implements ActionListener {
			
			/**
			 * コンストラクタ
			 */
			public ContextPopupListener() {
			}

			/**
			 * アクションイベント
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {

				String com = e.getActionCommand();
				
				if (com.equals("search")) {
					// URLパラメータ生成
					StringBuffer urlParam = new StringBuffer();
					
					String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_PEAK];
					
					urlParam.append("?type=" + typeName);							// type：peak
					urlParam.append("&num=" + specData.getSelectedPeakNum());		// num ：
					urlParam.append("&tol=0");										// tol ：0
					urlParam.append("&int=5");										// int ：5
					
					int index = 0;
					Iterator<Double> ite = specData.getSelectedPeakList().iterator();
					while (ite.hasNext()) {
						if (index != 0) {
							urlParam.append("&op"+ index +"=and");					// op  ：and
						} else {
							urlParam.append("&op"+ index +"=or");					// op  ：or
						}
						urlParam.append("&mz"+ index +"=" + String.valueOf(ite.next()));	// mz  ：
						index++;
					}
					urlParam.append("&sortKey=name&sortAction=1&pageNo=1&exec=&inst=all");
					
					// JSP呼び出し
					String reqUrl = SearchPage.baseUrl + "jsp/Result.jsp" + urlParam.toString();
					try {
						SearchPage.context.showDocument(new URL(reqUrl), "_blank");
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				else if (com.equals("reset")) {
					specData.clearSelectedPeakList();
					specData.initAllSelectedPeak();
					setStatusLabel();
					PackageViewPanel.this.repaint();
				}
			}
		}
	}
	
	/**
	 * ボタンペイン
	 * PackageViewPanelのインナークラス
	 */
	class ButtonPane extends JPanel implements MouseListener, MouseMotionListener {

		private boolean isInMZ = false;					// show all m/zボタンカーソルインフラグ
		private boolean isInMZMatch = false;				// show match m/zボタンカーソルインフラグ
		private boolean isInChgColor = false;				// change colorボタンカーソルインフラグ
		private boolean isInFlat = false;					// flatボタンカーソルインフラグ
		private boolean isMZState = false;				// イベント前show all m/zボタン状態
		private boolean isMZMatchState = false;			// イベント前show match m/zボタン状態
		private boolean isChgColorState = false;			// イベント前change colorボタン状態
		private boolean isFlatState = false;				// イベント前flatボタン状態
		
		/**
		 * コンストラクタ
		 */
		public ButtonPane() {
			
			// レイアウト指定
			setLayout(new FlowLayout());
			
			leftMostBtn = new JButton("<<");
			leftMostBtn.setName("<<");
			leftMostBtn.addMouseListener(this);
			leftMostBtn.setMargin(new Insets(0, 0, 0, 0));
			
			leftBtn = new JButton(" < ");
			leftBtn.setName("<");
			leftBtn.addMouseListener(this);
			leftBtn.setMargin(new Insets(0, 0, 0, 0));

			rightBtn = new JButton(" > ");
			rightBtn.setName(">");
			rightBtn.addMouseListener(this);
			rightBtn.setMargin(new Insets(0, 0, 0, 0));

			rightMostBtn = new JButton(">>");
			rightMostBtn.setName(">>");
			rightMostBtn.addMouseListener(this);
			rightMostBtn.setMargin(new Insets(0, 0, 0, 0));

			if (!isInitRate) {
				leftMostBtn.setEnabled(true);
				leftBtn.setEnabled(true);
				rightBtn.setEnabled(true);
				rightMostBtn.setEnabled(true);
			}
			else {
				leftMostBtn.setEnabled(false);
				leftBtn.setEnabled(false);
				rightBtn.setEnabled(false);
				rightMostBtn.setEnabled(false);
			}
			
			mzDisp = new JToggleButton("show all m/z");
			mzDisp.setName("mz");
			mzDisp.addMouseListener(this);
			mzDisp.addMouseMotionListener(this);
			mzDisp.setMargin(new Insets(0, 0, 0, 0));
			mzDisp.setSelected(false);

			mzMatchDisp = new JToggleButton("show match m/z");
			mzMatchDisp.setName("match");
			mzMatchDisp.addMouseListener(this);
			mzMatchDisp.addMouseMotionListener(this);
			mzMatchDisp.setMargin(new Insets(0, 0, 0, 0));
			mzMatchDisp.setSelected(false);
			
			chgColor = new JToggleButton("change color");
			chgColor.setName("chgColor");
			chgColor.addMouseListener(this);
			chgColor.addMouseMotionListener(this);
			chgColor.setMargin(new Insets(0, 0, 0, 0));
			chgColor.setSelected(false);
			
			// レイアウト調整用ブランクラベル
			JLabel blankLabel1 = new JLabel("   ");
			
			xAxisUp = new JButton("←");
			xAxisUp.setName("xup");
			xAxisUp.addMouseListener(this);
			xAxisUp.addMouseMotionListener(this);
			xAxisUp.setMargin(new Insets(0, 0, 0, 0));
			
			xAxisDown = new JButton("→");
			xAxisDown.setName("xdown");
			xAxisDown.addMouseListener(this);
			xAxisDown.addMouseMotionListener(this);
			xAxisDown.setMargin(new Insets(0, 0, 0, 0));
			
			yAxisUp = new JButton("↑");
			yAxisUp.setName("yup");
			yAxisUp.addMouseListener(this);
			yAxisUp.addMouseMotionListener(this);
			yAxisUp.setMargin(new Insets(0, 0, 0, 0));
			
			yAxisDown = new JButton("↓");
			yAxisDown.setName("ydown");
			yAxisDown.addMouseListener(this);
			yAxisDown.addMouseMotionListener(this);
			yAxisDown.setMargin(new Insets(0, 0, 0, 0));
			
			topAngleBtn = new JButton("top angle");
			topAngleBtn.setName("top");
			topAngleBtn.addMouseListener(this);
			topAngleBtn.setMargin(new Insets(0,0,0,0));
			
			sideAngleBtn = new JButton("side angle");
			sideAngleBtn.setName("side");
			sideAngleBtn.addMouseListener(this);
			sideAngleBtn.setMargin(new Insets(0,0,0,0));
			
			flat = new JToggleButton("flat");
			flat.setName("flat");
			flat.addMouseListener(this);
			flat.addMouseMotionListener(this);
			flat.setMargin(new Insets(0, 0, 0, 0));
			flat.setSelected(false);
			
			add(leftMostBtn);
			add(leftBtn);
			add(rightBtn);
			add(rightMostBtn);
			add(mzDisp);
			add(mzMatchDisp);
			add(chgColor);
			add(blankLabel1);
			add(xAxisUp);
			add(xAxisDown);
			add(yAxisUp);
			add(yAxisDown);
			add(topAngleBtn);
			add(sideAngleBtn);
			add(flat);
		}
		
		
		/**
		 * マウスクリックイベント
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			
			String btnName = e.getComponent().getName();
			
			if (!e.getComponent().isEnabled()) {
				return;
			}
			else if (animationTimer != null && animationTimer.isRunning()) {
				return;
			}
			
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				
				if (btnName.equals("<<")) {
					massStart = Math.max(0, massStart - massRange);
					PackageViewPanel.this.repaint();
				}
				else if (btnName.equals("<")) {
					massStart = Math.max(0, massStart - massRange / 4);
					PackageViewPanel.this.repaint();
				}
				else if (btnName.equals(">")) {
					massStart = Math.min(massRangeMax - massRange, massStart + massRange / 4);
					PackageViewPanel.this.repaint();
				}
				else if (btnName.equals(">>")) {
					massStart = Math.min(massRangeMax - massRange, massStart + massRange);
					PackageViewPanel.this.repaint();
				}
				else if (btnName.equals("top")) {
					if (moveXPoint != minMoveXPoint || moveYPoint != maxMoveYPoint) {
						allBtnCtrl(false);
						// マウスカーソル設定
						PackageViewPanel.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						moveTimer = new Timer(10,
								  new MoveAnimationTimer(btnName));
						moveTimer.start();
					}
				}
				else if (btnName.equals("side")) {
					if (moveXPoint != minMoveXPoint || moveYPoint != minMoveYPoint) {
						allBtnCtrl(false);
						// マウスカーソル設定
						PackageViewPanel.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						moveTimer = new Timer(10,
								  new MoveAnimationTimer(btnName));
						moveTimer.start();
					}
				}
			}
		}

		/**
		 * マウスプレスイベント
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			
			String btnName = e.getComponent().getName();
			
			if (!e.getComponent().isEnabled()) {
				return;
			}
			else if (animationTimer != null && animationTimer.isRunning()) {
				// ボタン押下前状態を保持
				isMZState = mzDisp.isSelected();
				isMZMatchState = mzMatchDisp.isSelected();
				isChgColorState = chgColor.isSelected();
				isFlatState = flat.isSelected();
				return;
			}
			
			// PlotPaneカーソルポイント初期化
			cursorPoint = new Point();
			
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				
				int time = 10;
				
				// コントロールキーが同時に押下されている場合は、動作を遅くする
				if (e.isControlDown()) {
					time = 70;
				}
				
				if (btnName.equals("xup")
						|| btnName.equals("xdown")
						|| btnName.equals("yup")
						|| btnName.equals("ydown")) {
					
					moveTimer = new Timer(time,
							  new MoveAnimationTimer(btnName));
					moveTimer.start();
				}
			}
		}

		/**
		 * マウスリリースイベント
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			
			String btnName = e.getComponent().getName();
			
			if (!e.getComponent().isEnabled()) {
				return;
			}
			else if (animationTimer != null && animationTimer.isRunning()) {
				// ボタン押下前状態に戻す
				if (btnName.equals("mz")) {
					mzDisp.setSelected(isMZState);
				}
				else if (btnName.equals("match")) {
					mzMatchDisp.setSelected(isMZMatchState);
				}
				else if (btnName.equals("chgColor")) {
					chgColor.setSelected(isChgColorState);
				}
				else if (btnName.equals("flat")) {
					flat.setSelected(isFlatState);
				}
				return;
			}
			
			
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				
				if (btnName.equals("mz")) {
					if (isInMZ) {
						mzMatchDisp.setSelected(false);
						PackageViewPanel.this.repaint();
					}
				}
				else if (btnName.equals("match")) {
					if (isInMZMatch) {
						mzDisp.setSelected(false);
						PackageViewPanel.this.repaint();
					}
				}
				else if (btnName.equals("chgColor")) {
					if (isInChgColor) {
						PackageViewPanel.this.repaint();
					}
				}
				else if (btnName.equals("flat")) {
					if (isInFlat) {
						
						// flatボタン状態取得
						if (flat.isSelected()) {
							if (tmpMoveXPoint == -1 && tmpMoveYPoint == -1) {
								// 現在のx軸、y軸可動値を退避
								tmpMoveXPoint = moveXPoint;
								tmpMoveYPoint = moveYPoint;
							}
						}
						
						allBtnCtrl(false);
						// マウスカーソル設定
						PackageViewPanel.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						moveTimer = new Timer(10,
								  new MoveAnimationTimer(btnName));
						moveTimer.start();
					}
				}
				else if (btnName.equals("xup")
						|| btnName.equals("xdown")
						|| btnName.equals("yup")
						|| btnName.equals("ydown")) {
					
					if (moveTimer != null && moveTimer.isRunning()) {
						moveTimer.stop();
					}
				}
			}
		}

		/**
		 * マウスエンターイベント
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
			
			String btnName = e.getComponent().getName();
			
			if (!e.getComponent().isEnabled()) {
				return;
			}
			else if (animationTimer != null && animationTimer.isRunning()) {
				return;
			}
			
			if (btnName.equals("mz")) {
				isInMZ = true;
			}
			else if (btnName.equals("match")) {
				isInMZMatch = true;
			}
			else if (btnName.equals("chgColor")) {
				isInChgColor = true;
			}
			else if (btnName.equals("flat")) {
				isInFlat = true;
			}
		}

		/**
		 * マウスイグジットイベント
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			
			String btnName = e.getComponent().getName();
			
			if (!e.getComponent().isEnabled()) {
				return;
			}
			else if (animationTimer != null && animationTimer.isRunning()) {
				return;
			}
			
			if (btnName.equals("mz")) {
				isInMZ = false;
			}
			else if (btnName.equals("match")) {
				isInMZMatch = false;
			}
			else if (btnName.equals("chgColor")) {
				isInChgColor = false;
			}
			else if (btnName.equals("flat")) {
				isInFlat = false;
			}
		}

		/**
		 * マウスドラッグイベント
		 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
		 */
		public void mouseDragged(MouseEvent e) {
		}

		/**
		 * マウスムーブイベント
		 * @see java.awt.event.MouseMotionListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseMoved(MouseEvent e) {
		}
		
		
		/**
		 * グラフ可動をアニメーション化するクラス
		 * FunctionPain2のインナークラス
		 */
		class MoveAnimationTimer implements ActionListener {

			private String btnName = "";				// ボタン名
			
			private int moveXNum = 1;					// x軸の一度に稼動する値
			private int moveYNum = 1;					// y軸の一度に稼動する値
			
			/**
			 * コンストラクタ
			 * @param btnName ボタン名
			 */
			public MoveAnimationTimer(String btnName) {
				this.btnName = btnName;
				moveXNum = (((int)maxMoveXPoint / 90) > 0) ? ((int)maxMoveXPoint / 90) : 1;
				moveYNum = (((int)maxMoveYPoint / 70) > 0) ? ((int)maxMoveYPoint / 70) : 1;
			}

			/**
			 * アクションイベント
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				
				if (btnName.equals("xup")) {
					if (moveXPoint < maxMoveXPoint) {
						moveXPoint += moveXNum;
						moveXPoint = (moveXPoint > maxMoveXPoint) ? maxMoveXPoint : moveXPoint;
					}
					else {
						moveTimer.stop();
					}
				}
				else if (btnName.equals("xdown")) {
					if (moveXPoint > minMoveXPoint) {
						moveXPoint -= moveXNum;
						moveXPoint = (moveXPoint < minMoveXPoint) ? minMoveXPoint : moveXPoint;
					}
					else {
						moveTimer.stop();
					}
				}
				else if (btnName.equals("yup")) {
					if (moveYPoint < maxMoveYPoint) {
						moveYPoint += moveYNum;
						moveYPoint = (moveYPoint > maxMoveYPoint) ? maxMoveYPoint : moveYPoint;
					}
					else {
						moveTimer.stop();
					}
				}
				else if (btnName.equals("ydown")) {
					if (moveYPoint > minMoveYPoint) {
						moveYPoint -= moveYNum;
						moveYPoint = (moveYPoint < minMoveYPoint) ? minMoveYPoint : moveYPoint;
					}
					else {
						moveTimer.stop();
					}
				}
				else if (btnName.equals("top")) {
					
					boolean isMovedX = false;
					boolean isMovedY = false;
					if (moveXPoint > minMoveXPoint) {
						moveXPoint -= moveXNum;
						moveXPoint = (moveXPoint < minMoveXPoint) ? minMoveXPoint : moveXPoint;
					}
					else {
						isMovedX = true;
					}
					
					if (moveYPoint < maxMoveYPoint) {
						moveYPoint += moveYNum;
						moveYPoint = (moveYPoint > maxMoveYPoint) ? maxMoveYPoint : moveYPoint;
					}
					else {
						isMovedY = true;
					}
					
					if (isMovedX && isMovedY) {
						moveTimer.stop();
						// マウスカーソル設定
						PackageViewPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						allBtnCtrl(true);
					}
				}
				else if (btnName.equals("side")) {
					
					boolean isMovedX = false;
					boolean isMovedY = false;
					if (moveXPoint > minMoveXPoint) {
						moveXPoint -= moveXNum;
						moveXPoint = (moveXPoint < minMoveXPoint) ? minMoveXPoint : moveXPoint;
					}
					else {
						isMovedX = true;
					}
					
					if (moveYPoint > minMoveYPoint) {
						moveYPoint -= moveYNum;
						moveYPoint = (moveYPoint < minMoveYPoint) ? minMoveYPoint : moveYPoint;
					}
					else {
						isMovedY = true;
					}
					
					if (isMovedX && isMovedY) {
						moveTimer.stop();
						// マウスカーソル設定
						PackageViewPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						allBtnCtrl(true);
					}
				}
				else if (btnName.equals("flat")) {
					
					boolean isMovedX = false;
					boolean isMovedY = false;
					
					if (flat.isSelected()) {
						
						if (moveXPoint > 0) {
							moveXPoint -= moveXNum;
							moveXPoint = (moveXPoint < 0) ? 0 : moveXPoint;
						}
						else {
							isMovedX = true;
						}
						
						if (moveYPoint > 0) {
							moveYPoint -= moveYNum;
							moveYPoint = (moveYPoint < 0) ? 0 : moveYPoint;
						}
						else {
							isMovedY = true;
						}
						
						if (isMovedX && isMovedY) {
							moveTimer.stop();
							// マウスカーソル設定
							PackageViewPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
							allBtnCtrl(true);
							
							// 各グラフ可動ボタンの制御
							xAxisDown.setEnabled(false);
							xAxisUp.setEnabled(false);
							yAxisDown.setEnabled(false);
							yAxisUp.setEnabled(false);
							topAngleBtn.setEnabled(false);
							sideAngleBtn.setEnabled(false);
						}
					}
					else {
						
						if (moveXPoint < tmpMoveXPoint) {
							moveXPoint += moveXNum;
							moveXPoint = (moveXPoint > tmpMoveXPoint) ? tmpMoveXPoint : moveXPoint;
						}
						else {
							isMovedX = true;
						}
						
						if (moveYPoint < tmpMoveYPoint) {
							moveYPoint += moveYNum;
							moveYPoint = (moveYPoint > tmpMoveYPoint) ? tmpMoveYPoint : moveYPoint;
						}
						else {
							isMovedY = true;
						}
						
						if (isMovedX && isMovedY) {
							moveTimer.stop();
							// マウスカーソル設定
							PackageViewPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
							allBtnCtrl(true);
							
							tmpMoveXPoint = -1;
							tmpMoveYPoint = -1;
						}
					}
				}
				PackageViewPanel.this.repaint();
			}
		}
	}

	/**
	 * テーブルリストセレクトリスナークラス
	 * PackageViewPanelのインナークラス
	 */
	class LmSelectionListener implements ListSelectionListener {

		/**
		 * バリューチェンジイベント
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			
			// 選択行を可視にするためスクロールバー位置設定
			int selRow = recTable.getSelectedRow();
			int selCol = recTable.getSelectedColumn();
			Rectangle cellRect = recTable.getCellRect(selRow, selCol, false	);
			if(cellRect != null) {
				recTable.scrollRectToVisible( cellRect );
			}
			
			PackageViewPanel.this.repaint();
		}
	}
	
	/**
	 * テーブルマウスリスナークラス
	 * PackageViewPanelのインナークラス
	 */
	class TblMouseListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			super.mouseClicked(e);
			
			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			
			Point p = e.getPoint();
			int selRow = recTable.rowAtPoint(p);
			int selCol = recTable.columnAtPoint(p);
			
			if (selRow == -1 || selCol == -1) {
				return;
			}
			
			// チェックボックスの選択処理
			int disableCol = recTable.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_DISABLE);
			if (selCol == disableCol) {
				recTable.setValueAt(
						!Boolean.parseBoolean(String.valueOf(recTable.getValueAt(selRow, disableCol))),
						selRow, 
						disableCol);
				return;
			}
			
			// レコードページ表示処理
			if (e.getClickCount() == 2) {
				int siteCol = recTable.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_CONTRIBUTOR);
				if (String.valueOf(recTable.getValueAt(selRow, siteCol)).equals("")) {
					return;
				}
				
				showRecordPage(selRow);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			
			if (SwingUtilities.isRightMouseButton(e)) {
				isDragCancel = true;
				dragRowIndex = -1;
				PackageViewPanel.this.setCursor(Cursor.getDefaultCursor());
				recTable.repaint();
				return;
			}
			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			
			isDragCancel = false;
			
			Point p = e.getPoint();
			pressRowIndex = recTable.rowAtPoint(p);
			PackageViewPanel.this.repaint();
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			
			// 右リリースの場合
			if (SwingUtilities.isRightMouseButton(e)) {
				recListPopup(e);
			}
			
			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			if (isDragCancel) {
				return;
			}
			
			dragRowIndex = -1;
			
			Point p = e.getPoint();
		    releaseRowIndex = recTable.rowAtPoint(p);
			
			if (!isSortStatus() && pressRowIndex != releaseRowIndex && releaseRowIndex != -1) {
				// レコードドロップ(移動)
				DefaultTableModel dataModel = (DefaultTableModel)recSorter.getTableModel();
				dataModel.moveRow(pressRowIndex, pressRowIndex, releaseRowIndex);
				recTable.setRowSelectionInterval(releaseRowIndex, releaseRowIndex);
				
				// データクラスソート
				specData.sortRecInfo(recTable);
			}
			recTable.repaint();
			setCursor(Cursor.getDefaultCursor());
		}
	}
	
	/**
	 * テーブルマウスモーションリスナークラス
	 * PackageViewPanelのインナークラス
	 */
	class TblMouseMotionListener extends MouseMotionAdapter {
		
		@Override
		public void mouseDragged(MouseEvent e) {
			super.mouseDragged(e);
			
			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			if (isDragCancel) {
				recTable.setRowSelectionInterval(pressRowIndex, pressRowIndex);
				return;
			}
			
			Point p = e.getPoint();
			dragRowIndex = recTable.rowAtPoint(p);
			
			// カーソル変更
			Cursor cursor = Cursor.getDefaultCursor();
			if ( !isSortStatus()  && pressRowIndex != dragRowIndex) {
				try {
					cursor = Cursor.getSystemCustomCursor("MoveDrop.32x32");
				} catch (Exception ex) {
					// ドロップ用システムカーソルが存在しない場合、カーソル変更なし
				}
			}
			else {
				try {
					cursor = Cursor.getSystemCustomCursor("MoveNoDrop.32x32");
				} catch (Exception ex) {
					// ドロップ不可用システムカーソルが存在しない場合、カーソル変更なし
				}				
			}
			PackageViewPanel.this.setCursor(cursor);
			
			// ドラッグ中は選択行を移動しない
			recTable.setRowSelectionInterval(pressRowIndex, pressRowIndex);
			recTable.repaint();
		}
	}
	
	/**
	 * テーブルキーリスナークラス
	 * PackageViewPanelのインナークラス
	 */
	class TblKeyListener extends KeyAdapter {
		
		@Override
		public void keyReleased(KeyEvent e) {
			super.keyReleased(e);
			
			// ESCキープレスでドラッグイベント解除
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				isDragCancel = true;
				dragRowIndex = -1;
				PackageViewPanel.this.setCursor(Cursor.getDefaultCursor());
				recTable.repaint();
			}
		}
	}
	
	/**
	 * テーブルレンダラー
	 * PackageViewPanelのインナークラス
	 * オリジナルレンダラー。
	 */
	class TblRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if(isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			}else{
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			
			// 非表示レコードの文字色変更
			int disableCol = recTable.getColumnModel().getColumnIndex(SearchPage.COL_LABEL_DISABLE);
			if (Boolean.parseBoolean(String.valueOf(recTable.getValueAt(row, disableCol)))) {
				if (isSelected) {
					setForeground(Color.GRAY);
				}
				else {
					setForeground(Color.LIGHT_GRAY);	
				}
			}
			
			if (value instanceof Boolean) {
				if (column == disableCol) {
					// チェックボックス返却
					JCheckBox obj = new JCheckBox(null, null, ((Boolean)table
							.getValueAt(row, column)).booleanValue());
					if (isSelected) {
						obj.setBackground(table.getSelectionBackground());
					} else {
						obj.setBackground(table.getBackground());
					}
					obj.setHorizontalAlignment(CENTER);
					return obj;
				}
			}
			return this;
		}
	}
	
	/**
	 * ペインマウスリスナー
	 * PackageViewPanelのインナークラス
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
}
