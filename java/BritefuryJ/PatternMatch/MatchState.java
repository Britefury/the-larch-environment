//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.HashMap;
import java.util.IdentityHashMap;

import BritefuryJ.ParserHelpers.DebugNode;


class MatchState
{
	private static class MemoKey
	{
		public int position;
		public MatchExpression rule;
		
		public MemoKey(int position, MatchExpression rule)
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
	

	
	private IdentityHashMap<Object, HashMap<MemoKey, MatchResult> > memos;
	private Object currentInput;
	private HashMap<MemoKey, MatchResult> currentMemo;
	protected Object arg;
	protected MatchAction delegateAction;
	
	protected DebugNode debugStack;
	protected boolean bDebuggingEnabled;
	
	
	
	public MatchState(Object arg, MatchAction delegateAction)
	{
		this.memos = new IdentityHashMap<Object, HashMap<MemoKey, MatchResult> >();
		this.arg = arg;
		this.delegateAction = delegateAction;
	}
	
	
	protected void enableDebugging()
	{
		bDebuggingEnabled = true;
		debugStack = null;
	}
	
	
	
	MatchResult memoisedMatch(MatchExpression rule, Object input, int start, int stop)
	{
		if ( input != currentInput )
		{
			currentInput = input;
			
			currentMemo = memos.get( currentInput );
			if ( currentMemo == null )
			{
				currentMemo = new HashMap<MemoKey, MatchResult>();
				memos.put( currentInput, currentMemo );
			}
		}
		
		MemoKey key = new MemoKey( start, rule );
		MatchResult memoEntry = currentMemo.get( key );
		
		if ( memoEntry == null )
		{
			// Create the memo-entry, and memoise
			MatchResult answer = rule.evaluateNode( this, input, start, stop );
			
			currentMemo.put( key, answer );
			
			return answer;
		}
		else
		{
			return memoEntry;
		}
	}
}


