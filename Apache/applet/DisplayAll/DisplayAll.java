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
 * スペクトル表示 クラス
 *
 * ver 2.0.5 2008.12.05
 *
 ******************************************************************************/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import massbank.GetConfig;
import massbank.MassBankCommon;

/**
 * スペクトル表示 クラス
 */
public class DisplayAll extends JApplet
{
	private static final long serialVersionUID = 1L;
	int MASS_MAX = 0;
	static int INTENSITY_MAX = 1000;
	static int MARGIN = 15;
	static int MIN_RANGE = 5;
	static int DEF_EX_PANE_SIZE = 150;

	float massStart = 0;
	float massRange = 0;
	int intensityRange = INTENSITY_MAX;
	boolean head2tail = false;
	boolean underDrag = false;
	Point fromPos = null;
	Point toPos = null;
	float xscale = 0;
	JSplitPane jsp_plt2ext = null;
	boolean isMZFlag = false;
	int numSpct;
	PlotPane[] plotPane;
	Peak[] peaks1 = null;
	private String baseUrl;
	private HitPeaks hitPeaks = new HitPeaks();
	private String reqType = "";
	private String[] precursor = null;
	private String searchParam = "";
	
	/**
	 * 
	 */
	class PlotPane extends JPanel implements MouseListener,
			MouseMotionListener
	{
		private static final long serialVersionUID = 1L;
		private Timer timer = null;
		private int idPeak;
		private long lastClickedTime = 0;						// 最後にクリックした時間

		/**
		 * 拡大処理をアニメーション化するクラス
		 */
		class AnimationTimer implements ActionListener
		{
			final int LOOP = 15;
			int loopCoef;
			int minx;
			int width;
			float tmpMassStart;
			float tmpMassRange;
			int tmpIntensityRange;
			int movex;

			/**
			 * 
			 */
			public AnimationTimer(int w, int x)
			{
				loopCoef = 0;
				minx = x;
				width = w;
				movex = 0 + MARGIN;
				// 目的拡大率を算出
				float xs = (getWidth() - 2.0f * MARGIN)
						/ massRange;
				tmpMassStart = massStart
						+ ((minx - MARGIN) / xs);
				tmpMassRange = 10 * (width / (10 * xs));
				if (tmpMassRange < MIN_RANGE) {
					tmpMassRange = MIN_RANGE;
				}

				// Intensityのレンジを設定
				if (massRange <= MASS_MAX)
				{
					// 最大値を検出。
					int max = 0;
					float start = Math.max(tmpMassStart, 0.0f);
					for (int i=0; i<peaks1.length; i++) {
						if (max < peaks1[i].getMaxIntensity(start, start + tmpMassRange)) {
							max = peaks1[i].getMaxIntensity(start, start + tmpMassRange);
						}
					}
					// 50単位に変換してスケールを決定
					tmpIntensityRange = (int)((1.0f + max / 50.0f) * 50.0f);
					if(tmpIntensityRange > INTENSITY_MAX)
						tmpIntensityRange = INTENSITY_MAX;
				}
			}

			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e)
			{
				xscale = (getWidth() - 2.0f * MARGIN) / massRange;
//				float yscale = (getHeight() - 2.0f * MARGIN) / intensityRange;
				int xpos = (movex + minx) / 2;
				if (Math.abs(massStart - tmpMassStart) <= 2
						&& Math.abs(massRange - tmpMassRange) <= 2)
				{
					xpos = minx;
					massStart = tmpMassStart;
					massRange = tmpMassRange;
					timer.stop();
					DisplayAll.this.repaint();
				} else {
					loopCoef++;
					massStart = massStart
							+ (((tmpMassStart + massStart) / 2 - massStart)
									* loopCoef / LOOP);
					massRange = massRange
							+ (((tmpMassRange + massRange) / 2 - massRange)
									* loopCoef / LOOP);
					intensityRange = intensityRange
							+ (((tmpIntensityRange + intensityRange) / 2 - intensityRange)
									* loopCoef / LOOP);
					if (loopCoef >= LOOP)
					{
						movex = xpos;
						loopCoef = 0;
					}
				}
				repaint();
			}
		}

