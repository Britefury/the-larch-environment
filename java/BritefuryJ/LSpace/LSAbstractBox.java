//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
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
