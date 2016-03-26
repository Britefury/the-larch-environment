//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSpaceBin;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.SpaceBin;

public class ScrolledViewport extends AbstractScrolledViewport
{
	private double width, height;
	private LSSpaceBin.SizeConstraint sizeConstraintX, sizeConstraintY;
	
	
	public ScrolledViewport(Object child, double width, double height, LSSpaceBin.SizeConstraint sizeConstraintX,  LSSpaceBin.SizeConstraint sizeConstraintY, boolean scrollX, boolean scrollY, PersistentState state)
	{
		super( child, scrollX, scrollY, state );
		this.width = width;
		this.height = height;
		this.sizeConstraintX = sizeConstraintX;
		this.sizeConstraintY = sizeConstraintY;
	}
	
	public ScrolledViewport(Object child, double width, double height, LSSpaceBin.SizeConstraint sizeConstraintX,  LSSpaceBin.SizeConstraint sizeConstraintY, PersistentState state)
	{
		this( child, width, height, sizeConstraintX, sizeConstraintY, true, true, state );
	}
	
	public ScrolledViewport(Object child, double width, double height, boolean scrollX, boolean scrollY, PersistentState state)
	{
		this( child, width, height, LSSpaceBin.SizeConstraint.LARGER, LSSpaceBin.SizeConstraint.LARGER, scrollX, scrollY, state );
	}
	
	public ScrolledViewport(Object child, double width, double height, PersistentState state)
	{
		this( child, width, height, LSSpaceBin.SizeConstraint.LARGER, LSSpaceBin.SizeConstraint.LARGER, true, true, state );
	}

	
	@Override
	protected Pres createViewportBin(LSElement viewport)
	{
		return new SpaceBin( width, height, sizeConstraintX, sizeConstraintY, viewport );
	}
}
