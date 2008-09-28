//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree.Marker;

import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementTree.BranchElement;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.LeafElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Marker.Marker.Bias;

public class ElementMarker
{
	protected ElementTree tree;
	protected Marker widgetMarker;
	
	
	public ElementMarker(ElementTree tree, Marker widgetMarker)
	{
		this.tree = tree;
		this.widgetMarker = widgetMarker;
	}



	public Element getElement()
	{
		DPContentLeaf widget = widgetMarker.getWidget();
		if ( widget != null )
		{
			return widget.getElement();
		}
		else
		{
			return null;
		}
	}
	
	public int getPosition()
	{
		return widgetMarker.getPosition();
	}
	
	public int getPositionInSubtree(Element subtreeRoot)
	{
		Element element = getElement();
		if ( subtreeRoot == element )
		{
			return getPosition();
		}
		else
		{
			BranchElement b = (BranchElement)subtreeRoot;
			if ( element != null  &&  element.isInSubtreeRootedAt( b ) )
			{
				return getPosition() + element.getContentOffsetInSubtree( b );
			}
			else
			{
				throw new DPWidget.IsNotInSubtreeException();
			}
		}
	}
	
	public Bias getBias()
	{
		return widgetMarker.getBias();
	}
	
	
	public int getIndex()
	{
		return widgetMarker.getIndex();
	}
	
	public int getIndexInSubtree(Element subtreeRoot)
	{
		int p = getPositionInSubtree( subtreeRoot );
		return getBias() == Bias.END  ?  p + 1  :  p;
	}
	

	public void setPosition(int position)
	{
		widgetMarker.setPosition( position );
	}
	
	public void setPositionAndBias(int position, Bias bias)
	{
		widgetMarker.setPositionAndBias( position, bias );
	}
	
	public void set(LeafElement element, int position, Bias bias)
	{
		widgetMarker.set( (DPContentLeaf)element.getWidgetAtContentStart(), position, bias );
	}
	
	
	public static int getIndex(int position, Bias bias)
	{
		return Marker.getIndex( position, bias );
	}
	
	
	
	public boolean isValid()
	{
		return widgetMarker.isValid();
	}
	
	
	
	public ElementMarker clone()
	{
		try
		{
			return (ElementMarker)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	
	
	public Marker getWidgetMarker()
	{
		return widgetMarker;
	}
}
