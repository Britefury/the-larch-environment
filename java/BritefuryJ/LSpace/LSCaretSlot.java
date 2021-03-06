//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.LayoutTree.LayoutNode;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeCaretSlot;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.StyleParams.CaretSlotStyleParams;
import BritefuryJ.LSpace.Util.TextVisual;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class LSCaretSlot extends LSContentLeafEditable
{
	public enum SlotType
	{
		SEGMENT_BOUNDARY,
		ITEM_BOUNDARY
	}
	
	
	protected final static int FLAGS_CARETSLOT_START = FLAGS_CONTENTLEAFEDITABLE_END;
	
	protected static final int FLAG_SLOTTYPE_SEGMENT_BOUNDARY = FLAGS_CARETSLOT_START * 0x00;
	protected static final int FLAG_SLOTTYPE_ITEM_BOUNDARY = FLAGS_CARETSLOT_START * 0x01;
	protected static final int _FLAG_SLOTTYPE_MASK = FLAGS_CARETSLOT_START * 0x01;
	
	protected final static int FLAGS_CARETSLOT_END = FLAGS_CARETSLOT_START * 0x2;
	

	
	
	public LSCaretSlot(SlotType slotType)
	{
		this( CaretSlotStyleParams.defaultStyleParams, slotType );
	}
	
	public LSCaretSlot(CaretSlotStyleParams styleParams, SlotType slotType)
	{
		super( styleParams, "" );
		
		if ( slotType == SlotType.SEGMENT_BOUNDARY )
		{
			flags |= FLAG_SLOTTYPE_SEGMENT_BOUNDARY;
		}
		else if ( slotType == SlotType.ITEM_BOUNDARY )
		{
			flags |= FLAG_SLOTTYPE_ITEM_BOUNDARY;
		}
		else
		{
			throw new RuntimeException( "Invalid SlotType" );
		}
		
		layoutNode = new LayoutNodeCaretSlot( this );
	}
	
	

	public TextVisual getVisual()
	{
		return ((CaretSlotStyleParams)styleParams).getVisual();
	}
	
	
	//
	//
	// CARET METHODS
	//
	//
	
	@Override
	public void drawCaret(Graphics2D graphics, Marker c)
	{
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getAllocRefY()  -  layout.getRequisitionBox().getReqRefY();
		AffineTransform current = pushLocalToRootGraphicsTransform( graphics );
		graphics.translate( 0.0, deltaY );
		getVisual().drawCaret( graphics, 0 );
		popGraphicsTransform( graphics, current );
	}

	@Override
	public void drawTextSelection(Graphics2D graphics, int startIndex, int endIndex)
	{
	}



	public int getMarkerRange()
	{
		return textRepresentation.length();
	}

	@Override
	public int getMarkerPositionForPoint(Point2 localPos)
	{
		return 0;
	}


	public Point2 getMarkerPosition(Marker marker)
	{
		if ( marker.getElement() != this )
		{
			throw new RuntimeException( "Marker is not within the bounds of this element" );
		}
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getAllocRefY()  -  layout.getRequisitionBox().getReqRefY();
		return getVisual().getCharacterBoundaryPosition( 0 ).add( new Vector2( 0.0, deltaY ) );
	}
	
	
	//
	//
	// CARET SLOT
	//
	//
	
	public boolean isCaretSlot()
	{
		return true;
	}
	
	
	
	public boolean shouldCaretStopHere(LSContentLeaf prevElement)
	{
		int slotType = flags & _FLAG_SLOTTYPE_MASK;
		if ( slotType == FLAG_SLOTTYPE_SEGMENT_BOUNDARY )
		{
			return LSSegment.getSegmentOf( this ) != LSSegment.getSegmentOf( prevElement );
		}
		else if ( slotType == FLAG_SLOTTYPE_ITEM_BOUNDARY )
		{
			return !prevElement.isCaretSlot();
		}
		else
		{
			throw new RuntimeException();
		}
	}
}
