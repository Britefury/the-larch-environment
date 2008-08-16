package BritefuryJ.GSymViewSwing.ElementViewFactories;

import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;

public class VBoxViewFactory implements ElementViewFactory
{
	public static VBoxViewFactory viewFactory = new VBoxViewFactory();
	
	public View createView(Element e)
	{
		return new BoxView( e, View.Y_AXIS );
	}
}
