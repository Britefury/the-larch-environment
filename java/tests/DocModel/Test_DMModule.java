//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectField;
import BritefuryJ.DocModel.DMSchema;

public class Test_DMModule extends TestCase
{
	private static DMSchema m = new DMSchema( "m", "m", "tests.DocModel.Test_DMSchema.m" );
	private static DMObjectClass c = new DMObjectClass( m, "c", new String[] {} );
	
	public void test_get()
	{
		assertSame( m.get( "c" ), c );
	}

	public void test_getitem()
	{
		assertSame( m.__getitem__( "c" ), c );
	}
	
	public void test_newInstance()
	{
		DMObjectField f1[] = { new DMObjectField( "x" ) };

		DMObjectClass A = m.newClass( "A", f1 );
		DMObjectClass B = m.newClass( "B", new String[] { "x" } );
		DMObjectClass C = m.newClass( "C", A, f1 );
		DMObjectClass D = m.newClass( "D", B, new String[] { "x" } );

		assertSame( m.get( "A" ), A );
		assertSame( m.get( "B" ), B );
		assertSame( m.get( "C" ), C );
		assertSame( m.get( "D" ), D );
	}
}