		/**
		 * 
		 */
		public PlotPane(int id)
		{
			idPeak = id;
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		/**
		 * 
		 */
		int setStep(int range)
		{
			if (range < 20)
				return 2;
			if (range < 50)
				return 5;
			if (range < 100)
				return 10;
			if (range < 250)
				return 25;
			if (range < 500)
				return 50;
			return 100;
		}

		/* (非 Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			int width = getWidth();
			int height = getHeight();
			xscale = (width - 2.0f * MARGIN) / massRange;
			
			ArrayList<Float> mz1Ary = null;
			ArrayList<Float> mz2Ary = null;
			ArrayList<Integer> colorAry = null;

			// ピークバー色づけするカラーをセット
			int colorTblNum = 1;
			Color[] colorTbl = {
					new Color(0xD2,0x69,0x48), new Color(0x22,0x8B,0x22),
					new Color(0x41,0x69,0xE1), new Color(0xBD,0x00,0x8B),
					new Color(0x80,0x80,0x00), new Color(0x8B,0x45,0x13	),
					new Color(0x9A,0xCD,0x32)
			};

			// ヒットしたピークを取得
			if ( reqType.equals("peak") || reqType.equals("diff") ) {
				mz1Ary = hitPeaks.getMz1(idPeak);
				mz2Ary = hitPeaks.getMz2(idPeak);
				colorAry = hitPeaks.getBarColor(idPeak);
			}

			//上部余白のサイズをセット
			int marginTop = 0;
			if ( reqType.equals("diff") ) {
				marginTop = 70;
			}
			else {
				marginTop = MARGIN;
			}
			
			float yscale = (height - (float)(MARGIN +marginTop) ) / intensityRange;
			
			// 背景を白にする
			g.setColor(Color.white);
			g.fillRect(0, 0, width, height);

			g.setFont(g.getFont().deriveFont(9.0f));
			g.setColor(Color.lightGray);

			//========================================================
			// 目盛りを描画
			//========================================================
			g.drawLine(MARGIN, marginTop, MARGIN, height - MARGIN);
			g.drawLine(MARGIN, height - MARGIN, width - MARGIN, height - MARGIN);

			// x軸
			int step = setStep((int)massRange);
			int start = (step - (int)massStart % step) % step;
			for (int i = start; i < (int)massRange; i += step)
			{
				g.drawLine(MARGIN + (int) (i * xscale),
						height - MARGIN, MARGIN + (int) (i * xscale),
						height - MARGIN + 2);
				g.drawString(String.valueOf(i + massStart),
						MARGIN + (int) (i * xscale) - 5,
						height - 1);
			}

			// y軸
			for (int i = 0; i <= intensityRange; i += intensityRange / 5)
			{
				g.drawLine(MARGIN - 2, height - MARGIN - (int) (i * yscale),
							MARGIN,	height - MARGIN - (int) (i * yscale));
				g.drawString(String.valueOf(i),	0, height - MARGIN - (int) (i * yscale));
			}

			// ピークがない場合
			if ( peaks1[idPeak].mz[0] == 0 ) {
				g.setFont( new Font("Arial", Font.ITALIC, 24) );
				g.setColor( Color.LIGHT_GRAY );
				g.drawString( "No peak was observed.",	width/2-110, height / 2 );
				return;
			}

			float hitMz1Prev = 0;
			float hitMz2Prev = 0;
			int hitMz1Cnt = -1;
			int hitMz2Cnt = -1;
			
			//========================================================
			// ピークバーを描画
			//========================================================
			g.setColor(Color.black);
			int end, its, x, w;
			float peak;
			start = peaks1[idPeak].getIndex(massStart);
			end = peaks1[idPeak].getIndex(massStart + massRange);
			for (int i = start; i < end; i++) {
				peak = peaks1[idPeak].getMZ(i);
				its = peaks1[idPeak].getIntensity(i);
				x = MARGIN + (int) ((peak - massStart) * xscale) - (int) Math.floor(xscale / 8);
				w = (int) (xscale / 8);
				if(MARGIN > x){
					w = w - (MARGIN - x);
					x = MARGIN;
				}
				if ( w < 2 ) {
					w = 2;
				}
				
				// ピーク検索、ピーク差検索の場合、ヒットしたピークに色づけする
				boolean isHit = false;
				if ( reqType.equals("peak") || reqType.equals("diff") ) {
					int j = 0;
					float mz = 0;
					
					// ヒットピークmz1と一致しているか
					for ( j = 0; j < mz1Ary.size(); j++ ) {
						mz = mz1Ary.get(j);
						if ( peak == mz ) {
							isHit = true;
							if ( mz - hitMz1Prev >= 1 ) {
								hitMz1Cnt++;
							}
							
							if ( colorAry.get(j) != null ) {
								colorTblNum = colorAry.get(j);
							}
							else {
								colorTblNum = hitMz1Cnt - (hitMz1Cnt / colorTbl.length) * colorTbl.length;
								hitPeaks.setBarColor( idPeak, j, colorTblNum );
							}
							hitMz1Prev = mz;
							break;
						}
					}
					// mz1と不一致の場合、ヒットピークmz2と一致しているか
					if ( !isHit ) {
						for ( j = 0; j < mz2Ary.size(); j++ ) {
							mz = mz2Ary.get(j);
							if ( peak == mz ) {
								isHit = true;
								
								if ( colorAry.get(j) != null ) {
									colorTblNum = colorAry.get(j);
								}
								else if ( mz - hitMz2Prev >= 1 ) {
									colorTblNum = hitMz2Cnt - (hitMz2Cnt / colorTbl.length) * colorTbl.length;
									hitPeaks.setBarColor( idPeak, j, colorTblNum );
								}
								else {}
								hitMz2Cnt++;
								hitMz2Prev = mz;
								break;
							}
						}
					}
					// ヒットピークと一致している場合、描画色をセット
					if ( isHit ) {
						g.setColor( colorTbl[colorTblNum] );
					}
				}

				// ピークバーを描画
				g.fill3DRect(x,	height - MARGIN - (int) (its * yscale),
								w, (int)(its * yscale), true);
				
				// m/z値を描画
				if ( its > intensityRange * 0.4 || isMZFlag || isHit ) {
					if ( isMZFlag && its > intensityRange * 0.4 ) {
						g.setColor(Color.red);
					}
					else if ( isHit ) {
					}
					else {
						g.setColor(Color.black);
					}
					g.drawString(String.valueOf(peak),
								x, height - MARGIN - (int) (its * yscale));
				}
				g.setColor(Color.black);
			}
			
			//========================================================
			// ピーク差検索でヒットしたピークの位置を表示
			//========================================================
			if ( reqType.equals("diff") ) {
				String[] diffmzs = hitPeaks.getDiffMz(idPeak);
				String diffmz = diffmzs[ hitPeaks.getListNum()-1 ];
				int pos = diffmz.indexOf(".");
				if ( pos > 0 ) {
					BigDecimal bgMzZDiff = new BigDecimal( diffmz );
					diffmz = (bgMzZDiff.setScale(1, BigDecimal.ROUND_DOWN)).toString(); 
				}

				float mz1Prev = 0;
				int hitCnt = 0;
				for ( int j = 0; j < mz1Ary.size(); j++ ) {
					float mz1 = mz1Ary.get(j);
					float mz2 = mz2Ary.get(j);
					if ( mz1 - mz1Prev >= 1 ) {
						g.setColor(Color.GRAY);

						/* ピークバーのライン幅 */
						int barWidth = (int)Math.floor(xscale / 8);
						/* 横線左の開始位置 */
						int x1 = MARGIN + (int)((mz1 - massStart) * xscale) - barWidth/2;
						/* 横線右の開始位置 */
						int x2 = MARGIN + (int)((mz2 - massStart) * xscale) - barWidth/2;
						/* 横線右の開始位置 */
						int xc = x1 + (x2-x1) / 2 - 12;
						/* Ｙ座標 */
						int y = height - MARGIN - (int)( (1035 * yscale) + (++hitCnt * 12) );
						/* 文字幅 */
						int xm = (int)(diffmz.length() * 5)+4;

						int padding = 5;

						// 横線描画
						g.drawLine( x1,y , xc,y );
						g.drawLine( xc + xm + padding,y, x2,y );
						// 縦線描画
						g.drawLine( x1,y, x1,y+4 );
						g.drawLine( x2,y, x2,y+4 );

						// ピーク差の値を描画
						colorTblNum = colorAry.get(j);
						g.setColor( colorTbl[colorTblNum] );
						g.fillRect( xc, y - padding, (xc + xm + padding) - xc, padding*2 );
						g.setColor( Color.WHITE );
						g.drawString( diffmz, xc + padding , y+3 );
					}
					mz1Prev = mz1;
				}
			}

			// プレカーサーm/zにマーク付け
			if ( !precursor[idPeak].equals("") ) {
				int pre = Integer.parseInt(precursor[idPeak]);
				int xPre = MARGIN + (int)((pre - massStart) * xscale) - (int)Math.floor(xscale / 8);
				int yPre = height - MARGIN;
				
				// プリカーサーm/zがグラフ内の場合のみ描画
				if (xPre >= MARGIN && xPre <= width - MARGIN) {
					int [] xp = { xPre, xPre + 6, xPre - 6 };
					int [] yp = { yPre, yPre + 6, yPre + 6 };
					g.setColor( Color.RED );
					g.fillPolygon( xp, yp, xp.length );
				}
			}

			if (underDrag)
			{// マウスでドラッグした領域を黄色い線で囲む
				g.setXORMode(Color.white);
				g.setColor(Color.yellow);
				int xpos = Math.min(fromPos.x, toPos.x);
				width = Math.abs(fromPos.x - toPos.x);
				g.fillRect(xpos, 0, width, height - MARGIN);
				g.setPaintMode();
			}
		}

		/**
		 * 
		 */
		public void mousePressed(MouseEvent e)
		{
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {

				if(timer != null && timer.isRunning())
					return;
	
				fromPos = toPos = e.getPoint();
			}
		}

		/**
		 * 
		 */
		public void mouseDragged(MouseEvent e)
		{
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				if(timer != null && timer.isRunning())
					return;
	
				underDrag = true;
				toPos = e.getPoint();
				repaint();
			}
		}

		/**
		 * 
		 */
		public void mouseReleased(MouseEvent e)
		{
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				if (!underDrag || (timer != null && timer.isRunning()))
					return;
				underDrag = false;
				if ((fromPos != null) && (toPos != null)) {
					if (Math.min(fromPos.x, toPos.x) < 0)
						massStart = Math.max(0, massStart - massRange / 3);
	
					else if (Math.max(fromPos.x, toPos.x) > getWidth())
						massStart = Math.min(MASS_MAX - massRange, massStart + massRange / 3);
					else {
						if (peaks1 != null) {
							timer = new Timer(30,
									new AnimationTimer(Math.abs(fromPos.x - toPos.x),
											Math.min(fromPos.x, toPos.x)));
							timer.start();
						} else {
							fromPos = toPos = null;
							repaint();
						}
					}
				}
			}
		}

		/**
		 * 
		 */
		public void mouseClicked(MouseEvent e)
		{
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				
				// クリック間隔算出
				long interSec = (e.getWhen() - lastClickedTime);
				lastClickedTime = e.getWhen();
				
				// ダブルクリックの場合（クリック間隔280ミリ秒以内）
				if(interSec <= 280){
					
					// 拡大処理
					fromPos = toPos = null;
					initMass();
					repaint();
				}
			}
		}

		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseMoved(MouseEvent e) {}
	}

	/**
	 * 
	 */
	@SuppressWarnings("serial")
	class ButtonPane extends JPanel implements ActionListener
	{
		private String comNameDiff = "show_diff";
		
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

			JButton mzDisp = new JButton("show all m/z");
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

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent ae)
		{
			String com = ae.getActionCommand();
			if (com.equals("<<"))
				massStart = Math
						.max(0, massStart - massRange);
			else if (com.equals("<"))
				massStart = Math.max(0, massStart - massRange
						/ 4);
			else if (com.equals(">"))
				massStart = Math.min(MASS_MAX - massRange,
						massStart + massRange / 4);
			else if (com.equals(">>"))
				massStart = Math.min(MASS_MAX - massRange,
						massStart + massRange);
			else if (com.equals("mz"))
				isMZFlag = ! isMZFlag;

			// Diffボタン押下時
			int pos = com.indexOf( comNameDiff );
			if ( pos >= 0 ) {
				int num = Integer.parseInt(com.substring(comNameDiff.length()));
				hitPeaks.setListNum(num);
			}
			DisplayAll.this.repaint();
		}
		
		/**
		 * 
		 */
		public void addDiffButton(int idPeak)
		{
			// Diffボタン表示
			if ( reqType.equals("diff") ) {
				String[] diffmzs = hitPeaks.getDiffMz(idPeak);
				JButton[] diffbtn = new JButton[diffmzs.length];
				for ( int i = 0; i < diffmzs.length; i++) {
					diffbtn[i] = new JButton( "Diff." + diffmzs[i] );
					diffbtn[i].setActionCommand( comNameDiff + Integer.toString(i+1) );
					diffbtn[i].addActionListener(this);
					diffbtn[i].setMargin( new Insets(0, 0, 0, 0) );
					add(diffbtn[i]);
				}		
			}
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("serial")
	class NameButton extends JButton implements ActionListener {
		String acc;
		String site;

		public NameButton(String name, String id, String site) {
			super("<html><a href=\"\">" + name + "</a></html>");
			acc = id;
			this.site = site;
			this.addActionListener(this);
		}

		public void actionPerformed(ActionEvent ae) {
			try {
				String typeName = "";
				if ( reqType.equals("diff") ) {
					typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISPDIFF];
				}
				else {
					typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
				}
				String reqStr = baseUrl + MassBankCommon.DISPATCHER_NAME + "?type=" + typeName + "&id=" + acc + "&site=" + this.site;
				if ( reqType.equals("peak") || reqType.equals("diff") ) {
					reqStr += searchParam;
				}
				DisplayAll.this.getAppletContext().showDocument(new URL(reqStr), "_blank");

			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	public void init() {
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

		// 環境設定ファイルから連携サイトのURLを取得
		String confPath = getCodeBase().toString();
		confPath = confPath.replaceAll( "jsp/", "" );
		GetConfig conf = new GetConfig(confPath);
		String[] urlList = conf.getSiteUrl();
		String severUrl = conf.getServerUrl();
		baseUrl = severUrl + "jsp/";

		try {
			// ピーク検索、ピーク差検索時のパラメータ取得
			int paramMzNum = 0;
			String paramMz  = "";
			String paramTol = "";
			String paramInt = "";
			if ( getParameter("type") != null ) {
				reqType = getParameter("type");
				if ( reqType.equals("peak") || reqType.equals("diff") ) {
					paramMzNum = Integer.parseInt( getParameter("pnum") );

					searchParam = "&num=" + paramMzNum;
					for ( int i = 0; i < paramMzNum; i++ ) {
						String pnum = Integer.toString(i);
						String mz = getParameter( "mz" + pnum );
						String tol = getParameter( "tol" + pnum );
						String rInt = getParameter( "int" + pnum );
						paramMz  += mz  + ",";
						paramTol += tol + ",";
						paramInt += rInt + ",";
						searchParam += "&mz" + pnum + "=" + mz;
						searchParam += "&tol" + pnum + "=" + tol;
						searchParam += "&int" + pnum + "=" + rInt;
					}
				}
			}

			numSpct = Integer.valueOf(getParameter("num"));
			plotPane = new PlotPane[numSpct];
			clear();
			peaks1 = new Peak[numSpct];
			String[][] nameList = new String[numSpct][4];
			String[] param = new String[urlList.length];
			int[] cnt = new int[urlList.length];
			for ( int i = 0; i < urlList.length; i++ ) {
				 param[i] = "";
			}

			int siteMax = 0;
			for ( int i = 0; i < numSpct; i++ ) {
				String pnum = Integer.toString(i+1);
				// パラメータ取得
				nameList[i][0] = getParameter( "id" + pnum );
				nameList[i][1] = getParameter( "name" + pnum );
				nameList[i][2] = getParameter( "site" + pnum );

				int site = Integer.parseInt( nameList[i][2] );
				nameList[i][3] = Integer.toString( cnt[site]++ );
				param[site] += nameList[i][0] + ",";
				
				if ( siteMax < site ) {
					siteMax = site;
				}
			}

			String line;
			String[] tmp;
			ArrayList resultList = new ArrayList();
			for ( int i = 0; i < siteMax + 1; i++ ) {
				if ( cnt[i] == 0 ) {
					resultList.add( null );
					continue;
				}
				
				// パラメータ最後尾カンマを取り除く
				param[i] = param[i].substring( 0, param[i].length() - 1 );
				
				// リクエストURLセット
				String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GDATA2];
				String reqStr = baseUrl + MassBankCommon.DISPATCHER_NAME + "?type=" + typeName
				 + "&id=" + param[i] + "&site=" + Integer.toString(i);
				
				if ( reqType.equals("peak") || reqType.equals("diff") ) {
					reqStr += "&diff=";
					if ( reqType.equals("peak") ) {
						reqStr += "no";
					}
					else {
						reqStr += "yes";
					}
					reqStr += "&mz=" + paramMz.substring( 0, paramMz.length() -1 );
					reqStr += "&tol=" + paramTol.substring( 0, paramTol.length() -1 );
					reqStr += "&int=" + paramInt.substring( 0, paramInt.length() -1 );
				}
				
				URL url = new URL( reqStr );
				URLConnection con = url.openConnection();

				// レスポンス取得
				BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
				ArrayList<String> result = new ArrayList<String>();

				// レスポンス格納　
				while ( (line = in.readLine()) != null ) {
					if ( !line.equals("") ) {
						result.add( line );
					}
				}
				resultList.add( result );
				in.close();
			}
			
			hitPeaks.mzInfoList = new ArrayList[numSpct];
			precursor = new String[numSpct];
			Vector mzAry = new Vector();
			for ( int i = 0; i < numSpct; i++ ) {
				int site = Integer.parseInt( nameList[i][2] );
				int no = Integer.parseInt( nameList[i][3] );
				ArrayList result = (ArrayList)resultList.get(site);
				line = (String)result.get(no);

				// ピーク検索、ピーク差検索から呼ばれた場合、ヒットしたm/z値が返るので格納する
				String findStr = "hit=";
				int pos = line.indexOf( findStr );
				if ( pos > 0 ) { 
					String hit = line.substring( pos + 4 );
					String[] hitMzInfo = hit.split("\t");
					
					boolean isDiff = false;
					// ピーク検索の場合
					if ( reqType.equals("diff") ) {
						isDiff = true;
					}
					// m/z値を格納
					ArrayList mzInfoList = hitPeaks.setMz( hitMzInfo, isDiff );
					hitPeaks.mzInfoList[i] = new ArrayList();
					hitPeaks.mzInfoList[i].addAll(mzInfoList);
					line = line.substring( 0, pos );
				}

				// プレカーサー
				findStr = "precursor=";
				pos = line.indexOf(findStr);
				int posNext = 0;
				if ( pos > 0 ) { 
					posNext = line.indexOf( "\t", pos );
					precursor[i] = line.substring( pos + findStr.length(), posNext );
					line = line.substring( 0, pos );
				}
				else {
					precursor[i] = "";
				}

				tmp = line.split("\t\t");
				Vector mzs = new Vector();

				// m/z格納
				for (int j = 0; j < tmp.length; j++ ) {
					mzs.add( tmp[j] );
				}
				mzAry.add( mzs );
				
				// m/zの最大値を整数第2で切り上げた値をレンジの最大値とする
				String lastValStr = (String)mzs.lastElement();
				String[] lastVals = lastValStr.split("\t");
				int massMax = new BigDecimal(lastVals[0]).setScale(-2, BigDecimal.ROUND_UP).intValue();
				if ( massMax > MASS_MAX ) {
					MASS_MAX = massMax;
				}
			}
			
			massRange = MASS_MAX;

			for ( int i = 0; i < numSpct; i++ ) {
				plotPane[i] = new PlotPane(i);
				plotPane[i].setPreferredSize( new Dimension(1000, 800) );
				plotPane[i].repaint(); 

				JPanel pane1 = new JPanel();
				String name = nameList[i][1];
				String id = nameList[i][0];
				String site = nameList[i][2];
				pane1.add( new NameButton( name, id, site ) );
				pane1.add( new JLabel("ID:" + id ) );
				pane1.setLayout( new FlowLayout(FlowLayout.LEFT) );
				pane1.setMaximumSize( new Dimension(pane1.getMaximumSize().width, 100) );
				add(pane1);
				add(plotPane[i]);
				ButtonPane pane2 = new ButtonPane();
				pane2.addDiffButton(plotPane[i].idPeak);
				pane2.setLayout( new FlowLayout(FlowLayout.LEFT, 0, 0) );
				pane2.setMaximumSize( new Dimension(pane2.getMaximumSize().width, 100) );
				add(pane2);
				peaks1[i] = new Peak((Vector<String>)mzAry.get(i));
			}
			initMass();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void clear()
	{
		peaks1 = null;
		massStart = 0;
		massRange = MASS_MAX;
		intensityRange = INTENSITY_MAX;
	}

	/**
	 * 
	 */
	public void initMass()
	{
		massRange = -1;
		for ( int id = 0; id < numSpct; id ++ ) {
			float max = peaks1[id].getMaxMZ();
			if ( massRange < max ) { massRange = max; }
		}

		// massRangeを100単位にそろえる
		massRange = (float) Math.ceil(massRange / 100.0) * 100.0f;
		massStart = 0;
		intensityRange = INTENSITY_MAX;

		repaint();
	}

	/**
	 * 
	 */
	public int getIntensity()
	{
		return intensityRange;
	}
}
