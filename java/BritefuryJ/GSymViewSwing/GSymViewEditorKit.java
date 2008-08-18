package BritefuryJ.GSymViewSwing;

import javax.swing.text.StyledEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import BritefuryJ.GSymViewSwing.ElementViewFactories.ElementViewFactory;

public class GSymViewEditorKit extends StyledEditorKit
{
	private static class ViewFac implements ViewFactory 
	{
		public View create(Element e)
		{
			GSymViewDocument doc = (GSymViewDocument)e.getDocument();
			
			ElementViewFactory elementViewFactory = doc.getElementViewFactory( e );
			return elementViewFactory.createView( e );
		}
	}

	
	
	private static final long serialVersionUID = 1L;
	
	private static ViewFac viewFactory = new ViewFac();
	
	
	
	
	public Document createDefaultDocument()
	{
		return new GSymViewDocument();
	}
	
	
	public ViewFactory getViewFactory()
	{
		return viewFactory;
	}
}
