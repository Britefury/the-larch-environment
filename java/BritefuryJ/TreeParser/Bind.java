//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.List;

public class Bind extends UnaryBranchExpression
{
	protected String name;
	
	
	public Bind(String name, Object subexp)
	{
		super( subexp );
		this.name = name;
	}
	
	public Bind(String name, TreeParserExpression subexp)
	{
		super( subexp );
		this.name = name;
	}
	
	
	
	public String getName()
	{
		return name;
	}
	

	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		TreeParseResult res = subexp.processNode( state, input );
		
		if ( res.isValid() )
		{
			return res.bind( name, res.getValue(), 0 );
		}
		else
		{
			return res;
		}
	}
	
	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		TreeParseResult res = subexp.processList( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return res.bind( name, res.getValue(), start );
		}
		else
		{
			return res;
		}
	}
	
	
	public boolean compareTo(TreeParserExpression x)
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
