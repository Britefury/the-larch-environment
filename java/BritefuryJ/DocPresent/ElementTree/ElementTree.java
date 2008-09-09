//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
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
