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
 * スペクトル表示パネルクラス
 *
 * ver 1.0.2 2011.08.10
 *
 ******************************************************************************/
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import massbank.MassBankCommon;

public class PlotPane extends JPanel implements MouseListener, MouseMotionListener
{
	private static final int MARGIN = 15;
	private static final int MIN_RANGE = 5;
	private static final int INTENSITY_MAX = 1000;
	private static final Color[] colorTbl = {
		new Color(0xD2,0x69,0x48), new Color(0x22,0x8B,0x22),
		new Color(0x41,0x69,0xE1), new Color(0xBD,0x00,0x8B),
		new Color(0x80,0x80,0x00), new Color(0x8B,0x45,0x13),
		new Color(0x9A,0xCD,0x32)
	};

	private final double TOLERANCE = 0.01d;

	private double massStart = 0;
	private double massRange = 0;
	private double massMax = 0;
	private int hitNum = 0;
	private boolean isMZFlag = false;
	private String[] diffMzs = null;
	private int intensityRange = INTENSITY_MAX;

	private Peak peaks1 = null;
	private String precursor = "";

	private long lastClickedTime = 0;		// 最後にクリックした時間
	private Timer timer = null;

	private boolean underDrag = false;
	private Point fromPos = null;
	private Point toPos = null;
	private double xscale = 0;
	private double yscale = 0;

	private String reqType = "";
	private ArrayList<Double>[] hitPeaks1 = null;
	private ArrayList<Double>[] hitPeaks2 = null;
	private ArrayList<Integer>[] barColors = null;
	private int diffMargin = 0;
	private ArrayList<String> formulas = null;

	private Graphics g = null;
	private int panelWidth = 0;
	private int panelHeight = 0;

	private ArrayList<Double> mz1Ary = null;
	private ArrayList<Double> mz2Ary = null;
	private ArrayList<Integer> colorAry = null;

	private int hitMz1Cnt = -1;
	private int hitMz2Cnt = -1;
	private double prevHitMz1 = 0;
	private double prevHitMz2 = 0;

	private Point cursorPoint = null;		// マウスカーソルポイント
	private final Color onCursorColor = Color.blue;	// カーソル上色
	private final Color selectColor = Color.cyan.darker();	// 選択ピーク色
	
	private ArrayList<String> selectPeakList = null;
	
	private JPopupMenu selectPopup = null;			// ピーク選択ポップアップメニュー
	private JPopupMenu contextPopup = null;		// コンテキストポップアップメニュ
	
