//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Vector;

public class DebugNode
{
	private DebugNode prev;
	private Vector<DebugNode> callChildren, memoChildren;

	private ParserExpression expression;
	private ParseResult result;
	
	
	public DebugNode(DebugNode prev, ParserExpression expression)
	{
		callChildren = new Vector<DebugNode>();
		memoChildren = new Vector<DebugNode>();
		this.prev = prev;
		this.expression = expression;
		this.result = null;
	}
	
	
	protected void setResult(ParseResult result)
	{
		this.result = result;
	}
	
	protected void addCallChild(DebugNode node)
	{
		callChildren.add( node );
	}
			
	protected void addMemoChild(DebugNode node)
	{
		memoChildren.add( node );
	}
			
	
	protected DebugNode getPrev()
	{
		return prev;
	}


	public Vector<DebugNode> getCallChildren()
	{
		return callChildren;
	}

	public Vector<DebugNode> getMemoChildren()
	{
		return memoChildren;
	}


	public ParserExpression getExpression()
	{
		return expression;
	}
	
	public ParseResult getResult()
	{
		return result;
	}
}
