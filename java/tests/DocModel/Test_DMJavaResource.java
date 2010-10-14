//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.DocModel;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;
import BritefuryJ.DocModel.Resource.DMJavaResource;

public class Test_DMJavaResource extends TestCase
{
	public void test_constructor() throws IOException
	{
		String s = DMJavaResource.serialise( Color.RED );
		DMJavaResource r = new DMJavaResource( Color.RED );
		assertEquals( s, r.getSerialisedForm() );
	}

	public void test_getValue() throws IOException
	{
		DMJavaResource r = new DMJavaResource( Color.RED );
		assertEquals( Color.RED, r.getValue() );
	}
	
	public void test_serialisation() throws IOException, ClassNotFoundException
	{
		DMJavaResource rOut = new DMJavaResource( Color.RED );
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream( outStream );
		objOut.writeObject( rOut );
		
		ByteArrayInputStream inStream = new ByteArrayInputStream( outStream.toByteArray() );
		ObjectInputStream objIn = new ObjectInputStream( inStream );
		DMJavaResource x = (DMJavaResource)objIn.readObject();
		
		assertEquals( Color.RED, x.getValue() );
	}
}
