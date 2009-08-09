//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Transformation;

import java.util.Arrays;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMList;
import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMModule.ClassAlreadyDefinedException;
import BritefuryJ.Transformation.DefaultIdentityTransformationFunction;
import BritefuryJ.Transformation.TransformationFunction;

public class Test_DefaultIdentityTransformationFunction extends TestCase
{
	private DMModule m;


	public void setUp()
	{
		m = new DMModule( "m", "m", "test.m" );
	}
	
	public void tearDown()
	{
		m = null;
	}

	
	
	public void test_identity_transform() throws ClassAlreadyDefinedException
	{
		TransformationFunction xform = new TransformationFunction()
		{
			public Object apply(Object x, TransformationFunction innerNodeXform)
			{
				if ( x instanceof DMObject )
				{
					return ((DMObject)x).getDMClass().getName();
				}
				else if ( x instanceof DMList )
				{
					return "[]";
				}
				else if ( x instanceof String )
				{
					return x;
				}
				else
				{
					throw new RuntimeException();
				}
			}
		};
		
		
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObjectClass B = m.newClass( "B", new String[] { "p", "q" } );
		
		DMObject a0 = A.newInstance( new Object[] { "a", "b" } );
		DMObject b0 = B.newInstance( new Object[] { "c", Arrays.asList( new Object[] { "d", a0 } ) } );
		
		DefaultIdentityTransformationFunction identity = new DefaultIdentityTransformationFunction();
		DMObject b0x = (DMObject)identity.apply( b0, xform );
		
		assertNotSame( b0x, b0 );
		assertEquals( b0x.get( 0 ), "c" );
		assertNotSame( b0x.get( 1 ), b0.get( 1 ) );
		DMList x1 = (DMList)b0x.get( 1 );
		assertEquals( x1.get( 0 ), "d" );
		assertEquals( x1.get( 1 ), "A" ); 
	}

}
