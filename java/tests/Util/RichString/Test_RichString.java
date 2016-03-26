//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.Util.RichString;

import java.util.Arrays;

import junit.framework.TestCase;
import BritefuryJ.Util.RichString.RichString;
import BritefuryJ.Util.RichString.RichStringBuilder;

public class Test_RichString extends TestCase
{
	private class Tag
	{
		String name;
		
		public Tag(String name)
		{
			this.name = name;
		}
		
		
		public boolean equals(Object x)
		{
			if ( this == x )
			{
				return true;
			}
			
			if ( x instanceof Tag )
			{
				return name.equals( ((Tag)x).name );
			}
			
			return false;
		}
	}
	
	
	
	private RichString richStringOf(Object... items)
	{
		RichStringBuilder builder = new RichStringBuilder( Arrays.asList( items ) );
		return builder.richString();
	}
	
	private Tag tag(String name)
	{
		return new Tag( name );
	}
	
	
	
	public void test_equals()
	{
		RichString s = richStringOf( "Hello", tag("abc"), " world" );
		
		assertEquals( richStringOf( "Hello", tag("abc"), " world" ), s );
		assertEquals( richStringOf( "He", "llo", tag("abc"), " world" ), s );
		assertEquals( richStringOf( "Hell", "o", tag("abc"), " world" ), s );
		assertEquals( richStringOf( "Hello", tag("abc"), " ", "world" ), s );
	}

	public void test_indexOf_String()
	{
		RichString s = richStringOf( "Hello", tag("abc"), " world", tag("def"), " the end" );
		
		assertEquals( 0, s.indexOf( "Hello" ) );
		assertEquals( 1, s.indexOf( "ello" ) );
		assertEquals( 1, s.indexOf( "el" ) );
		assertEquals( 7, s.indexOf( "wor" ) );
		assertEquals( 18, s.indexOf( "end" ) );

		assertEquals( 11, s.indexOf( "d" ) );
		assertEquals( 20, s.indexOf( "d", 15 ) );
		assertEquals( 20, s.indexOf( "d", 18 ) );
		assertEquals( -1, s.indexOf( "d", 13, 15 ) );
	}


	public void test_indexOf_Object()
	{
		RichString s = richStringOf( "Hello", tag("abc"), " world", tag("abc"), " the end" );
		
		assertEquals( 5, s.indexOf( tag("abc") ) );
		assertEquals( 12, s.indexOf( tag("abc"), 6 ) );
		assertEquals( -1, s.indexOf( tag("abc"), 6, 9 ) );
	}
	
	
	public void test_split_String()
	{
		RichString s = richStringOf( "Hello", tag("abc"), " world", tag("abc"), " the end" );
		
		assertTrue( Arrays.equals( new RichString[] {
				richStringOf( "He" ), richStringOf(), richStringOf( "o", tag("abc"), " wor" ), richStringOf( "d", tag("abc"), " the end" ) },
				s.split( "l" ) ) );
	}
}
