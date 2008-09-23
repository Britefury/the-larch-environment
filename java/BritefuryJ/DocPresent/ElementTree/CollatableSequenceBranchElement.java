//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import BritefuryJ.DocPresent.DPContainerSequence;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public abstract class CollatableSequenceBranchElement extends CollatableBranchElement
{
	protected Vector<Element> children;
	
	
	
	protected CollatableSequenceBranchElement(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		children = new Vector<Element>();
	}


	public DPContainerSequence getWidget()
	{
		return (DPContainerSequence)widget;
	}



	public void setChildren(List<Element> xs)
	{
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
		
		for (Element x: added)
		{
			x.setParent( this );
			x.setElementTree( tree );
		}
		
		onChildListChanged();
	}
	

	
	protected void refreshContainerWidgetContents()
	{
		Vector<DPWidget> childWidgets = new Vector<DPWidget>();
		childWidgets.setSize( children.size() );
		for (int i = 0; i < children.size(); i++)
		{
			childWidgets.set( i, children.get( i ).getWidget() );
		}
		
		getWidget().setChildren( childWidgets );
	}

	
	public List<Element> getChildren()
	{
		return children;
	}
}
