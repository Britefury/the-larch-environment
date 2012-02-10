//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.Benchmarks.StyleSheet;

import BritefuryJ.AttributeTable.Attribute;
import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.AttributeTable;

public class BenchmarkAttributeTable
{
	private static class ApplyAttributeTable
	{
		private AttributeTable t;
		
		public ApplyAttributeTable(AttributeTable t)
		{
			this.t = t;
		}
		
		public AttributeTable table(AttributeTable base)
		{
			return base.withAttrs( t );
		}
	}
	
	private static AttributeNamespace ns;
	private static Attribute a, b, c, d;
	private static AttributeTable attrs1, attrs2, attrs3, attrs4;
	private static ApplyAttributeTable x1, x2, x3, x4;
	
	
	private static void init()
	{
		ns = new AttributeNamespace( "benchmark" );
		a = new Attribute( ns, "a", Integer.class, 1 );
		b = new Attribute( ns, "b", Integer.class, 1 );
		c = new Attribute( ns, "c", Integer.class, 1 );
		d = new Attribute( ns, "d", Integer.class, 1 );
		
		attrs1 = AttributeTable.values( a.as( -1 ), b.as( -2 ), c.as( -3 ), d.as( -4 ) );
		attrs2 = AttributeTable.values( a.as( -10 ), b.as( -20 ), c.as( -30 ), d.as( -40 ) );
		attrs3 = AttributeTable.values( a.as( -100 ), b.as( -200 ), c.as( -300 ), d.as( -400 ) );
		attrs4 = AttributeTable.values( a.as( -1000 ), b.as( -2000 ), c.as( -3000 ), d.as( -4000 ) );
		
		x1 = new ApplyAttributeTable(attrs1);
		x2 = new ApplyAttributeTable(attrs2);
		x3 = new ApplyAttributeTable(attrs3);
		x4 = new ApplyAttributeTable(attrs4);
	}
	
	
	
	private static int derive()
	{
		AttributeTable y1 = x1.table( AttributeTable.instance );
		AttributeTable y2 = x2.table( y1 );
		AttributeTable y3 = x3.table( AttributeTable.instance );
		AttributeTable y4 = x4.table( y3 );
		return (Integer)y1.get( a ) + (Integer)y2.get( b ) + (Integer)y3.get( c ) + (Integer)y4.get( d );
	}
	
	
	private static int test(int repeats)
	{
		int accum = 0;
		for (int i = 0; i < repeats; i++)
		{
			accum += derive();
		}
		return accum;
	}
	
	
	private static final int REPEATS = 1000000;
	private static final int WARMUP = 1000;
	
	public static void main(String args[])
	{
		init();
		
		test(WARMUP);
		
		long t1 = System.nanoTime();
		test(REPEATS);
		long t2 = System.nanoTime();
		
		System.out.println( "Time to derive " + REPEATS + " attribute tables: " + (t2-t1)*1.0e-9 );
	}
	
	
}
