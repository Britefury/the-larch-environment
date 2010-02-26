//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class AlignmentTestPage extends SystemPage
{
	protected AlignmentTestPage()
	{
		register( "alignment" );
	}
	
	
	public String getTitle()
	{
		return "Alignment test";
	}
	
	protected String getDescription()
	{
		return "Demonstrates horizontal and vertical alignment options.";
	}

	protected DPWidget createContents()
	{
		PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
		PrimitiveStyleSheet textStyleSheet = styleSheet.withBackground( new FilledOutlinePainter( new Color( 1.0f, 1.0f, 0.7f ), new Color( 1.0f, 1.0f, 0.0f ) ) );
		
		DPBorder halignLeft = styleSheet.border( textStyleSheet.text( "hAlign=left" ).alignHLeft() );
		DPBorder halignCentre = styleSheet.border( textStyleSheet.text( "hAlign=centre" ).alignHCentre() );
		DPBorder halignRight = styleSheet.border( textStyleSheet.text( "hAlign=right" ).alignHRight() );
		DPBorder halignExpand = styleSheet.border( textStyleSheet.text( "hAlign=expand" ).alignHExpand() );
		
		
		DPWidget spacer = styleSheet.withBackground( new FilledOutlinePainter( new Color( 0.8f, 0.85f, 1.0f ), new Color( 0.0f, 0.25f, 1.0f ) ) ).box( styleSheet.text( "SPACER" ).pad( 5.0, 200.0 ) );
		

		DPBorder valignBaselines = styleSheet.border( textStyleSheet.text( "vAlign=ref_y" ).alignVBaselines() );
		DPBorder valignBaselinesExpand = styleSheet.border( textStyleSheet.text( "vAlign=ref_y-expand" ).alignVBaselinesExpand() );
		DPBorder valignTop = styleSheet.border( textStyleSheet.text( "vAlign=top" ).alignVTop() );
		DPBorder valignCentre = styleSheet.border( textStyleSheet.text( "vAlign=centre" ).alignVCentre() );
		DPBorder valignBottom = styleSheet.border( textStyleSheet.text( "vAlign=bottom" ).alignVBottom() );
		DPBorder valignExpand = styleSheet.border( textStyleSheet.text( "vAlign=expand" ).alignVExpand() );

		
		DPWidget vAlignBox = styleSheet.withHBoxSpacing( 10.0 ).hbox( Arrays.asList( new DPWidget[] { valignBaselines.alignVExpand(), valignBaselinesExpand.alignVExpand(), valignTop.alignVExpand(),
				valignCentre.alignVExpand(), valignBottom.alignVExpand(), valignExpand.alignVExpand() } ) );
		
		
		DPWidget bottomBox = styleSheet.withHBoxSpacing( 50.0 ).hbox( Arrays.asList( new DPWidget[] { spacer, vAlignBox.alignVExpand() } ) );
		
		
		return styleSheet.withVBoxSpacing( 10.0 ).vbox( Arrays.asList( new DPWidget[] { halignLeft.alignHExpand(), halignCentre.alignHExpand(), halignRight.alignHExpand(),
				halignExpand.alignHExpand(), bottomBox } ) );
	}
}
