//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.NodeParser;

import java.util.ArrayList;
import java.util.HashMap;

public class DebugParseResult extends ParseResult
{
	public static class DebugNode
	{
		private DebugNode prev;
		private ArrayList<DebugNode> callChildren, memoChildren;

		private ParserExpression expression;
		private ParseResult result;
		
		
		public DebugNode(DebugNode prev, ParserExpression expression)
		{
			callChildren = new ArrayList<DebugNode>();
			memoChildren = new ArrayList<DebugNode>();
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
		
		public ParseResult getResult()
		{
			return result;
		}
	}

	
	
	
	public DebugNode debugNode;
	
	
	protected DebugParseResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid, HashMap<String, Object> bindings, DebugNode debugNode)
	{
		super( value, begin, end, bSuppressed, bValid, bindings );
		this.debugNode = debugNode;
	}
	
}
