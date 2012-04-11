package legacy.util;

import java.util.Collection;
import java.util.Vector;

public class StringVector extends Vector<String>
{

	public StringVector(String... a)
	{
		for(String aa:a)add(aa);
	}
	public StringVector() {
		super();
		// TODO Auto-generated constructor stub
	}

	public StringVector(Collection<? extends String> c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	public StringVector(int initialCapacity, int capacityIncrement) {
		super(initialCapacity, capacityIncrement);
		// TODO Auto-generated constructor stub
	}

	public StringVector(int initialCapacity) {
		super(initialCapacity);
		// TODO Auto-generated constructor stub
	}	

}
