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
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldCascading;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldChildPack;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldDirect;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldPack;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldSet;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValues;

public class Test_StyleSheetValues extends TestCase
{
	private ElementStyleSheetField aElementField = ElementStyleSheetField.newField( "a", Integer.class );
	private ElementStyleSheetField bElementField = ElementStyleSheetField.newField( "b", Integer.class );
	private ElementStyleSheetField childPack_dElementField = ElementStyleSheetField.newField( "childPack_d", Integer.class );
	private ElementStyleSheetField pack_dElementField = ElementStyleSheetField.newField( "pack_d", Integer.class );

	private StyleSheetValueFieldDirect aValueField = StyleSheetValueFieldDirect.newField( "a", Integer.class, 10, aElementField );
	private StyleSheetValueFieldCascading bValueField = StyleSheetValueFieldCascading.newField( "b", Integer.class, 1, bElementField );
	private StyleSheetValueFieldChildPack childPack_dValueField = StyleSheetValueFieldChildPack.newField( "childPack_d", Integer.class, 15, childPack_dElementField );
	private StyleSheetValueFieldPack pack_dValueField = StyleSheetValueFieldPack.newField( "pack_d", Integer.class, 5, pack_dElementField, childPack_dValueField );
	
	
	public void test_containsKey()
	{
		StyleSheetValues x = new StyleSheetValues();
		
		assertTrue( x.containsKey( "a" ) );
		assertTrue( x.containsKey( "b" ) );
		assertTrue( x.containsKey( "childPack_d" ) );
		assertTrue( x.containsKey( "pack_d" ) );
	}

	public void test_get()
	{
		StyleSheetValues x = new StyleSheetValues();
		
		assertEquals( x.get( "a" ), 10 );
		assertEquals( x.get( "b" ), 1 );
		assertEquals( x.get( "childPack_d" ), 15 );
		assertEquals( x.get( "pack_d" ), 5 );
		assertEquals( x.get( aValueField ), 10 );
		assertEquals( x.get( bValueField ), 1 );
		assertEquals( x.get( childPack_dValueField ), 15 );
		assertEquals( x.get( pack_dValueField ), 5 );
	}

	public void test_equals()
	{
		StyleSheetValues x1 = new StyleSheetValues();
		StyleSheetValues x2 = new StyleSheetValues();
		
		assertTrue( x1.equals( x2 ) );
	}
	
	
	
