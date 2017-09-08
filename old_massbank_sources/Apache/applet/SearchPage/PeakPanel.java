/*******************************************************************************
 *
 * Copyright (C) 2010 JST-BIRD MassBank
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
 * ピークパネル クラス
 *
 * ver 1.0.10 2011.08.10
 *
 ******************************************************************************/

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import massbank.MassBankCommon;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * ピークパネル クラス
 */
@SuppressWarnings("serial")
public class PeakPanel extends JPanel {

	public static final int INTENSITY_MAX = 1000;	// 最大強度

	private static final int MARGIN = 12;				// マージン
	
	private static final int MASS_RANGE_MIN = 5;		// 最小マスレンジ

	private static int massRangeMax = 0;
	
	private PeakData peaks1 = null;
	private PeakData peaks2 = null;

	private double massStart = 0;
	private double massRange = 0;

	private int intensityRange = INTENSITY_MAX;

	private boolean head2tail = false;	// 比較用パネルフラグ

	private Point fromPos = null;			// ドラッグ開始ポイント
	private Point toPos = null;			// ドラッグ終了ポイント
	
	private double xscale = 0;
	
	private SearchPage searchPage = null;			// SearchPageオブジェクト

	private String tolVal = null;					// Tolerance入力値
	private boolean tolUnit = true;				// Tolerance単位選択値（true：unit、false：ppm）
	
	private Point cursorPoint = null;				// マウスカーソルポイント
	
	private String typeLbl1 = " ";								// スペクトル種別1文字列
	private String typeLbl2 = " ";								// スペクトル種別2文字列
	
	public static final String SP_TYPE_QUERY = "Query";		// スペクトル種別（クエリー）
	public static final String SP_TYPE_COMPARE = "Compare";	// スペクトル種別（比較）
	public static final String SP_TYPE_RESULT = "Result";		// スペクトル種別（結果）
	
	private static final String SP_TYPE_MERGED = "MERGED SPECTRUM";	// スペクトル種別（統合）
	private int TYPE_LABEL_1 = 1;								// ラベル1
	private int TYPE_LABEL_2 = 2;								// ラベル2
	
	private JLabel nameLbl = null;					// 化合物名ラベル

	private String precursor = "";					// プリカーサー
	
	private boolean isNoPeak = false;

	private ArrayList<String> selectPeakList = null;

	private long lastClickedTime = 0;

	private JButton leftMostBtn = null;
	private JButton leftBtn = null;
	private JButton rightBtn = null;
	private JButton rightMostBtn = null;

	private JToggleButton mzDisp = null;
	private JToggleButton mzHitDisp = null;

	private static boolean isInitRate = false;	// 初期倍率フラグ(true:未拡大、false:拡大中)

	public BufferedImage structImgM = null;
	public BufferedImage structImgS = null;
	public String formula = "";
	public String emass = "";

	/**
	 * コンストラクタ
	 * @param isHead2Tail 比較用パネルフラグ（true：比較用パネル、false：比較用パネル以外）
	 */
	public PeakPanel(boolean isHead2Tail) {
		selectPeakList = new ArrayList<String>();
		head2tail = isHead2Tail;
		
		if ( head2tail ) {
			typeLbl1 = SP_TYPE_COMPARE;
			typeLbl2 = " ";
		}
		
		GridBagConstraints gbc = null;						// レイアウト制約オブジェクト
		GridBagLayout gbl = new GridBagLayout();
		
		JPanel typePane1 = new TypePane(TYPE_LABEL_1, new Color(153 , 153, 153), 16);
		typePane1.setMinimumSize(new Dimension(22, 76));
		typePane1.setPreferredSize(new Dimension(22, 76));
		typePane1.setMaximumSize(new Dimension(22, 76));
		
		JPanel typePane2 = new TypePane(TYPE_LABEL_2, new Color(0 , 0, 255), 9);
		typePane2.setPreferredSize(new Dimension(22, 0));
		
		JPanel typePane = new JPanel();
		typePane.setLayout(new BoxLayout(typePane, BoxLayout.Y_AXIS));
		typePane.add(typePane1);
		typePane.add(typePane2);
		
		gbc = new GridBagConstraints();						// レイアウト制約初期化
		gbc.fill = GridBagConstraints.VERTICAL;				// 垂直サイズの変更のみを許可
		gbc.weightx = 0;									// 余分の水平スペースを分配しない
		gbc.weighty = 1;									// 余分の垂直スペースを分配
		gbc.gridheight = GridBagConstraints.REMAINDER;		// 行最後のコンポーネントに指定
		gbl.setConstraints(typePane, gbc);	
		
		
		PlotPane plotPane = new PlotPane();		
		
		gbc = new GridBagConstraints();						// レイアウト制約初期化
		gbc.fill = GridBagConstraints.BOTH;					// 垂直、水平サイズの変更を許可
		gbc.weightx = 1;									// 余分の水平スペースを分配
		gbc.weighty = 1;									// 余分の垂直スペースを分配
		gbc.gridwidth = GridBagConstraints.REMAINDER;		// 列最後のコンポーネントに指定
		gbl.setConstraints(plotPane, gbc);
		
		
		ButtonPane btnPane =new ButtonPane();	
		
		gbc = new GridBagConstraints();						// レイアウト制約初期化
		gbc.fill = GridBagConstraints.BOTH;					// 垂直、水平サイズの変更を許可
		gbc.weightx = 1;									// 余分の水平スペースを分配
		gbc.weighty = 0;									// 余分の垂直スペースを分配しない
		gbc.gridwidth = GridBagConstraints.REMAINDER;		// 列最後のコンポーネントに指定
		gbc.gridheight = GridBagConstraints.REMAINDER;		// 行最後のコンポーネントに指定
		gbl.setConstraints(btnPane, gbc);
		
		setLayout(gbl);
		add(typePane);
		add(plotPane);
		add(btnPane);
	}

