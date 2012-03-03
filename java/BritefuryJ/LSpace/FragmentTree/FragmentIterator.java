//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.FragmentTree;

import java.util.Iterator;

import BritefuryJ.LSpace.LSContainer;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSFragment;

public class FragmentIterator implements Iterator<LSFragment>
{
	private static class StackEntry
	{
		public LSElement element;
		public int childIndex;
		public StackEntry prev;
		
		public StackEntry(LSElement element, int childIndex, StackEntry prev)
		{
			this.element = element;
			this.childIndex = childIndex;
			this.prev = prev;
		}
	}
	
	private LSFragment root;
	private StackEntry stack;
	private LSFragment next;
	
	
	public FragmentIterator(LSFragment root)
	{
		this.root = root;
		stack = new StackEntry( root, 0, null );
		next = advance( true );
	}

	
	@Override
	public boolean hasNext()
	{
		return next != null;
	}

	@Override
	public LSFragment next()
	{
		LSFragment fragment = next;
		next = advance( false );
		return fragment;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
	
	
	private LSFragment advance(boolean bFirstAdvance)
	{
		while ( true )
		{
			if ( stack == null )
			{
				throw new RuntimeException( "Stack should never empty" );
			}
			
			LSElement element = stack.element;

			// Check if we have found a new fragment
			if ( element instanceof LSFragment )
			{
				if ( element == root )
				{
					if ( !bFirstAdvance )
					{
						return null;
					}
				}
				else
				{
					pop();
					stack.childIndex++;
					return (LSFragment)element;
				}
			}
			
			if ( element instanceof LSContainer )
			{
				LSContainer container = (LSContainer)element;
				if ( stack.childIndex < container.getChildren().size() )
				{
					push( container.getChildren().get( stack.childIndex ) );
				}
				else
				{
					pop();
					stack.childIndex++;
				}
			}
			else
			{
				pop();
				stack.childIndex++;
			}
		}
	}
	
	
	
	private void push(LSElement element)
	{
		stack = new StackEntry( element, 0, stack );
	}
	
	
	private void pop()
	{
		stack = stack.prev;
	}
}
