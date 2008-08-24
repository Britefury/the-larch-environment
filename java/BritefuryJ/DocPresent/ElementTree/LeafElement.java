package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.WidgetContentListener;
import BritefuryJ.DocPresent.Marker.Marker;

public class LeafElement extends Element implements WidgetContentListener
{
	protected LeafElement(DPContentLeaf widget)
	{
		super( widget );
		widget.setContentListener( this );
	}
	
	
	
	public DPContentLeaf getWidget()
	{
		return (DPContentLeaf)super.getWidget();
	}

	
	@Override
	public void contentInserted(Marker m, String x)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contentRemoved(Marker m, int length)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contentReplaced(Marker m, int length, String x)
	{
		// TODO Auto-generated method stub
		
	}
}
