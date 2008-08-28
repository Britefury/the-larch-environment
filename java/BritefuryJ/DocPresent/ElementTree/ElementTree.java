package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPPresentationArea;

public class ElementTree
{
	protected RootElement root;
	
	
	public ElementTree()
	{
		root = new RootElement();
		root.setElementTree( this );
	}
	
	
	public RootElement getRoot()
	{
		return root;
	}
	
	public DPPresentationArea getPresentationArea()
	{
		return root.getWidget();
	}
	
	
	protected void registerElement(Element elem)
	{
		
	}
	
	protected void unregisterElement(Element elem)
	{
		
	}
}
