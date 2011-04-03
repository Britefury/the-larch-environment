//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;

public class DPRegion extends DPProxy
{
	public static class SharableSelectionFilter implements ElementFilter
	{
		private DPRegion regionA;
		private ClipboardHandlerInterface handlerA;
		
		
		public SharableSelectionFilter(DPRegion regionA)
		{
			this.regionA = regionA;
			handlerA = regionA.getClipboardHandler();
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
				ClipboardHandlerInterface handlerB = regionB != null  ?  regionB.getClipboardHandler()  :  null;
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

	
	
	private ClipboardHandlerInterface clipboardHandler;

	
	
	
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
		
		clipboardHandler = element.clipboardHandler;
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
	// CLIPBOARD HANDLER
	//
	//
	
	public void setClipboardHandler(ClipboardHandlerInterface handler)
	{
		clipboardHandler = handler;
	}
	
	public ClipboardHandlerInterface getClipboardHandler()
	{
		return clipboardHandler;
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
