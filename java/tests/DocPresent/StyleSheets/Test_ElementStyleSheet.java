//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.StyleSheets;

import junit.framework.TestCase;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheetField;

public class Test_ElementStyleSheet extends TestCase
{
	private ElementStyleSheetField aField = ElementStyleSheetField.newField( "a", Integer.class );
	
	
	public void test_containsKey()
	{
		ElementStyleSheet x = new ElementStyleSheet();
		ElementStyleSheet y = new ElementStyleSheet( new String[] { "a" },  new Object[] { 5 } );
		
		assertFalse( x.containsKey( "a" ) );
		assertTrue( y.containsKey( "a" ) );
	}

	public void test_get()
	{
		ElementStyleSheet x = new ElementStyleSheet();
		ElementStyleSheet y = new ElementStyleSheet( new String[] { "a" },  new Object[] { 5 } );
		ElementStyleSheet z = new ElementStyleSheet( new String[] { "a" },  new Object[] { 10 } );
		
		assertEquals( x.get( "a" ), null );
		assertEquals( y.get( "a" ), 5 );
		assertEquals( z.get( "a" ), 10 );
		assertEquals( x.get( aField ), null );
		assertEquals( y.get( aField ), 5 );
		assertEquals( z.get( aField ), 10 );
	}

	public void test_equals()
	{
		ElementStyleSheet x1 = new ElementStyleSheet();
		ElementStyleSheet x2 = new ElementStyleSheet();
		ElementStyleSheet y1 = new ElementStyleSheet( new String[] { "a" },  new Object[] { 5 } );
		ElementStyleSheet y2 = new ElementStyleSheet( new String[] { "a" },  new Object[] { 5 } );
		
		assertTrue( x1.equals( x2 ) );
		assertTrue( y1.equals( y2 ) );
		assertFalse( x1.equals( y2 ) );
	}
}
