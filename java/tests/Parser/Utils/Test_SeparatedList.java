//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named "COPYING" that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser.Utils;

import tests.Parser.ParserTestCase;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Utils.SeparatedList;
import BritefuryJ.Parser.Utils.Tokens;

public class Test_SeparatedList extends ParserTestCase
{
	public void testSeparatedList()
	{
		ParserExpression parser0 = SeparatedList.separatedList( Tokens.identifier, ",", false, false, false );
		ParserExpression parser1 = SeparatedList.separatedList( Tokens.identifier, ",", true, false, false );
		ParserExpression parser0T = SeparatedList.separatedList( Tokens.identifier, ",", false, true, false );
		ParserExpression parser1T = SeparatedList.separatedList( Tokens.identifier, ",", true, true, false );
		ParserExpression parser0R = SeparatedList.separatedList( Tokens.identifier, ",", false, false, true );
		ParserExpression parser1R = SeparatedList.separatedList( Tokens.identifier, ",", true, false, true );
		ParserExpression parser0TR = SeparatedList.separatedList( Tokens.identifier, ",", false, true, true );
		ParserExpression parser1TR = SeparatedList.separatedList( Tokens.identifier, ",", true, true, true );

		matchTestSX( parser0, "", "()" );
		matchIncompleteTest( parser0, "," );
		matchTestSX( parser0, "ab", "(ab)" );
		matchIncompleteTest( parser0, "ab," );
		matchTestSX( parser0, "ab,cd", "(ab cd)" );
		matchIncompleteTest( parser0, "ab,cd," );
		matchTestSX( parser0, "ab,cd,ef", "(ab cd ef)" );
		matchIncompleteTest( parser0, "ab,cd,ef," );
		
		matchFailTest( parser1, "" );
		matchFailTest( parser1, "," );
		matchTestSX( parser1, "ab", "(ab)" );
		matchIncompleteTest( parser1, "ab," );
		matchTestSX( parser1, "ab,cd", "(ab cd)" );
		matchIncompleteTest( parser1, "ab,cd," );
		matchTestSX( parser1, "ab,cd,ef", "(ab cd ef)" );
		matchIncompleteTest( parser1, "ab,cd,ef," );

		matchTestSX( parser0T, "", "()" );
		matchIncompleteTest( parser0T, "," );
		matchTestSX( parser0T, "ab", "(ab)" );
		matchTestSX( parser0T, "ab,", "(ab)" );
		matchTestSX( parser0T, "ab,cd", "(ab cd)" );
		matchTestSX( parser0T, "ab,cd,", "(ab cd)" );
		matchTestSX( parser0T, "ab,cd,ef", "(ab cd ef)" );
		matchTestSX( parser0T, "ab,cd,ef,", "(ab cd ef)" );
		
		matchFailTest( parser1T, "" );
		matchFailTest( parser1T, "," );
		matchTestSX( parser1T, "ab", "(ab)" );
		matchTestSX( parser1T, "ab,", "(ab)" );
		matchTestSX( parser1T, "ab,cd", "(ab cd)" );
		matchTestSX( parser1T, "ab,cd,", "(ab cd)" );
		matchTestSX( parser1T, "ab,cd,ef", "(ab cd ef)" );
		matchTestSX( parser1T, "ab,cd,ef,", "(ab cd ef)" );
	
		
		matchTestSX( parser0R, "", "()" );
		matchIncompleteTest( parser0R, "," );
		matchIncompleteTest( parser0R, "ab" );
		matchTestSX( parser0R, "ab,", "(ab)" );
		matchTestSX( parser0R, "ab,cd", "(ab cd)" );
		matchIncompleteTest( parser0R, "ab,cd," );
		matchTestSX( parser0R, "ab,cd,ef", "(ab cd ef)" );
		matchIncompleteTest( parser0R, "ab,cd,ef," );
		
		matchFailTest( parser1R, "" );
		matchFailTest( parser1R, "," );
		matchFailTest( parser1R, "ab" );
		matchTestSX( parser1R, "ab,", "(ab)" );
		matchTestSX( parser1R, "ab,cd", "(ab cd)" );
		matchIncompleteTest( parser1R, "ab,cd," );
		matchTestSX( parser1R, "ab,cd,ef", "(ab cd ef)" );
		matchIncompleteTest( parser1R, "ab,cd,ef," );

		matchTestSX( parser0TR, "", "()" );
		matchIncompleteTest( parser0TR, "," );
		matchIncompleteTest( parser0TR, "ab" );
		matchTestSX( parser0TR, "ab,", "(ab)" );
		matchTestSX( parser0TR, "ab,cd", "(ab cd)" );
		matchTestSX( parser0TR, "ab,cd,", "(ab cd)" );
		matchTestSX( parser0TR, "ab,cd,ef", "(ab cd ef)" );
		matchTestSX( parser0TR, "ab,cd,ef,", "(ab cd ef)" );
		
		matchFailTest( parser1TR, "" );
		matchFailTest( parser1TR, "," );
		matchFailTest( parser1TR, "ab" );
		matchTestSX( parser1TR, "ab,", "(ab)" );
		matchTestSX( parser1TR, "ab,cd", "(ab cd)" );
		matchTestSX( parser1TR, "ab,cd,", "(ab cd)" );
		matchTestSX( parser1TR, "ab,cd,ef", "(ab cd ef)" );
		matchTestSX( parser1TR, "ab,cd,ef,", "(ab cd ef)" );
	}

		
	public void testDelimitedSeparatedList()
	{
		ParserExpression parser = SeparatedList.delimitedSeparatedList( Tokens.identifier, "[", "]", ",", false, false, false );
		matchFailTest( parser, "" );
		matchFailTest( parser, "ab" );
		matchTestSX( parser, "[]", "()" );
		matchTestSX( parser, "[ab]", "(ab)" );
		matchTestSX( parser, "[ab,cd]", "(ab cd)" );
		matchFailTest( parser, "ab,cd]" );
		matchFailTest( parser, "[ab,cd" );
	}

}
