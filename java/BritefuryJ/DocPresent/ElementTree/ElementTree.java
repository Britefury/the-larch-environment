package BritefuryJ.DocPresent.ElementTree;

import java.util.HashMap;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementTree.Caret.ElementCaret;

public class ElementTree
{
	protected RootElement root;
	protected HashMap<DPWidget,Element> widgetToElement;
	protected ElementCaret caret;
	
	
	public ElementTree()
	{
		widgetToElement = new HashMap<DPWidget,Element>();
		root = new RootElement();
		root.setElementTree( this );
		caret = new ElementCaret( this, getPresentationArea().getCaret() );
	}
	
	
	public RootElement getRoot()
	{
		return root;
	}
	
	public DPPresentationArea getPresentationArea()
	{
		return root.getWidget();
	}
	
	public ElementCaret getCaret()
	{
		return caret;
	}
	
	
	protected void registerElement(Element elem)
	{
		DPWidget widget = elem.getWidget();
		if ( widget != null )
		{
			widgetToElement.put( widget, elem );
		}
	}
	
	protected void unregisterElement(Element elem)
	{
		DPWidget widget = elem.getWidget();
		if ( widget != null )
		{
			widgetToElement.remove( widget );
		}
	}
	
	
	public Element getElementForWidget(DPWidget w)
	{
		return widgetToElement.get( w );
	}
}
