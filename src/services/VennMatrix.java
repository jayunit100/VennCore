/**
 * 
 */
package services;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import legacy.util.Utilities;

/**
 * 
 * 
 * Biojava does not supply a robust and simple API for matrices. This class
 * makes a simple int[][] array as a matrix which is to be used, for example with
 * the NeedlemanWunsch algorithm. 
 * 
 * It has empty constructor, and also a String constructor, which creates an int[][]
 * from a String which is in NCBI matrix format ftp://ftp.ncbi.nlm.nih.gov/blast/matrices/BLOSUM60 .
 * 
   
   #  Matrix made by matblas from blosum62.iij
#  * column uses minimum score
#  BLOSUM Clustered Scoring Matrix in 1/2 Bit Units
#  Blocks Database = /data/blocks_5.0/blocks.dat
#  Cluster Percentage: >= 62
#  Entropy =   0.6979, Expected =  -0.5209
   A  R  N  D  C  Q  E  G  H  I  L  K  M  F  P  S  T  W  Y  V  B  Z  X  *
A  4 -1 -2 -2  0 -1 -1  0 -2 -1 -1 -1 -1 -2 -1  1  0 -3 -2  0 -2 -1  0 -4 
R -1  5  0 -2 -3  1  0 -2  0 -3 -2  2 -1 -3 -2 -1 -1 -3 -2 -3 -1  0 -1 -4 
N -2  0  6  1 -3  0  0  0  1 -3 -3  0 -2 -3 -2  1  0 -4 -2 -3  3  0 -1 -4 
D -2 -2  1  6 -3  0  2 -1 -1 -3 -4 -1 -3 -3 -1  0 -1 -4 -3 -3  4  1 -1 -4 
C  0 -3 -3 -3  9 -3 -4 -3 -3 -1 -1 -3 -1 -2 -3 -1 -1 -2 -2 -1 -3 -3 -2 -4 
Q -1  1  0  0 -3  5  2 -2  0 -3 -2  1  0 -3 -1  0 -1 -2 -1 -2  0  3 -1 -4 
E -1  0  0  2 -4  2  5 -2  0 -3 -3  1 -2 -3 -1  0 -1 -3 -2 -2  1  4 -1 -4 
G  0 -2  0 -1 -3 -2 -2  6 -2 -4 -4 -2 -3 -3 -2  0 -2 -2 -3 -3 -1 -2 -1 -4 
H -2  0  1 -1 -3  0  0 -2  8 -3 -3 -1 -2 -1 -2 -1 -2 -2  2 -3  0  0 -1 -4 
I -1 -3 -3 -3 -1 -3 -3 -4 -3  4  2 -3  1  0 -3 -2 -1 -3 -1  3 -3 -3 -1 -4 
L -1 -2 -3 -4 -1 -2 -3 -4 -3  2  4 -2  2  0 -3 -2 -1 -2 -1  1 -4 -3 -1 -4 
K -1  2  0 -1 -3  1  1 -2 -1 -3 -2  5 -1 -3 -1  0 -1 -3 -2 -2  0  1 -1 -4 
M -1 -1 -2 -3 -1  0 -2 -3 -2  1  2 -1  5  0 -2 -1 -1 -1 -1  1 -3 -1 -1 -4 
F -2 -3 -3 -3 -2 -3 -3 -3 -1  0  0 -3  0  6 -4 -2 -2  1  3 -1 -3 -3 -1 -4 
P -1 -2 -2 -1 -3 -1 -1 -2 -2 -3 -3 -1 -2 -4  7 -1 -1 -4 -3 -2 -2 -1 -2 -4 
S  1 -1  1  0 -1  0  0  0 -1 -2 -2  0 -1 -2 -1  4  1 -3 -2 -2  0  0  0 -4 
T  0 -1  0 -1 -1 -1 -1 -2 -2 -1 -1 -1 -1 -2 -1  1  5 -2 -2  0 -1 -1  0 -4 
W -3 -3 -4 -4 -2 -2 -3 -2 -2 -3 -2 -3 -1  1 -4 -3 -2 11  2 -3 -4 -3 -2 -4 
Y -2 -2 -2 -3 -2 -1 -2 -3  2 -1 -1 -2 -1  3 -3 -2 -2  2  7 -1 -3 -2 -1 -4 
V  0 -3 -3 -3 -1 -2 -2 -3 -3  3  1 -2  1 -1 -2 -2  0 -3 -1  4 -3 -2 -1 -4 
B -2 -1  3  4 -3  0  1 -1  0 -3 -4  0 -3 -3 -2  0 -1 -4 -3 -3  4  1 -1 -4 
Z -1  0  0  1 -3  3  4 -2  0 -3 -3  1 -1 -3 -1  0 -1 -3 -2 -2  1  4 -1 -4 
X  0 -1 -1 -1 -2 -1 -1 -1 -1 -1 -1 -1 -1 -1 -2  0  0 -2 -1 -1 -1 -1 -1 -4 
* -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4 -4  1 

Taken from ftp://ftp.ncbi.nlm.nih.gov/blast/matrices/BLOSUM62
   
 * @author jayunit100
 *
 */
