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


public class ParserState
{
	private static class MemoEntry
	{
		public ParserExpression rule;
		public Object answer;
		public int pos;
		public boolean bEvaluating, bLeftRecursionDetected;
		public HashMap<ParserExpression, LeftRecursiveApplication> lrApplications;
		int growingLRParseCount;
	};
	
	
	private static class RuleInvocation
	{
		public ParserExpression rule;
		public MemoEntry memoEntry;
		public RuleInvocation outerInvocation;
	}
	
	
	private static class LeftRecursiveApplication
	{
		public ParserExpression rule;
		public MemoEntry memoEntry;
		public HashSet<MemoEntry> involvedSet, evalSet;
	}
	
	
	
	private HashMap<Integer, HashMap<ParserExpression, MemoEntry> > memo;
	private RuleInvocation ruleInvocationStack;
	private Pattern ignoreChars;
	
	
	public ParserState(String ignoreCharsRegex)
	{
		this.memo = new HashMap<Integer, HashMap<ParserExpression, MemoEntry> >();
		ignoreChars = Pattern.compile( ignoreCharsRegex );
	}
	
	
	public int skipIgnoredChars(String input, int start, int stop)
	{
		Matcher m = ignoreChars.matcher( input.subSequence( start, stop ) );
		m.find();
		return m.end();
	}
}
