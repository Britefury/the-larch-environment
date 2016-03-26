//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util;

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

	public static int quadHash(int a, int b, int c, int d)
	{
		int mult = 1000003;
		int x = 0x345678;
		x = ( x ^ d ) * mult;
		mult += 82520 + 6;
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
