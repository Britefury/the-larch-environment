//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Map;

import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.DebugParseResultInterface;

public class DebugParseResult extends ParseResult implements DebugParseResultInterface
{
	public DebugNode debugNode;
	
	
	protected DebugParseResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid, boolean bMerge, Map<String, Object> bindings, DebugNode debugNode)
	{
		super( value, begin, end, bSuppressed, bValid, bMerge, bindings );
		this.debugNode = debugNode;
	}
	

	public DebugNode getDebugNode()
	{
		return debugNode;
	}
}
