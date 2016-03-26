//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.Editor.RichText;

import BritefuryJ.Editor.RichText.Attrs.RichTextAttributes;
import BritefuryJ.Editor.RichText.SupportForUnitTests;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FlattenTest extends TestCase
{
	private static class Tester
	{
		private Object in[];

		private Tester(Object in[])
		{
			this.in = in;
		}


		void becomes(Object ...exp)
		{
			List<Object> input = Arrays.asList( in );
			List<Object> expected = Arrays.asList( exp );
			List<Object> out = SupportForUnitTests.flattenParagraphs( input );
			assertEquals( expected, out );
		}

		void raises(Class<? extends Exception> exceptionClass)
		{
			List<Object> input = Arrays.asList( in );

			try
			{
				SupportForUnitTests.flattenParagraphs( input );
			}
			catch (Throwable t)
			{
				assertTrue( exceptionClass.isInstance( t ) );
				return;
			}
			fail( "Expected an exception of type " + exceptionClass );
		}
	}

	private static Tester flattenTest(Object ...in)
	{
		return new Tester( in );
	}

	private static Object pstart(RichTextAttributes attrs)
	{
		return SupportForUnitTests.tagPStart( attrs );
	}

	private static Object sstart()
	{
		return sstart( new HashMap<Object, Object>() );
	}

	private static Object sstart(Map<Object, Object> attrs)
	{
		return SupportForUnitTests.tagSStart(RichTextAttributes.fromValues(attrs, null));
	}

	private static Object send()
	{
		return SupportForUnitTests.tagSEnd();
	}


	private static Object span(Object ...in)
	{
		return SupportForUnitTests.span( in, new RichTextAttributes() );
	}

	private static Object iembed(Object value)
	{
		return SupportForUnitTests.iembed( value );
	}

	private static Object p(Object ...in)
	{
		return SupportForUnitTests.p( in, new RichTextAttributes() );
	}

	private static Object pembed(Object value)
	{
		return SupportForUnitTests.pembed( value );
	}

	private static Object newline()
	{
		return SupportForUnitTests.newline();
	}



	//
	// THE TESTS
	//


	public void test_newlineBeforeParaEmbed_atStart()
	{
		// User inserted newline before paragraph embed at the start of the document
		flattenTest(  "\n", pembed( 5 ) ).becomes( pstart( null ), pembed( 5 ) );
	}

	public void test_newlineBeforeParaEmbed()
	{
		// Newline before paragraph embed, but after text.
		// Suppress the newline
		flattenTest(  "Test\n", pembed( 5 ) ).becomes( span( "Test" ), pembed( 5 ) );
	}



	public void test_paragraphBoundary_endStart()
	{
		// Paragraph boundary; text content followed by newline followed by paragraph start tag or paragraph
		flattenTest( "Hello\n", pstart( null ), "World\n" ).becomes( span( "Hello" ), pstart( null ), span( "World" ) );
		flattenTest( "Hello\n", p( "World" ) ).becomes( span( "Hello" ), p( "World" ) );
	}

	public void test_newlineSplit()
	{
		// User inserted newline into text content to split paragraph in two
		flattenTest( "Hello\nWorld\n" ).becomes( span( "Hello" ), pstart( null ), span( "World" ) );
		flattenTest( "Hello\n", iembed( 5 ), "\n" ).becomes( span( "Hello" ), pstart( null ), span( iembed( 5 ) ) );
		flattenTest( "Hello\n", sstart(), "World\n" ).becomes( span( "Hello" ), pstart( null ), span( "World" ) );
		flattenTest( sstart(), "Hello\n", send(), "World\n" ).becomes( span( "Hello" ), pstart( null ), span( "World" ) );
		flattenTest( "Hello\n", sstart(), span( "World"), "\n" ).becomes( span( "Hello" ), pstart( null ), span( "World" ) );
		flattenTest( "Hello\n", newline() ).becomes( span( "Hello" ), pstart( null ) );
		flattenTest( "Hello\n", newline(), "World\n" ).becomes( span( "Hello" ), pstart( null ), pstart( null ), span( "World" ) );
	}

	public void test_newlineAtEnd()
	{
		// Newline may appear at the end of the document
		flattenTest( pstart( null ), "Test\n" ).becomes( pstart( null ), span( "Test" ) );
	}



	public void test_removeUnnecessaryPStart()
	{
		flattenTest( pstart( null ), pembed( 5 ) ).becomes( pembed( 5 ) );
		flattenTest( pstart( null ), p( "Test" ) ).becomes( p( "Test" ) );
	}

	public void test_passPStart()
	{
		// A paragraph start tag followed by text content should pass through
		flattenTest( pstart( null ), "Test" ).becomes( pstart( null ), span( "Test" ) );
		flattenTest( pstart( null ), iembed( 5 ) ).becomes( pstart( null ), span( iembed( 5 ) ) );
		flattenTest( pstart( null ), sstart(), "Test", send() ).becomes( pstart( null ), span( "Test" ) );
		flattenTest( pstart( null ), span( "Test" ) ).becomes( pstart( null ), span( "Test" ) );
		flattenTest( pstart( null ), "\n" ).becomes( pstart( null ) );
	}



	public void test_pembedThenNewline()
	{
		// User has inserted a newline after a paragraph embed
		flattenTest( p( "Test" ), pembed( 5 ), "\n" ).becomes( p( "Test" ), pembed( 5 ), pstart( null ) );
	}



	public void test_textThenPStart()
	{
		// Text content then a paragraph start with NO newline
		// User has deleted newline
		flattenTest( "Hello", pstart( null ), "World" ).becomes( span( "Hello" ), span( "World" ) );
		flattenTest( iembed( 5 ), pstart( null ), "World" ).becomes( span( iembed( 5 ) ), span( "World" ) );
		flattenTest( span( "Hello" ), pstart( null ), "World" ).becomes( span( "Hello" ), span( "World" ) );
		flattenTest( sstart(), pstart( null ), "World", send() ).becomes( span( "World" ) );
		flattenTest( sstart(), "Hello", send(), pstart( null ), "World" ).becomes( span( "Hello" ), span( "World" ) );
	}



	public void test_string()
	{
		// Pass through text
		flattenTest( "Test\n" ).becomes( span( "Test" ) );
		flattenTest( "Test" ).becomes( span( "Test" ) );
	}



	public void test_iembed()
	{
		// Pass through inline embed
		flattenTest( iembed(  5 ), "\n" ).becomes( span( iembed(  5 ) ) );
		flattenTest( iembed(  5 ) ).becomes( span( iembed(  5 ) ) );
	}



	public void test_sstart_and_send()
	{
		// Span start and end tags become span
		flattenTest( sstart(), "Test", send() ).becomes( span( "Test" ) );
		flattenTest( iembed( 5 ) ).becomes( span( iembed(  5 ) ) );
	}



	public void test_pstart()
	{
		// Paragraph start tag passes through
		flattenTest( pstart( null ), "Test" ).becomes( pstart( null ), span( "Test" ) );
	}



	public void test_span()
	{
		// Span passes through
		flattenTest( span( "Test" ) ).becomes( span( "Test" ) );
	}
}
