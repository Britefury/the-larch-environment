//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BritefuryJ.DocPresent.StreamValue.StreamValueAccessor;
import BritefuryJ.ParserHelpers.TraceNode;
import BritefuryJ.Utils.HashUtils;


public class ParserState
{
	private static class SourceKey
	{
		private Object input;
		private ParserExpression.Mode mode;
		private int hash;
		
		public SourceKey(Object input, ParserExpression.Mode mode)
		{
			this.input = input;
			this.mode = mode;
			this.hash = HashUtils.doubleHash( System.identityHashCode( input ), mode.hashCode() );
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
			
			if ( x instanceof SourceKey )
			{
				SourceKey sx = (SourceKey)x;
				return input == sx.input  &&  mode == sx.mode;
			}
			else
			{
				return false;
			}
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
	
	
	
	
	private static class MemoEntry
	{
		public SourceKey sourceKey;
		public MemoKey memoKey;
		public ParseResult answer;
		public boolean bEvaluating, bLeftRecursionDetected;
		public HashSet<MemoEntry> dependents;		// dependents; for a left-recursive application
		
		
		public MemoEntry(SourceKey sourceKey, MemoKey memoKey)
		{
			this.sourceKey = sourceKey;
			this.memoKey = memoKey;
			answer = ParseResult.failure( memoKey.pos );
			bEvaluating = bLeftRecursionDetected = false;
		}
		
		public void addDependent(MemoEntry dep)
		{
			if ( dependents == null )
			{
				dependents = new HashSet<MemoEntry>();
			}
			if ( dep.memoKey.pos == memoKey.pos )
			{
				dependents.add( dep );
			}
		}
	}
	
	
	private HashMap<SourceKey, HashMap<MemoKey, MemoEntry>> table;
	private SourceKey currentSourceKey;
	private Object currentInput;
	private ParserExpression.Mode currentMode;
	private HashMap<MemoKey, MemoEntry> currentMemo;
	
	private Pattern junkPattern;
	private HashSet<MemoEntry> dependencies;
	protected TraceNode traceStack;
	protected boolean bTracingEnabled;
	protected ParseAction delegateAction;
	
	
	
	public ParserState(String junkRegex, ParseAction delegateAction)
	{
		this.table = new HashMap<SourceKey, HashMap<MemoKey, MemoEntry>>();
		junkPattern = Pattern.compile( junkRegex );
		dependencies = null;
		this.delegateAction = delegateAction;
	}
	
	
	public int skipJunkChars(String input, int start)
	{
		if ( start < input.length() )
		{
			Matcher m = junkPattern.matcher( input.subSequence( start, input.length() ) );
			if ( m.find() )
			{
				return start + m.end();
			}
		}

		return start;
	}
	
	public int skipJunkChars(StreamValueAccessor input, int start)
	{
		if ( start <= input.length() )
		{
			return input.skipRegEx( start, junkPattern );
		}
		else
		{
			return start;
		}
	}
	
	
	protected void enableTrace()
	{
		bTracingEnabled = true;
		traceStack = null;
	}
	

	
	ParseResult memoisedMatchNode(ParserExpression rule, Object input)
	{
		return memoisedMatch( rule, ParserExpression.Mode.NODE, input, 0 );
	}
	
	ParseResult memoisedMatchString(ParserExpression rule, String input, int start)
	{
		return memoisedMatch( rule, ParserExpression.Mode.STRING, input, start );
	}
	
	ParseResult memoisedMatchStream(ParserExpression rule, StreamValueAccessor input, int start)
	{
		return memoisedMatch( rule, ParserExpression.Mode.STREAM, input, start );
	}
	
	ParseResult memoisedMatchList(ParserExpression rule, List<Object> input, int start)
	{
		return memoisedMatch( rule, ParserExpression.Mode.LIST, input, start );
	}
	

	ParseResult memoisedMatch(ParserExpression rule, ParserExpression.Mode mode, Object input, int start)
	{
		if ( input != currentInput  ||  mode != currentMode )
		{
			currentSourceKey = new SourceKey( input, mode );
			currentInput = input;
			currentMode = mode;

			currentMemo = table.get( currentSourceKey );
			if ( currentMemo == null )
			{
				currentMemo = new HashMap<MemoKey, MemoEntry>();
				table.put( currentSourceKey, currentMemo );
			}
		}
		
		MemoKey memoKey = new MemoKey( start, rule );
		MemoEntry memoEntry = currentMemo.get( memoKey );
		
		if ( memoEntry == null )
		{
			// Create the memo-entry, and memoise
			memoEntry = new MemoEntry( currentSourceKey, memoKey );
			currentMemo.put( memoKey, memoEntry );
			
			
			// Take a copy of the dependencies, and clear the global list
			HashSet<MemoEntry> deps = dependencies;
			dependencies = null;
			
			
			// Mark the rule is 'evaluating'
			memoEntry.bEvaluating = true;
			

			// Evaluate the rule, at position @start
			ParseResult answer = applyRule( rule, mode, input, start );
			

			// Merge dependency lists, into global list
			if ( deps != null )
			{
				if ( dependencies == null )
				{
					dependencies = deps;
				}
				else
				{
					dependencies.addAll( deps );
				}
			}
			// Register @memoEntry as a dependent of all current dependencies
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
				answer = growLeftRecursiveParse( rule, mode, input, start, memoEntry, answer );
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
				onLeftRecursionDetected( memoEntry );
			}
			return memoEntry.answer;
		}
	}
	
	
	private void onLeftRecursionDetected(MemoEntry memoEntry)
	{
		// Left recursion has been detected
		memoEntry.bLeftRecursionDetected = true;
		
		if ( dependencies == null )
		{
			dependencies = new HashSet<MemoEntry>();
		}
		dependencies.add( memoEntry );
	}




	private ParseResult growLeftRecursiveParse(ParserExpression rule, ParserExpression.Mode mode, Object input, int start, MemoEntry memoEntry, ParseResult answer)
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
					if ( d.sourceKey == currentSourceKey )
					{
						currentMemo.remove( d.memoKey );
					}
					else
					{
						table.get( d.sourceKey ).remove( d.memoKey );
					}
				}
			}
			memoEntry.dependents = null;
			
			// Try re-evaluation
			ParseResult res = applyRule( rule, mode, input, start );
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
	
	
	
	@SuppressWarnings("unchecked")
	private ParseResult applyRule(ParserExpression rule, ParserExpression.Mode mode, Object input, int start)
	{
		if ( mode == ParserExpression.Mode.STRING )
		{
			return rule.handleStringChars( this, (String)input, start );
		}
		else if ( mode == ParserExpression.Mode.STREAM )
		{
			return rule.handleStreamItems( this, (StreamValueAccessor)input, start );
		}
		else if ( mode == ParserExpression.Mode.NODE )
		{
			return rule.handleNode( this, input );
		}
		else if ( mode == ParserExpression.Mode.LIST )
		{
			return rule.handleListItems( this, (List<Object>)input, start );
		}
		else
		{
			throw new RuntimeException( "Invalid mode" );
		}
	}
}


