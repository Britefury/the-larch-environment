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
		return (DPContentLeaf)widget;
	}

	
	public void contentInserted(Marker m, String x)
	{
		onContentModified();
	}

	public void contentRemoved(Marker m, int length)
	{
		onContentModified();
	}

	public void contentReplaced(Marker m, int length, String x)
	{
		onContentModified();
	}
}
