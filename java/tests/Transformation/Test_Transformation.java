//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Transformation;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMModule.ClassAlreadyDefinedException;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;
import BritefuryJ.Transformation.DefaultIdentityTransformationFunction;
import BritefuryJ.Transformation.Transformation;
import BritefuryJ.Transformation.TransformationFunction;

public class Test_Transformation extends TestCase
{
	private DMModule m;
	private DMObjectClass TwoStrings, TwoNodes, StringNode;
	private TransformationFunction x1, x2, x3;
	private DMObject data_s, data_nss, data_bs, data_bs_x1, data_bs_x2, data_bs_x12, data_nbss, data_nbss_x1, data_nbss_x2, data_nbss_x12;


	public void setUp()
	{
		m = new DMModule( "m", "m", "test.m" );
		try
		{
			TwoStrings = m.newClass( "TwoStrings", new String[] { "s", "t" } );
			TwoNodes = m.newClass( "TwoNodes", new String[] { "m", "n" } );
			StringNode = m.newClass( "StringNode", new String[] { "s", "n" } );
		}
		catch (ClassAlreadyDefinedException e)
		{
			fail();
		}
		
		
		data_s = TwoStrings.newInstance( new Object[] { "a", "b" } );
		
		data_nss = TwoNodes.newInstance( new Object[] { TwoStrings.newInstance( new Object[] { "a", "b" } ), TwoStrings.newInstance( new Object[] { "c", "d" } ) } );

		data_bs = StringNode.newInstance( new Object[] { "a", TwoStrings.newInstance( new Object[] { "b", "c" } ) } );
		data_bs_x1 = StringNode.newInstance( new Object[] { "ajk", TwoStrings.newInstance( new Object[] { "b", "c" } ) } );
		data_bs_x2 = StringNode.newInstance( new Object[] { "apq", TwoStrings.newInstance( new Object[] { "b", "c" } ) } );
		data_bs_x12 = StringNode.newInstance( new Object[] { "ajkpq", TwoStrings.newInstance( new Object[] { "b", "c" } ) } );

		data_nbss = TwoNodes.newInstance( new Object[] { StringNode.newInstance( new Object[] { "a", TwoStrings.newInstance( new Object[] { "b", "c" } ) } ), TwoStrings.newInstance( new Object[] { "d", "e" } ) } );
		data_nbss_x1 = TwoNodes.newInstance( new Object[] { StringNode.newInstance( new Object[] { "ajk", TwoStrings.newInstance( new Object[] { "b", "c" } ) } ), TwoStrings.newInstance( new Object[] { "d", "e" } ) } );
		data_nbss_x2 = TwoNodes.newInstance( new Object[] { StringNode.newInstance( new Object[] { "apq", TwoStrings.newInstance( new Object[] { "b", "c" } ) } ), TwoStrings.newInstance( new Object[] { "d", "e" } ) } );
		data_nbss_x12 = TwoNodes.newInstance( new Object[] { StringNode.newInstance( new Object[] { "ajkpq", TwoStrings.newInstance( new Object[] { "b", "c" } ) } ), TwoStrings.newInstance( new Object[] { "d", "e" } ) } );

		
		
		x1 = new TransformationFunction()
		{
			public Object apply(Object x, TransformationFunction innerNodeXform)
			{
				DMObject dx = (DMObject)x;
				if ( dx.getDMClass() == StringNode )
				{
					try
					{
						return StringNode.newInstance( new Object[] { (String)dx.get( "s" ) + "jk", innerNodeXform.apply( dx.get( "n" ), innerNodeXform ) } );
					}
					catch (InvalidFieldNameException e)
					{
						throw new RuntimeException();
					}
				}
				else
				{
					return TransformationFunction.cannotApplyTransformationValue;
				}
			}
		};

		x2 = new TransformationFunction()
		{
			public Object apply(Object x, TransformationFunction innerNodeXform)
			{
				DMObject dx = (DMObject)x;
				if ( dx.getDMClass() == StringNode )
				{
					try
					{
						return StringNode.newInstance( new Object[] { (String)dx.get( "s" ) + "pq", innerNodeXform.apply( dx.get( "n" ), innerNodeXform ) } );
					}
					catch (InvalidFieldNameException e)
					{
						throw new RuntimeException();
					}
				}
				else
				{
					return TransformationFunction.cannotApplyTransformationValue;
				}
			}
		};

		x3 = new TransformationFunction()
		{
			public Object apply(Object x, TransformationFunction innerNodeXform)
			{
				DMObject dx = (DMObject)x;
				if ( dx.getDMClass() == StringNode )
				{
					try
					{
						String s = (String)dx.get( "s" );
						if ( s.startsWith( "x" ) )
						{
							return StringNode.newInstance( new Object[] { "pq" + s, innerNodeXform.apply( dx.get( "n" ), innerNodeXform ) } );
						}
						else
						{
							return TransformationFunction.cannotApplyTransformationValue;
						}
					}
					catch (InvalidFieldNameException e)
					{
						throw new RuntimeException();
					}
				}
				else
				{
					return TransformationFunction.cannotApplyTransformationValue;
				}
			}
		};
	}
	
