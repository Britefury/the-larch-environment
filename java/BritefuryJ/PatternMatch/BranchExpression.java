//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BranchExpression extends MatchExpression
{
	protected MatchExpression[] subexps;
	

	public BranchExpression(MatchExpression[] subexps)
	{
		this.subexps = subexps;
	}
	
	public BranchExpression(Object[] subexps)
	{
		this( Arrays.asList( subexps ) );
	}
	
	public BranchExpression(List<Object> subexps)
	{
		ArrayList<MatchExpression> xs = new ArrayList<MatchExpression>();
		
		for (Object x: subexps)
		{
			xs.add( toMatchExpression( x ) );
		}
		
		this.subexps = new MatchExpression[xs.size()];
		xs.toArray( this.subexps );
	}
	
	
	public List<MatchExpression> getSubExpressions()
	{
		return Arrays.asList( subexps );
	}


	public List<MatchExpression> getChildren()
	{
		return Arrays.asList( subexps );
	}


	public boolean compareTo(MatchExpression x)
	{
		if ( x instanceof BranchExpression )
		{
			BranchExpression xb = (BranchExpression)x;
			
			if ( subexps.length != xb.subexps.length )
			{
				return false;
			}
			
			for (int i = 0; i < subexps.length; i++)
			{
				if ( !subexps[i].compareTo( xb.subexps[i] ) )
				{
					return false;
				}
			}
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	protected String subexpsToString()
	{
		String result = "";
		
		if ( subexps.length > 0 )
		{
			result = subexps[0].toString();
			
			for (int i = 1; i < subexps.length; i++)
			{
				result += ", ";
				result += subexps[i].toString();
			}
		}
		
		return result;
	}
	
	protected MatchExpression[] appendToSubexps(MatchExpression x)
	{
		MatchExpression[] xs = new MatchExpression[subexps.length + 1];
		System.arraycopy( subexps, 0, xs, 0, subexps.length );
		xs[subexps.length] = x;
		return xs;
	}
}
