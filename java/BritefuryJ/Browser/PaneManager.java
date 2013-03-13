package BritefuryJ.Browser;

import BritefuryJ.Projection.AbstractPerspective;


public interface PaneManager
{
	public void setLeftPaneContent(Object contents, AbstractPerspective perspective);
	public void clearLeftPaneContent();

	public void setRightPaneContent(Object contents, AbstractPerspective perspective);
	public void clearRightPaneContent();

	public void setTopPaneContent(Object contents, AbstractPerspective perspective);
	public void clearTopPaneContent();

	public void setBottomPaneContent(Object contents, AbstractPerspective perspective);
	public void clearBottomPaneContent();
}