	/**
	 * コンストラクタ
	 * @
	 */
	public PlotPane(String reqType, Peak p, ColorInfo colorInfo, String precursor) {
		this.selectPeakList = new ArrayList<String>();
		this.peaks1 = p;
		this.precursor = precursor;

		//---------------------------------------------
		// m/zの最大値をセットする
		//---------------------------------------------
		double maxMz = 0;
		double massMax = 0;

		//(1) ピークデータがある場合
		if ( this.peaks1.getCount() > 0 ) {
			maxMz = this.peaks1.compMaxMzPrecusor(precursor);

			// m/zの最大値を整数第2で切り上げた値をレンジの最大値とする
			massMax = new BigDecimal(maxMz).setScale(-2, BigDecimal.ROUND_UP).intValue();
		}
		//(2) ピークデータがない場合
		else {
			if ( !precursor.equals("") ) {
				massMax = Double.parseDouble(precursor);
			}
		}
		this.massMax = massMax;
		
		// maxMzが100で割り切れる場合は+100の余裕を持つ
		if (maxMz != 0d && (maxMz % 100.0d) == 0d) {
			maxMz += 100.0d;
		}
		
		// massRangeを100単位にそろえる
		this.massRange = (double)Math.ceil(maxMz / 100.0) * 100.0d;
		
		//---------------------------------------------
		// ピークの色づけに必要な情報をセットする
		//---------------------------------------------
		this.reqType = reqType;
		if ( colorInfo != null ) {
			this.hitPeaks1  = colorInfo.getHitPeaks1();
			this.hitPeaks2  = colorInfo.getHitPeaks2();
			this.barColors  = colorInfo.getBarColors();
			this.diffMzs    = colorInfo.getDiffMzs();
			this.diffMargin = colorInfo.getDiffMargin();
			this.formulas   = colorInfo.getFormulas();
		}

		cursorPoint = new Point();
		
		// リスナー追加
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/**
	 * 
	 */
	int setStep(int range)
	{
		if (range < 20)  return 2;
		if (range < 50)  return 5;
		if (range < 100) return 10;
		if (range < 250) return 25;
		if (range < 500) return 50;
		return 100;
	}

	/**
	 * ピークの描画処理
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		this.g = g;

		//---------------------------------------------
		// フレームを描画する
		//---------------------------------------------
		drawChartFrame();

		//---------------------------------------------
		// ピークを描画する
		//---------------------------------------------
		if ( this.peaks1 == null || this.peaks1.getCount() == 0 ) {
			// ピークがない場合の表示
			drawNoPeak();
		}
		else {
			// ピークバーを描画
			drawPeakBar();

			// ピーク差検索のヒット位置を表示
			if ( this.reqType.equals("diff") ) {
				drawHitDiffPos();
			}
			else if ( this.reqType.equals("nloss") ) {
				drawHitNlossPos();
			}
		}

		//---------------------------------------------
		// プレカーサーm/zにマーク付け
		//---------------------------------------------
		drawPrecursorMark();

		//---------------------------------------------
		// マウスでドラッグした領域を黄色い線で囲む
		//---------------------------------------------
		if ( underDrag ) {
			fillRectRange();
		}
	}

	/**
	 * フレームを描画する
	 */
	private void drawChartFrame()
	{
		//---------------------------------------------
		// 上部余白のサイズをセット
		//---------------------------------------------
	 	int marginTop = 0;
		if ( this.reqType.equals("diff") ) {
			marginTop = MARGIN + 10 + 12 * this.diffMargin;
		}
		else if ( this.reqType.equals("product") ) {
			marginTop = MARGIN + 10 + 20;
		}
		else if ( this.reqType.equals("nloss") ) {
			marginTop = MARGIN + 10 + 60;
		}
		else {
			marginTop = MARGIN;
		}

		this.panelWidth = getWidth();
		this.panelHeight = getHeight();
		this.xscale = (this.panelWidth - 2.0d * MARGIN) / massRange;
		this.yscale = (this.panelHeight - (double)(MARGIN + marginTop) ) / this.intensityRange;

		//---------------------------------------------
		// 背景色を白で塗りつぶす
		//---------------------------------------------
		g.setColor(Color.white);
		g.fillRect(0, 0, this.panelWidth, this.panelHeight);

		g.setFont(g.getFont().deriveFont(9.0f));
		g.setColor(Color.lightGray);

		int x = MARGIN;
		int y = this.panelHeight - MARGIN;
		g.drawLine(x, marginTop, x, y);
		g.drawLine(x, y, this.panelWidth - MARGIN, y);

		//---------------------------------------------
		// x軸
		//---------------------------------------------
		int step = setStep((int)massRange);
		int start = (step - (int)massStart % step) % step;
		y = this.panelHeight - MARGIN;
		for (int i = start; i < (int)massRange; i += step) {
			x = MARGIN + (int)(i * xscale);
			g.drawLine(x, y, x, y + 3);

			// 小数点以下の桁数統一
			String mzStr = formatMass(i + massStart, true);
			g.drawString(mzStr, x - 5, this.panelHeight - 1);
		}

		//---------------------------------------------
		// y軸
		//---------------------------------------------
		for (int i = 0; i <= this.intensityRange; i += this.intensityRange / 5) {
			y = this.panelHeight - MARGIN - (int)(i * yscale);
			g.drawLine(MARGIN - 2, y, MARGIN, y);
			g.drawString(String.valueOf(i), 0, y);
		}
	}


	/**
	 * ピークバーを描画する(Peak Searchの場合)
	 */
	private void drawPeakBar()
	{
		if ( this.reqType.equals("peak") || this.reqType.equals("diff") ) {
			this.mz1Ary = this.hitPeaks1[hitNum];
			this.mz2Ary = this.hitPeaks2[hitNum];
			this.colorAry = this.barColors[hitNum];
		}
		else if ( this.reqType.equals("product") || this.reqType.equals("nloss") ) {
			this.mz1Ary = this.hitPeaks1[0];
			this.mz2Ary = this.hitPeaks2[0];
		}

		boolean isOnPeak;		// カーソルピーク上フラグ
		boolean isSelectPeak;	// 選択済みピークフラグ
		int start = this.peaks1.getIndex(massStart);
		int end = this.peaks1.getIndex(massStart + massRange);

		for ( int i = start; i < end; i++ ) {
			isOnPeak = false;
			isSelectPeak = this.peaks1.isSelectPeakFlag(i);
			double mz = this.peaks1.getMz(i);
			int its = this.peaks1.getIntensity(i);
			int w = (int)(xscale / 8);
			int h = (int)(its * yscale);
			int x = MARGIN + (int) ((mz - massStart) * xscale) - (int) Math.floor(xscale / 8);
			int y = this.panelHeight - MARGIN - h;
			
			// 描画パラメータ（高さ、位置）調整
			if (h == 0) {
				y -= 1;
				h = 1;
			}
			// 描画パラメータ（幅）調整
			if ( w < 2 ) {
				w = 2;
			} else if (w < 3) {
				w = 3;
			}
			
			// y軸より左側には描画しないように調整
			if (MARGIN >= x) {
				w = (w - (MARGIN - x) > 0) ? (w - (MARGIN - x)) : 1;
				x = MARGIN + 1;
			}
			
			// カーソルピーク上判定
			if (x <= cursorPoint.getX() 
					&& cursorPoint.getX() <= (x + w)
					&& y <= cursorPoint.getY() 
					&& cursorPoint.getY() <= (y + h)) {
				
				isOnPeak = true;
			}
			
			//---------------------------------------------
			// ヒットしたピークに色づけする
			//---------------------------------------------
			Color color = Color.black;
			//(1) Peak Searchの場合
			if ( this.reqType.equals("peak") || this.reqType.equals("diff") ) {
				color = getColorForPeak(mz);
			}
			//(2) Quick Search by Peakの場合
			else if ( this.reqType.equals("qpeak") ) {
				color = getColorForQuick(mz);
			}
			//(3) Product Ion検索の場合
			else if ( this.reqType.equals("product") ) {
				color = drawHitProduct(mz, its);
			}
			//(4) Neutral Loss検索の場合
			else if ( this.reqType.equals("nloss") ) {
				color = getColorForNloss(mz);
			}

			boolean isHit = true;
			if ( color == Color.black ) {
				isHit = false;
			}
			
			//---------------------------------------------
			// ピークバー描画
			//---------------------------------------------
			if ( isOnPeak ) {
				color = onCursorColor;
				if ( isSelectPeak ) {
					color = selectColor;
				}
			}
			else if ( isSelectPeak ) {
				color = selectColor;
			}
			//** 描画色をセット **
			g.setColor(color);
			g.fill3DRect(x, y, w, h, true);

			//----------------------------------------------------
			// m/z値を描画
			// 1) "all m/z"ボタンONで、intensityが400以上は「赤色」
			// 2) "all m/z"ボタンOFFで、ヒットピークはバーと同色
			// 3) それ以外は黒色
			//----------------------------------------------------
			if ( its > this.intensityRange * 0.4 || isMZFlag || isHit || isOnPeak || isSelectPeak ) {
				float fontSize = 9.0f;
				if ( isOnPeak ) {
					color = onCursorColor;
					fontSize = 14.0f;
					if ( isSelectPeak ) {
						color = selectColor;
					}
				}
				else if ( isSelectPeak ) {
					color = selectColor;
				}
				else if ( isMZFlag && its > this.intensityRange * 0.4 ) {
					color = Color.red;
				}
				g.setFont(g.getFont().deriveFont(fontSize));
				g.setColor(color);
				
				// 小数点以下の桁数統一
				String mzStr = formatMass(mz, false);
				g.drawString(mzStr, x, y);
			}
			
			//----------------------------------------------------
			// 強度値を描画
			//----------------------------------------------------
			if ( isOnPeak || isSelectPeak ) {
				// 強度目盛り描画
				if ( isOnPeak ) {
					g.setColor(onCursorColor);
				}
				if ( isSelectPeak ) {
					g.setColor(selectColor);
				}
				g.drawLine(MARGIN + 4, y, MARGIN - 4, y);
				
				// 強度値描画
				g.setColor(Color.lightGray);
				g.setFont(g.getFont().deriveFont(9.0f));
				if ( isOnPeak && isSelectPeak ) {
					g.setColor(Color.gray);
				}
				g.drawString(String.valueOf(its), MARGIN + 7, y + 1);
			}
		}
	}

	/**
	 * 描画色を取得する(通常Peak Searchの場合)
	 */
	private Color getColorForPeak(double mz)
	{
		int num = 1;
		boolean isHit = false;

		//(1) ヒットピークmz1と一致しているか
		for ( int j = 0; j < this.mz1Ary.size(); j++ ) {
			double hitMz1 = this.mz1Ary.get(j);
			if ( mz == hitMz1 ) {
				isHit = true;
				if ( hitMz1 - this.prevHitMz1 >= 1 ) {
					this.hitMz1Cnt++;
				}
				if ( this.colorAry.get(j) != null ) {
					num = this.colorAry.get(j);
				}
				else {
					num = this.hitMz1Cnt - (this.hitMz1Cnt / colorTbl.length) * colorTbl.length;
					this.barColors[hitNum].set( j, num );
				}
				this.prevHitMz1 = hitMz1;
				break;
			}
		}

		//(2) mz1と不一致の場合、ヒットピークmz2と一致しているか
		if ( !isHit ) {
			for ( int j = 0; j < this.mz2Ary.size(); j++ ) {
				double hitMz2 = this.mz2Ary.get(j);
				if ( mz == hitMz2 ) {
					isHit = true;
					if ( colorAry.get(j) != null ) {
						num = colorAry.get(j);
					}
					else if ( hitMz2 - this.prevHitMz2 >= 1 ) {
						num = this.hitMz2Cnt - (this.hitMz2Cnt / colorTbl.length) * colorTbl.length;
						this.barColors[hitNum].set( j, num );
					}
					else {}
					this.hitMz2Cnt++;
					this.prevHitMz2 = hitMz2;
					break;
				}
			}
		}

		if ( isHit ) {
			return colorTbl[num];
		}
		return Color.black;
	}

	/**
	 * 描画色を取得する(Quick Search by Peakの場合)
	 */
	private Color getColorForQuick(double mz)
	{
		double hitMz = 0;
		boolean isQhit = false;
		Color color = Color.black;

		// [完全一致] - 赤色をセット
		if ( this.hitPeaks1.length == 2 ) {
			ArrayList<Double> hitMzAry = this.hitPeaks1[1];
			for ( int j = 0; j < hitMzAry.size(); j++ ) {
				hitMz = hitMzAry.get(j);
				if ( mz == hitMz ) {
					isQhit = true;
					color = Color.RED;
					break;
				}
			}
		}
		// [トレランス内に入っている] - マゼンタ色をセット
		if ( !isQhit ) {
			ArrayList<Double> tolMzAry = this.hitPeaks1[0];
			for ( int j = 0; j < tolMzAry.size(); j++ ) {
				hitMz = tolMzAry.get(j);
				if ( mz == hitMz ) {
					isQhit = true;
					color = Color.MAGENTA;
					break;
				}
			}
		}

		//** 描画色をセット **
		if ( isQhit ) {
			return color;
		}
		return Color.black;
	}


	/**
	 * 描画色を取得する(Neutral Loss検索の場合)
	 */
	private Color getColorForNloss(double mz)
	{
		int num = 0;
		String prevFormula = "";
		double hitMz = 0;
		for ( int i = 0; i < this.mz1Ary.size(); i++ ) {
			double mz1 = this.mz1Ary.get(i);
			double mz2 = this.mz2Ary.get(i);
			if ( mz >= mz1 - TOLERANCE && mz <= mz1 + TOLERANCE ) {
				hitMz = mz1;
				break;
			}
			else if ( mz >= mz2 - TOLERANCE && mz <= mz2 + TOLERANCE ) {
				hitMz = mz2;
				break;
			}
			String formula = this.formulas.get(i);
			if ( !formula.equals(prevFormula) ) {
				num++;
			}
			prevFormula = formula;
		}

		ArrayList<Double> mzAry = new ArrayList<Double>();
		if ( hitMz > 0 ) {
//  		System.out.println("hitMz:" + String.valueOf(hitMz));
			for ( int i = 0; i < peaks1.getCount(); i++ ) {
				double getMz = this.peaks1.getMz(i);
				if ( getMz >= hitMz - TOLERANCE && getMz <= hitMz + TOLERANCE ) {
					mzAry.add( getMz );
				}
			}

			int maxInte = 0;
			int maxInteIndex = 0;
			for ( int i = 0; i < mzAry.size(); i++ ){
				int index = this.peaks1.getIndex( mzAry.get(i) );
				int inte = this.peaks1.getIntensity(index);
				if ( inte > maxInte ) {
					maxInte = inte;
					maxInteIndex = index;
				}
			}

			if ( maxInte > 0 && mz == this.peaks1.getMz(maxInteIndex) ) {
				return Color.MAGENTA;
			}
		}
		return Color.black;
	}

	/**
	 * 分子式を描画し、描画色を返す(Product Ion検索の場合)
	 */
	private Color drawHitProduct(double mz, int its)
	{
		for ( int i = 0; i < this.mz1Ary.size(); i++ ) {
			double mz1 = this.mz1Ary.get(i);
			if ( mz >= mz1 - TOLERANCE && mz <= mz1 + TOLERANCE ) {
				ArrayList<Double> mzAry = new ArrayList<Double>();
				for ( int j = 0; j < peaks1.getCount(); j++ ) {
					double getMz = this.peaks1.getMz(j);
					if ( getMz >= mz1 - TOLERANCE && getMz <= mz1 + TOLERANCE ) {
						mzAry.add( getMz );
					}
				}

				int maxInte = 0;
				int maxInteIndex = 0;
				for ( int j = 0; j < mzAry.size(); j++ ){
					int index = this.peaks1.getIndex( mzAry.get(j) );
					int inte = this.peaks1.getIntensity(index);
					if ( inte > maxInte ) {
						maxInte = inte;
						maxInteIndex = index;
					}
				}
				if ( maxInte > 0 && mz == this.peaks1.getMz(maxInteIndex) ) {
					String formula = this.formulas.get(i);
					int barWidth = (int)Math.floor(this.xscale / 8);
					int x = MARGIN + (int)((mz1 - this.massStart) * this.xscale) - barWidth / 2;
					int y = this.panelHeight - MARGIN - (int)(its * yscale) - 10;
					int xm = (int)(formula.length() * 5) + 10;

					g.setColor( Color.MAGENTA );
					g.fillRect( x - 1, y - 9, xm, 11 );
					g.setColor( Color.WHITE );
					g.drawString( formula, x, y );
					return Color.MAGENTA;
				}
			}
		}
		return Color.black;
	}

	/**
	 * ピークデータなしを表示する
	 */
	private void drawNoPeak()
	{
		g.setFont( new Font("Arial", Font.ITALIC, 24) );
		g.setColor( Color.LIGHT_GRAY );
		g.drawString( "No peak was observed.", this.panelWidth / 2 - 110, this.panelHeight / 2 );
	}

	/**
	 * ピーク差検索のヒット箇所を表示する
	 */
	private void drawHitDiffPos()
	{
		String diffmz = diffMzs[hitNum];
		int pos = diffmz.indexOf(".");
		if ( pos > 0 ) {
			BigDecimal bgMzZDiff = new BigDecimal( diffmz );
			diffmz = (bgMzZDiff.setScale(1, BigDecimal.ROUND_DOWN)).toString(); 
		}

		double mz1Prev = 0;
		int hitCnt = 0;
		for ( int j = 0; j < this.mz1Ary.size(); j++ ) {
			double mz1 = this.mz1Ary.get(j);
			double mz2 = this.mz2Ary.get(j);
			if ( mz1 - mz1Prev >= 1 ) {
				g.setColor(Color.GRAY);

				/* ピークバーのライン幅 */
				int barWidth = (int)Math.floor(xscale / 8);
				/* 横線左の開始位置 */
				int x1 = MARGIN + (int)((mz1 - massStart) * xscale) - barWidth / 2;
				/* 横線右の開始位置 */
				int x2 = MARGIN + (int)((mz2 - massStart) * xscale) - barWidth / 2;
				/* 横線右の開始位置 */
				int xc = x1 + (x2 - x1) / 2 - 12;
				/* Ｙ座標 */
				int y = this.panelHeight - ( MARGIN + (int)((INTENSITY_MAX + MARGIN*2) * yscale) + 5 + (++hitCnt * 12) );
				/* 文字幅 */
				int xm = (int)(diffmz.length() * 5) + 4;

				int padding = 5;

				// 横線描画
				g.drawLine( x1, y , xc, y );
				g.drawLine( xc + xm + padding, y, x2, y );

				// 縦線描画
				g.drawLine( x1, y, x1, y + 4 );
				g.drawLine( x2, y, x2, y + 4 );

				// ピーク差の値を描画
				int num = colorAry.get(j);
				g.setColor( colorTbl[num] );
				g.fillRect( xc, y - padding, (xc + xm + padding) - xc, padding * 2 );
				g.setColor( Color.WHITE );
				g.drawString( diffmz, xc + padding , y + 3 );
			}
			mz1Prev = mz1;
		}
	}

	/**
	 * ニュートラルロス差検索のヒット箇所を表示する
	 */
	private void drawHitNlossPos()
	{
		this.mz1Ary = this.hitPeaks1[0];
		this.mz2Ary = this.hitPeaks2[0];
		String prevFormula = "";
		int prevX2 = 0;
		int colorNum = -1;
		int num = 0;
		for ( int j = 0; j < this.mz1Ary.size(); j++ ) {
			double mz1 = this.mz1Ary.get(j);
			double mz2 = this.mz2Ary.get(j);
			String formula = this.formulas.get(j);
			g.setColor(Color.GRAY);

			/* ピークバーのライン幅 */
			int barWidth = (int)Math.floor(xscale / 8);
			/* 横線左の開始位置 */
			int x1 = MARGIN + (int)((mz1 - massStart) * xscale) - barWidth / 2;
			/* 横線右の開始位置 */
			int x2 = MARGIN + (int)((mz2 - massStart) * xscale) - barWidth / 2;
			/* Ｙ座標 */
			if ( x1 < prevX2 ) {
				num++;
			}
			int y = this.panelHeight - ( MARGIN + (int)((INTENSITY_MAX + MARGIN*2) * yscale) + 12 + num * 13 );
			/* 文字幅 */
			int xm = (int)(formula.length() * 5) + 4;

			int padding = 5;

			// 横線描画
			g.drawLine( x1, y , x2, y );

			// 縦線描画
			g.drawLine( x1, y, x1, y + 4 );
			g.drawLine( x2, y, x2, y + 4 );

			// ピーク差の値を描画
			if ( !formula.equals(prevFormula) ) {
				colorNum++;
			}
			g.setColor( colorTbl[colorNum] );

			int width = xm + padding * 2;
			int hight = padding * 2 - 1;
			int x = x1 + (x2 - x1 - width) / 2;
			if ( width > x2 - x1 ) {
				x = x1;
			}

			// 塗りつぶした四角に分子式の文字を描画
			g.fillRect( x, y - padding - 3, width - 1, hight );
			g.setColor( Color.WHITE );
			g.drawString( formula, x + (padding / 2) + 1, y );
			prevFormula = formula;
			prevX2 = x2;
		}
	}

	/**
	 * プレカーサーm/zにマーク付ける
	 */
	private void drawPrecursorMark()
	{
		if ( !this.precursor.equals("") ) {
			int pre = Integer.parseInt(this.precursor);
			int xPre = MARGIN + (int)((pre - massStart) * xscale) - (int)Math.floor(xscale / 8);
			int yPre = this.panelHeight - MARGIN;
			// プリカーサーm/zがグラフ内の場合のみ描画する
			if (xPre >= MARGIN && xPre <= this.panelWidth - MARGIN) {
				int [] xp = { xPre, xPre + 6, xPre - 6 };
				int [] yp = { yPre, yPre + 6, yPre + 6 };
				g.setColor( Color.RED );
				g.fillPolygon( xp, yp, xp.length );
			}
		}
	}

	/**
	 * マウスでドラッグした領域を黄色い線で囲む
	 */
	private void fillRectRange()
	{
		int xpos = Math.min(fromPos.x, toPos.x);
		int width = Math.abs(fromPos.x - toPos.x);
		g.setXORMode(Color.white);
		g.setColor(Color.yellow);
		g.fillRect(xpos, 0, width, this.panelHeight - MARGIN);
		g.setPaintMode();
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
	 * massStart値をセット
	 */
	public void setMassStart(double val)
	{
		this.massStart = val;
	}

	/**
	 * massMax値をセット
	 */
	public void setMassMax(double val)
	{
		this.massMax = val;
	}

	/**
	 * isMZFlag値をセット
	 */
	public void setIsMZFlag(boolean val)
	{
		this.isMZFlag = val;
	}

	/**
	 * hitNum値をセット
	 */
	public void setHitNum(int val)
	{
		this.hitNum = val;
	}


	/**
	 * massStart値を取得
	 */
	public double getMassStart()
	{
		return this.massStart;
	}

	/**
	 * massRange値を取得
	 */
	public double getMassRange()
	{
		return this.massRange;
	}

	/**
	 * massMax値を取得
	 */
	public double getMassMax()
	{
		return this.massMax;
	}


	/**
	 * マウスプレスイベント
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			if (timer != null && timer.isRunning()) {
				return;
			}
			fromPos = toPos = e.getPoint();
		}
	}

	/**
	 * マウスドラッグイベント
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			if (timer != null && timer.isRunning()) {
				return;
			}
			this.underDrag = true;
			toPos = e.getPoint();
			repaint();
		}
	}

	/**
	 * マウスリリースイベント
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		// 左リリースの場合
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			if (!underDrag || (timer != null && timer.isRunning())) {
				return;
			}
			underDrag = false;
			if ((fromPos != null) && (toPos != null)) {
				if (Math.min(fromPos.x, toPos.x) < 0) {
					massStart = Math.max(0, massStart - massRange / 3);
				}
				else if (Math.max(fromPos.x, toPos.x) > getWidth()) {
					massStart = Math.min(massMax - massRange, massStart + massRange / 3);
				}
				else {
					if ( this.peaks1 != null ) {
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
		// 右リリースの場合
		else if (SwingUtilities.isRightMouseButton(e)) {
			
			if (timer != null && timer.isRunning()) {
				return;
			}
			
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
			
			if (peaks1 != null) {
				if (selectPeakList.size() != 0) {
					item1.setEnabled(true);
					item2.setEnabled(true);
				}
			}
			
			// ポップアップメニュー表示
			contextPopup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * マウスクリックイベント
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if (timer != null && timer.isRunning()) {
			return;
		}
		
		// 左クリックの場合
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			
			// クリック間隔算出
			long interSec = (e.getWhen() - lastClickedTime);
			lastClickedTime = e.getWhen();
			
			// ダブルクリックの場合（クリック間隔280ミリ秒以内）
			if(interSec <= 280){
				fromPos = toPos = null;
				initMass();
			}
			// シングルクリックの場合
			else {
				// マウスクリックポイント
				Point p = e.getPoint();
				
				ArrayList<Integer> tmpClickPeakList = new ArrayList<Integer>();
				
				int height = getHeight();
				float yscale = (height - 2.0f * MARGIN) / intensityRange;
				int start, end, its, tmpX, tmpY, tmpWidth, tmpHight;
				double mz;
				start = peaks1.getIndex(massStart);
				end = peaks1.getIndex(massStart + massRange);

				for (int i = start; i < end; i++) {

					mz = peaks1.getMz(i);
					its = peaks1.getIntensity(i);
					tmpX = MARGIN + (int) ((mz - massStart) * xscale)
							- (int) Math.floor(xscale / 8); // Peak描画始点（X座標）
					tmpY = height - MARGIN - (int) (its * yscale); // Peak描画始点（Y座標）
					tmpWidth = (int) (xscale / 8); // 始点からの幅
					tmpHight = (int) (its * yscale); // 始点からの高さ

					if (MARGIN > tmpX) {
						tmpWidth = tmpWidth - (MARGIN - tmpX);
						tmpX = MARGIN;
					}

					if (tmpWidth < 2) {
						tmpWidth = 2;
					} else if (tmpWidth < 3) {
						tmpWidth = 3;
					}

					// マウスダウンした場所（X/Y座標）がPeakの描画エリアに含まれているかを判定
					if (tmpX <= p.getX() && p.getX() <= (tmpX + tmpWidth)
							&& tmpY <= p.getY()
							&& p.getY() <= (tmpY + tmpHight)) {

						tmpClickPeakList.add(i);
					}
				}

				// マウスダウンポイントにPeakが1つある場合、
				// マウスクリックと同時にPeakの色を変更する
				if (tmpClickPeakList.size() == 1) {

					int index = tmpClickPeakList.get(0);

					if (!peaks1.isSelectPeakFlag(index)) {
						if (peaks1.getSelectPeakNum() < MassBankCommon.PEAK_SEARCH_PARAM_NUM) {
							// 選択状態を設定
							selectPeakList.add(String.valueOf(peaks1
									.getMz(index)));
							peaks1.setSelectPeakFlag(index, true);
						} else {
							JOptionPane.showMessageDialog(PlotPane.this,
									" m/z of " + MassBankCommon.PEAK_SEARCH_PARAM_NUM + " peak or more cannot be selected. ",
									"Warning",
									JOptionPane.WARNING_MESSAGE);
							cursorPoint = new Point();
						}
					} else if (peaks1.isSelectPeakFlag(index)) {

						// 選択状態を解除
						selectPeakList.remove(String.valueOf(peaks1
								.getMz(index)));
						peaks1.setSelectPeakFlag(index, false);
					}
					PlotPane.this.repaint();
				}
				// マウスダウンポイントにPeakが2つ以上ある場合、
				// マウスクリックと同時にポップアップメニューを表示する
				else if (tmpClickPeakList.size() >= 2) {

					// ポップアップメニューインスタンス生成
					selectPopup = new JPopupMenu();
					JMenuItem item = null;
					int index = -1;

					// ポップアップメニュー追加
					for (int i = 0; i < tmpClickPeakList.size(); i++) {

						index = tmpClickPeakList.get(i);
						item = new JMenuItem(String.valueOf(peaks1.getMz(index)));
						selectPopup.add(item);
						item.addActionListener(new SelectMZPopupListener(index));

						if (peaks1.getSelectPeakNum() >= MassBankCommon.PEAK_SEARCH_PARAM_NUM
								&& !peaks1.isSelectPeakFlag(index)) {

							// Peak選択数がMAXの場合、選択済みPeak以外は選択不可を設定
							item.setEnabled(false);
						}
					}

					// ポップアップメニュー表示
					selectPopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

	/**
	 * 初期化
	 */
	public void initMass()
	{
		massRange = this.peaks1.compMaxMzPrecusor(this.precursor);
		
		// massRangeが100で割り切れる場合は+100の余裕を持つ
		if (massRange != 0d && (massRange % 100.0d) == 0d) {
			massRange += 100.0d;
		}
		
		// massRangeを100単位にそろえる
		massRange = (double) Math.ceil(massRange / 100.0) * 100.0d;
		massStart = 0;
		this.intensityRange = INTENSITY_MAX;
		repaint();
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
		// ポップアップが表示されている場合
		if ((selectPopup != null && selectPopup.isVisible())
				|| contextPopup != null && contextPopup.isVisible()) {
			
			return;
		}
		cursorPoint = e.getPoint();
		PlotPane.this.repaint();
	}

	/**
	 * 拡大処理をアニメーション化するクラス
	 */
	class AnimationTimer implements ActionListener
	{
		final int LOOP = 15;
		int loopCoef;
		int minx;
		int width;
		double tmpMassStart;
		double tmpMassRange;
		int tmpIntensityRange;
		int movex;

		public AnimationTimer(int w, int x)
		{
			loopCoef = 0;
			minx = x;
			width = w;
			movex = 0 + MARGIN;
			// 目的拡大率を算出
			double xs = (getWidth() - 2.0f * MARGIN) / massRange;
			tmpMassStart = massStart
					+ ((minx - MARGIN) / xs);
			tmpMassRange = 10 * (width / (10 * xs));
			if (tmpMassRange < MIN_RANGE) {
				tmpMassRange = MIN_RANGE;
			}

			// Intensityのレンジを設定
			if ((peaks1 != null) && (massRange <= massMax)) {
				// 最大値を検出。
				int max = 0;
				double start = Math.max(tmpMassStart, 0.0d);
				max = peaks1.getMaxIntensity(start, start + tmpMassRange);
				// 50単位に変換してスケールを決定
				tmpIntensityRange = (int)((1.0f + max / 50.0f) * 50.0f);
				if (tmpIntensityRange > INTENSITY_MAX) {
					tmpIntensityRange = INTENSITY_MAX;
				}
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			xscale = (getWidth() - 2.0f * MARGIN) / massRange;
			double yscale = (getHeight() - 2.0f * MARGIN) / intensityRange;
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
				massStart += (((tmpMassStart + massStart) / 2 - massStart) * loopCoef / LOOP);
				massRange += (((tmpMassRange + massRange) / 2 - massRange) * loopCoef / LOOP);
				intensityRange += (((tmpIntensityRange + intensityRange) / 2 - intensityRange) * loopCoef / LOOP);
				if (loopCoef >= LOOP) {
					movex = xpos;
					loopCoef = 0;
				}
			}
			repaint();
		}
	}
	
	/**
	 * ピーク選択ポップアップメニューリスナークラス
	 * PlotPaneのインナークラス
	 */
	class SelectMZPopupListener implements ActionListener {

		/** インデックス */
		private int index = -1;
		
		/**
		 * コンストラクタ
		 * @param index インデックス
		 */
		public SelectMZPopupListener(int index) {
			this.index = index;
		}

		/**
		 * アクションイベント
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			if (!peaks1.isSelectPeakFlag(index)
					&& peaks1.getSelectPeakNum() < MassBankCommon.PEAK_SEARCH_PARAM_NUM) {
				// 選択状態を設定
				selectPeakList.add(String.valueOf(peaks1.getMz(index)));
				peaks1.setSelectPeakFlag(index, true);
			} else if (peaks1.isSelectPeakFlag(index)) {
				// 選択状態を解除
				selectPeakList.remove(String.valueOf(peaks1.getMz(index)));
				peaks1.setSelectPeakFlag(index, false);
			}

			cursorPoint = new Point();
			PlotPane.this.repaint();
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
				urlParam.append("&num=" + peaks1.getSelectPeakNum());			// num ：
				urlParam.append("&tol=0");										// tol ：0
				urlParam.append("&int=5");										// int ：5
				
				for (int i = 0; i < peaks1.getSelectPeakNum(); i++) {
					if (i != 0) {
						urlParam.append("&op" + i + "=and");					// op ：and
					} else {
						urlParam.append("&op" + i + "=or");						// op ：or
					}
					urlParam.append("&mz" + i + "=" + selectPeakList.get(i));	// mz ：
				}
				urlParam.append("&sortKey=name&sortAction=1&pageNo=1&exec=&inst=all");
				
				// JSP呼び出し
				String reqUrl = Display.baseUrl + "jsp/Result.jsp"
						+ urlParam.toString();
				try {
					Display.context.showDocument(new URL(reqUrl), "_blank");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (com.equals("reset")) {
				if (peaks1 != null) {
					selectPeakList = new ArrayList<String>();
					peaks1.initSelectPeakFlag();
				}
			}
			
			cursorPoint = new Point();
			PlotPane.this.repaint();
		}
	}
}
