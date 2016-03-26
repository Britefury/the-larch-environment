//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;

public abstract class BranchExpression extends ParserExpression
{
	protected final ParserExpression[] subexps;
	

	public BranchExpression(ParserExpression[] subexps)
	{
		this.subexps = subexps;
	}
	
	public BranchExpression(Object[] subexps) throws ParserCoerceException
	{
		this.subexps = new ParserExpression[subexps.length];
		
		int i = 0;
		for (Object x: subexps)
		{
			this.subexps[i++] = coerce( x );
		}
	}
	
	
	public List<ParserExpression> getSubExpressions()
	{
		return Arrays.asList( subexps );
	}


	public List<ParserExpression> getChildren()
	{
		return Arrays.asList( subexps );
	}


	public boolean isEquivalentTo(ParserExpression x)
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
				if ( !subexps[i].isEquivalentTo( xb.subexps[i] ) )
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
		StringBuilder result = new StringBuilder();
		
		if ( subexps.length > 0 )
		{
			result.append( subexps[0].toString() );
			
			for (int i = 1; i < subexps.length; i++)
			{
				result.append( ", " );
				result.append( subexps[i].toString() );
			}
		}
		
		return result.toString();
	}
	
	protected ParserExpression[] appendToSubexps(ParserExpression x)
	{
		ParserExpression[] xs = new ParserExpression[subexps.length + 1];
		System.arraycopy( subexps, 0, xs, 0, subexps.length );
		xs[subexps.length] = x;
		return xs;
	}
}
