package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPPresentationArea;

public class RootElement extends BinElement
{
	public RootElement()
	{
		super( new DPPresentationArea() );
	}
	
	
	public DPPresentationArea getWidget()
	{
		return (DPPresentationArea)widget;
	}
}
