package BritefuryJ.DocPresent.ElementTree;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import BritefuryJ.DocPresent.DPContainerSequence;
import BritefuryJ.DocPresent.DPWidget;


public class SequenceBranchElement extends BranchElement
{
	protected Vector<Element> children;
	
	
	
	protected SequenceBranchElement(DPContainerSequence widget)
	{
		super( widget );
	}


	public void setChildren(List<Element> xs)
	{
		Vector<DPWidget> childWidgets = new Vector<DPWidget>();

		childWidgets.setSize( xs.size() );
		for (int i = 0; i < xs.size(); i++)
		{
			childWidgets.set( i, xs.get( i ).getWidget() );
		}
		
		HashSet<Element> added, removed;
		
		added = new HashSet<Element>( xs );
		removed = new HashSet<Element>( children );
		added.removeAll( children );
		removed.removeAll( xs );
		
		
		for (Element x: removed)
		{
			x.setParent( null );
		}
		
		children.clear();
		children.addAll( xs );
		
		getWidget().setChildren( childWidgets );

		for (Element x: added)
		{
			x.setParent( this );
		}
	}
	

	
	public DPContainerSequence getWidget()
	{
		return (DPContainerSequence)super.getWidget();
	}
}
