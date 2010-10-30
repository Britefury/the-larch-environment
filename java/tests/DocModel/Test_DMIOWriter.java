//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import BritefuryJ.DocModel.DMIOWriter;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMSchema;
import BritefuryJ.DocModel.Resource.DMJavaResource;
import BritefuryJ.DocModel.Resource.DMPyResource;

public class Test_DMIOWriter extends TestCase
{
	protected static class TestWriter extends DMIOWriter
	{
		protected static void test_escape(StringBuilder builder, char c)
		{
			escape( builder, c );
		}
		
		protected static String test_quoteString(String s)
		{
			return quoteString( s );
		}
	}
	
	private static DMSchema schema, module2;
	private static DMObjectClass A, A2;
	
	
	static
	{
		schema = new DMSchema( "schema", "m", "test.schema" );
		module2 = new DMSchema( "module2", "m", "test.module2" );
		A = schema.newClass( "A", new String[] { "x", "y" } );
		A2 = module2.newClass( "A2", new String[] { "x", "y" } );
	}
	
	
	
	
	public void escapeTest(char input, String expected)
	{
		StringBuilder builder = new StringBuilder();
		TestWriter.test_escape( builder, input );
		assertEquals( builder.toString(), expected );
	}


	public void writeTest(Object input, String expected)
	{
		String res = null;
		try
		{
			res = DMIOWriter.writeAsString( input );
		}
		catch (DMIOWriter.InvalidDataTypeException e)
		{
			System.out.println( "PARSE FAILURE" );
			fail();
		}
		
		if ( !res.equals( expected ) )
		{
			System.out.println( "VALUES ARE NOT THE SAME" );
			System.out.println( "EXPECTED:" );
			System.out.println( expected );
			System.out.println( "RESULT:" );
			System.out.println( res );
		}
		
		assertTrue( res.equals( expected ) );
	}
	
	


	
	public void testEscape()
	{
		escapeTest( '\n', "\\n" );
		escapeTest( '\r', "\\r" );
		escapeTest( '\t', "\\t" );
		escapeTest( '\\', "\\\\" );
		escapeTest( (char)8, "\\x8x" );
		escapeTest( (char)0x1258, "\\x1258x" );
	}
	
