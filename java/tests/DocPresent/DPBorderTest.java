//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.DocPresent;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPBorderTest extends DocPresentTestBase
{
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
	
	
	protected DPWidget createWidget()
	{
		DPText[] c0 = makeTexts( "LEFT" );
		DPText[] c1 = makeTexts( "CENTRE" );
		DPText[] c2 = makeTexts( "RIGHT" );
		DPText[] c3 = makeTexts( "EXPAND" );
		
		VBoxStyleSheet b0s = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		DPVBox b0 = new DPVBox( b0s );
		b0.extend( c0 );
		
		VBoxStyleSheet b1s = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.CENTRE, 0.0, false, 0.0 );
		DPVBox b1 = new DPVBox( b1s );
		b1.extend( c1 );
		
		VBoxStyleSheet b2s = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.RIGHT, 0.0, false, 0.0 );
		DPVBox b2 = new DPVBox( b2s );
		b2.extend( c2 );
		
		VBoxStyleSheet b3s = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.EXPAND, 0.0, true, 0.0 );
		DPVBox b3 = new DPVBox( b3s );
		b3.extend( c3 );
		
		
		Border b = new EmptyBorder( 20.0, 40.0, 60.0, 80.0, new Color( 0.75f, 0.75f, 1.0f ) );
		DPBorder border = new DPBorder( b );
		border.setChild( b0 );
		DPHBox hb = new DPHBox();
		hb.append( border );

		
		VBoxStyleSheet boxS = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.EXPAND, 20.0, false, 0.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( hb );
		box.append( b1 );
		box.append( b2 );
		box.append( b3 );
		
		return box;
	}




	private DPBorderTest()
	{
		JFrame frame = new JFrame( "DPBorder test" );
		initFrame( frame );
		

		
		AbstractAction setBorderChildAction = new AbstractAction( "Set border child" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				DPVBox box = (DPVBox)contentWidget;
				DPHBox hb = (DPHBox)box.get( 0 );
				DPBorder b = (DPBorder)hb.get( 0 );
				b.setChild( new DPText( "Test" ) );
			}
		};

		AbstractAction setHBoxChild0Action = new AbstractAction( "Set hbox child 0" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				DPVBox box = (DPVBox)contentWidget;
				DPHBox hb = (DPHBox)box.get( 0 );
				hb.set( 0, new DPText( "Test" ) );
			}
		};

		AbstractAction setHBoxChildrenAction = new AbstractAction( "Set hbox children" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				DPVBox box = (DPVBox)contentWidget;
				DPHBox hb = (DPHBox)box.get( 0 );
				hb.setChildren( Arrays.asList( new DPWidget[] { new DPText( "Test" ) } ) );
			}
		};

		AbstractAction setVBoxChild0Action = new AbstractAction( "Set vbox child 0" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				DPVBox box = (DPVBox)contentWidget;
				box.set( 0, new DPText( "Test" ) );
			}
		};

		AbstractAction setVBoxChildrenAction = new AbstractAction( "Set vbox children" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				DPVBox box = (DPVBox)contentWidget;
				box.setChildren( Arrays.asList( new DPWidget[] { new DPText( "A" ), new DPText( "B")  } ) );
			}
		};

		
		// Menu
		JMenu actionMenu = new JMenu( "Action" );
		actionMenu.add( setBorderChildAction );
		actionMenu.add( setHBoxChild0Action );
		actionMenu.add( setHBoxChildrenAction );
		actionMenu.add( setVBoxChild0Action );
		actionMenu.add( setVBoxChildrenAction );
		
		menuBar.add( actionMenu );
	}



	public static void main(String[] args)
	{
		new DPBorderTest();
	}
}
