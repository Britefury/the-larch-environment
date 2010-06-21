//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser.Utils;

import tests.Parser.ParserTestCase;
import BritefuryJ.DocModel.DMSchema;
import BritefuryJ.DocModel.DMSchemaResolver;
import BritefuryJ.DocModel.DMSchema.InvalidSchemaNameException;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Utils.Tokens;

public class Test_Tokens extends ParserTestCase
{
	protected DMSchema s;
	protected DMSchemaResolver resolver = new DMSchemaResolver()
	{
		public DMSchema getSchema(String location)
		{
			return location.equals( "Tests.Parser.Tokens" )  ? s :  null;
		}
	};


	protected DMSchemaResolver getModuleResolver()
	{
		return resolver;
	}
	

	
	public void setUp()
	{
		try
		{
			s = new DMSchema( "Tokens", "m", "Tests.Parser.Tokens" );
		}
		catch (InvalidSchemaNameException e)
		{
			throw new RuntimeException( e.toString() );
		}
	}
	
	public void tearDown()
	{
		s = null;
	}

	
	public void testIdentifier()
	{
		ParserExpression parser = Tokens.identifier;
		matchTestStringAndStream( parser, "ab", "ab" );
		matchTestStringAndStream( parser, "ab12", "ab12" );
		matchFailTestStringAndStream( parser, "12ab" );
		matchTestStringAndStream( parser, "_ab", "_ab" );
	}

	public void testJavaIdentifier()
	{
		ParserExpression parser = Tokens.javaIdentifier;
		matchTestStringAndStream( parser, "ab", "ab" );
		matchTestStringAndStream( parser, "ab12", "ab12" );
		matchFailTestStringAndStream( parser, "12ab" );
		matchTestStringAndStream( parser, "_ab", "_ab" );
	}
	

	public void testDecimalInteger()
	{
		ParserExpression parser = Tokens.decimalInteger;
		matchTestStringAndStream( parser, "123", "123" );
		matchTestStringAndStream( parser, "-123", "-123" );
	}

	public void testDecimalIntegerNoOctal()
	{
		ParserExpression parser = Tokens.decimalIntegerNoOctal;
		matchTestStringAndStream( parser, "123", "123" );
		matchTestStringAndStream( parser, "0", "0" );
		matchSubTestStringAndStream( parser, "0123", "0", 1 );
	}


	public void testHexadecimalInteger()
	{
		ParserExpression parser = Tokens.hexInteger;
		matchTestStringAndStream( parser, "0x123", "0x123" );
		matchTestStringAndStream( parser, "0X123", "0X123" );
		matchTestStringAndStream( parser, "0x0123456789abcdef", "0x0123456789abcdef" );
		matchTestStringAndStream( parser, "0x0123456789ABCDEF", "0x0123456789ABCDEF" );
	}

	public void testOctalInteger()
	{
		ParserExpression parser = Tokens.octalInteger;
		matchFailTestStringAndStream( parser, "12" );
		matchFailTestStringAndStream( parser, "0" );
		matchTestStringAndStream( parser, "01", "01" );
		matchTestStringAndStream( parser, "01234567", "01234567" );
		matchSubTestStringAndStream( parser, "0123456789", "01234567", 8 );
	}

	public void testFloatingPoint()
	{
		ParserExpression parser = Tokens.floatingPoint;
		matchTestStringAndStream( parser, "3.14", "3.14" );
		matchTestStringAndStream( parser, "-3.14", "-3.14" );
		matchTestStringAndStream( parser, "3.", "3." );
		matchTestStringAndStream( parser, "-3.", "-3." );
		matchTestStringAndStream( parser, ".14", ".14" );
		matchTestStringAndStream( parser, "-.14", "-.14" );

		matchTestStringAndStream( parser, "3.14e5", "3.14e5" );
		matchTestStringAndStream( parser, "3.14e-5", "3.14e-5" );
		matchTestStringAndStream( parser, "-3.14e5", "-3.14e5" );
		matchTestStringAndStream( parser, "-3.14e-5", "-3.14e-5" );
		matchTestStringAndStream( parser, "3.e5", "3.e5" );
		matchTestStringAndStream( parser, "3.e-5", "3.e-5" );
		matchTestStringAndStream( parser, "-3.e5", "-3.e5" );
		matchTestStringAndStream( parser, "-3.e-5", "-3.e-5" );
		matchTestStringAndStream( parser, ".14e5", ".14e5" );
		matchTestStringAndStream( parser, ".14e-5", ".14e-5" );
		matchTestStringAndStream( parser, "-.14e5", "-.14e5" );
		matchTestStringAndStream( parser, "-.14e-5", "-.14e-5" );
	}

	
	
