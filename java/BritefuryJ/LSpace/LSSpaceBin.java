//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeSpaceBin;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;

public class LSSpaceBin extends LSBin
{
	protected final static int FLAGS_SPACEBIN_START = FLAGS_BIN_END;
	
	protected final static int FLAG_MAX_SIZE = FLAGS_SPACEBIN_START * 0x1;
	
	protected final static int FLAGS_SPACEBIN_END = FLAGS_SPACEBIN_START << 1;
	

	
	private double width, height;
	
	
	public LSSpaceBin(double width, double height)
	{
		this( ContainerStyleParams.defaultStyleParams, width, height, false );
	}
	
	public LSSpaceBin(double width, double height, boolean maxSize)
	{
		this( ContainerStyleParams.defaultStyleParams, width, height, maxSize );
	}
	
	public LSSpaceBin(ContainerStyleParams styleParams, double width, double height)
	{
		this( styleParams, width, height, false );
	}
	
	public LSSpaceBin(ContainerStyleParams styleParams, double width, double height, boolean maxSize)
	{
		super( styleParams );
		
		this.width = width;
		this.height = height;
		
		setFlagValue( FLAG_MAX_SIZE, maxSize );
		
		layoutNode = new LayoutNodeSpaceBin( this );
	}
	
	
	
	//
	//
	// Space bin
	//
	//
	
	public double getWidth()
	{
		return width;
	}
	
	public double getHeight()
	{
		return height;
	}
	
	
	public boolean isMaxSize()
	{
		return testFlag( FLAG_MAX_SIZE );
	}
}
