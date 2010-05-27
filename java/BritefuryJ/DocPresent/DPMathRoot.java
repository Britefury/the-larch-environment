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

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeMathRoot;
import BritefuryJ.DocPresent.StyleParams.MathRootStyleParams;

public class DPMathRoot extends DPContainer
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
	
	protected DPMathRoot(DPMathRoot element)
	{
		super( element );
		
		layoutNode = new LayoutNodeMathRoot( this );
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	protected void clonePostConstuct(DPElement src)
	{
		super.clonePostConstuct( src );
		DPElement child = ((DPMathRoot)src).getChild();
		if ( child != null )
		{
			setChild( child.clonePresentationSubtree() );
		}
	}
	
	
	public DPElement clonePresentationSubtree()
	{
		DPMathRoot clone = new DPMathRoot( this );
		clone.clonePostConstuct( this );
		return clone;
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
	
	

	public List<DPElement> getChildren()
	{
		if ( child != null )
		{
			DPElement[] children = { child };
			return Arrays.asList( children );
		}
		else
		{
			DPElement[] children = {};
			return Arrays.asList( children );
		}
	}

	
	

	protected void onRealise()
	{
		super.onRealise();
		MathRootStyleParams s = (MathRootStyleParams) styleParams;
		s.realise( rootElement );
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
			double width = getWidth();
			double height = getHeight();

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

	
	
	//
	//
	// TEXT REPRESENTATION METHODS
	//
	//
	
	protected String computeSubtreeTextRepresentation()
	{
		return child != null  ?  child.getTextRepresentation()  :  "";
	}
}
