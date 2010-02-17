//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Utils;

public class HashUtils
{
	// Hash code combiner; from Jython/CPython

	public static int doubleHash(int a, int b)
	{
		int mult = 1000003;
		int x = 0x345678;
		x = ( x ^ b ) * mult;
		mult += 82520 + 2;
		x = ( x ^ a ) * mult;
		return x + 97351;
	}
	
	public static int tripleHash(int a, int b, int c)
	{
		int mult = 1000003;
		int x = 0x345678;
		x = ( x ^ c ) * mult;
		mult += 82520 + 4;
		x = ( x ^ b ) * mult;
		mult += 82520 + 2;
		x = ( x ^ a ) * mult;
		return x + 97351;
	}

        public static int nHash(int n[])
	{
		int mult = 1000003;
		int x = 0x345678;
		int i = n.length;
		while ( --i >= 0 )
		{
			x = ( x ^ n[i] ) * mult;
			mult += 82520 + i + i;
		}
		return x + 97351;
	}
}
