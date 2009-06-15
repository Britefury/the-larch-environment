//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.StyleSheets.MathRootStyleSheet;
import BritefuryJ.Math.Point2;

public class DPMathRoot extends DPContainer
{
	protected DPWidget child;
	
	
	
	public DPMathRoot()
	{
		this( MathRootStyleSheet.defaultStyleSheet );
	}

	public DPMathRoot(MathRootStyleSheet styleSheet)
	{
		super( styleSheet );
	}
	
	
	
	public DPWidget getChild()
	{
		return child;
	}
	
	public void setChild(DPWidget child)
	{
		if ( child != this.child )
		{
			DPWidget prevChild = this.child;
			
			if ( prevChild != null )
			{
				unregisterChild( prevChild );
				registeredChildren.remove( prevChild );
			}
			
			this.child = child;
			
			if ( this.child != null )
			{
				registeredChildren.add( child );
				registerChild( child, null );				
			}
			
			queueResize();
		}
	}
	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		assert child == this.child;
		setChild( null );
	}
	
	

	protected List<DPWidget> getChildren()
	{
		if ( child != null )
		{
			DPWidget[] children = { child };
			return Arrays.asList( children );
		}
		else
		{
			DPWidget[] children = {};
			return Arrays.asList( children );
		}
	}

	
	

	protected void onRealise()
	{
		super.onRealise();
		MathRootStyleSheet s = (MathRootStyleSheet)styleSheet;
		s.realise( presentationArea );
	}

	
	
	protected void draw(Graphics2D graphics)
	{
		super.draw( graphics );
		
		if ( child != null )
		{
			double allocationX = layoutBox.getAllocationX();
			double allocationY = layoutBox.getAllocationY();

			MathRootStyleSheet s = (MathRootStyleSheet)styleSheet;
			
			Stroke curStroke = graphics.getStroke();
			Paint curPaint = graphics.getPaint();
			
			double thickness = s.getThickness();
			
			Stroke stroke = new BasicStroke( (float)thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL );
			graphics.setStroke( stroke );
			graphics.setPaint( s.getColour() );
			
			double yOffset = thickness * 0.5;
			double glyphWidth = s.getGlyphWidth();
			double glyphLineWidths[] = s.getGlyphLineWidths();
			double h = allocationY - thickness;

			Path2D.Double path = new Path2D.Double();
			path.moveTo( 0.0, yOffset + h * 0.65 );
			path.lineTo( glyphLineWidths[0], yOffset + h * 0.6 );
			path.lineTo( glyphLineWidths[0] + glyphLineWidths[1], yOffset + h );
			path.lineTo( glyphWidth, yOffset );
			path.lineTo( allocationX, yOffset );
			
			graphics.draw( path );
			
			graphics.setStroke( curStroke );
			graphics.setPaint( curPaint );
		}
	}

	
	
	protected void updateRequisitionX()
	{
		if ( child != null )
		{
			MathRootStyleSheet s = (MathRootStyleSheet)styleSheet;
			
			layoutBox.setRequisitionX( child.refreshRequisitionX() );
			layoutBox.borderX( s.getGlyphWidth(), 0.0 );
		}
		else
		{
			layoutBox.clearRequisitionX();
		}
	}

	protected void updateRequisitionY()
	{
		if ( child != null )
		{
			MathRootStyleSheet s = (MathRootStyleSheet)styleSheet;
			
			layoutBox.setRequisitionY( child.refreshRequisitionY() );
			layoutBox.borderY( s.getBarSpacing() + s.getThickness(), 0.0 );
		}
		else
		{
			layoutBox.clearRequisitionY();
		}
	}
	

	
	
	protected void updateAllocationX()
	{
		if ( child != null )
		{
			MathRootStyleSheet s = (MathRootStyleSheet)styleSheet;

			double prevWidth = child.layoutBox.getAllocationX();
			double offset = s.getGlyphWidth();
			layoutBox.allocateChildX( child.layoutBox, offset, layoutBox.getAllocationX() - offset );
			child.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		if ( child != null )
		{
			MathRootStyleSheet s = (MathRootStyleSheet)styleSheet;

			double prevHeight = child.layoutBox.getAllocationY();
			double offset = s.getBarSpacing() + s.getThickness();
			layoutBox.allocateChildY( child.layoutBox, offset, layoutBox.getAllocationY() - offset );
			child.refreshAllocationY( prevHeight );
		}
	}

	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		if ( child == null )
		{
			return null;
		}
		else
		{
			return getLeafClosestToLocalPointFromChild( registeredChildren.get( 0 ), localPos, filter );
		}
	}

	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		if ( child != null )
		{
			DPWidget[] navList = { child };
			return Arrays.asList( navList );
		}
		else
		{
			return null;
		}
	}
}
