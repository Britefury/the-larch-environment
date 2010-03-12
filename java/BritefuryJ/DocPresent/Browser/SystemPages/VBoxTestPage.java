//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPElement;
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
	PrimitiveStyleSheet t12Style = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 12 ) );
	PrimitiveStyleSheet t18Style = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 18 ) ).withForeground( new Color( 0.0f, 0.3f, 0.6f ) );
	PrimitiveStyleSheet t24Style = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 24 ) );

	
	
	private DPHBox makeRefAlignedHBox(int refPointIndex, String header)
	{
		DPVBox v = styleSheet.vbox( Arrays.asList( new DPElement[] { styleSheet.text( "First item" ), styleSheet.text( "Second item" ),
				styleSheet.text( "Third item" ), styleSheet.text( "Fourth item item" ) } ), refPointIndex );

		return styleSheet.hbox( Arrays.asList( new DPElement[] { t18Style.text( header ), v, t18Style.text( "After" ) } ) );
	}
	
	

	protected DPElement createContents()
	{
		DPVBox vboxTest = styleSheet.vbox( Arrays.asList( new DPElement[] { t24Style.text( "VBox" ), t12Style.text( "First item" ), t12Style.text( "Second item" ), t12Style.text( "Third item" ) } ) );
		
		DPVBox hAlignTest = styleSheet.withVBoxSpacing( 10.0 ).vbox( Arrays.asList( new DPElement[] { t24Style.text( "Horizontal alignment" ),
				textOnGreyStyle.text( "Left" ).alignHLeft(),textOnGreyStyle.text( "Centre" ).alignHCentre(), textOnGreyStyle.text( "Right" ).alignHRight(), textOnGreyStyle.text( "Expand" ).alignHExpand() } ) );
		
		
		DPVBox refPointAlignTest = styleSheet.withVBoxSpacing( 20.0 ).vbox( Arrays.asList( new DPElement[] { t24Style.text( "VBox reference point alignment" ),
				makeRefAlignedHBox( 0, "ALIGN_WITH_0" ), makeRefAlignedHBox( 1, "ALIGN_WITH_1" ), makeRefAlignedHBox( 2, "ALIGN_WITH_2" ), makeRefAlignedHBox( 3, "ALIGN_WITH_3" ) } ) );
		
		
		return styleSheet.withVBoxSpacing( 20.0 ).vbox( Arrays.asList( new DPElement[] { outlineStyleSheet.border( vboxTest ),
				outlineStyleSheet.border( hAlignTest ), outlineStyleSheet.border( refPointAlignTest ) } ) );
	}
}
