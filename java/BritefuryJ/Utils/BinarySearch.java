//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Utils;

public class BinarySearch
{
	public static int binarySearchInsertionPoint(double[] sorted, double key)
	{
		return binarySearchInsertionPoint( sorted, 0, sorted.length, key );
	}

	public static int binarySearchInsertionPoint(double[] sorted, int lo, int hi, double key)
	{
		while ( lo < hi )
		{
			int mid = ( lo + hi ) / 2;
			if ( key < sorted[mid] )
			{
				hi = mid;
			}
			else if ( key > sorted[mid] )
			{
				lo = mid + 1;
			}
			else
			{
				return mid;
			}
		}
		
		return lo;
	}
}
