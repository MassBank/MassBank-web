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
 * PackageView クラス
 *
 * ver 1.0.8 2011.08.10
 *
 ******************************************************************************/

import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import massbank.GetConfig;

/**
 * PackageView クラス
 */
@SuppressWarnings("serial")
public class PackageView extends JApplet {

	public static String baseUrl = "";

	public static final int MAX_DISPLAY_NUM = 20;		// 最大表示可能件数
	
	private PackageViewPanel pkgView = null;			// PackageViewPanelコンポーネント

	public static AppletContext context = null;		// アプレットコンテキスト
	public static int initAppletWidth = 0;			// アプレット初期画面サイズ(幅)
	public static int initAppletHight = 0;			// アプレット初期画面サイズ(高さ)
	
	private int seaqCompound = 0;						// 化合物名自動生成用
	private int seaqId = 0;							// ID自動生成用
	
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
		baseUrl = conf.getServerUrl();
		
		
		// ツールチップマネージャー設定
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setInitialDelay(50);
		ttm.setDismissDelay(8000);
		
		
		setLayout(new BorderLayout());
		
		// メインパネル
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		Border border = BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(),	new EmptyBorder(0, 5, 0, 5));
		mainPanel.setBorder(border);
		
		// PackageView生成及び、初期化
		pkgView = new PackageViewPanel();
		pkgView.initAllRecInfo();

		mainPanel.add(new HeaderPane(), BorderLayout.NORTH);
		mainPanel.add(pkgView, BorderLayout.CENTER);
		mainPanel.add(new FooterPane(), BorderLayout.SOUTH);
		
		add(mainPanel);
		
		
		// ユーザーファイル読込み
		if (getParameter("file") != null) {
			loadFile(getParameter("file"));
		}
	}
	
	/**
	 * ファイル読み込み処理
	 * @param fileName ファイル名
	 */
	private void loadFile(String fileName) {
		seaqCompound = 0;
		seaqId = 0;
		String reqUrl = baseUrl + "jsp/PackageView.jsp?file=" + fileName;
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
		
		// PackageViewレコード情報生成
		String line = "";
		String peaksLine = "";
		PackageRecData recData = null;
		int recNum = 0;
		try {
			for (int i=0; i<lineList.size(); i++) {
				
				line = lineList.get(i);
				
				// コメント行読み飛ばし
				if (line.trim().startsWith("//")) {
					continue;
				}
				
				// レコード情報取得処理
				if (line.trim().indexOf(":") == -1 && line.trim().length() != 0) {
					if (recData == null) {
						recData = new PackageRecData();
					}
					if (line.lastIndexOf(";") != -1) {
						peaksLine += line.trim();						
					}
					else {
						peaksLine += line.trim() + ";";
					}
				}
				else if (line.trim().startsWith("Name:")) {
					if (recData == null) {
						recData = new PackageRecData();
					}
					recData.setName(line.substring(5).trim());
				}
				else if (line.trim().startsWith("Num Peaks:")) {
					// Num Peaks:は無視
				}
				else if (line.trim().startsWith("Precursor:")) {
					if (recData == null) {
						recData = new PackageRecData();
					}
					recData.setPrecursor(line.substring(10).trim());
				}
				else if (line.trim().startsWith("ID:")) {
					if (recData == null) {
						recData = new PackageRecData();
					}
					recData.setId(line.substring(3).trim());
				}
				
				
				// レコード情報追加処理
				if (line.trim().length() == 0 || i == lineList.size()-1) {
					
					if (recData != null) {
						
						recNum++;
						if (recNum > MAX_DISPLAY_NUM) {
							// WARNING：最大20スペクトルまでの表示
							JOptionPane.showMessageDialog(
									null,
									"Display of up to " + MAX_DISPLAY_NUM + " spectra.",
									"Warning",
									JOptionPane.WARNING_MESSAGE);
							break;
						}
						
						// == クエリーレコードフラグ ===
						recData.setQueryRecord(false);
						
						// === ID ===
						if (recData.getId().equals("")) {
							recData.setId(createId());
						}
						
						// === 化合物名 ===
						if (recData.getName().equals("")) {
							recData.setName(createName());
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
							
							// === ピーク数 ===
							int num = peakList.size();
							if (num == 1) {
								if (peakList.get(0).split("\t")[0].equals("0") && peakList.get(0).split("\t")[1].equals("0")) {
									num = 0;
								}
							}
							recData.setPeakNum( num );
							
							for (int j=0; j<recData.getPeakNum(); j++ ) {
								// === m/z ===
								recData.setMz( j, peakList.get(j).split("\t")[0] );
								
								// === 強度 ===
								recData.setIntensity(j, peakList.get(j).split("\t")[1] );
							}
						}
						
						// === ピーク色 ===
						recData.setPeakColorType(PackageRecData.COLOR_TYPE_BLACK);
						
						// レコード情報追加
						pkgView.addRecInfo(recData);
					}
					
					recData = null;
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
		
		// レコード情報追加後処理
		pkgView.addRecInfoAfter(PackageSpecData.SORT_KEY_NAME);
	}

	/**
	 * ID生成
	 * IDを自動生成し返却する
	 * @return 化合物名
	 */
	private String createId() {
		String tmpId = "US";
		
		synchronized (this) {
			if (seaqId <= MAX_DISPLAY_NUM) {
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
			if (seaqCompound <= MAX_DISPLAY_NUM) {
				seaqCompound++;
			} else {
				seaqCompound = 0;
			}
		}
		DecimalFormat df = new DecimalFormat("00");
		return tmpName + df.format(seaqCompound);
	}
	
	/**
	 * ヘッダーペイン
	 * PackageViewのインナークラス
	 */
	class HeaderPane extends JPanel {
		
		/**
		 * コンストラクタ
		 */
		public HeaderPane() {

			JLabel title = new JLabel();
			title.setText(" Spectral Browser    ver. 1.07 ");
			title.setPreferredSize(new Dimension(0, 18));
			
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc;
			gbc = new GridBagConstraints();						// レイアウト制約初期化
			gbc.fill = GridBagConstraints.HORIZONTAL;			// 水平サイズの変更を許可
			gbc.weightx = 1;									// 余分の水平スペースを分配
			gbc.weighty = 0;									// 余分の垂直スペースを分配しない
			gbc.gridwidth = GridBagConstraints.REMAINDER;		// 列最後のコンポーネントに指定
			gbc.insets = new Insets(5, 0, 5, 0);
			gbl.setConstraints(title, gbc);
			
			setLayout(gbl);
			add(title);
		}
	}
	
	/**
	 * フッターペイン
	 * PackageViewのインナークラス
	 */
	class FooterPane extends JPanel {
		
		/**
		 * コンストラクタ
		 */
		public FooterPane() {
			
			JLabel copyRighit = new JLabel();
			copyRighit.setText("Copyright (C) 2006 MassBank Project");
			copyRighit.setPreferredSize(new Dimension(0, 16));
			copyRighit.setHorizontalAlignment(SwingConstants.RIGHT);
			
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc;
			gbc = new GridBagConstraints();						// レイアウト制約初期化
			gbc.fill = GridBagConstraints.HORIZONTAL;			// 水平サイズの変更を許可
			gbc.weightx = 1;									// 余分の水平スペースを分配
			gbc.weighty = 0;									// 余分の垂直スペースを分配しない
			gbc.gridwidth = GridBagConstraints.REMAINDER;		// 列最後のコンポーネントに指定
			gbc.insets = new Insets(2, 0, 3, 1);
			gbl.setConstraints(copyRighit, gbc);
			
			setLayout(gbl);
			add(copyRighit);
		}
	}
	
	/**
	 * ピークコンパレータ
	 * PackageViewのインナークラス。
	 * m/zの昇順ソートを行う。
	 */
	class PeakComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			String mz1 = String.valueOf(o1).split("\t")[0];
			String mz2 = String.valueOf(o2).split("\t")[0];
			return Float.valueOf(mz1).compareTo(Float.valueOf(mz2));
		}
	}
}
