//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout.Padding;

public class ElementPaddingY extends AbstractElementPadding
{
	private double topPadding, bottomPadding;
	
	
	public ElementPaddingY(double topPadding, double bottomPadding)
	{
		this.topPadding = topPadding;
		this.bottomPadding = bottomPadding;
	}
	

	
	public double getLeftPadding()
	{
		return 0.0;
	}

	public double getRightPadding()
	{
		return 0.0;
	}

	public double getTopPadding()
	{
		return topPadding;
	}

	public double getBottomPadding()
	{
		return bottomPadding;
	}



	public AbstractElementPadding padX(double left, double right)
	{
		return new ElementPadding( left, right, topPadding, bottomPadding );
	}
	
	public AbstractElementPadding padY(double top, double bottom)
	{
		return new ElementPaddingY( topPadding + top, bottomPadding + bottom );
	}
	
	public AbstractElementPadding pad(double left, double right, double top, double bottom)
	{
		return new ElementPadding( left, right, topPadding + top, bottomPadding + bottom );
	}
}