	/**
	 * スペクトル種別ペインクラス
	 * PeakPanelのインナークラス
	 */
	class TypePane extends JPanel {
		
		private int lblNo = -1;
		private Color fontColor = new Color(0, 0, 0);
		private int fontSize = 1;
		
		/**
		 * デフォルトコンストラクタ
		 * @deprecated
		 */
		private TypePane() {
		}
		
		/**
		 * コンストラクタ
		 * @param lbl ラベル番号
		 * @param color フォントカラー
		 * @param size フォントサイズ
		 */
		public TypePane(int lbl, Color color, int size) {
			this.lblNo = lbl;
			this.fontColor = color;
			this.fontSize = size;
		}
		
		/**
		 * ペイントコンポーネント
		 * @param g
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
	    public void paintComponent(Graphics g) {

			// スペクトルが表示されている場合のみ処理を行う（ピークがない場合も表示）
			if ((!head2tail && peaks1 != null) 
					|| (head2tail && peaks2 != null)
					|| (!head2tail && peaks1 == null && isNoPeak) ) {
				
		        Graphics2D g2 = (Graphics2D)g;
		        
		        // 色セット
		        g2.setPaint(fontColor);
		        
		        // アンチエイリアス
		        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		        FontRenderContext frc = new FontRenderContext(null, true, true);
		        Font font;
		        Shape shape;
		        if (lblNo == TYPE_LABEL_1) {
		        	font = new Font("Arial", Font.ITALIC, fontSize);
		        	shape = new TextLayout(typeLbl1, font, frc).getOutline(null);
		        }
		        else {
		        	font = new Font("Arial", Font.ITALIC | Font.BOLD, fontSize);
		        	shape = new TextLayout(typeLbl2, font, frc).getOutline(null);
		        }
		        Rectangle2D b = shape.getBounds();
		        
		        // 描画位置変換（回転）オブジェクト取得
		        AffineTransform at1 = AffineTransform.getRotateInstance(Math.toRadians(-90), b.getX(), b.getY());
		        
		        // 描画位置変換（平行移動）オブジェクト取得
		        AffineTransform at2;
		        if (lblNo == TYPE_LABEL_1) {
		        	at2 = AffineTransform.getTranslateInstance(3, b.getWidth() + b.getHeight() + 5);
		        }
		        else {
		        	at2 = AffineTransform.getTranslateInstance(7, getHeight() + 1);
		        }
		        // 変換を適用して描画
		        g2.fill(at2.createTransformedShape(at1.createTransformedShape(shape)));
	    	}
	    }
	}
	
	/**
	 * スペクトル表示ペイン
	 * PeakPanelのインナークラス
	 */
	class PlotPane extends JPanel implements MouseListener, MouseMotionListener {
		
		private JPopupMenu selectPopup = null;			// ピーク選択ポップアップメニュー
		private JPopupMenu contextPopup = null;		// コンテキストポップアップメニュ

		private Timer timer = null;					// 拡大処理用タイマーオブジェクト
		
		private boolean underDrag = false;			// ドラッグ中フラグ
		
		private final int STATUS_NORAML = 0;			// ピーク描画用ステータス（NOMAL）
		private final int STATUS_NEXT_LAST = 1;		// ピーク描画用ステータス（NEXTLAST）
		private final int STATUS_CLOSED = 2;			// ピーク描画用ステータス（CLOSED）
		
		private final Color onCursorColor = Color.blue;		// カーソル上色
		