	public void testQuoteString()
	{
		assertEquals( TestWriter.test_quoteString( "a" ), "\"a\"" );
		assertEquals( TestWriter.test_quoteString( "a b" ), "\"a b\"" );
		assertEquals( TestWriter.test_quoteString( "abcdefgh" ), "\"abcdefgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcdefgh`" ), "\"abcdefgh`\"" );
		assertEquals( TestWriter.test_quoteString( "\nabcdefgh" ), "\"\\nabcdefgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcd\nefgh" ), "\"abcd\\nefgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcdefgh\n" ), "\"abcdefgh\\n\"" );
		assertEquals( TestWriter.test_quoteString( "\nab\ncd\nef\ngh\n" ), "\"\\nab\\ncd\\nef\\ngh\\n\"" );
		assertEquals( TestWriter.test_quoteString( "ab\ncd\nef\ngh\n" ), "\"ab\\ncd\\nef\\ngh\\n\"" );
		assertEquals( TestWriter.test_quoteString( "\nab\ncd\nef\ngh" ), "\"\\nab\\ncd\\nef\\ngh\"" );
		assertEquals( TestWriter.test_quoteString( "ab\ncd\nef\ngh" ), "\"ab\\ncd\\nef\\ngh\"" );
		assertEquals( TestWriter.test_quoteString( "abcd\refgh" ), "\"abcd\\refgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcd\tefgh" ), "\"abcd\\tefgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcd\\efgh" ), "\"abcd\\\\efgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcd" + new Character( (char)0x1258 ).toString() + "efgh" ), "\"abcd\\x1258xefgh\"" );
	}
	
	
	public void testWriteString()
	{
		writeTest( "a", "a" );
		writeTest( "a b", "\"a b\"" );
		writeTest( "abcdefgh", "abcdefgh" );
		writeTest( "abcdefgh()", "\"abcdefgh()\"" );
		writeTest( "\nabcdefgh", "\"\\nabcdefgh\"" );
		writeTest( "abcd\nefgh", "\"abcd\\nefgh\"" );
		writeTest( "abcdefgh\n", "\"abcdefgh\\n\"" );
		writeTest( "\nab\ncd\nef\ngh\n", "\"\\nab\\ncd\\nef\\ngh\\n\"" );
		writeTest( "ab\ncd\nef\ngh\n", "\"ab\\ncd\\nef\\ngh\\n\"" );
		writeTest( "\nab\ncd\nef\ngh", "\"\\nab\\ncd\\nef\\ngh\"" );
		writeTest( "ab\ncd\nef\ngh", "\"ab\\ncd\\nef\\ngh\"" );
		writeTest( "abcd\refgh", "\"abcd\\refgh\"" );
		writeTest( "abcd\tefgh", "\"abcd\\tefgh\"" );
		writeTest( "abcd\\efgh", "\"abcd\\\\efgh\"" );
		writeTest( "abc\\ndef", "\"abc\\\\ndef\"" );
		writeTest( "abcd" + new Character( (char)0x1258 ).toString() + "efgh", "\"abcd\\x1258xefgh\"" );
	}
	
	
	public void testWriteNull()
	{
		writeTest( null, "`null`" );
	}
	
	
	public void testWriteEmptyString()
	{
		Object[] xs = { "" };
		writeTest( Arrays.asList( xs ), "[\"\"]" );
	}

	
	public void testWriteEmptyList()
	{
		Object[] xs = { };
		writeTest( Arrays.asList( xs ), "[]" );
	}

	
	public void testWriteList()
	{
		Object[] xs = { "a", "b", "c" };
		writeTest( Arrays.asList( xs ), "[a b c]" );
	}
	
	
	public void testWriteNestedList()
	{
		Object[] a = {};
		Object[] b = { "a", "b", "c" };
		Object[] xs = { "x", "y", Arrays.asList( a ), "z", Arrays.asList( b ), "w" };
		writeTest( Arrays.asList( xs ), "[x y [] z [a b c] w]" );
	}
	
	
	
	public void testWriteObject()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		writeTest( a, "{m=test.schema : (m A x=0 y=1)}" );
		DMObject a1 = A.newInstance( new Object[] { "0" } );
		writeTest( a1, "{m=test.schema : (m A x=0)}" );
	}
	
	
	
	public void testWriteJavaResource() throws IOException
	{
		DMJavaResource jr = new DMJavaResource( Color.RED );
		writeTest( jr, "<<Ja: " + DMIOWriter.stringAsAtom( DMJavaResource.serialise( Color.RED ) ) + ">>" );
	}


	public void testWritePyResource() throws IOException
	{
		PyObject pyValue = new PyTuple( new PyInteger( 1 ), new PyInteger( 2 ), new PyInteger( 3 ) );
		DMPyResource pr = new DMPyResource( pyValue );
		writeTest( pr, "<<Py: " + DMIOWriter.stringAsAtom( DMPyResource.serialise( pyValue ) ) + ">>" );
	}


	public void testWriteNestedObject()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		DMObject b = A.newInstance( new Object[] { a, "2" } );
		writeTest( b, "{m=test.schema : (m A x=(m A x=0 y=1) y=2)}" );
	}

	public void testWriteObjectInListInObject()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		List<Object> b = Arrays.asList( new Object[] { a, "abc" } );
		DMObject c = A.newInstance( new Object[] { b, "2" } );
		writeTest( c, "{m=test.schema : (m A x=[(m A x=0 y=1) abc] y=2)}" );
	}


	public void testWriteObject_moduleNameCollision()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		DMObject b = A2.newInstance( new Object[] { "0", "1" } );
		List<Object> l = Arrays.asList( new Object[] { a, b } );
		writeTest( l, "{m=test.schema m2=test.module2 : [(m A x=0 y=1) (m2 A2 x=0 y=1)]}" );
	}


	public void testWriteNestedResource() throws IOException
	{
		DMJavaResource jr = new DMJavaResource( Color.RED );
		PyObject pyValue = new PyTuple( new PyInteger( 1 ), new PyInteger( 2 ), new PyInteger( 3 ) );
		DMPyResource pr = new DMPyResource( pyValue );
		List<Object> x = Arrays.asList( new Object[] { jr, pr, "abc" } );
		writeTest( x, "[<<Ja: " + DMIOWriter.stringAsAtom( DMJavaResource.serialise( Color.RED ) ) + ">> <<Py: " + DMIOWriter.stringAsAtom( DMPyResource.serialise( pyValue ) ) + ">> abc]" );
	}
}
