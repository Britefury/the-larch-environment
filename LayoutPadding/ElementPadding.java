//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout.Padding;

public class ElementPadding extends AbstractElementPadding
{
	private double leftPadding, rightPadding, topPadding, bottomPadding;
	
	
	public ElementPadding(double leftPadding, double rightPadding, double topPadding, double bottomPadding)
	{
		this.leftPadding = leftPadding;
		this.rightPadding = rightPadding;
		this.topPadding = topPadding;
		this.bottomPadding = bottomPadding;
	}
	

	
	public double getLeftPadding()
	{
		return leftPadding;
	}

	public double getRightPadding()
	{
		return rightPadding;
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
		return new ElementPadding( leftPadding, rightPadding, top, bottom );
	}
	
	public AbstractElementPadding pad(double left, double right, double top, double bottom)
	{
		return new ElementPadding( leftPadding + left, rightPadding + right, topPadding + top, bottomPadding + bottom );
	}
}
