//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public abstract class BranchExpression extends ParserExpression
{
	ParserExpression[] subexps;
	

	public BranchExpression(Object[] subexps) throws ParserCoerceException
	{
		this( Arrays.asList( subexps ) );
	}
	
	@SuppressWarnings("unchecked")
	public BranchExpression(List<Object> subexps) throws ParserCoerceException
	{
		Vector<ParserExpression> xs = new Vector<ParserExpression>();
		
		for (Object x: subexps)
		{
			if ( x instanceof ParserExpression )
			{
				xs.add( (ParserExpression)x );
			}
			else if ( x instanceof String )
			{
				xs.add( coerce( (String)x ) );
			}
			else if ( x instanceof List )
			{
				xs.add( coerce( (List<Object>)x ) );
			}
			else
			{
				throw new ParserCoerceException();
			}
		}
		
		this.subexps = new ParserExpression[xs.size()];
		xs.toArray( this.subexps );
	}
	
	
	public List<ParserExpression> getSubExpressions()
	{
		return Arrays.asList( subexps );
	}


	public List<ParserExpression> getChildren()
	{
		return Arrays.asList( subexps );
	}


	public boolean compareTo(ParserExpression x)
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
	
	protected List<Object> joinSubexp(ParserExpression x)
	{
		Vector<Object> xs = new Vector<Object>();
		xs.addAll( Arrays.asList( subexps) );
		xs.add( x );
		return xs;
	}
}