		/**
		 * コンストラクタ
		 */
		public PlotPane() {
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
		 * ペイントコンポーネント
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int width = getWidth();
			int height = getHeight();
			xscale = (width - 2.0d * MARGIN) / massRange;
			double yscale = (height - 2.0d * MARGIN) / intensityRange;
			// 背景を白にする
			g.setColor(Color.white);
			g.fillRect(0, 0, width, height);

			if ( !head2tail && peaks1 != null) {
				boolean isSizeM = false;
				// 構造式の画像を表示する
				if ( structImgM != null && height > structImgM.getHeight() ) {
					g.drawImage(structImgM, (width - structImgM.getWidth()), 0, null);
					isSizeM = true;
				}
				else if ( structImgS != null && height > structImgS.getHeight() ) {
					g.drawImage(structImgS, (width - structImgS.getWidth()), 5, null);
					isSizeM = false;
				}

				// FORMULA, EXACT MASSを表示する
				if ( !formula.equals("") ) {
					String info = formula + " (" + emass + ")";
					int xPos = 0;
					int fontSize = 0;
					if ( isSizeM ) {
						xPos = width - info.length() * 7;
						fontSize = 12;
					}
					else {
						xPos = width - info.length() * 6;
						fontSize = 10;
					}
					g.setFont(new Font("SansSerif",Font.BOLD,fontSize));
					g.setColor(new Color(0x008000));
					g.drawString(info, xPos - 2, 12);
				}
			}

			g.setFont(g.getFont().deriveFont(9.0f));
			g.setColor(Color.lightGray);
			if (!head2tail) {
				// 目盛りを描く
				g.drawLine(MARGIN, MARGIN, MARGIN, height - MARGIN);
				g.drawLine(MARGIN, height - MARGIN, width - MARGIN, height
						- MARGIN);
				// x軸
				int step = stepCalc((int)massRange);
				int start = (step - (int)massStart % step) % step;
				for (int i = start; i < (int)massRange; i += step) {
					g.drawLine(MARGIN + (int)(i * xscale), height - MARGIN,
							MARGIN + (int)(i * xscale), height - MARGIN + 2);
					g.drawString(formatMass(i + massStart, true), MARGIN
							+ (int)(i * xscale) - 5, height - 1);
				}
				// y軸
				for (int i = 0; i <= intensityRange; i += intensityRange / 5) {
					g.drawLine(MARGIN - 2,
							height - MARGIN - (int)(i * yscale), MARGIN,
							height - MARGIN - (int)(i * yscale));
					g.drawString(String.valueOf(i), 0, height - MARGIN
							- (int)(i * yscale));
				}
			} else {
				// HEAD2TAIL
				// 目盛りを描く
				g.drawLine(MARGIN, MARGIN, MARGIN, height - MARGIN);
				g.drawLine(MARGIN, height / 2, width - MARGIN, height / 2);
				// x軸
				int step = stepCalc((int)massRange);
				int start = (step - (int)massStart % step) % step;
				for (int i = start; i < (int)massRange; i += step) {
					g.drawLine(MARGIN + (int)(i * xscale), height / 2 + 1,
							MARGIN + (int)(i * xscale), height / 2 - 1);

					g.drawString(formatMass(i + massStart, true), MARGIN
							+ (int)(i * xscale) - 5, height - 1);
				}
				// y軸
				for (int i = 0; i <= intensityRange; i += intensityRange / 5) {
					g.drawLine(MARGIN - 2, height / 2 - (int)(i * yscale) / 2,
							MARGIN, height / 2 - (int)(i * yscale) / 2);

					g.drawString(String.valueOf(i), 0, height / 2
							- (int)(i * yscale) / 2);

					g.drawLine(MARGIN - 2, height / 2 + (int)(i * yscale) / 2,
							MARGIN, height / 2 + (int)(i * yscale) / 2);

					g.drawString(String.valueOf(i), 0, height / 2
							+ (int)(i * yscale) / 2);
				}
			}

			// クエリ用パネル、検索結果用パネル
			if (!head2tail) {
				int start, end;
				if (peaks1 != null) {
					int its, x, y, w, h;
					double mz;
					boolean isOnPeak;		// カーソルピーク上フラグ
					boolean isSelectPeak;	// 選択済みピークフラグ
					
					start = peaks1.getIndex(massStart);
					end = peaks1.getIndex(massStart + massRange);
					
					for (int i=start; i<end; i++) {
						
						mz = peaks1.getMz(i);
						its = peaks1.getIntensity(i);
						isOnPeak = false;
						isSelectPeak = peaks1.isSelectPeakFlag(i);
						
						x = MARGIN + (int)((mz - massStart) * xscale) - (int)Math.floor(xscale / 8);
						y = height - MARGIN - (int)(its * yscale);
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
						
						
						// m/z値、Peak描画
						g.setColor(Color.black);
						g.setFont(g.getFont().deriveFont(9.0f));
						if (isOnPeak) {
							g.setColor(onCursorColor);
							g.setFont(g.getFont().deriveFont(14.0f));
							if (isSelectPeak) {
								g.setColor(Color.cyan.darker());
							}
							g.drawString(formatMass(mz, false), x, y);
						}
						else if (isSelectPeak) {
							g.setColor(Color.cyan.darker());
							g.drawString(formatMass(mz, false), x, y);
						}
						else if (mzDisp.isSelected()) {
							if (its > intensityRange * 0.4) {
								g.setColor(Color.red);
							}
							g.drawString(formatMass(mz, false), x, y);
							g.setColor(Color.black);
						}
						else {
							if (its > intensityRange * 0.4) {
								g.drawString(formatMass(mz, false), x, y);
							}
						}
						// fill3DRectメソッドで第3引数、第4引数に0が指定されると
						// 正しく描画できないので注意（Javaのバグ）
						g.fill3DRect(x, y, w, h, true);
						
						
						if (isOnPeak || isSelectPeak) {
							// 強度目盛り描画
							if (isOnPeak) {
								g.setColor(onCursorColor);
							}
							if (isSelectPeak) {
								g.setColor(Color.cyan.darker());
							}
							g.drawLine(MARGIN + 4, y, MARGIN - 4, y);

							// 強度描画
							g.setColor(Color.lightGray);
							g.setFont(g.getFont().deriveFont(9.0f));
							if (isOnPeak && isSelectPeak) {
								g.setColor(Color.gray);
							}
							g.drawString(String.valueOf(its), MARGIN + 7, y + 1);
						}
					}

					// プリカーサーm/zに三角マーク付け
					if ( !precursor.equals("") ) {
						
						int pre = Integer.parseInt(precursor);
						int preX = MARGIN + (int)((pre - massStart) * xscale) - (int)Math.floor(xscale / 8);

						// プリカーサーm/zがグラフ内の場合のみ描画
						if ( preX >= MARGIN 
								&& preX <= width - MARGIN ) {
							
							int[] xp = { preX, preX+6, preX-6 };
							int[] yp = { height - MARGIN, height-MARGIN+5, height-MARGIN+5 };
							g.setColor( Color.RED );
							g.fillPolygon( xp, yp, 3 );
						}
					}
					
					allBtnCtrl(true);
				}
				// ピークがない場合
				else if (isNoPeak) {
					g.setFont(new Font("Arial", Font.ITALIC, 24));
					g.setColor(Color.lightGray);
					g.drawString("No peak was observed.", width / 2 - 110,
							height / 2);
					allBtnCtrl(false);
				} else {
					selectPeakList.clear();
					allBtnCtrl(false);
				}
			}
			// 比較用パネル
			else if (peaks2 != null) {
				
				// 共通ピークを検出
				int start1 = peaks1.getIndex(massStart);
				int end1 = peaks1.getIndex(massStart + massRange);
				int start2 = peaks2.getIndex(massStart);
				int end2 = peaks2.getIndex(massStart + massRange);
				if (end1 > 0) {
					end1 -= 1;
				}
				if (end2 > 0) {
					end2 -= 1;
				}
				int ind1 = start1;
				int ind2 = start2;
				double mz1 = peaks1.getMz(ind1);
				double mz2 = peaks2.getMz(ind2);
				int its1 = peaks1.getIntensity(ind1);
				int its2 = peaks2.getIntensity(ind2);

				int x = 0, y = 0, y2 = 0, w = 0, h = 0, h2 = 0;
				boolean isMz1Update = false;
				boolean isMz2Update = false;
				int mz1status = STATUS_NORAML;
				int mz2status = STATUS_NORAML;
				if (peaks1.getMz(end1) < massStart) {
					mz1status = STATUS_CLOSED;
				}
				if (peaks2.getMz(end2) < massStart) {
					mz2status = STATUS_CLOSED;
				}
				boolean isMatchPeak = false; // 完全一致ピークプラグ

				while (mz1status < STATUS_CLOSED || mz2status < STATUS_CLOSED) {
					isMz1Update = false;
					isMz2Update = false;
					isMatchPeak = false;
					if (ind1 == end1 && mz1status == STATUS_NORAML) {
						mz1status = STATUS_NEXT_LAST;
					}
					if (ind2 == end2 && mz2status == STATUS_NORAML) {
						mz2status = STATUS_NEXT_LAST;
					}

					w = (int) (xscale / 8);
					
					// 描画パラメータ（幅）調整
					if (w < 2) {
						w = 2;
					} else if (w < 3) {
						w = 3;
					}
					
					g.setColor(Color.black);
					
					if (mz1 == mz2) {
						
						isMatchPeak = true;
						x = MARGIN + (int)((mz1 - massStart) * xscale) - (int)Math.floor(xscale / 8);
						y = height / 2 - (int)((its1 * yscale) / 2);
						y2 = height / 2 + 1;
						h = (int)((its1 * yscale) / 2);
						h2 = (int)((its2 * yscale) / 2);

						// 描画パラメータ（高さ、位置）調整
						if (h == 0) {
							h = 1;
						}
						if (h2 == 0) {
							h2 = 1;
						}

						
						// y軸より左側には描画しないように調整
						if (MARGIN > x) {
							w = (w - (MARGIN - x) > 0) ? (w - (MARGIN - x)) : 1;
							x = MARGIN + 1;
						}
						
						// m/z値描画
						if (mzDisp.isSelected()) {
							if ((int)(its1 * yscale) / 2 >= ((height - MARGIN * 2) / 2) * 0.4) {
								g.setColor(Color.red);
							}
							g.drawString(formatMass(mz1, false), x, y);
							g.setColor(Color.black);

							if ((int)(its2 * yscale) / 2 >= ((height - MARGIN * 2) / 2) * 0.4) {
								g.setColor(Color.red);
							}
							g.drawString(formatMass(mz2, false), x, (y2 + h2 + 7));
							g.setColor(Color.black);
						} else {
							if (!mzHitDisp.isSelected()) {
								if ((int)(its1 * yscale) / 2 >= ((height - MARGIN * 2) / 2) * 0.4) {
									g.drawString(formatMass(mz1, false), x, y);
								}
								if ((int)(its2 * yscale) / 2 >= ((height - MARGIN * 2) / 2) * 0.4) {
									g.drawString(formatMass(mz2, false), x, (y2
											+ h2 + 7));
								}
							}
						}

						// 強度がCutoff以上のピークの場合に色づけ
						if (its1 >= SearchPage.CUTOFF_THRESHOLD
								&& its2 >= SearchPage.CUTOFF_THRESHOLD) {
							// 描画色を赤色にセット
							g.setColor(Color.red);

							if (mzHitDisp.isSelected()) {
								g.drawString(formatMass(mz1, false), x, y);
								g.drawString(formatMass(mz2, false), x,
										(y2 + h2 + 7));
							}
						}

						if (mz1status == STATUS_NEXT_LAST) {
							mz1status = STATUS_CLOSED;
						}
						if (mz2status == STATUS_NEXT_LAST) {
							mz2status = STATUS_CLOSED;
						}
						isMz1Update = true;
						isMz2Update = true;
					} else if ((mz2 < mz1 && mz2status != STATUS_CLOSED)
							|| mz1status == STATUS_CLOSED) {
						
						// mz2(対象の化合物)の座標をセット
						x = MARGIN + (int)((mz2 - massStart) * xscale) - (int)Math.floor(xscale / 8);
						y = height / 2 + 1;
						h = (int)(its2 * yscale / 2);
						
						// 描画パラメータ（高さ、位置）調整
						if (h == 0) {
							h = 1;
						}

						// y軸より左側には描画しないように調整
						if (MARGIN > x) {
							w = (w - (MARGIN - x) > 0) ? (w - (MARGIN - x)) : 1;
							x = MARGIN + 1;
						}
						
						// m/z値描画
						if (mzDisp.isSelected()) {
							if (h >= ((height - MARGIN * 2) / 2) * 0.4) {
								g.setColor(Color.red);
							}
							g.drawString(formatMass(mz2, false), x, (y + h + 7));
							g.setColor(Color.black);
						} else if (!mzHitDisp.isSelected()
								&& h >= ((height - MARGIN * 2) / 2) * 0.4) {
							g.drawString(formatMass(mz2, false), x, (y + h + 7));
						}

						// mz2(対象の化合物)がTolerance内かチェック
						if (checkTolerance(true, mz2, its2, peaks1)) {
							// 描画色をマゼンタ色にセット
							g.setColor(Color.magenta);
							if (mzHitDisp.isSelected()) {
								g.drawString(formatMass(mz2, false), x,
										(y + h + 7));
							}
						}

						if (mz2status == STATUS_NEXT_LAST) {
							mz2status = STATUS_CLOSED;
						}
						isMz2Update = true;
					} else if ((mz1 < mz2 && mz1status != STATUS_CLOSED)
							|| mz2status == STATUS_CLOSED) {
						
						// mz1(クエリ値)の座標をセット
						x = MARGIN + (int)((mz1 - massStart) * xscale) - (int)Math.floor(xscale / 8);
						y = height / 2 - (int)((its1 * yscale) / 2);
						h = (int)((its1 * yscale) / 2);
						
						// 描画パラメータ（高さ、位置）調整
						if (h == 0) {
							h = 1;
						}
						
						// y軸より左側には描画しないように調整
						if (MARGIN > x) {
							w = (w - (MARGIN - x) > 0) ? (w - (MARGIN - x)) : 1;
							x = MARGIN + 1;
						}
						
						// m/z値描画
						if (mzDisp.isSelected()) {
							if (h >= ((height - MARGIN * 2) / 2) * 0.4) {
								g.setColor(Color.red);
							}
							g.drawString(formatMass(mz1, false), x, y);
							g.setColor(Color.black);
						} else if (!mzHitDisp.isSelected()
								&& h >= ((height - MARGIN * 2) / 2) * 0.4) {
							g.drawString(formatMass(mz1, false), x, y);
						}

						// mz2(対象の化合物)でTolerance内のものがあるかチェック
						if (checkTolerance(false, mz1, its1, peaks2)) {
							// 描画色を赤色にセット
							g.setColor(Color.red);
							if (mzHitDisp.isSelected()) {
								g.drawString(formatMass(mz1, false), x, y);
							}
						}

						if (mz1status == STATUS_NEXT_LAST) {
							mz1status = STATUS_CLOSED;
						}
						isMz1Update = true;
					} else {
					}

					// ピーク描画
					g.fill3DRect(x, y, w, h, true);
					if (isMatchPeak) {
						g.fill3DRect(x, y2, w, h2, true);
					}
					g.setColor(Color.black);

					if (isMz1Update) {
						if (ind1 < end1) {
							mz1 = peaks1.getMz(++ind1);
							its1 = peaks1.getIntensity(ind1);
						}
					}
					if (isMz2Update) {
						if (ind2 < end2) {
							mz2 = peaks2.getMz(++ind2);
							its2 = peaks2.getIntensity(ind2);
						}
					}
				}
				allBtnCtrl(true);
			} else {
				allBtnCtrl(false);
				if (head2tail) {
					mzHitDisp.setSelected(false);
					mzHitDisp.setEnabled(false);
				}
			}

			// スペクトルが表示されている場合のみ処理を行う
			if ((!head2tail && peaks1 != null) || (head2tail && peaks2 != null)) {

				if (underDrag) {// マウスでドラッグした領域を黄色い線で囲む
					g.setXORMode(Color.white);
					g.setColor(Color.yellow);
					int xpos = Math.min(fromPos.x, toPos.x);
					width = Math.abs(fromPos.x - toPos.x);
					g.fillRect(xpos, MARGIN, width, height - MARGIN * 2);
					g.setPaintMode();
				}
			}
		}

