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

import BritefuryJ.DocModel.*;
import junit.framework.TestCase;
import BritefuryJ.DocModel.DMSchema;

public class Test_DocModelSerialisation extends TestCase
{
	private DMSchema schema;
	private DMSchemaResolver resolver;
	private DMObjectClass A;


	public void setUp()
	{
		schema = new DMSchema( "schema", "m", "test.schema" );
		try
		{
			A = schema.newClass( "A", new String[] { "x", "y" } );
		}
		catch (DMSchema.ClassAlreadyDefinedException e)
		{
			throw new RuntimeException();
		}
		
		
		resolver = new DMSchemaResolver()
		{
			public DMSchema getSchema(String location)
			{
				if ( location.equals( "test.schema" ) )
				{
					return schema;
				}
				else
				{
					return null;
				}
			}
		};
	}
	
	
	public void tearDown()
	{
		schema = null;
		resolver = null;
		A = null;
	}
	
	
	
	public void test_serialisation() throws IOException, ClassNotFoundException
	{
		DMObject obj = A.newInstance( new Object[] { "a", new DMList( Arrays.asList( new Object[]{ "x" } ) ) } );
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		DMObjectOutputStream outDM = new DMObjectOutputStream( outStream );
		outDM.writeObject( obj );
		
		ByteArrayInputStream inStream = new ByteArrayInputStream( outStream.toByteArray() );
		DMObjectInputStream inDM = new DMObjectInputStream( inStream, resolver );
		DMObject obj2 = (DMObject)inDM.readObject();
		
		assertNotSame( obj, obj2 );
		assertEquals( obj, obj2 );
		assertSame( obj.getDMObjectClass(), obj2.getDMObjectClass() );
	}
}
