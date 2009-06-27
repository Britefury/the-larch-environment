//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser.Utils;

import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMModuleResolver;
import BritefuryJ.Parser.Utils.Tokens;
import BritefuryJ.ParserOld.ParserExpression;
import tests.ParserOld.ParserTestCase;

public class Test_Tokens extends ParserTestCase
{
	protected DMModule M;
	protected DMModuleResolver resolver = new DMModuleResolver()
	{
		public DMModule getModule(String location) throws CouldNotResolveModuleException
		{
			return location.equals( "Tests.Parser.Tokens" )  ?  M  :  null;
		}
	};
	
	
	
	protected DMModuleResolver getModuleResolver()
	{
		return resolver;
	}
	

	
	public void setUp()
	{
		M = new DMModule( "Tokens", "m", "Tests.Parser.Tokens" );
	}
	
	public void tearDown()
	{
		M = null;
	}

	
	public void testIdentifier()
	{
		ParserExpression parser = Tokens.identifier;
		matchTest( parser, "ab", "ab" );
		matchTest( parser, "ab12", "ab12" );
		matchFailTest( parser, "12ab" );
		matchTest( parser, "_ab", "_ab" );
	}

	public void testJavaIdentifier()
	{
		ParserExpression parser = Tokens.javaIdentifier;
		matchTest( parser, "ab", "ab" );
		matchTest( parser, "ab12", "ab12" );
		matchFailTest( parser, "12ab" );
		matchTest( parser, "_ab", "_ab" );
	}
	

	public void testDecimalInteger()
	{
		ParserExpression parser = Tokens.decimalInteger;
		matchTest( parser, "123", "123" );
		matchTest( parser, "-123", "-123" );
	}

	public void testDecimalIntegerNoOctal()
	{
		ParserExpression parser = Tokens.decimalIntegerNoOctal;
		matchTest( parser, "123", "123" );
		matchTest( parser, "0", "0" );
		matchSubTest( parser, "0123", "0", 1 );
	}


	public void testHexadecimalInteger()
	{
		ParserExpression parser = Tokens.hexInteger;
		matchTest( parser, "0x123", "0x123" );
		matchTest( parser, "0X123", "0X123" );
		matchTest( parser, "0x0123456789abcdef", "0x0123456789abcdef" );
		matchTest( parser, "0x0123456789ABCDEF", "0x0123456789ABCDEF" );
	}

	public void testOctalInteger()
	{
		ParserExpression parser = Tokens.octalInteger;
		matchFailTest( parser, "12" );
		matchFailTest( parser, "0" );
		matchTest( parser, "01", "01" );
		matchTest( parser, "01234567", "01234567" );
		matchSubTest( parser, "0123456789", "01234567", 8 );
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

	
	
	public void testJavaCharacterLiteral()
	{
		ParserExpression parser = Tokens.javaCharacterLiteral;
		matchTest( parser, "'a'", "'a'" );
		matchFailTest( parser, "'a" );
		matchTest( parser, "'\\b'", "'\\b'" );
		matchTest( parser, "'\\t'", "'\\t'" );
		matchTest( parser, "'\\n'", "'\\n'" );
		matchTest( parser, "'\\f'", "'\\f'" );
		matchTest( parser, "'\\r'", "'\\r'" );
		matchTest( parser, "'\\\"'", "'\\\"'" );
		matchTest( parser, "'\\''", "'\\''" );
		matchTest( parser, "'\\\\'", "'\\\\'" );
		matchFailTest( parser, "'\\a'" );
		matchTest( parser, "'\\0'", "'\\0'" );
		matchTest( parser, "'\\1'", "'\\1'" );
		matchTest( parser, "'\\2'", "'\\2'" );
		matchTest( parser, "'\\3'", "'\\3'" );
		matchTest( parser, "'\\4'", "'\\4'" );
		matchTest( parser, "'\\5'", "'\\5'" );
		matchTest( parser, "'\\6'", "'\\6'" );
		matchTest( parser, "'\\7'", "'\\7'" );
		matchTest( parser, "'\\77'", "'\\77'" );
		matchFailTest( parser, "'\\777'" );
		matchFailTest( parser, "'\\477'" );
		matchTest( parser, "'\\377'", "'\\377'" );
		matchTest( parser, "'\\u0123'", "'\\u0123'" );
		matchTest( parser, "'\\u01aF'", "'\\u01aF'" );
		matchFailTest( parser, "'\\u01a'" );
		matchFailTest( parser, "'\\u01a043'" );
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
	
	
	public void testJavaStringLiteral()
	{
		ParserExpression parser = Tokens.javaStringLiteral;
		matchTest( parser, "\"abc\"", "\"abc\"" );
		matchSubTest( parser, "\"abc\"q", "\"abc\"", 5 );
		matchTest( parser, "\"abc\\b\\t\\n\\f\\r\\\"\\\'\\\\xyz\"", "\"abc\\b\\t\\n\\f\\r\\\"\\\'\\\\xyz\"" );
		matchFailTest( parser, "\"abc\\a\\t\\n\\f\\r\\\"\\\'\\\\xyz\"" );
		matchTest( parser, "\"abc\\0\\1\\2\\3\\4\\5\\6\\7xyz\"", "\"abc\\0\\1\\2\\3\\4\\5\\6\\7xyz\"" );
		matchTest( parser, "\"abc\\347xyz\"", "\"abc\\347xyz\"" );
		matchTest( parser, "\"abc\\u0123xyz\"", "\"abc\\u0123xyz\"" );
		matchTest( parser, "\"abc\\u01aFxyz\"", "\"abc\\u01aFxyz\"" );
		matchFailTest( parser, "\"abc\\u347xyz\"" );
	}


}
