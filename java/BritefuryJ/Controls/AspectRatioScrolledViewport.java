//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.AspectRatioBin;

public class AspectRatioScrolledViewport extends AbstractScrolledViewport
{
	private double minWidth, aspectRatio;
	
	
	public AspectRatioScrolledViewport(Object child, double minWidth, double aspectRatio, boolean scrollX, boolean scrollY, PersistentState state)
	{
		super( child, scrollX, scrollY, state );
		this.minWidth = minWidth;
		this.aspectRatio = aspectRatio;
	}
	
	public AspectRatioScrolledViewport(Object child, double minWidth, double aspectRatio, PersistentState state)
	{
		this( child, minWidth, aspectRatio, true, true, state );
	}
	

	@Override
	protected Pres createViewportBin(LSElement viewport)
	{
		return new AspectRatioBin( minWidth, aspectRatio, viewport );
	}
}
