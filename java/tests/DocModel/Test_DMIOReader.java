//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.util.ArrayList;

import BritefuryJ.DocModel.DMIOReader;
import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMModuleResolver;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMModule.ClassAlreadyDefinedException;
import junit.framework.TestCase;

public class Test_DMIOReader extends TestCase
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
		catch (ClassAlreadyDefinedException e)
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

	
	
	
	public void readTest(String input, Object expected)
	{
		Object res = null;
		try
		{
			res = DMIOReader.readFromString( input, resolver );
		}
		catch (DMIOReader.ParseErrorException e)
		{
			System.out.println( "PARSE FAILURE" );
			fail();
		}
		
		boolean bEqual = res.equals( expected );

		if ( !bEqual )
		{
			System.out.println( "VALUES ARE NOT THE SAME" );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
			System.out.println( "RESULT:" );
			System.out.println( res.toString() );
		}
		
		assertTrue( bEqual );
	}



	
	public void testReadUnquotedString()
	{
		readTest( "abc", "abc" );
		readTest( "abc123ABC_", "abc123ABC_" );
		//readTest( "abc123ABC_+-*/%^&|!$@.<>~", "abc123ABC_+-*/%^&|!$@.<>~" );
	}

/*	public void testReadQuotedString()
	{
		readTest( "\"abcd\"", "abcd" );
		readTest( "\"ab\\ncd\"", "ab\ncd" );
		readTest( "\"ab\\tcd\"", "ab\tcd" );
		readTest( "\"ab\\rcd\"", "ab\rcd" );
		readTest( "\"ab\\\\cd\"", "ab\\cd" );
		readTest( "\"ab\\x0107xcd\"", "ab" + new Character( (char)Integer.valueOf( "107", 16 ).intValue() ).toString() + "cd" );
	}*/

	/*public void testReadEmptyString()
	{
		readTest( "\"\"", "" );
	}*/
	
	public void testReadList()
	{
		ArrayList<Object> h = new ArrayList<Object>();
		h.add( "h" );
		h.add( "1" );
		h.add( "2L" );
		h.add( "3.0" );
		
		ArrayList<Object> g = new ArrayList<Object>();
		g.add( "g" );
		g.add( h );
		g.add( "Hi" );

		ArrayList<Object> f = new ArrayList<Object>();
		f.add( "f" );
		f.add( g );
		f.add( "There" );
		f.add( new Character( (char)Integer.valueOf( "0107", 16 ).intValue() ).toString() );

		readTest( "[f [g [h 1 2L 3.0] \"Hi\"] \"There\" \"\\x0107x\"]", f );
	}
	
	/*public void testReadObject()
	{
		DMObject o = A.newInstance( new Object[] { "0", "1" } );
		readTest( "{m=test.module (m A x=0 y=1)}", o );
	}*/
}
