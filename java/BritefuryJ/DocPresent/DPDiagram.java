//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Diagram.DiagramNode;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;

public class DPDiagram extends DPStatic
{
	protected static int FLAG_DIAGRAM_SHRINK_X = FLAGS_ELEMENT_END * 0x1;
	protected static int FLAG_DIAGRAM_SHRINK_Y = FLAGS_ELEMENT_END * 0x2;
	
	protected DiagramNode diagram;
	protected double diagramWidth, diagramHeight;
	
	
	public DPDiagram(DiagramNode diagram)
	{
		this( WidgetStyleSheet.defaultStyleSheet, diagram, -1.0, -1.0, false, false );
	}
	
	public DPDiagram(WidgetStyleSheet styleSheet, DiagramNode diagram)
	{
		this( styleSheet, diagram, -1.0, -1.0, false, false );
	}
	
	public DPDiagram(DiagramNode diagram, double width, double height, boolean bShrinkX, boolean bShrinkY)
	{
		this( WidgetStyleSheet.defaultStyleSheet, diagram, width, height, bShrinkX, bShrinkY );
	}
	
	public DPDiagram(WidgetStyleSheet styleSheet, DiagramNode diagram, double width, double height, boolean bShrinkX, boolean bShrinkY)
	{
		super( styleSheet );
		
		this.diagram = diagram;
		this.diagramWidth = width;
		this.diagramHeight = height;
		setFlagValue( FLAG_DIAGRAM_SHRINK_X, bShrinkX );
		setFlagValue( FLAG_DIAGRAM_SHRINK_Y, bShrinkY );
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		diagram.draw( graphics );
	}
	
	

	
	protected void updateRequisitionX()
	{
		double width = diagram.getParentSpaceBoundingBox().getUpperX();
		if ( diagramWidth >= 0.0 )
		{
			if ( testFlag( FLAG_DIAGRAM_SHRINK_X ) )
			{
				width = Math.min( width, diagramWidth );
			}
			else
			{
				width = diagramWidth;
			}
		}
		layoutReqBox.setRequisitionX( width, 0.0 );
	}

	protected void updateRequisitionY()
	{
		double height = diagram.getParentSpaceBoundingBox().getUpperY();
		if ( diagramHeight >= 0.0 )
		{
			if ( testFlag( FLAG_DIAGRAM_SHRINK_Y ) )
			{
				height = Math.min( height, diagramHeight );
			}
			else
			{
				height = diagramHeight;
			}
		}
		layoutReqBox.setRequisitionY( height, 0.0 );
	}
}
