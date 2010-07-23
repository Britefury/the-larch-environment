//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.FragmentTree;

import java.util.Iterator;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPFragment;

public class FragmentIterator implements Iterator<DPFragment>
{
	private static class StackEntry
	{
		public DPElement element;
		public int childIndex;
		public StackEntry prev;
		
		public StackEntry(DPElement element, int childIndex, StackEntry prev)
		{
			this.element = element;
			this.childIndex = childIndex;
			this.prev = prev;
		}
	}
	
	private DPFragment root;
	private StackEntry stack;
	private DPFragment next;
	
	
	public FragmentIterator(DPFragment root)
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
	public DPFragment next()
	{
		DPFragment fragment = next;
		next = advance( false );
		return fragment;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
	
	
	private DPFragment advance(boolean bFirstAdvance)
	{
		while ( true )
		{
			if ( stack == null )
			{
				throw new RuntimeException( "Stack should never empty" );
			}
			
			DPElement element = stack.element;

			// Check if we have found a new fragment
			if ( element instanceof DPFragment )
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
					return (DPFragment)element;
				}
			}
			
			if ( element instanceof DPContainer )
			{
				DPContainer container = (DPContainer)element;
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
	
	
	
	private void push(DPElement element)
	{
		stack = new StackEntry( element, 0, stack );
	}
	
	
	private void pop()
	{
		stack = stack.prev;
	}
}
