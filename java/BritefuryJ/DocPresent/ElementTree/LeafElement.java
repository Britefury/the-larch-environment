//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.WidgetContentListener;
import BritefuryJ.DocPresent.Marker.Marker;

public abstract class LeafElement extends Element implements WidgetContentListener
{
	protected LeafElement(DPContentLeaf widget)
	{
		super( widget );
		widget.setContentListener( this );
	}
	
	
	
	public DPContentLeaf getWidget()
	{
		return (DPContentLeaf)widget;
	}

	
	public LeafElement getLeafAtContentPosition(int position)
	{
		return this;
	}


	
	
	public void contentInserted(Marker m, String x)
	{
		onContentModified();
	}

	public void contentRemoved(Marker m, int length)
	{
		onContentModified();
	}

	public void contentReplaced(Marker m, int length, String x)
	{
		onContentModified();
	}
}
