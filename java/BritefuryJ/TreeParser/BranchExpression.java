//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BranchExpression extends TreeParserExpression
{
	protected TreeParserExpression[] subexps;
	

	public BranchExpression(TreeParserExpression[] subexps)
	{
		this.subexps = subexps;
	}
	
	public BranchExpression(Object[] subexps)
	{
		this( Arrays.asList( subexps ) );
	}
	
	public BranchExpression(List<Object> subexps)
	{
		ArrayList<TreeParserExpression> xs = new ArrayList<TreeParserExpression>();
		
		for (Object x: subexps)
		{
			xs.add( coerce( x ) );
		}
		
		this.subexps = new TreeParserExpression[xs.size()];
		xs.toArray( this.subexps );
	}
	
	
	public List<TreeParserExpression> getSubExpressions()
	{
		return Arrays.asList( subexps );
	}


	public List<TreeParserExpression> getChildren()
	{
		return Arrays.asList( subexps );
	}


	public boolean compareTo(TreeParserExpression x)
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
	
	protected TreeParserExpression[] appendToSubexps(TreeParserExpression x)
	{
		TreeParserExpression[] xs = new TreeParserExpression[subexps.length + 1];
		System.arraycopy( subexps, 0, xs, 0, subexps.length );
		xs[subexps.length] = x;
		return xs;
	}
}
