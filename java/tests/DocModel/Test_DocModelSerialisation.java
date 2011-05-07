//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.io.IOException;

import junit.framework.TestCase;

public class Test_DocModelSerialisation extends TestCase
{
	//private static DMSchema schema;
	//private static DMObjectClass A;


	static
	{
		//schema = new DMSchema( "schema", "m", "test.DocModel.Test_DocModelSerialisation.schema" );
		//A = schema.newClass( "A", new String[] { "x", "y" } );
	}
	
	
	
	
	public void test_serialisation() throws IOException, ClassNotFoundException
	{
		// Document model objects are NOT SERIALISABLE
		assertTrue( false );
		
/*		DMObject obj = A.newInstance( new Object[] { "a", new DMList( Arrays.asList( new Object[]{ "x" } ) ) } );
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ObjectOutputStream outDM = new ObjectOutputStream( outStream );
		outDM.writeObject( obj );
		
		ByteArrayInputStream inStream = new ByteArrayInputStream( outStream.toByteArray() );
		ObjectInputStream inDM = new ObjectInputStream( inStream );
		DMObject obj2 = (DMObject)inDM.readObject();
		
		assertNotSame( obj, obj2 );
		assertEquals( obj, obj2 );
		assertSame( obj.getDMObjectClass(), obj2.getDMObjectClass() );*/
	}
}
