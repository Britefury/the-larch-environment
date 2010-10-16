//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMIOReader;
import BritefuryJ.DocModel.DMIOWriter;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectReader;
import BritefuryJ.DocModel.DMSchema;
import BritefuryJ.DocModel.DMSchemaResolver;
import BritefuryJ.DocModel.DMIOWriter.InvalidDataTypeException;
import BritefuryJ.DocModel.Resource.DMJavaResource;
import BritefuryJ.DocModel.Resource.DMPyResource;

public class Test_DMIOReader extends TestCase
{
	protected static class TestReader extends DMIOReader
	{
		protected TestReader(String source, DMSchemaResolver resolver)
		{
			super( source, resolver );
		}

		protected static MatchResult test_match(Pattern pattern, String source, int position)
		{
			return match( pattern, source, position );
		}
	}

	
	private DMSchema schemaA;
	private DMObjectClass A;
	
	private DMSchema schemaBv1, schemaBv7;
	private DMObjectClass B1, B7;
	
	private DMSchemaResolver resolver1, resolver2;
	
	private DMObjectReader b1ReaderForV7, b4ReaderForV7;
	
	public void setUp()
	{
		schemaA = new DMSchema( "schema", "m", "test.schemaA" );
		A = schemaA.newClass( "A", new String[] { "x", "y" } );
		
		schemaBv1 = new DMSchema( "schema", "m", "test.schemaB" );
		B1 = schemaBv1.newClass( "B", new String[] { "x", "y" } );

		schemaBv7 = new DMSchema( "schema", "m", "test.schemaB", 7 );
		B7 = schemaBv7.newClass( "B", new String[] { "x", "z" } );
		b1ReaderForV7 = new DMObjectReader()
		{
			@Override
			public DMObject readObject(Map<String, Object> fieldValues)
			{
				DMObject instance = B7.newInstance();
				instance.set( "x", fieldValues.get( "x" ) );
				instance.set( "z", fieldValues.get( "y" ) );
				return instance;
			}
		};
		b4ReaderForV7 = new DMObjectReader()
		{
			@Override
			public DMObject readObject(Map<String, Object> fieldValues)
			{
				DMObject instance = B7.newInstance();
				instance.set( "x", fieldValues.get( "x" ) );
				instance.set( "z", fieldValues.get( "w" ) );
				return instance;
			}
		};
		schemaBv7.registerReader( "B", 1, b1ReaderForV7 );
		schemaBv7.registerReader( "B", 4, b4ReaderForV7 );
		
		
		resolver1 = new DMSchemaResolver()
		{
			public DMSchema getSchema(String location)
			{
				if ( location.equals( "test.schemaA" ) )
				{
					return schemaA;
				}
				if ( location.equals( "test.schemaB" ) )
				{
					return schemaBv1;
				}
				else
				{
					return null;
				}
			}
		};

		resolver2 = new DMSchemaResolver()
		{
			public DMSchema getSchema(String location)
			{
				if ( location.equals( "test.schemaA" ) )
				{
					return schemaA;
				}
				if ( location.equals( "test.schemaB" ) )
				{
					return schemaBv7;
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
		schemaA = null;
		A = null;

		schemaBv1 = null;
		B1 = null;
		schemaBv7 = null;
		B7 = null;

		resolver1 = null;
		resolver2 = null;
		
		b1ReaderForV7 = null;
		b4ReaderForV7 = null;
	}

	
	
	
	public void matchTest(Pattern pattern, String input, String expected)
	{
		DMIOReader.MatchResult res = TestReader.test_match( pattern, input, 0 );
		
		if ( res.value == null )
		{
			System.out.println( "MATCH FAILURE" );
			System.out.println( "EXPECTED: " + expected );
		}
		

		assertNotNull( res.value );
		
		if ( !res.value.equals( expected ) )
		{
			System.out.println( "VALUES ARE NOT THE SAME" );
			System.out.println( "EXPECTED: " + expected );
			System.out.println( "RESULT: " + res.value );
		}
		
		
		assertTrue( res.value.equals( expected ) );
	}

	public void matchFailTest(Pattern pattern, String input)
	{
		DMIOReader.MatchResult res = TestReader.test_match( pattern, input, 0 );
		
		if ( res != null  &&  res.value.equals( input ) )
		{
			System.out.println( "MATCH SHOULD HAVE FAILED" );
			System.out.println( "RESULT: " + res.value );
		}
		

		assertTrue( res == null  ||  !res.value.equals( input ) );
	}
	
	
	

	public void readTest(String input, Object expected)
	{
		readTest( resolver1, input, expected );
	}

	public void readTest(DMSchemaResolver resolver, String input, Object expected)
	{
		Object res = null;
		try
		{
			res = DMIOReader.readFromString( input, resolver );
		}
		catch (DMIOReader.ParseErrorException e)
		{
			System.out.println( "PARSE FAILURE: " + e.getMessage() );
			fail();
		}
		
		boolean bEqual = res == expected  ||  res.equals( expected );

		try
		{
			if ( !bEqual )
			{
				System.out.println( "VALUES ARE NOT THE SAME" );
				System.out.println( "EXPECTED:" );
				System.out.println( expected == null  ?  "<null>"  :  DMIOWriter.writeAsString( expected ) );
				System.out.println( "RESULT:" );
				System.out.println( res == null  ?  "<null>"  :  DMIOWriter.writeAsString( res ) );
			}
		}
		catch (InvalidDataTypeException e)
		{
		}
		
		assertTrue( bEqual );
	}



	
	public void testWhitespace()
	{
		matchTest( DMIOReader.whitespace, " \t\n", " \t\n" );
	}

	public void testUnquotedString()
	{
		matchTest( DMIOReader.unquotedString, "abc123ABC_", "abc123ABC_" );
		matchTest( DMIOReader.unquotedString, "abc123ABC_+-*/%^&|!$@.~", "abc123ABC_+-*/%^&|!$@.~" );
		matchFailTest( DMIOReader.unquotedString, "abc123ABC_+-*/%^&|!$@.,=[]~(" );
		matchFailTest( DMIOReader.unquotedString, "abc123ABC_+-*/%^&|!$@.,=[]~)" );
		matchFailTest( DMIOReader.unquotedString, "abc123ABC_+-*/%^&|!$@.,=[]~<" );
		matchFailTest( DMIOReader.unquotedString, "abc123ABC_+-*/%^&|!$@.,=[]~>" );
		matchFailTest( DMIOReader.unquotedString, "abc123ABC_+-*/%^&|!$@.,=[]~\"" );
		matchFailTest( DMIOReader.unquotedString, "abc123ABC_+-*/%^&|!$@.,=[]~ " );
		matchFailTest( DMIOReader.unquotedString, "abc123ABC_+-*/%^&|!$@.,=[]~\t" );
		matchFailTest( DMIOReader.unquotedString, "abc123ABC_+-*/%^&|!$@.,=[]~\n" );
		matchFailTest( DMIOReader.unquotedString, "abc123ABC_+-*/%^&|!$@.,=[]~\\" );
		matchFailTest( DMIOReader.unquotedString, "abc123ABC_`" );
	}

	public void testQuotedString()
	{
		matchTest( DMIOReader.quotedString, "\"abc123ABC_\"", "\"abc123ABC_\"" );
		matchTest( DMIOReader.quotedString, "\"abc123ABC_`\"", "\"abc123ABC_`\"" );
		matchTest( DMIOReader.quotedString, "\"abc123()ABC_\"", "\"abc123()ABC_\"" );
		matchTest( DMIOReader.quotedString, "\"abc123( )ABC_\"", "\"abc123( )ABC_\"" );
		matchTest( DMIOReader.quotedString, "\"abc123(\\\\)ABC_\"", "\"abc123(\\\\)ABC_\"" );
		matchTest( DMIOReader.quotedString, "\"abc123(\\n)ABC_\"", "\"abc123(\\n)ABC_\"" );
		matchTest( DMIOReader.quotedString, "\"abc123(\\r)ABC_\"", "\"abc123(\\r)ABC_\"" );
		matchTest( DMIOReader.quotedString, "\"abc123(\\t)ABC_\"", "\"abc123(\\t)ABC_\"" );
		matchTest( DMIOReader.quotedString, "\"abc123(\\x123abcx)ABC_\"", "\"abc123(\\x123abcx)ABC_\"" );
		matchFailTest( DMIOReader.quotedString, "\"abc123(\\x)ABC_\"" );
		matchFailTest( DMIOReader.quotedString, "\"abc123(\\x123)ABC_\"" );
	}
	
	public void testHexCharEscape()
	{
		Pattern pat = Pattern.compile( DMIOReader.hexCharEscape );
		matchFailTest( pat, "x" );
		matchTest( pat, "\\x0x", "\\x0x" );
		matchTest( pat, "\\xAx", "\\xAx" );
		matchFailTest( pat, "\\xx" );
		matchFailTest( pat, "\\xA" );
		matchFailTest( pat, "\\xG" );
	}

	public void testWhitespaceEscape()
	{
		Pattern pat = Pattern.compile( DMIOReader.whitespaceEscape );
		matchFailTest( pat, "\\x" );
		matchTest( pat, "\\n", "\\n" );
		matchTest( pat, "\\r", "\\r" );
		matchTest( pat, "\\t", "\\t" );
		matchTest( pat, "\\\\", "\\\\" );
	}

	public void testEscapeSequence()
	{
		Pattern pat = Pattern.compile( DMIOReader.escapeSequence );
		matchTest( pat, "\\n", "\\n" );
		matchTest( pat, "\\r", "\\r" );
		matchTest( pat, "\\t", "\\t" );
		matchTest( pat, "\\\\", "\\\\" );
		matchFailTest( pat, "\\q" );
		matchTest( pat, "\\x0x", "\\x0x" );
		matchTest( pat, "\\xAx", "\\xAx" );
	}
	
	
	public void testReadUnquotedString()
	{
		readTest( "abc", "abc" );
		readTest( "abc123ABC_", "abc123ABC_" );
		readTest( "abc123ABC_+-*/%^&|!$@.~", "abc123ABC_+-*/%^&|!$@.~" );
	}

	public void testReadQuotedString()
	{
		readTest( "\"abcd\"", "abcd" );
		readTest( "\"ab\\ncd\"", "ab\ncd" );
		readTest( "\"ab\\tcd\"", "ab\tcd" );
		readTest( "\"ab\\rcd\"", "ab\rcd" );
		readTest( "\"ab\\\\cd\"", "ab\\cd" );
		readTest( "\"abc\\\\ndef\"", "abc\\ndef" );
		readTest( "\"ab\\x010axcd\"", "ab\u010acd" );
		readTest( "\"ab\\xffffxcd\"", "ab\uffffcd" );
	}

	public void testReadEmptyString()
	{
		readTest( "\"\"", "" );
	}
	
	public void testReadNull()
	{
		readTest( "`null`", null );
	}

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
	
	public void testReadObject()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		readTest( "{m=test.schemaA : (m A x=0 y=1)}", a );
	}

	public void testReadJavaResource() throws IOException
	{
		DMJavaResource jr = new DMJavaResource( Color.RED );
		readTest( "<<Ja: " + DMIOWriter.stringAsAtom( DMJavaResource.serialise( Color.RED ) ) + ">>", jr );
	}

	public void testReadPyResource() throws IOException
	{
		PyObject pyValue = new PyTuple( new PyInteger( 1 ), new PyInteger( 2 ), new PyInteger( 3 ) );
		DMPyResource pr = new DMPyResource( pyValue );
		readTest( "<<Py: " + DMIOWriter.stringAsAtom( DMPyResource.serialise( pyValue ) ) + ">>", pr );
	}

	public void testReadNestedObject()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		DMObject b = A.newInstance( new Object[] { a, "1" } );
		readTest( "{m=test.schemaA : (m A x=(m A x=0 y=1) y=1)}", b );
	}

	public void testReadObjectInListInObject()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		List<Object> b = Arrays.asList( new Object[] { a, "xyz" } );
		DMObject c = A.newInstance( new Object[] { b, "1" } );
		readTest( "{m=test.schemaA : (m A x=[(m A x=0 y=1) xyz] y=1)}", c );
	}
	