		/**
		 * マウスプレスイベント
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
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
			if (SwingUtilities.isLeftMouseButton(e)) {
				if (timer != null && timer.isRunning()) {
					return;
				}

				underDrag = true;
				toPos = e.getPoint();
				PeakPanel.this.repaint();
			}
		}

		/**
		 * マウスリリースイベント
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			// 左リリースの場合
			if (SwingUtilities.isLeftMouseButton(e)) {
				if (!underDrag || (timer != null && timer.isRunning())) {
					return;
				}
				underDrag = false;
				if ((fromPos != null) && (toPos != null)) {
					if (Math.min(fromPos.x, toPos.x) < 0)
						massStart = Math.max(0, massStart - massRange / 3);

					else if (Math.max(fromPos.x, toPos.x) > getWidth())
						massStart = Math.min(massRangeMax - massRange, massStart
								+ massRange / 3);
					else {
						// ドラッグ時ズームイン処理条件変更
						if ((!head2tail && peaks1 != null)
								|| (head2tail && peaks2 != null)) {

							PeakPanel.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
							
							isInitRate = false;
							
							timer = new Timer(30,
									new AnimationTimer(Math.abs(fromPos.x - toPos.x),
											Math.min(fromPos.x, toPos.x)));
							timer.start();
						} else {
							fromPos = toPos = null;
							PeakPanel.this.repaint();
						}
					}
				}
			}
			// 右リリースの場合
			else if (SwingUtilities.isRightMouseButton(e)) {
				
				if (timer != null && timer.isRunning()) {
					return;
				}
				
				// 比較用スペクトルパネルには表示しない
				if (head2tail) {
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
			if (SwingUtilities.isLeftMouseButton(e)) {

				// クリック間隔算出
				long interSec = (e.getWhen() - lastClickedTime);
				lastClickedTime = e.getWhen();

				// ダブルクリックの場合（クリック間隔280ミリ秒以内）
				if (interSec <= 280) {

					if ((!head2tail && peaks1 != null)
							|| (head2tail && peaks2 != null)) {

						// ズームアウト処理
						searchPage.setAllPlotAreaRange();
						fromPos = toPos = null;
						intensityRange = INTENSITY_MAX;
						isInitRate = true;
					}
				}
				// シングルクリックの場合（クリック間隔281ミリ秒以上）
				else {

					if (searchPage == null) {
						return;
					}

					// マウスクリックポイント
					Point p = e.getPoint();

					// 比較用パネルの場合、スペクトルがnullの場合
					if (head2tail || peaks1 == null) {
						return;
					}

					ArrayList<Integer> tmpClickPeakList = new ArrayList<Integer>();

					int height = getHeight();
					double yscale = (height - 2.0d * MARGIN) / intensityRange;
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
								JOptionPane.showMessageDialog(PeakPanel.this,
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
						PeakPanel.this.repaint();
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
			if (searchPage == null || head2tail || peaks1 == null) {
				return;
			}
			
			// ポップアップが表示されている場合
			if ((selectPopup != null && selectPopup.isVisible())
					|| contextPopup != null && contextPopup.isVisible()) {
				
				return;
			}
			
			cursorPoint = e.getPoint();
			PeakPanel.this.repaint();
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
				double xs = (getWidth() - 2.0d * MARGIN) / massRange;
				tmpMassStart = massStart + ((toX - MARGIN) / xs);
				tmpMassRange = 10 * (fromX / (10 * xs));
				if (tmpMassRange < MASS_RANGE_MIN) {
					tmpMassRange = MASS_RANGE_MIN;
				}

				// Intensityのレンジを設定
				if ((peaks1 != null) && (massRange <= massRangeMax)) {
					// 最大値を検出。
					int max = 0;
					double start = Math.max(tmpMassStart, 0.0d);
					max = searchPage.getMaxIntensity(start, start + tmpMassRange);
					if (peaks2 != null)
						max = Math.max(max, peaks2.getMaxIntensity(start, start
								+ tmpMassRange));
					// 50単位に変換してスケールを決定
					tmpIntensityRange = (int) ((1.0d + max / 50.0d) * 50.0d);
					if (tmpIntensityRange > INTENSITY_MAX)
						tmpIntensityRange = INTENSITY_MAX;
				}
			}
			
			/**
			 * アクションイベント
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				xscale = (getWidth() - 2.0d * MARGIN) / massRange;
				int xpos = (movex + toX) / 2;
				if (Math.abs(massStart - tmpMassStart) <= 2
						&& Math.abs(massRange - tmpMassRange) <= 2) {
					xpos = toX;
					massStart = tmpMassStart;
					massRange = tmpMassRange;
					timer.stop();
					searchPage.setAllPlotAreaRange(PeakPanel.this);
					PeakPanel.this.setCursor(Cursor.getDefaultCursor());
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
					if (loopCoef >= LOOP) {
						movex = xpos;
						loopCoef = 0;
					}
				}
				PeakPanel.this.repaint();
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
				PeakPanel.this.repaint();
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
					String reqUrl = SearchPage.baseUrl + "jsp/Result.jsp"
							+ urlParam.toString();
					try {
						searchPage.getAppletContext().showDocument(new URL(reqUrl), "_blank");
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
				PeakPanel.this.repaint();
			}
		}
	}
	
	/**
	 * ボタンペインクラス
	 * PeakPanelのインナークラス
	 */
	class ButtonPane extends JPanel implements ActionListener {
		
