//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.util.List;
import java.util.Stack;

public class TreeTraversal
{
	public static class ElementIsNotInSubtreeException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	public static interface ElementValueFn
	{
		public Object apply(LSElement element);
	}
	
	public static interface ElementValueAccumulatorFn
	{
		public Object accumulate(LSElement element, Object current);
	}
	
	public static interface BranchChildrenFn
	{
		public List<LSElement> getChildrenOf(LSContainer element);
	}
	
	
	
	
	
	/*
	 * Searches a tree, starting at element, going up through its ancestry, <code>applying value_fn</code> along the way, returning the first non-null value
	 * Stops at ancestor if it is not None
	 * 
	 * @param	element	element at which the search starts at
	 * @param	ancestor	element at which the search stops - this element can contribute the value
	 * @param	valueFn	element value function
	 * @returns			non-null value returned by valueFn
	 * @throws			ElementIsNotInSubtreeException if ancestor is non-null and not reached
	 * 
	 * If ancestor is not null, no value is found, and ancestor is not reached ElementIsNotInSubtreeException will be thrown
	 */
	public static Object findInheritedValueForElement(LSElement element, LSContainer ancestor, ElementValueFn valueFn)
	{
		while ( element != null )
		{
			Object value = valueFn.apply( element );
			if ( value != null )
			{
				return value;
			}
			if ( element == ancestor )
			{
				return null;
			}
			
			element = element.parent;
		}
		
		if ( ancestor == null )
		{
			return null;
		}
		else
		{
			throw new ElementIsNotInSubtreeException();
		}
	}
	

	/*
	 * Accumulates a value, traversing from element upwards through the tree, either to <code>ancestor</code> or to the root. The value is accumulated
	 * through repeated application of accumulatorFn.
	 * 
	 * @param	element		element at which the search starts at
	 * @param	ancestor		element at which the search stops - this element can contribute the value
	 * @param	accumulatorFn	element value accumulation function
	 * @returns				non-null value returned by valueFn
	 * @throws				ElementIsNotInSubtreeException if ancestor is non-null and not reached
	 * 
	 * If ancestor is not null, no value is found, and ancestor is not reached ElementIsNotInSubtreeException will be thrown
	 */
	public static Object accumulateInheritedValueForElement(LSElement element, LSContainer ancestor, ElementValueAccumulatorFn accumulatorFn, Object initialValue)
	{
		Object value = initialValue;
		
		while ( element != null )
		{
			value = accumulatorFn.accumulate( element, value );
			if ( element == ancestor )
			{
				return value;
			}
			
			element = element.parent;
		}
		
		if ( ancestor == null )
		{
			return value;
		}
		else
		{
			throw new ElementIsNotInSubtreeException();
		}
	}
	
	public static Object accumulateInheritedValueForElement(LSElement element, LSContainer ancestor, ElementValueAccumulatorFn accumulatorFn)
	{
		return accumulateInheritedValueForElement( element, ancestor, accumulatorFn, null );
	}
	
	
	/*
	 * Similar to <code>accumulateInheritedValueForElement<code>, except that <code>ancestor</code> will not have the accumulator function
	 * applied to it.
	 * 
	 * @param	element		element at which the search starts at
	 * @param	ancestor		element at which the search stops - this element can contribute the value
	 * @param	accumulatorFn	element value accumulation function
	 * @returns				non-null value returned by valueFn
	 * @throws				ElementIsNotInSubtreeException if ancestor is non-null and not reached
	 * 
	 * If ancestor is not null, no value is found, and ancestor is not reached ElementIsNotInSubtreeException will be thrown
	 */
	public static Object accumulateInheritedValueAlongEdgesForElement(LSElement element, LSContainer ancestor, ElementValueAccumulatorFn accumulatorFn, Object initialValue)
	{
		Object value = initialValue;
		
		while ( element != null )
		{
			if ( element == ancestor )
			{
				return value;
			}
			value = accumulatorFn.accumulate( element, value );
			
			element = element.parent;
		}
		
		if ( ancestor == null )
		{
			return value;
		}
		else
		{
			throw new ElementIsNotInSubtreeException();
		}
	}
	
	public static Object accumulateInheritedValueAlongEdgesForElement(LSElement element, LSContainer ancestor, ElementValueAccumulatorFn accumulatorFn)
	{
		return accumulateInheritedValueAlongEdgesForElement( element, ancestor, accumulatorFn, null );
	}
	
	
	
	
	
	/*
	 * Find the first element in the subtree rooted at <code>subtreeRoot</code> that passes the filter <code>elementFilter</code>.
	 * 
	 * @param		subtreeRoot		The root of the subtree to search
	 * @param		branchChildrenFn	A function to acquire the child elements beneath an element
	 * @param		elementFilter		The element filter used to test elements
	 * @param		branchFilter		Branch filter - element filter that determines if the search should descend into a branch.
	 */
	public static LSElement findFirstElementInSubtree(LSElement subtreeRoot, BranchChildrenFn branchChildrenFn, ElementFilter elementFilter, ElementFilter branchFilter)
	{
		Stack<LSElement> stack = new Stack<LSElement>();
		stack.add( subtreeRoot );
		
		while ( !stack.isEmpty() )
		{
			LSElement e = stack.pop();
			if ( elementFilter.testElement( e ) )
			{
				return e;
			}
			else
			{
				if ( e instanceof LSContainer  &&  ( branchFilter == null  ||  branchFilter.testElement( e ) ) )
				{
					List<LSElement> ch = branchChildrenFn.getChildrenOf( (LSContainer)e );
					for (int i = ch.size() - 1; i >= 0; i--)
					{
						stack.add( ch.get( i ) );
					}
				}
			}
		}
		
		return null;
	}
	
