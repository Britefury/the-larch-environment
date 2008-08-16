package BritefuryJ.GSymViewSwing.ElementViewFactories;

import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;

public class DocStructureDocRootViewFactory implements ElementViewFactory
{
	public static DocStructureDocRootViewFactory viewFactory = new DocStructureDocRootViewFactory();
	
	public View createView(Element e)
	{
		return new BoxView( e, View.Y_AXIS );
	}
}
