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
import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMModuleResolver;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectInputStream;
import BritefuryJ.DocModel.DMObjectOutputStream;

public class Test_DocModelSerialisation extends TestCase
{
	private DMModule module;
	private DMModuleResolver resolver;
	private DMObjectClass A;
	
	
	public void setUp()
	{
		module = new DMModule( "module", "m", "test.module" );
		try
		{
			A = module.newClass( "A", new String[] { "x", "y" } );
		}
		catch (DMModule.ClassAlreadyDefinedException e)
		{
			throw new RuntimeException();
		}
		
		
		resolver = new DMModuleResolver()
		{
			public DMModule getModule(String location)
			{
				if ( location.equals( "test.module" ) )
				{
					return module;
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
		module = null;
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
