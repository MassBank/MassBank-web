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
 * Renderer クラス
 *
 * ver 2.0.1 2008.12.05
 *
 ******************************************************************************/

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * Renderer クラス
 */
@SuppressWarnings("serial")
public class MyRenderer extends DefaultTreeCellRenderer {

	public Component getTreeCellRendererComponent(JTree tr, Object val, boolean sel, boolean ex, boolean lf, int row, boolean fc) {
		setText(val.toString());
		if ( val instanceof EmptyNode ) {
			setLeafIcon(BrowsePage.EmptyIcon);
		}
		else if ( val instanceof LeafNode ) {
			setLeafIcon(BrowsePage.MassIcon);
		}
		else { // InterNode
			setOpenIcon(BrowsePage.OpenIcon);
			setClosedIcon(BrowsePage.CloseIcon);
			setLeafIcon(BrowsePage.CloseIcon);
		}
		return super.getTreeCellRendererComponent(tr, val, sel, ex, lf, row, fc);
	}
}