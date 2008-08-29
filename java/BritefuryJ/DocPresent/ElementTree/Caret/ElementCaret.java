package BritefuryJ.DocPresent.ElementTree.Caret;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;

public class ElementCaret
{
	protected ElementTree tree;
	protected Caret widgetCaret;
	protected ElementMarker marker;
	
	
	
	public ElementCaret(ElementTree tree, Caret widgetCaret)
	{
		this.tree = tree;
		this.widgetCaret = widgetCaret;
		this.marker = new ElementMarker( tree, widgetCaret.getMarker() );
	}
	
	
	public ElementMarker getMarker()
	{
		return marker;
	}
	
	
	public Element getElement()
	{
		if ( marker != null )
		{
			return marker.getElement();
		}
		else
		{
			return null;
		}
	}
	
	
	
	public boolean isValid()
	{
		if ( marker != null )
		{
			return marker.isValid();
		}
		else
		{
			return false;
		}
	}
}
