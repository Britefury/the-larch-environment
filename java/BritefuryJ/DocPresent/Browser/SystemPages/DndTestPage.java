//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Input.DndDrop;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;

public class DndTestPage extends SystemPage
{
	private static StaticTextStyleSheet textStyle = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.BLACK );
	private static Border sourceBorder = new EmptyBorder( 10.0, 10.0, 10.0, 10.0, new Color( 0.75f, 0.85f, 1.0f ) );
	private static Border destBorder = new EmptyBorder( 10.0, 10.0, 10.0, 10.0, new Color( 1.0f, 0.9f, 0.75f ) );

	protected DndTestPage()
	{
		register( "tests.dnd" );
	}
	
	
	protected String getTitle()
	{
		return "Drag and Drop test";
	}
	
	protected String getDescription()
	{
		return "Text can be dragged from the sources to the destinations.";
	}
	
	
	
	protected DPWidget makeSourceElement(String title, final String dragData)
	{
		DPStaticText sourceText = new DPStaticText( textStyle, title );
		DPBorder sourceBorderElement = new DPBorder( sourceBorder );
		sourceBorderElement.setChild( sourceText );
		
		DndHandler sourceHandler = new DndHandler()
		{
			public int getSourceRequestedAction(PointerInputElement sourceElement, PointerInterface pointer, int button)
			{
				return COPY;
			}

			public Transferable createTransferable(PointerInputElement sourceElement)
			{
				return new StringSelection( dragData );
			}

			public void exportDone(PointerInputElement sourceElement, Transferable data, int action)
			{
			}
		};
		
		sourceBorderElement.enableDnd( sourceHandler );

		return sourceBorderElement;
	}
	
	protected DPWidget makeDestElement(String title)
	{
		DPStaticText destText = new DPStaticText( textStyle, title );
		DPBorder destBorderElement = new DPBorder( destBorder );
		destBorderElement.setChild( destText );
		
		DndHandler destHandler = new DndHandler()
		{
			public boolean canDrop(PointerInputElement destElement, DndDrop drop)
			{
				return drop.getTransferable() != null;
			}

			public boolean acceptDrop(PointerInputElement destElement, DndDrop drop)
			{
				Transferable x = drop.getTransferable();
				if ( x != null )
				{
					String text = null;
					try
					{
						text = (String)x.getTransferData( DataFlavor.stringFlavor );
					}
					catch (UnsupportedFlavorException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					((DPStaticText)((DPBorder)destElement).getChild()).setText( text );
					return true;
				}
				else
				{
					return false;
				}
			}
		};
		
		destBorderElement.enableDnd( destHandler );

		return destBorderElement;
	}
	

	
	protected DPWidget createContents()
	{
		StaticTextStyleSheet rowTitleStyle = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 14 ), Color.BLACK );
		
		
		DPStaticText sourceTitle = new DPStaticText( rowTitleStyle, "Source:" );
		DPWidget source0 = makeSourceElement( "abc", "abc" );
		DPWidget source1 = makeSourceElement( "xyz", "xyz" );

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


		
		DPHBox hbox = new DPHBox();
		hbox.append( table.padY( 10.0 ) );

		return hbox;
	}
}
