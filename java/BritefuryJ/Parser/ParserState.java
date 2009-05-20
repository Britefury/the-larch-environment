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
	
	
	
	
	private static class MemoEntry
	{
		public MemoKey key;
		public ParseResult answer;
		public boolean bEvaluating, bLeftRecursionDetected;
		public HashSet<MemoEntry> dependents;		// dependents; for a left-recursive application
		
		
		public MemoEntry(MemoKey key)
		{
			this.key = key;
			answer = ParseResult.failure( key.pos );
			bEvaluating = bLeftRecursionDetected = false;
		}
		
		public void addDependent(MemoEntry dep)
		{
			if ( dependents == null )
			{
				dependents = new HashSet<MemoEntry>();
			}
			if ( dep.key.pos == key.pos )
			{
				dependents.add( dep );
			}
		}
	};
	
	
	private HashMap<MemoKey, MemoEntry> memo;
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
			memoEntry = new MemoEntry( key );
			memo.put( key, memoEntry );
			
			
			// Take a copy of the dependencies, and clear the global list
			HashSet<MemoEntry> deps = dependencies != null  ?  (HashSet<MemoEntry>)dependencies.clone()  :  null;
			if ( dependencies != null )
			{
				dependencies.clear();
			}
			
			
			// Mark the rule is 'evaluating'
			memoEntry.bEvaluating = true;
			

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
			
			// Rule no longer evaluating, got an answer
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
					memo.remove( d.key );
				}
			}
			memoEntry.dependents = null;
			
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


