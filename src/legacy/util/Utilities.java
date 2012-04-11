/* * Created on Sep 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package legacy.util;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.sql.Blob;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Node;


/**
 * @author jay
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Utilities 
{
	public static class Array
	{
		public static double[] getDoubleArray(List floats)
		{
			double[] r = new double[floats.size()];
			for(int i = 0 ; i < floats.size(); i++)
			{
				Double d = Double.parseDouble(floats.get(i).toString());
				r[i] = d;
			}
			return r;
		}
	}

	/**
	 * parses the year out of a string.  used with medline. 
	 * @param s
	 * @return
	 */
	public static int parseYear(String s)
	{
		Vector<String> ss=Utilities.Regex.getMatches("\\d\\d\\d\\d", s.split(" "));
		if(ss != null && ss.size()==1 && isNumber(ss.get(0)))
			return Integer.parseInt(ss.get(0));
		
		else
		{
			System.err.println("cant unambiguously parse a year from "+s + " .... match vector : " +Utilities.collapse(" ",ss));
			return -1;
		}
	}
	
	public static class CollectionUtil
	{
		public static <T> ArrayList<T> order(String field,Collection<T> elements)
		{
			assert elements != null;
			ArrayList<T> l = new ArrayList<T>();  
			BeanComparator comp= new BeanComparator(field);
			l.addAll(elements);
			Collections.sort(l, comp);
			return l;
		}
	}
	/**
	 * An alias to Utilities.StringUt.replaceParameters
	 * @param s
	 * @param ss
	 * @return
	 */
		public static String rp(String s, Object... ss)
		{
			return Utilities.StringUt.replaceParameters(s,ss);
		}
		
		public static String rpb(String s, Object bean)
		{
			System.err.println("bean reaplace : " + s + bean);
			String s1=s;
			try
			{
				Map o =org.apache.commons.beanutils.BeanUtils.describe(bean);
				for(Object property : o.keySet())
				{
					if(s.contains("<"+property+">"))
					{
						s1=s1.replaceAll("<"+property+">", PropertyUtils.getProperty(bean, property+"")+"");
						System.err.println("--bean replace : " + property + " .");
					}
					else
						System.err.println("--bean replace : " + property + " failed.");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return s1;
			
		}
		
	/**
	 * Hashtable counter... very usefule.
	 * 
	 * 	
	 * @author vyas
	 *
	 * @param <X>
	 */
	public static class Counter<X> extends Hashtable<X,Integer> {
		
		Class<X> me;
		public Counter(Class<X> c)
		{
			me=c;
		}
		
		/**
		 * adds a new element, and increments the count if the element already exists.
		 * @param c
		 */
		public void increment(X c){
			if(this.containsKey(c))
			{
				this.put(c,this.get(c)+1);
			}
			else
				this.put(c,1);
		}
		
	} 
	
	
	/**
	 * returns the pairwise distance between two  strings.
	 * great for bioinformatics applications.
	 * A good measure is pairwise distance of around .6
	 * @param a
	 * @param b
	 * @return
	 */
	public static double getPairwiseDistance(String a, String b)
	{
		return Comparison.compareStrings(a, b);
	}
	public static Hashtable<Object,Integer> getHashtableTalley(Collection data)
	{
		Hashtable<Object,Integer> counter = new Hashtable<Object,Integer>();
		
		for(Object s : data )
		{
				System.out.println("  >" + s);
				int c = counter.containsKey(s) ? counter.get(s).intValue() : new Integer(0) ;
				int newValue = c+1;
				System.out.println("putting " + newValue + " for " + s);
				counter.put(s, new Integer(newValue));
		}
		return counter;
	}

	public static <T> Vector<T> getSortedVector(Class<T> c, Collection<? extends Comparable> s)
	{
		Vector<T>  v = new Vector<T>();
		Vector<Comparable> i = new Vector<Comparable>(s);
		Collections.sort(i);
		
		for(Comparable cc : i)
		{
			v.add((T)cc);
		}
		return v;
	}
	
	public static class DateUtil
	{
		public static java.util.Date getDateForYear(int y)
		{
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern("MM/dd/yyyy");
			Calendar calendar = Calendar.getInstance();
			try{
				return sdf.parse("1/1/"+y);
			}catch(Exception e)
			{
				return null;
			}
		}
	}
	public static String getIP(){
		 try {
		        InetAddress addr = InetAddress.getLocalHost();
		    
		        // Get IP Address
		        byte[] ipAddr = addr.getAddress();
		    
		        // Get hostname
		        String hostname = addr.getHostName();
		        return new String(ipAddr) + " "+hostname;
		 } 
		 catch (UnknownHostException e) {
		    }
		 return null;
	}
	public static <T> Vector<T> createVector(T... s)
	{
		Vector<T> v = new Vector<T>();
		for(T t : s)
		{
			v.add(t);
		}
		return v;
	}
	public static <T> Vector<T> getCollection(Class<T> type,Object[] objects)
	{
		Vector<T> v = new Vector<T>();
		for(Object o : objects)
		{
			if(type.isAssignableFrom(o.getClass()))
			{
				v.add((T)o);
			}
		}
		return v;
	}
	public static <T> T getObjectFromCollectionByClass(Class<T> clz, Collection c)
	{
		for(Object o : c)
		{
			if(clz.isAssignableFrom(o.getClass()))
			{
				return (T) o;
			}
			else
			{
				lg.info(o.getClass() + " not assignable from " + clz);
			}
		}
		return null;
	}
	public static String buildQuery(String template, String name, Object value)
	{
		return template.replaceAll("<"+name+">", value+"");
	}
	public static String SPLIT_UNDERSCORE = "\\|";
	public static boolean isPrmiitive(Object t)
	{
		return t instanceof String || t instanceof Integer;
	}
	public static boolean containsNumbers(String s) {
		char[] chars = s.toCharArray();
		for (char c : chars) {
			if (Character.isDigit(c)) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<List> getSublists(int size, List l) {
		class CollectionSplitter {

			/**
			 * @param args
			 */
			public ArrayList<List> subDivide(List list, int subCollectionSize) {
				ArrayList<List> results = new ArrayList<List>();
				if (list.size() <= subCollectionSize) {
					results.add(list);
					return results;
				}
				for (int i = 0; i < getNumberOfIterations(list,
						subCollectionSize); i++) {
					int maxLength = ((i + 1) * subCollectionSize > list.size()) ? list
							.size()
							: (i + 1) * subCollectionSize;
					List sublist = new ArrayList();
					sublist.add(list.subList(i * subCollectionSize, maxLength));
					results.add(sublist);
				}
				return results;
			}

			private int getNumberOfIterations(List list, int subCollectionSize) {
				return list.size() % subCollectionSize == 0 ? list.size()
						/ subCollectionSize
						: (list.size() / subCollectionSize) + 1;
			}
		}
		return new CollectionSplitter().subDivide(l, size);
	}

	public static boolean isAllUpperCase(String s) {
		char[] chars = s.toCharArray();
		for (char c : chars) {
			if (Character.isLetter(c) && !Character.isUpperCase(c)) {
				return false;
			}
		}
		return true;
	}

	public static Object[] subArray(Object[] a, int x) {
		Vector v = new Vector();
		for (Object o : a) {
			v.add(o);
			if (v.size() == x)
				return v.toArray();
		}
		System.err.println("impossible to make array of size " + x + " from "
				+ a);
		return v.toArray();
	}

	/**
	 * 	 System.out.println(Utilities.Regex.match("
	 *   inputs : "[^BIJOUZ]{2}", "YX" yield a size of 1 (YX);
	 *   inputs : "[^BIJOUZ]{1}", "YX" yield a size of 2 (Y,X);
	 *   
	 * @author jvyas
	 *
	 */
	public static class Regex 
	{
		/**
		 * a surrogate to stringut.indicesOf
		 * @param input
		 * @return
		 */
		public static Vector<Integer> getMatchIndices(String input, Pattern pattern, int len)
		{
			return StringUt.indicesOf(input, pattern, len);
		}
		
		public static Vector<String> getMatches(String regex,String[] candidates){
			Vector<String> str = new Vector<String>();
			// Compile regular expression
			try 
			{
				Pattern pattern = Pattern.compile(regex);

				for(String s: candidates)
				{
					// Determine if pattern exists in input
					Matcher matcher = pattern.matcher(s);
					if(matcher.find())
						str.add(s);
				}
				return str;
			}
			catch (Exception e) 
			{
				return str;
			}

		}
		
		public static Vector<String> match(String regex, String regexContainerString) {
			Vector<String> str = new Vector<String>();

			// Compile regular expression
			try {
				Pattern pattern = Pattern.compile(regex);

				// Determine if pattern exists in input
				Matcher matcher = pattern.matcher(regexContainerString);
				while (matcher.find()) // true or false
				{
					str.add(matcher.group());
				}
				if (str.size() == 0)
					System.err.println("no matches " + regex + " in " + regexContainerString);
				return str;
			} catch (Exception e) {
				return str;
			}
		}
	}

	public static InputStream getInputStream(String s) {
		StringBuffer sb1 = new StringBuffer(s);
		try {
			return new ByteArrayInputStream(sb1.toString().getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void print(Object[] s) {
		System.out.print("<ARRAY>\n");
		for (int i = 0; i < s.length; i++) {
			System.out.print("index " + i + " = \'" + s[i] + "\'\n");
		}

		System.out.println("</ARRAY>");
	}

	public static void print(Collection l) {
		int c = 0;
		Iterator i = l.iterator();
		while (i.hasNext()) {
			System.out.println(c++ + "=" + i.next());
		}
	}

	public static class Logging 
	{
		public static void removeRootLogger(){
			org.apache.log4j.Logger.getRootLogger().removeAllAppenders();
		}
		
		
		public static Logger setupLog4J(Class c)
		{
			return Logger.getLogger(c);
		}
		

		public static Logger setupLog4J(Object c)
		{
			return Logger.getLogger(c+"");
		}
		
	}

	public static class Number {
		public static float floatConvert(byte[] arr, int start) {
			int startPlace = 0;
			int lengthOfJavaByte = 4;
			int i = 0;
			byte[] tmp = new byte[lengthOfJavaByte];
			for (startPlace = start; startPlace < (start + lengthOfJavaByte); startPlace++) {
				tmp[i] = arr[startPlace];
				i++;
			}
			int logacc = 0;
			startPlace = 0;
			for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
				logacc |= ((long) (tmp[startPlace] & 0xff)) << shiftBy;
				startPlace++;
			}
			return Float.intBitsToFloat(logacc);
		}
	}

	public static int countInstances(Object a, Collection c) {
		int ins = 0;
		Iterator i = c.iterator();
		while (i.hasNext()) {
			if (i.next() == a)
				ins++;
		}
		return ins;
	}

	public static class TimeLog {
		long start;

		public TimeLog() {
			start = System.currentTimeMillis();
		}

		public TimeLog(String task) {
			this();
			taskName = task;
			state = true;
		}

		String taskName;

		boolean state;

		public Double timeSoFarSeconds() {
			return new Double(System.currentTimeMillis() - start) / 1000D;
		}
		public Double timeSoFarMinutes() 
		{
			return new Double(timeSoFarSeconds()/60);
		}
		public Double stop() {
			state = false;
			return new Double(System.currentTimeMillis() - start) / 1000D;
		}

		public String stopAndReport() {
			return stop() + " seconds to " + taskName;
		}

		public String reportGetRate(float tasks)
		{
			String r = report();
			r+= " rate in tasks per second = " + (tasks / this.timeSoFarSeconds()) ;
			return r;
		}
		public String report() {
			double d = this.timeSoFarSeconds();

			if (d > 3600)
				return Math.round(d / (3600)) + " hours ";
			else if (d > 60)
				return Math.round(d / 60) + " minutes ";
			else
				return d + " seconds ";
		}
	}

	public static class Converter
	{
		public static Object convert(Class c, Object s)
		{
			if(c.equals(String.class))
			{
				return (s.toString());			
			}
			if(c.equals(Boolean.class) || c.equals(boolean.class))
			{
				Boolean.parseBoolean(s.toString());			
			}
			if(c.equals(Double.class) || c.equals(double.class))
			{
				return Double.parseDouble(s.toString());			
			}
			if(c.equals(Float.class) || c.equals(float.class))
			{
				return Float.parseFloat(s.toString());			
			}
			if(c.equals(Integer.class) || c.equals(int.class))
			{
				return Integer.parseInt(s.toString());
			}
			if(c.equals(File.class) )
			{
				return new File(s.toString());
			}
			System.err.println("no type converter for  " + c);
			return null;
		}
	}
	
	public static class Bean 
	{
		public static Vector<String> getPropertiesSorted(Object o)
		{
			Vector<String> s1=getProperties(o);
			Collections.sort(s1);
			return s1;
		}
		public static Vector<String> getProperties(Object o)
		{
			try
			{
			Object [] ss=(BeanUtils.describe(o).keySet().toArray());
		
			StringVector ssv = new StringVector();
			for(Object s1:ss)
				ssv.add(""+s1);
			
			ssv.remove("class");
			return ssv;
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
		public static void bind(Object bean, String property, Object value)
		{
			try
			{
				Class type = PropertyUtils.getPropertyType(bean, property);
				PropertyUtils.setProperty(bean,property, Converter.convert(type,value));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		public static void interactiveBind(String property, Object bean)
		{
			if(property.equals("class"))
				return;
			
			try
			{
				String s =null;
				Class type = PropertyUtils.getPropertyType(bean, property);
				if(type.equals(Boolean.class) || type.equals(boolean.class))
				{
					System.out.println("Enter value(\"true\" or \"false\") for "+property);
					s = Utilities.getInputFromUser();
				}
				else
				{
					System.out.println("Enter value for "+type+":" + property);
					s = Utilities.getInputFromUser();
				}
				bind(bean, property, s);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		public static void interactivePopulate(Object o)
		{
			try
			{
				Collection keys=BeanUtils.describe(o).keySet();
				for(Object kk : keys)
				{
					interactiveBind(kk.toString(), o);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		public static <T> String collapse(Collection<T> c,String prop)
		{
			Vector<T> ts = new Vector<T>(c);
			String s = "";
			for(T t : ts)
			{
				try{
					s+=PropertyUtils.getProperty(t, prop);
					if(t != ts.lastElement())
						s+=",";
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			return s;
		}

		public static <T> T create(Class<T> c,String prop,Object value)
		{
			try
			{
				T t = c.newInstance();
				BeanUtils.setProperty(t, prop, value);
				return t;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		
		public static String describe(Object o) 
		{
			try 
			{
				return BeanUtils.describe(o) + "";
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				return Bean.class + " coulnt describe " + o.getClass() + " " + e
						+ " see console for stack trace";
			}
		}
	}

	public static String removeTags(String x) {
		int s = -1, e = -1;

		s = x.indexOf("<");
		e = x.indexOf(">");

		while (x.contains("<") || x.contains(">")) {
			x = StringUtils.substringBefore(x, "<")
					+ StringUtils.substringAfter(x, ">");
		}
		return x;
	}

	public static class Generics {
		public static <T> Vector<T> cast(Class<T> t, Object[] items) {
			Vector<T> i = new Vector<T>();
			for (Object o : items) {
				try {
					i.add((T) o);
				} catch (Exception e) {
					lg.warn("CANNOT CAST all " + items + " to " + t);
				}
			}
			return i;
		}
	}

	public static <F> F getRandomIndex(Class<F> c, Collection<F> f) {
		if (f.size() == 0)
			return null;
		int index = Utilities.getRandomNumberOfMax(f.size());
		return (F) f.toArray()[index];
	}

	public static void debugEnumeration(Enumeration e) {
		while (e.hasMoreElements()) {
			System.out.print(e.nextElement() + ",");
		}
	}

	public static class StringUt 
	{
		  public static String sizeStringTo(String content, int size,char pad)
		  {
			  while(content.length() > size)
			  {
				  content=content.substring(0,content.length()-1);
			  }
			  while(content.length() < size)
			  {
				  content=content+pad;
			  }
			  return content;
		  }
		  
	      public static HashSet<String> getNonSimilarSet(Collection hh,final double P,final int max)
	      {
	    		HashSet<String> nHs = new HashSet<String>()
	    		{
	    			/**
	    			 * only adds if similarity is very high
	    			 */
	    			public boolean add(String s1)
	    			{
	    				for(String s : this)
	    				{
	    					if(Utilities.StringUt.similarity(s,s1) < P || size()>max) 
	    						return false;
	    				}
	    				super.add(s1);
	    				return true;
	    			}
	    		};
	    	float f = hh.size();
	    	lg.info(f + " " + P );
	    	int count =0;
	    	TimeLog tt = new TimeLog("add entries (10 at a time) ");
	    	for(Object h : hh)
	    	{
	    		if(nHs.size()%10==0)
	    			System.out.println("try to add" +h + " \n" + tt.report() + " \n"+nHs.size());
	    		count++;
	    		nHs.add(h.toString());
	    	}
	    	
	    	System.err.println("non similar set resulting percentile retained " + nHs.size()/f);	
	    	return nHs;
	      }
		  public static Vector<Integer> indicesOf(String input, Pattern pattern, int len)
		  {
	            Matcher matcher = pattern.matcher(input);

	            Vector<Integer> indices=new Vector<Integer>();
	            
	            while (matcher.find()) 
	            {
	                    //matcher.group();
	            	if(matcher.group().length()==len)
	            		indices.add(matcher.start());
	                    //matcher.end();
	            }
	            return indices;
		  }
		
		  public static boolean isCommonWordOrNumber(String s)
		  {
			  s=Utilities.removeNonAlphaNumerics(s, new char[]{});
			  return fCOMMON_WORDS.contains(s.trim().toLowerCase()) || Utilities.isNumber(s);
		  }
		  
		  public static final Set<String> fCOMMON_WORDS = new HashSet<String>();
		  static 
		  {
			  	fCOMMON_WORDS.add("through");
			  	fCOMMON_WORDS.add("using");
			  	fCOMMON_WORDS.add("using");
			  	fCOMMON_WORDS.add("been");
			  	fCOMMON_WORDS.add("but");
			  	fCOMMON_WORDS.add("into");
			  	fCOMMON_WORDS.add("can");
			    fCOMMON_WORDS.add("by");
			    fCOMMON_WORDS.add("a");
			    fCOMMON_WORDS.add("and");
			    fCOMMON_WORDS.add("be");
			    fCOMMON_WORDS.add("for");
			    fCOMMON_WORDS.add("from");
			    fCOMMON_WORDS.add("has");
			    fCOMMON_WORDS.add("i");
			    fCOMMON_WORDS.add("in");
			    fCOMMON_WORDS.add("is");
			    fCOMMON_WORDS.add("it");
			    fCOMMON_WORDS.add("of");
			    fCOMMON_WORDS.add("on");
			    fCOMMON_WORDS.add("to");
			    fCOMMON_WORDS.add("that");
			    fCOMMON_WORDS.add("with");
			    fCOMMON_WORDS.add("as");
			    fCOMMON_WORDS.add("an");
			    fCOMMON_WORDS.add("the");
			    fCOMMON_WORDS.add("this");
			    fCOMMON_WORDS.add("these");
			    fCOMMON_WORDS.add("were");
			    fCOMMON_WORDS.add("or");
			    fCOMMON_WORDS.add("which");
			    fCOMMON_WORDS.add("its");
			    fCOMMON_WORDS.add("also");
			    fCOMMON_WORDS.add("not");
			    fCOMMON_WORDS.add("are");
			    fCOMMON_WORDS.add("is");
			    fCOMMON_WORDS.add("was");
			    fCOMMON_WORDS.add("we");
			    fCOMMON_WORDS.add("both");
		  }

			  /**
			  * Use to determine if a particular word entered in the
			  * search box should be discarded from the search.
			  */
			  public static boolean hasCommonWord( String aSentence ) 
			  {
				 for(String s11 : aSentence.split(" "))
			     {
					 if(aSentence.contains(aSentence.toLowerCase()))
					 	return true;
			     }
				 return false;
			  }
 
		public static String replaceParameters(String target,Object... pars)
		{
			for(int i = 0 ; i < pars.length; i++)
			{
				if(pars[i] != null)
				{
					pars[i] = pars[i].toString().replaceAll("\\$","<dollar>");
					pars[i] = pars[i].toString().replaceAll("'","");
				}
				target=target.replaceAll("<"+i+">", pars[i]+"");
			}
			return target;
		}
		
		/**
		 * 
		 * for a group of objects, if any element in that group.toString = the string
		 * return true;
		 * @param group
		 * @param s
		 * @return
		 */
		public static boolean containsStringIgnoreCase(Object[] group, String s)
		{
			for(Object o : group)
			{
				if(o.toString().trim().equalsIgnoreCase(s))
					return true;
			}
			return false;
		}
		
		public static boolean containsAllStringsIgnoreCase(Object[] group, String[] elements)
		{
			for(String e : elements)
			{
				if(! containsStringIgnoreCase(group,e))
					return false;
			}
			return true;
		}

		public static float similarity(String a1, String a2)
		{
			//System.out.println(">>> "+a1+" vs "+a2);
			double d=Comparison.compareStrings(a1, a2);
			System.out.println("Comparison returned " +d);
			return new Float(d);
		}

		/**
		 * Send a comma sepearated string of values as the 
		 * second arg 
		 * 
		 * splitAt("hi_dud[e", "_,[")
		 * will return { hi_,dud[,e }
		 * 
		 * Does not work if youre string has a :; in it
		 * 
		 * @param s
		 * @param splits
		 * @return
		 */
		public static String[] spliceAt(Object s, String splits) {
			String st = s + "";
			Vector<Integer> ints = new Vector<Integer>();
			String[] splitters = splits.split(",");

			for (String sp : splitters) {
				//retains the split flag
				st = st.replaceAll(sp, sp + "!");

			}
			System.out.println(st + " = " + st.split("!").length);

			return st.split("!");
		}

	}

	public static class Maths 
	{
		
		 public static double stddev(Double[] d)
		 {
			 SummaryStatistics s = new SummaryStatistics();
			 for(int i  = 0 ; i < d.length; i++)
			 s.addValue(d[i].doubleValue());
			 return s.getStandardDeviation();
		 }
		 
		 public static double getPearsonCorrelation(List<? extends java.lang.Number> s1 , List<? extends java.lang.Number> s2)
		 {
			 assert s1.size()==s2.size();
		        double[] scores1 = new double[s1.size()];
		        double[] scores2 = new double[s2.size()];
				 
		        for(int i = 0 ; i < s1.size() ; i++)
		        {
		        	scores1[i] = (double) s1.get(i).doubleValue();
		        	scores2[i] = (double) s2.get(i).doubleValue();
		        }
		        
			 	double result = 0;
		        double sum_sq_x = 0;
		        double sum_sq_y = 0;
		        double sum_coproduct = 0;
		        double mean_x = scores1[0];
		        double mean_y = scores2[0];
		 
		        for(int i=2;i<scores1.length+1;i+=1){
		            double sweep =Double.valueOf(i-1)/i;
		            double delta_x = scores1[i-1]-mean_x;
		            double delta_y = scores2[i-1]-mean_y;
		            sum_sq_x += delta_x * delta_x * sweep;
		            sum_sq_y += delta_y * delta_y * sweep;
		            sum_coproduct += delta_x * delta_y * sweep;
		            mean_x += delta_x / i;
		            mean_y += delta_y / i;
		        }
		        double pop_sd_x = (double) Math.sqrt(sum_sq_x/scores1.length);
		        double pop_sd_y = (double) Math.sqrt(sum_sq_y/scores1.length);
		        double cov_x_y = sum_coproduct / scores1.length;
		        result = cov_x_y / (pop_sd_x*pop_sd_y);
		        return result;
		    }
		
		 public static Float angleR(java.awt.Point a, java.awt.Point b) 
		 {
			 double s=StrictMath.asin((b.x-a.x)/Point2D.distance(a.x,a.y,b.x,b.y));
			 //return new Float(StrictMath.toDegrees(s));
			 return new Float(s);
		 }
		 
		 public static Float angleR(double ax,double ay, double bx, double by) 
		 {
			 double s=StrictMath.asin((bx-ax)/Point2D.distance(ax,ay,bx,by));
			 //return new Float(StrictMath.toDegrees(s));
			 return new Float(s);
		 }
		public static Float roundTo2(Float f )
		{
			return (float) (Math.round(f*100.0f)/100.0f);
		}
		
		public Float getFloatFromDouble(Double d)
		{
			return new Float(d);
		}
		
		/**
		 * Utilities.Maths.squareFunction(0,1000,.5f,similarity)
		 * Scales blast scores to 1000 if they are > 50% pairwise homologous.
		 * Otherwise scales them to 0.
		 * @param <T>
		 * @param max
		 * @param min
		 * @param cutoff
		 * @param value
		 * @return
		 */
		public static <T extends Comparable> T squareFunction(T min, T max, double cutoff, double value)
		{
			assert min.compareTo(max)<=0;
			if(value >= cutoff)
				return max;
			else 
				return min;
		}
		public static Hashtable<Float,Float> normalize(Collection<Float> f)
		{
			float min =Float.MAX_VALUE;
			//calc min;
			for(Float ff : f)
			{
				min=Math.min(ff, min);
			}
			
			//make min = 0;
			float max =Float.MIN_VALUE;

			Hashtable<Float,Float> fNormalized=new Hashtable<Float,Float>();
			for(Float ff : f)
			{
				//make ff in f->ff a non negative number
				float normalizedValue=ff+Math.abs(min);
				fNormalized.put(ff,normalizedValue);
				//store the max value so far
				max=Math.max(max, normalizedValue);
			}
			
			//for each ff in f->ff, divide ff by the max value
			for(Float ff : fNormalized.keySet())
			{
				fNormalized.put(ff, fNormalized.get(ff)/max);
			}	
			return fNormalized;
		}
		
		public static boolean inRange(int x, int s, int e)
		{
			if(e < s)
				Utilities.breakException("END  < START  ... " + e +" < " + s);
			return (x >= s && x <= e);
		}
		
		public static Double average(List<Double> n)
		{
			Iterator<Double> i = n.iterator();
			int sum = 0;
			while (i.hasNext()) {
				double x = i.next();
				sum += x;
			}
			if (n.size() == 0) {
				System.err.println("Divide by 0 ");
				Utilities.debugArray(n.toArray());
				return null;
			}
			return new Double(sum / (double)n.size());
		
		}
		public static Double median(List<Double> n)
		{
			ArrayList<Double> dd = new ArrayList<Double>(n);
			Collections.sort(dd);
			return dd.get(dd.size()/2);
		}
		public static Double median(Double[] d)
		{
			return median(Arrays.asList(d));
		}
		
		public static int average(Vector<Integer> n) {
			Iterator<Integer> i = n.iterator();
			int sum = 0;
			while (i.hasNext()) {
				int x = i.next();
				sum += x;
			}
			if (n.size() == 0) {
				System.err.println("Divide by 0 ");
				Utilities.debugArray(n.toArray());
				return -1;
			}
			return sum / n.size();
		}
		public static int median(Vector<Integer> n) 
		{
			Vector<Integer> ns = (Vector<Integer>) n.clone();
			Collections.sort(ns);
			return ns.get(n.size()/2);
		}

		public static int signConform(int n, int sign) {
			int i = -1;
			if (sign >= 0) {
				return Math.abs(n);
			}

			else
				return -Math.abs(n);
		}
	}

	public static Object findClosestMatch(Object[] entries, String s) {
		if (entries == null || entries.length == 0)
			return null;

		Object candidate = entries[0];
		int distance = StringUtils.getLevenshteinDistance(candidate + "", s);
		for (Object o : entries) {

			int distance2 = StringUtils.getLevenshteinDistance(o + "", s);
			if (distance2 < distance) {
				candidate = o;
				distance = distance2;
			}
		}
		return candidate;
	}

	public static String read(BufferedReader r) {
		try {
			String content = "", s;
			while ((s = r.readLine()) != null) {
				content += s;
			}
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			return "Utilities.read(Bufrerred Reader r) execption " + e + "0???";
		}
	}

	public static class CBeanUtils {
		public static Map toMap(Object o) {
			try {
				return BeanUtils.describe(o);
			} catch (Exception e) {
				lg.warn("cant deescribe "+o);
				e.printStackTrace();
				Hashtable h = new Hashtable();
				h.put("class", o.getClass());
				h.put("error", e);
				return h;
			}
		}
		public static <T> T fromMap(T o, Map properties)
		{
			for(Object s : properties.keySet())
			{
				try{
					PropertyUtils.setProperty(o, s.toString(), properties.get(s));
				}catch(Exception e)
				{
					e.printStackTrace();
					System.err.println(Utilities.class+"Couldnt set " + s + " on " + o + " "+e);
				}
			}
			return o;
		}
	}

	public static class NetworkUtils {

		public static String read(URL l) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(l
						.openStream()));

				String inputLine;
				String content = "";
				while ((inputLine = in.readLine()) != null) {
					content += inputLine + "\n";
				}

				in.close();
				return content;
			} catch (Exception e) {
				e.printStackTrace();
				return "ERROR READING " + l;
			}
		}

		public static File read(File f, URL l) {

			try {
				FileWriter fw = new FileWriter(f);
				BufferedReader in = new BufferedReader(new InputStreamReader(l
						.openStream()));

				String inputLine;
				int lines = 0;
				while ((inputLine = in.readLine()) != null) {
					fw.write(inputLine + "\n");
					lines++;
					if (lines > 500 && lines % 100 == 0) {
						System.err.println(" " + lines + " : " + inputLine);
					}
				}
				fw.flush();
				in.close();
				return f;
			} catch (Exception e) {
				e.printStackTrace();
				return f;
			}
		}
	}

	static Logger lg = Logger.getLogger(Utilities.class);

	public static class FileUtils 
	{
		

		/**
		 * Copy a file (in) to a new File (out). simple, easy.
		 * @param in
		 * @param out
		 * @throws Exception
		 */
		public static void copyFile(File in, File out) throws Exception {
		    FileInputStream fis  = new FileInputStream(in);
		    FileOutputStream fos = new FileOutputStream(out);
		    try {
		        byte[] buf = new byte[1024];
		        int i = 0;
		        while ((i = fis.read(buf)) != -1) {
		            fos.write(buf, 0, i);
		        }
		    } 
		    catch (Exception e) {
		        throw e;
		    }
		    finally {
		        if (fis != null) fis.close();
		        if (fos != null) fos.close();
		    }
		  }
		
		
		public static class FileAppender 
		{
			 BufferedWriter bw = null;
			public FileAppender(File f)
			{
				try{
		         bw = new BufferedWriter(new FileWriter(f, true));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			public void write(String s)
			{
			      try 
			      {
					 bw.write(s);
					 bw.newLine();
				  } 
			      catch (IOException ioe) 
			      {
			    	  ioe.printStackTrace();
			      } 
			 } // end try/catch/finally
			      
			 public void close()
			 {
				 try
				 {
					 bw.flush();
					 bw.close();
				 }catch(Exception e)
				 {
					 
				 }
			 }

		}
		
		public static FileAppender getFileWriter(File f){
			return new FileAppender(f);
		}
		
		/**
		 * Very userful.  Just input a url,... stringifies and returns it.
		 * @param u
		 * @return
		 */
		public static String read(URL u)
		{
			 System.out.println("Utilities: going to u."+u);
			try{
			BufferedReader in = new BufferedReader(
						new InputStreamReader(
						u.openStream()));

			String inputLine;
			String o="";
			while ((inputLine = in.readLine()) != null)
			   o+=inputLine+"\n";

			in.close();
			
			return o;
			}
			catch(Exception ee)
			{
				System.out.println("File not found : " + u.getFile() );
				return null;
			}
		}
	    public static Vector<File> traverse(File fileObject, int limit)
	    {
	        FolderTraversar fo=new FolderTraversar();
	    	fo.originalFileObject = fileObject;
	        fo.fileObject = fileObject;
	        fo.recursiveTraversal(fileObject,limit);
	        return fo.iterator;
	    }

		public static class FolderTraversar 
		{
		    private String indent = "";
		    private File originalFileObject;
		    private File fileObject;
		    private Vector<File> iterator=new Vector<File>();

		    public void recursiveTraversal(File fileObject, int limit)
		    {		
		        if (fileObject.isDirectory())
		        {
		        	System.out.println("dir:"+fileObject.getAbsolutePath());
		       
		        	indent = getIndent(fileObject);
	            	
		            File allFiles[] = fileObject.listFiles();
		            if(allFiles != null)
		            	for(File aFile : allFiles)
		            	{
		            		if(allFiles.length>=limit)
		            			return;
		            		recursiveTraversal(aFile, limit);
		            	}
		        }
		        else if (fileObject.isFile())
		        {
		        	System.out.println("file:"+fileObject.getAbsolutePath());
		        	iterator.add(fileObject);
		        }		
		    }

		    private String getIndent(File fileObject)
		    {
		        String original = originalFileObject.getAbsolutePath();
		        String fileStr = fileObject.getAbsolutePath();		
		        String subString = 
		            fileStr.substring(original.length(), fileStr.length());

		        String indent = ""; 
		        for(int index=0; index<subString.length(); index ++){
		            char aChar = subString.charAt(index);
		            if (aChar == File.separatorChar){
		                indent = indent + "  ";
		            }
		        }
		    return indent;
		    }
		}

		
		public static void BatchCopy(File f,int start, int end)
		{
			try{
			        BufferedReader rin=null;
			        File fout = new File(f.getName()+System.currentTimeMillis()+".out");

			        rin= new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			        fout.createNewFile();
			        PrintWriter pout = null;
			        pout= new PrintWriter(new OutputStreamWriter(new FileOutputStream(fout)));
			        int c=0;
			        while(c < start)
			        {
			        	String r = rin.readLine();
			        	float fc = c;
			        	float fs = start;
			        	c++;
			        	if((c%100000)==0)
			        	System.out.println(c+" "+fc/fs + " " + r);
			        }
			    	System.out.println(c + " "  );
			        c++;
			        System.out.println("type key to continue. ");
			        Utilities.getInputFromUser();
			        while (c >= start && c <= end)
			        {
			            	String sline = rin.readLine();
				            if(sline != null)
				            {
				                System.out.println(c + "copying line: "+sline);
				                pout.println(sline);
				            }
				            else
				            {
				            	System.out.println("line is null... ");
				            }
				            c++;
			        }
			        pout.close();
			        rin.close();
			}
			catch(Exception e){e.printStackTrace();}
		}
		public static FileIterator getFileIterator(File f){return new FileIterator(f);}
		public static class FileIterator implements Iterator<String>
		{
			String str;
			int linesRead=0;
			public boolean hasNext() 
			{
				try
				{
					boolean ready= in.ready();
					if(!ready)
					{
						in.close();
					}
					return ready;
				}
				catch(Exception e)
				{
					if(linesRead==0)
						e.printStackTrace();
					return false;
				}
			}
	
			public String next() 
			{
				linesRead++;
				try{
		        if ((str = in.readLine()) != null) 
		        {
		        	if(! hasNext())
						  in.close();
		        	return str;   
		        }
		        else 
		        	return null;
				}
				catch(Exception e)
				{
					e.printStackTrace();
					return null;
				}
			}
	
			public void remove() 
			{
				
			}
			BufferedReader in;
			public FileIterator(File f)
			{
				try 
				{
					in = new BufferedReader(new FileReader(f));
			    } 
				catch (IOException e) 
				{
					e.printStackTrace();
			    }
			}
		}
		/**
		 * Warning : Buggy for large URLS like the 
		 * pdb clusters ftp file.
		 * @param u
		 * @return
		 */
		public static String urlToString(String u) {
			StringBuffer webContents = new StringBuffer("");
			try 
			{
				URL url = new URL(u);
				URLConnection conn = url.openConnection();
				DataInputStream in = new DataInputStream(conn.getInputStream());
				BufferedReader bf = new BufferedReader(new InputStreamReader(in));
				int c = 0;
				while (bf.ready()) 
				{
					String line = bf.readLine();

					if(c++%100 == 0)
					{
						lg.info("reading"+c++ + " " + line);
					}
					
					webContents.append(line+"\n" );
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			return webContents.toString();
		}

		/**
		 * returns a file whose first hs bytes are stripped off. 
		 * @param hs
		 * @param f
		 * @return
		 */
		public static File strip(int hs, File f) {
			try {
				byte[] b = Utilities.FileUtils.getBytesFromFile(f);
				if (b.length > hs) {
					byte[] bs = new byte[b.length - hs];
					for (int i = hs; i < b.length - hs; i++) {

						bs[i - hs] = b[i];
					}
					return Utilities.FileUtils.writeToFile(new File(f
							.getAbsolutePath()
							+ "stripped"), bs);
				}

				else {
					System.err.println("cant strip " + hs + " bytes from a "
							+ b.length + " file");
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Failed returning null");
				return null;
			}
		}

		public static File copy(String u) {
			try {
				File copied = new File("temp_copy");
				URL url = new URL(u);
				//System.out.println("Opening connection to " +u  + "...");
				URLConnection urlC = url.openConnection();
				// Copy resource to local file, use remote file
				// if no local file name specified
				InputStream is = url.openStream();
				// Print info about resource
				//System.out.print("Copying resource (type: " +
				//urlC.getContentType());
				Date date = new Date(urlC.getLastModified());
				//System.out.println(", modified on: " +
				//date.toLocaleString() + ")...");
				System.out.flush();
				FileOutputStream fos = null;
				fos = new FileOutputStream(copied);
				int oneChar, count = 0;
				while ((oneChar = is.read()) != -1) {
					fos.write(oneChar);
					count++;
				}
				is.close();
				fos.close();
				//System.out.println(count + " byte(s) copied");
				return copied;
			} catch (Exception e) {
				System.err.println("Cant get to the internet ? check TCP... ");
				System.err.println(e.toString());
				return null;
			}
		}

		public static File getFileThatIsLocalTo(Class c, String path)
		{
			if(path.length()<1)
				{
					lg.warn("path is null or to small "  + path);
				return null;
				}
			
			File f;
			System.out.println("new-utils-getting file "+getPathToFileThatIsLocalt(c,path));
			String s = (getPathToFileThatIsLocalt(c,path));
			
			if(s == null )
			{
				lg.warn("File not found.  Are you running from a jar ? If so, class resources may not be found sometimes, checking in the root directory");
				f= new File(path);
			}
			
			else
				f=new File(s);
			
			if(! f.exists())
					lg.warn("File not found in root directory. " + path+" must be in the root or stored with class " + c.getName()+"... this is a problem with jars, sometimes. this bug may be fatal to your application.");
			return f;
			
		
		}
		
		public static byte[] getBytesFromFile(Class c, String s) {
			try {
				return FileUtils.getBytesFromFile(new File(Utilities.FileUtils
						.getPathToFileThatIsLocalt(c, s)));
			} catch (Exception e) {
				lg.warn("couldnt find " + s + " in folder with " + c);
				return null;
			}
		}
   
		public static String getSuffix(File f) {
			return StringUtils.substringAfter(f.getAbsolutePath(), ".");
		}

		public static int countLines(File f) {
			return fileToString(f).split("\n").length;
		}

		public static class PropertysFileUtils {
			/** 
			 * Automatically updates a file when you update a property. 
			 * The file must actually exist first though .
			 * @author Jay
			 *
			 */
			public static class PropertiesWithFile extends Properties {
				File props;
				public void reload(){
					try{
						load(new FileInputStream(ff));
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				/**
				 * tries to parse a point formatted as <x,y> 
				 * i.e.
				 * 
				 * xLocation=10,20 from the file key is "xLocation"
				 * @param key
				 * @return
				 */
				public Point getPoint(String key) {
					String removeTags = getProperty(key).replaceAll("<", "")
							.replaceAll(">", "");
					String[] prop = removeTags.split(",");
					Point p = new Point();
					p.x = Integer.parseInt(prop[0]);
					p.y = Integer.parseInt(prop[1]);
					return p;
				}
				File ff;
				public PropertiesWithFile(File f) {
					super();
					ff=f;
					try {
						load(new FileInputStream(f));
					} catch (Exception e) {
						e.printStackTrace();
					}
					props = f;
				}

				public File getFile() {
					return props;
				}

				public void store() {
					try {
						super.store(new FileOutputStream(this.props),
								"(jay is good at this stuff)");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			public PropertiesWithFile getPropertiesFrom(File f) {
				PropertiesWithFile p = new PropertiesWithFile(f);
				return p;
			}
		}

		public static void append(File f, String s) {

			BufferedWriter bw = null;

			try {
				System.out.println("appending : "+f.getAbsolutePath());
				if(! f.exists())
				{
					System.err.println("File does not exist !");
				}
				if(! f.canWrite())
				{
					System.err.println("File cannot be written ");
				}
				bw = new BufferedWriter(new FileWriter(f, true));
				bw.write(s);
				bw.newLine();
				bw.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally { // always close the file
				if (bw != null)
					try {
						bw.close();
					} catch (IOException ioe2) {
						// just ignore it
					}
			} // end try/catch/finally

		}

		static boolean reading = false;

		public static String fileToString(Class c, String localPath)
		{
			StringBuffer p=new StringBuffer();
			//find the file as a stream and write it to the working directory.
			//then return that.
			assert new File(localPath) .exists();
			InputStream instream = c.getResourceAsStream(localPath);
			InputStreamReader infile = new InputStreamReader(instream);
			BufferedReader inbuf = new BufferedReader(infile);
			try
			{

				 FileOutputStream fos = new FileOutputStream(new File(localPath));

				while(inbuf.ready())
				{
					p.append(inbuf.readLine()+"\n");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return p.toString();
		}
		
		public synchronized static String fileToString(File f) {
			while (reading)
				;
			reading = true;
			try {
				StringBuffer b = new StringBuffer();
				InputStreamReader fis = new InputStreamReader(
						new FileInputStream(f));
				char ch = 0;
				reading = false;
				while (fis.ready() && (ch = (char) fis.read()) > -1) {
					b.append(ch);
				}
				return b.toString();
			} catch (Exception e) {
				System.err.println("Utilities : Error reading file "
						+ f.getAbsolutePath() + " ");
				e.printStackTrace();
				return "Error " + e;
			}

		}

		public static File writeToWorkingDirectory(String dir, String n,
				String c) {
			try {
				File f = new File(FileUtils.getWorkingDirectory() + "/" + dir,
						n);
				FileWriter outFile = new FileWriter(f);
				PrintWriter out = new PrintWriter(outFile);

				// Also could be written as follows on one line
				// Printwriter out = new PrintWriter(new FileWriter(args[0]));
				// Write text to file
				out.print(c);
				out.close();
				return f;
			}

			catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		/**
		 * Returns the directory which spawned this java application instance.
		 * @return
		 */
		public static String getWorkingDirectory() {
			return System.getProperty("user.dir");
		}

		static public File writeToTempFile(String f, byte[] b, long lifespan) {
			System.out.println("REQUESTED WRITE TO TEMP FILE:" + f + ","
					+ b.length + " time :" + lifespan);
			try {
				File file = File.createTempFile(f, "temp");
				writeToFile(file, b);
				return file;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static byte[] getBytesFromFile(File f) throws Exception {
			InputStream is = new FileInputStream(f);

			// Get the size of the file
			long length = f.length();
			System.err.println("Getting bytes for file of size " + f.length());
			// You cannot create an array using a long type.
			// It needs to be an int type.
			// Before converting to an int type, check
			// to ensure that file is not larger than Integer.MAX_VALUE.
			if (length > Integer.MAX_VALUE) {
				// File is too large
			}

			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file "
						+ f.getName());
			}

			// Close the input stream and return bytes
			is.close();
			return bytes;
		}

		public static File writeToFile(String fname, byte[] content) {
			File f = new File(fname);
			return writeToFile(f, content);
		}

		public static File writeToFile(File f, byte[] content) {
			try {

				if (!f.exists())
					f.createNewFile();

				FileOutputStream f2 = new FileOutputStream(f);
				Logln("bytes output : " + content.length);

				//for(int j = 0 ; j < content.length; j++)
				//	Log (content[j]+",");
				f2.write(content);

				f2.close();
				return f;
			} catch (Exception e) {
				lg.warn("Couldnt create file " + f.getAbsolutePath());
				e.printStackTrace();
				return null;
			}
		}

		public static File writeToFile(String dir, String fname, byte[] content) {

			try {

				File f = new File(dir, fname);
				return writeToFile(f, content);

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

		public static void writeObjectToFile(String name, Serializable o)
				throws Exception {
			if (o == null)
				throw new NullPointerException("Null object : " + o);

			System.out.println("write: serializing  " + o);

			FileOutputStream fout = new FileOutputStream(name);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(o);
			oos.close();
			//check
			String x1 = readObjectFromFile(name) + "";
			if (!x1.toString().equals(o.toString()))
				throw new Exception(x1 + " != " + o + "... "
						+ "write not accurate");

			System.out.println("write: Done ");

		}

		public static Object readObjectFromFile(Blob file) 
		{
		        try
		        {
		        	ObjectInputStream in = new ObjectInputStream(file.getBinaryStream());
		        	// Deserialize the object
		        	Object o =  in.readObject();
		        	in.close();
		        	return o;
		        }
		        catch(Exception e)
		        {
		        	e.printStackTrace();
		        	return null;
		        }
		}
 

		public static Object readObjectFromFile(String file) throws Exception {
			// unserialize the Queue
			System.out.println("read:   unserializing  " + file);
			FileInputStream fin = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fin);
			Object o = ois.readObject();
			ois.close();
			System.out.println("read:   Done - object type =" + o.getClass());
			System.out.println("read:   Done - object value =" + o);
			return o;
		}

		public static File createFile(String n) {
			File f = new File(n);
			try 
			{
				if(! f.exists())
				f.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return f;
		}

		public static boolean fileExists(String s) {
			File f = new File(s);
			return f.exists();
		}

		public static String getPathToFileThatIsLocalt(Class c, String localPath) {
			try 
			{
				if (new File(c.getResource(localPath).getFile()).exists())
				{
					lg.info("file found " + localPath + " in " + c.getName() + " returning path "+c.getResource(localPath).getPath());
					return c.getResource(localPath).getPath();
				}
				else
				{
					lg.warn("fatal WARNING : " + localPath + " not found for class " + c.getName() +" returning null ");
					return null;
				}
					
			} 
			catch (Exception e) 
			{
				for (int i = 0; i < 10; i++)
					System.err.println(localPath
							+ " does not exist in the same folder as " + c);
				e.printStackTrace();
				for (int i = 0; i < 10; i++)
					System.err.println(localPath
							+ " does not exist in the same folder as " + c);
				return null;
			}

		}

	}

	/**
	 * Generates a "key" for a class. 
	 * Never rely on this method for 
	 * production quality applications. 
	 * @param c
	 * @return
	 */
	public static String generateKey(Class c) {
		return c.getSimpleName() + "_"
				+ Utilities.getSystemProperty("user.name") + "_"
				+ Utilities.getTimestampAfterAMPM();
	}


		
	
	public static String generateKey() {
		return Utilities.getSystemProperty("user.name") + "_"
				+ Utilities.getTimestampAfterAMPM();
	}

	public static class HTML 
	{
		
		public static class HeatColor extends OrderedHashtable<Float,String>
		{
			public Hashtable<Float,Float> valueNormalizations=new Hashtable<Float,Float>();
			
			public void insert(Float t,Float normalizedValue)
			{
				assert normalizedValue <= 1;
				assert normalizedValue >= 0;
				put(t,map.get(Math.round(normalizedValue*253)));
				//also remember what the normalized value of f was so this can be used later in the api if necessary (i.e. for a legend or key) .
				valueNormalizations.put(t,normalizedValue);
			}
			
			private Vector<String> map = new Vector<String>();

			public HeatColor(int size)
			{
				for(int i =0 ; i < 255; i += 1)
				{
					map.add("[255,"+ i +","+ i +"]");	 	  
				}
				
				Collections.reverse(map);
			}
			
			public String getHTML(Float f)
			{
				return get(f).replaceAll("\\[", "rgb(").replaceAll("\\]",")");			
			}
		}
		/**
		 *  get a color that maps to a heat map from red (low) to blue (high);
		 *  floats must range from 0 to 1.
		 * @param a
		 * @return
		 */
		public static HeatColor getHeatColor(Collection<Float> f)
		{
			Hashtable<Float,Float> fNormalized = Maths.normalize(f);

			System.out.println(Utilities.debugHashtable(fNormalized));

			//add heat colors in sorted order according to the key.
			Vector<Float> fNormalizedSorted = new Vector<Float>(fNormalized.keySet());
			Collections.sort(fNormalizedSorted);

			HashSet<Float> fTotal = new HashSet<Float>(fNormalizedSorted);
			HeatColor t=new HeatColor (fTotal.size());
			System.out.println("setup hashtable with "+fTotal.size()+" values.");
			
			for(Float ff : fNormalizedSorted)
			{
				System.out.println(ff+"->"+fNormalized.get(ff));
				t.insert(ff,fNormalized.get(ff));
			}
			
			return t;
			
		}
		
		public static String highlight(String text, String highlight, String color)
		{
			String pre="<span style=\"background-color: "+color+"\"><b>";
			text=text.replaceAll(highlight, pre+highlight+"</b></span>");
			return text;
		}
		
		public static String PINK = "#CC9999", YELLOW = "#CCFF66",
				LIGHT_GREEN = "#99FF99", ORANGE = "#FF9933", CYAN = "#66FFFF",
				GRAY = "#CCCCFF";

		public static String getHtmlByTransformation(File xml, File xsl) {
			try {

				TransformerFactory tFactory = TransformerFactory.newInstance();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				Transformer transformer = tFactory
						.newTransformer(new javax.xml.transform.stream.StreamSource(
								xsl));

				transformer.transform(
						new javax.xml.transform.stream.StreamSource(xml),
						new javax.xml.transform.stream.StreamResult(os));

				//new FileOutputStream("sdf.html").write(os.toByteArray());
				return new String(os.toByteArray());
				//"pulse/view/howto.xml"
			} catch (Exception e) {
				e.printStackTrace();
				return "Transformation Failed : " + e;
			}

		}

		/**
		 * remove all tags from a string.
		 * @param s
		 * @return
		 */
		public static String removeAllTags(String s) {
			String result = s;
			while (true) {
				int left = result.indexOf("<");
				if (left < 0) {
					return result;
				}
				int right = result.indexOf(">", left);
				if (right < 0) {
					return result;
				}
				result = result.substring(0, left)
						+ result.substring(right + 1);
			}
		}

		
		
		public static <T> String getTable(Collection<T> t,String... columns) throws Exception
		{
			Tag table = new Tag("table","class=sortable border=1 cellpadding=0");

			Tag header=new Tag("TR");

			if(columns.length>0)
			{

			}
			else 
			{
				Map m =BeanUtils.describe(t.iterator().next());
				m.remove("class");
				Set props = m.keySet()  ;
				columns=new String[props.size()];
				for(int i=0;i<columns.length;i++)
					columns[i]= props.toArray()[i]+"" ;
			}
			
			for(String s : columns)
			{
				//special header tag 
				Tag td = new Tag("TH");
				td.add(s+"<>");
				header.add(td);
			}
			table.add(header);
			
			
			for(T t1 : t)
			{
				Tag tr = new Tag("TR");
				for(String prop : columns)
				{
					Tag td = new Tag("TD");
					td.add(PropertyUtils.getProperty(t1, prop));
					tr.add(td);
				}
				table.add(tr);
			}
			return table.toString();
		}
		/**
		 * Create a html table from a map
		 * @param m
		 * @return
		 */
		public static Tag mapToHtml(Map m) {
			// creat a table
			Tag table = new Tag("table", "border=0 cellpadding=0 cellspacing=0");
			// create two rows with five columns each
			Iterator keys = m.keySet().iterator();
			while (keys.hasNext()) {
				Tag row = new Tag("tr");

				Object key = keys.next();
				//create new cell tag
				Tag cell = new Tag("td");
				// fill in content
				cell.add(key);

				Tag cell2 = new Tag("td");
				Tag bold = new Tag("i");
				bold.add(m.get(key));
				cell2.add(bold);
				// add cell (same object) to row1 and row2
				row.add(cell);
				row.add(cell2);
				table.add(row);
			}

			// now replace cell 4 in row 2
			// first, create new cell with its content
			Tag cell = new Tag("td");
			return table;
		}

	}

	public static interface Filter<T> {
		public abstract boolean pass(T t);
	}

	/**
	 * two props will be equivalent if their toString is case insensitive equal. 
	 * @author vyas
	 *
	 * @param <T>
	 */
	public static class PropertyEquivalenceFilter<T> implements Filter {
		String name;

		String value;

		boolean d;

		public void setDebug(boolean t) {
			d = t;
		}

		public PropertyEquivalenceFilter(T bean, String p) throws Exception {
			name = p;
			Map m = null;
			try {
				m = BeanUtils.describe(bean);
				if (m.containsKey(p)) {
					value = BeanUtils.getProperty(bean, name);
				}
			} catch (Exception e) {
				throw new Exception("Couldnt describe " + bean + e);
			}

			if (value == null)
				throw new Exception("Property " + p + " not found ... in " + m
						+ " of class " + bean.getClass());

		}

		public boolean pass(Object t) {
			try 
			{
				String x = BeanUtils.getProperty(t, name);
				//lg.info(d, x + " vs " + value);
				if (x!=null && x.equalsIgnoreCase(value)) 
				{
					//lg.info(d, "equal");
					return true;
				} else {
					//lg.info(d, "NOT equal");
					return false;
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				System.err.println(e + " thrown when reflecting for " + t + "."+ name);
				return false;
			}

		}

	}

	public static <T> Vector<T> getVector(T[] t) {
		Vector<T> v = new Vector<T>();
		for (T ob : t) {
			v.add(ob);
		}
		return v;
	}

	/**
	 * Takes a value as a string and sets it as the property value for a 
	 * bean of type t.  Then uses that as a prototype against the filterbyobjectcomparison
	 * method. Appears to do type equivalence by toString so dont expect it to work 
	 * on complex data types with overriden equals methods....
	 * @param <T>
	 * @param property
	 * @param value
	 * @param set
	 * @return
	 */
	public static <T> Vector<T> filterByValue(String property, String value,
			Collection<T> set) 
	{
		try 
		{
			T t = set.iterator().next();
			
			Object bean = t.getClass().newInstance();
			BeanUtils.setProperty(bean, property, value);
	
			Vector<T> s = filterByObjectComparison(property, bean, set);
			if (s == null || s.size() == 0)
			{
				return null;
			}
			else 
			{
				lg.info(s.size() + "matching items in " + set);
				return s;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * returns a vector of objects where the "property" value is equal to the "property" value
	 * in the prototype.
	 * 
	 * i.e. if you send "name" , and a bean with a "name" field, it will return only beans
	 * who have matching values for name to the name in the prototype
	 * 

	 * @param <T>
	 * @param property
	 * @param prototype
	 * @param set
	 * @return
	 */
	public static <T> Vector<T> filterByObjectComparison(String property,
			Object prototype, Collection<T> set) {
		Vector<T> filtrate = new Vector<T>();

		try 
		{
			Iterator<T> i = set.iterator();
			PropertyEquivalenceFilter filter = new PropertyEquivalenceFilter(prototype, property);
			filter.setDebug(true);
			while (i.hasNext()) 
			{
				T t = i.next();
				if (filter.pass(t)) 
				{
					 lg.info("\tadding " + Bean.describe(t) + " where " + PropertyUtils.getProperty(prototype, property) +" = " + PropertyUtils.getProperty(t, property));
					filtrate.add(t);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("Filter failed.  returning original set of size "
							+ set.size());
			filtrate = new Vector<T>(set);
		}
		return filtrate;
	}

	public static <T> Vector<T> filter(Filter<T> filter, Set<T> set) {
		Iterator<T> i = set.iterator();
		Vector<T> filtrate = new Vector<T>();

		while (i.hasNext()) {
			T t = i.next();
			if (filter.pass(t))
				;
			filtrate.add(t);
		}

		return filtrate;
	}

	public static void copyAllProperties(Map m, Object model) {
		Iterator keys = m.keySet().iterator();

		while (keys.hasNext()) {
			Object key = keys.next();
			Object value = m.get(key);
			try {
				BeanUtils.setProperty(model, key.toString(), value.toString());
			} catch (Exception ex) {
				System.err.println("couldnt set property " + key + " to "
						+ value + " in " + model.getClass());
			}
		}
	}

	public static Object findKeyByHashCode(Map m, int h) {
		Iterator it = m.keySet().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o.hashCode() == h) {
				return o;
			}

		}
		System.err.println(Utilities.collapse(",", m.keySet().toArray())
				+ " ?? " + h);
		return null;
	}

	/**
	 * throw an exception on 
	 * purpose so you can see a stack trace. 
	 *
	 */
	public static void breakException() {
		String s = null;
		s.toCharArray();
	}

	public static <T> void forEach(Iterator<T> i, ObjectAction a,boolean log)
	{
		int count=0;
		while(i.hasNext())
		{
			T t = i.next();
			if(log)
			{
				lg.info("Iterating ... " + count++ +":"+t +" ["+t.getClass()+"]");
			}
			a.run(t);
		}
	}
	public static abstract class ObjectAction<T>
	{
		public abstract void run(T t);
	}
	public static abstract interface SpreadsheetRowAction
	{
		public abstract void run(Hashtable<String,String> namesToValues);
	}

	/**
	 * throw an exception on 
	 * purpose so you can see a stack trace. 
	 *
	 */
	public static void breakException(String s) {
		lg
				.warn("THROWING BREAK EXCEPTION " + s
						+ "(throwing breakException !)");
		breakException();
	}

	public static final String OSName = System.getProperty("os.name");

	public static String getSysPropOSName() {
		return OSName;
	}

	public static boolean contains(char[] c, char t) {
		for (int i = 0; i < c.length; i++) {
			if (c[i] == t)
				return true;
			else
				;//	System.out.print(c[i] + "X" + t + ",");
		}
		return false;
	}

	public static boolean contains(Object[] c, Object t) {
		for (int i = 0; i < c.length; i++) {
			if (c[i].toString().equals(t.toString()))
				return true;
			else
				;//	System.out.print(c[i] + "X" + t + ",");
		}
		return false;
	}

	
	/**
	 * Not tested 
	 * @param r
	 * @param skips
	 * @return
	 */
	public static Vector<Character> getNonAlphaNumerics(String r, char[] skips) {
		char[] characters = r.toCharArray();
		Vector<Character> v = new Vector<Character>();
		for (int i = 0; i < characters.length; i++) {
			if (Character.isLetterOrDigit(characters[i])
					|| contains(skips, characters[i]))
				;
			else
				v.add(new Character(characters[i]));
		}

		return v;
	}

	public static String replaceNonAlphaNumerics(String r, char replacement) 
	{
		char[] characters;
		if(r==null)
			characters=new char[]{};
		else
			characters=r.toCharArray();
		Vector<Character> v = new Vector<Character>();
		for (int i = 0; i < characters.length; i++) 
		{
			if (Character.isLetterOrDigit(characters[i]))
				v.add(new Character(characters[i]));
			else
				v.add(replacement);
		}
		Object[] chars = v.toArray();
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
			s.append(chars[i].toString());

		return s.toString();
	}

	
	public static String removeNonAlphaNumerics(String r, char[] preserves) 
	{
		char[] characters;
		if(r==null)
			characters=new char[]{};
		else
			characters=r.toCharArray();
		Vector<Character> v = new Vector<Character>();
		for (int i = 0; i < characters.length; i++) {
			if (Character.isLetterOrDigit(characters[i])|| contains(preserves, characters[i]))
				v.add(new Character(characters[i]));
			else
				;
		}
		Object[] chars = v.toArray();
		String s = new String();
		for (int i = 0; i < chars.length; i++)
			s += chars[i].toString();

		return s;
	}

	/**
	 * returns the class name with 
	 * the package prefix removed.  i.e. 
	 * 
	 * 
	 * Class a.b.Jay will return Jay
	 * @return
	 */
	public static String getClassName(Class c) {
		String packageName = c.getPackage().getName();
		//System.out.println("packageName " + packageName);
		return c.getName().replace(packageName + ".", "");
	}

	/**Not yet implemented 
	 * 
	 * @param fieldName
	 * @param candidates
	 * @param value
	 * @return
	 
	 public static Object[] findObjectsWithFieldEqualto(String fieldName, Object[] candidates, String value)
	 {
	 
	 }
	 */

	/**
	 * Returns true if the object is an instance or subtype of the 
	 * given class.
	 * @param candidate
	 * @param c
	 * @return
	 */
	public static boolean isSubclassOrInstanceOf(Object candidate, Class c) {
		if (c.isInstance(candidate))
			return true;

		return false;
	}

	public static final boolean isOSX = OSName.equalsIgnoreCase("Mac OS X");

	public static final boolean isLinux = OSName.equalsIgnoreCase("Linux");

	public static final boolean isWindowsXP = OSName
			.equalsIgnoreCase("Windows XP");

	public static final boolean isWindows95 = OSName
			.equalsIgnoreCase("Windows 95");

	public static final boolean isWindows98 = OSName
			.equalsIgnoreCase("Windows 98");

	public static final boolean isWindowsME = OSName
			.equalsIgnoreCase("Windows ME");

	public static final boolean isWindows9598ME = isWindows95 || isWindows98
			|| isWindowsME;

	public static int countNulls(Object[] o) {
		int n = 0;
		for (int i = 0; i < o.length; i++)
			if (o[i] == null)
				n++;

		return n;
	}

	public static <T> T loadClass(String name) throws Exception {
		return (T) Class.forName(name).newInstance();

	}

	/**
	 * Be careful with this method.  
	 * @param <T>
	 * @param o
	 * @return
	 */
	public static <T> T getDeepClone(T o) {
		try {
			return (T) ObjectCloner.deepCopy(o);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static class ObjectCloner {
		// so that nobody can accidentally create an ObjectCloner object
		private ObjectCloner() {
		}

		// returns a deep copy of an object
		static public Object deepCopy(Object oldObj) throws Exception {

			ObjectOutputStream oos = null;
			ObjectInputStream ois = null;
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream(); // A
				oos = new ObjectOutputStream(bos); // B
				// serialize and pass the object
				oos.writeObject(oldObj); // C
				oos.flush(); // D
				ByteArrayInputStream bin = new ByteArrayInputStream(bos
						.toByteArray()); // E
				ois = new ObjectInputStream(bin); // F
				// return the new object
				return ois.readObject(); // G
			} catch (Exception e) {
				System.out.println("Exception in ObjectCloner = " + e);
				throw (e);
			} finally {
				// oos.close();
				//  ois.close();
			}
		}

	}

	/**
	 * A simple compressor 
	 * @param input
	 * @return
	 */
	public static byte[] compress1(byte[] input) {

		System.out.print("COMPRESSING ... ");
		// Create the compressor with highest level of compression
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);

		// Give the compressor the data to compress
		compressor.setInput(input);
		compressor.finish();

		// Create an expandable byte array to hold the compressed data.
		// You cannot use an array that's the same size as the orginal because
		// there is no guarantee that the compressed data will be smaller than
		// the uncompressed data.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

		// Compress the data
		byte[] buf = new byte[1024];
		while (!compressor.finished()) {
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}
		try {
			bos.close();
		} catch (IOException e) {
		}

		System.out.println("DONE COMPRESSING");
		// Get the compressed data
		return bos.toByteArray();
	}

	/**
	 * A simple decompressor the integer is used 
	 * to indicate that it should be used with 
	 * a byte[] using the "compress1" from this
	 * class as the compression algorithm
	 * 
	 * @param compressedData
	 * @return
	 */
	public static byte[] decompress1(byte[] compressedData) {
		System.out.print("DE COMPRESSING ... ");

		// Create the decompressor and give it the data to compress
		Inflater decompressor = new Inflater();
		decompressor.setInput(compressedData);

		// Create an expandable byte array to hold the decompressed data
		ByteArrayOutputStream bos = new ByteArrayOutputStream(
				compressedData.length);

		// Decompress the data
		byte[] buf = new byte[1024];
		while (!decompressor.finished()) {
			try {
				int count = decompressor.inflate(buf);
				bos.write(buf, 0, count);
			} catch (DataFormatException e) {
			}
		}
		try {
			bos.close();
		} catch (IOException e) {
		}
		System.out.print("... DONE DE COMPRESSING");

		// Get the decompressed data
		return bos.toByteArray();
	}

	static Random r = new Random();

	public static String getNthKey(int n, Map h) {
		if (h == null)
			return "no param";
		return getNth(n, h.keySet().toArray());
	}

	private static String getNth(int n, Object[] o) {
		try {
			return (o[n]) + "";
		} catch (Exception e) {
			return "    ";
		}
	}

	public static String getNthValue(int n, Map h) {
		if (h == null)
			return "no value";

		return getNth(n, h.values().toArray());
	}

	public static String getBeanMethodName(String name) {
		return "get" + (name.charAt(0) + "").toUpperCase() + name.substring(1);
	}

	public static String WIDGET = "widget";

	public static String FUNCTION = "FB";

	public static String WIDGET_L = "widget list";

	public static String NMRPIPE_DAO = " nmr dao ";

	public static boolean printOn = false;

	public static String[] defaultStringArray = {};

	public static String NMRPIPEMODEL = "nmrpm";

	public static String NmrPipeArgText = "f";

	public static String SCRIPT_VW = "script viewer ";

	public static void print(Point p) {

		if (printOn)
			Log(p.toString());
	}

	//p16 b53
	public static class Reflection 
	{
		public static Class getCollectionType(Collection c)
		{
			if(c != null && c.size() != 0)
			{
				return c.iterator().next().getClass();
			}
			else
			{
				if(c != null)
					lg.warn("cant figure out collection type " + c + " " + c.size() );
				else
					lg.warn("COLLECTIN IS NULL");
				return Object.class;
			}
		}
		
		public static <T> Hashtable<String, T> getInnerClassesThatExtend(
				Class<T> parentClass, Object container) {

			//String =field name , T1= field value.
			Hashtable<String, T> fieldsAndValues = new Hashtable<String, T>();
			try {
				Class[] innerclasses = container.getClass()
						.getDeclaredClasses();
				for (Class f : innerclasses) {
					if (f.getSuperclass() == parentClass) {
						fieldsAndValues.put(f.getName(), (T) f);
					}
				}
			} catch (Exception e) {
				lg.warn(e.toString());
				return null;
			}
			return fieldsAndValues;
		}

		public static <T1> Hashtable<String, T1> getFieldsOfType(
				Class<T1> type, Object container) 
		{

			//String =field name , T1= field value.
			Hashtable<String, T1> fieldsAndValues = new Hashtable<String, T1>();
			try {
				Field[] fields = container.getClass().getFields();
				for (Field f : fields) {
					String fieldClassName = f.getType().getCanonicalName();
					String typeClassName = type.getCanonicalName();
					if (fieldClassName.equals(typeClassName)) 
					{
						T1 t = (T1) f.get(container);
						if(f.getName() != null && t != null)
							fieldsAndValues.put(f.getName(), t);
					} else {
						//System.err.println("NOT EQUAL<" + fieldClassName+"><"+typeClassName+">");
						//System.out.println(f.getName()+ " <" + f.getType() + "> != " + type + " ( " + container + " ) " + fields.length);
					}
				}
			} 
			catch (Exception e) 
			{
				lg.warn(e+"");
				e.printStackTrace();
				return null;
			}
			System.err.println("FOUND " + fieldsAndValues.size()
					+ " instances of " + type);
			return fieldsAndValues;

		}
	}

	public static String debugHashtable(Map ha) {
		int maxLength = 0;
		Iterator k = ha.keySet().iterator();

		while (k.hasNext()) {
			String x = k.next() + "";
			if (x.length() > maxLength)
				maxLength = x.length();
		}
		maxLength += 5;

		String s = "\t \n <Elements=" + ha.size() + ">   \n" + maxLength;
		s += "\t\t    " + Utilities.pad("KEY", ' ', maxLength)
				+ "       VALUE  \n";
		s += "\t\t __________________________________________\n";
		k = ha.keySet().iterator();
		int ii = 1;
		while (k.hasNext()) {
			Object o = k.next();

			s += "\t\t "
					+ Utilities.pad("| " + ii++ + " " + o + "", ' ', maxLength)
					+ "|    " + ha.get(o) + "\n";
		}
		s += "\t\t  __________________________________________\n";
		return s + "\n </Elements>";
	}

	/**
	 * pads the string with whitespace until the 
	 * limit is reached, used for homogeneity in print
	 * statements. 
	 * @param a
	 * @param l
	 * @return
	 */
	public static String pad(String a, char c, int l) {
		while (a.length() < l) {
			a = a + c;
		}

		return a;
	}

	/**
	 * [1,2,3] -> [?,?,?] 
	 * @param o
	 * @param q
	 */
	public static Object[] replaceAll(Object[] o, String q) {
		for (int i = 0; i < o.length; i++)
			o[i] = q;

		return o;
	}

	public static Object[] getArray(int l, Object o) {
		Object[] os = new Object[l];
		for (int i = 0; i < os.length; i++)
			os[i] = o.toString();

		return os;
	}

	/**
	 * [1,2,3] -> [?,?,?] 
	 * @param o
	 * @param q
	 */
	public static String getParentheticalArray(Object[] o) {
		String x = collapse(",", o);

		return "(" + x + ")";
	}

	static int count = 0;

	/**
	 * stall for n seconds
	 * @param seconds
	 */
	public static void stall(int seconds) {
		long r = System.currentTimeMillis();
		long s = System.currentTimeMillis();

		while (s - r < (seconds * 1000)) {
			s = System.currentTimeMillis();
		}

	}

	public static String readStream(InputStream is) {
		try 
		{
			byte[] b = new byte[is.available()];
			is.read(b);
			String x = new String(b);
			return x;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String getInputFromUser() {
		// 1. Create an InputStreamReader using the standard input stream.
		InputStreamReader isr = new InputStreamReader(System.in);

		// 2. Create a BufferedReader using the InputStreamReader created.
		BufferedReader stdin = new BufferedReader(isr);

		// 3. Don't forget to prompt the user
		// Log( "Type some data for the program: " );

		// 4. Use the BufferedReader to read a line of text from the user.
		try {
			return stdin.readLine();
		} catch (Exception e) {
			return null;
		}

	}
	public static String getInputFromUser(String str) {
		System.out.println(str);
		return getInputFromUser();
	}

	/**
	 * Do not include \n characters, these
	 * are automatically formatted at display time.
	 * 
	 * 40 characters is about 3 inches long an a typical console. 
	 * @param ss
	 * @return
	 */
	public static String addNewLinesToString(String ss, int l) {
		Vector strings = new Vector();

		while (ss.length() > l) {
			strings.add(ss.substring(0, l));
			ss = ss.substring(l);
		}

		strings.add(ss);

		return collapse("\n", strings.toArray());
	}

	public static String getDirForPipeFiles() {
		return System.getProperty(Utilities.SYS_PROP_USER_HOME) + "/"
				+ Utilities.DIR_CONNJUR;
	}

	public static String getStringOf(Object[] o) {

		if (o.length > 1)

		{
			String x = o[0].toString();

			for (int i = 1; i < o.length; i++)
				x += "," + o[i];

			return x;

		} else if (o.length == 1)
			return o[0].toString();

		else
			return "EMPTY!";
	}

	public static byte[] getByteArray(InputStream i) {
		try {
			byte[] t = new byte[i.available()];
			//System.out.println("starting to read " + 0 + " to " + i.available());
			i.read(t, 0, i.available());
			return t;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Finds an object based on toString identity, from an array of objects.
	 * @param o
	 * @param o
	 * @return
	 */
	public static Object findInArrayByToString(Object[] oa, String s) {
		for (int i = 0; i < oa.length; i++) {
			if (s.toString().equalsIgnoreCase(oa[i].toString()))
				return oa[i];
		}

		return null;
	}

	/**
	 * Finds an object's index based on toString identity, from an array of objects.
	 * @param o
	 * @param o
	 * @return
	 */
	public static int findIndexInArrayByToString(Object[] oa, String s) {
		if (oa == null | s == null)
			return -2;
		for (int i = 0; i < oa.length; i++) {
			if (s.toString().equalsIgnoreCase(oa[i].toString()))
				return i;
		}

		return -1;
	}

	public static String[] concat(String s, Object[] elems) {
		String[] s1 = new String[elems.length];
		for (int i = 0; i < elems.length; i++)
			s1[i] = s + elems[i];

		return s1;
	}

	public static Object findInArrayByEquals(Object[] pa, String s) {
		for (int i = 0; i < pa.length; i++) {
			if (pa.equals(s))
				return pa[i];
		}

		return null;
	}

	public static int max(int[] x) {
		if (x == null)
			return -1;

		int temp = x[0];

		for (int i = 1; i < x.length; i++) {
			temp = max(temp, x[i]);
		}

		return temp;
	}

	public static float max(float[] x) 
	{
		if (x == null)
			return -1;

		float temp = x[0];

		for (int i = 1; i < x.length; i++) {
			temp = Math.max(temp, x[i]);
		}

		return temp;
	}

	public static void print(String x) {
		if (printOn)
			Log(x);
	}

	public static int getTableRow(Point valueLoc, Point tableLoc, int height) {

		return valueLoc.y - tableLoc.y;

	}

	public static String getTrailingString(char c, String s) {
		int start = StringUtils.lastIndexOf(s, c);

		if (start == -1)
			return s;

		else
			return s.substring(StringUtils.lastIndexOf(s, c), s.length());

	}

	public static void println(String x) {
		if (printOn)
			Logln(x);
	}

	/**
	 * numbers start to finish , inclusive
	 * getArray(1,3) returns (3-1) +  1 = 3 integers 1,2,3 ...
	 * @param start
	 * @param finish
	 * @return
	 */
	public static int[] getArray(int start, int finish) {
		int l = finish - start + 1;
		int[] x = new int[l];

		for (int i = 0; i < l; i++)
			x[i] = start++;

		return x;
	}

	/**
	 * Collapse an enumeration into string form.  uses the collapse (",",[]) method.
	 * @param e
	 * @return
	 */
	public static String collapse(Enumeration e) {
		Vector v = new Vector();
		while (e.hasMoreElements()) {
			Object o = e.nextElement();
			v.add(o);
			//Logln(o + " = " + System.getProperties().getProperty(o.toString()) );
		}
		return Utilities.collapse(",", v.toArray());

	}
	public static String collapse(Object[] e) 
	{
		return Utilities.collapse(",", e);
	}


	public static void debugArray(Object[] o) {
		Utilities.Logln("DESCTRIPTION OF array of size (bean): " + o.length);
		try {
			for (int i = 0; i < o.length; i++) {
				Object bean = o[i];
				Map m = BeanUtils.describe(bean);
				Logln(m.toString());
			}

		}

		catch (Exception e) {
			e.printStackTrace();
		}

		Logln("done");
	}

	//static final public String SYS_PROP_NAME = "user.name";
	static final public String SYS_PROP_USER_HOME = "user.home";

	public static final String SYS_PROP_VERSION = "java.version";

	static final public String DIR_CONNJUR = "Connjur";

	/**
	 * Makes a directory [home]/Connjur if it doesnt exist already.
	 * Where [home] is the root directory as determined by the 
	 * System.getProperty("user.home") method. 
	 * returns 1 if directory was created
	 * returns 2 if directory wasnt created, but exists
	 * returns -1 if directroy doesn exist, AND wasnt created .
	 * @param title
	 * @return
	 */
	public static int makeDirectoryInHome() {
		String f = getHomeDir();

		if ((new File(f)).exists()) {
			Logln("UTILS DIR EXISTS !!!  " + f);

			return 2;
		}

		Logln("UTILS MAKING DIR !!! " + f);

		if (new File(f).mkdirs())
			return 1;

		return -1;
	}

	public static String writeFileToHomeDir(String n, String c) {
		try {
			File f = new File(getHomeDir(), n);
			FileWriter outFile = new FileWriter(f);
			PrintWriter out = new PrintWriter(outFile);

			// Also could be written as follows on one line
			// Printwriter out = new PrintWriter(new FileWriter(args[0]));

			// Write text to file
			out.print(c);
			out.close();
			return f.getAbsolutePath();
		}

		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Not yet tested but should work.
	 * @param txt
	 */
	public static String writeFileToHomeDir(String txt) {

		try {
			File f = new File(getHomeDir(), "CONNJUR_"
					+ System.currentTimeMillis());
			FileWriter outFile = new FileWriter(f);
			PrintWriter out = new PrintWriter(outFile);

			// Also could be written as follows on one line
			// Printwriter out = new PrintWriter(new FileWriter(args[0]));

			// Write text to file
			out.print(txt);
			out.close();
			return f.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 public static void writeFileToHomeDir(URL f1, URL f2)
	 {
	 File f11 = new File(f1.getPath());
	 File f22 = new File(f2.getPath());

	 try 
	 {
	 copy(f11,f22);
	 }
	 catch(Exception e)
	 {
	 e.printStackTrace();
	 Logln("file copy failed ~!");
	 }

	 }
	 **/

	/**
	 private static  void copy(File src, File dst) throws IOException 
	 {
	 InputStream in = new FileInputStream(src);
	 OutputStream out = new FileOutputStream(dst);
	 
	 // Transfer bytes from in to out
	 byte[] buf = new byte[2024];
	 int len;
	 while ((len = in.read(buf)) > 0) 
	 {
	 out.write(buf, 0, len);
	 }
	 in.close();
	 out.close();
	 }
	 **/

	/**
	 * Returns the com.connjur root directory ... i.e.
	 *  \b\a\Connjur
	 */
	public static String getHomeDir() {
		return System.getProperty(SYS_PROP_USER_HOME);
	}

	public static String getSystemPropertyNAME() {
		return getSystemProperty("user.name");
	}

	public static String getSystemProperty(String p) {
		if (p == null) {
			return System.getProperties().toString();
		}
		return System.getProperty(p);
	}

	public static Object[] merge(Object[] a1, Object[] a2) {
		Object[] mergedArray = new Object[a1.length + a2.length];

		for (int i = 0; i < a1.length; i++) {
			mergedArray[i] = a1[i];
		}

		for (int i = 0; i < a2.length; i++) {
			mergedArray[a1.length + i] = a2[i];
		}
		return mergedArray;
	}

	public static String[] merge(String[] x, String[] y) {

		if (y == null)
			return x;
		if (x == null)
			return y;

		Vector v = new Vector();
		for (int i = 0; i < x.length; i++)
			v.add(x[i]);
		for (int i = 0; i < y.length; i++)
			v.add(y[i]);

		return vectorToString(v);
	}

	public static String[] vectorToString(Vector v) {
		String[] s = new String[v.size()];
		for (int i = 0; i < s.length; i++) {
			s[i] = v.get(i).toString();
		}

		return s;

	}

	public static String[] merge(String[] x, String[] y, int p) {

		String[] z = new String[max(x.length + y.length, p * 2)];
		Logln(z.length + "");
		int in = 0;

		for (int i = 0; i < x.length; i++) {

			z[i] = x[i];
			in = i;
		}

		while (in < p) {
			in++;
			z[in] = "!NULL";

		}
		for (int i = 0; i < y.length && in < z.length; i++) {
			z[in++] = y[i];
		}

		while (in < p * 2) {
			z[in] = "!NULL";
			in++;
		}

		return z;

	}

	public static int max(int a, int b) {
		if (a > b)
			return a;

		return b;
	}

	public static void bug(int msg, String[] s) {
		if (msg == 1) {
			Log(msg);

			for (int i = 0; i < s.length; i++) {
				Log(s[i]);
				if (i != s.length - 1)
					Log(" , ");

			}

			Logln("");
		}

	}

	/**
	 * Returns only the strings not duplicated in 
	 * the to string descriptions.  This method 
	 * is for convenience, but is dangerous
	 * because 2 objects can be distinct and have similar
	 * to String return values... 
	 * @param a
	 * @return
	 */
	public static <T> T[] removeDuplicateStrings(T[] a) {
		Vector v = new Vector();
		for (int i = 0; i < a.length; i++) {
			if (!containsString(v, a[i]))
				v.add((T)a[i]);
		}
		return (T[])v.toArray();
	}
	public static <T> Set<T> removeDuplicateStrings(Set<T> a) 
	{
		HashSet<T> v = new HashSet<T>();
		for(T aa : a )
		{	if (!containsString(v.toArray(), aa))
			{
				v.add(aa);
			}
		}
		return v;
	}
	public static Vector<String> removeDuplicateStrings(Vector<String> v) {
		Vector<String> s = new Vector<String>();
		for (int i = 0; i < v.size(); i++) {
			if (!containsString(v, v.get(i)))
				s.add(v.get(i));
		}
		return s;
	}

	public static Vector<String> removeDuplicates(Collection<String> s) {
		//System.out.println(s.size());
		TreeSet<String> m = new TreeSet<String>();
		for (String st : s) {
			m.add(st);
		}
		//System.out.println(m.size());
		Vector<String> str=new Vector<String>();
 		str.addAll(m);
		return str;
	}
	

	public static boolean containsDuplicates(Vector<String> s, int index) {
		String instance = s.get(index);
		for (int i = 0; i < s.size(); i++) {
			String cand = s.get(i);
			if (index != i) {
				boolean eq = cand.toString().equalsIgnoreCase(instance);
				if (eq)
					return true;
			}
		}
		return false;
	}

	/**
	 * see containsString(Object[],Object)
	 */
	public static boolean containsString(Vector v, Object o) {
		return containsString(v.toArray(), o);
	}

	/**
	 * Not case sensitive. 
	 * returns true if any of the array elements are equal to o.
	 * @param v
	 * @param o
	 * @return
	 */
	public static boolean containsString(Object[] v, Object o) {
		for (Object i : v) {
			if (i.toString().trim().toUpperCase().equals(o.toString().trim().toUpperCase()))
				return true;
			else
				;//;System.out.println(Utilities.collapse(" ", v) + " doesnt contain *" + o+"*");
		}
		return false;
	}

	/**
	 * Removes duplicate elements from a string array, also removes nulls in the process.
	 * @param a
	 * @return
	 */
	public static String[] removeDuplicates(String[] a) {
		Vector v = new Vector();

		for (int i = 0; i < a.length; i++)
			if (a[i] != null && !v.contains(StringUtils.trim(a[i]))) {
				//Logln("adding " + a[i]);
				v.add(a[i]);
			}

		String[] o = new String[v.size()];

		for (int i = 0; i < o.length; i++) {
			o[i] = v.get(i) + "";
		}

		return o;

	}

	public static String getTimestamp() {
		Format formatter;
		Date date = new Date();

		formatter = new SimpleDateFormat("yyyy_MMMdd_hmasSS");
		String s = formatter.format(date);

		return s;
	}

	public static String getTimestampAfterAMPM() {
		Format formatter;
		Date date = new Date();

		formatter = new SimpleDateFormat("sSS");
		String s = formatter.format(date);

		return s;
	}

	/**
	 String AA="[^BIOUZ\\s]+";
	 String aa="[G|A|V|L|M|F|W|P|S|T|C|Y|N|-|Q|D|E|K|R|H|X]";
	 * @param args
	 */
	public static void main(String[] args) 
	{ }

	public static class Test {
		public static Map map() {
			Hashtable h = new Hashtable();
			h.put("name", "jay");
			h.put("phone", "123-456-7890");
			h.put("car", "honda");
			return h;
		}
	}

	/**
	 * Read the contents of a string, return it as a text file.
	 * @param f
	 * @return
	 */
	public static String getFileAsString(File f) {
		try {
			BufferedReader inStream = new BufferedReader(new FileReader(f));
			String s = "";
			while (inStream.ready()) {
				s += inStream.readLine();
			}
			return s;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String collapse(String split, Collection s) {
		if(s.size()==0)
			return "";
		String q="";
		Object[] a = s.toArray();
		for(int i = 0 ; i < a.length-1; i++)
		{
			q += a[i] + ""+split;
		}
		
		q+=a[a.length-1];
		return q;
	}
	
	public static String collapse(String split, int[] s) 
	{
		if(s.length==0)
			return "";
		String q="";
		for(int i = 0 ; i <s.length-1 ; i++)
		{
			q += s[i] + ""+split;
		}
		
		q+=s[s.length-1];
		return q;
	}


	public static String collapse(String split, char[] a) 
	{
		String q="";
		for(int i = 0 ; i < a.length-1; i++)
		{
			q += a[i] + ""+split;
		}
		q+=a[a.length-1];
		return q;
	}

	public static String collapse(String split, byte[] s) {
		Vector ss = new Vector();
		for (byte b : s) {
			ss.add(b);
		}
		return collapse(split, ss);
	}

	public static String collapse(String split, long[] l) {
		String[] s = new String[l.length];
		for (int i = 0; i < l.length; i++) {
			s[i] = l[i] + "";
		}
		return collapse(split, l);
	}

	/**
	 * Return a collapsed array.  Used for debugging multiple object values at once. 
	 * @param split
	 * @param s
	 * @return
	 */
	public static String collapse(String split, Object[] s) 
	{
		if (s == null)
		{
			return "NULL ARRRAY !";
		}

		else if (s.length <= 0  )
		{
			return "EMPTY ARRAY";
		}

		else 
		{
			StringBuffer x = new StringBuffer((s[0]+"").toString());
			for (int i = 1; i < s.length; i++) 
			{
				if (i < (s.length))
					x.append(split);
				x.append(s[i]);
			}
			return x.toString();
		}

	}

	public static String collapseRemoveNulls(String split, Object[] s) {
		String x = collapse(split, s);
		return x.substring(0, x.indexOf("null"));
	}

	public static String[] getStringArray(Object[] o) {
		String[] s = new String[o.length];

		for (int i = 0; i < s.length; i++) {
			s[i] = o[i].toString() + "";
		}
		return s;
	}

	public static int[] getIntArray(int val, int length) {
		int[] vals = new int[length];

		for (int i = 0; i < length; i++)
			vals[i] = val;

		return vals;
	}

	public static Object[] getObjectArray(int[] s) {
		Object[] obs = new Object[s.length];

		for (int i = 0; i < s.length; i++)
			obs[i] = s[i] + "";

		return obs;
	}

	public static Object[] getObjectArray(String[] s) {
		Object[] obs = new Object[s.length];

		for (int i = 0; i < s.length; i++)
			obs[i] = s[i] + "";

		return obs;
	}

	/**
	 * compress the extra white space in a string into 
	 * single spaces
	 * @param input
	 * @return
	 */
	public static String normalizeString(String input) {
		String[] s = input.split(" ");
		String t = "";
		for (int i = 0; i < s.length; i++) {
			t = t + " " + s[i];
		}

		//now normalize the new lines.
		String[] s2 = input.split("\n");
		String t2 = "";
		for (int i = 0; i < s2.length; i++) {
			t2 = t2 + "\n" + s2[i];
		}

		return t2;
	}

	/**
	 * Code here taken (modified) from 
	 * http://www.koders.com/java/fid705F5AACAD4FC99C971F0350222393186E1D1326.aspx
	 * @author 
	 *
	 */
	public static class XML
	{
		public static void makeClass( String f,File xmlfile)
		{
			try 
			{
				//JAXBContext jc = JAXBContext.newInstance(f);
				//Unmarshaller u = jc.createUnmarshaller();
				//Object obj = u.unmarshal(xmlfile);
				//Marshaller m = jc.createMarshaller();
				//m.marshal(obj, System.out);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	/**
	 * An example of how to get uniprot data from an xml file.
	 * Document d = Utilities.XML.getDocument(u);
			Element i = d.getDocument().getRootElement();
			List n=i.elements();
			//for each element in the root.
			for(Object kk : n)
			{
				DefaultElement k1 = (DefaultElement)kk;
				if(k1.getName().equals("entry"));
				{
					System.out.println(k1.elementText("name"));
					List entryElements=k1.elements();
				
				}
			}
	 * @param url
	 * @return
	 */
		public static Document getDocument(URL url)
		{
		        try
		        {
		        	SAXReader reader = new SAXReader();
		        	Document document = reader.read(url);
		        	return document;
		        }
		        catch(Exception e)
		        {
		        	e.printStackTrace();
		        }
		        return null;
		}
		public static Map<String,Vector<String>> getValues(URL l, Vector<String> querys)
		{
			try
			{
				Document document = getDocument(l);
				Hashtable<String,Vector<String>> hash = new Hashtable<String,Vector<String>> ();
				for(String query : querys)
				{			
				     List<Node> nodes = document.selectNodes(query);
				      for (Node node : nodes)
				      {
				         String value = node.getNodeValue();
				         if(! hash.containsKey(query))
				         {
				        	hash.put(query,new Vector<String>());
				         }
				         hash.get(query).add(value);
				      }
				}
			return hash;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		public static String getField(String f, Node n) {
			String c = n.getAttributes().getNamedItem(f).getNodeValue();
			c = c.replaceAll(f + " ", "");
			return c;
		}

		public static interface XMLProvider {
			public String toXML();
		}

		public static boolean looksLikeXml(Object v) {
			return !(v.toString().indexOf("<") == -1);
		}

		/**
		 * generates a XML string out of a map.
		 */

		public static Element mapToRawXml(Map map, Element root) {
			Set keySet = map.keySet();
			Iterator i = keySet.iterator();
			while (i.hasNext()) {
				Object key = i.next();
				Object value = map.get(key);
				Element newElement = root.addElement(key.toString());

				//is an xml data type. 
				if (value instanceof Map) {
					mapToRawXml((Map) value, newElement);
				}

				else if (looksLikeXml(value)) {
					newElement.addElement(value + "");
				}
				//attributes make the definition smaller ! 
				else {
					newElement.addAttribute("value", value + "");
				}

			}
			return root;
		}

		/**
		 * Set of utilities for XML processing - especially for data structures 
		 * This is a class supporting the use of dom4J, an open source implementation
		 * of the DOM model for Java. See www.dom4J.org for documentation and information. 
		 * 
		 * Package: nzdis.util<br>
		 * File: Dom4JUtil.java<br>
		 * Created: 14/01/2004  15:41:51<br>
		 *
		 * @author  <a href="mailto:lehrler@infoscience.otago.ac.nz">Lars Ehrler</a>
		 * @version $Revision: 1.1 $Date: 2009/12/02 16:09:48 $
		 **/
		public static class Dom4JUtil {

			/*
			 * the standard-name of an element(for example for a map or a set) used in the XML 
			 */
			public static final String ELEMENT = "element";

			/**
			 * parses a XMLString to a Map.
			 */
			public static Map xmlToMap(String xmlString) throws Exception {

				Map resultMap = new HashMap();
				Document doc;
				doc = DocumentHelper.parseText(xmlString);

				Element root = doc.getRootElement();

				for (Iterator i = root.elementIterator(); i.hasNext();) {
					Element el = (Element) i.next();
					//String value=el.getText().substring(1); // removing bracket @see createElement
					//value=value.substring(0,value.length()-1); // removing bracket @see createElement
					String key = el.getName().substring(1); // removing underscore @see createElement
					resultMap.put(key, el.getText());
				}
				return resultMap;
			}

			/**
			 * parses a XMLString to a List.
			 * HAS BEEN KNOWN TO HANG !!!!  
			 */
			public static List xmlToList(String xmlString) throws Exception {
				List resultList = new ArrayList();
				Map map = xmlToMap(xmlString);
				int listSize = map.keySet().size();
				int processedElements = 0;
				int position = -1;
				while (processedElements < listSize) {
					position++;
					Integer positionI = new Integer(position);
					Object value = map.get(positionI);
					if (!(value == null)) {
						resultList.add(value);
						processedElements++;
					}
				}
				return resultList;
			}

			/**
			 * parses a XMLString to a Set. 
			 */
			public static Set xmlToSet(String xmlString) throws Exception {
				return new HashSet(xmlToMap(xmlString).values());
			}

			/**
			 * 
			 * generates a XML string out of a map.
			 * 
			 */
			public static String mapToXML(Map map, String rootName) {
				Document doc = DocumentHelper.createDocument();
				doc.addElement(rootName);
				Set keySet = map.keySet();
				Iterator i = keySet.iterator();
				while (i.hasNext()) {
					Object key = i.next();
					Object value = map.get(key);
					createElement(doc.getRootElement(), key + "", value + "");
				}
				return doc.asXML().replaceAll("&lt;", "<").replaceAll("&gt;",
						">");
			}

			/**
			 * generates a XML string out of a list. 
			 */
			public static String listToXml(List list, String rootName) {
				HashMap map = new HashMap();
				int i = list.size();
				for (int a = 0; a < i; a++) {
					map.put(new Integer(a).toString(), list.get(a));
				}
				return mapToXML(map, rootName);
			}

			/**
			 * generates a XML string out of a set.  
			 * @return the XML string or null if an error occured
			 */
			public static String setToXml(Collection set, String rootName) {
				return listToXml(new ArrayList(set), rootName);
			}

			/**
			 * creates a new Element as a text node 
			 */
			public static void createElement(Element parent, Object key,
					Object value) {
				//removed an _ from the prefix.  Why ? 
				//String tagName = "_"+key.toString();
				String tagName = key.toString();
				if (value == null) {
					parent.addElement(tagName).addText("");
				} else {
					if (value instanceof String) {
						parent.addElement(tagName).addText((String) value);
					} else {
						parent.addElement(tagName).addText(value.toString());
					}
				}
			}
		}
	}

	/**
	 * Compress the whitespace in astring to single spaces,
	 * but also gaurantee its minimal length to be the int argument.
	 * @param input
	 * @return
	 */
	public static String normalizeString(String input, int min) {
		String t = normalizeString(input);

		while (t.length() < min)
			t += " ";

		return t;
	}

	public static boolean isNumber(String xx) {
		try {
			Float.parseFloat(xx);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public static Object getRandomIndex(Collection obs) 
	{
		int index = getRandomNumberOfMax(obs.size());
	
		return obs.toArray()[Math.abs(index-1)];
	}

	public static Object getRandomIndex(char[] obs) {
		Vector v = new Vector();
		for (char c : obs)
			v.add(c);
		return getRandomIndex(v);
	}

	public static int getRandomNumberOfMax(int s) {
		if (s == 0)
			return 0;
		return r.nextInt(s);

	}

	public static boolean hasNullsOrBlanks(String[] o) {
		for (int i = 0; i < o.length; i++) {
			if (o[i] == null || o[i].toString().trim().length() == 0) {
				return true;
			} else
				Log(o[i].toString().trim().length() + "-" + o[i] + ";");
		}
		return false;
	}

	public static boolean hasNulls(Object[] o) {
		for (int i = 0; i < o.length; i++)
			if (o[i] == null)
				return true;

		return false;
	}

	public static void Logln(String s) {
		System.out.println("UTIL : " + s);
	}

	public static void Log(int s) {
		System.out.println("UTIL : " + s);
	}

	public static void Log(Object o) {
		System.out.println("UTIL : " + o.toString());
	}

	public static void Log(String s) {
		System.out.println("UTIL : " + s);
	}

	public static final int MEGABYTE = 1048576;

	
	public static double testMemoryHeapPercent()
	{
		double free = Runtime.getRuntime().freeMemory();
		double max = Runtime.getRuntime().maxMemory();
		return free/max;
	}
	/**
	 * Returns the megabytes of allocated memory. 
	 * @param maximumMegabytesToTest
	 * @return
	 */
	public static long testMemory(int maximumMegabytesToTest) {
		//Hold on to memory, or it will be garbage collected
		Object[] memoryHolder = new Object[maximumMegabytesToTest];
		int count = 0;
		try 
		{
			for (; count < memoryHolder.length; count++) 
			{
				memoryHolder[count] = new byte[MEGABYTE];
			}
		} 
		catch (OutOfMemoryError bounded) 
		{
			bounded.printStackTrace();

		}
		long highWater = Runtime.getRuntime().totalMemory();
		  System.out.println("High water in bytes: " 
		  + highWater);
		//  System.out.println("Megabytes allocatable in 
		//  megabytes: " + count);
		memoryHolder = null; //release for GC
		//We know we could allocate "count" megabytes and 
		//have a high water mark of "highWater". Return 
		//whichever you prefer.
		return count;
		//return highWater;
	}

	/**
	 * decode a point from a to string 
	 * @param s
	 * @return
	 */
	public static Point decodePoint(String s) {
		if (s == null)
			return new Point(0, 0);
		//System.out.println(s);
		String pxy = StringUtils.substringBetween(s, "[", "]");
		//System.out.println(pxy);

		String[] xy = pxy.split(",");

		int x = Integer.parseInt(StringUtils.substringAfter(xy[0], "="));
		int y = Integer.parseInt(StringUtils.substringAfter(xy[1], "="));

		return new Point(x, y);
	}

	public static boolean isSynchronized(String methodName, Class c) {
		try {
			return Modifier.isSynchronized(c.getMethod("run", new Class[] {})
					.getModifiers());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Run a runnable method as its own thread. 
	 * 
	 * IMPORTANT : The running method must be declared as syncrhonized or 
	 * an exception could be thrown. This can be done easily by typing the 
	 * word synchronized in the signature of the "run" method. 
	 * This is a very useful method.  Connjur's gui uses it.  
	 *
	 * ALSO : Put this code at the end of youre run method ... 
	 * 
	 *  try
	 *				 {
	 *					 this.wait(REFRESH_RATE);
	 *				 }
	 *				 catch(Exception e)
	 *				 {
	 *					 e.printStackTrace();
	 *				 }
	 * 
	 * @author vyas
	 * @param r (the runnable object) .
	 *
	 */

	/**
	 Deletes all files and subdirectories under dir.
	 Returns true if all deletions were successful.
	 If a deletion fails, the method stops attempting to delete and returns false.
	 THIS METHOD IS VERY DANGEROUS !
	 **/
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	public static class Web {

		public static void main(String[] args) throws IOException {

			System.in.read();
			
			try {
				URL url = new URL(
						"http://computing.dcu.ie/~humphrys/howtomailme.html");

				BufferedReader in = new BufferedReader(new InputStreamReader(
						url.openStream()));
				String str;

				while ((str = in.readLine()) != null) {
					//System.out.println(str);
				}

				in.close();
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
		}

	}

}