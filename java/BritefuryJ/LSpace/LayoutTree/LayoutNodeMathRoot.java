//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSMathRoot;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.LAllocHelper;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.LSpace.StyleParams.MathRootStyleParams;
import BritefuryJ.Math.Point2;

public class LayoutNodeMathRoot extends ArrangedLayoutNode
{
	public LayoutNodeMathRoot(LSMathRoot element)
	{
		super( element );
	}

	
	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSMathRoot mathRoot = (LSMathRoot)element;
		LSElement child = mathRoot.getChild();
		if ( child != null )
		{
			MathRootStyleParams s = (MathRootStyleParams)mathRoot.getStyleParams();
			
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
		LSMathRoot mathRoot = (LSMathRoot)element;
		LSElement child = mathRoot.getChild();
		if ( child != null )
		{
			MathRootStyleParams s = (MathRootStyleParams)mathRoot.getStyleParams();
			
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
		LSMathRoot mathRoot = (LSMathRoot)element;
		LSElement child = mathRoot.getChild();
		if ( child != null )
		{
			MathRootStyleParams s = (MathRootStyleParams)mathRoot.getStyleParams();

			LayoutNode childLayout = child.getLayoutNode();
			double prevWidth = childLayout.getAllocationBox().getAllocWidth();
			double offset = s.getGlyphWidth();
			LAllocHelper.allocateChildXAligned( childLayout.getAllocationBox(), childLayout.getRequisitionBox(), child.getAlignmentFlags(), offset, getAllocationBox().getAllocWidth() - offset );
			childLayout.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		LSMathRoot mathRoot = (LSMathRoot)element;
		LSElement child = mathRoot.getChild();
		if ( child != null )
		{
			MathRootStyleParams s = (MathRootStyleParams)mathRoot.getStyleParams();

			LayoutNode childLayout = child.getLayoutNode();
			LAllocV prevAllocV = childLayout.getAllocationBox().getAllocV();
			double offset = s.getBarSpacing() + s.getThickness();
			LAllocHelper.allocateChildYAligned( childLayout.getAllocationBox(), childLayout.getRequisitionBox(), child.getAlignmentFlags(), offset, getAllocationBox().getAllocV().borderY( offset, 0.0 ) );
			childLayout.refreshAllocationY( prevAllocV );
		}
	}
	
	
	
	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		LSMathRoot mathRoot = (LSMathRoot)element;
		LSElement child = mathRoot.getChild();
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
	
	public List<LSElement> horizontalNavigationList()
	{
		LSMathRoot mathRoot = (LSMathRoot)element;
		List<LSElement> children = mathRoot.getLayoutChildren();
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
