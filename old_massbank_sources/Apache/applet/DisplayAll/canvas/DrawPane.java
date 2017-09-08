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
 * DrawPane Class
 *
 * ver 1.0.1 2008.12.05
 *
 ******************************************************************************/

package canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import metabolic.DataRepository;
import metabolic.EnzFigure;
import metabolic.MolFigure;
import metabolic.PathFigure;
import searchPane.BasePane;
import searchPane.ViewerOptions;
import alg.graph.Graph;

@SuppressWarnings("serial")
public class DrawPane extends JPanel 
  {
    protected static DataFlavor objFlavor = new DataFlavor(
        "application/x-java-object", "Object");

    private static final String defaultFileName = "Default";

/*    private static final Cursor HAND1_CURSOR = Toolkit
        .getDefaultToolkit().createCustomCursor(
            searchPane.BasePane.getImageIcon("hand1.gif", "").getImage(),
            new Point(5, 5), "hand1");

    private static final Cursor HAND2_CURSOR = Toolkit
        .getDefaultToolkit().createCustomCursor(
            searchPane.BasePane.getImageIcon("hand2.gif", "").getImage(),
            new Point(5, 5), "hand2");
*/
    private Cursor HAND1_CURSOR = null;
    private Cursor HAND2_CURSOR = null;

    // Grid Parameters
    private static final int IMPORT_XMARGIN = 0;

    private static final int IMPORT_YMARGIN = 0;

    protected static final int BORDER_LIMIT = 10;

    private static final Color gridColor = Color.black;

    private static final BasicStroke gridStroke = new BasicStroke(
        0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
        10.0f, new float[] { 1, 9 }, 0);

    private static final int GRID_STEP = 10;

    private static final int NO_TOLERANCE = 0;

    // If you change these values, make sure to
    // change
    // PopupRepository.CanvasPopup.
    private boolean showGrid = false;

    private boolean snapGrid = false;

    private JPopupMenu canvasPopup = null;

    private PopupRepository.ComponentPopup compPopup = null;

    private PopupRepository.TracePopup tracePopup = null;

    // Default canvas size
    static protected final int OFFSET = 10;

    private int paperWidth = 700;

    private int paperHeight = 500;

    private View.Base parent = null;

    private Layer layer = null;

    private List<Object> layerL = new ArrayList<Object>();

    private JLabel message = null;

    private float zoomScale = 0.8f;

    private JViewport viewport = null;

    private AffineTransform zoomAT = new AffineTransform();

    private AbstractComponent symbolToAdd = null;

    private static List<AbstractComponent> clipboard = new ArrayList<AbstractComponent>();

    private Director director = new Director();

    private EditListener eListener = null;

    // Mouse Information
    private Point fromPos = new Point(0, 0);

    private Point toPos = new Point(0, 0);

    private Point2D.Float logicalFrom, logicalTo;

    private boolean underDrag = false;

    private boolean underScroll = false;

    // The parameter to keep the cursor mode.
    private int modeType = Cursor.DEFAULT_CURSOR;

    // Temporary angle parameters for rotation
    // drags.
    private int tmpAngle = 0, initAngle = 0;

    // Pointed component for showing connector
    // ports.
    protected AbstractComponent pointedSymbol = null;

    private MapOverviewPane sumPanel = null;

    // Graph information
    protected Graph metG = new Graph();

    protected HashMap IDtoNode = new HashMap();

    protected HashMap MaptoEdge = new HashMap();

    protected HashMap Node2Component = new HashMap();

    public void setScrollFlag(boolean b)
      {
        underScroll = b;
      }

    public Dimension getPictureSize()
      {
        return getLayer().getMinimumSize().getSize();
      }

    public Dimension getPaperSize()
      {
        return new Dimension(paperWidth, paperHeight);
      }

    public void setPaperSize(Dimension d)
      { // adjust papersize to the minimum are
        // required to show contents
        paperWidth = d.width;
        paperHeight = d.height;
        d.width = (int) (d.width * zoomScale) + 2 * OFFSET;
        d.height = (int) (d.height * zoomScale) + 2
            * OFFSET;
        setPreferredSize(d);
      }

    protected void rescalePaperSize()
      {
        Rectangle r = viewport.getViewRect();
        float scale = 100f * (r.width - 2 * OFFSET)
            / paperWidth - 1;
        if (scale < 50)
          scale = 50;
        zoomSpinner.setValue(new Double(scale / 100));
        zoomChangeTo(scale / 100);
      }

    protected void resizeViewFrame()
      {
        parent.resize();
      }

    protected int getModeType()
      {
        return modeType;
      }

    protected void setModeType(int i)
      {
        modeType = i;
        switch (i) {
        case ActionRepository.TRACE:
          setCursor(HAND1_CURSOR);
          break;
        default:
          setCursor(Cursor.getPredefinedCursor(i));
          break;
        }
      }

    private ViewerOptions drawOptions = new ViewerOptions();

    protected static boolean isResizeCursor(int cursor)
      {
        if ((cursor == Cursor.N_RESIZE_CURSOR)
            || (cursor == Cursor.E_RESIZE_CURSOR)
            || (cursor == Cursor.S_RESIZE_CURSOR)
            || (cursor == Cursor.W_RESIZE_CURSOR)
            || (cursor == Cursor.NE_RESIZE_CURSOR)
            || (cursor == Cursor.NW_RESIZE_CURSOR)
            || (cursor == Cursor.SE_RESIZE_CURSOR)
            || (cursor == Cursor.SW_RESIZE_CURSOR)
            || (cursor == Cursor.CROSSHAIR_CURSOR))
          return true;
        else
          return false;
      }

    public DrawPane(View.Base p, JLabel msg)
      {
        parent = p;
        message = msg;
//        eListener = new EditListener(this);
//        addMouseListener(eListener);
//        addMouseMotionListener(eListener);
        addKeyListener(eListener);
        zoomChangeTo(zoomScale);
        // ToolTipManager ttm =
        // ToolTipManager.sharedInstance();
        // ttm.setInitialDelay(100);

        layer = new Layer(defaultFileName, this);
        layerL.add(layer);

        // setup director
        director.setup(clipboard, layer);
        //director.addSelectedSymbolSensitiveDialogs(dialogs);
        director.clipboardChanged();
      }
    
    protected void setViewport(JViewport vp)
      {
        viewport = vp;
      }
    
    public javax.swing.JFrame getFrame()
      {
        return parent;
      }

    // EDIT OPERATIONS USED IN ACTIONREPOSITORY

    protected void copySelectedSymbols()
      {
        clipboard.clear();
        clipboard.addAll(layer.getSelected());
        director.clipboardChanged();
      }

    protected void deleteSelectedSymbols()
      {
        layer.setModified(true);
        layer.removeSelected();
        director.selectedSymbolsChanged();
        repaint();
      }

    protected AbstractComponent groupSymbols(
        List<AbstractComponent> L)
      {
        layer.setModified(true);
        if (L == null)
          L = layer.getSelected();
        AbstractComponent ac = layer.group(L);
        director.selectedSymbolsChanged();
        repaint();
        return ac;
      }

    protected List ungroupSymbols(List<AbstractComponent> L)
      {
        layer.setModified(true);
        if (L == null)
          L = layer.getSelected();
        List ret = layer.ungroup(L);
        director.selectedSymbolsChanged();
        repaint();
        return ret;
      }

    protected List selectAllSymbols()
      {
        if (modeType == ActionRepository.TRACE)
          return null;
        List l = layer.selectAll();
        director.selectedSymbolsChanged();
        return l;
      }

    protected void unselectAllSymbols()
      {
        layer.unselectAll();
      }

    protected List<AbstractComponent> pasteSymbols(List s)
      {
        layer.setModified(true);
        List<AbstractComponent> L = layer.paste(s,
            GRID_STEP);
        setClipboard(L);
        director.selectedSymbolsChanged();
        return L;
      }

    protected void bringToFront()
      {
        layer.setModified(true);
        layer.reorderSelected(Integer.MAX_VALUE);
        repaint();
      }

    protected void sendToBack()
      {
        layer.setModified(true);
        layer.reorderSelected(Integer.MIN_VALUE);
        repaint();
      }

    protected void bringForward()
      {
        layer.setModified(true);
        layer.reorderSelected(1);
        repaint();
      }

    protected void sendBackward()
      {
        layer.setModified(true);
        layer.reorderSelected(-1);
        repaint();
      }

    protected void flipHorizontal()
      {
        layer.setModified(true);
        layer.rotateSelected(0, true);
        repaint();
      }

    protected void flipVertical()
      {
        layer.setModified(true);
        layer.rotateSelected(180, true);
        repaint();
      }

    protected void rotateClock()
      {
        layer.setModified(true);
        layer.rotateSelected(90, false);
        repaint();
      }

    protected void rotateAntiClock()
      {
        layer.setModified(true);
        layer.rotateSelected(-90, false);
        repaint();
      }

    protected void alignSelectedVertically(int position)
      {
        layer.setModified(true);
        layer.alignSelected(position, true);
        repaint();
      }

    protected void alignSelectedHorizontally(int position)
      {
        layer.setModified(true);
        layer.alignSelected(position, false);
        repaint();
      }

    protected void changeSelectedFonts(float siz, int face)
      {
        layer.setModified(true);
        layer.changeFontSelected(siz, face);
        repaint();
      }

    protected void defaultSelection()
      {
        symbolToAdd = null;
        setModeType(Cursor.DEFAULT_CURSOR);
      }

    protected List<AbstractComponent> selectedSymbols()
      {
        return layer.getSelected();
      }

    public float getZoomScale()
      {
        return zoomScale;
      }

    public void zoomChangeTo(float val)
      {
        zoomScale = val;
        Dimension viewSize = new Dimension(
            (int) (paperWidth * zoomScale + 2 * OFFSET),
            (int) (paperHeight * zoomScale + 2 * OFFSET));
        setPreferredSize(viewSize);
        zoomAT.setToScale(zoomScale, zoomScale);
        zoomAT.translate(OFFSET / zoomScale, OFFSET
            / zoomScale);
        if (viewport != null)
          { // adjust to the focused symbol
            Rectangle R = viewport.getViewRect();
            AbstractComponent ac = layer.getFocused();
            if (ac != null)
              {
                Rectangle2D.Float r = ac.getRectBound();
                int x = (int) (r.x * zoomScale);
                int y = (int) (r.y * zoomScale);
                int height = (int) (r.height * zoomScale);
                int width = (int) (r.width * zoomScale);
                if ((y < R.y) || (x < R.x))
                  viewport.setViewPosition(new Point(x, y));
                else if ((y + height > R.y + R.height)
                    || (x + width > R.x + R.width))
                  {
                    viewport.setViewPosition(new Point(
                        (int) (x + width + OFFSET
                            * zoomScale - R.width),
                        (int) (y + height + OFFSET
                            * zoomScale - R.height)));
                  }
              }
            viewport.repaint();
          }
        revalidate();
      }

    public void paintComponent(Graphics g)
      {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(drawOptions
            .getRenderingHints());

        // scaling
        AffineTransform at = g2.getTransform();
        at.concatenate(zoomAT);
        g2.setTransform(at);

        Rectangle viewRect = null;
        if (viewport == null)
          viewRect = new Rectangle(0, 0, paperWidth,
              paperHeight);
        else
          {
            viewRect = viewport.getViewRect();
            viewRect.x -= OFFSET;
            viewRect.y -= OFFSET;
            viewRect.x /= zoomScale;
            viewRect.y /= zoomScale;
            viewRect.width /= zoomScale;
            viewRect.height /= zoomScale;
          }
        g2.setColor(Color.white);
        g2.fillRect(Math.max(0, viewRect.x), Math.max(0,
            viewRect.y), Math.min(paperWidth,
            viewRect.width), Math.min(paperHeight,
            viewRect.height));
        g2.setColor(Color.black);
        g2.drawRect(0, 0, paperWidth, paperHeight);
        // Shadow x axis
        if (viewRect.y + viewRect.height > paperHeight)
          g2.fillRect(Math.max(5, viewRect.x), paperHeight,
              Math.min(paperWidth, viewRect.width
                  + 5
                  - Math.max(0, viewRect.width + viewRect.x
                      - paperWidth)), 5);
        // Shadow y axis
        if (viewRect.x + viewRect.width > paperWidth)
          g2.fillRect(paperWidth, Math.max(5, viewRect.y),
              5, Math.min(paperHeight, viewRect.height
                  + 5
                  - Math.max(0, viewRect.height
                      + viewRect.y - paperHeight)));

        if (showGrid)
          {
            g2.setColor(gridColor);
            g2.setStroke(gridStroke);
            for (int x = 4 * GRID_STEP; (x < paperWidth)
                && (x <= viewRect.x + viewRect.width); x += 4 * GRID_STEP)
              {
                if (viewRect.x <= x)
                  g2.drawLine(x, 1, x, paperHeight - 1);
              }
            for (int y = 4 * GRID_STEP; (y < paperHeight)
                && (y <= viewRect.y + viewRect.height); y += 4 * GRID_STEP)
              if (viewRect.y <= y)
                g2.drawLine(1, y, paperWidth - 1, y);
          }
        layer.draw(g2, viewRect, underScroll);
      }

    // MOUSE OPERATIONS
    private Point2D.Float getLogicalAddr(Point p)
      {
        return new Point2D.Float(
            (p.x - OFFSET) / zoomScale, (p.y - OFFSET)
                / zoomScale);
      }

    private boolean isPopupTrigger(MouseEvent e)
      {
        if (!e.isPopupTrigger())
          return false;

        if (modeType == Cursor.CROSSHAIR_CURSOR)
          {
            symbolToAdd = null;
            layer.getFocused().setEditing(false);
            setModeType(Cursor.DEFAULT_CURSOR);
          }
        else
          {
            if (pointedSymbol != null)
              {
                if (modeType == ActionRepository.TRACE)
                  {
                    tracePopup.init(pointedSymbol, this);
                    tracePopup.show(this, e.getX(), e
                        .getY());
                  }
                else
                  {
                    compPopup.init(pointedSymbol, this);
                    compPopup
                        .show(this, e.getX(), e.getY());
                  }
              }
            else
              canvasPopup.show(this, e.getX(), e.getY());
          }
        return true;
      }

    class EditListener extends MouseAdapter implements
        MouseMotionListener, KeyListener,
        DropTargetListener
      {
        DropTarget dropTarget = null;

        public EditListener(DrawPane d)
          {
            dropTarget = new DropTarget(d, this);
          }

        public void mouseClicked(MouseEvent e)
          {
            if (layer.getFocused() == null)
              return;
            if (modeType == ActionRepository.TRACE)
              return;
            if (e.getClickCount() > 1)
              {
                layer.getFocused().doubleClickProcess();
                symbolToAdd = null;
                setModeType(Cursor.DEFAULT_CURSOR);
              }
          }

        public void mousePressed(MouseEvent e)
          {
            grabFocus();
            if (isPopupTrigger(e))
              return;
            if (e.getClickCount() > 1)
              return;
            fromPos = toPos = e.getPoint();
            logicalFrom = logicalTo = getLogicalAddr(fromPos);
            if (modeType == ActionRepository.TRACE)
              {
                mousePressedInTrace(e);
                return;
              }
            if (modeType == Cursor.CROSSHAIR_CURSOR)
              {
                // adding new symbols
                layer.addNew(symbolToAdd, logicalTo,
                    GRID_STEP);
                director.selectedSymbolsChanged();
                return;
              }
            tmpAngle = 0;
            if ((modeType == Cursor.HAND_CURSOR)
                && layer.getFocused() != null)
              {
                initAngle = (360 - layer
                    .getFocused()
                    .calcAngle(logicalFrom.x, logicalFrom.y)) % 360;
                return;
              }
            layer.setFocused(pointedSymbol);

            if (!isResizeCursor(modeType))
              {
                if (!layer.isSelected(pointedSymbol))
                  {
                    if (!e.isShiftDown())
                      layer.unselectAll();
                    if (pointedSymbol != null)
                      {
                        layer.addSelected(pointedSymbol);
                        setModeType(pointedSymbol
                            .processMouseEvent(e,
                                logicalFrom, logicalTo));
                      }
                  }
                else if (e.isShiftDown())
                  layer.removeFromSelected(pointedSymbol);
                director.selectedSymbolsChanged();
              }
          }

        private void limitTranslation(AbstractComponent ac)
          {
            Rectangle2D.Float rect = ac.getRectBound();
            if (BORDER_LIMIT >= rect.x + rect.width)
              ac.translateLocation(-rect.x - rect.width
                  + BORDER_LIMIT, 0);
            if (BORDER_LIMIT >= rect.y + rect.height)
              ac.translateLocation(0, -rect.y - rect.height
                  + BORDER_LIMIT);
            if (paperWidth - BORDER_LIMIT <= rect.x)
              ac.translateLocation(-rect.x + paperWidth
                  - BORDER_LIMIT, 0);
            if (paperHeight - BORDER_LIMIT <= rect.y)
              ac.translateLocation(0, -rect.y + paperHeight
                  - BORDER_LIMIT);
          }

        public void mouseReleased(MouseEvent e)
          {
            if (isPopupTrigger(e))
              return;
            if (modeType == ActionRepository.TRACE)
              {
                if (getCursor() == HAND2_CURSOR)
                  setCursor(HAND1_CURSOR);
                return;
              }

            if (underDrag)
              {
                underDrag = false;
                if (modeType == Cursor.DEFAULT_CURSOR)
                  {// End of Selection
                    if (!e.isShiftDown())
                      layer.unselectAll();
                    layer.selectRegion(logicalFrom,
                        logicalTo, NO_TOLERANCE);
                    director.selectedSymbolsChanged();
                  }
                else
                  {
                    store();
                    if (modeType == Cursor.MOVE_CURSOR)
                      {
                        layer.moveSelected(logicalTo.x
                            - logicalFrom.x, logicalTo.y
                            - logicalFrom.y,
                            snapGrid ? GRID_STEP : 0);

                        List<AbstractComponent> selected = layer
                            .getSelected();
                        for (int i = 0; i < selected.size(); i++)
                          {
                            AbstractComponent ac = selected
                                .get(i);
                            limitTranslation(ac);
                            ac = ac.getChain();
                            if ((ac != null)
                                && !(ac instanceof TextRepository.ArmName)
                                && !selected.contains(ac))
                              limitTranslation(ac);
                          }
                      }
                    else if (modeType == Cursor.HAND_CURSOR)
                      {
                        layer.rotateSelected(tmpAngle,
                            false);
                      }
                    else if (isResizeCursor(modeType))
                      {
                        layer.resizeSelected(modeType);
                      }

                  }
              }

            if (modeType == Cursor.CROSSHAIR_CURSOR)
              {
                if ((symbolToAdd != null)
                    && symbolToAdd.isEditing())
                  {
                    store();
                    symbolToAdd = symbolToAdd
                        .creationEndProcess();
                    if (symbolToAdd == null)
                      setModeType(Cursor.DEFAULT_CURSOR);
                  }
              }
            repaint();
            director.updateSummaryPanel();
          }

        private AbstractComponent getPointedSymbol(
            AbstractComponent draggingSymbol)
          {
            logicalTo = getLogicalAddr(toPos);
            AbstractComponent ac = layer
                .getSymbolAt(logicalTo);
            setToolTipText((ac == null) ? null : ac
                .getToolTipText());
            // no object to add or under focus
            if (draggingSymbol == null)
              return ac;

            if (draggingSymbol instanceof LineRepository.ConnectorBase)
              { // Focused symbol is a connector.
                // Show target of the dragged
                // connector.
                if ((ac != null) && (ac != draggingSymbol))
                  {
                    logicalTo = ac.getNearestPosition(
                        logicalFrom, 0);
                    // logicalTo = ac
                    // .getNearestRectBound(logicalTo);
                  }
                if (ac != null)
                  {
                    Graphics2D g2 = (Graphics2D) getGraphics();
                    AffineTransform at = g2.getTransform();
                    at.concatenate(zoomAT);
                    g2.setTransform(at);
                    ac.highlightBoundary(g2);
                  }
              }
            return ac;
          }

        public void mouseDragged(MouseEvent e)
          {
            if (modeType == ActionRepository.TRACE)
              {
                Point vp = viewport.getViewPosition();
                vp.x = e.getX() - vp.x;
                vp.y = e.getY() - vp.y;
                parent.scroll(toPos.x - vp.x, toPos.y
                    - vp.y);
                toPos = vp;
                return;
              }
            // First, erase the previous RubberBand,
            // and then set the new
            // toPos.
            underDrag = true;
            drawRubberBand();
            toPos = e.getPoint();
            AbstractComponent ac = layer.getFocused();
            pointedSymbol = getPointedSymbol(ac);
            if (modeType == Cursor.HAND_CURSOR)
              tmpAngle = (initAngle + ac.calcAngle(
                  logicalTo.x, logicalTo.y)) % 360;
            if (viewport != null)
              {
                Dimension d = viewport.getExtentSize();
                Point p = viewport.getViewPosition();
                int step = 20;
                if (toPos.x - p.x > d.width - BORDER_LIMIT)
                  {
                    if (toPos.x - p.x > d.width)
                      step *= 2;
                    parent.scroll(step, 0);
                  }
                else if (toPos.x - p.x < BORDER_LIMIT)
                  {
                    if (toPos.x < p.x)
                      step *= 2;
                    parent.scroll(-step, 0);
                  }
                if (toPos.y - p.y > d.height - BORDER_LIMIT)
                  {
                    if (toPos.y - p.y > d.height)
                      step *= 2;
                    parent.scroll(0, step);
                  }
                else if (toPos.y - p.y < BORDER_LIMIT)
                  {
                    if (toPos.y < p.y)
                      step *= 2;
                    parent.scroll(0, -step);
                  }
              }
            drawRubberBand();
          }

        public void mouseMoved(MouseEvent e)
          {
            if (modeType == ActionRepository.TRACE)
              {
                mouseMovedInTrace(e);
                return;
              }
            if ((layer.getFocused() != null)
                && layer.getFocused().isEditing())
              drawRubberBand();
            toPos = e.getPoint();
            pointedSymbol = getPointedSymbol(symbolToAdd);
            if ((layer.getFocused() != null)
                && layer.getFocused().isEditing())
              drawRubberBand();
            if (symbolToAdd != null)
              return;
            if (modeType == Cursor.CROSSHAIR_CURSOR)
              return;
            List<AbstractComponent> selected = layer
                .getSelected();
            for (int i = 0; i < selected.size(); i++)
              {
                AbstractComponent ac = selected.get(i);
                int type = ac.processMouseEvent(e,
                    logicalFrom, logicalTo);
                if (type == Cursor.DEFAULT_CURSOR)
                  continue;
                setModeType(type);
                pointedSymbol = ac;
                if (type == Cursor.HAND_CURSOR)
                  layer.setFocused(ac);
                else if (isResizeCursor(type))
                  setCursor(Cursor.getPredefinedCursor(ac
                      .getResizeDirection(logicalTo.x,
                          logicalTo.y)));
                return;
              }
            setModeType(Cursor.DEFAULT_CURSOR);
          }

        private void drawRubberBand()
          {
            Graphics2D g2 = (Graphics2D) getGraphics();
            g2.setXORMode(Color.white);
            g2.setColor(AbstractComponent.defaultCtrlColor);
            g2
                .setStroke(AbstractComponent.defaultCtrlStroke);
            if (modeType == Cursor.DEFAULT_CURSOR)
              {// rubberband
                int xpos = Math.min(fromPos.x, toPos.x);
                int ypos = Math.min(fromPos.y, toPos.y);
                int width = Math.abs(fromPos.x - toPos.x);
                int height = Math.abs(fromPos.y - toPos.y);
                if (height == 0)
                  g2.drawLine(xpos, ypos, xpos + width,
                      ypos);
                else if (width == 0)
                  g2.drawLine(xpos, ypos, xpos, ypos
                      + height);
                else
                  g2.drawRect(xpos, ypos, width, height);
              }
            else
              {// resizing objects or adding objects
                AffineTransform at = g2.getTransform();
                at.concatenate(zoomAT);
                g2.setTransform(at);
                layer.drawTemporaryBoundary(g2,
                    logicalFrom, logicalTo, tmpAngle,
                    modeType, snapGrid ? GRID_STEP : 0);
              }
            g2.setPaintMode();
          }

        public void keyTyped(KeyEvent e)
          {}

        public void keyReleased(KeyEvent e)
          {}

        public void keyPressed(KeyEvent e)
          {
            if (modeType == ActionRepository.TRACE)
              return;

            switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
              layer.moveSelected(
                  e.isShiftDown() ? GRID_STEP : 1, 0, 0);
              break;

            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
              layer.moveSelected(
                  e.isShiftDown() ? -GRID_STEP : -1, 0, 0);
              break;

            case KeyEvent.VK_UP:
            case KeyEvent.VK_KP_UP:
              layer.moveSelected(0,
                  e.isShiftDown() ? -GRID_STEP : -1, 0);
              break;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_KP_DOWN:
              layer.moveSelected(0,
                  e.isShiftDown() ? GRID_STEP : 1, 0);
              break;

            case KeyEvent.VK_PAGE_DOWN:
              layer.rotateSelected(e.isShiftDown() ? 30
                  : 10, false);
              break;

            case KeyEvent.VK_PAGE_UP:
              layer.rotateSelected(e.isShiftDown() ? -30
                  : -10, false);
              break;
            }
            repaint();
          }

        // DRAG & DROP
        public void dragEnter(DropTargetDragEvent e)
          {
            e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
          }

        public void dragOver(DropTargetDragEvent e)
          {}

        public void dragExit(DropTargetEvent e)
          {}

        public void dropActionChanged(DropTargetDragEvent e)
          {}

        public synchronized void drop(
            DropTargetDropEvent event)
          {
            try
              {
                if ((event.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) == 0)
                  {
                    event.rejectDrop();
                  }
                else
                  {
                    event
                        .acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable transferable = event
                        .getTransferable();
                    Point2D.Float p = getLogicalAddr(event
                        .getLocation());
                    layer.unselectAll();
                    if (transferable
                        .isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                      { // DnD from Windows
                        List fileList = (List) transferable
                            .getTransferData(DataFlavor.javaFileListFlavor);
                        for (Iterator I = fileList
                            .iterator(); I.hasNext();)
                          {
                            File f = (File) I.next();
                            String fname = f.getName();
                            if (fname.endsWith(".mol"))
                              // remove ".mol"
                              fname = fname.substring(0,
                                  fname.length() - 4);
                            String moldir = "file:///"
                                + f.getParent() + "/";
                            short idx = (short) doctype.AbstractDoc.urlList
                                .indexOf(moldir);
                            if (idx < 0)
                              {
                                doctype.AbstractDoc.urlList
                                    .add(moldir);
                                idx = (short) (doctype.AbstractDoc.urlList
                                    .size() - 1);
                              }
                            DataRepository.Base d = new DataRepository.MolData(
                                -1, fname, fname, 0, idx,
                                idx);
                            importArmData(
                                d,
                                new Point2D.Float(p.x, p.y),
                                false);
                            p.x += 20;
                            p.y += 20;
                          }
                      }
                    else
                      { // DnD within ARM
                        Object s = (Object) transferable
                            .getTransferData(DrawPane.objFlavor);
                        importArmData(
                            (DataRepository.Base) s,
                            new Point2D.Float(p.x, p.y),
                            true);
                      }
                    director.selectedSymbolsChanged();
                    repaint();
                    event.getDropTargetContext()
                        .dropComplete(true);
                  }
              }
            catch (Exception exception)
              {
                exception.printStackTrace();
                System.err.println("Exception"
                    + exception.getMessage());
                event.rejectDrop();
              }
            grabFocus();
          }

        // Trace Listener
        AbstractComponent pinnedMol = null;

        int pinnedPos = -1;

        int tracePos = -1;

        private void paintEdge(LineRepository.EnzArrow ea)
          {
            ea.setHighlight(ea.isHighlighted() ^ true);
            Graphics2D g2 = (Graphics2D) getGraphics();
            AffineTransform at = g2.getTransform();
            at.concatenate(zoomAT);
            g2.setTransform(at);
            // g2.setXORMode(Color.white);
            // MolFigure mf = (MolFigure)ea.getSource();
            // map.AtomMap M = ea.getMap();
            // mf.drawMapNumberings(g2, M);
            // mf = (MolFigure)ea.getTarget();
            // mf.drawMapNumberings(g2, M);
            // g2.setPaintMode();
          }

        private void mousePressedInTrace(MouseEvent e)
          {
            Point vp = viewport.getViewPosition();
            toPos.x = e.getX() - vp.x;
            toPos.y = e.getY() - vp.y;

            setCursor(HAND2_CURSOR);
            if (pointedSymbol == null)
              return;
            pointedSymbol.tracingProcess();
            if (pointedSymbol instanceof MolFigure)
              {
                MolFigure mf = (MolFigure) pointedSymbol;
                BitSet bs = mf.getHighlights();
                try
                  {
                    if ((pinnedMol != null)
                        && (pinnedMol != mf)
                        && (tracePos != -1)
                        && bs.get(tracePos))
                      {
                        layer.showTraceRoute(pinnedMol,
                            pinnedPos, mf, tracePos, true);
                        return;
                      }
                  }
                catch (InterruptedException ie)
                  {}
                tracePosition(mf, tracePos, (e == null)
                    || !e.isShiftDown());
                pinnedMol = mf;
                pinnedPos = tracePos;
              }
          }

        private void mouseMovedInTrace(MouseEvent e)
          {
            logicalTo = getLogicalAddr(e.getPoint());
            AbstractComponent ac = layer
                .getSymbolAt(logicalTo);

            if (ac instanceof MolFigure)
              {
                MolFigure mf = (MolFigure) ac;
                int pos = mf.getPositionToHighlight(
                    logicalTo, false);
                if (pos >= 0)
                  setCursor(Cursor
                      .getPredefinedCursor(Cursor.HAND_CURSOR));
                else
                  setCursor(HAND1_CURSOR);
                if (pos != tracePos)
                  {
                    Graphics2D g2 = (Graphics2D) getGraphics();
                    AffineTransform at = g2.getTransform();
                    at.concatenate(zoomAT);
                    g2.setTransform(at);
                    g2.setXORMode(Color.white);
                    g2
                        .setColor(AbstractComponent.defaultCtrlColor);
                    g2
                        .setStroke(AbstractComponent.defaultCtrlStroke);
                    if (pos == -1)
                      mf.highlightPosition(g2, tracePos);
                    else
                      mf.highlightPosition(g2, pos);
                    g2.setPaintMode();
                    tracePos = pos;
                  }
              }
            else if (pointedSymbol != ac)
              {
                if (ac instanceof LineRepository.EnzArrow)
                  {// exiting
                    // LineRepository.EnzArrow ea =
                    // (LineRepository.EnzArrow) ac;
                    // if (ea.isHighlighted())
                    // paintEdge(ea);
                  }
                else if (pointedSymbol instanceof LineRepository.EnzArrow)
                  {// entering
                    // LineRepository.EnzArrow ea =
                    // (LineRepository.EnzArrow)
                    // pointedSymbol;
                    // if (!ea.isHighlighted())
                    // paintEdge(ea);
                  }
              }
            if ((ac == null) || (ac != pointedSymbol))
              {
                setToolTipText(null);
                tracePos = -1;
                pointedSymbol = ac;
                return;
              }
          }
      }; // -- EditListener

    protected void tracePosition(MolFigure mf, int trace,
        boolean clear)
      {
        if (clear)
          layer.clearTracePositions();
        layer.propagateTracePosition(mf, trace, true);
        repaint();
      }

    public void setMessage(Object o)
      {
        String str = "Title: \"" + layer.getLayerFileName()
            + "\"  " + o;
        message.setText(str);
      }

    protected List<AbstractComponent> getClipboard()
      {
        return clipboard;
      }

    protected void setClipboard(List<AbstractComponent> c)
      {
        clipboard.clear();
        clipboard.addAll(c);
        director.clipboardChanged();
      }

    protected void setShowGrid(boolean b)
      {
        showGrid = b;
      }

    protected void setSnapGrid(boolean b)
      {
        snapGrid = b;
      }

    protected boolean getShowGrid()
      {
        return showGrid;
      }

    protected boolean getSnapGrid()
      {
        return snapGrid;
      }

    protected void setDirector(Director d)
      {
        director = d;
      }

    protected void setSymbolToAdd(AbstractComponent as)
      {
        symbolToAdd = as;
        setModeType((as != null) ? Cursor.CROSSHAIR_CURSOR
            : Cursor.DEFAULT_CURSOR);
      }

    protected void showSelectedSymbolProperties()
      {
        PropertiesDialog propDialog = new PropertiesDialog(
            parent, this);
        if (layer.getSelected().size() > 1)
          {
            propDialog.enableTab(PropertiesDialog.SIZETAB,
                false);
            propDialog.enableTab(PropertiesDialog.TEXTTAB,
                false);
            for (Iterator<AbstractComponent> i = layer
                .getSelected().iterator(); i.hasNext();)
              {
                AbstractComponent ac = i.next();
                ac.exportProperties(propDialog);
              }
          }
        else
          {
            layer.getFocused().exportProperties(propDialog);
          }
        propDialog.setLocation(new Point((int) toPos.x,
            (int) toPos.y));
        propDialog.pack();
        propDialog.setVisible(true);
      }

    protected void applyPropertyChanges(PropertiesDialog pd)
      {
        for (Iterator<AbstractComponent> i = layer
            .getSelected().iterator(); i.hasNext();)
          {
            AbstractComponent ac = i.next();
            ac.importProperties(pd);
            ac.setRectBound(ac.rectBound.width,
                ac.rectBound.height);
          }
        repaint();
      }

    protected void restorePropertyChanges()
      {
        for (Iterator<AbstractComponent> i = layer
            .getSelected().iterator(); i.hasNext();)
          {
            AbstractComponent ac = i.next();
            ac.restoreProperties();
          }
        repaint();
      }

    private boolean askUserToSave(Layer l)
      {
        if (layer.isModified())
          {
            int userSays = JOptionPane.showConfirmDialog(
                parent, "Save " + l.getLayerTitle()
                    + " before exiting ?",
                "Exits from the program",
                JOptionPane.YES_NO_CANCEL_OPTION);
            if (userSays == JOptionPane.CANCEL_OPTION)
              return false;
            else if (userSays == JOptionPane.YES_OPTION)
              save(l.getLayerFileName(), "txt");
          }
        return true;
      }

    public void exit()
      {
        for (int pos = 0; pos < layerL.size(); pos++)
          {
            if (!askUserToSave((Layer) layerL.get(pos)))
              return;
          }
        System.exit(0);
      }

    public void save()
      {
        save(layer.getLayerFileName(), "txt");
        layer.setModified(false);
      }

    public void save(String fileName, String ext)
      {
        if (!fileName.endsWith(ext))
          fileName += "." + ext;
        layer.setLayerFileName(fileName);
        try
          {
            if (ext.equals("txt"))
              {
                // Label the id number for each
                // component.
                Map<AbstractComponent, Integer> M = new HashMap<AbstractComponent, Integer>();
                for (Iterator<AbstractComponent> I = layer
                    .getAll().iterator(); I.hasNext();)
                  I.next().makeIntLabel(M);

                PrintWriter pw = new PrintWriter(
                    new FileOutputStream(new File(fileName)));
                pw.print("ARM Version "
                    + BasePane.programVersion
                    + "\nComponentSize\t"
                    + layer.getAll().size() + "\n");
                for (Iterator I = layer.getAll().iterator(); I
                    .hasNext();)
                  ((AbstractComponent) I.next())
                      .writeDataFormat(pw, M);
                pw.close();
              }
            else
              {
                BufferedImage img;
                Graphics2D g;
                // Determine the image size
                Rectangle R = layer.getMinimumSize();
                if (ext.equals("jpg") || ext.equals("jpeg"))
                  {
                    img = new BufferedImage(R.width,
                        R.height,
                        BufferedImage.TYPE_INT_RGB);
                    g = (Graphics2D) img.getGraphics();
                    g.setColor(Color.white);
                    g.fillRect(0, 0, R.width, R.height);
                  }
                else
                  // png
                  {
                    img = new BufferedImage(R.width,
                        R.height,
                        BufferedImage.TYPE_INT_ARGB);
                    g = (Graphics2D) img.getGraphics();
                  }
                g.setRenderingHints(drawOptions
                    .getRenderingHints());
                layer.selectAll();
                layer.moveSelected(-R.x, -R.y, 0);
                layer.unselectAll();
                layer.draw(g, null, false);
                layer.selectAll();
                layer.moveSelected(R.x, R.y, 0);
                layer.unselectAll();
                ImageIO.write(img, ext, new File(fileName));
                g.dispose();
              }
            setMessage("Saved as " + fileName);
          }
        catch (Exception exc)
          {
            setMessage("Saving failed: " + exc);
            exc.printStackTrace();
          }
        director.selectedSymbolsChanged();
        repaint();
      }

    public void open(String fileName, boolean isLatest,
        boolean askUserToLoad)
      {
        // Check if pathway mode is chosen.
        if (doctype.PathDoc.metG.numberOfNodes() == 0)
          {
            JOptionPane
                .showMessageDialog(parent,
                    "Choose Pathway Search Mode before reading the file.");
            return;
          }

        // Check if the file is already read.
        boolean overwriting = false;
        for (int i = 0; i < layerL.size(); i++)
          {
            String name = ((Layer) layerL.get(i))
                .getLayerFileName();
            if (name.equals(fileName))
              {
                setLayer((Layer) layerL.get(i));
                if (!askUserToLoad)
                  return;
                int userSays = JOptionPane
                    .showConfirmDialog(parent,
                        "Overwrite the layer under edit?",
                        "Reloading file",
                        JOptionPane.YES_NO_OPTION);
                if (userSays == JOptionPane.NO_OPTION)
                  return;
                else if (userSays == JOptionPane.YES_OPTION)
                  {
                    overwriting = true;
                    break;
                  }
              }
          }
        setCursor(Cursor
            .getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (!overwriting)
          newDraw(fileName, false);

        // Read the data file.
        try
          {
            if (fileName.endsWith(".txt"))
              {
                Map<Integer, AbstractComponent> M = new HashMap<Integer, AbstractComponent>();
                BufferedReader br = new BufferedReader(
                    new FileReader(new File(fileName)));
                List<AbstractComponent> L = readComponents(
                    br, M);
                br.close();
                for (Iterator<AbstractComponent> I = M
                    .values().iterator(); I.hasNext();)
                  I.next().updateLinksByMap(M, false);

                for (Iterator<AbstractComponent> I = M
                    .values().iterator(); I.hasNext();)
                  {
                    AbstractComponent o = I.next();
                    if (o instanceof LineRepository.EnzArrow)
                      {
                        if (((LineRepository.EnzArrow) o)
                            .getText() != null)
                          {
                            String txt = ((LineRepository.EnzArrow) o)
                                .getText();
                            DataRepository.Base dat = ((LineRepository.EnzArrow) o).data;
                            if (dat == null)
                              {
                                System.out
                                    .println("no data for "
                                        + o);
                              }
                            else
                              {
                                ((LineRepository.EnzArrow) o)
                                    .setText(null);
                                TextRepository.ArmName an = dat
                                    .getTextComponent(null);
                                an.initialization(this, o
                                    .getLocation());
                                an.setText(txt);
                                an.setRectBound(60, 0);
                                o.setChain(an);
                                an.setChain(o);
                                L.add(an);
                              }
                          }
                      }
                  }
                layer.setAll(L);
              }
            layer.assignAllGenes();
            setMessage("loaded.");
          }
        catch (Exception exc)
          {
            setMessage("Loading failed: " + exc);
            exc.printStackTrace();
          }
        UndoStack.clear();
        repaint();
        store();
        if (sumPanel != null)
          sumPanel.selectLayer(null);
        director.selectedSymbolsChanged();
        setCursor(Cursor.getDefaultCursor());
      }

    public void close()
      {
        if (!askUserToSave(layer))
          return;
        symbolToAdd = null;
        // Remove a set of elements (layer) from the
        // network.
        layerL.remove(layer);
        if (layerL.size() > 0)
          layer = (Layer) layerL.get(0);
        else
          {
            layer = new Layer(defaultFileName, this);
            layerL.add(layer);
          }
        director.setup(clipboard, layer);
        director.selectedSymbolsChanged();
        repaint();
        if (sumPanel != null)
          sumPanel.selectLayer(null);
      }

    public void print(Graphics2D g2)
      {
        layer.draw(g2, new Rectangle(0, 0, paperWidth,
            paperHeight), false);
      }

    public void newDraw(String name, boolean preview)
      {
        if (layer.getLayerFileName() != null
            && layer.getLayerFileName().equals(
                defaultFileName)
            && layer.getAll().size() == 0)
          layerL.remove(layer);
        layer = new Layer(name, this);
        director.setup(clipboard, layer);
        symbolToAdd = null;
        if (!preview)
          layerL.add(layer);
        if (sumPanel != null)
          sumPanel.selectLayer(layer);
        // grabFocus();
      }

    public void showOptions()
      {
        drawOptions.editProperties(getLocationOnScreen());
      }

    protected void lockSymbols(boolean lock)
      {
        if (lock)
          {
            List<AbstractComponent> selected = layer
                .getSelected();
            for (Iterator<AbstractComponent> i = selected
                .iterator(); i.hasNext();)
              i.next().setLock(true);
            layer.unselectAll();
          }
        else
          {
            List all = layer.getAll();
            for (Iterator i = all.iterator(); i.hasNext();)
              ((AbstractComponent) i.next()).setLock(false);
          }
      }

    protected void toggleTraceMode()
      {
        layer.unselectAll();
        symbolToAdd = null;
        repaint();
        if (modeType != ActionRepository.TRACE)
          setModeType(ActionRepository.TRACE);
        else
          setModeType(Cursor.DEFAULT_CURSOR);
      }

    private void showAppletMessage()
      {
        JOptionPane
            .showMessageDialog(
                parent,
                "Java applet does not allow access to the local environment (including the clipboard). Please download the software and use it as an application.",
                "Java applet information",
                JOptionPane.INFORMATION_MESSAGE);
      }

    // LAYER

    public Layer getLayer()
      {
        return layer;
      }

    protected Object[] getLayers()
      {
        return layerL.toArray();
      }

    protected void addSummaryPanel(MapOverviewPane p)
      {
        if (p != null)
          {
            sumPanel = p;
            director.addSummaryPanel(p);
            p.selectLayer(layer);
          }
      }

    protected void setLayer(Layer L)
      {
        layer = L;
        layer.unselectAll();
        director.setup(clipboard, layer);
        layer.assignAllGenes();
        symbolToAdd = null;
        repaint();
        setMessage("selected.");
        if (sumPanel != null)
          sumPanel.selectLayer(L);
      }

    protected void moveLayer(int from, int to)
      {
        if (from == to)
          return;
        Object c = layerL.remove(from);
        layerL.add(to, c);
      }

    protected List<AbstractComponent> getMetabolites(Layer L)
      {
        List ly = layer.getAll();
        List<AbstractComponent> R = new ArrayList<AbstractComponent>();
        for (Iterator I = ly.iterator(); I.hasNext();)
          {
            AbstractComponent ac = (AbstractComponent) I
                .next();
            if (ac instanceof MolFigure)
              R.add(ac);
          }
        return R;
      }

    public List<AbstractComponent> getMetabolites()
      {
        return getMetabolites(layer);
      }

    protected void importArmData(DataRepository.Base dat,
        Point2D.Float p, boolean showText)
      {
        AbstractComponent fig = dat
            .getFigureComponent(this);
        TextRepository.ArmName txt = showText ? dat
            .getTextComponent(this) : null;
        p.x += IMPORT_XMARGIN;
        p.y += IMPORT_YMARGIN;
        layer.addNew(fig, p, GRID_STEP);
        if (fig instanceof PathFigure)
          {
            List<AbstractComponent> L = new ArrayList<AbstractComponent>();
            L.add(fig);
            layer.ungroup(L);
          }
        else if (txt != null)
          {
            if (fig instanceof MolFigure)
              {
                p.y += (int) fig.getRectBound().height;
                Rectangle2D.Float bound = fig
                    .getRectBound();
                txt.setRectBound(bound.width, 20);
              }
            else if (fig instanceof EnzFigure)
              {
                Rectangle2D.Float bound = fig
                    .getRectBound();
                p.x += bound.width / 2 + 20;
                p.y += bound.height / 2;
                txt.setRectBound(80, 20);
              }
            layer.addNew(txt, p, GRID_STEP);
            fig.setChain(txt);
          }
        layer.assignAllGenes();
      }

    // UNDO / REDO
    protected void store()
      {
        UndoStack.store(layer.getAll());
        director.enableUndo(true);
      }

    protected void undo()
      {
        List<AbstractComponent> L = UndoStack.undo();
        if (L == null)
          {
            director.enableUndo(false);
            JOptionPane.showMessageDialog(parent,
                "No undo information");
            return;
          }
        layer.setAll(L);
        layer.unselectAll();
        repaint();
        director.selectedSymbolsChanged();
        director.enableRedo(true);
      }

    protected void redo()
      {
        List<AbstractComponent> L = UndoStack.redo();
        if (L == null)
          {
            director.enableRedo(false);
            JOptionPane.showMessageDialog(parent,
                "No redo information");
            return;
          }
        layer.setAll(L);
        layer.unselectAll();
        repaint();
        director.selectedSymbolsChanged();
        director.enableUndo(true);
      }

    protected ArrayList<AbstractComponent> readComponents(
        BufferedReader br, Map<Integer, AbstractComponent> M)
        throws IOException
      {
        AbstractComponent ac = null;
        ArrayList<AbstractComponent> ret = new ArrayList<AbstractComponent>();
        String line = br.readLine(); // Version
        while (line != null
            && !line.startsWith("ComponentSize"))
          line = br.readLine();
        if (line == null)
          return ret;
        String[] W = line.split(" |\\t");
        int c = Integer.parseInt(W[1]);
        for (int i = 0; i < c; i++)
          {
            line = br.readLine();
            if (line == null)
              break;
            if (line.equals("MolFigure"))
              ac = new MolFigure();
            else if (line.equals("EnzFigure"))
              ac = new EnzFigure();
            else if (line.equals("Rectangle"))
              ac = new ShapeRepository.Rect();
            else if (line.equals("RoundRect"))
              ac = new ShapeRepository.RoundRect();
            else if (line.equals("Oval"))
              ac = new ShapeRepository.Oval();
            else if (line.equals("Line"))
              ac = new LineRepository.Line(0, false);
            else if (line.equals("Connector"))
              ac = new LineRepository.Connector();
            else if (line.equals("EnzArrow"))
              ac = new LineRepository.EnzArrow();
            else if (line.equals("Group"))
              ac = new ComponentGroup();
            else if (line.equals("TextBox"))
              ac = new TextRepository.TextBox();
            else if (line.equals("MolName"))
              ac = new TextRepository.MolName();
            else if (line.equals("EnzName"))
              ac = new TextRepository.EnzName();
            else
              ac = null;
            if (ac != null)
              {
                ac.readDataFormat(this, br, M);
                ac.setRectBound();
                ret.add(ac);
              }
          }
        return ret;
      }

    private Action newCanvas, open, close, save, saveAs,
        exit, find, properties, about, zoomIn, zoomOut,
        aboutMe, print, options, defarrow, look, mass,
        summary, layerlist, cut, copy, paste, undo, redo,
        delete, addSymbol, group, ungroup, selectAll,
        bringToFront, sendToBack, bringForward,
        sendBackward, flipHoriz, flipVert, rotateClock,
        rotateAClock, alignTop, alignBottom, alignVert,
        alignHoriz, alignRight, alignLeft, fontLarger,
        fontSmaller, fontNormal, fontBold, fontItalic,
        trace, lock, unlock, pSize, amount;

    private JSpinner zoomSpinner = null;

    public void prepareMenusForPopups(boolean isTraceActive)
      {
        bringToFront = new ActionRepository.BringToFront(
            this);
        bringForward = new ActionRepository.BringForward(
            this);
        sendToBack = new ActionRepository.SendToBack(this);
        sendBackward = new ActionRepository.SendBackward(
            this);

        flipHoriz = new ActionRepository.FlipHorizontal(
            this);
        flipVert = new ActionRepository.FlipVertical(this);
        rotateClock = new ActionRepository.RotateClock(this);
        rotateAClock = new ActionRepository.RotateAntiClock(
            this);
        selectAll = new ActionRepository.SelectAll(this);
        cut = new ActionRepository.Cut(this);
        copy = new ActionRepository.Copy(this);
        paste = new ActionRepository.Paste(this);
        group = new ActionRepository.Group(this);
        ungroup = new ActionRepository.Ungroup(this);

        if (isTraceActive)
          trace = new ActionRepository.Trace(this);
        defarrow = new ActionRepository.Default(this);

        compPopup = new PopupRepository.ComponentPopup(
            parent, this);
        tracePopup = new PopupRepository.TracePopup(parent,
            this);
        canvasPopup = new PopupRepository.CanvasPopup(
            parent, this);
      }

    protected void fillPopupEditMenu(JPopupMenu p)
      {
        boolean IS_JP = AbstractComponent.IS_JP;

        if (trace != null)
          addMenus(p, new Action[] { trace, null });
        addMenus(p, new Action[] { cut, copy, paste, null });

        p.add(fillMenu(IS_JP ? "反転/回転" : "Flip/Rotate",
            'f', new Action[] { flipHoriz, flipVert,
                rotateClock, rotateAClock }));
        p.addSeparator();

        if (bringToFront != null)
          {
            p.add(fillMenu(IS_JP ? "順番" : "Order", 'o',
                new Action[] { bringToFront, sendToBack,
                    bringForward, sendBackward }));
          }

        if (group != null)
          {
            p.add(fillMenu(IS_JP ? "グループ" : "Group", 'g',
                new Action[] { group, ungroup }));
            p.addSeparator();
          }

        addMenu(p, properties);
      }

    protected void prepareMenusForPreview(JToolBar toolBar)
      {
        properties = new ActionRepository.Properties(this);
        zoomIn = new ActionRepository.ZoomIn(this);
        zoomOut = new ActionRepository.ZoomOut(this);
        about = new ActionRepository.About(this);

        prepareMenusForPopups(false);

        director
            .addSelectedSymbolSensitiveActions(new Action[] {
                properties, cut, copy, flipHoriz, flipVert,
                rotateClock, rotateAClock });
        director
            .addClipboardSensitiveActions(new Action[] { paste });
        setShowGrid(false);

        if (!BasePane.isApplet())
          {
            saveAs = new ActionRepository.SaveAs(this);
            print = new ActionRepository.Print(this);
            addActions(toolBar, new Action[] { saveAs,
                print });
          }

        zoomSpinner = makeZoomSpinner(this, zoomIn, zoomOut);
        addActions(toolBar, new Action[] { zoomIn, zoomOut,
            trace, about });
      }

    protected void prepareMenusForViewFrame(
        JMenuBar jMenuBar, Palette.Symbols symbolPalette,
        JToolBar basicToolBar, JToolBar figureToolBar)
      {
        newCanvas = new ActionRepository.New(this);
        close = new ActionRepository.Close(this);

        undo = new ActionRepository.Undo(this);
        redo = new ActionRepository.Redo(this);
        properties = new ActionRepository.Properties(this);
        delete = new ActionRepository.Delete(this);
        about = new ActionRepository.About(this);
        addSymbol = new ActionRepository.AddSymbol(this);
        aboutMe = new ActionRepository.AboutMe(this);
        options = new ActionRepository.ViewOptions(this);

        zoomIn = new ActionRepository.ZoomIn(this);
        zoomOut = new ActionRepository.ZoomOut(this);
        find = new ActionRepository.Find(this);
        look = new ActionRepository.Info(this);
        mass = new ActionRepository.MolMass(this);
        selectAll = new ActionRepository.SelectAll(this);

        alignTop = new ActionRepository.AlignTop(this);
        alignBottom = new ActionRepository.AlignBottom(this);
        alignLeft = new ActionRepository.AlignLeft(this);
        alignRight = new ActionRepository.AlignRight(this);
        alignVert = new ActionRepository.AlignVertical(this);
        alignHoriz = new ActionRepository.AlignHorizontal(
            this);

        fontLarger = new ActionRepository.FontLarger(this);
        fontSmaller = new ActionRepository.FontSmaller(this);
        fontNormal = new ActionRepository.FontNormal(this);
        fontBold = new ActionRepository.FontBold(this);
        fontItalic = new ActionRepository.FontItalic(this);

        lock = new ActionRepository.Lock(this);
        unlock = new ActionRepository.Unlock(this);
        amount = new ActionRepository.Amount(this);
        pSize = new ActionRepository.PaperSize(this);

        prepareMenusForPopups(true);

        boolean IS_JP = AbstractComponent.IS_JP;
        if (searchPane.BasePane.isApplet())
          {

            jMenuBar.add(fillMenu(IS_JP ? "ファイル" : "File",
                'f', new Action[] { newCanvas, options }));
          }
        else
          {
            open = new ActionRepository.Open(this);
            save = new ActionRepository.Save(this);
            saveAs = new ActionRepository.SaveAs(this);
            exit = new ActionRepository.Exit(this);
            print = new ActionRepository.Print(this);

            jMenuBar.add(fillMenu(IS_JP ? "ファイル" : "File",
                'f', new Action[] { newCanvas, open, close,
                    save, saveAs, print, options, exit }));
          }

        jMenuBar.add(fillMenu(IS_JP ? "編集" : "Edit", 'e',
            new Action[] { undo, redo, cut, copy, paste,
                delete, selectAll, find, lock, unlock,
                group, ungroup }));

        JMenu insertM = new JMenu(IS_JP ? "挿入" : "Insert");
        insertM.setMnemonic('i');
        JMenu symM = fillMenu(IS_JP ? "記号" : "Symbols",
            's', null);
        symbolPalette.setSymbolsMenu(symM);
        insertM.add(symM);
        jMenuBar.add(insertM);

          { // Prepare insert menu
            Action smi = new ActionRepository.InputSmi(this);
            Action enz = new ActionRepository.InputEnz(this);
            JMenu metabM;
            if (searchPane.BasePane.isApplet())
              {
                metabM = fillMenu(IS_JP ? "代謝要素"
                    : "Pathway Elements", 'p',
                    new Action[] { smi, enz });
              }
            else
              {
                Action mol = new ActionRepository.InputMol(
                    this);
                metabM = fillMenu(IS_JP ? "代謝要素"
                    : "Pathway Elements", 'p',
                    new Action[] { mol, smi, enz });
              }
            insertM.add(metabM);
          }

        JMenu formatM = new JMenu(IS_JP ? "様式" : "Format");
        formatM.setMnemonic('o');
        addAction(formatM, pSize);
        formatM
            .add(fillMenu(IS_JP ? "フォント" : "Font", 'f',
                new Action[] { fontNormal, fontBold,
                    fontItalic }));
        formatM.add(fillMenu(IS_JP ? "配置" : "Alignment",
            'a', new Action[] { alignTop, alignHoriz,
                alignBottom, alignLeft, alignVert,
                alignRight }));
        jMenuBar.add(formatM);

        jMenuBar.add(fillMenu(IS_JP ? "ツール" : "Tools", 't',
            new Action[] { trace }));

        jMenuBar.add(fillMenu(IS_JP ? "ウィンドウ" : "Window",
            'w', new Action[] { mass }));

        jMenuBar.add(fillMenu(IS_JP ? "ヘルプ" : "Help", 'h',
            new Action[] { about, aboutMe }));

        // BASIC TOOL BAR
        addActions(basicToolBar, new Action[] { newCanvas,
            open, save, null, print, null, cut, copy,
            paste, undo, redo, null, properties, delete,
            null });

        zoomSpinner = makeZoomSpinner(this, zoomIn, zoomOut);

        addActions(basicToolBar, new Action[] { zoomIn,
            zoomOut });
        basicToolBar.add(zoomSpinner);

        addActions(basicToolBar, new Action[] { null, find,
            look });

        // FIGURE TOOL BAR
        addActions(figureToolBar, new Action[] { group,
            ungroup, null, defarrow });
        addActions(figureToolBar, symbolPalette
            .getSymbolsAction());

        addActions(figureToolBar, new Action[] { null,
            alignTop, alignHoriz, alignBottom, alignLeft,
            alignVert, alignRight, null, fontLarger,
            fontSmaller, fontNormal, fontBold, fontItalic,
            null, trace });

        // DIRECTOR SETUP
        director
            .addClipboardSensitiveActions(new Action[] { paste });
        director
            .addSelectedSymbolSensitiveActions(new Action[] {
                cut, copy, delete, properties, flipHoriz,
                flipVert, ungroup, rotateClock,
                rotateAClock, bringForward, sendBackward,
                bringToFront, sendToBack, lock, fontLarger,
                fontSmaller, fontNormal, fontBold,
                fontItalic, });
        director
            .addMultipleSelectionSensitiveActions(new Action[] {
                group, alignTop, alignBottom, alignVert,
                alignLeft, alignRight, alignHoriz });
        director.addTraceSensitiveActions(new Action[] {
            cut, paste, copy, delete, properties, group,
            ungroup, bringForward, sendBackward,
            bringToFront, sendToBack, lock, unlock,
            alignTop, alignBottom, alignVert, alignLeft,
            alignRight, alignHoriz, fontLarger,
            fontSmaller, fontNormal, fontBold, fontItalic,
            undo, redo });

        director
            .addUndoSensitiveActions(new Action[] { undo });
        director
            .addRedoSensitiveActions(new Action[] { redo });

        // Save the initial white screen and start.
        store();
      }

    private void addMenu(JPopupMenu c, Action a)
      {
        if (a == null)
          return;
        JMenuItem mi = c.add(a);
        mi.setMnemonic(a.getMnemonic());
      }

    private void addMenus(JPopupMenu c, Action[] A)
      {
        for (int i = 0; i < A.length; i++)
          if (A[i] == null)
            c.addSeparator();
          else
            addMenu(c, A[i]);
      }

    private void addAction(JMenu menu, Action action)
      {
        JMenuItem item = menu.add(action);
        item.setMnemonic(action.getMnemonic());
        if (action.getAccelerator() != ' ')
          item.setAccelerator(KeyStroke.getKeyStroke(action
              .getAccelerator(), ActionEvent.CTRL_MASK));
        item.setToolTipText(action.getToolTipText());
      }

    private void addActions(JToolBar bar, Action[] A)
      {
        for (int i = 0; i < A.length; i++)
          addAction(bar, A[i]);
      }

    private JButton addAction(JToolBar bar, Action a)
      {
        if (a == null)
          {
            bar.addSeparator();
            return null;
          }
        JButton b = bar.add(a);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setMnemonic(a.getMnemonic());
        b.setToolTipText(a.getToolTipText());
        return b;
      }

    private JMenu fillMenu(String title, char mnemonic,
        Action[] actions)
      {
        JMenu menu = new JMenu(title);
        menu.setMnemonic(mnemonic);
        if (actions != null)
          for (int i = 0; i < actions.length; i++)
            addAction(menu, actions[i]);
        return menu;
      }

    private JMenu fillRadioMenu(String title,
        char mnemonic, Action[] actions)
      {
        JMenu menu = new JMenu(title);
        menu.setMnemonic(mnemonic);
        ButtonGroup bg = new ButtonGroup();
        if (actions != null)
          for (int i = 0; i < actions.length; i++)
            {
              JRadioButtonMenuItem rb = new JRadioButtonMenuItem(
                  actions[i]);
              bg.add(rb);
              menu.add(rb);
              if (i == 0)
                rb.setSelected(true);
            }
        return menu;
      }

    private JSpinner makeZoomSpinner(final DrawPane canvas,
        Action zoomIn, Action zoomOut)
      {
        SpinnerNumberModel snm = new SpinnerNumberModel(
            0.8, 0.4, 3.0, 0.1);
        final JSpinner zoomSpinner = new JSpinner(snm);
        zoomSpinner.setMaximumSize(new Dimension(60, 30));
        JSpinner.NumberEditor sne = new JSpinner.NumberEditor(
            zoomSpinner, "###%");
        zoomSpinner.setEditor(sne);
        zoomSpinner.addChangeListener(new ChangeListener()
          {
            public void stateChanged(ChangeEvent ce)
              {
                canvas
                    .zoomChangeTo((float) ((Double) (zoomSpinner
                        .getValue())).doubleValue());
              }
          });

        zoomIn.setActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                Object o = zoomSpinner.getNextValue();
                if (o != null)
                  zoomSpinner.setValue(o);
              }
          });
        zoomOut.setActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                Object o = zoomSpinner.getPreviousValue();
                if (o != null)
                  zoomSpinner.setValue(o);
              }
          });

        return zoomSpinner;
      }

    public static void setApplet(JApplet p)
      {
        BasePane.setApplet(p);
      }
  }