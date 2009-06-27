//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserOld;

import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class StructuralObject extends ParserExpression
{
	private DMObjectClass cls;
	
	
	public StructuralObject(DMObjectClass cls)
	{
		super();
		
		this.cls = cls;
	}
	
	
	protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
	{
		start = state.skipJunkChars( input, start );
		
		Object value[] = input.matchStructuralNode( start );
		
		if ( value != null )
		{
			Object x = value[0];
			if ( x instanceof DMObject )
			{
				DMObject dx = (DMObject)x;
				if ( dx.isInstanceOf( cls ) )
				{
					return new ParseResult( dx, start, start + 1 );
				}
			}
		}
		
		return ParseResult.failure( start );
	}
	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof StructuralObject )
		{
			StructuralObject sx = (StructuralObject)x;
			
			return cls == sx.cls;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "StructuralNode()";
	}

}
