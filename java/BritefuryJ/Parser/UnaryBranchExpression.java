//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
	}
	
	public UnaryBranchExpression(String subexp)
	{
		this.subexp = coerce( subexp );
	}
	
	public UnaryBranchExpression(Object subexp) throws ParserCoerceException
	{
		this.subexp = coerce( subexp );
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
	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof UnaryBranchExpression )
		{
			UnaryBranchExpression ux = (UnaryBranchExpression)x;
			return subexp.isEquivalentTo( ux.subexp );
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
