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
	public static enum SizeConstraint
	{
		LARGER,
		SMALLER,
		FIXED
	}
	
	
	protected final static int FLAGS_SPACEBIN_START = FLAGS_BIN_END;
	
	protected final static int _FLAG_SIZE_CONSTRAINT_START = FLAGS_SPACEBIN_START * 0x1;
	protected final static int FLAG_SIZE_CONSTRAINT_LARGER = _FLAG_SIZE_CONSTRAINT_START * 0x00;
	protected final static int FLAG_SIZE_CONSTRAINT_SMALLER = _FLAG_SIZE_CONSTRAINT_START * 0x01;
	protected final static int FLAG_SIZE_CONSTRAINT_FIXED = _FLAG_SIZE_CONSTRAINT_START * 0x02;
	protected final static int _FLAG_SIZE_CONSTRAINT_END = _FLAG_SIZE_CONSTRAINT_START << 2;
	protected final static int _FLAG_SIZE_CONSTRAINT_MASK = _FLAG_SIZE_CONSTRAINT_START * 0x03;
	
	protected final static int FLAGS_SPACEBIN_END = _FLAG_SIZE_CONSTRAINT_END << 0;
	

	
	private double width, height;
	
	
	public LSSpaceBin(ContainerStyleParams styleParams, double width, double height, SizeConstraint sizeConstraint, LSElement child)
	{
		super( styleParams, child );
		
		this.width = width;
		this.height = height;
		
		setFlagMaskValue( _FLAG_SIZE_CONSTRAINT_MASK, sizeConstraintToFlag( sizeConstraint ) );
		
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
	
	
	public SizeConstraint getSizeConstraint()
	{
		return flagToSizeConstraint( getFlagMaskValue( _FLAG_SIZE_CONSTRAINT_MASK ) );
	}
	
	
	
	private static int sizeConstraintToFlag(SizeConstraint c)
	{
		if ( c == SizeConstraint.LARGER )
		{
			return FLAG_SIZE_CONSTRAINT_LARGER;
		}
		else if ( c == SizeConstraint.SMALLER )
		{
			return FLAG_SIZE_CONSTRAINT_SMALLER;
		}
		else if ( c == SizeConstraint.FIXED )
		{
			return FLAG_SIZE_CONSTRAINT_FIXED;
		}
		else
		{
			throw new RuntimeException( "Invalid size constraint" );
		}
	}
	
	private static SizeConstraint flagToSizeConstraint(int flag)
	{
		if ( flag == FLAG_SIZE_CONSTRAINT_LARGER )
		{
			return SizeConstraint.LARGER;
		}
		else if ( flag == FLAG_SIZE_CONSTRAINT_SMALLER )
		{
			return SizeConstraint.SMALLER;
		}
		else if ( flag == FLAG_SIZE_CONSTRAINT_FIXED )
		{
			return SizeConstraint.FIXED;
		}
		else
		{
			throw new RuntimeException( "Invalid size constraint flag value" );
		}
	}
}
