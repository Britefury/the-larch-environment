//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeParagraphIndentMarker;
import BritefuryJ.DocPresent.StyleParams.ElementStyleParams;

public class DPParagraphIndentMarker extends DPEmpty
{
	public DPParagraphIndentMarker()
	{
		super( );
		
		layoutNode = new LayoutNodeParagraphIndentMarker( this );
	}
	
	public DPParagraphIndentMarker(ElementStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeParagraphIndentMarker( this );
	}
	
	protected DPParagraphIndentMarker(DPParagraphIndentMarker element)
	{
		super( element );
		
		layoutNode = new LayoutNodeParagraphIndentMarker( this );
	}
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPParagraphIndentMarker clone = new DPParagraphIndentMarker( this );
		clone.clonePostConstuct( this );
		return clone;
	}
}
