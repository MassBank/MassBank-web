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
 * スペクトル表示アプレット
 *
 * ver 2.0.11 2009.12.10
 *
 ******************************************************************************/
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.BoxLayout;

import java.applet.AppletContext;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Vector;
import massbank.GetConfig;
import massbank.MassBankCommon;

/**
 * Displayクラス
 */
@SuppressWarnings("serial")
public class Display extends JApplet
{
	public static AppletContext context = null;
	public static String baseUrl = "";
	
	private PlotPane plotPane = null;
	private String[] diffMzs = null;
	
	/**
	 * 初期処理
	 */
	public void init() {
		
		// アプレットコンテキスト取得
		context = getAppletContext();
		
		// 環境設定ファイルからサーバURL取得
		String confPath = getCodeBase().toString();
		confPath = confPath.replaceAll( "jsp/", "" );
		GetConfig conf = new GetConfig(confPath);
		baseUrl = conf.getServerUrl();
		
		//---------------------------------------------
		// リクエスト種別をセットする
		//---------------------------------------------
		String reqType = "";
		if ( getParameter("type") != null ) {
			reqType = getParameter("type");
		}

		//---------------------------------------------
		// プレカーサ値をセットする
		//---------------------------------------------
		String precursor = "";
		if ( getParameter("precursor") != null ) {
			precursor = getParameter("precursor");
		}

		//---------------------------------------------
		// ピークデータを取得する
		//---------------------------------------------
		Vector<String> vecMzs = new Vector<String>();
		if ( getParameter("qpeak") != null ) {
			// Appletパラメータからピークデータを取得する
			String paramPeak = getParameter("qpeak");
			String line[] = paramPeak.split("@");
			for ( int i = 0; i < line.length; i++ ) {
				String item[] = line[i].split(",");
				vecMzs.add( item[0] + "\t" + item[1] );
			}
		}
		else {
			// DBからピークデータを取得する
			vecMzs = getPeaksFromDB();
		}

		//---------------------------------------------
		// ピークの色づけに必要な情報を取得する
		//---------------------------------------------
		ColorInfo colorInfo = getColorInfo(reqType);

		setLayout(new BorderLayout());
		//---------------------------------------------
		// スペクトル表示パネル追加
		//---------------------------------------------
		Peak peak = new Peak(vecMzs);
		this.plotPane = new PlotPane(reqType, peak, colorInfo, precursor);
		add(plotPane, BorderLayout.CENTER);

		//---------------------------------------------
		// ボタンパネル追加
		//---------------------------------------------
		ButtonPane pane = new ButtonPane();
		if ( reqType.equals("diff") ) {
			pane.addDiffButton();
		}
		add(pane, BorderLayout.SOUTH);
	}

	/**
	 * ピークの色づけに必要な情報を取得する
	 * @param reqType
	 */
	public ColorInfo getColorInfo(String reqType)
	{
		ColorInfo colorInfo = null;
		if ( reqType.equals("") ) {
			return null;
		}
		// Product Ion / Neutral Loss 検索の場合
		else if ( reqType.equals("product") || reqType.equals("nloss") ) {
			int num = Integer.parseInt(getParameter("num"));
			ArrayList<Double>[] hitPeaks1 = new ArrayList[1];
			ArrayList<Double>[] hitPeaks2 = new ArrayList[1];
			hitPeaks1[0] = new ArrayList<Double>();
			hitPeaks2[0] = new ArrayList<Double>();
			ArrayList<Double> mz1Ary = new ArrayList<Double>();
			ArrayList<Double> mz2Ary = new ArrayList<Double>();
			ArrayList<String> formulaAry = new ArrayList<String>();

			if ( reqType.equals("product") ) {
				for ( int i = 0; i < num; i++ ) {
					String line = getParameter( "ion" + String.valueOf(i+1) );
					String[] items = line.split(",");
					mz1Ary.add( Double.parseDouble(items[0]) );
					formulaAry.add( items[1] );
				}
			}
			else {
				for ( int i = 0; i < num; i++ ) {
					String line = getParameter( "nloss" + String.valueOf(i+1) );
					String[] items = line.split(",");
					mz1Ary.add( Double.parseDouble(items[0]) );
					mz2Ary.add( Double.parseDouble(items[1]) );
					formulaAry.add( items[2] );
				}
			}
			hitPeaks1[0].addAll(mz1Ary);
			hitPeaks2[0].addAll(mz2Ary);

			// 色づけ情報をセット
			colorInfo = new ColorInfo();
			colorInfo.setHitPeaks1(hitPeaks1);
			colorInfo.setHitPeaks2(hitPeaks2);
			colorInfo.setFormulas(formulaAry);
		}
		else {
			int diffMargin = 0;
			int num = Integer.parseInt(getParameter("num"));
			ArrayList<Double>[] hitPeaks1 = new ArrayList[num];
			ArrayList<Double>[] hitPeaks2 = new ArrayList[num];
			ArrayList<Integer>[] barColors = new ArrayList[num];

			ArrayList<Double> mz1Ary = null;
			ArrayList<Double> mz2Ary = null;
			ArrayList<Integer> colorAry = null;
			this.diffMzs = new String[num];
			for ( int i = 0; i < num; i++ ) {
				String line = getParameter( "mz" + String.valueOf(i+1) );
				String[] mzs = line.split("@");
				mz1Ary = new ArrayList<Double>();
				mz2Ary = new ArrayList<Double>();
				colorAry = new ArrayList<Integer>();

				//---------------------------------------------
				// Peak Difference Search の場合
				//---------------------------------------------
				if ( reqType.equals("diff") ) {
					diffMzs[i] = getParameter( "diff" + String.valueOf(i+1) );
					for ( int j = 0; j < mzs.length; j++ ) {
						String[] mzPair = mzs[j].split(",");
						mz1Ary.add( Double.parseDouble(mzPair[0]) );
						mz2Ary.add( Double.parseDouble(mzPair[1]) );
						colorAry.add( null );
					}
					diffMargin = Integer.parseInt( getParameter("margin") );
				}
				//---------------------------------------------
				// Peak Search の場合
				//---------------------------------------------
				else if ( reqType.equals("peak") || reqType.equals("qpeak") ) {
					mz1Ary = new ArrayList<Double>();
					for ( int j = 0; j < mzs.length; j++ ) {
						mz1Ary.add( Double.parseDouble(mzs[j]) );
						colorAry.add(0);
					}
				}

				// 色づけ情報を格納
				hitPeaks1[i] = new ArrayList<Double>();
				hitPeaks2[i] = new ArrayList<Double>();
				barColors[i] = new ArrayList<Integer>();
				hitPeaks1[i].addAll(mz1Ary);
				hitPeaks2[i].addAll(mz2Ary);
				barColors[i].addAll(colorAry);
			}

			// 色づけ情報をセット
			colorInfo = new ColorInfo();
			colorInfo.setHitPeaks1(hitPeaks1);
			colorInfo.setHitPeaks2(hitPeaks2);
			colorInfo.setBarColors(barColors);
			colorInfo.setDiffMargin(diffMargin);
			colorInfo.setDiffMzs(diffMzs);
		}
		return colorInfo;
	}

