//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

public class Bind extends UnaryBranchExpression
{
	private String name;
	
	
	public Bind(String name, String subexp)
	{
		super( subexp );
		this.name = name;
	}
	
	public Bind(String name, ParserExpression subexp)
	{
		super( subexp );
		this.name = name;
	}
	
	
	
	public String getName()
	{
		return name;
	}
	

	protected ParseResult parseString(ParserState state, String input, int start, int stop)
	{
		ParseResult res = subexp.evaluateString( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return res.bind( name, res.getValue(), start );
		}
		else
		{
			return res;
		}
	}

	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
	{
		ParseResult res = subexp.evaluateNode( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return res.bind( name, res.getValue(), start );
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Bind )
		{
			Bind bx = (Bind)x;
			return super.compareTo( x )  &&  name.equals( bx.name );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Bind( " + name + ": " + subexp.toString() + " )";
	}
}
