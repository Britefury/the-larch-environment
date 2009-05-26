//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import BritefuryJ.ParserHelpers.DebugNode;


class TreeParserState
{
	private static class MemoKey
	{
		public int position;
		public TreeParserExpression rule;
		
		public MemoKey(int position, TreeParserExpression rule)
		{
			this.position = position;
			this.rule = rule;
		}
		
		public int hashCode()
		{
			int mult = 1000003;
			int x = 0x345678;
			x = ( x ^ rule.hashCode() ) * mult;
			mult += 82520 + 2;
			x = ( x ^ position ) * mult;
			return x + 97351;
		}
		
		public boolean equals(Object x)
		{
			if ( this == x )
			{
				return true;
			}
			
			if ( x instanceof MemoKey )
			{
				MemoKey mx = (MemoKey)x;
				return position == mx.position  &&  rule == mx.rule;
			}
			else
			{
				return false;
			}
		}
	}
	

	
	private IdentityHashMap<Object, HashMap<MemoKey, TreeParseResult> > memos;
	private Object currentInput;
	private HashMap<MemoKey, TreeParseResult> currentMemo;
	protected Object arg;
	protected TreeParseAction delegateAction;
	
	protected DebugNode debugStack;
	protected boolean bDebuggingEnabled;
	
	
	
	public TreeParserState(Object arg, TreeParseAction delegateAction)
	{
		this.memos = new IdentityHashMap<Object, HashMap<MemoKey, TreeParseResult> >();
		this.arg = arg;
		this.delegateAction = delegateAction;
	}
	
	
	protected void enableDebugging()
	{
		bDebuggingEnabled = true;
		debugStack = null;
	}
	
	
	
	TreeParseResult memoisedMatchNode(TreeParserExpression rule, Object input)
	{
		if ( input != currentInput )
		{
			currentInput = input;
			
			currentMemo = memos.get( currentInput );
			if ( currentMemo == null )
			{
				currentMemo = new HashMap<MemoKey, TreeParseResult>();
				memos.put( currentInput, currentMemo );
			}
		}
		
		MemoKey key = new MemoKey( 0, rule );
		TreeParseResult memoEntry = currentMemo.get( key );
		
		if ( memoEntry == null )
		{
			// Create the memo-entry, and memoise
			TreeParseResult answer = rule.processNode( this, input );
			
			currentMemo.put( key, answer );
			
			return answer;
		}
		else
		{
			return memoEntry;
		}
	}


	TreeParseResult memoisedMatchList(TreeParserExpression rule, List<Object> input, int start, int stop)
	{
		if ( input != currentInput )
		{
			currentInput = input;
			
			currentMemo = memos.get( currentInput );
			if ( currentMemo == null )
			{
				currentMemo = new HashMap<MemoKey, TreeParseResult>();
				memos.put( currentInput, currentMemo );
			}
		}
		
		MemoKey key = new MemoKey( start, rule );
		TreeParseResult memoEntry = currentMemo.get( key );
		
		if ( memoEntry == null )
		{
			// Create the memo-entry, and memoise
			TreeParseResult answer = rule.processList( this, input, start, stop );
			
			currentMemo.put( key, answer );
			
			return answer;
		}
		else
		{
			return memoEntry;
		}
	}
}