	public void testReadNestedResource() throws IOException
	{
		DMJavaResource jr = new DMJavaResource( Color.RED );
		PyObject pyValue = new PyTuple( new PyInteger( 1 ), new PyInteger( 2 ), new PyInteger( 3 ) );
		DMPyResource pr = new DMPyResource( pyValue );
		List<Object> x = Arrays.asList( new Object[] { jr, pr, "abc" } );
		readTest( "[<<Ja: " + DMIOWriter.stringAsAtom( DMJavaResource.serialise( Color.RED ) ) + ">> <<Py: " + DMIOWriter.stringAsAtom( DMPyResource.serialise( pyValue ) ) + ">> abc]", x );
	}

	
	public void testVersioning()
	{
		DMObject b;
		// Try old schema, v1
		b = B1.newInstance( new Object[] { "0", "1" } );
		readTest( resolver1, "{m=test.schemaB : (m B x=0 y=1)}", b );

		// Try new schema, v1-7
		b = B7.newInstance( new Object[] { "0", "1" } );
		readTest( resolver2, "{m=test.schemaB : (m B x=0 y=1)}", b );
		readTest( resolver2, "{m=test.schemaB<2> : (m B x=0 w=1)}", b );
		readTest( resolver2, "{m=test.schemaB<3> : (m B x=0 w=1)}", b );
		readTest( resolver2, "{m=test.schemaB<4> : (m B x=0 w=1)}", b );
		readTest( resolver2, "{m=test.schemaB<5> : (m B x=0 z=1)}", b );
		readTest( resolver2, "{m=test.schemaB<6> : (m B x=0 z=1)}", b );
		readTest( resolver2, "{m=test.schemaB<7> : (m B x=0 z=1)}", b );
	}
}
