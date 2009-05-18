//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree.Selection;

import java.util.ArrayList;

import BritefuryJ.DocPresent.ElementTree.BranchElement;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Selection.SelectionListener;

public class ElementSelection implements SelectionListener
{
	protected ElementTree tree;
	protected Selection widgetSelection;

	protected ElementMarker marker0, marker1;
	
	private ElementMarker startMarker, endMarker;
	private ArrayList<Element> startPathFromCommonRoot, endPathFromCommonRoot;
	private BranchElement commonRoot;

	
	
	public ElementSelection(ElementTree tree, Selection widgetSelection)
	{
		this.tree = tree;
		this.widgetSelection = widgetSelection;
		this.widgetSelection.addSelectionListener( this );
		marker0 = new ElementMarker( tree, widgetSelection.getMarker0() );
		marker1 = new ElementMarker( tree, widgetSelection.getMarker1() );
	}



	public boolean isEmpty()
	{
		return widgetSelection.isEmpty();
	}
	
	
	
	public ElementMarker getMarker0()
	{
		return marker0;
	}
	
	public ElementMarker getMarker1()
	{
		return marker1;
	}
	
	
	public void clear()
	{
		widgetSelection.clear();
	}
	
	
	
	public ElementMarker getStartMarker()
	{
		refresh();
		return startMarker;
	}
	
	public ElementMarker getEndMarker()
	{
		refresh();
		return endMarker;
	}
	
	public ArrayList<Element> getStartPathFromCommonRoot()
	{
		refresh();
		return startPathFromCommonRoot;
	}
	
	public ArrayList<Element> getEndPathFromCommonRoot()
	{
		refresh();
		return endPathFromCommonRoot;
	}
	
	public BranchElement getCommonRoot()
	{
		refresh();
		return commonRoot;
	}
	
	
	private void refresh()
	{
		if ( startMarker == null )
		{
			if ( !isEmpty() )
			{
				Element e0 = marker0.getElement();
				ArrayList<Element> path0 = new ArrayList<Element>();
				Element e1 = marker1.getElement();
				ArrayList<Element> path1 = new ArrayList<Element>();
				Element.getPathsFromCommonSubtreeRoot( e0, path0, e1, path1 );
				
				boolean bInOrder = true;
				commonRoot = null;
				
				if ( path0.size() > 1  &&  path1.size() > 1 )
				{
					commonRoot = (BranchElement)path0.get( 0 );
					bInOrder = widgetSelection.getStartMarker() == widgetSelection.getMarker0();
				}
				else if ( path0.size() == 1  &&  path1.size() == 1 )
				{
					if ( e0 != e1 )
					{
						throw new RuntimeException( "Paths have length 1, but widgets are different" );
					}
					bInOrder = marker0.getIndex()  <  marker1.getIndex();
				}
				else
				{
					throw new RuntimeException( "Paths should either both have length == 1, or both have length > 1" );
				}
				
				
				startMarker = bInOrder  ?  marker0  :  marker1;
				endMarker = bInOrder  ?  marker1  :  marker0;
				startPathFromCommonRoot = bInOrder  ?  path0  :  path1;
				endPathFromCommonRoot = bInOrder  ?  path1  :  path0;
			}
		}
	}

	
	
	public void selectionChanged(Selection s)
	{
		if ( startMarker != null )
		{
			startMarker = endMarker = null;
			startPathFromCommonRoot = endPathFromCommonRoot = null;
			commonRoot = null;
		}
	}
}
