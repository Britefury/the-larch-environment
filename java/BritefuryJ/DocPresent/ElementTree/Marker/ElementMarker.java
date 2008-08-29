package BritefuryJ.DocPresent.ElementTree.Marker;

import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.LeafElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Marker.Marker.Bias;

public class ElementMarker
{
	protected ElementTree tree;
	protected Marker widgetMarker;
	
	
	public ElementMarker(ElementTree tree, Marker widgetMarker)
	{
		this.tree = tree;
		this.widgetMarker = widgetMarker;
	}



	public LeafElement getElement()
	{
		return (LeafElement)tree.getElementForWidget( widgetMarker.getWidget() );
	}
	
	public int getPosition()
	{
		return widgetMarker.getPosition();
	}
	
	public Bias getBias()
	{
		return widgetMarker.getBias();
	}
	
	
	public void setPosition(int position)
	{
		widgetMarker.setPosition( position );
	}
	
	public void setPositionAndBias(int position, Bias bias)
	{
		widgetMarker.setPositionAndBias( position, bias );
	}
	
	public void set(LeafElement element, int position, Bias bias)
	{
		widgetMarker.set( (DPContentLeaf)element.getWidgetAtContentStart(), position, bias );
	}
	
	
	public int getIndex()
	{
		return widgetMarker.getIndex();
	}
	
	public static int getIndex(int position, Bias bias)
	{
		return Marker.getIndex( position, bias );
	}
	
	
	
	public boolean isValid()
	{
		return widgetMarker.isValid();
	}
	
	
	
	public ElementMarker clone()
	{
		try {
			return (ElementMarker)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
}
