//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.NodeParser;

import java.util.HashMap;


class ParserState
{
	private static class MemoKey
	{
		public int position;
		public ParserExpression rule;
		
		public MemoKey(int position, ParserExpression rule)
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
	}
	

	
	private HashMap<Object, HashMap<MemoKey, ParseResult> > memos;
	private Object currentInput;
	private HashMap<MemoKey, ParseResult> currentMemo;
	
	protected DebugParseResult.DebugNode debugStack;
	protected boolean bDebuggingEnabled;
	
	
	
	public ParserState()
	{
		this.memos = new HashMap<Object, HashMap<MemoKey, ParseResult> >();
	}
	
	
	protected void enableDebugging()
	{
		bDebuggingEnabled = true;
		debugStack = null;
	}
	
	
	
	ParseResult memoisedMatch(ParserExpression rule, Object input, int start, int stop)
	{
		if ( input != currentInput )
		{
			currentInput = input;
			
			currentMemo = memos.get( currentInput );
			if ( currentMemo == null )
			{
				currentMemo = new HashMap<MemoKey, ParseResult>();
				memos.put( currentInput, currentMemo );
			}
		}
		
		MemoKey key = new MemoKey( start, rule );
		ParseResult memoEntry = currentMemo.get( key );
		
		if ( memoEntry == null )
		{
			// Create the memo-entry, and memoise
			ParseResult answer = rule.evaluateNode( this, input, start, stop );
			
			currentMemo.put( key, answer );
			
			return answer;
		}
		else
		{
			return memoEntry;
		}
	}
}


