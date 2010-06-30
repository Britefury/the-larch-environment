//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class AlignmentTestPage extends SystemPage
{
	protected AlignmentTestPage()
	{
		register( "tests.alignment" );
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
		PrimitiveStyleSheet dividerStyleSheet = styleSheet.withShapePainter( new FillPainter( new Color( 1.0f, 0.5f, 0.0f ) ) );
		PrimitiveStyleSheet subtitleStyleSheet = styleSheet.withFontSize( 16 ).withFontFace( "Serif" ).withFontBold( true );
		PrimitiveStyleSheet sectionStyleSheet = styleSheet.withVBoxSpacing( 5.0 );
		
		DPElement halignTitle = subtitleStyleSheet.staticText( "Horizontal alignment" ); 
		DPBorder halignLeft = styleSheet.border( textStyleSheet.staticText( "hAlign=LEFT" ).alignHLeft() );
		DPBorder halignCentre = styleSheet.border( textStyleSheet.staticText( "hAlign=CENTRE" ).alignHCentre() );
		DPBorder halignRight = styleSheet.border( textStyleSheet.staticText( "hAlign=RIGHT" ).alignHRight() );
		DPBorder halignExpand = styleSheet.border( textStyleSheet.staticText( "hAlign=EXPAND" ).alignHExpand() );
		DPElement halignSection = sectionStyleSheet.vbox( new DPElement[] { halignTitle, halignLeft.alignHExpand(), halignCentre.alignHExpand(), halignRight.alignHExpand(), halignExpand.alignHExpand() } );
		
		
		DPElement halignInHBoxTitle = subtitleStyleSheet.staticText( "Horizontal alignment in h-box" ); 
		DPElement halignHBPack = textStyleSheet.staticText( "PACK" ).alignHPack();
		DPElement halignHBLeft = textStyleSheet.staticText( "LEFT" ).alignHLeft();
		DPElement halignHBCentre = textStyleSheet.staticText( "CENTRE" ).alignHCentre();
		DPElement halignHBRight = textStyleSheet.staticText( "RIGHT" ).alignHRight();
		DPElement halignHBExpand = textStyleSheet.staticText( "EXPAND" ).alignHExpand();
		DPElement hAlignHBox = styleSheet.border( styleSheet.hbox( new DPElement[] {
				halignHBPack, dividerStyleSheet.box( 1.0, 1.0 ).alignVExpand(),
				halignHBLeft, dividerStyleSheet.box( 1.0, 1.0 ).alignVExpand(),
				halignHBCentre, dividerStyleSheet.box( 1.0, 1.0 ).alignVExpand(),
				halignHBRight, dividerStyleSheet.box( 1.0, 1.0 ).alignVExpand(),
				halignHBExpand
		} ).alignHExpand() );
		DPElement halignHBoxSection = sectionStyleSheet.vbox( new DPElement[] { halignInHBoxTitle, hAlignHBox.alignHExpand() } );

		DPElement valignTitle = subtitleStyleSheet.staticText( "Vertical alignment" ); 

		DPElement refVBox = styleSheet.vbox( new DPElement[] { styleSheet.staticText( "0" ), styleSheet.staticText( "1 (ref-y)" ), styleSheet.staticText( "2" ),
						styleSheet.staticText( "3" ), styleSheet.staticText( "4" ), styleSheet.staticText( "5" ) }, 1 );
		DPElement refBox = styleSheet.withBackground( new FilledOutlinePainter( new Color( 0.8f, 0.85f, 1.0f ), new Color( 0.0f, 0.25f, 1.0f ) ) ).bin( refVBox.pad( 5.0, 5.0 ) );
		

		DPBorder valignBaselines = styleSheet.border( textStyleSheet.staticText( "vAlign=REFY" ).alignVRefY() );
		DPBorder valignBaselinesExpand = styleSheet.border( textStyleSheet.staticText( "vAlign=REFY_EXPAND" ).alignVRefYExpand() );
		DPBorder valignTop = styleSheet.border( textStyleSheet.staticText( "vAlign=TOP" ).alignVTop() );
		DPBorder valignCentre = styleSheet.border( textStyleSheet.staticText( "vAlign=CENTRE" ).alignVCentre() );
		DPBorder valignBottom = styleSheet.border( textStyleSheet.staticText( "vAlign=BOTTOM" ).alignVBottom() );
		DPBorder valignExpand = styleSheet.border( textStyleSheet.staticText( "vAlign=EXPAND" ).alignVExpand() );

		DPElement valignContentsBox = styleSheet.withHBoxSpacing( 10.0 ).hbox( new DPElement[] { valignBaselines.alignVRefYExpand(), valignBaselinesExpand.alignVRefYExpand(),
				valignTop.alignVRefYExpand(), valignCentre.alignVRefYExpand(), valignBottom.alignVRefYExpand(), valignExpand.alignVRefYExpand() } );
		
		
		DPElement vAlignBox = styleSheet.withHBoxSpacing( 50.0 ).hbox( new DPElement[] { refBox.alignVRefYExpand(), valignContentsBox.alignVRefYExpand() } );
		
		DPElement valignSection = sectionStyleSheet.vbox( new DPElement[] { valignTitle, vAlignBox.alignHExpand() } );
		
		DPElement mainBox = styleSheet.withVBoxSpacing( 15.0 ).vbox( new DPElement[] { halignSection.alignHExpand(), halignHBoxSection.alignHExpand(), valignSection } );
		return styleSheet.bin( mainBox );
	}
}
