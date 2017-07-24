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
 * TreeListener クラス
 *
 * ver 2.0.5 2008.12.05
 *
 ******************************************************************************/

import javax.swing.tree.*;
import javax.swing.event.*;

/**
 * TreeListener クラス
 */
public class MyTreeListener implements TreeWillExpandListener {
	
	/**
	 * ノード展開
	 */
	public void treeWillExpand(TreeExpansionEvent e) {
		TreePath path = e.getPath();
		InterNode node = (InterNode) path.getLastPathComponent();
		if ( node.loaded ) {
			return;
		}
		node.loadSon();
		BrowsePage.treeModel.reload(node);
		
		// ルートノードからの距離を取得
		int lebel = node.getLevel();
		
		// サイトによってツリー表示の方法を切り替える
		for (int i=0; i<BrowsePage.mode.length; i++) {
			if (lebel == Integer.parseInt(BrowsePage.mode[i]) && !node.isLeaf()) {
				// 子ノード展開
				childNodeExpand(node);
			}
		}
	}

	/**
	 * ノード収納
	 */
	public void treeWillCollapse(TreeExpansionEvent e) {
	}
	
	/**
	 * 引数で受け取ったノードの子ノードを展開
	 * @param node 元となるノード
	 */
	private void childNodeExpand(InterNode node) {
		
		// 1つめの子ノードが葉ノードの場合は展開処理を行わない
		if (node.getFirstChild() instanceof InterNode) {
			
			// 1つめの子ノード取得
			InterNode childNode = (InterNode)node.getFirstChild();
			
			for (int i=0; i<node.getChildCount(); i++) {
				
				// 子ノードまでを展開
				BrowsePage.tree.expandPath(new TreePath(childNode.getPath()));
			
				// 次の子ノード取得
				childNode = (InterNode)node.getChildAfter(childNode);
			}
		}
	}
}
