//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent;

import java.util.List;
import java.util.Stack;

public class ElementSearchPreOrderDepthFirst
{
	public static DPElement search(DPElement subtreeRoot, ElementFilter filter, boolean bForwards)
	{
		Stack<DPElement> stack = new Stack<DPElement>();

		stack.push( subtreeRoot );
		
		while ( !stack.isEmpty() )
		{
			DPElement e = stack.pop();
			
			// PRE-ORDER: test e
			if ( filter.testElement( e ) )
			{
				return e;
			}
			
			List<DPElement> children = e.getSearchChildren();
			
			if ( bForwards )
			{
				for (int i = 1; i <= children.size(); i++)
				{
					stack.push( children.get( children.size() - i ) );
				}
			}
			else
			{
				stack.addAll( children );
			}
		}
		
		return null;
	}

	public static DPElement search(DPElement subtreeRoot, ElementFilter filter)
	{
		return search( subtreeRoot, filter, true );
	}
	
	public static DPElement searchByType(DPElement subtreeRoot, final Class<?> elementClass, boolean bForwards)
	{
		ElementFilter filter = new ElementFilter()
		{
			public boolean testElement(DPElement element)
			{
				return elementClass.isInstance( element );
			}
		};
		
		return search( subtreeRoot, filter, bForwards );
	}

	public static DPElement searchByType(DPElement subtreeRoot, final Class<?> elementClass)
	{
		return searchByType( subtreeRoot, elementClass, true );
	}
}
