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
 * ver 2.0.8 2008.12.05
 *
 ******************************************************************************/

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import massbank.GetConfig;
import massbank.MassBankCommon;

/**
 * スペクトル表示 クラス
 */
@SuppressWarnings("serial")
public class Display extends JApplet
{
	private int MASS_MAX = 0;
	static int INTENSITY_MAX = 1000;
	static int MARGIN = 15;
	static int MIN_RANGE = 5;
	static int DEF_EX_PANE_SIZE = 150;

	Peak peaks1 = null;	
	float massStart = 0;
	private float massRange = 0;
	int intensityRange = INTENSITY_MAX;
	boolean head2tail = false;
	boolean underDrag = false;
	Point fromPos = null;
	Point toPos = null;
	float xscale = 0;
	PlotPane plotPane = null;
	JSplitPane jsp_plt2ext = null;
	boolean isMZFlag = false;
	String precursor = "";
	private String reqType = "";
	private int diffMargin = 0;
	private String[] diffMzs = null;
	private ArrayList<Float>[] hitPeaks1 = null;
	private ArrayList<Float>[] hitPeaks2 = null;
	private ArrayList<Integer>[] barColors = null;
	private int hitNum = 0;

	class PlotPane extends JPanel implements MouseListener,
			MouseMotionListener
	{
		private long lastClickedTime = 0;						// 最後にクリックした時間
		private Timer timer = null;


		// 拡大処理をアニメーション化するクラス
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

			public AnimationTimer(int w, int x)
			{
				loopCoef = 0;
				minx = x;
				width = w;
				movex = 0 + MARGIN;
				// 目的拡大率を算出
				float xs = (getWidth() - 2.0f * MARGIN) / massRange;
				tmpMassStart = massStart
						+ ((minx - MARGIN) / xs);
				tmpMassRange = 10 * (width / (10 * xs));
				if (tmpMassRange < MIN_RANGE) {
					tmpMassRange = MIN_RANGE;
				}

				// Intensityのレンジを設定
				if ((peaks1 != null) && (massRange <= MASS_MAX))
				{
					// 最大値を検出。
					int max = 0;
					float start = Math.max(tmpMassStart, 0.0f);
					max = peaks1.getMaxIntensity(start, start + tmpMassRange);
					// 50単位に変換してスケールを決定
					tmpIntensityRange = (int)((1.0f + max / 50.0f) * 50.0f);
					if(tmpIntensityRange > INTENSITY_MAX)
						tmpIntensityRange = INTENSITY_MAX;
				}
			}

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

		public PlotPane()
		{
			addMouseListener(this);
			addMouseMotionListener(this);
		}

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

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			int width = getWidth();
			int height = getHeight();
			xscale = (width - 2.0f * MARGIN) / massRange;

			ArrayList<Float> mz1Ary = null;
			ArrayList<Float> mz2Ary = null;
			ArrayList<Integer> colorAry = null;

			// ピークバーを色づけするカラーをセット
			int colorTblNum = 1;
			Color[] colorTbl = {
				new Color(0xD2,0x69,0x48), new Color(0x22,0x8B,0x22),
				new Color(0x41,0x69,0xE1), new Color(0xBD,0x00,0x8B),
				new Color(0x80,0x80,0x00), new Color(0x8B,0x45,0x13),
				new Color(0x9A,0xCD,0x32)
			};

			// ヒットしたピークを取得
			if ( reqType.equals("peak") || reqType.equals("diff") ) {
				mz1Ary = hitPeaks1[hitNum];
				mz2Ary = hitPeaks2[hitNum];
				colorAry = barColors[hitNum];
			}
			
			// 上部余白のサイズをセット
			int marginTop = 0;
			if ( reqType.equals("diff") ) {
				marginTop = MARGIN + 10 + 12 * diffMargin;
			}
			else {
				marginTop = MARGIN;
			}

			float yscale = (height - (float)(MARGIN + marginTop) ) / intensityRange;
			// 背景を白にする
			g.setColor(Color.white);
			g.fillRect(0, 0, width, height);

			g.setFont(g.getFont().deriveFont(9.0f));
			g.setColor(Color.lightGray);

			// 目盛りを描く
			g.drawLine(MARGIN, marginTop, MARGIN, height - MARGIN);
			g.drawLine(MARGIN, height - MARGIN, width - MARGIN, height - MARGIN);

			// x軸
			int step = setStep((int)massRange);
			int start = (step - (int)massStart % step) % step;

			
			for (int i = start; i < (int)massRange; i += step)	{
				g.drawLine(MARGIN + (int) (i * xscale),
						height - MARGIN, MARGIN + (int) (i * xscale),
						height - MARGIN + 3);
				g.drawString(String.valueOf(i + massStart),
						MARGIN + (int) (i * xscale) - 5,
						height - 1);
			}
			// y軸
			for (int i = 0; i <= intensityRange; i += intensityRange / 5) {
				g.drawLine(MARGIN - 2, height - MARGIN - (int) (i * yscale),
						MARGIN,
						height - MARGIN - (int) (i * yscale));
				g.drawString(String.valueOf(i),
						0,
						height - MARGIN - (int) (i * yscale));
			}

			//========================================================
			// ピークバーを描画
			//========================================================
			int end, its, x, w;
			float peak;

			if (peaks1 != null) {
				float hitMz1Prev = 0;
				float hitMz2Prev = 0;
				int hitMz1Cnt = -1;
				int hitMz2Cnt = -1;
				start = peaks1.getIndex(massStart);
				end = peaks1.getIndex(massStart + massRange);

				for ( int i = start; i < end; i++ ) {
					peak = peaks1.getMZ(i);
					its = peaks1.getIntensity(i);
					x = MARGIN + (int) ((peak - massStart) * xscale) - (int) Math.floor(xscale / 8);
					w = (int)(xscale / 8);
					if ( MARGIN > x ) {
						w = w - (MARGIN - x);
						x = MARGIN;
					}
					if ( w < 2 ) {
						w = 2;
					}
					g.setColor(Color.black);

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
									barColors[hitNum].set( j, colorTblNum );
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
										barColors[hitNum].set( j, colorTblNum );
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
					else if ( reqType.equals("qpeak") ) {
						float mz = 0;
						Color hitColor = null;
						boolean isQhit = false;

						if ( hitPeaks1.length == 2 ) {
							ArrayList<Float> hitMzAry = hitPeaks1[1];
							for ( int j = 0; j < hitMzAry.size(); j++ ) {
								mz = hitMzAry.get(j);
								if ( peak == mz ) {
									isQhit = true;
									hitColor = Color.RED;
									break;
								}
							}
						}
						if ( !isQhit ) {
							ArrayList<Float> tolMzAry = hitPeaks1[0];
							for ( int j = 0; j < tolMzAry.size(); j++ ) {
								mz = tolMzAry.get(j);
								if ( peak == mz ) {
									isQhit = true;
									hitColor = Color.MAGENTA;
									break;
								}
							}
						}
						// ヒットピークと一致している場合、描画色をセット
						if ( isQhit ) {
							g.setColor( hitColor );
							if ( its > intensityRange * 0.4 ) {
								 isHit = true;
							}
						}
					}

					// ピーク描画
					g.fill3DRect( x,height - MARGIN - (int)(its * yscale),
							w,(int) (its * yscale),	true );

					// m/z値を描画
					if ( its > intensityRange * 0.4 || isMZFlag || isHit ) {
						if ( isMZFlag && its > intensityRange * 0.4 ) {
							g.setColor(Color.red);
						}
						else if ( isHit ) {
							// 色は変えない
						}
						else {
							g.setColor(Color.black);
						}
						g.drawString(String.valueOf(peak),
									x, height - MARGIN - (int) (its * yscale));
					}
				}
			}

			// ピークがない場合
			else {
				g.setFont( new Font("Arial", Font.ITALIC, 24) );
				g.setColor( Color.LIGHT_GRAY );
				g.drawString( "No peak was observed.",	width/2-110, height / 2 );
			}

			//========================================================
			// ピーク差検索でヒットしたピークの位置を表示
			//========================================================
			if ( reqType.equals("diff") ) {
				String diffmz = diffMzs[hitNum];
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
						int y = height - ( MARGIN + (int)((INTENSITY_MAX + MARGIN*2) * yscale) + 5 + (++hitCnt * 12) );
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
			if ( !precursor.equals("") ) {
				int pre = Integer.parseInt(precursor);
				int xPre = MARGIN + (int)((pre - massStart) * xscale) - (int)Math.floor(xscale / 8);
				int yPre = height - MARGIN;
				// プリカーサーm/zがグラフ内の場合のみ描画する
				if (xPre >= MARGIN && xPre <= width - MARGIN) {
					int [] xp = { xPre, xPre + 6, xPre - 6 };
					int [] yp = { yPre, yPre + 6, yPre + 6 };
					g.setColor( Color.RED );
					g.fillPolygon( xp, yp, xp.length );
				}
			}


			// マウスでドラッグした領域を黄色い線で囲む
			if (underDrag) {
				g.setXORMode(Color.white);
				g.setColor(Color.yellow);
				int xpos = Math.min(fromPos.x, toPos.x);
				width = Math.abs(fromPos.x - toPos.x);
				g.fillRect(xpos, 0, width, height - MARGIN);
				g.setPaintMode();
			}
		}

		
		public void mousePressed(MouseEvent e)
		{
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				if(timer != null && timer.isRunning())
					return;
	
				fromPos = toPos = e.getPoint();
			}
		}

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

