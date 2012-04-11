package visualization;

import java.awt.Color;
import java.util.List;
import java.util.Observable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.Highlighter.Highlight;

import legacy.bioinformatics.GPdbUtils;
import legacy.util.Utilities;

import org.apache.commons.lang.StringUtils;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
/**
 * By default, uses ribbons;
 * This class creats jmol commands
 * @author jayunit100
 *
 */
public class JMolWrapper extends Observable {
	public static String firstCommand = "select all;";
	int hr = 20;
	int hg = 100;
	int hb = 2;

	public void vennStyle()
	{
		this.execute("select dna; color green; select rna; color purple; restrict not hoh; spacefill 0; ribbons; wireframe off;");
	}
	public void martyStyle()
	{
		this.execute("select dna; color green; select rna; color purple; restrict not hoh; spacefill on; wireframe off;");
	}

	public JMolWrapper(Structure s) {
		setStructure(s);
	}

	Structure s1;

	public void setDefaultColors() {
		this.execute("select * ; set defaultColors Jmol;");
	}

	/**
	 * Modifies a biojava structure by non numeric atom groups.
	 * 
	 * @param s
	 */
	public void setStructure(Structure s) {
		s1 = s;
		for (Chain c : s.getChains()) {
			Vector<Group> cgr = new Vector<Group>();

			for (int i = 0; i < c.getAtomGroups().size(); i++) {
				// add all atom groups that dont have numbers for their pdb
				// code.
				if (!Utilities.isNumber(c.getAtomGroup(i).getPDBCode())) {
					cgr.add(c.getAtomGroup(i));
					System.err.println("removing group" + " "
							+ c.getAtomGroup(i).getPDBCode() + " "
							+ c.getAtomGroup(i).getPDBName());
				} else {
					if (i % 20 == 0)
						;
					// System.out.print(i+",");
				}
			}
			for (Group g : cgr) {
				c.getAtomGroups().remove(g);
			}
		}
	}

	/**
	 * range : 10-700 700 gives a full looking model.
	 * 
	 * @param c
	 */
	public void spacefill(int c) {
		execute("wireframe off; spacefill " + c + " ;");
	}

	public void highlight(Chain c, Highlight h, int[] rgb) {
		this.highlight(c, h.getStartOffset(), h.getEndOffset(), new Color(
				rgb[0], rgb[1], rgb[2]));
	}

	public void highlight(String chainName, int start, int end, Color co) {
		try {
			Chain c = this.s1.getChainByPDB(chainName);
			this.highlight(c, start, end, co);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Highlights the index at the "getGroupAt" method of PdbUtils
	 * 
	 * @param c
	 * @param start
	 * @param end
	 * @param rgb
	 */
	public void highlight(Chain c, int start, int end, Color rgb) {
		for (int i = start; i < end; i++) {
			String mrgb = "[<0>,<1>,<2>]";
			mrgb = Utilities.StringUt.replaceParameters(mrgb, rgb.getRed(), rgb
					.getGreen(), rgb.getBlue());
			// //System.out.println(mrgb);
			highlight(c.getName().charAt(0), GPdbUtils.getGroupAt(c, i), mrgb);
		}
	}

	
	public void highlight(Character chain, Group g, Color rgb) {
		Integer[] i = new Integer[3];
		i[0] = rgb.getRed();
		i[1] = rgb.getGreen();
		i[2] = rgb.getBlue();

		String color = "[" + StringUtils.join(i, ",") + "]";
		execute("select " + g.getPDBCode() + ":" + chain + "; color " + color);
	}

	/**
	 * Highlights all subsequences with a sequence motif.
	 * @param c1
	 * @param regex
	 * @param rgb
	 */
	public void highlightMotif(Chain c1, String regex, float[] rgb) {
		List<Chain> chains = s1.getChains();
		StringBuffer myStringBuffer = new StringBuffer();
		Pattern myP = Pattern.compile(regex);

		try {
			Matcher myMatcher = myP.matcher(c1.getAtomSequence());
			while (myMatcher.find()) {
				int start = myMatcher.start();
				int end = myMatcher.end();
				// System.out.println("matching " + start + " " + end +
				// c1.getAtomSequence().substring(start,end));

				for (int i = start; i < end + 1; i++) {
					// System.out.println("this function is disabled.");
					Color c = new Color(rgb[0], rgb[1], rgb[2]);
					this.highlight(c1, i, i + 1, c);
				}

			}
			myMatcher.appendTail(myStringBuffer);
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getSequence(Chain chain) {
		try {
			String sequence = chain.getAtomSequence();
			return sequence;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void echo(String s)
	{
		execute("('echo '\"" + s + "\"');");
	}
	/**
	 * First argument is the atom type (i.e. CA,C,O,etc.)  Tested with "CA"
	 * @param g
	 * @param s
	 */
	public void label(String atom,Group g, String s) {
		execute("select: " + g.getParent().getName() + " and *."+ atom +" and resno="
				+ g.getPDBCode() + "; label " + s + " ;");
	}

	public void residueAction(Character chain, Group ag, String command) {
		String q = "select: " + chain + " and resno=" + ag.getPDBCode() + ";"
				+ command;
		// System.out.println("command="+q);
		this.execute(q);
	}

	public void sidechains(boolean on) {
		if (!on) {
			this.execute("select: sidechain; spacefill off; wireframe off;");
		} else
			this.execute("select: sidechain; spacefill off; wireframe on;");
	}

	public void spacefill(int x, Character chain, Group ag) {
		String q = "spacefill <0>;";
		q = Utilities.StringUt.replaceParameters(q, x);
		// System.out.println("command="+q);
		this.residueAction(chain, ag, q);
	}

	public void highlight(Character chain, Group ag, String color) {
		try {
			if (s1.getChainByPDB(chain + "") == null) {
				System.err.println("invalid chain ");
				return;
			}

			// System.out.println("highlighting " + ag.getPDBName() + " "
			// +ag.getPDBCode());
			// System.out.println("select :" + chain +
			// " and resno="+ag.getPDBCode()+" ; color " +color+" ;");
			execute("select :" + chain + " and resno=" + ag.getPDBCode()
					+ " ; color " + color);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Vector<String> jMolCommands = new Vector<String>();

	public String getScript() {
		String sc = "";

		for (String s : jMolCommands) {
			sc += (s + ";");
		}

		return sc;
	}

	public void execute(String s) {
		jMolCommands.add(s);
	}

	public static void main(String[] args) {
		JMolWrapper r = new JMolWrapper(GPdbUtils.getStructure("1G65" ));
		r.spacefill(200);
		// while(1==1)
		{
			// String chain_res=PdbUtils.read("input chain-residue");
			// r.highlight(chain_res.split("-")[0].charAt(0),Integer.parseInt(chain_res.split("-")[1]),Colors.MAROON);
			// r.highlight('A', 80, 90, Colors.GREEN);
			// r.highlightMotif("P..P", Colors.BROWN);

			Vector<String> s = new Vector<String>();
			// for(String si : chain_res.split(","))
			// {
			// s.add(si);
			// }
			// r.highlight('B', s);
		}

	}

}