//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectField;
import BritefuryJ.DocModel.DMModule.ClassAlreadyDefinedException;
import BritefuryJ.DocModel.DMModule.UnknownClassException;
import junit.framework.TestCase;

public class Test_DMModule extends TestCase
{
	public void test_get() throws UnknownClassException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m", "m", "test.m" );
		
		DMObjectClass c = new DMObjectClass( m, "c", new String[] {} );
		
		assertSame( m.get( "c" ), c );
	}

	public void test_getitem() throws ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m", "m", "test.m" );
		
		DMObjectClass c = new DMObjectClass( m, "c", new String[] {} );
		
		assertSame( m.__getitem__( "c" ), c );
	}
	
	public void test_newInstance() throws UnknownClassException, ClassAlreadyDefinedException
	{
		DMObjectField f1[] = { new DMObjectField( "x" ) };

		DMModule m = new DMModule( "m", "m", "test.m" );
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