	public void testJavaCharacterLiteral()
	{
		ParserExpression parser = Tokens.javaCharacterLiteral;
		matchTestStringAndStream( parser, "'a'", "'a'" );
		matchFailTestStringAndStream( parser, "'a" );
		matchTestStringAndStream( parser, "'\\b'", "'\\b'" );
		matchTestStringAndStream( parser, "'\\t'", "'\\t'" );
		matchTestStringAndStream( parser, "'\\n'", "'\\n'" );
		matchTestStringAndStream( parser, "'\\f'", "'\\f'" );
		matchTestStringAndStream( parser, "'\\r'", "'\\r'" );
		matchTestStringAndStream( parser, "'\\\"'", "'\\\"'" );
		matchTestStringAndStream( parser, "'\\''", "'\\''" );
		matchTestStringAndStream( parser, "'\\\\'", "'\\\\'" );
		matchFailTestStringAndStream( parser, "'\\a'" );
		matchTestStringAndStream( parser, "'\\0'", "'\\0'" );
		matchTestStringAndStream( parser, "'\\1'", "'\\1'" );
		matchTestStringAndStream( parser, "'\\2'", "'\\2'" );
		matchTestStringAndStream( parser, "'\\3'", "'\\3'" );
		matchTestStringAndStream( parser, "'\\4'", "'\\4'" );
		matchTestStringAndStream( parser, "'\\5'", "'\\5'" );
		matchTestStringAndStream( parser, "'\\6'", "'\\6'" );
		matchTestStringAndStream( parser, "'\\7'", "'\\7'" );
		matchTestStringAndStream( parser, "'\\77'", "'\\77'" );
		matchFailTestStringAndStream( parser, "'\\777'" );
		matchFailTestStringAndStream( parser, "'\\477'" );
		matchTestStringAndStream( parser, "'\\377'", "'\\377'" );
		matchTestStringAndStream( parser, "'\\u0123'", "'\\u0123'" );
		matchTestStringAndStream( parser, "'\\u01aF'", "'\\u01aF'" );
		matchFailTestStringAndStream( parser, "'\\u01a'" );
		matchFailTestStringAndStream( parser, "'\\u01a043'" );
	}

	
	public void testSingleQuotedString()
	{
		ParserExpression parser = Tokens.singleQuotedString;
		matchTestStringAndStream( parser, "'abc'", "'abc'" );
		matchTestStringAndStream( parser, "'ab\\'c'", "'ab\\'c'" );
		matchSubTestStringAndStream( parser, "'abc'113", "'abc'", 5 );
	}


	public void testDoubleQuotedString()
	{
		ParserExpression parser = Tokens.doubleQuotedString;
		matchTestStringAndStream( parser, "\"abc\"", "\"abc\"" );
		matchTestStringAndStream( parser, "\"ab\\\"c\"", "\"ab\\\"c\"" );
		matchSubTestStringAndStream( parser, "\"abc\"113\"", "\"abc\"", 5 );
	}


	public void testQuotedString()
	{
		ParserExpression parser = Tokens.quotedString;
		matchTestStringAndStream( parser, "'abc'", "'abc'" );
		matchTestStringAndStream( parser, "'ab\\'c'", "'ab\\'c'" );
		matchSubTestStringAndStream( parser, "'abc'113", "'abc'", 5 );
		matchTestStringAndStream( parser, "\"abc\"", "\"abc\"" );
		matchTestStringAndStream( parser, "\"ab\\\"c\"", "\"ab\\\"c\"" );
		matchSubTestStringAndStream( parser, "\"abc\"113\"", "\"abc\"", 5 );
	}


	public void testUnicodeString()
	{
		ParserExpression parser = Tokens.unicodeString;
		matchTestStringAndStream( parser, "u'abc'", "u'abc'" );
		matchTestStringAndStream( parser, "u'ab\\'c'", "u'ab\\'c'" );
		matchSubTestStringAndStream( parser, "u'abc'113", "u'abc'", 6 );
		matchTestStringAndStream( parser, "u\"abc\"", "u\"abc\"" );
		matchTestStringAndStream( parser, "u\"ab\\\"c\"", "u\"ab\\\"c\"" );
		matchSubTestStringAndStream( parser, "u\"abc\"113\"", "u\"abc\"", 6 );
	}
	
	
	public void testJavaStringLiteral()
	{
		ParserExpression parser = Tokens.javaStringLiteral;
		matchTestStringAndStream( parser, "\"abc\"", "\"abc\"" );
		matchSubTestStringAndStream( parser, "\"abc\"q", "\"abc\"", 5 );
		matchTestStringAndStream( parser, "\"abc\\b\\t\\n\\f\\r\\\"\\\'\\\\xyz\"", "\"abc\\b\\t\\n\\f\\r\\\"\\\'\\\\xyz\"" );
		matchFailTestStringAndStream( parser, "\"abc\\a\\t\\n\\f\\r\\\"\\\'\\\\xyz\"" );
		matchTestStringAndStream( parser, "\"abc\\0\\1\\2\\3\\4\\5\\6\\7xyz\"", "\"abc\\0\\1\\2\\3\\4\\5\\6\\7xyz\"" );
		matchTestStringAndStream( parser, "\"abc\\347xyz\"", "\"abc\\347xyz\"" );
		matchTestStringAndStream( parser, "\"abc\\u0123xyz\"", "\"abc\\u0123xyz\"" );
		matchTestStringAndStream( parser, "\"abc\\u01aFxyz\"", "\"abc\\u01aFxyz\"" );
		matchFailTestStringAndStream( parser, "\"abc\\u347xyz\"" );
	}


}
