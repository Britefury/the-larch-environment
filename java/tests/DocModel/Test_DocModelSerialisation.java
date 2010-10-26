//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMList;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectInputStream;
import BritefuryJ.DocModel.DMObjectOutputStream;
import BritefuryJ.DocModel.DMSchema;

public class Test_DocModelSerialisation extends TestCase
{
	private static DMSchema schema;
	private static DMObjectClass A;


	static
	{
		schema = new DMSchema( "schema", "m", "test.DocModel.Test_DocModelSerialisation.schema" );
		A = schema.newClass( "A", new String[] { "x", "y" } );
	}
	
	
	
	
	public void test_serialisation() throws IOException, ClassNotFoundException
	{
		DMObject obj = A.newInstance( new Object[] { "a", new DMList( Arrays.asList( new Object[]{ "x" } ) ) } );
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		DMObjectOutputStream outDM = new DMObjectOutputStream( outStream );
		outDM.writeObject( obj );
		
		ByteArrayInputStream inStream = new ByteArrayInputStream( outStream.toByteArray() );
		DMObjectInputStream inDM = new DMObjectInputStream( inStream );
		DMObject obj2 = (DMObject)inDM.readObject();
		
		assertNotSame( obj, obj2 );
		assertEquals( obj, obj2 );
		assertSame( obj.getDMObjectClass(), obj2.getDMObjectClass() );
	}
}
