//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.Parser.Utils;

import tests.Parser.ParserTestCase;
import BritefuryJ.DocModel.DMSchema;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Utils.Tokens;

public class Test_Tokens extends ParserTestCase
{
	protected static DMSchema s;


	static
	{
		s = new DMSchema( "Tokens", "m", "tests.Parser.Utils.Tokens.Tokens" );
	}
	
	
	public void testIdentifier()
	{
		ParserExpression parser = Tokens.identifier;
		matchTestStringAndRichString( parser, "ab", "ab" );
		matchTestStringAndRichString( parser, "ab12", "ab12" );
		matchFailTestStringAndRichString( parser, "12ab" );
		matchTestStringAndRichString( parser, "_ab", "_ab" );
	}

	public void testJavaIdentifier()
	{
		ParserExpression parser = Tokens.javaIdentifier;
		matchTestStringAndRichString( parser, "ab", "ab" );
		matchTestStringAndRichString( parser, "ab12", "ab12" );
		matchFailTestStringAndRichString( parser, "12ab" );
		matchTestStringAndRichString( parser, "_ab", "_ab" );
	}
	

	public void testDecimalInteger()
	{
		ParserExpression parser = Tokens.decimalInteger;
		matchTestStringAndRichString( parser, "123", "123" );
		matchTestStringAndRichString( parser, "-123", "-123" );
	}

	public void testDecimalIntegerNoOctal()
	{
		ParserExpression parser = Tokens.decimalIntegerNoOctal;
		matchTestStringAndRichString( parser, "123", "123" );
		matchTestStringAndRichString( parser, "0", "0" );
		matchSubTestStringAndRichString( parser, "0123", "0", 1 );
	}


	public void testHexadecimalInteger()
	{
		ParserExpression parser = Tokens.hexInteger;
		matchTestStringAndRichString( parser, "0x123", "0x123" );
		matchTestStringAndRichString( parser, "0X123", "0X123" );
		matchTestStringAndRichString( parser, "0x0123456789abcdef", "0x0123456789abcdef" );
		matchTestStringAndRichString( parser, "0x0123456789ABCDEF", "0x0123456789ABCDEF" );
	}

	public void testOctalInteger()
	{
		ParserExpression parser = Tokens.octalInteger;
		matchFailTestStringAndRichString( parser, "12" );
		matchFailTestStringAndRichString( parser, "0" );
		matchTestStringAndRichString( parser, "01", "01" );
		matchTestStringAndRichString( parser, "01234567", "01234567" );
		matchSubTestStringAndRichString( parser, "0123456789", "01234567", 8 );
	}

	public void testFloatingPoint()
	{
		ParserExpression parser = Tokens.floatingPoint;
		matchTestStringAndRichString( parser, "3.14", "3.14" );
		matchTestStringAndRichString( parser, "-3.14", "-3.14" );
		matchTestStringAndRichString( parser, "3.", "3." );
		matchTestStringAndRichString( parser, "-3.", "-3." );
		matchTestStringAndRichString( parser, ".14", ".14" );
		matchTestStringAndRichString( parser, "-.14", "-.14" );

		matchTestStringAndRichString( parser, "3.14e5", "3.14e5" );
		matchTestStringAndRichString( parser, "3.14e-5", "3.14e-5" );
		matchTestStringAndRichString( parser, "-3.14e5", "-3.14e5" );
		matchTestStringAndRichString( parser, "-3.14e-5", "-3.14e-5" );
		matchTestStringAndRichString( parser, "3.e5", "3.e5" );
		matchTestStringAndRichString( parser, "3.e-5", "3.e-5" );
		matchTestStringAndRichString( parser, "-3.e5", "-3.e5" );
		matchTestStringAndRichString( parser, "-3.e-5", "-3.e-5" );
		matchTestStringAndRichString( parser, ".14e5", ".14e5" );
		matchTestStringAndRichString( parser, ".14e-5", ".14e-5" );
		matchTestStringAndRichString( parser, "-.14e5", "-.14e5" );
		matchTestStringAndRichString( parser, "-.14e-5", "-.14e-5" );
	}

	
	