		/**
		 * コンストラクタ
		 */
		public ButtonPane() {
			leftMostBtn = new JButton("<<");
			leftMostBtn.setActionCommand("<<");
			leftMostBtn.addActionListener(this);
			leftMostBtn.setMargin(new Insets(0, 0, 0, 0));
			leftMostBtn.setEnabled(false);

			leftBtn = new JButton(" < ");
			leftBtn.setActionCommand("<");
			leftBtn.addActionListener(this);
			leftBtn.setMargin(new Insets(0, 0, 0, 0));
			leftBtn.setEnabled(false);

			rightBtn = new JButton(" > ");
			rightBtn.setActionCommand(">");
			rightBtn.addActionListener(this);
			rightBtn.setMargin(new Insets(0, 0, 0, 0));
			rightBtn.setEnabled(false);

			rightMostBtn = new JButton(">>");
			rightMostBtn.setActionCommand(">>");
			rightMostBtn.addActionListener(this);
			rightMostBtn.setMargin(new Insets(0, 0, 0, 0));
			rightMostBtn.setEnabled(false);

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
			mzDisp.setActionCommand("mz");
			mzDisp.addActionListener(this);
			mzDisp.setMargin(new Insets(0, 0, 0, 0));
			mzDisp.setSelected(false);
			mzDisp.setEnabled(false);

			if (head2tail) {
				mzHitDisp = new JToggleButton("show hit m/z");
				mzHitDisp.setActionCommand("mzhit");
				mzHitDisp.addActionListener(this);
				mzHitDisp.setMargin(new Insets(0, 0, 0, 0));
				mzHitDisp.setSelected(false);
				mzHitDisp.setEnabled(false);
			}

			nameLbl = new JLabel();
			nameLbl.setForeground(Color.blue);
			
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			add(leftMostBtn);
			add(leftBtn);
			add(rightBtn);
			add(rightMostBtn);
			add(mzDisp);
			if (head2tail) {
				add(mzHitDisp);
			}
			else {
				add(nameLbl);
			}
		}

