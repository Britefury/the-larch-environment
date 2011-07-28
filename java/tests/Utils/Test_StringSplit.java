//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.Utils;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import BritefuryJ.Util.StringSplit;

public class Test_StringSplit extends TestCase
{
	private void splitTest(String input, String pattern, String[] results)
	{
		List<String> xs = StringSplit.split( input, pattern );
		assertEquals( xs, Arrays.asList( results ) );
	}
	
	
	public void test_split()
	{
		splitTest( "abc", "b", new String[] { "a", "c" } );
		splitTest( "abc", "c", new String[] { "ab", "" } );
		splitTest( "abbc", "b", new String[] { "a", "", "c" } );
		splitTest( "abbbc", "b", new String[] { "a", "", "", "c" } );
	}
}
