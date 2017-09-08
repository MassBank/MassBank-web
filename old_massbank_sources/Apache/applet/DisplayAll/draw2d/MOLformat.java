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
 * MOLformat Class
 *
 * ver 1.0.1 2008.12.05
 *
 ******************************************************************************/

package draw2d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

public class MOLformat implements Cloneable
	{
		static private final String BLANKSPACE = "          ";

		// Three comment lines.
		public String comm1 = "";

		public String comm2 = "";

		public String comm3 = "";

		public String countLine = "";

		public int numberOfNodes = 0;

		public int numberOfEdges = 0;

		public int j1 = 0;

		public int j2 = 0;

		public int chiralFlag = 0;

		// The atom block values.
		public float[] tx = null;

		public float[] ty = null;

		public float[] tz = null;

		public String[] atomSymbol = null;

		public short[] massDiff1 = null;

		public short[] charge2 = null;

		public short[] stereoParity3 = null;

		public short[] hydrogen4 = null;

		// The bond block values.
		public short[][] bondBlock = null;

		public String trailers = "M  END\n";

		public MOLformat copyCoordinates()
			{
				MOLformat mf;
				try
					{
						mf = (MOLformat) (this.clone());
					}
				catch (CloneNotSupportedException e)
					{
						System.out.println(e);
						return null;
					}
				mf.tx = new float[numberOfNodes];
				mf.ty = new float[numberOfNodes];
				mf.tz = new float[numberOfNodes];
				mf.charge2 = new short[numberOfNodes];
				for (int i = 0; i < numberOfNodes; i++)
					{
						mf.tx[i] = tx[i];
						mf.ty[i] = ty[i];
						mf.tz[i] = tz[i];
						mf.charge2[i] = charge2[i];
					}
				mf.bondBlock = new short[numberOfEdges][4];
				for (int i = 0; i < numberOfEdges; i++)
					{
						for (int j = 0; j < 4; j++)
							mf.bondBlock[i][j] = bondBlock[i][j];
					}
				return mf;
			}

		private int getBondNumber(int atom1, int atom2)
			{
				for (int i = 0; i < numberOfEdges; i++)
					{
						if ((bondBlock[i][0] == atom1 + 1)
								&& (bondBlock[i][1] == atom2 + 1))
							return (i + 1);
						if ((bondBlock[i][1] == atom1 + 1)
								&& (bondBlock[i][0] == atom2 + 1))
							return (i + 1);
					}
				return -1;
			}

		protected void setBondStereo(int from, int to,
				int up_or_down)
			{
				from++;
				to++;
				for (int i = 0; i < numberOfEdges; i++)
					{
						if ((bondBlock[i][0] == from)
								&& (bondBlock[i][1] == to))
							{
								bondBlock[i][3] = (short) up_or_down;
								return;
							}
						else if ((bondBlock[i][0] == to)
								&& (bondBlock[i][1] == from))
							{
								bondBlock[i][0] = (short) from;
								bondBlock[i][1] = (short) to;
								bondBlock[i][3] = (short) up_or_down;
								return;
							}
					}
			}

		public void read(String str)
			{
				int pos = 0;
				String[] lines = str.split("\n");
				comm1 = lines[0];
				comm2 = lines[1];
				comm3 = lines[2];
				// Read the counts line.
				countLine = lines[3];
				if (countLine.length() < 15)
					{
						throw new IllegalStateException(
								"can't parse the count line");
					}
				numberOfNodes = Integer.parseInt(countLine
						.substring(0, 3).trim());
				numberOfEdges = Integer.parseInt(countLine
						.substring(3, 6).trim());
				String n = countLine.substring(6, 9).trim();
				if (n.length() > 0)
					j1 = Integer.parseInt(n);
				n = countLine.substring(9, 12).trim();
				if (n.length() > 0)
					j2 = Integer.parseInt(n);
				n = countLine.substring(12, 15).trim();
				if (n.length() > 0)
					chiralFlag = Integer.parseInt(n);
				countLine = countLine.substring(15);

				// Initializes the arrays given the array size.
				tx = new float[numberOfNodes];
				ty = new float[numberOfNodes];
				tz = new float[numberOfNodes];
				atomSymbol = new String[numberOfNodes];
				massDiff1 = new short[numberOfNodes];
				charge2 = new short[numberOfNodes];
				stereoParity3 = new short[numberOfNodes];
				hydrogen4 = new short[numberOfNodes];
				bondBlock = null;
				trailers = "";

				// Read the atom block.
				pos = 4;
				for (int i = 0; i < numberOfNodes; i++)
					{
						String line = lines[pos + i];
						if (line.length() < 48)
							{
								throw new IllegalStateException(
										"can't read atom block");
							}
						tx[i] = (float) FortranFormat.atof(line
								.substring(0, 10).trim());
						ty[i] = (float) FortranFormat.atof(line
								.substring(10, 20).trim());
						tz[i] = (float) FortranFormat.atof(line
								.substring(20, 30).trim());
						atomSymbol[i] = line.substring(31, 34).trim();
						massDiff1[i] = Short.parseShort(line.substring(
								34, 36).trim());
						charge2[i] = Short.parseShort(line.substring(
								36, 39).trim());
						stereoParity3[i] = Short.parseShort(line
								.substring(39, 42).trim());
					}

				// Read the bond block.
				pos = 4 + numberOfNodes;
				for (int i = 0; i < numberOfEdges; i++)
					{
						String line = lines[pos + i];
						if (line.length() < 18)
							{
								throw new IllegalStateException(
										"can't read bond block");
							}
						if (bondBlock == null)
							bondBlock = new short[numberOfEdges][7];
						for (int j = 0; j < 6; j++)
							{
								n = line.substring(j * 3, (j + 1) * 3)
										.trim();
								if (n.length() > 0)
									bondBlock[i][j] = Short.parseShort(n);
							}
						if (line.length() > 20)
							bondBlock[i][6] = Short.parseShort(line
									.substring(18, 21).trim());
					}
				// Read the trailers.
				for (pos = 4 + numberOfNodes + numberOfEdges; pos < lines.length; pos++)
					trailers += lines[pos] + "\n";
				
				rescale();
			}

		public void read(BufferedReader br) throws IOException
			{
				String str = "";
				String line;
				while ((line = br.readLine()) != null)
					{
						str += line + "\n";
						if (line.equals("M  END"))
							break;
					}
				read(str);
			}

		private static String formatString(int i, int len)
			{
				String num = (new Integer(i)).toString();
				if (num.length() < len)
					num = BLANKSPACE.substring(0, len - num.length())
							+ num;
				return num;
			}

		private static String formatString(String sb, int len)
			{
				if (sb.length() < len)
					sb = BLANKSPACE.substring(0, len - sb.length())
							+ sb;
				return sb;
			}

		public String toString()
			{
				StringBuffer ret = new StringBuffer();
				ret.append(comm1);
				ret.append("\n");
				ret.append(comm2);
				ret.append("\n");
				ret.append(comm3);
				ret.append("\n");
				ret.append(formatString(numberOfNodes, 3));
				ret.append(formatString(numberOfEdges, 3));
				ret.append("  0  0");
				ret.append(formatString(chiralFlag, 3));
				for (int j = 0; j < 5; j++)
					ret.append("  0");
				ret.append("999 V2000");
				ret.append("\n");

				DecimalFormat axis = new DecimalFormat("#0.0000;-#");
				for (int i = 0; i < numberOfNodes; i++)
					{
						ret
								.append(formatString(axis.format(tx[i]), 10));
						ret
								.append(formatString(axis.format(ty[i]), 10));
						ret
								.append(formatString(axis.format(tz[i]), 10));
						ret.append(" ");
						ret.append(atomSymbol[i]);
						if ((atomSymbol[i]).length() == 1)
							ret.append(" ");
						ret.append(" ");

						if (massDiff1 == null)
							ret.append(" 0");
						else
							ret.append(formatString(massDiff1[i], 2));

						if (charge2 == null)
							ret.append("  0");
						else
							ret.append(formatString(charge2[i], 3));

						if (stereoParity3 == null)
							ret.append("  0");
						else
							ret.append(formatString(stereoParity3[i], 3));

						// hydrogen 4
						ret.append("  0");

						for (int j = 5; j <= 12; j++)
							ret.append("  0");
						ret.append("\n");
					}

				for (int i = 0; i < numberOfEdges; i++)
					{
						ret.append(formatString(bondBlock[i][0], 3));
						ret.append(formatString(bondBlock[i][1], 3));
						ret.append(formatString(bondBlock[i][2], 3));
						ret.append(formatString(bondBlock[i][3], 3));
						for (int j = 4; j <= 6; j++)
							ret.append("  0");
						ret.append("\n");
					}
				ret.append(trailers);
				return ret.toString();
			}

		public void write(OutputStream os)
			{
				PrintWriter bw = new PrintWriter(os);
				bw.print(toString());
				if (bw.checkError())
					System.err.println("MOL: Data Output error");
			}

		public int[] abstractPositions()
			{
				int c = 0;
				for (int i = 0; i < numberOfNodes; i++)
					{
						if (atomSymbol[i].startsWith("R")
								|| atomSymbol[i].startsWith("X"))
							c++;
					}
				if (c == 0)
					return null;

				int[] ret = new int[c];
				for (int i = 0; i < numberOfNodes; i++)
					{
						if (atomSymbol[i].startsWith("R")
								|| atomSymbol[i].startsWith("X"))
							ret[--c] = i;
					}
				return ret;
			}

		/*
		 * 座標の重複や結合の上書きが無いかどうかをチェックする
		 */
		public String checkDuplicateAtomsAndBonds()
			{
				String msg = null;
				for (int i = 0; i < numberOfNodes; i++)
					{
						if (tx[i] == 0 && ty[i] == 0)
							continue;
						for (int j = i + 1; j < numberOfNodes; j++)
							{
								if (Math.abs(tx[i] - tx[j]) < 0.1
										&& Math.abs(ty[i] - ty[j]) < 0.1)
									{
										if (msg == null)
											msg = "";
										msg += "原子位置近接 " + i + "個目"
												+ atomSymbol[i] + "\t" + j + "個目"
												+ atomSymbol[j] + "\n";
									}
							}
					}
				for (int i = 0; i < numberOfEdges; i++)
					{
						for (int j = i + 1; j < numberOfNodes; j++)
							{
								if ((bondBlock[i][0] == bondBlock[j][0] && bondBlock[i][1] == bondBlock[j][1])
										|| (bondBlock[i][0] == bondBlock[j][1] && bondBlock[i][1] == bondBlock[j][0]))
									{
										if (msg == null)
											msg = "";
										msg += "結合重複 " + bondBlock[i][0] + ""
												+ "←→" + bondBlock[i][1] + "\n";
									}
							}
					}
				return msg;
			}

		/**
		 * 特定原子の除去
		 */
		public boolean removeHydrogens()
			{
				boolean ret = false;

				for (int i = 0; i < numberOfNodes; i++)
					{
						if (!atomSymbol[i].equals("H"))
							continue;
						int parent = -1;
						int chiralType = 0;
						int edgeToDelete = -1;
						for (int j = 0; j < numberOfEdges; j++)
							{
								if (bondBlock[j][0] == i + 1)
									{ // bond "H -> ??"
										parent = bondBlock[j][1] - 1;
										chiralType = bondBlock[j][3];
										edgeToDelete = j;
										break;
									}
								else if (bondBlock[j][1] == i + 1)
									{ // bond "?? -> H"
										parent = bondBlock[j][0] - 1;
										chiralType = bondBlock[j][3];
										edgeToDelete = j;
										break;
									}
							}
						// Hが付属している親が４つの枝を持つならHは除かない。
						int pdegree = 0;
						for (int j = 0; j < numberOfEdges; j++)
							if ((bondBlock[j][0] == parent + 1)
									|| (bondBlock[j][1] == parent + 1))
								pdegree++;
						if (pdegree == 4)
							continue;

						// アトムブロックの末尾をi番目に移動すると不斉情報が
						// 崩れるのでi番目を消去し全体を前につめる
						for (int j = i + 1; j < numberOfNodes; j++)
							{
								tx[j - 1] = tx[j];
								ty[j - 1] = ty[j];
								tz[j - 1] = tz[j];
								atomSymbol[j - 1] = atomSymbol[j];
								charge2[j - 1] = charge2[j];
								stereoParity3[j - 1] = stereoParity3[j];
								hydrogen4[j - 1] = hydrogen4[j];
							}
						numberOfNodes--;

						if (edgeToDelete != -1)
							{
								// iに付属していた結合を消去
								int last = numberOfEdges - 1;
								for (int k = 0; k < 4; k++)
									bondBlock[edgeToDelete][k] = bondBlock[last][k];
								numberOfEdges--;
							}

						// i以降の原子についていたリンクを張り替える
						int childLargerThanH = 0;
						boolean hasChirality = false;
						int edgeToUpdate = -1;
						for (int j = 0; j < numberOfEdges; j++)
							{
								if (bondBlock[j][0] == parent + 1)
									{
										if (bondBlock[j][1] > i + 1)
											childLargerThanH++;
										hasChirality |= (bondBlock[j][3] != 0);
										if (bondBlock[j][3] == 0)
											edgeToUpdate = j;
									}
								if (bondBlock[j][1] == parent + 1)
									{
										if (bondBlock[j][0] > i + 1)
											childLargerThanH++;
										hasChirality |= (bondBlock[j][3] != 0);
										if (bondBlock[j][3] == 0)
											edgeToUpdate = j;
									}
								if (bondBlock[j][0] > i + 1)
									bondBlock[j][0]--;
								if (bondBlock[j][1] > i + 1)
									bondBlock[j][1]--;
							}

						// Chiralityの取り扱い
						if (parent != -1)
							{
								if (stereoParity3[parent] != 0)
									{// H (i番目)がなくなったのでパリティを変更
										if (childLargerThanH % 2 == 1)
											{
												if (stereoParity3[parent] == 1)
													stereoParity3[parent] = 2;
												else if (stereoParity3[parent] == 2)
													stereoParity3[parent] = 1;
											}
									}
								if (!hasChirality)
									{
										if (chiralType == 1)
											{// Hが上向きだったのでどれかを下向きにする
												bondBlock[edgeToUpdate][3] = 6;
											}
										else if (chiralType == 6)
											{// Hが下向きだったのでどれかを上向きにする
												bondBlock[edgeToUpdate][3] = 1;
											}
									}
							}
						ret = true;
						// i番目を消去して前に詰めているはずなので、もう一度iを評価
						i--;
					}
				return ret;
			}

		/*
		 * 不斉炭素情報の修正
		 */
		public boolean flipChiralityOfDegree4()
			{
				boolean ret = false;
				for (int i = 0; i < numberOfNodes; i++)
					{
						if (!atomSymbol[i].equals("C"))
							{
								if (stereoParity3[i] == 0)
									continue;
								int degree = 0;
								for (int j = 0; j < numberOfEdges; j++)
									{
										if ((bondBlock[j][0] - 1 == (short) i)
												|| (bondBlock[j][1] - 1 == (short) i))
											degree++;
									}
								if (degree == 4)
									{
										System.out.println(comm1
												+ " flip stereo parity at " + i);
										if (stereoParity3[i] == 1)
											stereoParity3[i] = 2;
										else if (stereoParity3[i] == 2)
											stereoParity3[i] = 1;
										ret = true;
									}
							}
					}
				return ret;
			}

		/*
		 * MOLfileのz座標をクリア
		 */
		public void clearZcoordinate()
			{
				for (int i = 0; i < numberOfNodes; i++)
					tz[i] = 0;
			}

		/**
		 * MOLfileの原子や結合指定部分で、表示に使われない部分をクリア
		 */
		public boolean clearAtomAndBondProperties(
				boolean clearChirality)
			{
				for (int i = 0; i < numberOfEdges; i++)
					{
						int j = clearChirality ? 3 : 5;
						for (; j < 7; j++)
							bondBlock[i][j] = 0;
					}
				for (int i = 0; i < numberOfNodes; i++)
					{
						massDiff1[i] = 0;
						hydrogen4[i] = 0;
					}
				return true;
			}

		public boolean removeOxygenCharges()
			{
				boolean ret = false;
				for (int i = 0; i < numberOfNodes; i++)
					{
						if (atomSymbol[i].equals("O"))
							{
								if (charge2[i] == 0)
									continue;
								// Count the degree of oxygen
								int degree = 0;
								for (int j = 0; j < numberOfEdges; j++)
									{
										if ((bondBlock[j][0] - 1 == (short) i)
												|| (bondBlock[j][1] - 1 == (short) i))
											degree++;
									}
								if (degree == 1)
									charge2[i] = 0;
								ret = true;
							}
					}
				return ret;
			}

		/*
		 * 構造中にあるＲやＢｓという構造を、ＲＧＰとしてＴｒａｉｌｅｒに登録
		 */
		public void registerRgroupInTrailer()
			{
				LinkedList<Integer> Rs = new LinkedList<Integer>();
				for (int i = 0; i < numberOfNodes; i++)
					{
						if (atomSymbol[i].equals("R")
								|| atomSymbol[i].equals("Bs"))
							Rs.addLast(new Integer(i));
					}
				if (Rs.size() > 0)
					{
						int pos = 1;
						trailers += "M  RGP";
						trailers += formatString(Rs.size(), 3);
						for (Iterator<Integer> I = Rs.iterator(); I
								.hasNext();)
							{
								int h = I.next().intValue();
								trailers += formatString(h + 1, 4)
										+ formatString(pos++, 4);
							}
						trailers += "\n";
					}
			}

		private void increaseNodeAndEdgeSize(int nodeDif,
				int edgeDif)
			{
				// atomBlockをコピー
				float[] newtx = new float[numberOfNodes + nodeDif];
				float[] newty = new float[numberOfNodes + nodeDif];
				float[] newtz = new float[numberOfNodes + nodeDif];
				String[] newatomSymbol = new String[numberOfNodes
						+ nodeDif];
				short[] newmassDiff1 = new short[numberOfNodes
						+ nodeDif];
				short[] newcharge2 = new short[numberOfNodes
						+ nodeDif];
				short[] newstereoParity3 = new short[numberOfNodes
						+ nodeDif];
				short[] newhydrogen4 = new short[numberOfNodes
						+ nodeDif];
				for (int i = 0; i < numberOfNodes; i++)
					{
						newtx[i] = tx[i];
						newty[i] = ty[i];
						newtz[i] = tz[i];
						newatomSymbol[i] = atomSymbol[i];
						newmassDiff1[i] = massDiff1[i];
						newcharge2[i] = charge2[i];
						newstereoParity3[i] = stereoParity3[i];
						newhydrogen4[i] = hydrogen4[i];
					}

				// bondBlockのコピー
				short[][] newbondBlock = new short[numberOfEdges
						+ edgeDif][7];
				for (int i = 0; i < numberOfEdges; i++)
					{
						for (int j = 0; j < 4; j++)
							newbondBlock[i][j] = bondBlock[i][j];
					}

				// 新しい構造と入れ替え
				numberOfNodes += nodeDif;
				numberOfEdges += edgeDif;
				tx = newtx;
				ty = newty;
				tz = newtz;
				atomSymbol = newatomSymbol;
				massDiff1 = newmassDiff1;
				charge2 = newcharge2;
				stereoParity3 = newstereoParity3;
				hydrogen4 = newhydrogen4;
				bondBlock = newbondBlock;
			}

		public void addCarbonCarbonDoubleBond(int atomPos)
			{ // もともとあるＣの位置にＣ＝Ｃを挿入
				// 原子１個分、結合１個分の空き領域を用意
				increaseNodeAndEdgeSize(1, 1);
				// add the new carbon atom
				tx[numberOfNodes - 1] = tx[atomPos - 1] + 1;
				ty[numberOfNodes - 1] = ty[atomPos - 1] + 1;
				tz[numberOfNodes - 1] = tz[atomPos - 1];
				atomSymbol[numberOfNodes - 1] = "C";
				massDiff1[numberOfNodes - 1] = 0;
				charge2[numberOfNodes - 1] = 0;
				stereoParity3[numberOfNodes - 1] = 0;
				hydrogen4[numberOfNodes - 1] = 0;
				// add the new double bond
				bondBlock[numberOfEdges - 1][0] = (short) atomPos;
				bondBlock[numberOfEdges - 1][1] = (short) numberOfNodes;
				bondBlock[numberOfEdges - 1][2] = (short) 2;
			}

		public void setChargeInfo(String line)
			{
				StringTokenizer s = new StringTokenizer(line);
				s.nextToken();
				s.nextToken();
				s.nextToken();
				while (s.hasMoreTokens())
					{
						int pos = Integer.parseInt(s.nextToken());
						int chg = Integer.parseInt(s.nextToken());
						pos--; // MOLformatクラスでは１でなく０からスタート
						if (pos < 0)
							{
								System.out.println(comm1
										+ ": invalid charge at " + pos);
								continue;
							}
						switch (chg) {
						case 0:
							charge2[pos] = 0;
							break;
						case 3:
							charge2[pos] = 1;
							break;
						case 2:
							charge2[pos] = 2;
							break;
						case 1:
							charge2[pos] = 3;
							break;
						case -1:
							charge2[pos] = 5;
							break;
						case -2:
							charge2[pos] = 6;
							break;
						case -3:
							charge2[pos] = 7;
							break;
						default:
							System.out.println(comm1
									+ ": unknown charge at " + pos);
							break;
						}
					}
			}

		/*
		 * トレイラーの"M CHG"処理
		 */
		public boolean processChargeInTrailer()
			{
				boolean ret = false;
				String newtrailers = new String();
				String[] L = trailers.split("\n");
				for (int i = 0; i < L.length; i++)
					{
						String line = L[i];
						if (line.startsWith("M  CHG"))
							{
								setChargeInfo(line);
								ret = true;
							}
						else
							newtrailers += line + "\n";
					}
				if (newtrailers.equals(""))
					trailers = "M  END\n";
				else
					trailers = newtrailers;
				return ret;
			}

		public boolean processCCDoubleBondInTrailer()
			{
				boolean ret = false;
				String newtrailers = new String();
				String[] L = trailers.split("\n");
				for (int i = 0; i < L.length; i++)
					{
						String line = L[i];
						if (line.startsWith("A    "))
							{// 原子グループ
								int position = Integer.parseInt(line
										.substring(4).trim());
								line = L[++i];
								if (line.equals("CH=CH")
										|| line.equals("CH=C")
										|| line.equals("C=CH")
										|| line.equals("C=C"))
									addCarbonCarbonDoubleBond(position);
								else
									{
										newtrailers += line + "\n";
									}
								ret = true;
							}
						else
							newtrailers += line + "\n";
					}
				if (newtrailers.equals(""))
					trailers = "M  END\n";
				else
					trailers = newtrailers;
				return ret;
			}

		public boolean foldChemicalGroup(
				Vector<int[]> groupList, String str)
			{
				// 登録されているスーパーアトムの番号最大値と、
				// スーパーアトム位置をチェック
				String[] L = trailers.split("\n");
				int maxSuperAtom = 0;
				for (int i = 0; i < L.length; i++)
					{
						String line = L[i];
						if (line.startsWith("M  STY")
								&& line.endsWith("SUP"))
							{
								String w = line.substring(9, 13).trim();
								int count = Integer.parseInt(w);
								if (count > maxSuperAtom)
									maxSuperAtom = count;
								line = L[++i];
								while (line.startsWith("M  SLB")
										|| line.startsWith("M  SAL")
										|| line.startsWith("M  SBL")
										|| line.startsWith("M  SMT")
										|| line.startsWith("M  SBV"))
									{
										if (line.startsWith("M  SAL"))
											{
												w = line.substring(13, 17).trim();
												int position = Integer.parseInt(w);
												// groupListの中にいま見ているスーパーアトムがあるか確認
												for (int j = 0; j < groupList
														.size(); j++)
													{
														int[] elem = groupList.get(j);
														if (elem[1] + 1 == position)
															elem[1] = -1;
													}
											}
										line = L[++i];
									}
								i--;
							}
					}
				// もしgroupListでポジションが－１になっていないものがあれば
				// スーパーアトムとして登録
				boolean ret = false;
				for (int j = 0; j < groupList.size(); j++)
					{
						int[] elem = groupList.get(j);
						if (elem[1] < 0)
							continue;
						ret = true;
						int src = elem[0];
						int tgt = elem[1];
						String label = formatString(++maxSuperAtom, 4);
						StringBuffer sb = new StringBuffer();
						sb.append("M  STY  1");
						sb.append(label);
						sb.append(" SUP\n");
						sb.append("M  SLB  1");
						sb.append(label);
						sb.append(label);
						sb.append("\n");
						sb.append("M  SAL");
						sb.append(label);
						sb.append(formatString(elem.length - 1, 3));
						for (int k = 1; k < elem.length; k++)
							sb.append(formatString(elem[k] + 1, 4));
						sb.append("\n");
						sb.append("M  SBL");
						sb.append(label);
						sb.append("  1");
						sb.append(formatString(getBondNumber(src, tgt),
								4));
						sb.append("\n");
						sb.append("M  SMT");
						sb.append(label);
						sb.append("  ");
						sb.append(str);
						sb.append("\n");
						sb.append("M  SVB");
						sb.append(label);
						sb.append(formatString(getBondNumber(src, tgt),
								3));
						sb.append(formatString(String.valueOf(tx[tgt]),
								10));
						sb.append(formatString(String.valueOf(ty[tgt]),
								10));
						sb.append("\n");
						trailers = sb.toString() + trailers;
					}
				return ret;
			}

		/*
		 * strで与えられた文字列を持つSuperAtom表記をトレイラーから除去する。
		 * strがnullのときは全てのsuperatom表記を除去。
		 */
		public void unfoldChemicalGroup(String str)
			{
				// 登録されているスーパーアトム位置をチェック
				String[] L = trailers.split("\n");
				if (L.length <= 1) // "M END" only
					return;
				boolean[] bL = new boolean[L.length];
				for (int i = 0; i < L.length; i++)
					{
						String line = L[i];
						if (line.startsWith("M  STY")
								&& line.endsWith("SUP"))
							{
								int initPos = i;
								boolean flag = false;
								line = L[++i];
								while (line.startsWith("M  SLB")
										|| line.startsWith("M  SAL")
										|| line.startsWith("M  SBL")
										|| line.startsWith("M  SMT")
										|| line.startsWith("M  SBV"))
									{
										if (line.startsWith("M  SMT"))
											{
												String s = line.substring(10)
														.trim();
												if (s.startsWith("^"))
													s = s.substring(1);
												// unfolding chemical group?
												if ((str == null) || s.equals(str))
													flag = true;
											}
										line = L[++i];
									}
								// for unfolding group, mask bL flags
								if (flag)
									// ここをどのラインまで消去するか？ K<i？？
									for (int k = initPos; k <= i; k++)
										bL[k] = true;
								i--;
							}
					}
				String newtrailers = "";
				for (int i = 0; i < bL.length; i++)
					if (!bL[i])
						newtrailers += L[i] + "\n";
				if (newtrailers.equals(""))
					newtrailers = "M  END\n";
				trailers = newtrailers;
			}

		public void foldAllChemicalGroups()
			{
				foldCOOH();
				foldOCH3();
				foldCH2OH();
			}

		public boolean foldCOOH()
			{
				ConnectionTable ct = new ConnectionTable(this, true);
				Vector<int[]> carboxylG = ChemicalGroups
						.getCOOH(ct);
				if (carboxylG == null)
					return false;
				return foldChemicalGroup(carboxylG, "COOH");
			}

		public boolean foldOCH3()
			{
				ConnectionTable ct = new ConnectionTable(this, true);
				Vector<int[]> methoxyG = ChemicalGroups.getOCH3(ct);
				if (methoxyG == null)
					return false;
				return foldChemicalGroup(methoxyG, "OCH3");
			}

		public boolean foldCH2OH()
			{
				ConnectionTable ct = new ConnectionTable(this, true);
				Vector<int[]> ethylG = ChemicalGroups.getCH2OH(ct);
				if (ethylG == null)
					return false;
				return foldChemicalGroup(ethylG, "CH2OH");
			}

		public Vector<String> getSuperAtomDescriptions()
			{
				Vector<String> v = null;
				String[] lines = trailers.split("\n");
				for (int i = 0; i < lines.length; i++)
					{
						if (!lines[i].startsWith("M  SMT"))
							continue;
						if (v == null)
							v = new Vector<String>();
						v.add(lines[i].substring(9));
					}
				return v;
			}

		public BitSet getSuperAtomPositions()
			{
				BitSet bs = new BitSet(numberOfNodes);
				String[] lines = trailers.split("\n");
				for (int i = 0; i < lines.length; i++)
					{
						if (!lines[i].startsWith("M  SAL"))
							continue;
						String[] nums = lines[i].split(" ");
						for (int j = 2; j < nums.length; j++)
							bs.set(Integer.parseInt(nums[j]));
					}
				return bs;
			}

		public Vector<String> getNonStandardAtoms()
			{
				Vector<String> v = null;
				for (int i = 0; i < numberOfNodes; i++)
					{
						String str = atomSymbol[i];
						if (Reactant.smilesAtoms.indexOf(str) >= 0)
							continue;
						if (v == null)
							v = new Vector<String>();
						v.add(str);
					}
				return v;
			}

		/*
		 * 3行あるコメント行を自動生成する機能。外部から定義ファイルを読み込む。
		 * fieldsがカギ括弧で括られるフィールド名、fieldInfoがタブ区切りのテキスト
		 * １行目にフィールド名、１列目にMOLファイル名。 // 2006/07/28修正
		 */
		public boolean generateCommentLines(String[] fields,
				String fieldInfo)
			{
				StringBuffer sb;
				String[] info = null;
				comm1 = "";
				comm2 = "";
				comm3 = "";
				/* comment line 1 */
				if (fieldInfo != null)
					{
						// フィールド末尾にデータがない場合、
						// \tで終わっているとsplitが削除してしまうのを防ぐ
						fieldInfo += "\tN.A.";
						info = fieldInfo.split("\t");
						if (info.length < fields.length)
							return false;
						sb = new StringBuffer();
						for (int i = 0; i < fields.length; i++)
							{
								sb.append("<");
								sb.append(fields[i]);
								sb.append("> ");
								sb.append(info[i]);
								if (i < fields.length - 1)
									sb.append("\t");
							}
						comm3 = sb.toString();
					}
				/* comment line */
				try
					{
						sb = new StringBuffer();
						sb.append("\t");
						Reactant r = new Reactant(null, this);
						sb.append("<FORMULA> ");
						String formula = r.toFormula();
						sb.append(formula);
						sb.append("\t<EXACTMASS> ");
						sb.append(util.MolMass.molecularMass(formula,
								true));
						sb.append("\t<AVERAGEMASS> ");
						sb.append(util.MolMass.molecularMass(formula,
								false));
						sb.append("\t<SMILES> ");
						sb.append(r.toSmiles(true));
						comm3 += sb.toString();
					}
				catch (Exception e)
					{
						System.err.println(fieldInfo);
						e.printStackTrace();
						return false;
					}
				/* comment line */
				comm3 += "\t<COPYRIGHT> ARM PROJECT http://www.metabolome.jp/";
				return true;
			}

		public String getComment()
			{
				return comm3;
			}

		public void rescale()
			{
				// Rescale the bond length so that the least
				// atom-atom distance will be 1.
				double scale = Double.MAX_VALUE;
				if (bondBlock == null)
					return;
				for (int i = 0; i < bondBlock.length; i++)
					{
						int x = bondBlock[i][0]-1;
						int y = bondBlock[i][1]-1;
						double dist = Math.sqrt((tx[x] - tx[y])
								* (tx[x] - tx[y]) + (ty[x] - ty[y])
								* (ty[x] - ty[y]) + (tz[x] - tz[y])
								* (tz[x] - tz[y]));
						if (dist < scale)
							scale = dist;
					}
				for (int i = 0; i < numberOfNodes; i++)
					{
						tx[i] /= scale;
						ty[i] /= scale;
						tz[i] /= scale;
					}
			}
	}
