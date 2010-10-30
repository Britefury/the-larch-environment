//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel;

import java.util.BitSet;

public class DMIO
{
	public static final String unquotedStringPunctuationChars = "+-*/%^&|!$.~@";
	public static final String quotedStringPunctuationChars = unquotedStringPunctuationChars + ",=<>[]{}~'()`# ";

	public static final BitSet unquotedCharBits = new BitSet();
	public static final BitSet quotedCharBits = new BitSet();
	
	
	static
	{
		for (int i = (int)'0'; i <= (int)'9'; i++)
		{
			unquotedCharBits.set( i );
			quotedCharBits.set( i );
		}

		for (int i = (int)'A'; i <= (int)'Z'; i++)
		{
			unquotedCharBits.set( i );
			quotedCharBits.set( i );
		}

		for (int i = (int)'a'; i <= (int)'z'; i++)
		{
			unquotedCharBits.set( i );
			quotedCharBits.set( i );
		}
		
		unquotedCharBits.set( (int)'_' );
		quotedCharBits.set( (int)'_' );

		for (int i = 0; i < unquotedStringPunctuationChars.length(); i++)
		{
			unquotedCharBits.set( (int)unquotedStringPunctuationChars.charAt( i ) );
		}
		
		for (int i = 0; i < quotedStringPunctuationChars.length(); i++)
		{
			quotedCharBits.set( (int)quotedStringPunctuationChars.charAt( i ) );
		}
	}
}
