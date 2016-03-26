//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
