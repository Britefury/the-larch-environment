//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;

public class LSLineBreakCostSpan extends LSSpan
{
	public LSLineBreakCostSpan(ContainerStyleParams styleParams, LSElement[] items)
	{
		super( styleParams, items );
	}
	
	
	
	//
	//
	// LAYOUT METHODS
	//
	//
	
	public int getParagraphLinebreakCostModifier()
	{
		return 1;
	}
}
