package experimental;

import java.util.LinkedList;

public class TestAlgorithms {

	
	public static void main(String[] args)
	{
		TestAlgorithms a = new TestAlgorithms();
		System.out.println(a.calcSqrt(64));
		System.out.println("non leaf nodes in a 64 leaf list = " +a.countnodes(64));

		System.out.println("Starting link list reversal");
		Link rev = a.reverse(new Link(1,2,3,4));
		System.out.println("Linked list reversal = " + rev);

		//System.out.println("Starting LL middle search");
		//Link med = new Link(1,2,3,4,5);
		//System.out.println("LL middle = " +middle(med)); 
	}
	
	static class Link
	{
		public int value;
		public Link next;
		public Link(int... values)
		{
				
			this.value=values[0];
				for(int i = 1; i < values.length; i++)
				{
					Link newL = new Link(values[i]);
					this.next=newL;
				}
		}
		public String toString()
		{
			return (value +" " + next);
		}
	}
	
	public int middle(Link list)
	{
		int count =0;
		int mid = list.value;
		while(list.next != null)
		{
			count++;
			
		}
		System.err.println("untested ");
		return mid;
	}
	
	public Link reverse(Link list)
	{
		int myValue= list.value;
		System.out.println(list.value);
		if(list.next==null)
		{
			System.out.println("head = " + list.value+" is at end, returning.");
			return list;
		}
		else
		{
			Link reversed = reverse(list.next);
			Link end = reversed;
			while(end.next!=null)
				end=end.next;
			end.next=new Link(myValue);
			return reversed;
		}
	}
	
	public int countnodes(int nodes)
	{
		int sum = 0;
		while(nodes>0)
		{
			nodes=nodes/2;
			sum += nodes;
		}
		return sum;
	}
	
	public Integer calcSqrt(int input)
	{
		for(int i = 0; i < input; i++)
			if(i*i==input)
				return i;
			else
				;
		return null;
	}
		
}
