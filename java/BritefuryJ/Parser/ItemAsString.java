//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

public class ItemAsString extends UnaryBranchExpression
{
	public ItemAsString(String subexp)
	{
		super( subexp );
	}
	
	public ItemAsString(ParserExpression subexp)
	{
		super( subexp );
	}
	
	
	protected ParseResult parseString(ParserState state, String input, int start, int stop)
	{
		return subexp.evaluateString( state, input, start, stop );
	}


	@SuppressWarnings("unchecked")
	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
	{
		if ( input instanceof String )
		{
			return subexp.evaluateString( state, (String)input, start, stop );
		}
		else if ( input instanceof List )
		{
			try
			{
				List<Object> xs = (List<Object>)input;
				if ( stop > start )
				{
					String s = (String)xs.get( start );
					ParseResult res = subexp.evaluateString( state, s, 0, s.length() );
					if ( res.isValid() )
					{
						return new ParseResult( res.getValue(), start, start + 1 );
					}
				}
			}
			catch (ClassCastException e)
			{
			}
		}

		return ParseResult.failure( start );
	}



	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof ItemAsString )
		{
			return super.compareTo( x );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "ItemAsString( " + subexp.toString() + " )";
	}
}
