//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class DndTestPage extends SystemPage
{
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet textStyle = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 16 ) );
	private static PrimitiveStyleSheet sourceStyle = styleSheet.withBackground( new FillPainter( new Color( 0.75f, 0.85f, 1.0f ) ) );
	private static PrimitiveStyleSheet destStyle = styleSheet.withBackground( new FillPainter( new Color( 1.0f, 0.9f, 0.75f  ) ) );
	private static PrimitiveStyleSheet tableStyle = styleSheet.withTableColumnSpacing( 25.0 ).withTableRowSpacing( 25.0 );
	
	
	protected DndTestPage()
	{
		register( "tests.dnd" );
	}
	
	
	public String getTitle()
	{
		return "Drag and Drop test";
	}
	
	protected String getDescription()
	{
		return "Text can be dragged from the sources to the destinations.";
	}
	
	
	
	protected DPElement makeSourceElement(String title, final String dragData)
	{
		final DPText textElement = textStyle.text( title );
		DPElement source = sourceStyle.box( textElement.pad( 10.0, 10.0 ) );
		
		ObjectDndHandler.SourceDataFn sourceDataFn = new ObjectDndHandler.SourceDataFn()
		{
			public Object createSourceData(PointerInputElement sourceElement)
			{
				return dragData;
			}
		};
		
		ObjectDndHandler sourceDndHandler = new ObjectDndHandler( new ObjectDndHandler.DndSource[] { new ObjectDndHandler.DndSource( String.class, sourceDataFn ) },
				new ObjectDndHandler.DndDest[] {} );

		source.enableDnd( sourceDndHandler );

		return source;
	}
	
	protected DPElement makeDestElement(String title)
	{
		final DPText textElement = textStyle.text( title );
		DPElement dest = destStyle.box( textElement.pad( 10.0, 10.0 ) );

		ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
		{
			public boolean acceptDrop(PointerInputElement destElement, Object data)
			{
				String text = (String)data;
				textElement.setText( text );
				return true;
			}
		};
		
		ObjectDndHandler destDndHandler = new ObjectDndHandler( new ObjectDndHandler.DndSource[] {}, new ObjectDndHandler.DndDest[] { new ObjectDndHandler.DndDest( String.class, dropFn ) } );


		dest.enableDnd( destDndHandler );

		return dest;
	}
	

	
	protected DPElement createContents()
	{
		DPElement source0 = makeSourceElement( "abc", "abc" );
		DPElement source1 = makeSourceElement( "xyz", "xyz" );

		DPElement dest0 = makeDestElement( "abc" );
		DPElement dest1 = makeDestElement( "xyz" );
		
		return tableStyle.table( new DPElement[][] { { styleSheet.text( "Source:" ), source0, source1 },
				{ styleSheet.text( "Destination:" ), dest0, dest1 } } ).padY( 10.0 );
	}
}