	public void test_cascade()
	{
		StyleSheetValues x = new StyleSheetValues();
		
		assertEquals( x.get( "a" ), 10 );
		assertEquals( x.get( "b" ), 1 );
		assertEquals( x.get( "childPack_d" ), 15 );
		assertEquals( x.get( "pack_d" ), 5 );
		
		StyleSheetValueFieldSet noneUsed = new StyleSheetValueFieldSet();
		StyleSheetValueFieldSet allUsed = new StyleSheetValueFieldSet( aValueField, bValueField, childPack_dValueField, pack_dValueField );

		
		ElementStyleSheet e1 = new ElementStyleSheet( new String[] { "a", "b", "childPack_d", "pack_d" },  new Object[] { 2, 4, 6, 8 } );
		StyleSheetValues y = StyleSheetValues.cascade( x, e1, noneUsed );
		StyleSheetValues y1 = StyleSheetValues.cascade( x, e1, noneUsed );
		
		assertEquals( y.get( "a" ), 2 );
		assertEquals( y.get( "b" ), 4 );
		assertEquals( y.get( "childPack_d" ), 6 );
		assertEquals( y.get( "pack_d" ), 8 );
		assertTrue( y.equals( y1 ) );

	
		ElementStyleSheet f1 = new ElementStyleSheet( new String[] { "a" },  new Object[] { 11 } );
		ElementStyleSheet f2 = new ElementStyleSheet( new String[] { "b" },  new Object[] { 13 } );
		ElementStyleSheet f2b = new ElementStyleSheet( new String[] { "b" },  new Object[] { null } );
		ElementStyleSheet f3 = new ElementStyleSheet( new String[] { "childPack_d" },  new Object[] { 17 } );
		ElementStyleSheet f4 = new ElementStyleSheet( new String[] { "pack_d" },  new Object[] { 19 } );
		assertEquals( StyleSheetValues.cascade( y, f1, allUsed ).get( "a" ), 11 );
		assertEquals( StyleSheetValues.cascade( y, f1, allUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.cascade( y, f1, allUsed ).get( "childPack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f1, allUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f2, allUsed ).get( "a" ), 10 );
		assertEquals( StyleSheetValues.cascade( y, f2, allUsed ).get( "b" ), 13 );
		assertEquals( StyleSheetValues.cascade( y, f2, allUsed ).get( "childPack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f2, allUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f2, allUsed ).get( "a" ), 10 );
		assertEquals( StyleSheetValues.cascade( y, f2b, allUsed ).get( "b" ), null );
		assertEquals( StyleSheetValues.cascade( y, f2, allUsed ).get( "childPack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f2, allUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f3, allUsed ).get( "a" ), 10 );
		assertEquals( StyleSheetValues.cascade( y, f3, allUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.cascade( y, f3, allUsed ).get( "childPack_d" ), 17 );
		assertEquals( StyleSheetValues.cascade( y, f3, allUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f4, allUsed ).get( "a" ), 10 );
		assertEquals( StyleSheetValues.cascade( y, f4, allUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.cascade( y, f4, allUsed ).get( "childPack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f4, allUsed ).get( "pack_d" ), 19 );

		assertEquals( StyleSheetValues.cascade( y, f1, noneUsed ).get( "a" ), 11 );
		assertEquals( StyleSheetValues.cascade( y, f1, noneUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.cascade( y, f1, noneUsed ).get( "childPack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f1, noneUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f2, noneUsed ).get( "a" ), 2 );
		assertEquals( StyleSheetValues.cascade( y, f2, noneUsed ).get( "b" ), 13 );
		assertEquals( StyleSheetValues.cascade( y, f2, noneUsed ).get( "childPack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f2, noneUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f2, noneUsed ).get( "a" ), 2 );
		assertEquals( StyleSheetValues.cascade( y, f2b, noneUsed ).get( "b" ), null );
		assertEquals( StyleSheetValues.cascade( y, f2, noneUsed ).get( "childPack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f2, noneUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f3, noneUsed ).get( "a" ), 2 );
		assertEquals( StyleSheetValues.cascade( y, f3, noneUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.cascade( y, f3, noneUsed ).get( "childPack_d" ), 17 );
		assertEquals( StyleSheetValues.cascade( y, f3, noneUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f4, noneUsed ).get( "a" ), 2 );
		assertEquals( StyleSheetValues.cascade( y, f4, noneUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.cascade( y, f4, noneUsed ).get( "childPack_d" ), 6 );
		assertEquals( StyleSheetValues.cascade( y, f4, noneUsed ).get( "pack_d" ), 19 );
	}

	public void test_packingContainerCascade()
	{
		StyleSheetValues x = new StyleSheetValues();
		
		assertEquals( x.get( "a" ), 10 );
		assertEquals( x.get( "b" ), 1 );
		assertEquals( x.get( "childPack_d" ), 15 );
		assertEquals( x.get( "pack_d" ), 5 );

		
		StyleSheetValueFieldSet noneUsed = new StyleSheetValueFieldSet();
		StyleSheetValueFieldSet allUsed = new StyleSheetValueFieldSet( aValueField, bValueField, childPack_dValueField, pack_dValueField );

		
		ElementStyleSheet e1 = new ElementStyleSheet( new String[] { "a", "b", "childPack_d", "pack_d" },  new Object[] { 2, 4, 6, 8 } );
		StyleSheetValues y = StyleSheetValues.packingContainerCascade( x, e1, noneUsed );
		StyleSheetValues y1 = StyleSheetValues.packingContainerCascade( x, e1, noneUsed );
		
		assertEquals( y.get( "a" ), 2 );
		assertEquals( y.get( "b" ), 4 );
		assertEquals( y.get( "childPack_d" ), 6 );
		assertEquals( y.get( "pack_d" ), 8 );
		assertTrue( y.equals( y1 ) );

	
		ElementStyleSheet f1 = new ElementStyleSheet( new String[] { "a" },  new Object[] { 11 } );
		ElementStyleSheet f2 = new ElementStyleSheet( new String[] { "b" },  new Object[] { 13 } );
		ElementStyleSheet f3 = new ElementStyleSheet( new String[] { "childPack_d" },  new Object[] { 17 } );
		ElementStyleSheet f4 = new ElementStyleSheet( new String[] { "pack_d" },  new Object[] { 19 } );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f1, allUsed ).get( "a" ), 11 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f1, allUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f1, allUsed ).get( "childPack_d" ), 15 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f1, allUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f2, allUsed ).get( "a" ), 10 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f2, allUsed ).get( "b" ), 13 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f2, allUsed ).get( "childPack_d" ), 15 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f2, allUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f3, allUsed ).get( "a" ), 10 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f3, allUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f3, allUsed ).get( "childPack_d" ), 17 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f3, allUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f4, allUsed ).get( "a" ), 10 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f4, allUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f4, allUsed ).get( "childPack_d" ), 15 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f4, allUsed ).get( "pack_d" ), 19 );

		assertEquals( StyleSheetValues.packingContainerCascade( y, f1, noneUsed ).get( "a" ), 11 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f1, noneUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f1, noneUsed ).get( "childPack_d" ), 15 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f1, noneUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f2, noneUsed ).get( "a" ), 2 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f2, noneUsed ).get( "b" ), 13 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f2, noneUsed ).get( "childPack_d" ), 15 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f2, noneUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f3, noneUsed ).get( "a" ), 2 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f3, noneUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f3, noneUsed ).get( "childPack_d" ), 17 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f3, noneUsed ).get( "pack_d" ), 6 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f4, noneUsed ).get( "a" ), 2 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f4, noneUsed ).get( "b" ), 4 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f4, noneUsed ).get( "childPack_d" ), 15 );
		assertEquals( StyleSheetValues.packingContainerCascade( y, f4, noneUsed ).get( "pack_d" ), 19 );
	}
}
