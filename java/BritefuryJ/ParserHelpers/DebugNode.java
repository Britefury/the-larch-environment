//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserHelpers;

import java.util.ArrayList;

import BritefuryJ.Parser.ParserExpression;

public class DebugNode
{
	private DebugNode prev;
	private ArrayList<DebugNode> callChildren, memoChildren;

	private ParserExpression expression;
	private ParseResultInterface result;
	
	private Object input;
	private int start;
	
	
	public DebugNode(DebugNode prev, ParserExpression expression, Object input, int start)
	{
		callChildren = new ArrayList<DebugNode>();
		memoChildren = new ArrayList<DebugNode>();
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
	
	public void addCallChild(DebugNode node)
	{
		callChildren.add( node );
	}
			
	public void addMemoChild(DebugNode node)
	{
		memoChildren.add( node );
	}
			
	
	public DebugNode getPrev()
	{
		return prev;
	}


	public ArrayList<DebugNode> getCallChildren()
	{
		return callChildren;
	}

	public ArrayList<DebugNode> getMemoChildren()
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
