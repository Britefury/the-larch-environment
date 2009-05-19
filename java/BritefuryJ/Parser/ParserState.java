//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.Utils.HashUtils;


public class ParserState
{
	private static class MemoEntry
	{
		public ParserExpression rule;
		public int pos;
		public ParseResult answer;
		public boolean bEvaluating, bLeftRecursionDetected;
		public HashSet<MemoEntry> dependents;		// dependents; for a left-recursive application
		
		
		public MemoEntry(ParserExpression rule, int pos)
		{
			this.rule = rule;
			this.pos = pos;
			answer = ParseResult.failure( pos );
			bEvaluating = bLeftRecursionDetected = false;
		}
		
		public void addDependent(MemoEntry dep)
		{
			if ( dependents == null )
			{
				dependents = new HashSet<MemoEntry>();
			}
			if ( dep.pos == pos )
			{
				dependents.add( dep );
			}
		}
	};
	
	
	private static class RuleInvocation
	{
		public ParserExpression rule;
		public MemoEntry memoEntry;
		public RuleInvocation outerInvocation;
		
		public RuleInvocation(ParserExpression rule, MemoEntry memoEntry, RuleInvocation outerInvocation)
		{
			this.rule = rule;
			this.memoEntry = memoEntry;
			this.outerInvocation = outerInvocation;
		}
	}
	
	
	private static class MemoKey
	{
		private int pos;
		private ParserExpression rule;
		private int hash;
		
		public MemoKey(int pos, ParserExpression rule)
		{
			this.pos = pos;
			this.rule = rule;
			this.hash = HashUtils.doubleHash( pos, rule.hashCode() );
		}
		
		public int hashCode()
		{
			return hash;
		}
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof MemoKey )
			{
				MemoKey mx = (MemoKey)x;
				return pos == mx.pos  &&  rule == mx.rule;
			}
			else
			{
				return false;
			}
		}
	}
	
	
	
	
	private HashMap<MemoKey, MemoEntry> memo;
	private RuleInvocation ruleInvocationStack;
	private Pattern junkPattern;
	private HashSet<MemoEntry> dependencies;
	protected DebugNode debugStack;
	protected boolean bDebuggingEnabled;
	
	
	
	public ParserState(String junkRegex)
	{
		this.memo = new HashMap<MemoKey, MemoEntry>();
		junkPattern = Pattern.compile( junkRegex );
		dependencies = null;
	}
	
	
	public int skipJunkChars(String input, int start, int stop)
	{
		Matcher m = junkPattern.matcher( input.subSequence( start, stop ) );
		if ( m.find() )
		{
			return start + m.end();
		}
		else
		{
			return start;
		}
	}
	
	
	protected void enableDebugging()
	{
		bDebuggingEnabled = true;
		debugStack = null;
	}
	

	
	
	
	@SuppressWarnings("unchecked")
	ParseResult memoisedMatchString(ParserExpression rule, String input, int start, int stop)
	{
		MemoKey key = new MemoKey( start, rule );
		MemoEntry memoEntry = memo.get( key );
		
		if ( memoEntry == null )
		{
			// Create the memo-entry, and memoise
			memoEntry = new MemoEntry( rule, start );
			memo.put( key, memoEntry );
			
			
			// Create a rule invocation record, and push onto the rule invocation stack
			ruleInvocationStack = new RuleInvocation( rule, memoEntry, ruleInvocationStack );
			memoEntry.bEvaluating = true;
			

			// Take a copy of the dependencies, and clear the global list
			HashSet<MemoEntry> deps = dependencies != null  ?  (HashSet<MemoEntry>)dependencies.clone()  :  null;
			if ( dependencies != null )
			{
				dependencies.clear();
			}
			
			
			// Evaluate the rule, at position @start
			ParseResult answer = rule.evaluateString( this, (String)input, start, stop );
			

			// Merge dependency lists, into global list
			if ( deps != null )
			{
				if ( dependencies == null )
				{
					dependencies = new HashSet<MemoEntry>();
				}
				dependencies.addAll( deps );
			}
			if ( dependencies != null )
			{
				for (MemoEntry d: dependencies)
				{
					if ( memoEntry != d )
					{
						d.addDependent( memoEntry );
					}
				}
			}

			
			// If left-recursion has been detected
			if ( memoEntry.bLeftRecursionDetected )
			{
				// Grow the left recursive parse
				answer = growLeftRecursiveParse( rule, input, start, stop, memoEntry, answer );
			}
			
			// Pop the rule invocation off the rule invocation stack
			ruleInvocationStack = ruleInvocationStack.outerInvocation;
			memoEntry.bEvaluating = false;
			memoEntry.answer = answer;
			
			return answer;
		}
		else
		{
			if ( memoEntry.bEvaluating )
			{
				// Somewhere lower down in the call stack is the outer application of @rule; this application
				// is recursive; specifically left-recursive since we are at the same position in the stream

				// Left recursion has been detected
				onLeftRecursionDetected( rule, input, start, stop, memoEntry );
			}
			return memoEntry.answer;
		}
	}
	
	
	private void onLeftRecursionDetected(ParserExpression rule, Object input, int start, int stop, MemoEntry memoEntry)
	{
		// Left recursion has been detected
		memoEntry.bLeftRecursionDetected = true;
		
		if ( dependencies == null )
		{
			dependencies = new HashSet<MemoEntry>();
		}
		dependencies.add( memoEntry );
	}




	private ParseResult growLeftRecursiveParse(ParserExpression rule, Object input, int start, int stop, MemoEntry memoEntry, ParseResult answer)
	{
		while ( true )
		{
			// Put the answer and position into the memo entry for the next attempt
			memoEntry.answer = answer;
			// now, clear all dependents
			if ( memoEntry.dependents != null )
			{
				for (MemoEntry d: memoEntry.dependents)
				{
					memo.remove( new MemoKey( d.pos, d.rule ) );
				}
			}
			
			// Try re-evaluation
			ParseResult res = rule.evaluateString( this, (String)input, start, stop );
			// Fail or no additional characters consumed?
			if ( !res.isValid()  ||  res.end <= answer.end )
			{
				// Further applications will not improve matters
				break;
			}
			// This application of @rule improved matters; take the answer and position to use for the next iteration
			answer = res;
		}

		// Left recursive application is finished
		memoEntry.bLeftRecursionDetected = false;

		return answer;
	}
}


