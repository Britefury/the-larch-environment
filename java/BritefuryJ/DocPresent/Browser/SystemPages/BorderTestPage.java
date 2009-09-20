//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class BorderTestPage extends SystemPage
{
	protected BorderTestPage()
	{
		register( "tests.border" );
	}
	
	
	public String getTitle()
	{
		return "Border test";
	}
	
	protected String getDescription()
	{
		return "The border element is used to provide the background colours, and the black boxes. Horizontal and vertical element alignments are demonstrated.";
	}

	protected static DPText[] makeTexts(String header)
	{
		TextStyleSheet t12 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText h = new DPText( t18, header );
		DPText t0 = new DPText( t12, "Hello" );
		DPText t1 = new DPText( t12, "World" );
		DPText t2 = new DPText( t12, "Foo" );
		
		DPText[] texts = { h, t0, t1, t2 };
		return texts;
	}
	
	protected static DPWidget makeTextOnGrey(String text)
	{
		DPStaticText t = new DPStaticText( text );
		
		EmptyBorder b = new EmptyBorder( new Color( 0.8f, 0.8f, 0.8f ) );
		DPBorder border = new DPBorder( b );
		border.setChild( t );
		return border;
	}
	
	
	protected DPWidget createContents()
	{
		VBoxStyleSheet mainBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, 10.0 );
		DPVBox mainBox = new DPVBox( mainBoxStyle );
		
		SolidBorder singlePixelBorder = new SolidBorder( 1.0, 2.0, Color.black, null );
		
		DPBorder halignLeft = new DPBorder( singlePixelBorder );
		DPBorder halignCentre = new DPBorder( singlePixelBorder );
		DPBorder halignRight = new DPBorder( singlePixelBorder );
		DPBorder halignExpand = new DPBorder( singlePixelBorder );
		
		halignLeft.setChild( makeTextOnGrey( "hAlign=left" ).alignHLeft() );
		halignCentre.setChild( makeTextOnGrey( "hAlign=centre" ).alignHCentre() );
		halignRight.setChild( makeTextOnGrey( "hAlign=right" ).alignHRight() );
		halignExpand.setChild( makeTextOnGrey( "hAlign=expand" ).alignHExpand() );
		
		
		
		
		EmptyBorder spacerBorder = new EmptyBorder( 5.0, 5.0, 200.0, 200.0, new Color( 0.7f, 0.8f, 0.9f ) );
		DPBorder spacer = new DPBorder( spacerBorder );
		spacer.setChild( new DPStaticText( "SPACER" ) );
		
		
		DPBorder valignBaselines = new DPBorder( singlePixelBorder );
		DPBorder valignBaselinesExpand = new DPBorder( singlePixelBorder );
		DPBorder valignTop = new DPBorder( singlePixelBorder );
		DPBorder valignCentre = new DPBorder( singlePixelBorder );
		DPBorder valignBottom = new DPBorder( singlePixelBorder );
		DPBorder valignExpand = new DPBorder( singlePixelBorder );
		
		
		valignBaselines.setChild( makeTextOnGrey( "vAlign=baselines" ).alignVBaselines() );
		valignBaselinesExpand.setChild( makeTextOnGrey( "vAlign=b-expand" ).alignVBaselinesExpand() );
		valignTop.setChild( makeTextOnGrey( "vAlign=top" ).alignVTop() );
		valignCentre.setChild( makeTextOnGrey( "vAlign=centre" ).alignVCentre() );
		valignBottom.setChild( makeTextOnGrey( "vAlign=bottom" ).alignVBottom() );
		valignExpand.setChild( makeTextOnGrey( "vAlign=expand" ).alignVExpand() );

		
		HBoxStyleSheet vAlignBoxStyle = new HBoxStyleSheet( 10.0 );
		DPHBox vAlignBox = new DPHBox( vAlignBoxStyle );
		vAlignBox.append( valignBaselines.alignVExpand() );
		vAlignBox.append( valignBaselinesExpand.alignVExpand() );
		vAlignBox.append( valignTop.alignVExpand() );
		vAlignBox.append( valignCentre.alignVExpand() );
		vAlignBox.append( valignBottom.alignVExpand() );
		vAlignBox.append( valignExpand.alignVExpand() );
		
		
		HBoxStyleSheet bottomBoxStyle = new HBoxStyleSheet( 50.0 );
		DPHBox bottomBox = new DPHBox( bottomBoxStyle );
		bottomBox.append( spacer );
		bottomBox.append( vAlignBox.alignVExpand() );
		
		
		mainBox.append( halignLeft.alignHExpand() );
		mainBox.append( halignCentre.alignHExpand() );
		mainBox.append( halignRight.alignHExpand() );
		mainBox.append( halignExpand.alignHExpand() );
		mainBox.append( bottomBox );
		
		
		return mainBox;
	}
}
