//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Utils;

import BritefuryJ.Utils.BinarySearch;
import junit.framework.TestCase;

public class Test_BinarySearch extends TestCase
{
	public void test_binarySearch()
	{
		double data[] = { 1.0, 2.0, 3.0, 4.0 };
		
		assertSame( BinarySearch.binarySearchInsertionPoint( data, 0.5 ), 0 );
		assertSame( BinarySearch.binarySearchInsertionPoint( data, 1.0 ), 0 );
		assertSame( BinarySearch.binarySearchInsertionPoint( data, 1.5 ), 1 );
		assertSame( BinarySearch.binarySearchInsertionPoint( data, 2.0 ), 1 );
		assertSame( BinarySearch.binarySearchInsertionPoint( data, 2.5 ), 2 );
		assertSame( BinarySearch.binarySearchInsertionPoint( data, 3.0 ), 2 );
		assertSame( BinarySearch.binarySearchInsertionPoint( data, 3.5 ), 3 );
		assertSame( BinarySearch.binarySearchInsertionPoint( data, 4.0 ), 3 );
		assertSame( BinarySearch.binarySearchInsertionPoint( data, 4.5 ), 4 );
	}
}
