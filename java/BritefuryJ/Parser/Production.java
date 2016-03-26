//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;



/*
 * Production
 * 
 * Production:node( input )			->  result = memoisedMatch( Production.subexp:node( input ) )
 * Production:string( input, start )	->  result = memoisedMatch( Production.subexp:string( input, start ) )
 * Production:richStr( input, start )	->  result = memoisedMatch( Production.subexp:richStr( input, start ) )
 * Production:list( input, start )		->  result = memoisedMatch( Production.subexp:list( input, start ) )
 */
public class Production extends ParserExpression
{
	public static class CannotOverwriteProductionExpressionException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	
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

	
	
	public void setExpression(Object exp) throws ParserCoerceException, CannotOverwriteProductionExpressionException
	{
		if ( subexp != null )
		{
			throw new CannotOverwriteProductionExpressionException();
		}
		subexp = coerce( exp );
	}
	
	public void setExpression(ParserExpression exp) throws CannotOverwriteProductionExpressionException
	{
		if ( subexp != null )
		{
			throw new CannotOverwriteProductionExpressionException();
		}
		subexp = exp;
	}
	
	public ParserExpression getExpression()
	{
		return subexp;
	}

	
	
	@Override
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		return state.memoisedMatchNode( subexp, input );
	}

	@Override
	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		return state.memoisedMatchString( subexp, input, start );
	}

	@Override
	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		return state.memoisedMatchRichString( subexp, input, start );
	}

	@Override
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		return state.memoisedMatchList( subexp, input, start );
	}


	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = { subexp };
		return Arrays.asList( children );
	}

	
	
	public boolean isEquivalentTo(ParserExpression x)
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
				return subexp.isEquivalentTo( px.subexp ); 
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
			return "Production( <" + getExpressionName() + "> )";
		}
	}
}
