//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;

public interface ElementPainter
{
	public void drawBackground(LSElement element, Graphics2D graphics);
	public void draw(LSElement element, Graphics2D graphics);
}
