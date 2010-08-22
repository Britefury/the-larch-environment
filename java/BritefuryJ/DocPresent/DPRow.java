//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeRow;
import BritefuryJ.DocPresent.StyleParams.RowStyleParams;


public class DPRow extends DPAbstractBox
{
	public DPRow()
	{
		this( RowStyleParams.defaultStyleParams);
	}
	
	public DPRow(RowStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeRow( this );
	}
	
	protected DPRow(DPRow element)
	{
		super( element );
		
		layoutNode = new LayoutNodeRow( this );
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPRow clone = new DPRow( this );
		clone.clonePostConstuct( this );
		return clone;
	}
}