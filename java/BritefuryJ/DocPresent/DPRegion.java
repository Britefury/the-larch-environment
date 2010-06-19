//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;

public class DPRegion extends DPProxy
{
	public static class SharableSelectionFilter implements ElementFilter
	{
		private DPRegion regionA;
		private EditHandler handlerA;
		
		
		public SharableSelectionFilter(DPRegion regionA)
		{
			this.regionA = regionA;
			handlerA = regionA.getEditHandler();
		}
		
		
		public boolean testElement(DPElement element)
		{
			DPRegion regionB = element.getRegion();
			
			if ( regionB == regionA )
			{
				return true;
			}
			else
			{
				EditHandler handlerB = regionB.getEditHandler();
				if ( handlerA != null )
				{
					return handlerA.canShareSelectionWith( handlerB );
				}
				else
				{
					return false;
				}
			}
		}
	}

	
	
	private EditHandler editHandler;

	
	
	
	public DPRegion()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public DPRegion(ContainerStyleParams styleParams)
	{
		super(styleParams);
	}
	
	protected DPRegion(DPRegion element)
	{
		super( element );
		
		editHandler = element.editHandler;
	}
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPRegion clone = new DPRegion( this );
		clone.clonePostConstuct( this );
		return clone;
	}

	
	

	//
	//
	// EDIT HANDLER
	//
	//
	
	public void setEditHandler(EditHandler handler)
	{
		editHandler = handler;
	}
	
	public EditHandler getEditHandler()
	{
		return editHandler;
	}
	
	

	
	
	public DPRegion getRegion()
	{
		return this;
	}
	
	
	
	//
	//
	// SHARABLE SELECTION FILTER
	//
	//
	
	public SharableSelectionFilter sharableSelectionFilter()
	{
		return new SharableSelectionFilter( this );
	}
}
