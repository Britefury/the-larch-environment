//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser;

import BritefuryJ.Projection.AbstractPerspective;


public interface PaneManager
{
	public EdgePane getLeftEdgePane();
	public EdgePane getRightEdgePane();
	public EdgePane getTopEdgePane();
	public EdgePane getBottomEdgePane();
}