		/**
		 * アクションイベント
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent ae) {
			String com = ae.getActionCommand();
			if (com.equals("<<")) {
				massStart = Math.max(0, massStart - massRange);
			} else if (com.equals("<")) {
				massStart = Math.max(0, massStart - massRange / 4);
			} else if (com.equals(">")) {
				massStart = Math.min(massRangeMax - massRange, massStart + massRange / 4);
			} else if (com.equals(">>")) {
				massStart = Math.min(massRangeMax - massRange, massStart + massRange);
			} else if (com.equals("mz")) {
				if (head2tail && mzDisp.isSelected()) {
					mzHitDisp.setSelected(false);
				}
			} else if (com.equals("mzhit")) {
				if (mzHitDisp.isSelected()) {
					mzDisp.setSelected(false);
				}
			}
			searchPage.setAllPlotAreaRange(PeakPanel.this);
			PeakPanel.this.repaint();
		}
	}

	public void clear() {
		peaks1 = peaks2 = null;
		massStart = 0;
		massRangeMax = 0;
		massRange = 0;
		intensityRange = INTENSITY_MAX;
		isNoPeak = false;
		isInitRate = true;
		if (!head2tail) {
			setSpectrumInfo("", "", "", "", false);
		}
	}

	/**
	 * ピーク情報設定
	 * @param p ピーク情報
	 * @param index インデックス
	 */
	public void setPeaks(PeakData p, int index) {
		if (index == 0) {
			peaks1 = p;
			if (!head2tail) {
				selectPeakList.clear();
			}
		} else if (index == 1) {
			peaks2 = p;
		}

		if (peaks1 != null) {
			massRange = peaks1.compMaxMzPrecusor(precursor);
		}

		if (peaks2 != null) {
			massRange = Math.max(peaks2.compMaxMzPrecusor(precursor), massRange);
			mzHitDisp.setEnabled(true);
			mzHitDisp.setSelected(true);
		}
		
		// massRangeが100で割り切れる場合は+100の余裕を持つ
		if (massRange != 0d && (massRange % 100.0d) == 0d) {
			massRange += 100.0d;
		}
		// massRangeを100単位にそろえる
		massRange = Math.ceil(massRange / 100.0d) * 100.0d;

		massStart = 0;
		intensityRange = INTENSITY_MAX;
		massRangeMax = (int)massRange;

		this.repaint();
	}

