//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser.Utils;

import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Utils.Tokens;
import tests.Parser.ParserTestCase;

public class Test_Tokens extends ParserTestCase
{
	public void testIdentifier()
	{
		ParserExpression parser = Tokens.identifier;
		matchTest( parser, "ab", "ab" );
		matchTest( parser, "ab12", "ab12" );
		matchFailTest( parser, "12ab" );
		matchTest( parser, "_ab", "_ab" );
	}


	public void testSingleQuotedString()
	{
		ParserExpression parser = Tokens.singleQuotedString;
		matchTest( parser, "'abc'", "'abc'" );
		matchTest( parser, "'ab\\'c'", "'ab\\'c'" );
		matchSubTest( parser, "'abc'113", "'abc'", 5 );
	}


	public void testDoubleQuotedString()
	{
		ParserExpression parser = Tokens.doubleQuotedString;
		matchTest( parser, "\"abc\"", "\"abc\"" );
		matchTest( parser, "\"ab\\\"c\"", "\"ab\\\"c\"" );
		matchSubTest( parser, "\"abc\"113\"", "\"abc\"", 5 );
	}


	public void testQuotedString()
	{
		ParserExpression parser = Tokens.quotedString;
		matchTest( parser, "'abc'", "'abc'" );
		matchTest( parser, "'ab\\'c'", "'ab\\'c'" );
		matchSubTest( parser, "'abc'113", "'abc'", 5 );
		matchTest( parser, "\"abc\"", "\"abc\"" );
		matchTest( parser, "\"ab\\\"c\"", "\"ab\\\"c\"" );
		matchSubTest( parser, "\"abc\"113\"", "\"abc\"", 5 );
	}


	public void testUnicodeString()
	{
		ParserExpression parser = Tokens.unicodeString;
		matchTest( parser, "u'abc'", "u'abc'" );
		matchTest( parser, "u'ab\\'c'", "u'ab\\'c'" );
		matchSubTest( parser, "u'abc'113", "u'abc'", 6 );
		matchTest( parser, "u\"abc\"", "u\"abc\"" );
		matchTest( parser, "u\"ab\\\"c\"", "u\"ab\\\"c\"" );
		matchSubTest( parser, "u\"abc\"113\"", "u\"abc\"", 6 );
	}


	public void testDecimalInteger()
	{
		ParserExpression parser = Tokens.decimalInteger;
		matchTest( parser, "123", "123" );
		matchTest( parser, "-123", "-123" );
	}


	public void testHexadecimalInteger()
	{
		ParserExpression parser = Tokens.hexInteger;
		matchTest( parser, "0x123", "0x123" );
		matchTest( parser, "0x0123456789abcdef", "0x0123456789abcdef" );
		matchTest( parser, "0x0123456789ABCDEF", "0x0123456789ABCDEF" );
	}



	public void testFloatingPoint()
	{
		ParserExpression parser = Tokens.floatingPoint;
		matchTest( parser, "3.14", "3.14" );
		matchTest( parser, "-3.14", "-3.14" );
		matchTest( parser, "3.", "3." );
		matchTest( parser, "-3.", "-3." );
		matchTest( parser, ".14", ".14" );
		matchTest( parser, "-.14", "-.14" );

		matchTest( parser, "3.14e5", "3.14e5" );
		matchTest( parser, "3.14e-5", "3.14e-5" );
		matchTest( parser, "-3.14e5", "-3.14e5" );
		matchTest( parser, "-3.14e-5", "-3.14e-5" );
		matchTest( parser, "3.e5", "3.e5" );
		matchTest( parser, "3.e-5", "3.e-5" );
		matchTest( parser, "-3.e5", "-3.e5" );
		matchTest( parser, "-3.e-5", "-3.e-5" );
		matchTest( parser, ".14e5", ".14e5" );
		matchTest( parser, ".14e-5", ".14e-5" );
		matchTest( parser, "-.14e5", "-.14e5" );
		matchTest( parser, "-.14e-5", "-.14e-5" );
	}
}
