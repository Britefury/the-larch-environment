//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.util.ArrayDeque;
import java.util.List;

public class ElementSearchBredthFirst
{
	public static LSElement search(LSElement subtreeRoot, ElementFilter filter, boolean bForwards)
	{
		ArrayDeque<LSElement> queue = new ArrayDeque<LSElement>();

		queue.add( subtreeRoot );
		
		while ( !queue.isEmpty() )
		{
			LSElement e = queue.removeFirst();
			
			// Test e
			if ( filter.testElement( e ) )
			{
				return e;
			}
			
			List<LSElement> children = e.getSearchChildren();
			
			if ( bForwards )
			{
				queue.addAll( children );
			}
			else
			{
				for (int i = 1; i <= children.size(); i++)
				{
					queue.add( children.get( children.size() - i ) );
				}
			}
		}
		
		return null;
	}
	
	public static LSElement search(LSElement subtreeRoot, ElementFilter filter)
	{
		return search( subtreeRoot, filter, true );
	}

	
	
	public static LSElement searchByType(LSElement subtreeRoot, final Class<?> elementClass, boolean bForwards)
	{
		ElementFilter filter = new ElementFilter()
		{
			public boolean testElement(LSElement element)
			{
				return elementClass.isInstance( element );
			}
		};
		
		return search( subtreeRoot, filter, bForwards );
	}

	public static LSElement searchByType(LSElement subtreeRoot, final Class<?> elementClass)
	{
		return searchByType( subtreeRoot, elementClass, true );
	}
}