	/**
	 * DBからピークデータを取得する
	 * @return ピークデータ
	 */
	public Vector<String> getPeaksFromDB()
	{
		String jspUrl = baseUrl + "jsp/";
		String id = getParameter("id");
		String site = getParameter("site");
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GDATA];
		String reqStr = jspUrl + MassBankCommon.DISPATCHER_NAME + "?type=" + typeName + "&id=" + id + "&site=" + site;
		String line = "";
		String[] tmp = null;
		Vector<String> mzs = null;
		try {
			URL url = new URL( reqStr );
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			boolean isStartSpace = true;
			mzs = new Vector<String>();
			while (true) {
				line = in.readLine();
				// 先頭スペースを読み飛ばすため
				if ( line == null ) {
					break;
				}
				else if ( line.equals("") ) {
					if ( isStartSpace ) {
						continue;
					}
					else {
						break;
					}
				}
				else {
					isStartSpace = false;
				}
				tmp = line.split("\t");
				line = tmp[0] + "\t" + tmp[2];
				mzs.add(line);
			}
			in.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return mzs;
	}


	/**
	 * ボタンパネルクラス
	 */
	class ButtonPane extends JPanel implements ActionListener
	{
		JToggleButton mzDisp = null;
		private String comNameDiff = "show_diff";

		/**
		 * コンストラクタ
		 */
		ButtonPane()
		{
			JButton leftmostB = new JButton("<<");
			leftmostB.setActionCommand("<<");
			leftmostB.addActionListener(this);
			leftmostB.setMargin(new Insets(0, 0, 0, 0));

			JButton leftB = new JButton(" < ");
			leftB.setActionCommand("<");
			leftB.addActionListener(this);
			leftB.setMargin(new Insets(0, 0, 0, 0));

			JButton rightB = new JButton(" > ");
			rightB.setActionCommand(">");
			rightB.addActionListener(this);
			rightB.setMargin(new Insets(0, 0, 0, 0));

			JButton rightmostB = new JButton(">>");
			rightmostB.setActionCommand(">>");
			rightmostB.addActionListener(this);
			rightmostB.setMargin(new Insets(0, 0, 0, 0));

			mzDisp = new JToggleButton("show all m/z");
			mzDisp.setActionCommand("mz");
			mzDisp.addActionListener(this);
			mzDisp.setMargin(new Insets(0, 0, 0, 0));

			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			add(leftmostB);
			add(leftB);
			add(rightB);
			add(rightmostB);
			add(mzDisp);
		}

		public void actionPerformed(ActionEvent ae)
		{
			String com = ae.getActionCommand();
			double massStart = plotPane.getMassStart();
			double massRange = plotPane.getMassRange();
			double massMax = plotPane.getMassMax();
			if (com.equals("<<")) {
				plotPane.setMassStart( Math.max(0, massStart - massRange) );
			}
			else if (com.equals("<")) {
				plotPane.setMassStart( Math.max(0, massStart - massRange / 4) );
			}
			else if (com.equals(">")) {
				plotPane.setMassStart( Math.min(massMax - massRange, massStart + massRange / 4) );
			}
			else if (com.equals(">>")) {
				plotPane.setMassStart( Math.min(massMax - massRange, massStart + massRange) );
			}
			else if (com.equals("mz")) {
				plotPane.setIsMZFlag( mzDisp.isSelected() );
			}
			// Diffボタン押下時
			int pos = com.indexOf( comNameDiff );
			if ( pos >= 0 ) {
				int num = Integer.parseInt(com.substring(comNameDiff.length()));
				plotPane.setHitNum( num );
			}

			Display.this.repaint();
		}

		public void addDiffButton()
		{
			if ( diffMzs == null || diffMzs.length == 1 ) {
				return;
			}
			// Diffボタン表示
			for ( int i = 0; i < diffMzs.length; i++) {
				JButton diffbtn = new JButton();
				diffbtn = new JButton( "Diff." + diffMzs[i] );
				diffbtn.setActionCommand( comNameDiff + Integer.toString(i) );
				diffbtn.addActionListener(this);
				diffbtn.setMargin( new Insets(0, 0, 0, 0) );
				add(diffbtn);
			}
		}
	}
}
