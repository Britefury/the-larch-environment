//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ParserHelpers;

import java.util.ArrayList;

import BritefuryJ.Parser.ParserExpression;

public class TraceNode
{
	private TraceNode prev;
	private ArrayList<TraceNode> callChildren, memoChildren;

	private ParserExpression expression;
	private ParseResultInterface result;
	
	private Object input;
	private int start;
	
	
	public TraceNode(TraceNode prev, ParserExpression expression, Object input, int start)
	{
		callChildren = new ArrayList<TraceNode>();
		memoChildren = new ArrayList<TraceNode>();
		this.prev = prev;
		this.expression = expression;
		this.result = null;
		this.input = input;
		this.start = start;
	}
	
	
	public void setResult(ParseResultInterface result)
	{
		this.result = result;
	}
	
	public void addCallChild(TraceNode node)
	{
		callChildren.add( node );
	}
			
	public void addMemoChild(TraceNode node)
	{
		memoChildren.add( node );
	}
			
	
	public TraceNode getPrev()
	{
		return prev;
	}


	public ArrayList<TraceNode> getCallChildren()
	{
		return callChildren;
	}

	public ArrayList<TraceNode> getMemoChildren()
	{
		return memoChildren;
	}


	public ParserExpression getExpression()
	{
		return expression;
	}
	
	public ParseResultInterface getResult()
	{
		return result;
	}
	
	public Object getInput()
	{
		return input;
	}
	
	public int getStart()
	{
		return start;
	}
}
