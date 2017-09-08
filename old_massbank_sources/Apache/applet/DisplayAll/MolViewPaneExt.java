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
 * Multiple Spectra Display用 MolViewPane 拡張クラス
 *
 * ver 1.0.0 2009.12.14
 *
 ******************************************************************************/

import javax.swing.JApplet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Dimension;

@SuppressWarnings("serial")
public class MolViewPaneExt extends MolViewPane implements MouseListener
{
	public MolViewPaneExt(String molData, int size, JApplet applet)
	{
		float zoomRate = 0.8f;

		setApplet(applet);
		init();												// 初期化
		read("", "test", molData);							// mol文字列を渡す
		unselectedSymbols();								// 構造の選択をはずす

		int psize = (int)(size / (zoomRate * 1.1));
		int rsize = (int)(psize / 1.1);
			
		// 構造のサイズを指定
		Dimension d = getSymbolSize();						// シンボルのサイズを取得
		if(d.getWidth() > d.getHeight()+5) {
			setRectBound(rsize, 1);							// 幅の方が大きければ、幅を基準に調整
		}
		else {
			setRectBound(1, rsize);							// 高さの方が大きければ、高さを基準に調整
		}
		zoomChangeTo(zoomRate);								// 拡大率の指定
		setPaperSize( new Dimension(psize, psize) );		// 背景のサイズを指定
		addMouseListener(this);

	}
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e){}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e)  {}
	public void mouseDragged(MouseEvent e) {}
}
