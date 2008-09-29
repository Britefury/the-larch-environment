//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils;

import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.RegEx;

public class Tokens
{
	public static ParserExpression identifier = new RegEx( "[A-Za-z_][A-Za-z0-9_]*" );
	public static ParserExpression singleQuotedString = new RegEx( "\'(?:[^\'\\n\\r\\\\]|(?:\'\')|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\'" );
	public static ParserExpression doubleQuotedString = new RegEx( "\"(?:[^\"\\n\\r\\\\]|(?:\"\")|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\"" );
	public static ParserExpression quotedString = new RegEx( "(?:\"(?:[^\"\\n\\r\\\\]|(?:\"\")|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\")|(?:\'(?:[^\'\\n\\r\\\\]|(?:\'\')|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\')" );
	public static ParserExpression unicodeString = new RegEx( "(u|U)((?:\"(?:[^\"\\n\\r\\\\]|(?:\"\")|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\")|(?:\'(?:[^\'\\n\\r\\\\]|(?:\'\')|(?:\\\\x[0-9a-fA-F]+)|(?:\\\\.))*\'))" );
	public static ParserExpression decimalInteger = new RegEx( "[\\-]?[0-9]+" );
	public static ParserExpression hexInteger = new RegEx( "0x[0-9A-Fa-f]+" );
	public static ParserExpression integer = decimalInteger.__or__( hexInteger );
	public static ParserExpression floatingPoint = new RegEx( "[\\-]?(([0-9]+\\.[0-9]*)|(\\.[0-9]+))(e[\\-]?[0-9]+)?" );
}
