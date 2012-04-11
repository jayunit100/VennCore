package standalones;

import java.awt.Color;
import java.io.File;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JFrame;

import legacy.bioinformatics.GPdbUtils;
import legacy.bioinformatics.GPdbUtils.ATOM;
import legacy.util.Utilities.Counter;
import legacy.util.Utilities.Maths;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Structure;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Input structure
 * @author vyas
 *
 */
public class NmrDC 
{
	//parallel vectors.
	Vector<Atom[]> modelAtoms = new Vector<Atom[]>();
	public double[] bins;
	String name;
	public NmrDC(String n, Structure nmr, int chain) throws Exception
	{
		name=n;

		Atom[] ref = GPdbUtils.getAtoms(GPdbUtils.getModel(nmr,0), ATOM.CA);
		
		//Reads in all atoms , 
		System.out.println("Reading in all atom arrays.");
		for(int m = 0 ; m <nmr.nrModels() ; m++)
		{
			Atom[] modelM = GPdbUtils.getAtoms(GPdbUtils.getModel(nmr, m),ATOM.CA);
			modelAtoms.add(modelM);
			System.out.println("Done reading atoms for model " + m);
		}
		//now we have the models ./.. 
		System.out.println("By Residue ");
		for(int i = 0 ; i < ref.length; i++)
		{
			Vector<Atom> atomRepresentatives=getVerticalAtoms(i);
			System.out.println(i + "=" +CoordinateL2(atomRepresentatives));
		}
		System.out.println("By Bin");
		Counter<Integer> c = new Counter<Integer>(Integer.class);
		TreeMap<Integer,Integer> sorted = new TreeMap<Integer,Integer>();
		
		for(int i = 0 ; i < ref.length; i++)
		{
			Vector<Atom> atomRepresentatives=getVerticalAtoms(i);
			int bin = CoordinateL2(atomRepresentatives);
			c.increment(bin);
			sorted.put(bin,c.get(bin));
		}
		plot(createBins(sorted));
		
	}
	public double[] createBins(TreeMap<Integer,Integer> sorted)
	{
		//rmsd 0.1 - 10 -> 1 -> 100
		double[] values = new double[50];
		for(int j = 0 ; j < values.length; j++)
		{
			if(sorted.containsKey(j))
			{
				values[j]=sorted.get(j);
			}
		}
		return values;
	}
	
	int high;
	public Vector<Atom> getVerticalAtoms(int i)
	{
		Vector<Atom> v1 = new Vector<Atom>();
		for(Atom[] array :modelAtoms)
		{
			v1.add(array[i]);
		}
		return v1;
	}
	
	/**
	 * Returns "10*" the mean. 
	 * so .1 would -> 1.  
	 * This keeps binning relatively simple.
	 * @param allAtoms
	 * @return
	 */
	public int Coordinate(Vector<Atom> allAtoms)
	{
		//get the mean atomic coordinate... 
		Atom aSTD = GPdbUtils.getStandardDeviation(allAtoms.toArray(new Atom[]{}));
		double mean = (aSTD.getX()+aSTD.getY()+aSTD.getZ())/3;
		
		//System.out.println("Mean : " + mean + " " + "... " + Math.round(mean));
		return (int)Math.round(10*mean);
	}

	/**
	 * Returns "10*" the mean. 
	 * so .1 would -> 1.  
	 * This keeps binning relatively simple.
	 * @param allAtoms
	 * @return
	 */
	public int CoordinateL2(Vector<Atom> allAtoms) throws Exception
	{
		//int d1=Coordinate(allAtoms);
		//get the mean atomic coordinate... 
		Atom aAverage= GPdbUtils.getAverage(allAtoms.toArray(new Atom[]{}));
		Vector<Double> distance = new Vector<Double>();
		for(Atom a1 : allAtoms)
		{
			distance.add(Calc.getDistance(a1, aAverage));
		}
		
		//System.out.println("Mean : " + mean + " " + "... " + Math.round(mean));

		
		int d2= (int)Math.round(10*Maths.stddev(distance.toArray(new Double[]{})));
		//System.out.println(d1 +"," + d2);
		return d2;
	}

	
	JFreeChart chart;
	public void plot(double[] v)
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for(int i = 0 ; i < v.length; i++)
		{
			String k ="";
			if(i%2==0)
				k=(((float)i/(float)10)+"").substring(0,3);
			dataset.addValue(v[i], k,k);
		}
		String plotTitle = "Histogram " + name; 
		String yaxis = "count";
		String xaxis = "mean std deviation"; 
		PlotOrientation orientation = PlotOrientation.VERTICAL; 
		boolean show = false; 
		boolean toolTips = false;
		boolean urls = false; 

		chart = ChartFactory.createBarChart( plotTitle, xaxis, yaxis, dataset, orientation, show, toolTips, urls);
		  CategoryPlot plot = chart.getCategoryPlot();
	        plot.setBackgroundPaint(Color.lightGray);
	        plot.setDomainGridlinePaint(Color.white);
	        plot.setRangeGridlinePaint(Color.white);
	        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer(); renderer.setItemMargin(-6);
		JFrame f= new JFrame();
		f.getContentPane().add(new ChartPanel(chart));
		f.setVisible(true);
		
		int width = 800;
		int height = 700; 
	}
	
	public void export(String name)
	{
		try 
		{
			ChartUtilities.saveChartAsPNG(new File("/raid/Users/vyas/Desktop/histogram"+name+".PNG"), chart, 500, 500);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{  
		File[] inputs = new File[]
	     {
			new File("/raid/Users/vyas/nmr/cyana/benchmark/A_15270/final.pdb"),
			new File("/raid/Users/vyas/nmr/cyana/benchmark/A_16790/final.pdb"),
			new File("/raid/Users/vyas/nmr/cyana/benchmark/A_6546/final.pdb")
	     };

		try
		{
			for(File f : inputs)
			{
				NmrDC nmrDc=new NmrDC(f.getParentFile().getName(),GPdbUtils.getStructure(f),0);

				nmrDc.export(f.getParentFile().getName());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
	}
}


