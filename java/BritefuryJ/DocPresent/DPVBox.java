//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeVBox;
import BritefuryJ.DocPresent.StyleParams.VBoxStyleParams;


public class DPVBox extends DPAbstractBox
{
	public static class InvalidTypesettingException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	protected final static int FLAG_HAS_REFPOINT_INDEX = FLAGS_CONTAINER_END * 0x1;
	
	protected final static int FLAGS_VBOX_END = FLAGS_CONTAINER_END << 1;

	
	
	private int refPointIndex = 0;
	

	
	public DPVBox()
	{
		this( VBoxStyleParams.defaultStyleParams);
	}
	
	public DPVBox(VBoxStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeVBox( this );
		clearFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	protected DPVBox(DPVBox element)
	{
		super( element );
		
		layoutNode = new LayoutNodeVBox( this );
		setFlagValue( FLAG_HAS_REFPOINT_INDEX, element.testFlag( FLAG_HAS_REFPOINT_INDEX ) );
		refPointIndex = element.refPointIndex;
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPVBox clone = new DPVBox( this );
		clone.clonePostConstuct( this );
		return clone;
	}

	
	
	
	//
	//
	// Ref-point index
	//
	//


	public void setRefPointIndex(int refPointIndex)
	{
		this.refPointIndex = refPointIndex;
		setFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	public void clearRefPointIndex()
	{
		clearFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	
	
	public boolean hasRefPointIndex()
	{
		return testFlag( FLAG_HAS_REFPOINT_INDEX );
	}
	
	public int getRefPointIndex()
	{
		return refPointIndex;
	}
}
