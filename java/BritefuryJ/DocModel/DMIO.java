//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel;

import java.util.regex.Pattern;

public class DMIO
{
	public static final String unquotedStringPunctuationChars = "+-*/%^&|!$.~@";
	public static final String quotedStringPunctuationChars = unquotedStringPunctuationChars + ",=<>[]{}~'()`# ";

	public static final Pattern unquotedString = Pattern.compile( "[0-9A-Za-z_" + Pattern.quote( unquotedStringPunctuationChars ) + "]+" );
}
