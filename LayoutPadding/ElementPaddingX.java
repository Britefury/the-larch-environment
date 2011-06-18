//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout.Padding;

public class ElementPaddingX extends AbstractElementPadding
{
	private double leftPadding, rightPadding;
	
	
	public ElementPaddingX(double leftPadding, double rightPadding)
	{
		this.leftPadding = leftPadding;
		this.rightPadding = rightPadding;
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
		return 0.0;
	}

	public double getBottomPadding()
	{
		return 0.0;
	}



	public AbstractElementPadding padX(double left, double right)
	{
		return new ElementPaddingX( leftPadding + left, rightPadding + right );
	}
	
	public AbstractElementPadding padY(double top, double bottom)
	{
		return new ElementPadding( leftPadding, rightPadding, top, bottom );
	}
	
	public AbstractElementPadding pad(double left, double right, double top, double bottom)
	{
		return new ElementPadding( leftPadding + left, rightPadding + right, top, bottom );
	}
}
