//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class VBoxTestPage extends SystemPage
{
	protected VBoxTestPage()
	{
		register( "tests.vbox" );
	}
	
	public String getTitle()
	{
		return "V-Box test";
	}
	
	protected String getDescription()
	{
		return "The V-box element arranges its child elements in a vertical box. The v-box typesetting property controls the position of the baseline requested by a v-box."; 
	}

	
	PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	PrimitiveStyleSheet outlineStyleSheet = styleSheet.withBorder( new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null ) );
	PrimitiveStyleSheet textOnGreyStyle = styleSheet.withBackground( new FillPainter( new Color( 0.8f, 0.8f, 0.8f ) ) );
	PrimitiveStyleSheet t12Style = styleSheet.withFontSize( 12 );
	PrimitiveStyleSheet t18Style = styleSheet.withFontSize( 18 ).withForeground( new Color( 0.0f, 0.3f, 0.6f ) );
	PrimitiveStyleSheet t24Style = styleSheet.withFontSize( 24 );

	
	
	private DPHBox makeRefAlignedHBox(int refPointIndex, String header)
	{
		DPVBox v = styleSheet.vbox( new DPElement[] { styleSheet.staticText( "First item" ), styleSheet.staticText( "Second item" ),
				styleSheet.staticText( "Third item" ), styleSheet.staticText( "Fourth item item" ) }, refPointIndex );

		return styleSheet.hbox( new DPElement[] { t18Style.staticText( header ), v, t18Style.staticText( "After" ) } );
	}
	
	

	protected DPElement createContents()
	{
		DPVBox vboxTest = styleSheet.vbox( new DPElement[] { t24Style.staticText( "VBox" ), t12Style.staticText( "First item" ), t12Style.staticText( "Second item" ), t12Style.staticText( "Third item" ) } );
		
		DPVBox hAlignTest = styleSheet.withVBoxSpacing( 10.0 ).vbox( new DPElement[] {
				t24Style.staticText( "Horizontal alignment" ),
				textOnGreyStyle.staticText( "Left" ).alignHLeft(),
				textOnGreyStyle.staticText( "Centre" ).alignHCentre(),
				textOnGreyStyle.staticText( "Right" ).alignHRight(),
				textOnGreyStyle.staticText( "Expand" ).alignHExpand() } );
		
		
		DPVBox refPointAlignTest = styleSheet.withVBoxSpacing( 20.0 ).vbox(
				new DPElement[] { t24Style.staticText( "VBox reference point alignment" ),
				makeRefAlignedHBox( 0, "ALIGN_WITH_0" ),
				makeRefAlignedHBox( 1, "ALIGN_WITH_1" ),
				makeRefAlignedHBox( 2, "ALIGN_WITH_2" ),
				makeRefAlignedHBox( 3, "ALIGN_WITH_3" ) } );
		
		
		return styleSheet.withVBoxSpacing( 20.0 ).vbox( new DPElement[] {
				outlineStyleSheet.border( vboxTest ),
				outlineStyleSheet.border( hAlignTest ),
				outlineStyleSheet.border( refPointAlignTest ) } );
	}
}
