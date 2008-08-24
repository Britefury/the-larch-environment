package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPPresentationArea;

public class ElementTree
{
	protected RootElement root;
	
	
	public ElementTree()
	{
		root = new RootElement();
	}
	
	
	public RootElement getRoot()
	{
		return root;
	}
	
	public DPPresentationArea getPresentationArea()
	{
		return root.getWidget();
	}
}