	/**
	 * ピーク情報取得
	 * @param index インデックス
	 * @return ピーク情報
	 */
	public PeakData getPeaks(int index) {
		if (index == 0) {
			return peaks1;
		}
		return peaks2;
	}
	
	public double getMassStart() {
		return massStart;
	}
	
	/**
	 * マスレンジ取得
	 * @return マスレンジ
	 */
	public double getMassRange() {
		return massRange;
	}
	
	/**
	 * 
	 * @param s
	 * @param r
	 * @param i
	 */
	public void setMass(double s, double r, int i) {
		massStart = s;
		massRange = r;
		intensityRange = i;
		this.repaint();
	}

	/**
	 * 強度レンジ取得
	 * @return 強度レンジ
	 */
	public int getIntensityRange() {
		return intensityRange;
	}

	/**
	 * 強度レンジ設定
	 * @param range 強度レンジ
	 */
	public void setIntensityRange(int range) {
		intensityRange = range;
	}

	/**
	 * SearchPageオブジェクト設定
	 * @param obj SearchPageオブジェクト
	 */
	public void setSearchPage(SearchPage obj) {
		searchPage = obj;
	}

	/**
	 * Tolerance入力値セット
	 * @param val tolerance値
	 * @param unit unitフラグ（true：unit、false：ppm）
	 */
	public void setTolerance(String val, boolean unit) {
		tolVal = val;
		tolUnit = unit;
	}
	
	
	/**
	 * スペクトル情報設定
	 * スペクトル下部の化合物名設定及び、ツールチップ文字列設定
	 * @param name 化合物名
	 * @param key スペクトルレコードを特定できるキー文字列
	 * @param percursor プリカーサー
	 * @param spType スペクトル種別（Query または Result)
	 * @param invalid 統合スペクトル判定無効フラグ
	 */
	public void setSpectrumInfo(String name, String key, String precursor, String spType, boolean invalid) {
		
		typeLbl1 = " ";
		typeLbl2 = " ";
		
		// 統合スペクトルの場合は「MERGED SPECTRUM」を表示
		if (key.length() != 0 ) {
			typeLbl1 = spType;
			if ( !invalid ) {
				if ( name.indexOf("MERGED") != -1 ) {
					typeLbl2 = SP_TYPE_MERGED;
				}
			}
		}
		
		nameLbl.setText("  " + name);
		if (name.trim().length() != 0 && key.trim().length() != 0) {
			nameLbl.setToolTipText(key + ":  " + name);
		}
		this.precursor = precursor;
	}
	
