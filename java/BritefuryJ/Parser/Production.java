//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;



public class Production extends ParserExpression
{
	public static class CannotOverwriteProductionExpressionException extends Exception
	{
		private static final long serialVersionUID = 1L;
	};
	
	
	protected String expressionName;
	protected ParserExpression subexp;
	
	
	public Production(String name)
	{
		this.expressionName = name;
		this.subexp = null;
	}
	
	public Production(String name, ParserExpression subexp)
	{
		this.expressionName = name;
		this.subexp = subexp;
	}
	
	public Production(String name, Object subexp) throws ParserCoerceException
	{
		this.expressionName = name;
		this.subexp = coerce( subexp );
	}
	
	

	public String getExpressionName()
	{
		return expressionName;
	}

	
	
	public ParserExpression setExpression(Object exp) throws ParserCoerceException, CannotOverwriteProductionExpressionException
	{
		if ( subexp != null )
		{
			throw new CannotOverwriteProductionExpressionException();
		}
		subexp = coerce( exp );
		return this;
	}
	
	public ParserExpression setExpression(ParserExpression exp) throws CannotOverwriteProductionExpressionException
	{
		if ( subexp != null )
		{
			throw new CannotOverwriteProductionExpressionException();
		}
		subexp = exp;
		return this;
	}
	
	public ParserExpression getExpression()
	{
		return subexp;
	}

	
	
	
	protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
	{
		return state.memoisedMatchStream( subexp, input, start );
	}



	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = { subexp };
		return Arrays.asList( children );
	}

	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Production )
		{
			Production px = (Production)x;
			if ( subexp == null  &&  px.subexp == null )
			{
				return true;
			}
			else if ( subexp != null  &&  px.subexp != null )
			{
				return subexp.compareTo( px.subexp ); 
			}
		}

		return false;
	}
	


	public String toString()
	{
		if ( subexp == null )
		{
			return "Production( <null> )";
		}
		else
		{
			return "Production( <" + subexp.getExpressionName() + "> )";
		}
	}
}
