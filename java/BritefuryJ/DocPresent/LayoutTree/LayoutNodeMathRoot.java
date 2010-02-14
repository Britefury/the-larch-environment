//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPMathRoot;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleParams.MathRootStyleParams;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.LAllocHelper;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;

public class LayoutNodeMathRoot extends ArrangedLayoutNode
{
	public LayoutNodeMathRoot(DPMathRoot element)
	{
		super( element );
	}

	
	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPMathRoot mathRoot = (DPMathRoot)element;
		DPWidget child = mathRoot.getChild();
		if ( child != null )
		{
			MathRootStyleParams s = (MathRootStyleParams)mathRoot.getStyleSheet();
			
			layoutReqBox.setRequisitionX( child.getLayoutNode().refreshRequisitionX() );
			layoutReqBox.borderX( s.getGlyphWidth(), 0.0 );
		}
		else
		{
			layoutReqBox.clearRequisitionX();
		}
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPMathRoot mathRoot = (DPMathRoot)element;
		DPWidget child = mathRoot.getChild();
		if ( child != null )
		{
			MathRootStyleParams s = (MathRootStyleParams)mathRoot.getStyleSheet();
			
			layoutReqBox.setRequisitionY( child.getLayoutNode().refreshRequisitionY() );
			layoutReqBox.borderY( s.getBarSpacing() + s.getThickness(), 0.0 );
		}
		else
		{
			layoutReqBox.clearRequisitionY();
		}
	}
	
	
	
	protected void updateAllocationX()
	{
		DPMathRoot mathRoot = (DPMathRoot)element;
		DPWidget child = mathRoot.getChild();
		if ( child != null )
		{
			MathRootStyleParams s = (MathRootStyleParams)mathRoot.getStyleSheet();

			LayoutNode childLayout = child.getLayoutNode();
			double prevWidth = childLayout.getAllocationBox().getAllocationX();
			double offset = s.getGlyphWidth();
			LAllocHelper.allocateChildXAligned( childLayout.getAllocationBox(), childLayout.getRequisitionBox(), child.getAlignmentFlags(), offset, getAllocationBox().getAllocationX() - offset );
			childLayout.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		DPMathRoot mathRoot = (DPMathRoot)element;
		DPWidget child = mathRoot.getChild();
		if ( child != null )
		{
			MathRootStyleParams s = (MathRootStyleParams)mathRoot.getStyleSheet();

			LayoutNode childLayout = child.getLayoutNode();
			LAllocV prevAllocV = childLayout.getAllocationBox().getAllocV();
			double offset = s.getBarSpacing() + s.getThickness();
			LAllocHelper.allocateChildYAligned( childLayout.getAllocationBox(), childLayout.getRequisitionBox(), child.getAlignmentFlags(), offset, getAllocationBox().getAllocV().borderY( offset, 0.0 ) );
			childLayout.refreshAllocationY( prevAllocV );
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		DPMathRoot mathRoot = (DPMathRoot)element;
		DPWidget child = mathRoot.getChild();
		if ( child == null )
		{
			return null;
		}
		else
		{
			return getLeafClosestToLocalPointFromChild( child, localPos, filter );
		}
	}

	
	
	
	//
	// Focus navigation methods
	//
	
	public List<DPWidget> horizontalNavigationList()
	{
		DPMathRoot mathRoot = (DPMathRoot)element;
		List<DPWidget> children = mathRoot.getLayoutChildren();
		if ( children.size() > 0 )
		{
			return children;
		}
		else
		{
			return null;
		}
	}
}
