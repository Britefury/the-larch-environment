//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.StyleParams.AbstractBoxStyleParams;


abstract public class LSAbstractBox extends LSContainerSequence
{
	public LSAbstractBox(AbstractBoxStyleParams styleParams, LSElement[] items)
	{
		super( styleParams, items );
	}


	
	

	public double getSpacing()
	{
		return ((AbstractBoxStyleParams) styleParams).getSpacing();
	}
}
