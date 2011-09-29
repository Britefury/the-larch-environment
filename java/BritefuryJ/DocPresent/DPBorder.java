//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Border.AbstractBorder;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeBorder;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;


public class DPBorder extends DPBin
{
	public static FilledBorder defaultBorder = new FilledBorder( 0.0, 0.0, 0.0, 0.0 );
	
	protected AbstractBorder border;
	
	
	
	public DPBorder()
	{
		this( defaultBorder, ContainerStyleParams.defaultStyleParams );
	}

	public DPBorder(AbstractBorder border)
	{
		this( border, ContainerStyleParams.defaultStyleParams );

		if ( border == null )
		{
			throw new RuntimeException( "Cannot have null border" );
		}
	}

	public DPBorder(ContainerStyleParams styleParams)
	{
		this( defaultBorder, styleParams);
	}
	
	public DPBorder(AbstractBorder border, ContainerStyleParams styleParams)
	{
		super(styleParams);
		
		if ( border == null )
		{
			throw new RuntimeException( "Cannot have null border" );
		}
		
		this.border = border;
		layoutNode = new LayoutNodeBorder( this );
	}
	
	
	
	//
	//
	// Border
	//
	//
	
	public AbstractBorder getBorder()
	{
		return border;
	}
	
	public void setBorder(AbstractBorder b)
	{
		if ( b != border )
		{
			if ( b.getLeftMargin() != border.getLeftMargin()  ||  b.getRightMargin() != border.getRightMargin()  ||  b.getTopMargin() != border.getTopMargin()  ||  b.getBottomMargin() != border.getBottomMargin() )
			{
				queueResize();
			}
			else
			{
				queueFullRedraw();
			}
			border = b;
		}
	}
	
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		border.draw( graphics, 0.0, 0.0, getActualWidth(), getActualHeight() );
	}
}
