//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Layout.HorizontalLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheetField;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldChildPack;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldDirect;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldPack;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldSet;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValues;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;



public class DPHBox extends DPAbstractBox
{
	protected static ElementStyleSheetField xSpacingField = ElementStyleSheetField.newField( "xSpacing", Double.class );
	protected static ElementStyleSheetField vAlignmentField = ElementStyleSheetField.newField( "vAlignment", VAlignment.class );
	
	protected static StyleSheetValueFieldDirect xSpacingValueField = StyleSheetValueFieldDirect.newField( "xSpacing", Double.class, 0.0, xSpacingField );
	protected static StyleSheetValueFieldDirect vAlignmentValueField = StyleSheetValueFieldDirect.newField( "vAlignment", VAlignment.class, VAlignment.CENTRE, vAlignmentField );
	
	
	protected static StyleSheetValueFieldSet useStyleSheetFields_HBox = useStyleSheetFields_Element.join( xSpacingValueField, vAlignmentValueField );
	
	
	public static ElementStyleSheetField childPack_xPaddingField = HorizontalLayout.childPack_xPaddingField;
	public static ElementStyleSheetField pack_xPaddingField = HorizontalLayout.pack_xPaddingField;
	public static ElementStyleSheetField childPack_xExpandField = HorizontalLayout.childPack_xExpandField;
	public static ElementStyleSheetField pack_xExpandField = HorizontalLayout.pack_xExpandField;
	
	public static StyleSheetValueFieldChildPack childPack_xPaddingValueField = HorizontalLayout.childPack_xPaddingValueField;
	public static StyleSheetValueFieldPack pack_xPaddingValueField = HorizontalLayout.pack_xPaddingValueField;
	public static StyleSheetValueFieldChildPack childPack_xExpandValueField = HorizontalLayout.childPack_xExpandValueField;
	public static StyleSheetValueFieldPack pack_xExpandValueField = HorizontalLayout.pack_xExpandValueField;

	
	
	public DPHBox()
	{
		this( null );
	}
	
	public DPHBox(ElementStyleSheet styleSheet)
	{
		super( styleSheet );
	}
	
	
	
	
	protected void updateRequisitionX()
	{
		refreshCollation();
		
		LReqBox[] childBoxes = new LReqBox[collationLeaves.length];
		StyleSheetValues styleSheets[] = new StyleSheetValues[collationLeaves.length];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionX();
			styleSheets[i] = collationLeaves[i].styleSheetValues;
		}

		HorizontalLayout.computeRequisitionX( layoutReqBox, childBoxes, getXSpacing(), styleSheets );
	}

	protected void updateRequisitionY()
	{
		LReqBox[] childBoxes = new LReqBox[collationLeaves.length];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionY();
		}

		HorizontalLayout.computeRequisitionY( layoutReqBox, childBoxes, getVAlignment() );
	}
	

	

	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		double prevWidths[] = getCollatedChildrenAllocationX();
		StyleSheetValues styleSheets[] = getCollatedChildrenStyleSheetValues();
		
		HorizontalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getXSpacing(), styleSheets );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationX( prevWidths[i] );
			i++;
		}
	}
	
	
	
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		double prevHeights[] = getCollatedChildrenAllocationY();
		
		HorizontalLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getVAlignment() );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationY( prevHeights[i] );
			i++;
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointHorizontal( Arrays.asList( collationLeaves ), localPos, filter );
	}



	protected AABox2[] computeCollatedBranchBoundsBoxes(DPContainer collatedBranch, int rangeStart, int rangeEnd)
	{
		refreshCollation();
		
		DPWidget startLeaf = collationLeaves[rangeStart];
		DPWidget endLeaf = collationLeaves[rangeEnd-1];
		double xStart = startLeaf.getPositionInParentSpaceX();
		double xEnd = endLeaf.getPositionInParentSpaceX()  +  endLeaf.getAllocationInParentSpaceX();
		AABox2 box = new AABox2( xStart, 0.0, xEnd, getAllocationY() );
		return new AABox2[] { box };
	}

	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getCollatedChildren();
	}
	
	
	protected StyleSheetValueFieldSet getUsedStyleSheetValueFields()
	{
		return useStyleSheetFields_HBox;
	}

	
	protected VAlignment getVAlignment()
	{
		return (VAlignment)styleSheetValues.get( vAlignmentValueField );
	}
	
	protected double getXSpacing()
	{
		return (Double)styleSheetValues.get( xSpacingValueField );
	}
	
	
	
	public static ElementStyleSheet styleSheet()
	{
		return styleSheet( VAlignment.BASELINES, 0.0, false, 0.0 );
	}

	public static ElementStyleSheet styleSheet(VAlignment alignment, double spacing, boolean bExpand, double padding)
	{
		return new ElementStyleSheet( new String[] { "vAlignment", "xSpacing", "childPack_xExpand", "childPack_xPadding" }, new Object[] { alignment, spacing, bExpand, padding } );
	}
}
