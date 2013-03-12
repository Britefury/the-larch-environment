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
		FIXED,
		NONE
	}
	
	
	protected final static int FLAGS_SPACEBIN_START = FLAGS_BIN_END;
	

	protected final static int CONSTRAINT_FLAG_VALUE_LARGER = 0x00;
	protected final static int CONSTRAINT_FLAG_VALUE_SMALLER = 0x01;
	protected final static int CONSTRAINT_FLAG_VALUE_FIXED = 0x02;
	protected final static int CONSTRAINT_FLAG_VALUE_NONE = 0x03;



	protected final static int _FLAG_SIZE_CONSTRAINT_X_START = FLAGS_SPACEBIN_START * 0x1;
	protected final static int _FLAG_SIZE_CONSTRAINT_X_END = _FLAG_SIZE_CONSTRAINT_X_START << 2;
	protected final static int _FLAG_SIZE_CONSTRAINT_X_MASK = _FLAG_SIZE_CONSTRAINT_X_START * 0x03;
	
	protected final static int _FLAG_SIZE_CONSTRAINT_Y_START = _FLAG_SIZE_CONSTRAINT_X_END * 0x1;
	protected final static int _FLAG_SIZE_CONSTRAINT_Y_END = _FLAG_SIZE_CONSTRAINT_Y_START << 2;
	protected final static int _FLAG_SIZE_CONSTRAINT_Y_MASK = _FLAG_SIZE_CONSTRAINT_Y_START * 0x03;

	protected final static int FLAGS_SPACEBIN_END = _FLAG_SIZE_CONSTRAINT_Y_END << 0;
	

	
	private double width, height;
	
	
	public LSSpaceBin(ContainerStyleParams styleParams, double width, double height,
			  SizeConstraint sizeConstraintX, SizeConstraint sizeConstraintY, LSElement child)
	{
		super( styleParams, child );
		
		this.width = width;
		this.height = height;
		
		setFlagMaskValue( _FLAG_SIZE_CONSTRAINT_X_MASK, sizeConstraintToFlag( sizeConstraintX ) * _FLAG_SIZE_CONSTRAINT_X_START );
		setFlagMaskValue( _FLAG_SIZE_CONSTRAINT_Y_MASK, sizeConstraintToFlag( sizeConstraintY ) * _FLAG_SIZE_CONSTRAINT_Y_START );

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
	
	
	public void setWidth(double width)
	{
		this.width = width;
		queueResize();
	}

	public void setHeight(double height)
	{
		this.height = height;
		queueResize();
	}


	public SizeConstraint getSizeConstraintX()
	{
		return flagToSizeConstraint( getFlagMaskValue( _FLAG_SIZE_CONSTRAINT_X_MASK )  /  _FLAG_SIZE_CONSTRAINT_X_START );
	}
	
	public SizeConstraint getSizeConstraintY()
	{
		return flagToSizeConstraint( getFlagMaskValue( _FLAG_SIZE_CONSTRAINT_Y_MASK )  /  _FLAG_SIZE_CONSTRAINT_Y_START );
	}


	public void setSizeConstraintX(SizeConstraint constraintX)
	{
		setFlagMaskValue( _FLAG_SIZE_CONSTRAINT_X_MASK, sizeConstraintToFlag( constraintX ) * _FLAG_SIZE_CONSTRAINT_X_START );
		queueResize();
	}

	public void setSizeConstraintY(SizeConstraint constraintY)
	{
		setFlagMaskValue( _FLAG_SIZE_CONSTRAINT_Y_MASK, sizeConstraintToFlag( constraintY ) * _FLAG_SIZE_CONSTRAINT_Y_START );
		queueResize();
	}



	private static int sizeConstraintToFlag(SizeConstraint c)
	{
		if ( c == SizeConstraint.LARGER )
		{
			return CONSTRAINT_FLAG_VALUE_LARGER;
		}
		else if ( c == SizeConstraint.SMALLER )
		{
			return CONSTRAINT_FLAG_VALUE_SMALLER;
		}
		else if ( c == SizeConstraint.FIXED )
		{
			return CONSTRAINT_FLAG_VALUE_FIXED;
		}
		else if ( c == SizeConstraint.NONE )
		{
			return CONSTRAINT_FLAG_VALUE_NONE;
		}
		else
		{
			throw new RuntimeException( "Invalid size constraint" );
		}
	}
	
	private static SizeConstraint flagToSizeConstraint(int flag)
	{
		if ( flag == CONSTRAINT_FLAG_VALUE_LARGER )
		{
			return SizeConstraint.LARGER;
		}
		else if ( flag == CONSTRAINT_FLAG_VALUE_SMALLER )
		{
			return SizeConstraint.SMALLER;
		}
		else if ( flag == CONSTRAINT_FLAG_VALUE_FIXED )
		{
			return SizeConstraint.FIXED;
		}
		else if ( flag == CONSTRAINT_FLAG_VALUE_NONE )
		{
			return SizeConstraint.NONE;
		}
		else
		{
			throw new RuntimeException( "Invalid size constraint flag value" );
		}
	}
}
