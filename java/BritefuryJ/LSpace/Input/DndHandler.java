//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.datatransfer.Transferable;
import java.awt.geom.AffineTransform;

import javax.swing.TransferHandler;

import BritefuryJ.Graphics.FilledOutlinePainter;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.Math.Xform2;

public abstract class DndHandler
{
	public final static int COPY = TransferHandler.COPY;
	public final static int COPY_OR_MOVE = TransferHandler.COPY_OR_MOVE;
	public final static int LINK = TransferHandler.LINK;
	public final static int MOVE = TransferHandler.MOVE;
	public final static int NONE = TransferHandler.NONE;
	
	public final static int ASPECT_NONE = 0;
	public final static int ASPECT_NORMAL = 0x1;
	public final static int ASPECT_DOC_NODE = 0x2;
	public static final Color dndHighlightPaint = new Color( 1.0f, 0.5f, 0.0f );
	public static final FilledOutlinePainter dndHighlightPainter = new FilledOutlinePainter( new Color( 1.0f, 0.8f, 0.0f, 0.2f ), new Color( 1.0f, 0.5f, 0.0f, 0.5f ) );
	
	
	public static abstract class PotentialDrop
	{
		protected LSElement destElement;


		public PotentialDrop(LSElement destElement)
		{
			this.destElement = destElement;
		}


		public abstract void draw(Graphics2D graphics);
		public abstract void queueRedraw();


		public LSElement getDestElement()
		{
			return destElement;
		}
	}

	
	
	public static void drawCaretDndHighlight(Graphics2D graphics, LSElement dndTargetElement, Marker marker)
	{
		if ( marker != null  &&  marker.isValid() )
		{
			LSContentLeafEditable leaf = marker.getElement();
			
			AffineTransform prevX = graphics.getTransform();
			Paint prevP = graphics.getPaint();
			
			Xform2 x = dndTargetElement.getRootToLocalXform();
			x.apply( graphics );
			graphics.setPaint( dndHighlightPaint );
			
			leaf.drawCaret( graphics, marker );
			
			graphics.setPaint( prevP );
			graphics.setTransform( prevX );
		}
	}

	public abstract boolean isSource(LSElement sourceElement);

	public int getSourceRequestedAction(LSElement sourceElement, PointerInterface pointer, int button)
	{
		return COPY;
	}
	
	public int getSourceRequestedAspect(LSElement sourceElement, PointerInterface pointer, int button)
	{
		return ASPECT_NORMAL;
	}
	
	public Transferable createTransferable(LSElement sourceElement, int aspect)
	{
		return null;
	}
	
	public void exportDone(LSElement sourceElement, Transferable data, int action)
	{
	}
	

	
	
	
	public abstract boolean isDest(LSElement sourceElement);

	public PotentialDrop negotiatePotentialDrop(LSElement destElement, DndDropSwing drop)
	{
		return null;
	}
	
	public boolean acceptDrop(LSElement destElement, DndDropSwing drop)
	{
		return false;
	}
}
