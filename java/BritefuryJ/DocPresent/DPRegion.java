//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.DocPresent.StyleParams.RegionStyleParams;

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
			DPRegion regionB = getRegionOf( element );
			
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

	
	
	public static final int FLAG_EDITABLE = FLAGS_PROXY_END * 0x1;
	public static final int FLAG_SELECTABLE = FLAGS_PROXY_END * 0x2;
	
	
	protected final static int FLAGS_REGION_END = FLAGS_PROXY_END * 0x4;

	
	private ClipboardHandlerInterface clipboardHandler;

	
	
	
	public DPRegion()
	{
		this( RegionStyleParams.defaultStyleParams );
	}

	public DPRegion(RegionStyleParams styleParams)
	{
		super( styleParams );
	
		setFlagValue( FLAG_EDITABLE, styleParams.getEditable() );
		setFlagValue( FLAG_SELECTABLE, styleParams.getSelectable() );
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
	
	

	
	
	//
	// REGION
	//
	
	protected boolean isRegion()
	{
		return true;
	}
	
	protected static DPRegion getRegionOf(DPElement e)
	{
		while ( e != null )
		{
			if ( e.isRegion() )
			{
				return (DPRegion)e;
			}
			
			e = e.parent;
		}
		return null;
	}
	
	
	
	
	
	//
	//
	// EDITABILITY METHODS
	//
	//
	
	public void setEditable()
	{
		setFlag( FLAG_EDITABLE );
	}
	
	public void setNonEditable()
	{
		clearFlag( FLAG_EDITABLE );
	}
	
	public boolean isEditable()
	{
		return testFlag( FLAG_EDITABLE );
	}
	
	
	
	
	//
	//
	// SELECTABILITY METHODS
	//
	//
	
	public void setSelectable()
	{
		setFlag( FLAG_SELECTABLE );
	}
	
	public void setUnselectable()
	{
		clearFlag( FLAG_SELECTABLE );
	}
	
	public boolean isSelectable()
	{
		return testFlag( FLAG_SELECTABLE );
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
