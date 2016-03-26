//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.util.List;
import java.util.Stack;

public class ElementSearchPreOrderDepthFirst
{
	public static LSElement search(LSElement subtreeRoot, ElementFilter filter, boolean bForwards)
	{
		Stack<LSElement> stack = new Stack<LSElement>();

		stack.push( subtreeRoot );
		
		while ( !stack.isEmpty() )
		{
			LSElement e = stack.pop();
			
			// PRE-ORDER: test e
			if ( filter.testElement( e ) )
			{
				return e;
			}
			
			List<LSElement> children = e.getSearchChildren();
			
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
