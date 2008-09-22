//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import BritefuryJ.DocPresent.DPContainerSequence;
import BritefuryJ.DocPresent.DPWidget;


public abstract class SequenceBranchElement extends BranchElement
{
	protected Vector<Element> children;
	
	
	
	protected SequenceBranchElement(DPContainerSequence widget)
	{
		super( widget );
		
		children = new Vector<Element>();
	}


	public void setChildren(List<Element> xs)
	{
		Vector<DPWidget> childWidgets = new Vector<DPWidget>();

		childWidgets.setSize( xs.size() );
		for (int i = 0; i < xs.size(); i++)
		{
			childWidgets.set( i, xs.get( i ).getWidget() );
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
	

	
	public DPContainerSequence getWidget()
	{
		return (DPContainerSequence)widget;
	}



	public List<Element> getChildren()
	{
		return children;
	}
}