	public void testJavaCharacterLiteral()
	{
		ParserExpression parser = Tokens.javaCharacterLiteral;
		matchTestStringAndRichString( parser, "'a'", "'a'" );
		matchFailTestStringAndRichString( parser, "'a" );
		matchTestStringAndRichString( parser, "'\\b'", "'\\b'" );
		matchTestStringAndRichString( parser, "'\\t'", "'\\t'" );
		matchTestStringAndRichString( parser, "'\\n'", "'\\n'" );
		matchTestStringAndRichString( parser, "'\\f'", "'\\f'" );
		matchTestStringAndRichString( parser, "'\\r'", "'\\r'" );
		matchTestStringAndRichString( parser, "'\\\"'", "'\\\"'" );
		matchTestStringAndRichString( parser, "'\\''", "'\\''" );
		matchTestStringAndRichString( parser, "'\\\\'", "'\\\\'" );
		matchFailTestStringAndRichString( parser, "'\\a'" );
		matchTestStringAndRichString( parser, "'\\0'", "'\\0'" );
		matchTestStringAndRichString( parser, "'\\1'", "'\\1'" );
		matchTestStringAndRichString( parser, "'\\2'", "'\\2'" );
		matchTestStringAndRichString( parser, "'\\3'", "'\\3'" );
		matchTestStringAndRichString( parser, "'\\4'", "'\\4'" );
		matchTestStringAndRichString( parser, "'\\5'", "'\\5'" );
		matchTestStringAndRichString( parser, "'\\6'", "'\\6'" );
		matchTestStringAndRichString( parser, "'\\7'", "'\\7'" );
		matchTestStringAndRichString( parser, "'\\77'", "'\\77'" );
		matchFailTestStringAndRichString( parser, "'\\777'" );
		matchFailTestStringAndRichString( parser, "'\\477'" );
		matchTestStringAndRichString( parser, "'\\377'", "'\\377'" );
		matchTestStringAndRichString( parser, "'\\u0123'", "'\\u0123'" );
		matchTestStringAndRichString( parser, "'\\u01aF'", "'\\u01aF'" );
		matchFailTestStringAndRichString( parser, "'\\u01a'" );
		matchFailTestStringAndRichString( parser, "'\\u01a043'" );
	}

	
	public void testSingleQuotedString()
	{
		ParserExpression parser = Tokens.singleQuotedString;
		matchTestStringAndRichString( parser, "'abc'", "'abc'" );
		matchTestStringAndRichString( parser, "'ab\\'c'", "'ab\\'c'" );
		matchSubTestStringAndRichString( parser, "'abc'113", "'abc'", 5 );
	}


	public void testDoubleQuotedString()
	{
		ParserExpression parser = Tokens.doubleQuotedString;
		matchTestStringAndRichString( parser, "\"abc\"", "\"abc\"" );
		matchTestStringAndRichString( parser, "\"ab\\\"c\"", "\"ab\\\"c\"" );
		matchSubTestStringAndRichString( parser, "\"abc\"113\"", "\"abc\"", 5 );
	}


	public void testQuotedString()
	{
		ParserExpression parser = Tokens.quotedString;
		matchTestStringAndRichString( parser, "'abc'", "'abc'" );
		matchTestStringAndRichString( parser, "'ab\\'c'", "'ab\\'c'" );
		matchSubTestStringAndRichString( parser, "'abc'113", "'abc'", 5 );
		matchTestStringAndRichString( parser, "\"abc\"", "\"abc\"" );
		matchTestStringAndRichString( parser, "\"ab\\\"c\"", "\"ab\\\"c\"" );
		matchSubTestStringAndRichString( parser, "\"abc\"113\"", "\"abc\"", 5 );
	}


	public void testUnicodeString()
	{
		ParserExpression parser = Tokens.unicodeString;
		matchTestStringAndRichString( parser, "u'abc'", "u'abc'" );
		matchTestStringAndRichString( parser, "u'ab\\'c'", "u'ab\\'c'" );
		matchSubTestStringAndRichString( parser, "u'abc'113", "u'abc'", 6 );
		matchTestStringAndRichString( parser, "u\"abc\"", "u\"abc\"" );
		matchTestStringAndRichString( parser, "u\"ab\\\"c\"", "u\"ab\\\"c\"" );
		matchSubTestStringAndRichString( parser, "u\"abc\"113\"", "u\"abc\"", 6 );
	}
	
	
	public void testJavaStringLiteral()
	{
		ParserExpression parser = Tokens.javaStringLiteral;
		matchTestStringAndRichString( parser, "\"abc\"", "\"abc\"" );
		matchSubTestStringAndRichString( parser, "\"abc\"q", "\"abc\"", 5 );
		matchTestStringAndRichString( parser, "\"abc\\b\\t\\n\\f\\r\\\"\\\'\\\\xyz\"", "\"abc\\b\\t\\n\\f\\r\\\"\\\'\\\\xyz\"" );
		matchFailTestStringAndRichString( parser, "\"abc\\a\\t\\n\\f\\r\\\"\\\'\\\\xyz\"" );
		matchTestStringAndRichString( parser, "\"abc\\0\\1\\2\\3\\4\\5\\6\\7xyz\"", "\"abc\\0\\1\\2\\3\\4\\5\\6\\7xyz\"" );
		matchTestStringAndRichString( parser, "\"abc\\347xyz\"", "\"abc\\347xyz\"" );
		matchTestStringAndRichString( parser, "\"abc\\u0123xyz\"", "\"abc\\u0123xyz\"" );
		matchTestStringAndRichString( parser, "\"abc\\u01aFxyz\"", "\"abc\\u01aFxyz\"" );
		matchFailTestStringAndRichString( parser, "\"abc\\u347xyz\"" );
	}


}