	/**
	 * プリカーサー取得
	 * @return プリカーサー
	 */
	public String getPrecursor() {
		return precursor;
	}
	
	/**
	 * ピーク有無フラグ設定
	 * @param isNoPeak ピーク有無状態（true：無し、false：有り）
	 */
	public void setNoPeak(boolean isNoPeak) {
		this.isNoPeak = isNoPeak;
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
	}
	
	/**
	 * Tolerance内であるかをチェックする（Cutoffも考慮する）
	 * 
	 * @param mode 比較対象(true：mz1、false：mz2)
	 * @param compMz 比較元m/z
	 * @param compIts 比較元Intensity
	 * @param peaks ピーク情報
	 * @return 結果(true：Tolerance内、false：Torerance外)
	 */
	private boolean checkTolerance(boolean mode, double compMz, int compIts, PeakData peaks) {
		
		// 比較元が強度がCutoffより小さい場合
		if (compIts < SearchPage.CUTOFF_THRESHOLD) {
			return false;
		}

		double tolerance = 0;
		long lngTolerance = 0;
		long mz1;
		long mz2;
		int its1 = 0;
		int its2 = 0;
		long minusRange;
		long plusRange;
		final int TO_INTEGER_VAL = 100000;	// 丸め誤差が生じるため整数化するのに使用

		// Tolerance入力値
		tolerance = Double.parseDouble(tolVal);
		
		mz1 = mz2 = (long) (compMz * TO_INTEGER_VAL);
		for (int i = peaks.getPeakNum() - 1; i >= 0; i--) {
			if (mode) {
				mz1 = (long) (peaks.getMz(i) * TO_INTEGER_VAL);
				its1 = peaks.getIntensity(i);
			} else {
				mz2 = (long) (peaks.getMz(i) * TO_INTEGER_VAL);
				its2 = peaks.getIntensity(i);
			}

			// unitの場合
			if (tolUnit) {
				lngTolerance = (int) (tolerance * TO_INTEGER_VAL);
				minusRange = mz1 - lngTolerance;
				plusRange = mz1 + lngTolerance;
			}
			// ppmの場合
			else {
				minusRange = (long) (mz1 * (1 - tolerance / 1000000));
				plusRange = (long) (mz1 * (1 + tolerance / 1000000));
			}

			// これ以降で交差はありえない
			if ((mode && plusRange < mz2) || (!mode && minusRange > mz2)) {
				return false;
			}

			// 交差内であるか
			if (minusRange <= mz2 && mz2 <= plusRange) {
				if ((mode && its1 >= SearchPage.CUTOFF_THRESHOLD)
						|| (!mode && its2 >= SearchPage.CUTOFF_THRESHOLD)) {

					return true;
				}
			}
		}
		return false;
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
	 * 構造式画像の読み込み
	 * @param gifMUrl 通常画像のURL
	 * @param gifSUrl 小さい画像のUR
	 */
	public void loadStructGif(String gifMUrl, String gifSUrl) {
		try {
			if ( !gifMUrl.equals("") ) {
				this.structImgM = ImageIO.read(new URL(gifMUrl));
			}
			else {
				this.structImgM = null;
			}
			if ( !gifSUrl.equals("") ) {
				this.structImgS = ImageIO.read(new URL(gifSUrl));
			}
			else {
				this.structImgS = null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 化合物情報のセット
	 * @param formula 分子式
	 * @param emass 精密質量
	 */
	public void setCompoundInfo(String formula, String emass) {
		this.formula = formula;
		this.emass = emass;
	}
}


