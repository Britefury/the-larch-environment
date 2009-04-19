//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import BritefuryJ.DocPresent.DPContainerSequence;
import BritefuryJ.DocPresent.DPWidget;


public abstract class SequenceBranchElement extends BranchElement
{
	protected ArrayList<Element> children;
	
	
	
	protected SequenceBranchElement(DPContainerSequence container)
	{
		super( container );
		
		children = new ArrayList<Element>();
	}


	public DPContainerSequence getWidget()
	{
		return (DPContainerSequence)widget;
	}



	public void setChildren(List<Element> xs)
	{
		ArrayList<DPWidget> childWidgets = new ArrayList<DPWidget>();

		childWidgets.ensureCapacity( xs.size() );
		for (Element x: xs)
		{
			childWidgets.add( x.getWidget() );
		}
		
		HashSet<Element> added, removed;
		
		added = new HashSet<Element>( xs );
		removed = new HashSet<Element>( children );
		added.removeAll( children );
		removed.removeAll( xs );
		
		
		for (Element x: removed)
		{
			x.setParent( null );
			x.setElementTree( null );
		}
		
		children.clear();
		children.addAll( xs );
		
		getWidget().setChildren( childWidgets );

		for (Element x: added)
		{
			x.setParent( this );
			x.setElementTree( tree );
		}
		
		onChildListChanged();
	}
	

	
	public List<Element> getChildren()
	{
		return children;
	}
	
	
	protected void getSubtreeContent(StringBuilder builder)
	{
		for (Element child: children)
		{
			child.getSubtreeContent( builder );
		}
	}
	
	public int getContentLength()
	{
		int length = 0;
		
		for (Element child: children)
		{
			length += child.getContentLength();
		}
		
		return length;
	}
}
