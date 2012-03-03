//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.python.core.PySlice;

import BritefuryJ.JythonInterface.JythonIndex;
import BritefuryJ.JythonInterface.JythonSlice;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeOverlay;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;

public class LSOverlay extends LSContainer
{
	public LSOverlay()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public LSOverlay(ContainerStyleParams styleParams)
	{
		super( styleParams );
		
		layoutNode = new LayoutNodeOverlay( this );
	}


	
	//
	//
	// Child list access and modifications
	//
	//
	
	public void setChildren(LSElement items[])
	{
		setChildren( Arrays.asList( items ) );
	}
	
	public void setChildren(List<LSElement> items)
	{
		if ( registeredChildren.isEmpty() )
		{
			if ( items.size() > 0 )
			{
				// Set contents of @childEntries list
				registeredChildren.addAll( items );
		
				// Register added entries
				for (LSElement child: items)
				{
					registerChild( child );
				}
	
				onChildListModified();
				queueResize();
			}
		}
		else if ( registeredChildren.size() == 1  &&  items.size() <= 1 )
		{
			// Special case for when there is only 1 child
			if ( items.size() == 0 )
			{
				unregisterChild( registeredChildren.get( 0 ) );
				registeredChildren.clear();
				onChildListModified();
				queueResize();
			}
			else if ( items.size() == 1 )
			{
				LSElement prevChild = registeredChildren.get( 0 );
				LSElement newChild = items.get( 0 );
				
				if ( newChild != prevChild )
				{
					unregisterChild( prevChild );
					registeredChildren.set( 0, newChild );
					registerChild( newChild );
					onChildListModified();
					queueResize();
				}
			}
		}
		else
		{
			HashSet<LSElement> added, removed;
			
			added = new HashSet<LSElement>( items );
			removed = new HashSet<LSElement>( registeredChildren );
			added.removeAll( registeredChildren );
			removed.removeAll( items );

			
			// Unregister removed entries
			for (LSElement child: removed)
			{
				unregisterChild( child );
			}
			
			// Set contents of @childEntries list
			registeredChildren.clear();
			registeredChildren.addAll( items );
	
			// Register added entries
			for (LSElement child: added)
			{
				registerChild( child );
			}

			onChildListModified();
			queueResize();
		}
	}
	
	
	public void clear()
	{
		// Unregister removed entries
		for (LSElement child: registeredChildren)
		{
			unregisterChild( child );
		}
		
		// Set contents of @childEntries list
		registeredChildren.clear();

		onChildListModified();
		queueResize();
	}

	
	public boolean isSingleElementContainer()
	{
		return false;
	}
	
	

	
	
	public int size()
	{
		return registeredChildren.size();
	}
	
	public int __len__()
	{
		return size();
	}
	
	
	
	public LSElement get(int index)
	{
		return registeredChildren.get( index );
	}
	
	public LSElement __getitem__(int index)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement index out of range" );

		return get( index );
	}
	
	public LSElement[] __getitem__(PySlice slice)
	{
		LSElement[] in = new LSElement[registeredChildren.size()];
		
		for (int i = 0; i < in.length; i++)
		{
			in[i] = registeredChildren.get( i );
		}
		
		return (LSElement[])JythonSlice.arrayGetSlice( in, slice );
	}
	
	
	
	public void set(int index, LSElement child)
	{
		LSElement oldChild = registeredChildren.get( index );
		unregisterChild( oldChild );
		registeredChildren.set( index, child );
		registerChild( child );
		onChildListModified();
		queueResize();
	}
	
	public void __setitem__(int index, LSElement item)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement assignment index out of range" );

		set( index, item );
	}

	@SuppressWarnings("unchecked")
	public void __setitem__(PySlice slice, LSElement[] items)
	{
		HashSet<LSElement> oldEntrySet = new HashSet<LSElement>( registeredChildren );
		
		LSElement[] oldChildArray = registeredChildren.toArray( new LSElement[registeredChildren.size()] );
		LSElement[] newChildEntriesArray = (LSElement[])JythonSlice.arraySetSlice( oldChildArray, slice, items );
		
		HashSet<LSElement> newEntrySet = new HashSet<LSElement>( registeredChildren );
		
		
		HashSet<LSElement> removed = (HashSet<LSElement>)oldEntrySet.clone();
		removed.removeAll( newEntrySet );
		HashSet<LSElement> added = (HashSet<LSElement>)newEntrySet.clone();
		added.removeAll( oldEntrySet );
		
		
		for (LSElement child: removed)
		{
			unregisterChild( child );
		}

		registeredChildren.clear();
		registeredChildren.addAll( Arrays.asList( newChildEntriesArray ) );
		
		for (LSElement child: added)
		{
			registerChild( child );
		}
		
		
		onChildListModified();
		queueResize();
	}
	
	

	
	protected void replaceChildWithEmpty(LSElement child)
	{
		int index = registeredChildren.indexOf( child );
		set( index, new LSBlank() );
	}
		
	protected void replaceChild(LSElement child, LSElement replacement)
	{
		int index = registeredChildren.indexOf( child );
		set( index, replacement );
	}
	
	

	
	
	public List<LSElement> getChildren()
	{
		return registeredChildren;
	}






	@Override
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
	}
	
	@Override
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		super.handleDrawBackground( graphics, areaBox );
		super.handleDraw( graphics, areaBox );

		
		AABox2 clipBox = getLocalClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();
			for (LSElement child: registeredChildren)
			{
				if ( child.getAABoxInParentSpace().intersects( areaBox ) )
				{
					child.getLocalToParentXform().apply( graphics );
					AABox2 childBox = child.getParentToLocalXform().transform( areaBox );
					child.handleDrawBackground( graphics, childBox );
					child.handleDraw( graphics, childBox );
					graphics.setTransform( currentTransform );
				}
			}
		}
	}
}
