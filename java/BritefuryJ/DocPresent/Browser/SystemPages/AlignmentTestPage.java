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
import BritefuryJ.DocPresent.DPElement;
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

	protected DPElement createContents()
	{
		PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
		PrimitiveStyleSheet textStyleSheet = styleSheet.withBackground( new FilledOutlinePainter( new Color( 1.0f, 1.0f, 0.7f ), new Color( 1.0f, 1.0f, 0.0f ) ) );
		
		DPBorder halignLeft = styleSheet.border( textStyleSheet.text( "hAlign=LEFT" ).alignHLeft() );
		DPBorder halignCentre = styleSheet.border( textStyleSheet.text( "hAlign=CENTRE" ).alignHCentre() );
		DPBorder halignRight = styleSheet.border( textStyleSheet.text( "hAlign=RIGHT" ).alignHRight() );
		DPBorder halignExpand = styleSheet.border( textStyleSheet.text( "hAlign=EXPAND" ).alignHExpand() );
		
		
		DPElement spacer = styleSheet.withBackground( new FilledOutlinePainter( new Color( 0.8f, 0.85f, 1.0f ), new Color( 0.0f, 0.25f, 1.0f ) ) ).box( styleSheet.text( "SPACER" ).pad( 5.0, 50.0 ) );
		

		DPBorder valignBaselines = styleSheet.border( textStyleSheet.text( "vAlign=REFY" ).alignVBaselines() );
		DPBorder valignBaselinesExpand = styleSheet.border( textStyleSheet.text( "vAlign=REFY_EXPAND" ).alignVBaselinesExpand() );
		DPBorder valignTop = styleSheet.border( textStyleSheet.text( "vAlign=TOP" ).alignVTop() );
		DPBorder valignCentre = styleSheet.border( textStyleSheet.text( "vAlign=CENTRE" ).alignVCentre() );
		DPBorder valignBottom = styleSheet.border( textStyleSheet.text( "vAlign=BOTTOM" ).alignVBottom() );
		DPBorder valignExpand = styleSheet.border( textStyleSheet.text( "vAlign=EXPAND" ).alignVExpand() );

		
		DPElement vAlignBox = styleSheet.withHBoxSpacing( 10.0 ).hbox( Arrays.asList( new DPElement[] { valignBaselines.alignVExpand(), valignBaselinesExpand.alignVExpand(), valignTop.alignVExpand(),
				valignCentre.alignVExpand(), valignBottom.alignVExpand(), valignExpand.alignVExpand() } ) );
		
		
		DPElement bottomBox = styleSheet.withHBoxSpacing( 50.0 ).hbox( Arrays.asList( new DPElement[] { spacer, vAlignBox.alignVExpand() } ) );
		
		
		return styleSheet.withVBoxSpacing( 10.0 ).vbox( Arrays.asList( new DPElement[] { halignLeft.alignHExpand(), halignCentre.alignHExpand(), halignRight.alignHExpand(),
				halignExpand.alignHExpand(), bottomBox } ) );
	}
}
