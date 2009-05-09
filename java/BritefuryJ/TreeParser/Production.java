//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.Arrays;
import java.util.List;


public class Production extends TreeParserExpression
{
	public static class CannotOverwriteProductionExpressionException extends Exception
	{
		private static final long serialVersionUID = 1L;
	};
	
	
	protected String expressionName;
	protected TreeParserExpression subexp;
	
	
	public Production(String name)
	{
		this.expressionName = name;
		this.subexp = null;
	}
	
	public Production(String name, TreeParserExpression subexp)
	{
		this.expressionName = name;
		this.subexp = subexp;
	}
	
	public Production(String name, Object subexp)
	{
		this.expressionName = name;
		this.subexp = coerce( subexp );
	}
	
	

	public String getExpressionName()
	{
		return expressionName;
	}

	
	
	public TreeParserExpression setExpression(Object exp) throws CannotOverwriteProductionExpressionException
	{
		if ( subexp != null )
		{
			throw new CannotOverwriteProductionExpressionException();
		}
		subexp = coerce( exp );
		return this;
	}
	
	public TreeParserExpression setExpression(TreeParserExpression exp) throws CannotOverwriteProductionExpressionException
	{
		if ( subexp != null )
		{
			throw new CannotOverwriteProductionExpressionException();
		}
		subexp = exp;
		return this;
	}
	
	public TreeParserExpression getExpression()
	{
		return subexp;
	}

	
	
	
	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		return state.memoisedMatchNode( subexp, input ).clearBindings();
	}

	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		return state.memoisedMatchList( subexp, input, start, stop ).clearBindings();
	}



	public List<TreeParserExpression> getChildren()
	{
		TreeParserExpression[] children = { subexp };
		return Arrays.asList( children );
	}

	
	
	public boolean compareTo(TreeParserExpression x)
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
