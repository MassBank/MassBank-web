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
 * Multiple Spectra Display用 MolViewPane クラス
 *
 * ver 1.0.0 2009.12.14
 *
 ******************************************************************************/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import metabolic.MolFigure;
import canvas.DrawPane;

@SuppressWarnings("serial")
public class MolViewPane extends DrawPane
{
	private boolean   alreadyRead;
	private draw2d.MOLformat mft;
	private MolFigure mf;
	Dimension size;

	/**
	 * 新しいMolViewPaneを構築します。
	 */
	public MolViewPane()
	{
		super(null, null);
		mft = new draw2d.MOLformat();
		alreadyRead = false;
		size = new Dimension(-1, -1);
	}

	/**
	 * 初期化します。
	 */
	public void init()
	{
		util.MolMass.init();
		prepareMenusForPopups(false);
	}

	/**
	 * クリアします。
	 */
	public void clear()
	{
		if(mf != null)
			mf.clear();
	}

	/**
	 * 与えられたID、名前、データをセットします。
	 * @param id ID
	 * @param name 名前
	 * @param data molデータ
	 */
	public void read(String id, String name, String data)
	{
		mft.read(data);
		set(id, name);
		alreadyRead = true;
		size = getPaperSize();
		size.setSize(size.getWidth()-26, size.getHeight()-35);
		unselectAllSymbols();
	}

	/**
	 * 与えられたID、名前、データをセットします。
	 * @param id ID
	 * @param name 名前
	 * @param file molファイル
	 */
	public void read(String id, String name, File file)
		throws FileNotFoundException, IOException
	{
		java.io.BufferedReader br = new java.io.BufferedReader(new InputStreamReader(new FileInputStream(file)));
		mft.read(br);
		set(id, name);
		alreadyRead = true;
		size = getPaperSize();
		size.setSize(size.getWidth()-26, size.getHeight()-35);
		unselectAllSymbols();
	}

	/**
	 * シンボルの選択をはずします。
	 */
	public void unselectedSymbols()
	{
		unselectAllSymbols();
	}

	/**
	 * 既に読み込み済みであるかを調べます。
	 * @return 既に読み込み済みならtrue、まだならfalseを返します。
	 */
	public boolean isAlreadyRead()
	{
		return alreadyRead;
	}

	/**
	 * サイズを設定します。
	 * @param w 幅
	 * @param h 高さ
	 */
	public void setRectBound(float w, float h)
	{
		mf.setRectBound(w, h);
	}

	/**
	 * スケールを設定します。
	 * @param s スケール
	 */
	public void setScale(float s)
	{
		mf.setScale(s);
	}

	/**
	 * 現在のスケールを取得します。
	 * @return スケール値
	 */
	public float getScale()
	{
		return mf.getScale();
	}

	/**
	 * シンボルのサイズを取得します。
	 * @return サイズ
	 */
	public Dimension getSymbolSize()
	{
		return size;
	}

	/**
	 * molファイルをセットし、描画します。
	 */
	private void set(String id, String name)
	{
		mf = new MolFigure(id, name, mft);
		mf.initialization(this, new Point2D.Float(0, 0), 20);
		mf.setRectBound();
		newDraw(id, true);
		getLayer().addNew(mf, new Point2D.Float(5, 5), 0); // 位置
		Dimension d = getPictureSize();
		setPaperSize(d);
		setBackground(Color.WHITE);
		super.repaint();
	}
}