	public static LSElement findFirstElementInSubtree(LSElement subtreeRoot, BranchChildrenFn branchChildrenFn, ElementFilter elementFilter)
	{
		return findFirstElementInSubtree( subtreeRoot, branchChildrenFn, elementFilter, null );
	}

	
	/*
	 * Find the last element in the subtree rooted at <code>subtreeRoot</code> that passes the filter <code>elementFilter</code>.
	 * 
	 * @param		subtreeRoot		The root of the subtree to search
	 * @param		branchChildrenFn	A function to acquire the child elements beneath an element
	 * @param		elementFilter		The element filter used to test elements
	 * @param		branchFilter		Branch filter - element filter that determines if the search should descend into a branch.
	 */
	public static LSElement findLastElementInSubtree(LSElement subtreeRoot, BranchChildrenFn branchChildrenFn, ElementFilter elementFilter, ElementFilter branchFilter)
	{
		Stack<LSElement> stack = new Stack<LSElement>();
		stack.add( subtreeRoot );
		
		while ( !stack.isEmpty() )
		{
			LSElement e = stack.pop();
			if ( elementFilter.testElement( e ) )
			{
				return e;
			}
			else
			{
				if ( e instanceof LSContainer  &&  ( branchFilter == null  ||  branchFilter.testElement( e ) ) )
				{
					stack.addAll( branchChildrenFn.getChildrenOf( (LSContainer)e ) );
				}
			}
		}
		
		return null;
	}
	
	public static LSElement findLastElementInSubtree(LSElement subtreeRoot, BranchChildrenFn branchChildrenFn, ElementFilter elementFilter)
	{
		return findLastElementInSubtree( subtreeRoot, branchChildrenFn, elementFilter, null );
	}
	
	
	
	
	public static LSElement previousElement(LSElement element, LSContainer subtreeRoot, BranchChildrenFn branchChildrenFn, ElementFilter elementFilter, boolean testAncestors)
	{
		// Until found:
		while ( true )
		{
			List<LSElement> children = null;
			int index = -1;
			LSContainer parent = null;
			
			// Navigate through ancestors of @element, until we find one that has a children in the backwards direction
			while ( true )
			{
				parent = element.parent;
				if ( parent == null  ||  element == subtreeRoot )
				{
					return null;
				}
				if ( testAncestors  &&  elementFilter.testElement( parent ) )
				{
					return parent;
				}
				
				children = branchChildrenFn.getChildrenOf( parent );
				index = children.indexOf( element );
				if ( index > 0 )
				{
					break;
				}
				else
				{
					element = parent;
				}
			}
			
			// @parent contains an element that we can navigate backwards through
			// @index is the index of the sub-tree in which @element can be found
			
			// For each child of @parent before index, moving backwards:
			for (int i = index - 1; i >= 0; i--)
			{
				LSElement subtree = children.get( i );
				LSElement x = findLastElementInSubtree(subtree, branchChildrenFn, elementFilter);
				if ( x != null )
				{
					return x;
				}
			}
			
			// Failed to find an element - traverse up the tree
			element = parent;
		}
	}
	
	public static LSElement previousElement(LSElement element, LSContainer subtreeRoot, BranchChildrenFn branchChildrenFn, ElementFilter elementFilter)
	{
		return previousElement( element, subtreeRoot, branchChildrenFn, elementFilter, false );
	}
	

	public static LSElement nextElement(LSElement element, LSContainer subtreeRoot, BranchChildrenFn branchChildrenFn, ElementFilter elementFilter, boolean testAncestors)
	{
		// Until found:
		while ( true )
		{
			List<LSElement> children = null;
			int index = -1;
			LSContainer parent = null;
			
			// Navigate through ancestors of @element, until we find one that has a children in the backwards direction
			while ( true )
			{
				parent = element.parent;
				if ( parent == null  ||  element == subtreeRoot )
				{
					return null;
				}
				if ( testAncestors  &&  elementFilter.testElement( parent ) )
				{
					return parent;
				}
				
				children = branchChildrenFn.getChildrenOf( parent );
				index = children.indexOf( element );
				if ( index < children.size() - 1 )
				{
					break;
				}
				else
				{
					element = parent;
				}
			}
			
			// @parent contains an element that we can navigate backwards through
			// @index is the index of the sub-tree in which @element can be found
			
			// For each child of @parent before index, moving backwards:
			for (int i = index + 1; i < children.size(); i++)
			{
				LSElement subtree = children.get( i );
				LSElement x = findFirstElementInSubtree(subtree, branchChildrenFn, elementFilter);
				if ( x != null )
				{
					return x;
				}
			}
			
			// Failed to find an element - traverse up the tree
			element = parent;
		}
	}
	
	public static LSElement nextElement(LSElement element, LSContainer subtreeRoot, BranchChildrenFn branchChildrenFn, ElementFilter elementFilter)
	{
		return nextElement( element, subtreeRoot, branchChildrenFn, elementFilter, false );
	}
}