public class VennMatrix 
{
	public static String AMINO="ARNBDCQZEGHILKMFPSTWYVX*";
	
	public int[][] matrix = new int[24][24];

	/**
	 * Neutral.
	 */
	public VennMatrix()
	{
		for(int i = 1 ; i < 25 ; i++)
		{
			//for each line in matrix, i.e.
			//   	A  R  N  B  D  C  Q  Z  E  G  H  I  L  K  M  F  P  S  T  W  Y  V  X  *
			//   A  1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1
			for(int j=1;j<25;j++)
			{
				if(i==j)
					matrix[i-1][j-1]=1;
				else
					matrix[i-1][j-1]=-1;
			}
		}
	}
	/**
	 * Takes in an ncbi matrix ( ftp://ftp.ncbi.nlm.nih.gov/blast/matrices/MATCH )
	 * @param matrixText
	 */
	public VennMatrix (String matrixText)
	{
		matrixText=matrixText.trim();
		String[] allLines = matrixText.split("\n");
		System.out.println("Recieved matrix with " + allLines.length + " lines.");
		Vector<String> lines = new Vector<String>();
		for(String l : allLines)
		{
			//skip matrix comments and the header line.
			if(l.contains("#"))
				;
			else
				lines.add(l);
		}
		
		System.out.println("Done skipping comments.  header row is " + lines.firstElement());
		
		
		Iterator<String> rows = lines.iterator();
		//keep track of which row you are on 
		int i = 1;
		
		//skip header 
		rows.next();
		
		//for each row in the matrix.
		while(rows.hasNext())
		{
			String row = rows.next() ;
			//   For each line in matrix, i.e.
			//   A  R  N  B  D  C  Q  Z  E  G  H  I  L  K  M  F  P  S  T  W  Y  V  X  *
			//   A  1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1
			String[] pen = row.trim().split("\\s+");
			System.out.print(i + " : ");
			for(int j=1; j<25; j++)
			{
				matrix[i-1][j-1]=Integer.parseInt(pen[j]);
				System.out.print ( matrix[i-1][j-1]+" ");
			}
			System.out.println();
			//increment the row number that corresponds to the iterator
			i++;
		}
	}
	
	public String toString()
	{
		String k="";
		for(int i = 0 ; i < matrix.length; i++)
		{	for(int j = 0 ; j < matrix[i].length ; j++)
			{
				k+=(matrix[i][j] +"\t");
			}
			k+="\n";
		}
		return k;
	}
	
	public static void main(String[] args)
	{
		VennMatrix m1 = new VennMatrix(Utilities.FileUtils.fileToString(new File("database/match")));

		VennMatrix n = new VennMatrix();


		System.out.println("match");
		System.out.println(m1.toString());

		System.out.println("neutral");
		System.out.println(n.toString());
		
	}
}
