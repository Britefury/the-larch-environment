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
		
		DPBorder halignLeft = styleSheet.border( textStyleSheet.staticText( "hAlign=LEFT" ).alignHLeft() );
		DPBorder halignCentre = styleSheet.border( textStyleSheet.staticText( "hAlign=CENTRE" ).alignHCentre() );
		DPBorder halignRight = styleSheet.border( textStyleSheet.staticText( "hAlign=RIGHT" ).alignHRight() );
		DPBorder halignExpand = styleSheet.border( textStyleSheet.staticText( "hAlign=EXPAND" ).alignHExpand() );
		
		
		DPElement refVBox = styleSheet.vbox( Arrays.asList( new DPElement[] { styleSheet.staticText( "0" ), styleSheet.staticText( "1 (ref-y)" ), styleSheet.staticText( "2" ),
						styleSheet.staticText( "3" ), styleSheet.staticText( "4" ), styleSheet.staticText( "5" ) } ), 1 );
		DPElement refBox = styleSheet.withBackground( new FilledOutlinePainter( new Color( 0.8f, 0.85f, 1.0f ), new Color( 0.0f, 0.25f, 1.0f ) ) ).box( refVBox.pad( 5.0, 5.0 ) );
		

		DPBorder valignBaselines = styleSheet.border( textStyleSheet.staticText( "vAlign=REFY" ).alignVRefY() );
		DPBorder valignBaselinesExpand = styleSheet.border( textStyleSheet.staticText( "vAlign=REFY_EXPAND" ).alignVRefYExpand() );
		DPBorder valignTop = styleSheet.border( textStyleSheet.staticText( "vAlign=TOP" ).alignVTop() );
		DPBorder valignCentre = styleSheet.border( textStyleSheet.staticText( "vAlign=CENTRE" ).alignVCentre() );
		DPBorder valignBottom = styleSheet.border( textStyleSheet.staticText( "vAlign=BOTTOM" ).alignVBottom() );
		DPBorder valignExpand = styleSheet.border( textStyleSheet.staticText( "vAlign=EXPAND" ).alignVExpand() );

		
		DPElement vAlignBox = styleSheet.withHBoxSpacing( 10.0 ).hbox( Arrays.asList( new DPElement[] { valignBaselines.alignVRefYExpand(), valignBaselinesExpand.alignVRefYExpand(),
				valignTop.alignVRefYExpand(), valignCentre.alignVRefYExpand(), valignBottom.alignVRefYExpand(), valignExpand.alignVRefYExpand() } ) );
		
		
		DPElement bottomBox = styleSheet.withHBoxSpacing( 50.0 ).hbox( Arrays.asList( new DPElement[] { refBox.alignVRefYExpand(), vAlignBox.alignVRefYExpand() } ) );
		
		
		DPElement mainBox = styleSheet.withVBoxSpacing( 10.0 ).vbox( Arrays.asList( new DPElement[] { halignLeft.alignHExpand(), halignCentre.alignHExpand(), halignRight.alignHExpand(),
				halignExpand.alignHExpand(), bottomBox } ) );
		return styleSheet.box( mainBox );
	}
}
