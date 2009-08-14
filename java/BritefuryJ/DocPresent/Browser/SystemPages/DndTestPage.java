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
import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.DndDrag;
import BritefuryJ.DocPresent.DndListener;
import BritefuryJ.DocPresent.DndOperation;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.Math.Point2;

public class DndTestPage extends SystemPage
{
	private static StaticTextStyleSheet textStyle = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
	private static Border sourceBorder = new EmptyBorder( 10.0, 10.0, 10.0, 10.0, new Color( 0.75f, 0.85f, 1.0f ) );
	private static Border destBorder = new EmptyBorder( 10.0, 10.0, 10.0, 10.0, new Color( 1.0f, 0.9f, 0.75f ) );
	private static DndOperation dndOp = new DndOperation();

	protected DndTestPage()
	{
		register( "tests.dnd" );
	}
	
	
	protected String getTitle()
	{
		return "Drag and Drop test";
	}
	
	
	
	protected DPWidget makeSourceElement(String title, final Object beginData, final Object dragData)
	{
		DPStaticText sourceText = new DPStaticText( textStyle, title );
		DPBorder sourceBorderElement = new DPBorder( sourceBorder );
		sourceBorderElement.setChild( sourceText );
		
		DndListener sourceListener = new DndListener()
		{
			public Object onDndBegin(DndDrag drag)
			{
				return beginData;
			}

			public Object dndDragTo(DndDrag drag, DPWidget dest)
			{
				return dragData;
			}

			
			public boolean dndCanDropFrom(DndDrag drag, DPWidget dest, Point2 destLocalPos)
			{
				return false;
			}

			public void onDndMotion(DndDrag drag, DPWidget dest, Point2 destLocalPos)
			{
			}

			public void dndDropFrom(DndDrag drag, DPWidget dest, Point2 destLocalPos)
			{
			}
		};
		
		sourceBorderElement.enableDnd( sourceListener );
		sourceBorderElement.addDndSourceOp( dndOp );

		return sourceBorderElement;
	}
	
	protected DPWidget makeDestElement(String title)
	{
		DPStaticText destText = new DPStaticText( textStyle, title );
		DPBorder destBorderElement = new DPBorder( destBorder );
		destBorderElement.setChild( destText );
		
		DndListener destListener = new DndListener()
		{
			public Object onDndBegin(DndDrag drag)
			{
				return null;
			}

			public Object dndDragTo(DndDrag drag, DPWidget dest)
			{
				return null;
			}

			
			public boolean dndCanDropFrom(DndDrag drag, DPWidget dest, Point2 destLocalPos)
			{
				return true;
			}

			public void onDndMotion(DndDrag drag, DPWidget dest, Point2 destLocalPos)
			{
			}

			public void dndDropFrom(DndDrag drag, DPWidget dest, Point2 destLocalPos)
			{
				((DPStaticText)((DPBorder)dest).getChild()).setText( (String)drag.getDragData() );
			}
		};
		
		destBorderElement.enableDnd( destListener );
		destBorderElement.addDndDestOp( dndOp );

		return destBorderElement;
	}
	

	
	protected DPWidget createContents()
	{
		StaticTextStyleSheet instructionsStyle = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.BLACK );
		DPStaticText instructions = new DPStaticText( instructionsStyle, "Drag data from sources to destinations." );
		
		
		StaticTextStyleSheet rowTitleStyle = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 14 ), Color.BLACK );
		
		
		DPStaticText sourceTitle = new DPStaticText( rowTitleStyle, "Source:" );
		DPWidget source0 = makeSourceElement( "abc", "begin", "abc" );
		DPWidget source1 = makeSourceElement( "xyz", "begin", "xyz" );

		DPStaticText destTitle = new DPStaticText( rowTitleStyle, "Destination:" );
		DPWidget dest0 = makeDestElement( "abc" );
		DPWidget dest1 = makeDestElement( "xyz" );
		
		
		TableStyleSheet tableStyle = new TableStyleSheet( VAlignment.BASELINES, HAlignment.LEFT, 25.0, false, 0.0, 25.0, false, 0.0 );
		
		
		DPTable table = new DPTable( tableStyle );
		table.put( 0, 0, sourceTitle );
		table.put( 1, 0, source0 );
		table.put( 2, 0, source1 );
		table.put( 0, 1, destTitle );
		table.put( 1, 1, dest0 );
		table.put( 2, 1, dest1 );


		
		VBoxStyleSheet vboxS = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 20.0, false, 0.0 );
		DPVBox vbox = new DPVBox( vboxS );
		vbox.append( instructions );
		vbox.append( table );
		
		HBoxStyleSheet hboxS = new HBoxStyleSheet( VAlignment.TOP, 0.0, false, 10.0 );
		DPHBox hbox = new DPHBox( hboxS );
		hbox.append( vbox );

		return hbox;
	}
}
