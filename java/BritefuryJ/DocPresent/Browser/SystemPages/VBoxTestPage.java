//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

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

	
	StyleSheet2 styleSheet = StyleSheet2.instance;
	StyleSheet2 outlineStyleSheet = styleSheet.withAttr( Primitive.border, new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null ) );
	StyleSheet2 textOnGreyStyle = styleSheet.withAttr( Primitive.background, new FillPainter( new Color( 0.8f, 0.8f, 0.8f ) ) );
	StyleSheet2 t12Style = styleSheet.withAttr( Primitive.fontSize, 12 );
	StyleSheet2 t18Style = styleSheet.withAttr( Primitive.fontSize, 18 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.3f, 0.6f ) );
	StyleSheet2 t24Style = styleSheet.withAttr( Primitive.fontSize, 24 );

	
	
	private Pres makeRefAlignedHBox(int refPointIndex, String header)
	{
		Pres v = new VBox( new Pres[] { styleSheet.applyTo( new StaticText( "First item" ) ), styleSheet.applyTo( new StaticText( "Second item" ) ),
				styleSheet.applyTo( new StaticText( "Third item" ) ), styleSheet.applyTo( new StaticText( "Fourth item item" ) ) }, refPointIndex );
		v = styleSheet.withAttr( Primitive.vboxSpacing, 0.0 ).applyTo( v );
		return new HBox( new Pres[] { t18Style.applyTo( new StaticText( header ) ), v, t18Style.applyTo( new StaticText( "After" ) ) } );
	}
	
	

	protected DPElement createContents()
	{
		Pres vboxTest = new VBox( new Pres[] { t24Style.applyTo( new StaticText( "VBox" ) ),
				t12Style.applyTo( new StaticText( "First item" ) ),
				t12Style.applyTo( new StaticText( "Second item" ) ),
				t12Style.applyTo( new StaticText( "Third item" ) ) } );
		
		Pres hAlignTest = styleSheet.withAttr( Primitive.vboxSpacing, 10.0 ).applyTo( new VBox( new Pres[] {
				t24Style.applyTo( new StaticText( "Horizontal alignment" ) ),
				textOnGreyStyle.applyTo( new StaticText( "Left" ) ).alignHLeft(),
				textOnGreyStyle.applyTo( new StaticText( "Centre" ) ).alignHCentre(),
				textOnGreyStyle.applyTo( new StaticText( "Right" ) ).alignHRight(),
				textOnGreyStyle.applyTo( new StaticText( "Expand" ) ).alignHExpand() } ) );
		
		
		Pres refPointAlignTest = styleSheet.withAttr( Primitive.vboxSpacing, 20.0 ).applyTo( new VBox( new Pres[] {
				t24Style.applyTo( new StaticText( "VBox reference point alignment" ) ),
				makeRefAlignedHBox( 0, "ALIGN_WITH_0" ),
				makeRefAlignedHBox( 1, "ALIGN_WITH_1" ),
				makeRefAlignedHBox( 2, "ALIGN_WITH_2" ),
				makeRefAlignedHBox( 3, "ALIGN_WITH_3" ) } ) );
		
		
		return new Body( new Pres[] {
				outlineStyleSheet.applyTo( new Border( vboxTest ) ),
				outlineStyleSheet.applyTo( new Border( hAlignTest ) ),
				outlineStyleSheet.applyTo( new Border( refPointAlignTest ) ) } ).present();
	}
}
