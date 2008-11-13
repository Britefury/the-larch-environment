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


class ParserState
{
	private static class MemoEntry
	{
		public ParserExpression rule;
		public ParseResult answer;
		public boolean bEvaluating, bLeftRecursionDetected;
		public HashMap<ParserExpression, LeftRecursiveApplication> lrApplications;
		public int growingLRParseCount;
		
		
		public MemoEntry(ParserExpression rule, int start)
		{
			this.rule = rule;
			answer = ParseResult.failure( start );
			bEvaluating = bLeftRecursionDetected = false;
			lrApplications = new HashMap<ParserExpression, LeftRecursiveApplication>();
			growingLRParseCount = 0;
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
	
	
	private static class LeftRecursiveApplication
	{
		public ParserExpression rule;
		public MemoEntry memoEntry;
		public HashSet<MemoEntry> involvedSet, evalSet;
		
		public LeftRecursiveApplication(ParserExpression rule, MemoEntry memoEntry)
		{
			this.rule = rule;
			this.memoEntry = memoEntry;
			involvedSet = new HashSet<MemoEntry>();
			evalSet = new HashSet<MemoEntry>();
		}
	}
	
	
	
	private HashMap<Integer, HashMap<ParserExpression, MemoEntry> > memo;
	private RuleInvocation ruleInvocationStack;
	private Pattern junkPattern;
	protected DebugNode debugStack;
	protected boolean bDebuggingEnabled;
	
	
	
	public ParserState(String junkRegex)
	{
		this.memo = new HashMap<Integer, HashMap<ParserExpression, MemoEntry> >();
		junkPattern = Pattern.compile( junkRegex );
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
		MemoEntry memoEntry = recall( rule, input, start, stop );
		
		if ( memoEntry == null )
		{
			// Create the memo-entry, and memoise
			memoEntry = new MemoEntry( rule, start );
			Integer iStart = new Integer( start );
			HashMap<ParserExpression, MemoEntry> posMemo = memo.get( iStart );
			
			if ( posMemo == null )
			{
				posMemo = new HashMap<ParserExpression, MemoEntry>();
				memo.put( iStart, posMemo );
			}
			
			posMemo.put( rule, memoEntry );
			
			
			HashMap<ParserExpression, MemoEntry> posMemoCopy = (HashMap<ParserExpression, MemoEntry>)posMemo.clone();
			
			// Create a rule invocation record, and push onto the rule invocation stack
			ruleInvocationStack = new RuleInvocation( rule, memoEntry, ruleInvocationStack );
			memoEntry.bEvaluating = true;
			
			ParseResult answer = rule.evaluateString( this, (String)input, start, stop );
			
			if ( memoEntry.bLeftRecursionDetected )
			{
				// Grow the left recursive parse
				answer = growLeftRecursiveParse( rule, input, start, stop, memoEntry, answer );
				// Restore the memo
				memo.put( iStart, posMemoCopy );
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
				// recursive; specifically left-recursive since we are at the same position in the stream
				if ( memoEntry.growingLRParseCount > 0 )
				{
					// The left-recursive parse is being grown right now
					onLeftRecursionInnerReapplication( rule, input, start, stop, memoEntry );
				}
				else
				{
					// Left recursion has been detected
					onLeftRecursionDetected( rule, input, start, stop, memoEntry );
				}
			}
			return memoEntry.answer;
		}
	}
	
	
	private MemoEntry recall(ParserExpression rule, Object input, int start, int stop)
	{
		// Get the memo-entry from the memo table
		HashMap<ParserExpression, MemoEntry> posMemo = memo.get( new Integer( start ) );
		MemoEntry memoEntry = null;
		
		if ( posMemo != null )
		{
			memoEntry = posMemo.get( rule );
		}
		
		
		if ( memoEntry != null  &&  memoEntry.lrApplications.size() > 0 )
		{
			// This rule application is involved in a left-recursive application of a rule
			boolean bInEvalSet = false;
			for (LeftRecursiveApplication lrApplication: memoEntry.lrApplications.values())
			{
				// Remove from the evaluation set
				if ( lrApplication.evalSet.contains( memoEntry ) )
				{
					lrApplication.evalSet.remove( memoEntry );
					bInEvalSet = true;
				}
			}
			
			
			if ( bInEvalSet )
			{
				// Create a rule invocation record, and push onto the rule invocation stack
				ruleInvocationStack = new RuleInvocation( rule, memoEntry, ruleInvocationStack );
				memoEntry.bEvaluating = true;
				// Just evaluate it, and fill in the memo entry with the new values
				ParseResult res = rule.evaluateString( this, (String)input, start, stop );
				memoEntry.answer = res;
				// Pop the rule invocation off the rule invocation stack
				ruleInvocationStack = ruleInvocationStack.outerInvocation;
				memoEntry.bEvaluating = false;
			}
		}
		
		return memoEntry;
	}
	
	
	private void onLeftRecursionDetected(ParserExpression rule, Object input, int start, int stop, MemoEntry memoEntry)
	{
		// Left recursion has been detected
		memoEntry.bLeftRecursionDetected = true;
		
		// Create a left-recursive application record, if one does not already exist
		LeftRecursiveApplication lrApplication = memoEntry.lrApplications.get( rule );
		if ( lrApplication == null )
		{
			lrApplication = new LeftRecursiveApplication( rule, memoEntry );
			memoEntry.lrApplications.put( rule, lrApplication );
		}
		
		
		// No rule invocation record has been created for this invocation yet, so the current top of
		// the stack points to the outer invocation
		RuleInvocation invocation = ruleInvocationStack;
	
		// Walk up the stack until @memoEntry is encountered again, adding each entry to the involvedSet set
		// Simply walking until we reach the bottom of the stack is not sufficient; in the case where the outer
		// invocation of @rule is invoked inside another left-recursive rule, B, we will reach the outer invocation of B,
		// not the outer invocation of @rule. Stopping when we reach the same memo-entry will ensure that we
		// stop at the outer invocation of @rule.
		while ( invocation != null  &&  invocation.memoEntry != memoEntry )
		{
			lrApplication.involvedSet.add( invocation.memoEntry );
			invocation.memoEntry.lrApplications.put( rule, lrApplication );
			invocation = invocation.outerInvocation;
		}
	}



	private void onLeftRecursionInnerReapplication(ParserExpression rule, Object input, int start, int stop, MemoEntry memoEntry)
	{
		// Create a left-recursive application record, if one does not already exist
		LeftRecursiveApplication lrApplication = memoEntry.lrApplications.get( rule );
		assert( lrApplication != null );
		
		// No rule invocation record has been created for this invocation yet, so the current top of
		// the stack points to the outer invocation
		RuleInvocation invocation = ruleInvocationStack;
	
		// Walk up the stack until @memoEntry is encountered again, adding each entry to the involvedSet set
		// Simply walking until we reach the bottom of the stack is not sufficient; in the case where the outer
		// invocation of @rule is invoked inside another left-recursive rule, B, we will reach the outer invocation of B,
		// not the outer invocation of @rule. Stopping when we reach the same memo-entry will ensure that we
		// stop at the outer invocation of @rule.
		while ( invocation != null  &&  invocation.memoEntry != memoEntry )
		{
			lrApplication.involvedSet.add( invocation.memoEntry );
			invocation = invocation.outerInvocation;
		}
	}



	@SuppressWarnings("unchecked")
	private ParseResult growLeftRecursiveParse(ParserExpression rule, Object input, int start, int stop, MemoEntry memoEntry, ParseResult answer)
	{
		memoEntry.growingLRParseCount++;
		LeftRecursiveApplication lrApplication = memoEntry.lrApplications.get( rule );
		assert( lrApplication != null );
		
		
		while ( true )
		{
			// Put the answer and position into the memo entry for the next attempt
			memoEntry.answer = answer;
			// Prepare the evaluation set
			lrApplication.evalSet = (HashSet<MemoEntry>)lrApplication.involvedSet.clone();
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
		memoEntry.growingLRParseCount--;
		memoEntry.bLeftRecursionDetected = false;

		return answer;
	}
}


