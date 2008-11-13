//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.HashMap;

import BritefuryJ.ParserSupport.DebugNode;
import BritefuryJ.ParserSupport.DebugParseResultInterface;

public class DebugMatchResult extends MatchResult implements DebugParseResultInterface
{
	public DebugNode debugNode;
	
	
	protected DebugMatchResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid, HashMap<String, Object> bindings, DebugNode debugNode)
	{
		super( value, begin, end, bSuppressed, bValid, bindings );
		this.debugNode = debugNode;
	}
	

	public DebugNode getDebugNode()
	{
		return debugNode;
	}
}
