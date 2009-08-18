//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComponent;

import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheetField;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldCascading;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldDirect;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldSet;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValues;
import BritefuryJ.Math.Point2;

public class DPMathRoot extends DPContainer
{
	private static class GlyphMetrics
	{
		protected StyleSheetValues styleSheetValues;
		protected double glyphLineWidths[], glyphWidth, barSpacing;

		
		
		public GlyphMetrics(StyleSheetValues styleSheetValues, DPPresentationArea a)
		{
			this( styleSheetValues, a.getComponent() );
		}
		
		public GlyphMetrics(StyleSheetValues styleSheetValues, JComponent component)
		{
			this( styleSheetValues, (Graphics2D)component.getGraphics() );
		}
		
		public GlyphMetrics(StyleSheetValues styleSheetValues, Graphics2D graphics)
		{
			this.styleSheetValues = styleSheetValues;
			
			FontRenderContext frc = graphics.getFontRenderContext();
			LineMetrics metrics = ((Font)styleSheetValues.get( DPMathRoot.fontValueField )).getLineMetrics( " ", frc );
			double height = metrics.getAscent() + metrics.getDescent();
			
			glyphLineWidths = new double[3];
			barSpacing = height * 0.1;
			glyphLineWidths[2] = height * 0.5;
			glyphLineWidths[1] = glyphLineWidths[2] * 0.4;
			glyphLineWidths[0] = glyphLineWidths[2] * 0.3;
			glyphWidth = glyphLineWidths[0] + glyphLineWidths[1] + glyphLineWidths[2];
		}
	}
	
	
	
	protected static ElementStyleSheetField fontField = DPStaticText.fontField;
	protected static ElementStyleSheetField paintField = ElementStyleSheetField.newField( "mathRootPaint", Paint.class );
	protected static ElementStyleSheetField thicknessField = ElementStyleSheetField.newField( "mathRootThickness", Double.class );
	
	protected static StyleSheetValueFieldCascading fontValueField = DPStaticText.fontValueField;
	protected static StyleSheetValueFieldDirect paintValueField = StyleSheetValueFieldDirect.newField( "mathRootPaint", Paint.class, Color.black, paintField );
	protected static StyleSheetValueFieldDirect thicknessValueField = StyleSheetValueFieldDirect.newField( "mathRootThickness", Double.class, 1.5, thicknessField );
	
	
	protected static StyleSheetValueFieldSet useStyleSheetFields_MathRoot = useStyleSheetFields_Element.join( fontValueField, paintValueField, thicknessValueField );

	
	
	
	private static HashMap<StyleSheetValues, GlyphMetrics> metricsTable = new HashMap<StyleSheetValues, GlyphMetrics>();
	
	
	protected DPWidget child;
	protected GlyphMetrics glyphMetrics;
	
	
	
	public DPMathRoot()
	{
		this( null );
	}

	public DPMathRoot(ElementStyleSheet styleSheet)
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
			
			onChildListModified();
			queueResize();
		}
	}
	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		assert child == this.child;
		setChild( null );
	}
	
	

	public List<DPWidget> getChildren()
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
		if ( glyphMetrics == null  ||  glyphMetrics.styleSheetValues != styleSheetValues )
		{
			GlyphMetrics metrics = metricsTable.get( styleSheetValues );
			if ( metrics == null )
			{
				metrics = new GlyphMetrics( styleSheetValues, presentationArea );
				metricsTable.put( styleSheetValues, metrics );
			}
			glyphMetrics = metrics;
		}
	}

	
	
	protected void draw(Graphics2D graphics)
	{
		super.draw( graphics );
		
		if ( child != null  &&  glyphMetrics != null )
		{
			double allocationX = getAllocationX();
			double allocationY = getAllocationY();

			Stroke curStroke = graphics.getStroke();
			Paint curPaint = graphics.getPaint();
			
			double thickness = getThickness();
			
			Stroke stroke = new BasicStroke( (float)thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL );
			graphics.setStroke( stroke );
			graphics.setPaint( getPaint() );
			
			double yOffset = thickness * 0.5;
			double h = allocationY - thickness;

			Path2D.Double path = new Path2D.Double();
			path.moveTo( 0.0, yOffset + h * 0.65 );
			path.lineTo( glyphMetrics.glyphLineWidths[0], yOffset + h * 0.6 );
			path.lineTo( glyphMetrics.glyphLineWidths[0] + glyphMetrics.glyphLineWidths[1], yOffset + h );
			path.lineTo( glyphMetrics.glyphWidth, yOffset );
			path.lineTo( allocationX, yOffset );
			
			graphics.draw( path );
			
			graphics.setStroke( curStroke );
			graphics.setPaint( curPaint );
		}
	}

	
	
	protected void updateRequisitionX()
	{
		if ( child != null  &&  glyphMetrics != null )
		{
			layoutReqBox.setRequisitionX( child.refreshRequisitionX() );
			layoutReqBox.borderX( glyphMetrics.glyphWidth, 0.0 );
		}
		else
		{
			layoutReqBox.clearRequisitionX();
		}
	}

	protected void updateRequisitionY()
	{
		if ( child != null  &&  glyphMetrics != null )
		{
			layoutReqBox.setRequisitionY( child.refreshRequisitionY() );
			layoutReqBox.borderY( glyphMetrics.barSpacing + getThickness(), 0.0 );
		}
		else
		{
			layoutReqBox.clearRequisitionY();
		}
	}
	

	
	
	protected void updateAllocationX()
	{
		if ( child != null  &&  glyphMetrics != null )
		{
			double prevWidth = child.getAllocationX();
			double offset = glyphMetrics.glyphWidth;
			layoutAllocBox.allocateChildX( child.layoutAllocBox, offset, getAllocationX() - offset );
			child.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		if ( child != null  &&  glyphMetrics != null )
		{
			double prevHeight = child.getAllocationY();
			double offset = glyphMetrics.barSpacing + getThickness();
			layoutAllocBox.allocateChildY( child.layoutAllocBox, offset, getAllocationY() - offset );
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
	// Packing parameters
	//
	
	protected PackingParams getDefaultPackingParams()
	{
		return null;
	}
	
	
	//
	//
	// TEXT REPRESENTATION METHODS
	//
	//
	
	protected String computeSubtreeTextRepresentation()
	{
		return child != null  ?  child.getTextRepresentation()  :  "";
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
	
	
	
	protected StyleSheetValueFieldSet getUsedStyleSheetValueFields()
	{
		return useStyleSheetFields_MathRoot;
	}

	
	private Paint getPaint()
	{
		return (Paint)styleSheetValues.get( paintValueField );
	}

	private double getThickness()
	{
		return (Double)styleSheetValues.get( thicknessValueField );
	}
}
