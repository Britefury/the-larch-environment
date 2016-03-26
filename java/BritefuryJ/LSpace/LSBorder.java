//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.awt.*;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Graphics.FilledBorder;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeBorder;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;


public class LSBorder extends LSBin
{
	protected final static int FLAGS_BORDER_START = FLAGS_BIN_END;
	protected final static int FLAG_BORDER_CLIP = FLAGS_BORDER_START  *  0x1;
	protected final static int FLAGS_BORDER_END = FLAGS_BORDER_START  <<  1;



	public static final FilledBorder defaultBorder = new FilledBorder( 0.0, 0.0, 0.0, 0.0 );
	
	protected AbstractBorder border;
	
	
	
	public LSBorder(AbstractBorder border, LSElement child)
	{
		this( border, ContainerStyleParams.defaultStyleParams, child );
	}

	public LSBorder(AbstractBorder border, ContainerStyleParams styleParams, LSElement child)
	{
		super( styleParams, child );
		
		if ( border == null )
		{
			throw new RuntimeException( "Cannot have null border" );
		}
		
		this.border = border;
		layoutNode = new LayoutNodeBorder( this );
	}



	public boolean isClippingEnabled()
	{
		return testFlag( FLAG_BORDER_CLIP );
	}

	public void enableClipping()
	{
		setFlag( FLAG_BORDER_CLIP );
	}

	public void disableClipping()
	{
		clearFlag( FLAG_BORDER_CLIP );
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
	
	
	public boolean isRedrawRequiredOnHover()
	{
		return super.isRedrawRequiredOnHover()  ||  border.isHighlightable();
	}
	
	
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		border.drawBackground( graphics, 0.0, 0.0, getActualWidth(), getActualHeight(), isHoverActive() );
		Shape borderClip = border.getClipShape( graphics, 0.0, 0.0, getActualWidth(), getActualHeight() );
		if ( isClippingEnabled() )
		{
			Shape clipShape = graphics.getClip();
			graphics.clip( borderClip );
			super.handleDrawBackground( graphics, areaBox );
			graphics.setClip( clipShape );
		}
		else
		{
			super.handleDrawBackground( graphics, areaBox );
		}
	}

	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		Shape borderClip = border.getClipShape( graphics, 0.0, 0.0, getActualWidth(), getActualHeight() );
		if ( isClippingEnabled() )
		{
			Shape clipShape = graphics.getClip();
			graphics.clip( borderClip );
			super.handleDraw( graphics, areaBox );
			graphics.setClip( clipShape );
		}
		else
		{
			super.handleDraw( graphics, areaBox );
		}
		border.draw( graphics, 0.0, 0.0, getActualWidth(), getActualHeight(), isHoverActive() );
	}
}
