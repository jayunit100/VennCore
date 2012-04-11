/*
 * NeedlemanWunsch.java
 *
 * Created on 9 december 2006, 18:24
 */

package services;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

/**
 * Takes in a gap penalty. 
 * Use a gap of 0 for peptides.
 * @author DS
 */
public class NeedlemanWunsch 
{
    int d ;
   
    /**
     * takes in a matrix.  VennMatrix.blosum/VennMatrix.neutral ;
     * @param dd
     * @param matrix
     */
    public NeedlemanWunsch(int dd, int[][] matrix)
    {
    	d=dd;
    	simularity=matrix;
    }
    
    public static void main(String[] arg)
    {
    	NeedlemanWunsch n = new NeedlemanWunsch(-2,new VennMatrix().matrix);
    	
    	String[] vals=	n.align("QRDILFPDTPQVPLRKAAVDLSHFITQG","AYAPAQRDAAAAAAAILFPDTPQVPLRKAAVDLSHFITQGMGGKWSKSSVVGWPAIRERMRKTEP");
    	
    	System.out.println(vals[0]);
    	System.out.println(vals[1]);
    }

    /**
     * Returns the FIRST argument, after it has been aligned to the SECOND argument.
     * The SECOND argument preserves its residues.
     * @param a
     * @param b
     * @return
     */
    public   String[] align(String a, String b)
    {
    	a=a.trim().toUpperCase();
    	b=b.trim().toUpperCase();
    	
    	
    	if((a+b).contains(" ") || !StringUtils.isAlpha(a+b))
    	{
    		System.err.println("Recieved bad sequences, returning null.  \n-" + a + "-\n-" +b +"-\n might have a whitespace, or numeric characters."); 
    		return null;
    	}
    	int[][] ar =   calculateMatrix(convertStringToArr(a), convertStringToArr(b));
        return  getAlignments(ar, convertStringToArr(a), convertStringToArr(b), a,b);
    }
    
    /**
     * 
     * This debugs the array and prints the penalities out.
     * @param ar
     */
    public   void debug(int[][] ar)
    {
        for (int y = 0; y < ar.length; y++)
        {
            System.out.println("");
            for (int x = 0; x < ar[y].length; x++)
                System.out.print(ar[y][x] +"\t ");
        }

    }
    //used for debugging.
    int step=1;
    public   String[] getAlignments(int[][] ar, int[] A, int[] B, String sA, String sB)
    {
    	System.out.println("Aligning step " +step++ );
        String alA = "";
        String alB = "";        
        int i = sA.length();
        int j = sB.length();
        while (i > 0 && j > 0)
        {
            int score = ar[i][j];
            int scorediag = ar[i-1][j-1];
            int scoreup = ar[i][j-1];
            int scoreleft = ar[i-1][j];
            if (score == scorediag + simular(A[i-1], B[j-1]))
              {
                alA = sA.charAt(i-1) + alA;
                alB = sB.charAt(j-1) + alB;
                i--;j--;                
              }
            else if (score == scoreleft + d)
            {
                alA = sA.charAt(i-1) + alA;
                alB = "-" + alB;
                i--;
            }
            else if(score == scoreup + d)
            {
                alA = "-" + alA;
                alB = sB.charAt(j-1) + alB;
                j--;
            }
        }
        while(i > 0)
        {
            alA = sA.charAt(i - 1) + alA;
            alB = "-" + alB;
            i--;            
        }
        while(j > 0)
        {
            alA = "-" + alA;
            alB = sB.charAt(j - 1) + alB;
            j--;            
        }
       // System.out.println();
       // System.out.println("1="+alA+"\n");
      //  System.out.println("2="+alB+"\n");
        return new String[] {alA,alB};
    }
    
    /**
     * Create a punishment matrix.
     * @param source
     * @param dest
     * @return
     */
    public   int[][] calculateMatrix(int[] source, int[] dest)
    {
        int[][] res = new int[source.length+1][dest.length+1];
        for (int y = 0; y < source.length; y++)
            res[y][0] = d * y;
        
        
        for (int x = 0; x < dest.length; x++)
            res[0][x] = d * x;
        
        for (int y = 1; y < source.length + 1; y++)
            for (int x = 1; x < dest.length +1; x++)
                {                    
                    int k = res[y-1][x-1] + simular(source[y-1] , dest[x-1]);
                    int l = res[y-1][x] + d;
                    int m = res[y][x-1] + d;
                    k = Math.max(k,l);
                    res[y][x] = Math.max(k,m);
                }
        return res;
    }
    
    public static char[] a = VennMatrix.AMINO.toCharArray();
    
    public   int[] convertStringToArr(String str)
    {
    	str=str.toUpperCase();
    	ArrayList l = new ArrayList();
    	for (int i = 0; i < str.length(); i++)
        {
            int n = -1;
            for(int ci = 0 ; ci < a.length; ci++)
            	if(str.charAt(i) == a[ci])
            		n=ci;
            
            l.add(new Integer(n));
            if(n==-1)
            	System.out.println("neddleman wunsch : Failure on " + n + " in str : '" +str.charAt(i)+"'");
        }
        int[] arr = new int[l.size()];
        for (int i = 0; i < l.size();i++)
            arr[i] = ((Integer)l.get(i)).intValue();
        return arr;    

    }
    
    public int[][] simularity ;
    
    public int simular(int first, int second)
    {
    	//System.out.println(" " + simularity.length + " ");
    	//System.out.print(first + " " + second + " " );
    	//System.out.println(simularity[first][second]);
    	return simularity[first][second] ;
    }
    
    
}