//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;

public abstract class UnaryBranchExpression extends ParserExpression
{
	protected ParserExpression subexp;
	
	
	public UnaryBranchExpression(ParserExpression subexp)
	{
		this.subexp = subexp;

		if ( subexp == null )
		{
			throw new RuntimeException( "Sub expressions cannot be null" );
		}
	}
	
	public UnaryBranchExpression(Object subexp) throws ParserCoerceException
	{
		this( coerce( subexp ) );
	}
	
	
	
	public ParserExpression getSubExpression()
	{
		return subexp;
	}
	

	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = { subexp };
		return Arrays.asList( children );
	}
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof UnaryBranchExpression )
		{
			UnaryBranchExpression ux = (UnaryBranchExpression)x;
			return subexp.compareTo( ux.subexp );
		}
		else
		{
			return false;
		}
	}
	

	public String toString()
	{
		return "UnaryBranchExpression( " + subexp.toString() + " )";
	}
}
