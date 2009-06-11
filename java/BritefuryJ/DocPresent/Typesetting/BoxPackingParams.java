//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Typesetting;

public class BoxPackingParams extends PackingParams
{
	public double padding;
	public int packFlags;
	
	public BoxPackingParams(double padding)
	{
		this( padding, false );
	}

	public BoxPackingParams(boolean bExpand)
	{
		this( 0.0, bExpand );
	}

	public BoxPackingParams(double padding, boolean bExpand)
	{
		this.padding = padding;
		this.packFlags = TSBox.packFlags( bExpand );
	}
}