		public void mouseClicked(MouseEvent e)
		{
			// 左ボタンの場合
			if ( SwingUtilities.isLeftMouseButton(e) ) {
				
				// クリック間隔算出
				long interSec = (e.getWhen() - lastClickedTime);
				lastClickedTime = e.getWhen();
				
				// ダブルクリックの場合（クリック間隔280ミリ秒以内）
				if(interSec <= 280){
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

	class ButtonPane extends JPanel implements
			ActionListener
	{
		JToggleButton mzDisp = null;
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
				isMZFlag = mzDisp.isSelected();

			// Diffボタン押下時
			int pos = com.indexOf( comNameDiff );
			if ( pos >= 0 ) {
				int num = Integer.parseInt(com.substring(comNameDiff.length()));
				hitNum = num;
			}
					
			Display.this.repaint();
		}

		/**
		 * 
		 */
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

	public void init() {

		try {
			clear();
			Vector<String> vecMzs = new Vector<String>();
			if ( getParameter("qpeak") != null ) {
				String paramPeak = getParameter("qpeak");
				String line[] = paramPeak.split("@");
				for ( int i = 0; i < line.length; i++ ) {
					String item[] = line[i].split(",");
					vecMzs.add( item[0] + "\t" + item[1] );
				}
			}
			else {
				// 環境設定ファイルから連携サイトのURLを取得
				String confPath = getCodeBase().toString();
				confPath = confPath.replaceAll( "jsp/", "" );
				GetConfig conf = new GetConfig(confPath);
				String severUrl = conf.getServerUrl();
				String baseUrl = severUrl + "jsp/";
				String id = getParameter("id");
				String site = getParameter("site");

				if ( getParameter("type") != null ) {
					reqType = getParameter("type");
				}

				if ( !reqType.equals("") ) {
					int iNum = Integer.parseInt(getParameter("num"));
					hitPeaks1 = new ArrayList[iNum];
					hitPeaks2 = new ArrayList[iNum];
					barColors = new ArrayList[iNum];
					ArrayList<Float> mz1Ary = null;
					ArrayList<Float> mz2Ary = null;
					ArrayList<Integer> colorAry = null;
					diffMzs = new String[iNum];

					for ( int i = 0; i < iNum; i++ ) {
						String line = getParameter( "mz" + String.valueOf(i+1) );
						String[] mzs = line.split("@");
						mz1Ary = new ArrayList<Float>();
						mz2Ary = new ArrayList<Float>();
						colorAry = new ArrayList<Integer>();

						// Peak Difference Search の場合
						if ( reqType.equals("diff") ) {
							diffMzs[i] = getParameter( "diff" + String.valueOf(i+1) );
							for ( int j = 0; j < mzs.length; j++ ) {
								String[] mzPair = mzs[j].split(",");
								mz1Ary.add( Float.parseFloat(mzPair[0]) );
								mz2Ary.add( Float.parseFloat(mzPair[1]) );
								colorAry.add( null );
							}
							diffMargin = Integer.parseInt( getParameter("margin") );
						}
						else if ( reqType.equals("qpeak") ) {
							mz1Ary = new ArrayList<Float>();
							for ( int j = 0; j < mzs.length; j++ ) {
								mz1Ary.add( Float.parseFloat(mzs[j]) );
								colorAry.add(0);
							}
						}
						// Peak Search の場合
						else {
							mz1Ary = new ArrayList<Float>();
							for ( int j = 0; j < mzs.length; j++ ) {
								mz1Ary.add( Float.parseFloat(mzs[j]) );
								colorAry.add(0);
							}
						}

						// m/z値を格納
						hitPeaks1[i] = new ArrayList<Float>();
						hitPeaks2[i] = new ArrayList<Float>();
						barColors[i] = new ArrayList<Integer>();
						hitPeaks1[i].addAll(mz1Ary);
						hitPeaks2[i].addAll(mz2Ary);
						barColors[i].addAll(colorAry);
					}
				}

				if ( getParameter("precursor") != null ) {
					precursor = getParameter("precursor");
				}

				String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_GDATA];
				String reqStr = baseUrl + MassBankCommon.DISPATCHER_NAME + "?type=" + typeName + "&id=" + id + "&site=" + site;
				URL url = new URL( reqStr );
				URLConnection con = url.openConnection();
				String line;
				String[] tmp;
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				boolean isStartSpace = true;

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
					vecMzs.add(line);
				}
				in.close();
			}

			int massMax = 0;
			if ( vecMzs.size() > 0 ) {
				// m/zの最大値を整数第2で切り上げた値をレンジの最大値とする
				String lastValStr = vecMzs.lastElement();
				String[] lastVals = lastValStr.split("\t");
				massMax = new BigDecimal(lastVals[0]).setScale(-2, BigDecimal.ROUND_UP).intValue();
			}
			else {
				if ( !precursor.equals("") ) {
					massMax = Integer.parseInt(precursor);
				}
			}
			MASS_MAX = massMax;
			massRange = MASS_MAX;
			if ( vecMzs.size() > 0 ) {
				// m/z値の設定
				setPeaks(new Peak(vecMzs));
			}

			setLayout(new BorderLayout());
			plotPane = new PlotPane();
			add(plotPane, BorderLayout.CENTER);
			ButtonPane pane = new ButtonPane();
			if ( reqType.equals("diff") ) {
				pane.addDiffButton();
			}
			add(pane, BorderLayout.SOUTH);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void clear()
	{
		peaks1 = null;
		massStart = 0;
		massRange = MASS_MAX;
		intensityRange = INTENSITY_MAX;
	}

	public void setPeaks(Peak p)
	{
		peaks1 = p;
		initMass();
	}

	public void initMass()
	{
		massRange = peaks1.getMaxMZ();

		// massRangeを100単位にそろえる
		massRange = (float) Math.ceil(massRange / 100.0) * 100.0f;
		massStart = 0;
		intensityRange = INTENSITY_MAX;

		repaint();
	}

	public int getIntensity()
	{
		return intensityRange;
	}
}