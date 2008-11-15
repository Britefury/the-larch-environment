//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

public class Bind extends UnaryBranchExpression
{
	protected String name;
	
	
	public Bind(String name, Object subexp)
	{
		super( subexp );
		this.name = name;
	}
	
	public Bind(String name, MatchExpression subexp)
	{
		super( subexp );
		this.name = name;
	}
	
	
	
	public String getName()
	{
		return name;
	}
	

	protected MatchResult parseNode(MatchState state, Object input, int start, int stop)
	{
		MatchResult res = subexp.evaluateNode( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return res.bind( name, res.getValue(), start );
		}
		else
		{
			return res;
		}
	}
	
	
	public boolean compareTo(MatchExpression x)
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
