package BritefuryJ.DocPresent.ElementTree;

import java.util.List;

import BritefuryJ.DocPresent.DPContainer;

public abstract class BranchElement extends Element
{
	protected BranchElement(DPContainer widget)
	{
		super( widget );
	}


	public DPContainer getWidget()
	{
		return (DPContainer)widget;
	}
	
	
	
	protected abstract List<Element> getChildren();
	
	
	protected void setElementTree(ElementTree tree)
	{
		if ( tree != this.tree )
		{
			super.setElementTree( tree );
			
			for (Element c: getChildren())
			{
				c.setElementTree( tree );
			}
		}
	}
}
