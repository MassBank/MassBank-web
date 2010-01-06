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
 * 右クリックイベント処理 クラス
 *
 * ver 2.0.8 2010.01.06
 *
 ******************************************************************************/



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import massbank.MassBankCommon;

/**
 * 右クリックイベント処理クラス
 */
public class ShowMass implements MouseListener{

	/**
	 * mouseClicked処理
	 * @param e mouseClickedイベント
	 */
	public void mouseClicked(MouseEvent e) {
		
		// 左クリックの場合
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			
			// ダブルクリックの場合
		    if (e.getClickCount() >= 2){
		    	
				// 選択された全ての値のパスを取得
				TreePath [] paths = BrowsePage.tree.getSelectionPaths();
				if ( paths == null || paths.length > 1 ) {
					return;
				}
				
				// 選択されたスペクトル情報の取得
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[0].getLastPathComponent();
				if ( node instanceof LeafNode ) {
					LeafNode leaf = (LeafNode) node;
					String accs = (leaf.acc != null) ? leaf.acc : "";
					
					try {
						String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
						String reqStr = BrowsePage.baseUrl + MassBankCommon.DISPATCHER_NAME + "?type=" + typeName + "&id=" + accs;
						reqStr += "&site=" + BrowsePage.site;
						URL url = new URL( reqStr );
						BrowsePage.applet.getAppletContext().showDocument( url, "_blank" );
					} catch ( Exception ex ) {
						ex.printStackTrace();
					}
				}
		    }
		
		// 右クリックの場合
		} else if ( SwingUtilities.isRightMouseButton(e) ) {
			
			// 選択された全ての値のパスを取得
			TreePath [] paths = BrowsePage.tree.getSelectionPaths();
			if ( paths == null ) {
				return;
			}
			
			// 選択されたスペクトル情報の取得
			int l = paths.length;
			ArrayList<String> accs = new ArrayList<String>();
			ArrayList<String> names = new ArrayList<String>();
			int num = 0;
			for ( int i = 0; i < l; i ++ ) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
				if ( node instanceof LeafNode ) {
					LeafNode leaf = (LeafNode) node;
					names.add(leaf.name);
					accs.add(leaf.acc);
					num ++;
				}
			}
			
			// 葉ノードが1つも選択されていない場合
			if ( num == 0) {
				return;
			}
			
			// ポップアップメニュー作成
			JPopupMenu popup = new JPopupMenu();
			JMenuItem item1 = new JMenuItem( "Show Record" );
			JMenuItem item2 = new JMenuItem( "Multiple Display" );
			popup.add( item1 );
			popup.add( item2 );
			
			// 選択行が1行の場合
			if ( num == 1 ) {
				item1.addActionListener( new PopupEntryListener(accs.get(0)) );
				item1.setEnabled( true );
				item2.setEnabled( false );
			}
			// 選択行が複数の場合
			else if ( num > 1 ) {
				item2.addActionListener( 
						new PopupMultipleDisplayListener(
								(String[])accs.toArray(new String[accs.size()]), 
								(String[])names.toArray(new String[names.size()])) );
				item1.setEnabled( false );
				item2.setEnabled( true );
			}
			
			// ポップアップメニュー表示
			if ( num > 0 ) {
				popup.show( e.getComponent(), e.getX(), e.getY() );
			}
		}
	}
	
	/**
	 * mousePressed処理
	 * @param e mousePressedイベント
	 */
	public void mousePressed(MouseEvent e) {
	}
	
	/**
	 * mouseReleased処理
	 * @param e mouseReleasedイベント
	 */
	public void mouseReleased(MouseEvent e) {
	}
	
	/**
	 * mouseEntered処理
	 * @param e mouseEnteredイベント
	 */
	public void mouseEntered(MouseEvent e) {
	}
	
	/**
	 * mouseExited処理
	 * @param e mouseExitedイベント
	 */
	public void mouseExited(MouseEvent e) {
	}
	
	/**
	 * ポップアップメニュー "Show Record" 処理
	 */
	private class PopupEntryListener implements ActionListener {
		
		/** ACCESSION */
		private String acc = "";
		
		/**
		 * コンストラクタ
		 * @param acc ACCESSION
		 */
		public PopupEntryListener(String acc) {
			this.acc = acc;
		}
		
		/**
		 * イベント処理
		 * @param e アクションイベント
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
				String reqStr = BrowsePage.baseUrl + MassBankCommon.DISPATCHER_NAME + "?type=" + typeName + "&id=" + acc;
				reqStr += "&site=" + BrowsePage.site;
				URL url = new URL( reqStr );
				BrowsePage.applet.getAppletContext().showDocument( url, "_blank" );
			} catch ( Exception ex ) {
				ex.printStackTrace();
			}
		}

	}
	
	/**
	 * ポップアップメニュー "Multiple Display" 処理
	 */
	private class PopupMultipleDisplayListener implements ActionListener {
		
		/** ACCESSION配列 */
		private String[] accs = null;
		/** RECORD_TITLE配列 */
		private String[] names = null;
		
		/**
		 * コンストラクタ
		 * @param accs ACCESSION配列
		 * @param names RECORD_TITLE配列
		 */
		public PopupMultipleDisplayListener(String[] accs, String[] names) {
			this.accs = accs;
			this.names = names;
		}
		
		/**
		 * イベント処理
		 * @param e アクションイベント
		 */
		public void actionPerformed(ActionEvent e) {

			String reqUrl = BrowsePage.baseUrl + "Display.jsp";
			String param = "";
			for ( int i = 0; i < accs.length; i++ ) {
				String name = names[i];
				String id = accs[i];
				String formula = "";
				String mass = "";
				String site = BrowsePage.site;
				param += "id=" + name + "\t" + id + "\t" + formula + "\t" + mass + "\t" + site + "&";
			}
			param = param.substring( 0, param.length() -1 );

			try {
				URL url = new URL( reqUrl );
				URLConnection con = url.openConnection();
				con.setDoOutput(true);
				PrintStream out = new PrintStream( con.getOutputStream() );
				out.print( param );
				out.close();
				String line;
				String filename = "";
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				boolean isStartSpace = true;
				while ( (line = in.readLine() ) != null ) {
					// 先頭スペースを読み飛ばすため
					if ( line.equals("") ) {
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
					filename += line;
				}
				in.close();

				reqUrl += "?type=Multiple Display&" + "name=" + filename;
				BrowsePage.applet.getAppletContext().showDocument( new URL(reqUrl), "_blank" );

			} catch ( Exception ex ) {
				ex.printStackTrace();
			}
		}
	}
}
