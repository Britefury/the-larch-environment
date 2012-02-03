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
import java.util.List;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeMathRoot;
import BritefuryJ.DocPresent.StyleParams.MathRootStyleParams;

public class DPMathRoot extends DPContainerNonOverlayed
{
	protected DPElement child;
	
	
	
	public DPMathRoot()
	{
		this( MathRootStyleParams.defaultStyleParams);
	}

	public DPMathRoot(MathRootStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeMathRoot( this );
	}
	
	
	
	public DPElement getChild()
	{
		return child;
	}
	
	public void setChild(DPElement child)
	{
		if ( child != this.child )
		{
			if ( child.getLayoutNode() == null )
			{
				throw new ChildHasNoLayoutException();
			}

			DPElement prevChild = this.child;
			
			if ( prevChild != null )
			{
				unregisterChild( prevChild );
				registeredChildren.remove( prevChild );
			}
			
			this.child = child;
			
			if ( child != null )
			{
				registeredChildren.add( child );
				registerChild( child );				
			}
			
			onChildListModified();
			queueResize();
		}
	}
	
	
	protected void replaceChildWithEmpty(DPElement child)
	{
		assert child == this.child;
		setChild( null );
	}
	
	protected void replaceChild(DPElement child, DPElement replacement)
	{
		assert child == this.child;
		setChild( replacement );
	}
	
	

	public List<DPElement> getChildren()
	{
		return registeredChildren;
	}

	
	public boolean isSingleElementContainer()
	{
		return true;
	}

	
	

	public boolean isRedrawRequiredOnHover()
	{
		MathRootStyleParams s = (MathRootStyleParams)styleParams;
		return super.isRedrawRequiredOnHover()  ||  s.getHoverSymbolPaint() != null;
	}
	

	protected void draw(Graphics2D graphics)
	{
		super.draw( graphics );
		
		if ( child != null )
		{
			double width = getActualWidth();
			double height = getActualHeight();

			MathRootStyleParams s = (MathRootStyleParams)styleParams;
			Paint symbolPaint;
			if ( testFlag( FLAG_HOVER ) )
			{
				Paint hoverSymbolPaint = s.getHoverSymbolPaint();
				symbolPaint = hoverSymbolPaint != null  ?  hoverSymbolPaint  :  s.getSymbolPaint();
			}
			else
			{
				symbolPaint = s.getSymbolPaint();
			}
			
			Stroke curStroke = graphics.getStroke();
			Paint curPaint = graphics.getPaint();
			
			double thickness = s.getThickness();
			
			Stroke stroke = new BasicStroke( (float)thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL );
			graphics.setStroke( stroke );
			graphics.setPaint( symbolPaint);
			
			double yOffset = thickness * 0.5;
			double glyphWidth = s.getGlyphWidth();
			double glyphLineWidths[] = s.getGlyphLineWidths();
			double heightBelowBar = height - thickness;

			Path2D.Double path = new Path2D.Double();
			path.moveTo( 0.0, yOffset + heightBelowBar * 0.65 );
			path.lineTo( glyphLineWidths[0], yOffset + heightBelowBar * 0.6 );
			path.lineTo( glyphLineWidths[0] + glyphLineWidths[1], yOffset + heightBelowBar );
			path.lineTo( glyphWidth, yOffset );
			path.lineTo( width, yOffset );
			
			graphics.draw( path );
			
			graphics.setStroke( curStroke );
			graphics.setPaint( curPaint );
		}
	}
}
