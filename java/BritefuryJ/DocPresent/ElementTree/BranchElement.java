package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPContainer;

public abstract class BranchElement extends Element
{
	protected BranchElement(DPContainer widget)
	{
		super( widget );
	}


	public DPContainer getWidget()
	{
		return (DPContainer)super.getWidget();
	}
}
