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

import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.Layout.VerticalLayout;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheetField;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldChildPack;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldDirect;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldPack;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldSet;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValues;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;




public class DPVBox extends DPAbstractBox
{
	protected static ElementStyleSheetField ySpacingField = ElementStyleSheetField.newField( "ySpacing", Double.class );
	protected static ElementStyleSheetField hAlignmentField = ElementStyleSheetField.newField( "hAlignment", HAlignment.class );
	protected static ElementStyleSheetField vTypesettingField = ElementStyleSheetField.newField( "vTypesetting", VTypesetting.class );
	
	protected static StyleSheetValueFieldDirect ySpacingValueField = StyleSheetValueFieldDirect.newField( "ySpacing", Double.class, 0.0, ySpacingField );
	protected static StyleSheetValueFieldDirect hAlignmentValueField = StyleSheetValueFieldDirect.newField( "hAlignment", HAlignment.class, HAlignment.EXPAND, hAlignmentField );
	protected static StyleSheetValueFieldDirect vTypesettingValueField = StyleSheetValueFieldDirect.newField( "vTypesetting", VTypesetting.class, VTypesetting.NONE, vTypesettingField );
	
	
	protected static StyleSheetValueFieldSet useStyleSheetFields_VBox = useStyleSheetFields_Element.join( ySpacingValueField, hAlignmentValueField, vTypesettingValueField );
	
	
	public static ElementStyleSheetField childPack_yPaddingField = VerticalLayout.childPack_yPaddingField;
	public static ElementStyleSheetField pack_yPaddingField = VerticalLayout.pack_yPaddingField;
	public static ElementStyleSheetField childPack_yExpandField = VerticalLayout.childPack_yExpandField;
	public static ElementStyleSheetField pack_yExpandField = VerticalLayout.pack_yExpandField;
	
	public static StyleSheetValueFieldChildPack childPack_yPaddingValueField = VerticalLayout.childPack_yPaddingValueField;
	public static StyleSheetValueFieldPack pack_yPaddingValueField = VerticalLayout.pack_yPaddingValueField;
	public static StyleSheetValueFieldChildPack childPack_yExpandValueField = VerticalLayout.childPack_yExpandValueField;
	public static StyleSheetValueFieldPack pack_yExpandValueField = VerticalLayout.pack_yExpandValueField;

	
	
	public static class InvalidTypesettingException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	

	
	public DPVBox()
	{
		this( null );
	}
	
	public DPVBox(ElementStyleSheet styleSheet)
	{
		super( styleSheet );
	}
	
	
	
	
	protected void updateRequisitionX()
	{
		refreshCollation();
		
		LReqBox childBoxes[] = new LReqBox[collationLeaves.length];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionX();
		}

		VerticalLayout.computeRequisitionX( layoutReqBox, childBoxes );
	}

	protected void updateRequisitionY()
	{
		LReqBox childBoxes[] = new LReqBox[collationLeaves.length];
		StyleSheetValues styleSheets[] = new StyleSheetValues[collationLeaves.length];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionY();
			styleSheets[i] = collationLeaves[i].styleSheetValues;
		}

		VerticalLayout.computeRequisitionY( layoutReqBox, childBoxes, getVTypesetting(), getYSpacing(), styleSheets );
	}

	

	
	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		double prevWidths[] = getCollatedChildrenAllocationX();
		
		VerticalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getHAlignment() );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationX( prevWidths[i] );
			i++;
		}
	}

	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		double prevHeights[] = getCollatedChildrenAllocationY();
		StyleSheetValues styleSheets[] = getCollatedChildrenStyleSheetValues();
		
		VerticalLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getYSpacing(), styleSheets );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationY( prevHeights[i] );
			i++;
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( Arrays.asList( collationLeaves ), localPos, filter );
	}


	
	protected AABox2[] computeCollatedBranchBoundsBoxes(DPContainer collatedBranch, int rangeStart, int rangeEnd)
	{
		refreshCollation();
		
		DPWidget startLeaf = collationLeaves[rangeStart];
		DPWidget endLeaf = collationLeaves[rangeEnd-1];
		double yStart = startLeaf.getPositionInParentSpaceY();
		double yEnd = endLeaf.getPositionInParentSpaceY()  +  endLeaf.getAllocationInParentSpaceY();
		AABox2 box = new AABox2( 0.0, yStart, getAllocationX(), yEnd );
		return new AABox2[] { box };
	}

	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getCollatedChildren();
	}
	
	protected List<DPWidget> verticalNavigationList()
	{
		return getCollatedChildren();
	}



	
	protected StyleSheetValueFieldSet getUsedStyleSheetValueFields()
	{
		return useStyleSheetFields_VBox;
	}

	
	protected HAlignment getHAlignment()
	{
		return (HAlignment)styleSheetValues.get( hAlignmentValueField );
	}
	
	protected VTypesetting getVTypesetting()
	{
		return (VTypesetting)styleSheetValues.get( vTypesettingValueField );
	}
	
	protected double getYSpacing()
	{
		return (Double)styleSheetValues.get( ySpacingValueField );
	}
	
	
	
	public static ElementStyleSheet styleSheet(VTypesetting typesetting, HAlignment alignment, double spacing, boolean bExpand, double padding)
	{
		return new ElementStyleSheet( new String[] { "vTypesetting", "hAlignment", "ySpacing", "childPack_yExpand", "childPack_yPadding" }, new Object[] { typesetting, alignment, spacing, bExpand, padding } );
	}
}
