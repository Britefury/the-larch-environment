package BritefuryJ.Browser;

import BritefuryJ.Projection.AbstractPerspective;


public interface PaneManager
{
	public EdgePane getLeftEdgePane();
	public EdgePane getRightEdgePane();
	public EdgePane getTopEdgePane();
	public EdgePane getBottomEdgePane();
}
