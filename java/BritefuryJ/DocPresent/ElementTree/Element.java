package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.ContentInterface;
import BritefuryJ.DocPresent.DPWidget;

public abstract class Element
{
	protected DPWidget widget;
	protected BranchElement parent;
	
	
	protected Element(DPWidget widget)
	{
		this.widget = widget;
		parent = null;
	}
	
	
	public DPWidget getWidget()
	{
		return widget;
	}
	
	
	
	public ContentInterface getContentInterface()
	{
		if ( widget != null )
		{
			return widget.getContentInterface();
		}
		return null;
	}
	
	
	protected void setParent(BranchElement parent)
	{
		this.parent = parent;
	}
}
