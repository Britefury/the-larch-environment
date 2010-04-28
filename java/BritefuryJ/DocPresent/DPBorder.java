//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeBorder;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;


public class DPBorder extends DPBin
{
	public static FilledBorder defaultBorder = new FilledBorder( 0.0, 0.0, 0.0, 0.0 );
	
	protected Border border;
	
	
	
	public DPBorder()
	{
		this( defaultBorder, ContainerStyleParams.defaultStyleParams );
	}

	public DPBorder(Border border)
	{
		this( border, ContainerStyleParams.defaultStyleParams );
	}

	public DPBorder(ContainerStyleParams styleParams)
	{
		this( defaultBorder, styleParams);
	}
	
	public DPBorder(Border border, ContainerStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeBorder( this );
		this.border = border;
	}
	
	protected DPBorder(DPBorder element)
	{
		super( element );
		border = element.border;
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPBorder clone = new DPBorder( this );
		clone.clonePostConstuct( this );
		return clone;
	}

	
	
	//
	//
	// Border
	//
	//
	
	public Border getBorder()
	{
		return border;
	}
	
	public void setBorder(Border b)
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
		border.draw( graphics, 0.0, 0.0, getAllocationX(), getAllocationY() );
	}
}
