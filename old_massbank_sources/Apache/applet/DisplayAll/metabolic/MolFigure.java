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
 * MolFigure Class
 *
 * ver 1.0.1 2008.12.05
 *
 ******************************************************************************/

package metabolic;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import map.AtomMap;
import util.CustomFileFilter;
import util.Dialogs;
import util.MolMass;
import util.WebBrowser;
import alg.graph.GraphData;
import alg.graph.GraphNode;
import canvas.AbstractComponent;
import canvas.AbstractFigure;
import canvas.DrawPane;
import canvas.IconRepository;
import canvas.LineRepository;
import canvas.LineStroke;
import canvas.TextRepository;
import canvas.LineRepository.ConnectorBase;
import draw2d.ConnectionTable;
import draw2d.Draw2D;
import draw2d.MOLformat;
import draw2d.Reactant;

public class MolFigure extends AbstractFigure implements
    ActionListener, MolecularData
  {
    // COLORS AND NUMBERS //
    static DefaultHighlighter.DefaultHighlightPainter dhp1 = new DefaultHighlighter.DefaultHighlightPainter(
        null);

    static DefaultHighlighter.DefaultHighlightPainter dhp2 = new DefaultHighlighter.DefaultHighlightPainter(
        Color.lightGray);

    private static Object[][] defaultAtomColors = {
        { "O", defaultOxygenColor },
        { "N", defaultNitrogenColor },
        { "S", defaultSulfurColor },
        { "P", defaultPhosphorusColor } };

    private static Object[][] atomColors = defaultAtomColors;

    private static JFrame atomColorFrame = new metabolic.AtomColorFrame();

    static public Object[][] getAtomColors()
      {
        return atomColors;
      }

    static public void setAtomColors(Object[][] c)
      {
        atomColors = c;
      }

    static public void resetAtomColors()
      {
        atomColors = defaultAtomColors;
      }

    private static Color numberColor = Color.green;

    private static Color highlightColor = Color.red;

    private static Color editColor = Color.red;

    /**
     * Set color of the atomic numbering
     * 
     * @param c
     */
    public void setAtomNumberColor(Color c)
      {
        numberColor = c;
      }

    /**
     * Set color of the selected (highlighted) atoms
     * 
     * @param c
     */
    public void setHighlightUpColor(Color c)
      {
        highlightColor = c;
      }

    /**
     * Set color of the atoms under edit
     * 
     * @param c
     */
    public void setEditColor(Color c)
      {
        editColor = c;
      }

    public static Color[] mapColors = { Color.green,
        Color.blue, Color.red, Color.magenta, Color.cyan,
        Color.pink, Color.orange, Color.yellow };

    private static BasicStroke thickStroke = LineStroke
        .getStroke(4, 0);

    private static final Composite tranC = AlphaComposite
        .getInstance(AlphaComposite.SRC_OVER, 0.5F);

    private static final Composite tranC2 = AlphaComposite
        .getInstance(AlphaComposite.SRC_OVER, 0.7F);

    protected static float DEFSCALE = 10f;

    // MENUS //
    private static String EDIT = "Edit Structure";

    private static String MOL = "MOL file";

    private static String SMILE = "Smiles";

    private static String NAME = "Add Name";

    public static boolean AUTO_DRAW = false;

    public static boolean AUTO_ROTATE = true;

    private boolean autoDrawing = AUTO_DRAW;

    private boolean autoRotating = AUTO_ROTATE;

    private boolean handDrawing = false;

    private boolean autoScaling = true;

    private boolean drawFrame = false;

    private boolean showAtomNumber = false;

    private boolean showChirality = true;

    private boolean showAromaticity = false;

    private boolean showMonochrome = false;

    private boolean showCoreStructureColor = true;

    private int showHydrogenPattern = 2;

    private int hydrogen_ALL = 0, hydrogen_HETERO = 1,
        hydrogen_TERMINAL = 2, hydrogen_NO = 3;

    // bounding box of this object.
    private Rectangle2D.Float rectBound_ = null;

    /**
     * display the numbering for atoms
     * 
     * @param showNumbering
     */
    public void setAtomNumbering(boolean showNumbering)
      {
        showAtomNumber = showNumbering;
      }

    // Size of molecular figures
    private static final int maxWindowSize = 500;

    private static final int maxScaleSize = 500;

    private static final int minScaleSize = 5;

    private static final int pictureMargin = 15;

    private float origHeight = 0, origWidth = 0;

    private float origXoffset = 0, origYoffset = 0;

    private boolean showEdgeColors = false;

    private float zoomScale = 0;

    private BitSet nodeSelect = null;

    private Color[] nodeColor = null;

    // AMOUNT INFORMATION //
    private int amountIntensity = 0;

    private int amountRadius = 0;

    private Color radiusColor = new Color(0xff, 0xc0, 0xcb);

    private float[] xPosL = null;

    private float[] yPosL = null;

    // File I/O
    protected static JFileChooser chooser = null;

    private float fontHeight = 1;

    protected Reactant react = null;

    protected CompoundAmountMenu compMenu = null;

    public MolFigure()
      {
        objectLabel = "MolFigure";
      }

    public MolFigure(String id, String name, MOLformat mf)
      {
        objectLabel = "MolFigure";
        read(id, name, mf);
      }

    public void clear()
      {
        react = null;
        data = null;
        boundary = new GeneralPath();
        rectBound_ = new Rectangle2D.Float();
      }

    public void read(String id, String name, MOLformat mf)
      {
        react = new Reactant(id, mf);
        data = new DataRepository.MolData(0, id, name, 0,
            (short) 0, (short) 0);
        data.setInfo(mf.toString());
      }

    public void initialization(DrawPane d, Point2D.Float p)
      {
        initialization(d, p, DEFSCALE);
      }

    public void initialization(DrawPane d, Point2D.Float p,
        float scale)
      {
        pane = d;
        zoomScale = scale;
        fillColor = null;
        lineColor = defaultLineColor;
        lineStroke = defaultLineStroke;
        fontColor = defaultFontColor;
        fontStyle = defaultAtomFontStyle.deriveFont(defaultAtomFontStyle
        		.getSize()
      			* (float) Math.sqrt(scale / DEFSCALE));
        fixedRatio = true;
        setMolCoordinates();
        setOriginalSize();
        setColors();
        setEditing(false);
        if (p != null)
          setLocation(p.x, p.y);
        if (data != null)
          {
            StringBuffer sb = new StringBuffer();
            sb.append(data.firstName());
            if (data.id() != null)
              {
                sb.append(" (");
                sb.append(data.id());
                sb.append(")");
              }
            tooltiptext = sb.toString();
          }

        // Preparation of compMenu for abstract
        // chemical group
        // instantiateChemicalGroups();
      }

    protected void setColors()
      {
        ConnectionTable cTable = react.cTable;
        int numberOfNodes = cTable.numberOfNodes();
        showEdgeColors = false;
        nodeColor = new Color[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++)
          {
            nodeColor[i] = fontColor;
            if (!showMonochrome)
              for (int l = 0; l < atomColors.length; l++)
                if (((String) atomColors[l][0])
                    .equals(cTable.getAtom(i)))
                  {
                    nodeColor[i] = (Color) atomColors[l][1];
                    break;
                  }
          }

        showEdgeColors = true;

        if (showCoreStructureColor)
          draw2d.structure.Utils.colorMOLformat(react.id,
              react.molF, nodeColor);
      }

    public void setData(DataRepository.Base d)
      {
        data = (DataRepository.MolData) d;
      }

    public DataRepository.Base getData()
      {
        return data;
      }

    public float getScale()
      {
        return zoomScale;
      }

    public void setScale(float z)
      {
        zoomScale = z;
      }

    public boolean contains(Point2D.Float p)
      {
        if ((fillColor != null) || (data == null))
          return true;
        return rectBound_.contains(p);
        // if (drawFrame) return true;
        // contains(p,(int)(CTRL_POINT_TOLERANCE*zoomScale));
      }

    public void translateLocation(float xdif, float ydif)
      {
        super.translateLocation(xdif, ydif);
        rectBound_.setRect(rectBound_.x + xdif,
            rectBound_.y + ydif, rectBound_.width,
            rectBound_.height);
        computeDisplayCoordinates();
      }

    // overriding
    protected Point2D.Float getNearestPosition(
        Point2D.Float p)
      {
        Point2D.Float center = new Point2D.Float(
            rectBound.x + rectBound.width / 2, rectBound.y
                + rectBound.height / 2);
        double angle = Math.atan2(p.x - center.x, p.y
            - center.y);
        if (angle > Math.PI)
          angle -= 2 * Math.PI;
        if (angle < -Math.PI)
          angle += 2 * Math.PI;
        double rad1 = Math.atan2(rectBound_.width,
            rectBound_.height);
        Point2D.Float q;
        if ((angle >= rad1) && (angle <= Math.PI - rad1))
          {// east
            q = new Point2D.Float(rectBound_.width,
                rectBound_.height / 2 + (p.y - center.y)
                    * rectBound_.width
                    / (2 * (p.x - center.x)));
          }
        else if ((angle <= -rad1)
            && (angle >= -Math.PI + rad1))
          {// west
            q = new Point2D.Float(0, rectBound_.height / 2
                - (p.y - center.y) * rectBound_.width
                / (2 * (p.x - center.x)));

          }
        else if ((angle <= rad1) && (angle >= -rad1))
          {// south
            q = new Point2D.Float(rectBound_.width / 2
                + (p.x - center.x) * rectBound_.height
                / (2 * (p.y - center.y)), rectBound_.height);
          }
        else
          {// north
            q = new Point2D.Float(rectBound_.width / 2
                - (p.x - center.x) * rectBound_.height
                / (2 * (p.y - center.y)), 0);
          }
        q.x += rectBound_.x;
        q.y += rectBound_.y;
        return q;
      }

    protected void highlightBoundary(Graphics2D g)
      {
        g.setXORMode(Color.green);
        g.draw(rectBound_);
        g.setPaintMode();
      }

    protected List<JMenuItem> getTraceMenus(DrawPane d)
      {
        List<JMenuItem> L = getEditMenus(d);
        return L;
      }

    protected List<JMenuItem> getEditMenus(final DrawPane d)
      {
        List<JMenuItem> ret = new ArrayList<JMenuItem>();

        // Prepare View Menu
        JMenu viewMenu = new JMenu(IS_JP ? "表示"
            : "Draw style");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        ret.add(viewMenu);
        // DISPLAY STYLE
        JMenu menu = new JMenu(IS_JP ? "見せ方" : "Display");
        viewMenu.add(menu);
        JMenuItem item = new JCheckBoxMenuItem(
            IS_JP ? "自動描画" : "automatic layout",
            autoDrawing);
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                autoDrawing ^= true;
                ((JCheckBoxMenuItem) ae.getSource())
                    .setState(autoDrawing);
                react = null;
                setMolCoordinates();
                setOriginalSize();
                setRectBound();
                pane.repaint();
              }
          });
        item = new JCheckBoxMenuItem(IS_JP ? "自動回転"
            : "automatic flipping", autoRotating);
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                autoRotating ^= true;
                ((JCheckBoxMenuItem) ae.getSource())
                    .setState(autoRotating);
                setMolCoordinates();
                setOriginalSize();
                setRectBound();
                pane.repaint();
              }
          });
        item = new JCheckBoxMenuItem(IS_JP ? "自動スケーリング"
            : "automatic scaling", autoScaling);
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                autoScaling ^= true;
                ((JCheckBoxMenuItem) ae.getSource())
                    .setState(autoScaling);
                setRectBound();
                pane.repaint();
              }
          });
        item = new JCheckBoxMenuItem(IS_JP ? "手描き"
            : "handwriting", handDrawing);
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                handDrawing ^= true;
                ((JCheckBoxMenuItem) ae.getSource())
                    .setState(handDrawing);
                pane.repaint();
              }
          });
        item = new JCheckBoxMenuItem(IS_JP ? "フレーム"
            : "draw frame", drawFrame);
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                drawFrame ^= true;
                ((JCheckBoxMenuItem) ae.getSource())
                    .setState(drawFrame);
                pane.repaint();
              }
          });

        menu = new JMenu(IS_JP ? "色" : "Color");
        viewMenu.add(menu);
        item = new JCheckBoxMenuItem(IS_JP ? "白黒"
            : "monochrome", showMonochrome);
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                showMonochrome ^= true;
                ((JCheckBoxMenuItem) ae.getSource())
                    .setState(showMonochrome);
                setColors();
                pane.repaint();
              }
          });
        item = new JCheckBoxMenuItem(IS_JP ? "基本骨格認識"
            : "core structure", showCoreStructureColor);
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                showCoreStructureColor ^= true;
                ((JCheckBoxMenuItem) ae.getSource())
                    .setState(showCoreStructureColor);
                setColors();
                pane.repaint();
              }
          });
        item = new JMenuItem(IS_JP ? "原子の色" : "atom colors");
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                atomColorFrame.pack();
                atomColorFrame.setVisible(true);
              }
          });
        // HYDROGEN
        menu = new JMenu(IS_JP ? "水素" : "hydrogen");
        viewMenu.add(menu);

        String[] menuCommands = IS_JP ? new String[] {
            "全部", "末端", "炭素以外", "なし" } : new String[] {
            "all", "terminals", "hetero atoms", "none" };
        int[] hPatterns = { hydrogen_ALL,
            hydrogen_TERMINAL, hydrogen_HETERO, hydrogen_NO };
        for (int i = 0; i < menuCommands.length; i++)
          {
            String comm = menuCommands[i];
            final int pat = hPatterns[i];
            item = new JCheckBoxMenuItem(comm,
                showHydrogenPattern == pat);
            menu.add(item);
            item.addActionListener(new ActionListener()
              {
                public void actionPerformed(ActionEvent ae)
                  {
                    showHydrogenPattern = pat;
                    pane.repaint();
                  }
              });
          }
        // FOLDING GROUP
        menu = new JMenu(IS_JP ? "構造" : "structure");
        viewMenu.add(menu);

        JMenu menu2 = new JMenu(IS_JP ? "折りたたみ" : "fold");
        menu.add(menu2);
        item = new JMenuItem("COOH");
        menu2.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                react.molF.foldCOOH();
                react = new Reactant(data.id(), react.molF);
                setMolCoordinates();
                pane.repaint();
              }
          });
        item = new JMenuItem("OCH3");
        menu2.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                react.molF.foldOCH3();
                react = new Reactant(data.id(), react.molF);
                setMolCoordinates();
                pane.repaint();
              }
          });
        item = new JMenuItem("CH2OH");
        menu2.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                react.molF.foldCH2OH();
                react = new Reactant(data.id(), react.molF);
                setMolCoordinates();
                pane.repaint();
              }
          });

        item = new JMenuItem("all");
        menu2.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                react.molF.foldAllChemicalGroups();
                react = new Reactant(data.id(), react.molF);
                setMolCoordinates();
                pane.repaint();
              }
          });

        menu2 = new JMenu(IS_JP ? "展開" : "unfold");
        menu.add(menu2);
        String[] chemGroups = { "COOH", "OCH3", "CH2OH" };
        for (int i = 0; i < chemGroups.length; i++)
          {
            final String str = chemGroups[i];
            item = new JMenuItem(str);
            menu2.add(item);
            item.addActionListener(new ActionListener()
              {
                public void actionPerformed(ActionEvent ae)
                  {
                    react.molF.unfoldChemicalGroup(str);
                    react = new Reactant(data.id(),
                        react.molF);
                    setMolCoordinates();
                    pane.repaint();
                  }
              });
          }

        item = new JMenuItem("all");
        menu2.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                react.molF.unfoldChemicalGroup(null);
                react = new Reactant(data.id(), react.molF);
                setMolCoordinates();
                pane.repaint();
              }
          });

        // MISC
        menu = new JMenu(IS_JP ? "その他" : "misc");
        viewMenu.add(menu);
        item = new JMenuItem(IS_JP ? "グラフ不変数"
            : "graph invariant");
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                react.convertAromaticRings();
                react.convertCarboxylAndDiaminoGroups();
                int width = react.numberOfAtoms("C") / 2 + 1;
                Point[] rank = react.computeRank(width,
                    true, null);
                int[] perm = react.cTable.permute(rank);
                rank = react.computeRank(width, true, perm);
                react = new Reactant(data.id(), react.molF);
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < rank.length; i++)
                  {
                    Point p = rank[i];
                    sb.append(i);
                    sb.append("\t");
                    sb.append(p.x);
                    sb.append("\t");
                    sb.append(p.y);
                    sb.append("\n");
                  }
                JTextArea jt = new JTextArea(sb.toString());
                jt.setEditable(false);
                JDialog jd = new JDialog((JFrame) null);
                jd.getContentPane()
                    .add(new JScrollPane(jt));
                jd.pack();
                jd.setVisible(true);
              }

          });
        viewMenu.add(menu);
        item = new JCheckBoxMenuItem(IS_JP ? "原子の番号"
            : "atom number", showAtomNumber);
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                showAtomNumber ^= true;
                ((JCheckBoxMenuItem) ae.getSource())
                    .setState(showAtomNumber);
                pane.repaint();
              }
          });
        item = new JCheckBoxMenuItem(IS_JP ? "芳香環"
            : "aromaticity", showAromaticity);
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                showAromaticity ^= true;
                ((JCheckBoxMenuItem) ae.getSource())
                    .setState(showAromaticity);
                react = new Reactant(data.id(), react.molF);
                if (showAromaticity)
                  {
                    react.convertAromaticRings();
                    react.convertCarboxylAndDiaminoGroups();
                  }
                pane.repaint();
              }
          });
        item = new JCheckBoxMenuItem(IS_JP ? "不斉情報"
            : "chirality", showChirality);
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                showChirality ^= true;
                ((JCheckBoxMenuItem) ae.getSource())
                    .setState(showChirality);
                pane.repaint();
              }
          });

        item = new JMenuItem(IS_JP ? "水素の除去など"
            : "remove hydrogen, etc.");
        menu.add(item);
        item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                draw2d.structure.Utils.checkMOLformat(
                    react.molF, react.id, true, true, true);
                react = new Reactant(data.id(), react.molF);
                setMolCoordinates();
                pane.repaint();
              }
          });

        // Prepare Output Menu
        JMenu infoMenu = new JMenu(IS_JP ? "情報"
            : "Information");
        infoMenu.setMnemonic(KeyEvent.VK_I);
        ret.add(infoMenu);

        if (data != null)
          {
            JMenu dataMenu = new JMenu(IS_JP ? "データベース"
                : "Database");
            dataMenu
                .setToolTipText("<HTML>Make sure to allow<BR>"
                    + "browser window for popups</HTML>");
            infoMenu.add(dataMenu);
            dataMenu.add(WebBrowser.urlMenuItem("Google",
                'g', "http://www.google.com/search?q="
                    + data.firstName()));
            dataMenu.add(WebBrowser.urlMenuItem("KEGG",
                'k',
                "http://www.genome.jp/dbget-bin/www_bget?compound+"
                    + data.id()));
          }
        JMenu outputMenu = new JMenu(IS_JP ? "ファイル出力"
            : "Output File");
        infoMenu.add(outputMenu);

        JMenuItem showMolMenu = new JMenuItem(MOL, 'm');
        outputMenu.add(showMolMenu);
        showMolMenu.setActionCommand(MOL);
        showMolMenu.addActionListener(this);

        JMenuItem showSmilesMenu = new JMenuItem(SMILE, 's');
        outputMenu.add(showSmilesMenu);
        showSmilesMenu.setActionCommand(SMILE);
        showSmilesMenu.addActionListener(this);

        // Prepare Edit Menu
        JMenuItem editMenu = new JMenuItem(IS_JP ? "構造の編集"
            : EDIT, IconRepository.EDIT_ICON);
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.setActionCommand(EDIT);
        editMenu.addActionListener(this);
        ret.add(editMenu);

        // Prepare Replacement Menu
        if (compMenu != null)
          ret.add(compMenu);

        // Prepare ShowName Menu
        JMenuItem nameMenu = new JMenuItem(IS_JP ? "物質名追加"
            : NAME, IconRepository.NORMAL_ICON);
        nameMenu.setMnemonic('n');
        nameMenu.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent ae)
              {
                TextRepository.ArmName txt = data
                    .getTextComponent(d);
                MolFigure.this.setChain(txt);
                txt.setRectBound(rectBound.width, 10);
                d.getLayer().addNew(txt, getLocation(), 0);
                d.repaint();
              }
          });
        ret.add(nameMenu);

        return ret;
      }

    public void actionPerformed(ActionEvent e)
      {
        String s = e.getActionCommand();
        /*
         * if (s == cbMenuInfo[GROUP][0]) {
         * cbMenuValue[GROUP] ^= true; if (data != null) {
         * delCash(data); Reactant.delCash(data.id()); }
         * computeMolCoordinates(); setSizeAndColors();
         * setRectBound(); } else if (s ==
         * cbMenuInfo[COLOR][0]) cbMenuValue[NUMBER] = true;
         */

        if (s == EDIT)
          showEditPanel(null);
        else if (s == MOL)
          showMOLformatDialog(null);
        else if (s == SMILE)
          showSmilesFormatDialog(null);
      }

    public void setMolCoordinates()
      {
        if ((data == null) && (react == null))
          return;
        if (react == null)
          react = new Reactant(data.id(), data.getInfo());
        if (autoDrawing)
          Draw2D.drawAutomatically(react);
        if (autoRotating)
          {
            draw2d.structure.Utils.rotateMOLformat(react.id,
                react.molF);
            //molFが反転するとキラル情報が変わるのでcTableを作り直す。
            react =new Reactant(data.id(), react.molF);
          }
        react.abbreviateGroups();
      }

    public void setAutoDrawing(boolean b)
      {
        autoDrawing = b;
      }

    protected void setOriginalSize()
      {
        if (react == null)
          return;
        float xmax = -Float.MAX_VALUE, ymax = -Float.MAX_VALUE;
        float xmin = Float.MAX_VALUE, ymin = Float.MAX_VALUE;
        MOLformat molF = react.molF;
        int numberOfNodes = react.cTable.numberOfNodes();
        for (int i = 0; i < numberOfNodes; i++)
          {
            if (react.cTable.isHiddenNode(i) && !underEdit)
              continue;
            float xp = molF.tx[i];
            float yp = -molF.ty[i];
            if (xp > xmax)
              xmax = xp;
            if (xp < xmin)
              xmin = xp;
            if (yp > ymax)
              ymax = yp;
            if (yp < ymin)
              ymin = yp;
          }
        origXoffset = -xmin;
        origYoffset = -ymin;
        origHeight = Math.max(ymax - ymin, 1);
        origWidth = Math.max(xmax - xmin, 1);
        nodeSelect = new BitSet(numberOfNodes);
        xPosL = new float[numberOfNodes];
        yPosL = new float[numberOfNodes];
      }

    public void setRectBound(float prefWidth,
        float prefHeight)
      {
        if (react == null)
          return;
        ConnectionTable cTable = react.cTable;

        float prevScale = zoomScale;
        if ((prefWidth != 0) || (prefHeight != 0))
          {
            zoomScale = (rectBound.width * prefHeight < rectBound.height
                * prefWidth) ? (prefWidth - 2 * pictureMargin)
                / origWidth
                : (prefHeight - 2 * pictureMargin)
                    / origHeight;
            zoomScale = Math.min(zoomScale, maxScaleSize);
            zoomScale = Math.max(zoomScale, minScaleSize);
            autoScaling = false;
          }
        else if (autoScaling
            && (Math.max(origHeight, origWidth) > maxWindowSize
                / zoomScale))
          {
            zoomScale = maxWindowSize
                / (float) Math.max(origHeight, origWidth);
          }
        if (prevScale != zoomScale)
        	fontStyle = fontStyle.deriveFont(fontStyle
        			.getSize()
        			* (float) Math.sqrt(zoomScale / prevScale));

        float height = origHeight * zoomScale + 2
            * pictureMargin;
        float width = origWidth * zoomScale + 2
            * pictureMargin;

        rectBound.setRect(rectBound.x, rectBound.y, width,
            height);
        boundary = makeBoundary(width, height);
        setCtrlPoints(width, height, 8);

        Point2D.Float pointReserve = null;
        if (parent != null)
          {
            pointReserve = new Point2D.Float(rectBound.x,
                rectBound.y);
            Point2D.Float p = getLocation();
            rectBound.x = p.x;
            rectBound.y = p.y;
          }

        computeDisplayCoordinates();
        float xmax = Float.MIN_VALUE, ymax = Float.MIN_VALUE;
        float xmin = Float.MAX_VALUE, ymin = Float.MAX_VALUE;
        int fsiz = fontStyle.getSize() / 2;
        for (int i = 0; i < cTable.numberOfNodes(); i++)
          {
            if (cTable.isHiddenNode(i) && !underEdit)
              continue;
            float xp = (float) xPos(i);
            float yp = (float) yPos(i);
            float symbolW = (cTable.getDescrip(i) != null) ? cTable
                .getDescrip(i).length()
                * fsiz
                : 0;
            if (xp + symbolW > xmax)
              xmax = xp + symbolW;
            if (xp - symbolW < xmin)
              xmin = xp - symbolW;
            if (yp + fontHeight > ymax)
              ymax = yp + fontHeight;
            if (yp - fontHeight < ymin)
              ymin = yp - fontHeight;
          }
        height = (ymax - ymin) + 2 * pictureMargin;
        width = (xmax - xmin) + 2 * pictureMargin;
        rectBound_ = new Rectangle2D.Float(xmin
            - pictureMargin, ymin - pictureMargin, width,
            height);

        // Since the position of this object will be
        // changed when a
        // group of objects is rotated, restore the
        // current position.
        if (parent != null)
          {
            rectBound.x = pointReserve.x;
            rectBound.y = pointReserve.y;
          }
      }

    public Rectangle2D.Float getBoundingBox()
      {
        return rectBound_;
      }

    private void computeDisplayCoordinates()
      {
        MOLformat molF = react.molF;
        for (int i = 0; i < react.cTable.numberOfNodes(); i++)
          {
            xPosL[i] = (rotateX(
                (molF.tx[i] + origXoffset - origWidth / 2)
                    * zoomScale * (flipH ? -1 : 1),
                (-molF.ty[i] + origYoffset - origHeight / 2)
                    * zoomScale))
                + origWidth
                * zoomScale
                / 2
                + pictureMargin
                + rectBound.x;
            yPosL[i] = (rotateY(
                (molF.tx[i] + origXoffset - origWidth / 2)
                    * zoomScale * (flipH ? -1 : 1),
                (-molF.ty[i] + origYoffset - origHeight / 2)
                    * zoomScale))
                + origHeight
                * zoomScale
                / 2
                + pictureMargin + rectBound.y;
          }
      }

    private float xPos(int i)
      {
        return xPosL[i];
      }

    private float yPos(int i)
      {
        return yPosL[i];
      }

    private float strWidth(String s, Graphics g,
        FontMetrics fm)
      {
        if (s == null)
          return 0;
        float siz = 0;
        for (int i = 0; i < s.length(); i++)
          siz += charWidth(s.charAt(i), g, fm);
        return siz;
      }

    private float charWidth(char c, Graphics g,
        FontMetrics fm)
      {
        if (Character.isDigit(c))
          return fm.charWidth(c) * 0.7f;
        else
          return fm.charWidth(c);
      }

    private boolean regionContains(float x1, float y1,
        float width, float height, float x2, float y2)
      {
        return ((x1 <= x2) && (x2 <= x1 + width)
            && (y1 <= y2) && (y2 <= y1 + height));
      }

    public void draw(Graphics2D g2, boolean fastDrawing)
      {
        draw(g2, fastDrawing, false, 0, 0);
      }

    public void draw(Graphics2D g2, boolean fastDrawing,
        boolean dragging, int shiftX, int shiftY)
      {
        if (react.molF == null)
          return;
        ConnectionTable cTable = react.cTable;
        if (fillColor != null)
          {
            g2.setColor(fillColor);
            g2.fill(rectBound_);
          }
        g2.setStroke(lineStroke);
        g2.setColor(lineColor);
        if (fastDrawing || drawFrame)
          g2.draw(rectBound_);
        if (fastDrawing)
          return;

        // オブジェクトのグループが回転したとき、位置がずれるのでバックアップ
        Point2D.Float pointReserve = null;
        if (parent != null)
          {
            pointReserve = new Point2D.Float(rectBound.x,
                rectBound.y);
            Point2D.Float p = getLocation();
            rectBound.x = p.x;
            rectBound.y = p.y;
          }

        fontHeight = g2.getFontMetrics(fontStyle)
            .getHeight() / 3f;
        Composite origC = g2.getComposite();

        // drawCompoundAmountDistribution(g2, origC);

        if ((amountRadius != 0) || (amountIntensity != 0))
          {
            g2.setComposite(tranC);
            Ellipse2D.Float circle = new Ellipse2D.Float(
                rectBound.x + rectBound.width / 2
                    - amountRadius, rectBound.y
                    + rectBound.height / 2 - amountRadius,
                2 * amountRadius, 2 * amountRadius);
            GradientPaint gp = new GradientPaint(
                rectBound.x + rectBound.width / 2,
                rectBound.y + rectBound.height / 2
                    + amountRadius, Color.lightGray,
                rectBound.x + rectBound.width / 2,
                rectBound.y + rectBound.height / 2
                    - amountRadius, radiusColor);
            g2.setPaint(gp);
            g2.fill(circle);
            g2.setComposite(origC);
          }

        // DRAW EDGES
        for (int i = 0; i < cTable.numberOfNodes(); i++)
          {
            if (cTable.isHiddenNode(i) && !underEdit)
              continue;
            int[] adjs = cTable.getAdjs(i);
            String srcSym = cTable.getAtom(i);
            for (int p = 0; p < adjs.length; p++)
              {
                int[] adjBonds = cTable.getAdjBondType(i);
                int bondtype = adjBonds[p] / 10;
                int bondStereo = adjBonds[p] % 10;
                // オリジナルのMOLフォーマットに従った向きだけを描く。
                if (bondStereo == 9)
                  continue;
                if (cTable.isHiddenNode(adjs[p])
                    && !underEdit)
                  continue;
                float srcTmpx = xPos(i);
                float srcTmpy = yPos(i);
                float tgtTmpx = xPos(adjs[p]);
                float tgtTmpy = yPos(adjs[p]);

                if (underEdit && dragging)
                  {
                    if (nodeSelect.get(i))
                      {
                        srcTmpx += shiftX;
                        srcTmpy += shiftY;
                      }
                    if (nodeSelect.get(adjs[p]))
                      {
                        tgtTmpx += shiftX;
                        tgtTmpy += shiftY;
                      }
                  }

                float tan = (float) Math
                    .sqrt((tgtTmpx - srcTmpx)
                        * (tgtTmpx - srcTmpx)
                        + (tgtTmpy - srcTmpy)
                        * (tgtTmpy - srcTmpy));
                float sin = (tgtTmpy - srcTmpy) / tan;
                float cos = (tgtTmpx - srcTmpx) / tan;

                // エッジの長さを原子の長さに従って削る
                if ((showHydrogenPattern == hydrogen_ALL)
                    || cTable.isStraightBranch(i)
                    || !srcSym.equals("C")
                    || adjs.length <= 1)
                  {
                    switch (srcSym.length()) {
                    case 1:
                      srcTmpx += (fontHeight * cos * 1.1);
                      srcTmpy += (fontHeight * sin * 1.1);
                      break;
                    case 2:
                      srcTmpx += (fontHeight * cos * 1.5);
                      srcTmpy += (fontHeight * sin * 1.1);
                      break;
                    default:
                      srcTmpx += (fontHeight * cos * 1.5);
                      srcTmpy += (fontHeight * sin * 1.1);
                      break;
                    }
                  }
                // エッジの長さを原子の長さに従って削る
                String tgtSym = cTable.getAtom(adjs[p]);
                if ((showHydrogenPattern == hydrogen_ALL)
                    || cTable.isStraightBranch(adjs[p])
                    || !tgtSym.equals("C")
                    || cTable.getDegree(adjs[p]) <= 1)
                  {
                    switch (tgtSym.length()) {
                    case 1:
                      tgtTmpx -= (fontHeight * cos * 1.1);
                      tgtTmpy -= (fontHeight * sin * 1.1);
                      break;
                    case 2:
                      tgtTmpx -= (fontHeight * cos * 1.5);
                      tgtTmpy -= (fontHeight * sin * 1.1);
                      break;
                    default:
                      tgtTmpx -= (fontHeight * cos * 1.5);
                      tgtTmpy -= (fontHeight * sin * 1.1);
                      break;
                    }
                  }

                // 色の設定
                if (underEdit)
                  {
                    if (cTable.isHiddenNode(i)
                        || cTable.isHiddenNode(adjs[p]))
                      g2.setColor(defaultHiddenColor);
                    else if (nodeSelect.get(i)
                        || nodeSelect.get(adjs[p]))
                      g2.setColor(editColor);
                    else
                      g2.setColor(fontColor);
                  }
                else if (showEdgeColors)
                  {
                    if (nodeColor[i] != fontColor
                        && nodeColor[adjs[p]]
                            .equals(nodeColor[i]))
                      {
                        g2.setStroke(thickStroke);
                        g2.setColor(nodeColor[adjs[p]]);
                      }
                    else
                      {
                        g2.setStroke(defaultLineStroke);
                        g2.setColor(defaultBondColor);
                      }
                  }
                else
                  g2.setColor(defaultBondColor);

                // DRAW EDGE
                if ((bondtype == ConnectionTable.doubleBond)
                    || (bondtype == ConnectionTable.tripleBond))
                  {// double or triple bond
                    g2.draw(new Line2D.Float(srcTmpx
                        - fontHeight * sin / 3, srcTmpy
                        + fontHeight * cos / 3, tgtTmpx
                        - fontHeight * sin / 3, tgtTmpy
                        + fontHeight * cos / 3));
                    g2.draw(new Line2D.Float(srcTmpx
                        + fontHeight * sin / 3, srcTmpy
                        - fontHeight * cos / 3, tgtTmpx
                        + fontHeight * sin / 3, tgtTmpy
                        - fontHeight * cos / 3));
                  }
                if ((bondtype == ConnectionTable.singleBond)
                    || (bondtype == ConnectionTable.tripleBond)
                    || (bondtype == ConnectionTable.singleOrDouble))
                  {// single or triple bond
                    if (handDrawing)
                      {
                        g2.draw(new Line2D.Float(srcTmpx
                            - fontHeight * sin / 3, srcTmpy
                            - fontHeight * cos / 3,
                            tgtTmpx, tgtTmpy));
                      }
                    else if (showChirality
                        && bondStereo != 0)
                      {// stereo bond
                        if (bondStereo == 4)
                          { // nonspecific stereo bond
                            // ここは本当は波線にするべき
                            GeneralPath gp = new GeneralPath();
                            gp.moveTo(tgtTmpx - fontHeight
                                * sin / 2, tgtTmpy
                                + fontHeight * cos / 2);
                            gp.lineTo(srcTmpx, srcTmpy);
                            gp.lineTo(tgtTmpx + fontHeight
                                * sin / 2, tgtTmpy
                                - fontHeight * cos / 2);
                            gp.lineTo(tgtTmpx - fontHeight
                                * sin / 2, tgtTmpy
                                + fontHeight * cos / 2);
                            g2.setComposite(tranC);
                            g2.fill(gp);
                            g2.setComposite(origC);
                          }
                        else if ((bondStereo == 1)
                            || (bondStereo == 6))
                          {
                            boolean upward = (bondStereo == 1);
                            upward ^= flipH;
                            if (upward)
                              {// upward
                                GeneralPath gp = new GeneralPath();
                                gp.moveTo(tgtTmpx
                                    - fontHeight * sin / 2,
                                    tgtTmpy + fontHeight
                                        * cos / 2);
                                gp.lineTo(srcTmpx, srcTmpy);
                                gp.lineTo(tgtTmpx
                                    + fontHeight * sin / 2,
                                    tgtTmpy - fontHeight
                                        * cos / 2);
                                gp.lineTo(tgtTmpx
                                    - fontHeight * sin / 2,
                                    tgtTmpy + fontHeight
                                        * cos / 2);
                                g2.fill(gp);
                              }
                            else
                              { // downward
                                float xd = fontHeight * sin
                                    / 2;
                                float yd = fontHeight * cos
                                    / 2;
                                float vx = tgtTmpx
                                    - srcTmpx;
                                float vy = tgtTmpy
                                    - srcTmpy;
                                int hstep = (int) (tan / 2);
                                for (int d = 0; d <= hstep; d++)
                                  {
                                    g2
                                        .draw(new Line2D.Float(
                                            (srcTmpx + (vx - xd)
                                                * d / hstep),
                                            (srcTmpy + (vy + yd)
                                                * d / hstep),
                                            (srcTmpx + (vx + xd)
                                                * d / hstep),
                                            (srcTmpy + (vy - yd)
                                                * d / hstep)));
                                  }
                              }
                          }
                        else if ((bondStereo == 8)
                            || (bondStereo == 7))
                          { // up-up 状態
                            boolean upward = (bondStereo == 7);
                            upward ^= flipH;
                            if (upward)
                              {
                                GeneralPath gp = new GeneralPath();
                                gp.moveTo(tgtTmpx
                                    - fontHeight * sin / 2,
                                    tgtTmpy + fontHeight
                                        * cos / 2);
                                gp.lineTo(srcTmpx
                                    - fontHeight * sin / 2,
                                    srcTmpy + fontHeight
                                        * cos / 2);
                                gp.lineTo(srcTmpx
                                    + fontHeight * sin / 2,
                                    srcTmpy - fontHeight
                                        * cos / 2);
                                gp.lineTo(tgtTmpx
                                    + fontHeight * sin / 2,
                                    tgtTmpy - fontHeight
                                        * cos / 2);
                                gp.lineTo(tgtTmpx
                                    - fontHeight * sin / 2,
                                    tgtTmpy + fontHeight
                                        * cos / 2);
                                g2.fill(gp);
                              }
                            else
                              {// downward
                                float xd = fontHeight * sin
                                    / 2;
                                float yd = fontHeight * cos
                                    / 2;
                                float vx = tgtTmpx
                                    - srcTmpx;
                                float vy = tgtTmpy
                                    - srcTmpy;
                                int hstep = (int) (tan / 2);
                                for (int d = 0; d <= hstep; d++)
                                  {
                                    g2
                                        .draw(new Line2D.Float(
                                            srcTmpx - xd
                                                + d * vx
                                                / hstep,
                                            srcTmpy + yd
                                                + d * vy
                                                / hstep,
                                            srcTmpx + xd
                                                + d * vx
                                                / hstep,
                                            srcTmpy - yd
                                                + d * vy
                                                / hstep));
                                  }
                              }
                          }
                      }
                    else
                      // normal bond
                      {
                        g2.draw(new Line2D.Float(srcTmpx,
                            srcTmpy, tgtTmpx, tgtTmpy));
                      }
                  }
                else if ((bondtype == ConnectionTable.aromaticBond)
                    || (bondtype == ConnectionTable.singleOrAromatic)
                    || (bondtype == ConnectionTable.doubleOrAromatic))
                  {
                    g2.setStroke(thickStroke);
                    g2.draw(new Line2D.Float(srcTmpx,
                        srcTmpy, tgtTmpx, tgtTmpy));
                    g2.setStroke(lineStroke);
                  }
              }
          }

        // DRAW NODES
        FontMetrics fm = g2.getFontMetrics(fontStyle);
        for (int i = 0; i < cTable.numberOfNodes(); i++)
          {
            if (cTable.isHiddenNode(i) && !underEdit)
              continue;
            int deg = cTable.getDegree(i);
            String symbol = cTable.getAtom(i);
            String descrip = cTable.getDescrip(i);
            float symbolW = strWidth(symbol, g2, fm);
            float descripW = strWidth(descrip, g2, fm);
            float Tmpx = xPos(i);
            float Tmpy = yPos(i);
            if (underEdit && dragging && nodeSelect.get(i))
              {
                Tmpx += shiftX;
                Tmpy += shiftY;
              }
            // Draw Atomic Symbols
            g2.setFont(fontStyle);
            if (!symbol.equals("C")
                || cTable.isStraightBranch(i)
                || (showHydrogenPattern == hydrogen_ALL)
                || ((showHydrogenPattern == hydrogen_TERMINAL) && (deg <= 1)))
              {
                // set Color
                if (underEdit)
                  {
                    if (cTable.isHiddenNode(i))
                      g2.setColor(defaultHiddenColor);
                    else if (nodeSelect.get(i))
                      g2.setColor(editColor);
                    else
                      g2.setColor(fontColor);
                  }
                else
                  { // normal mode
                    g2.setColor(nodeColor[i]);
                  }

                g2.drawString(symbol,
                    (Tmpx - symbolW / 2f),
                    (Tmpy + fontHeight));

                if ((descrip != null)
                    && (!descrip.startsWith("H")
                        || (showHydrogenPattern == hydrogen_ALL)
                        || ((showHydrogenPattern == hydrogen_TERMINAL) && (deg <= 1)) || ((showHydrogenPattern == hydrogen_HETERO) && (!symbol
                        .equals("C")))))
                  {
                    boolean leftExists = false;
                    boolean rightExists = false;
                    boolean upExists = false;
                    boolean downExists = false;

                    int[] adj = cTable.getAdjs(i);
                    for (int j = 0; j < adj.length; j++)
                      {
                        float tx = xPos(adj[j]);
                        float ty = yPos(adj[j]);
                        if (underEdit && dragging
                            && nodeSelect.get(i))
                          {
                            tx += shiftX;
                            ty += shiftY;
                          }
                        double atan = Math.atan2(tx - Tmpx,
                            ty - Tmpy);
                        if ((atan < 2.1) && (1 < atan))
                          rightExists = true;
                        else if (Math.abs(atan) > 2.7)
                          upExists = true;
                        else if (Math.abs(atan) < 0.3)
                          downExists = true;
                        else if ((-2.1 < atan)
                            && (atan < -1))
                          leftExists = true;
                      }
                    for (int j = 0; j < cTable
                        .numberOfNodes(); j++)
                      {
                        if (cTable.isHiddenNode(j))
                          continue;
                        float tx = xPos(j);
                        float ty = yPos(j);
                        if (underEdit && dragging
                            && nodeSelect.get(i))
                          {
                            tx += shiftX;
                            ty += shiftY;
                          }

                        if (Math.abs(tx - Tmpx) > 20)
                          continue;
                        if (Math.abs(ty - Tmpy) > 20)
                          continue;
                        /*
                         * g2.draw(new
                         * Rectangle2D.Float(Tmpx + symbolW /
                         * 2f, Tmpy - 2 * fontHeight,
                         * descripW , 4 * fontHeight));
                         * g2.draw(new
                         * Rectangle2D.Float(Tmpx - symbolW /
                         * 2f - descripW, Tmpy - 2 *
                         * fontHeight, descripW, 4 *
                         * fontHeight)); g2.draw(new
                         * Rectangle2D.Float(Tmpx - symbolW /
                         * 2f, Tmpy - 4 * fontHeight,
                         * descripW, 3 * fontHeight));
                         * g2.draw(new
                         * Rectangle2D.Float(Tmpx - symbolW /
                         * 2f, Tmpy + fontHeight, descripW,
                         * 3 * fontHeight));
                         */
                        if (regionContains(Tmpx + symbolW
                            / 2f, Tmpy - 2 * fontHeight,
                            descripW, 4 * fontHeight, tx,
                            ty))
                          rightExists = true;
                        if (regionContains(Tmpx - symbolW
                            / 2f - descripW, Tmpy - 2
                            * fontHeight, descripW,
                            4 * fontHeight, tx, ty))
                          leftExists = true;
                        if (regionContains(Tmpx - symbolW
                            / 2f, Tmpy + fontHeight,
                            descripW, 3 * fontHeight, tx,
                            ty))
                          downExists = true;
                        if (regionContains(Tmpx - symbolW
                            / 2f, Tmpy - 4 * fontHeight,
                            descripW, 3 * fontHeight, tx,
                            ty))
                          upExists = true;
                      }

                    float xShift = symbolW / 2;
                    float yShift = fontHeight;
                    if (!rightExists)
                      ;
                    else if (!leftExists)
                      xShift -= (symbolW + descripW);
                    else if (!downExists)
                      {
                        yShift += 1.7f * fontHeight;
                        xShift -= symbolW;
                      }
                    else if (!upExists)
                      {
                        yShift -= 1.7f * fontHeight;
                        xShift -= symbolW;
                      }

                    int descLen = descrip.length();
                    for (int j = 0; j < descLen;)
                      {
                        String sym = null;
                        int beg, end;
                        // 原子とその添え字を一塊として取り出す
                        if (rightExists && !leftExists)
                          {
                            beg = end = descLen - (j + 1);
                            char c = descrip.charAt(end);
                            while ((beg > 0)
                                && (Character.isDigit(c) || Character
                                    .isLowerCase(c)))
                              c = descrip.charAt(--beg);
                          }
                        else
                          {
                            beg = end = j;
                            while (end + 1 < descLen)
                              {
                                char c = descrip
                                    .charAt(end + 1);
                                if (Character.isDigit(c)
                                    || Character
                                        .isLowerCase(c))
                                  ++end;
                                else
                                  break;
                              }
                          }
                        sym = descrip.substring(beg,
                            end + 1);
                        j += (end + 1 - beg);
                        for (int k = 0; k < sym.length(); k++)
                          {
                            char c = sym.charAt(k);
                            if (Character.isDigit(c))
                              {
                                g2.setFont(fontStyle
                                    .deriveFont(fontStyle
                                        .getSize() * 0.7f));
                              }
                            g2.drawString(c + "",
                                (Tmpx + xShift),
                                (Tmpy + yShift));
                            if (Character.isDigit(c))
                              {
                                xShift += charWidth(c, g2,
                                    fm) * 0.8f;
                                g2.setFont(fontStyle);
                              }
                            else
                              xShift += charWidth(c, g2, fm);
                          }
                      }
                  }
              }
            // 価数や原子のポジション番号
            if (underEdit)
              {// edit
                if (nodeSelect.get(i))
                  g2.setColor(editColor);
                else
                  g2.setColor(fontColor);
              }
            else if (nodeSelect != null)
              {
                if (nodeSelect.get(i))
                  {
                    GradientPaint gp = new GradientPaint(
                        Tmpx, Tmpy, highlightColor, Tmpx
                            + 1.4f * fontHeight, Tmpy
                            + 1.4f * fontHeight,
                        Color.white, true);
                    g2.setComposite(tranC);
                    g2.setPaint(gp);
                    Ellipse2D.Float circle = new Ellipse2D.Float(
                        (Tmpx - 2 * fontHeight),
                        (Tmpy - 2 * fontHeight),
                        (fontHeight * 4), (fontHeight * 4));
                    g2.fill(circle);
                    g2.setComposite(origC);
                  }
              }

            int massDiff = react.molF.massDiff1[i];
            int charge = react.molF.charge2[i];
            if ((massDiff != 0) || (charge != 0))
              {
                g2.setColor(nodeColor[i]);
                g2.setFont(fontStyle.deriveFont(fontStyle
                    .getSize() * 0.8f));
                if (massDiff != 0)
                  {
                    int num = (int) (MolMass
                        .atomicMass(symbol));
                    num += massDiff;
                    String script = String.valueOf(num);
                    float w = strWidth(script, g2, g2
                        .getFontMetrics(g2.getFont()));
                    g2.drawString(script,
                        (xPos(i) - w - symbolW), yPos(i));
                  }
                else
                  {
                    String script = "";
                    switch (charge) {
                    case 1:
                      script = "3+";
                      break;
                    case 2:
                      script = "2+";
                      break;
                    case 3:
                      script = "+";
                      break;
                    case 4:
                      script = "^";
                      break;
                    case 5:
                      script = "-";
                      break;
                    case 6:
                      script = "2-";
                      break;
                    case 7:
                      script = "3-";
                      break;
                    }
                    g2.drawString(script,
                        (xPos(i) + symbolW / 2),
                        yPos(i) - 2);
                  }
              }
          }

        // Draw numbers to each node
        if (showAtomNumber)
          {
            g2.setColor(numberColor);
            for (int i = 0; i < cTable.numberOfNodes(); i++)
              {
                if (cTable.isHiddenNode(i) && !underEdit)
                  continue;
                String symbol = cTable.getAtom(i);
                float symbolW = strWidth(symbol, g2, fm);
                g2.drawString(String.valueOf(i + 1),
                    (xPos(i) + symbolW / 2), yPos(i) + 10);
              }
          }

        // 保存したポジションを回復
        if (parent != null)
          {
            rectBound.x = pointReserve.x;
            rectBound.y = pointReserve.y;
          }
      }

    public void showMOLformatDialog(JFrame parent)
      {
        String text = react.molF.toString();
        util.Dialogs jD = util.Dialogs
            .createTextAreaDialog(parent, text,
                "MOL format of " + data);
        jD.setPreferredSize(new Dimension(600, 400));
        // highlighting every 5 lines
        JTextArea jt = jD.getTextArea();
        try
          {
            int limit = react.molF.numberOfNodes + 4;
            int line = 4;
            while (line < limit)
              {
                jt.getHighlighter().addHighlight(
                    jt.getLineStartOffset(line),
                    jt.getLineStartOffset(Math.min(
                        line + 5, limit)), dhp1);
                line += 10;
              }
            line = limit;
            limit = 3 + react.molF.numberOfNodes
                + react.molF.numberOfEdges;
            while (line < limit)
              {
                jt.getHighlighter().addHighlight(
                    jt.getLineStartOffset(line),
                    jt.getLineStartOffset(Math.min(
                        line + 5, limit)), dhp2);
                line += 10;
              }

          }
        catch (BadLocationException ble)
          {}
        jD.pack();
        jD.setVisible(true);
      }

    public void showSmilesFormatDialog(JFrame parent)
      {
        Dialogs jD = Dialogs.createTextAreaDialog(parent,
            react.toSmiles(true), "SMILES format of "
                + data);
        jD.setPreferredSize(new Dimension(550, 100));
        jD.pack();
        jD.setVisible(true);
      }

    static public void saveMolFile(MOLformat mf)
      {
        if (chooser == null)
          {
            chooser = new JFileChooser(".");
            chooser
                .addChoosableFileFilter(new CustomFileFilter(
                    "mol", "MOL File"));
          }

        BasicFileChooserUI ui = (BasicFileChooserUI) chooser
            .getUI();
        ui.setFileName(null);
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
          return;
        try
          {
            File f = chooser.getSelectedFile();
            PrintWriter pw = new PrintWriter(
                new FileWriter(f));
            pw.print(mf.toString());
            pw.close();
          }
        catch (Exception e)
          {
            System.err.println(e);
          }
      }

    public void setHighlights(BitSet bs)
      {
        nodeSelect = bs;
      }

    public BitSet getHighlights()
      {
        return nodeSelect;
      }

    public void clearHighlights()
      {
        highlighted = false;
        nodeSelect.clear();
      }

    public int getPositionToHighlight(Point2D.Float p,
        boolean includeOxygen)
      {
        final int TOLERANCE = 4;
        ConnectionTable cTable = react.cTable;
        for (int i = 0; i < cTable.numberOfNodes(); i++)
          {
            if (cTable.isHiddenNode(i))
              continue;
            if (!includeOxygen
                && !react.molF.atomSymbol[i].equals("C")
                && !react.molF.atomSymbol[i].equals("N")
                && !react.molF.atomSymbol[i].equals("R")
                && !react.molF.atomSymbol[i].equals("Bs"))
              continue;
            if (p.distance(xPos(i), yPos(i)) < TOLERANCE)
              return i;
          }
        return -1;
      }

    public void highlightPosition(Graphics2D g2, int i)
      {
        Ellipse2D.Float circle = new Ellipse2D.Float(
            (xPos(i) - fontHeight), (yPos(i) - fontHeight),
            (fontHeight * 2), (fontHeight * 2));
        g2.draw(circle);
      }

    public void drawStringAt(Graphics2D g2, int i,
        String str)
      {
        if ((i < 0) || (i > react.molF.numberOfNodes))
          return;
        float x = xPos(i);
        float y = yPos(i);
        g2.drawString(str, x, y + fontHeight);
      }

    protected int returnBrokenBondPosition()
      {
        int numberOfEdges = react.cTable.numberOfEdges();
        // Prepare bond information
        int breakp = -1;
        for (int p = 0; p < numberOfEdges; p++)
          {
            int vin = react.molF.bondBlock[p][0] - 1;
            int vout = react.molF.bondBlock[p][1] - 1;
            if ((nodeSelect.get(vin) && !nodeSelect
                .get(vout))
                || (!nodeSelect.get(vin) && nodeSelect
                    .get(vout)))
              {
                if (breakp != -1)
                  return -1;
                breakp = p;
              }
          }
        return breakp;
      }

    protected GeneralPath newBoundary(float w, float h)
      {
        zoomScale = (rectBound.width * h < rectBound.height
            * w) ? (w - 2 * pictureMargin) / origWidth
            : (h - 2 * pictureMargin) / origHeight;
        GeneralPath gp = new GeneralPath();
        ConnectionTable cTable = react.cTable;
        MOLformat molF = react.molF;
        if (cTable != null)
          for (int i = 0; i < cTable.numberOfNodes(); i++)
            {
              if (cTable.isHiddenNode(i) && !underEdit)
                continue;
              int[] adjs = cTable.getAdjs(i);
              for (int p = 0; p < adjs.length; p++)
                {
                  if (i > adjs[p])
                    continue;
                  float srcTmpx = (molF.tx[i] + origXoffset - origWidth / 2)
                      * zoomScale
                      * (flipH ? -1 : 1)
                      + origWidth
                      * zoomScale
                      / 2
                      + pictureMargin;
                  float srcTmpy = (-molF.ty[i]
                      + origYoffset - origHeight / 2)
                      * zoomScale
                      + origHeight
                      * zoomScale
                      / 2 + pictureMargin;
                  float tgtTmpx = (molF.tx[adjs[p]]
                      + origXoffset - origWidth / 2)
                      * zoomScale
                      * (flipH ? -1 : 1)
                      + origWidth
                      * zoomScale
                      / 2
                      + pictureMargin;
                  float tgtTmpy = (-molF.ty[adjs[p]]
                      + origYoffset - origHeight / 2)
                      * zoomScale
                      + origHeight
                      * zoomScale
                      / 2 + pictureMargin;
                  gp.moveTo(srcTmpx, srcTmpy);
                  gp.lineTo(tgtTmpx, tgtTmpy);
                }
            }
        gp.append(new Rectangle2D.Float(0, 0, w, h), false);
        return gp;
      }

    public AbstractComponent createNew()
      {
        return (AbstractComponent) this.clone();
      }

    public Object clone()
      {
        MolFigure a = (MolFigure) super.clone();
        a.rectBound_ = (rectBound_ != null) ? (Rectangle2D.Float) rectBound_
            .clone()
            : null;
        a.react = react;
        return a;
      }

    public void setAtomColors(int[] pos, Color col)
      {
        for (int i = 0; i < pos.length; i++)
          nodeColor[i] = col;
      }

    public void drawMapNumberings(Graphics2D g2, AtomMap M)
      {
        MOLformat molF = react.molF;
        short dir = -1;
        if (M.xID.equals(data.id()))
          dir = 0; // this molecule is X
        else if (M.yID.equals(data.id()))
          dir = 1; // this molecule is Y
        g2.setColor(Color.green);
        for (short j = 0; j < molF.numberOfNodes; j++)
          {
            short pos = M.getMapNumbering(j, dir);
            if (pos < 0)
              continue;
            float x = xPos(j);
            float y = yPos(j);
            g2
                .drawString(String.valueOf(pos), x - 4,
                    y + 4);
          }
      }

    public void setMappingColors(LinkedList mapL,
        int count, boolean highlightMap)
      { // highlightMap == true .... Called to show
        // specified mappings.
        // highlightMap == false ... Called to show
        // computed mappings.
        if (data == null)
          return;
        showEdgeColors = true;
        MOLformat molF = react.molF;
        if (highlightMap)
          {
            for (int i = 0; i < molF.numberOfNodes; i++)
              nodeColor[i] = Color.lightGray;
          }

        short[] nthColor = new short[molF.numberOfNodes];
        for (int i = 0; i < mapL.size(); i++)
          {
            AtomMap M = (AtomMap) (mapL.get(i));
            if (i >= mapColors.length)
              {
                System.err
                    .println("Mapping is probably incorrect.");
                System.err.print(M.toString());
                return;
              }
            Color c = mapColors[i];
            short dir = -1;
            if (M.xID.equals(data.id()))
              dir = 0; // X
            else if (M.yID.equals(data.id()))
              dir = 1; // Y
            for (int j = 0; j < molF.numberOfNodes; j++)
              if (M.isMapped(j, dir)
                  && (nthColor[j] <= count))
                {
                  nodeColor[j] = c;
                  (nthColor[j])++;
                }
          }
      }

    // FOR MOLEDIT.JAVA
    protected boolean isMouseOnSelected(Point p)
      {
        for (int i = 0; i < react.cTable.numberOfNodes(); i++)
          {
            if (nodeSelect.get(i)
                && (Math.abs(p.x - xPos(i)) <= 4)
                && (Math.abs(p.y - yPos(i)) <= 4))
              return true;
          }
        return false;
      }

    protected void selectRegion(Point fromP, Point toP,
        boolean additive)
      {
        for (int i = 0; i < react.cTable.numberOfNodes(); i++)
          {
            if ((fromP.x <= xPos(i)) && (xPos(i) <= toP.x)
                && (fromP.y <= yPos(i))
                && (yPos(i) <= toP.y))
              nodeSelect.set(i);
            else if (!additive)
              nodeSelect.clear(i);
          }
      }

    protected void selectPoint(Point p, boolean additive)
      {
        boolean one_selected = false;
        for (int i = 0; i < react.cTable.numberOfNodes(); i++)
          {
            if (!one_selected
                && (Math.abs(p.x - xPos(i)) <= 4)
                && (Math.abs(p.y - yPos(i)) <= 4))
              {
                nodeSelect.set(i);
                one_selected = true;
              }
            else if (!additive)
              nodeSelect.clear(i);
          }
      }

    public void showEditPanel(JFrame parent)
      {
        // Save the current figure information.
        BitSet tmpBs = nodeSelect;
        nodeSelect = new BitSet(react.cTable
            .numberOfNodes());
        float posx = rectBound.x;
        float posy = rectBound.y;
        Color fillCol = fillColor;
        float zoom = zoomScale;

        JFrame jd = new JFrame();
        MolEdit molP = new MolEdit(jd, data, react);
        jd.getContentPane().add(molP);
        jd.pack();
        jd.setVisible(true);

        // Restore the figure information.
        nodeSelect = tmpBs;
        rectBound.x = posx;
        rectBound.y = posy;
        fillColor = fillCol;
        zoomScale = zoom;
        Draw2D.assignEdgeChirality(react);
        setRectBound();
        underEdit = false;
      }

    public String getMolecularInformation(
        boolean includeSymmetry)
      {
        StringBuffer sb = new StringBuffer();
        sb.append("<ID>\t");
        sb.append(data.id());
        sb.append('\n');
        sb.append("<NAME>\t");
        sb.append(data.name().replace(';', '\n'));
        String formula = react.toFormula();
        sb.append('\n');
        sb.append("<FORMULA>\t");
        sb.append(formula);
        sb.append('\n');
        sb.append("<EXACT_MASS>\t");
        sb
            .append(util.MolMass.molecularMass(formula,
                true));
        sb.append('\n');
        sb.append("<AVERAGE_MASS>\t");
        sb.append(util.MolMass
            .molecularMass(formula, false));
        sb.append('\n');

        if (!includeSymmetry)
          return sb.toString();

        short[] p = react.getSymmetricPositions();
        sb.append("<IS_SYMMETRIC>\t");
        sb.append(p != null);

        return sb.toString();
      }

    public void setGraphData(GraphData gn)
      {
        data.gData = gn;
      }

    public GraphData getGraphData()
      {
        return (data != null) ? data.gData : null;
      }

    public void setAmountData(Color col, int radius)
      {
        radiusColor = col;
        amountRadius = radius;
      }

    public List<ConnectorBase> getEnzymes()
      { // /Used to trace positions
        if (connected == null)
          return null;
        List<ConnectorBase> L = new ArrayList<ConnectorBase>();
        for (int i = 0; i < connected.size(); i++)
          {
            LineRepository.ConnectorBase cd = (LineRepository.ConnectorBase) connected
                .get(i);
            if (cd instanceof LineRepository.EnzArrow)
              L.add(cd);
          }
        return L;
      }

    protected void bufferDataFormat(StringBuffer sb, Map M)
      {
        super.bufferDataFormat(sb, M);
        sb.append("ID\t");
        sb.append(data.id());
        sb.append("\n");

        sb.append("Zoom\t");
        sb.append(zoomScale);
        sb.append(" ");
        sb.append(flipH);
        sb.append(" ");
        sb.append("\n");

        sb.append("Prop\t");
        sb.append("\n");
      }

    protected String processReadLine(
        java.io.BufferedReader br)
        throws java.io.IOException
      {
        clear();
        String line = super.processReadLine(br);
        if (line.startsWith("ID\t"))
          {
            String id = line.substring(3).trim();
            data = doctype.AbstractDoc.compHash.get(id);
            if (doctype.AbstractDoc.IDtoNode != null)
              {
                GraphNode gn = doctype.AbstractDoc.IDtoNode
                    .get(id);
                if (gn == null)
                  System.out.println("No data found for "
                      + id);
                else
                  data.gData = gn.inf();
              }
            line = br.readLine();
          }
        if (line.startsWith("Zoom\t"))
          {
            StringTokenizer st = new StringTokenizer(line);
            st.nextToken(); // discard "Zoom"
            zoomScale = Float.valueOf(st.nextToken())
                .floatValue();
            flipH = Boolean.valueOf(st.nextToken())
                .booleanValue();
            line = br.readLine();
          }
        if (line.startsWith("Prop\t"))
          {
            StringTokenizer st = new StringTokenizer(line);
            st.nextToken(); // discard "Prop"
            /*
             * for (int i = 0; i < cbMenuValue.length; i++) {
             * if (!st.hasMoreTokens()) break;
             * cbMenuValue[i] = Boolean.valueOf(
             * st.nextToken()).booleanValue(); }
             */
            lineColor = defaultLineColor;
            lineStroke = defaultLineStroke;
            fontColor = defaultFontColor;
            fontStyle = defaultFontStyle;
            fixedRatio = true;
            setMolCoordinates();
            setColors();
            setEditing(false);

            line = br.readLine();
          }
        return line;
      }

    public void instantiateChemicalGroups()
      {
        if (compMenu == null)
          {
            int[] pos = react.molF.abstractPositions();
            if (pos != null)
              compMenu = new CompoundAmountMenu(pos, this);
          }
        if (compMenu != null)
          compMenu.init();
      }

    void drawCompoundAmountDistribution(Graphics2D g2,
        Composite origC)
      {
        // Compound Amount for Abstract Molecules
        if (compMenu != null)
          {
            int[][] drawInfo = compMenu.drawingInfo;
            int[] assignments = compMenu.assignments;
            g2.setComposite(tranC2);
            for (int i = 0; i < assignments.length; i++)
              {
                for (int j = 0; j < drawInfo[i].length; j++)
                  {
                    if (i == 0)
                      {
                        g2.setFont(fontStyle);
                        g2.setPaint(Color.black);
                        g2.drawString(Integer
                            .toString(j * 2 + 12),
                            (rectBound.x + rectBound.width
                                / 2 - 1.5f * fontHeight),
                            (rectBound.y + fontHeight * 4
                                * (j + 0.5f)));
                      }
                    g2.setPaint(Color.green);
                    if (assignments[i] != 0)
                      {
                        /*
                         * String s =
                         * lipid.Constants.fattyAcids[assignments[i]][0];
                         * int l = Integer.parseInt(s
                         * .substring(0, s.indexOf(":")));
                         * if ((l - 12) / 2 == j)
                         * g2.setPaint(Color.red);
                         */
                      }
                    if (i == 0)
                      g2
                          .fill3DRect(
                              (int) (rectBound.x
                                  + rectBound.width / 2
                                  - fontHeight * 2 - drawInfo[i][j] / 2),
                              (int) (rectBound.y + fontHeight
                                  * 4 * j),
                              drawInfo[i][j] / 2,
                              (int) fontHeight * 3, true);
                    else
                      g2
                          .fill3DRect(
                              (int) (rectBound.x
                                  + rectBound.width / 2 + fontHeight * 2),
                              (int) (rectBound.y + fontHeight
                                  * 4 * j),
                              drawInfo[i][j] / 2,
                              (int) fontHeight * 3, true);
                  }
              }
            g2.setComposite(origC);
          }
      }

    protected void doubleClickProcess()
      {
        showEditPanel(pane.getFrame());
      }

  }