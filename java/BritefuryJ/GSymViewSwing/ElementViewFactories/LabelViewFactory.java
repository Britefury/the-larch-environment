package BritefuryJ.GSymViewSwing.ElementViewFactories;

import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.View;

public class LabelViewFactory implements ElementViewFactory
{
	public static LabelViewFactory viewFactory = new LabelViewFactory();

	public View createView(Element e)
	{
		LabelView v = new LabelView( e );
		return v;
	}

}