	public void tearDown()
	{
		m = null;
		TwoStrings = TwoNodes = StringNode = null;
	}

	

	public void test_xform1()
	{
		Transformation xf = new Transformation( new DefaultIdentityTransformationFunction(), new TransformationFunction[] { x1 } );
		
		assertSame( xf.apply( data_s ), data_s );
		assertSame( xf.apply( data_nss ), data_nss );
		assertNotSame( xf.apply( data_bs ), data_bs );
		assertEquals( xf.apply( data_bs ), data_bs_x1 );
		assertNotSame( xf.apply( data_nbss ), data_nbss );
		assertEquals( xf.apply( data_nbss ), data_nbss_x1 );
	}

	public void test_xform2()
	{
		Transformation xf = new Transformation( new DefaultIdentityTransformationFunction(), new TransformationFunction[] { x2 } );
		
		assertSame( xf.apply( data_s ), data_s );
		assertSame( xf.apply( data_nss ), data_nss );
		assertNotSame( xf.apply( data_bs ), data_bs );
		assertEquals( xf.apply( data_bs ), data_bs_x2 );
		assertNotSame( xf.apply( data_nbss ), data_nbss );
		assertEquals( xf.apply( data_nbss ), data_nbss_x2 );
	}

	public void test_xform12()
	{
		Transformation xf = new Transformation( new DefaultIdentityTransformationFunction(), new TransformationFunction[] { x1, x2 } );
		
		assertSame( xf.apply( data_s ), data_s );
		assertSame( xf.apply( data_nss ), data_nss );
		assertNotSame( xf.apply( data_bs ), data_bs );
		assertEquals( xf.apply( data_bs ), data_bs_x12 );
		assertNotSame( xf.apply( data_nbss ), data_nbss );
		assertEquals( xf.apply( data_nbss ), data_nbss_x12 );
	}
	
	public void test_propagation()
	{
		//Test propagation:
		//When:
		//   - the original data is a nested node, 3 levels deep: Node1( Node2( focusNode( innerNode() ) ) )
		//   - we perform a transformation that affects only 'focusNode'
		//Ensure that the result is what is expected:
		//   - the root node is not the same object (by identity), although the contents are equal, all the way down to the transformed version of 'focusNode'
		//   - Result should be Node1'( Node2'( focusNode'( innerNode() ) ) )
		
		Transformation xf = new Transformation( new DefaultIdentityTransformationFunction(), new TransformationFunction[] { x3 } );
		
		DMObject d0 = TwoStrings.newInstance( new Object[] { "a", "b" } );
		DMObject d1 = StringNode.newInstance( new Object[] { "x", d0 } );
		DMObject d2 = StringNode.newInstance( new Object[] { "b", d1 } );
		DMObject data = StringNode.newInstance( new Object[] { "a", d2 } );

		DMObject r0 = TwoStrings.newInstance( new Object[] { "a", "b" } );
		DMObject r1 = StringNode.newInstance( new Object[] { "pqx", r0 } );
		DMObject r2 = StringNode.newInstance( new Object[] { "b", r1 } );
		DMObject result = StringNode.newInstance( new Object[] { "a", r2 } );
		
		DMObject dataXf = (DMObject)xf.apply( data );
		
		assertNotSame( dataXf, data );
		assertEquals( dataXf, result );
	}
}
