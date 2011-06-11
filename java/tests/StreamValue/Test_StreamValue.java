//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.StreamValue;

import java.util.Arrays;

import junit.framework.TestCase;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;

public class Test_StreamValue extends TestCase
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
	
	
	
	private StreamValue streamOf(Object... items)
	{
		StreamValueBuilder builder = new StreamValueBuilder( Arrays.asList( items ) );
		return builder.stream();
	}
	
	private Tag tag(String name)
	{
		return new Tag( name );
	}
	
	
	
	public void test_equals()
	{
		StreamValue s = streamOf( "Hello", tag("abc"), " world" );
		
		assertEquals( streamOf( "Hello", tag("abc"), " world" ), s );
		assertEquals( streamOf( "He", "llo", tag("abc"), " world" ), s );
		assertEquals( streamOf( "Hell", "o", tag("abc"), " world" ), s );
		assertEquals( streamOf( "Hello", tag("abc"), " ", "world" ), s );
	}

	public void test_indexOf_String()
	{
		StreamValue s = streamOf( "Hello", tag("abc"), " world", tag("def"), " the end" );
		
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
		StreamValue s = streamOf( "Hello", tag("abc"), " world", tag("abc"), " the end" );
		
		assertEquals( 5, s.indexOf( tag("abc") ) );
		assertEquals( 12, s.indexOf( tag("abc"), 6 ) );
		assertEquals( -1, s.indexOf( tag("abc"), 6, 9 ) );
	}
	
	
	public void test_split_String()
	{
		StreamValue s = streamOf( "Hello", tag("abc"), " world", tag("abc"), " the end" );
		
		assertTrue( Arrays.equals( new StreamValue[] {
				streamOf( "He" ), streamOf(), streamOf( "o", tag("abc"), " wor" ), streamOf( "d", tag("abc"), " the end" ) },
				s.split( "l" ) ) );
	}
}
