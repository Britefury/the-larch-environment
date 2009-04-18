//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import BritefuryJ.DocPresent.DPContainerSequence;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public abstract class CollatableSequenceBranchElement extends CollatableBranchElement
{
	protected ArrayList<Element> children;
	
	
	
	protected CollatableSequenceBranchElement(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		children = new ArrayList<Element>();
	}


	public DPContainerSequence getWidget()
	{
		return (DPContainerSequence)widget;
	}



	public void setChildren(List<Element> xs)
	{
		if ( children.isEmpty() )
		{
			children.addAll( xs );
			
			for (Element x: xs)
			{
				x.setParent( this );
				x.setElementTree( tree );
			}
		}
		else
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
		}
		
		onChildListChanged();
	}
	

	
	protected void refreshContainerWidgetContents()
	{
		ArrayList<DPWidget> childWidgets = new ArrayList<DPWidget>();
		childWidgets.ensureCapacity( children.size() );
		for (Element child: children)
		{
			childWidgets.add( child.getWidget() );
		}
		
		getWidget().setChildren( childWidgets );
	}

	
	public List<Element> getChildren()
	{
		return children;
	}
}
