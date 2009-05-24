//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class StructuralItem extends ParserExpression
{
	public StructuralItem()
	{
	}
	
	
	protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
	{
		start = state.skipJunkChars( input, start );
		
		Object value[] = input.matchStructuralNode( start );
		
		if ( value != null )
		{
			return new ParseResult( value[0], start, start + 1 );
		}
		
		return ParseResult.failure( start );
	}
	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof StructuralItem )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "StructuralNode()";
	}
}
