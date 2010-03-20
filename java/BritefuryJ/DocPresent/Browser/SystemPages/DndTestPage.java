//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.Math.Point2;

public class DndTestPage extends SystemPage
{
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet textStyle = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 16 ) );
	private static PrimitiveStyleSheet mathStyle = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet paletteTitleStyle = styleSheet.withFont( new Font( "Serif", Font.BOLD, 28 ) );
	private static PrimitiveStyleSheet paletteSectionStyle = styleSheet.withFont( new Font( "Serif", Font.PLAIN, 18 ) );
	private static PrimitiveStyleSheet outlineStyle = styleSheet.withBorder( new SolidBorder( 2.0, 10.0, new Color( 0.6f, 0.7f, 0.8f ), null ) );
	
	private static PrimitiveStyleSheet placeHolderStyle = styleSheet.withBackground( new FillPainter( new Color( 1.0f, 0.9f, 0.75f  ) ) );
	private static PrimitiveStyleSheet sourceStyle = styleSheet.withBackground( new FillPainter( new Color( 0.75f, 0.85f, 1.0f ) ) );
	
	
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
		return "A very simple drag and drop based formula editor. Drag elements from the palette into the yellow formula boxes.";
	}
	
	
	protected DPElement makeSource(DPElement contents, final ElementFactory factory)
	{
		DPElement source = sourceStyle.box( contents.pad( 4.0, 2.0 ) );
		
		ObjectDndHandler.SourceDataFn sourceDataFn = new ObjectDndHandler.SourceDataFn()
		{
			public Object createSourceData(PointerInputElement sourceElement)
			{
				return factory;
			}
		};
		
		source.addDragSource( ElementFactory.class, sourceDataFn );

		return source;
	}

	protected DPElement makeTextSource(final String text)
	{
		final ElementFactory factory = new ElementFactory()
		{
			public DPElement createElement(StyleSheet styleSheet)
			{
				return textStyle.staticText( text );
			}
		};

		return makeSource( textStyle.text( text ), factory );
	}
	
	
	protected DPElement makePlaceHolder()
	{
		final DPProxy placeHolder = styleSheet.proxy( placeHolderStyle.box( styleSheet.staticText( " " ).pad( 8.0, 8.0 ) ) );
		
		ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
		{
			public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data)
			{
				ElementFactory factory = (ElementFactory)data;
				placeHolder.setChild( factory.createElement( styleSheet ) );
				return true;
			}
		};
		
		placeHolder.addDropDest( ElementFactory.class, dropFn );
		
		return placeHolder;
	}
	
	
	protected DPElement makeFractionSource()
	{
		final ElementFactory factory = new ElementFactory()
		{
			public DPElement createElement(StyleSheet styleSheet)
			{
				return mathStyle.fraction( makePlaceHolder(), makePlaceHolder(), "/" );
			}
		};

		return makeSource( mathStyle.fraction( textStyle.staticText( "a" ), textStyle.staticText( "b" ), "/" ), factory );
	}
	
	protected DPElement makeScriptSource()
	{
		final ElementFactory factory = new ElementFactory()
		{
			public DPElement createElement(StyleSheet styleSheet)
			{
				return mathStyle.script( makePlaceHolder(), makePlaceHolder(), makePlaceHolder(), makePlaceHolder(), makePlaceHolder() );
			}
		};

		return makeSource( mathStyle.script( textStyle.staticText( "a" ), textStyle.staticText( "b" ), textStyle.staticText( "c" ), textStyle.staticText( "d" ), textStyle.staticText( "e" ) ), factory );
	}
	
	
	protected DPElement makePalette()
	{
		DPElement title = paletteTitleStyle.staticText( "Palette" ).alignHCentre();
		
		DPElement textsTitle = paletteSectionStyle.staticText( "Text:" );
		ArrayList<DPElement> textElements = new ArrayList<DPElement>();
		textElements.add( textsTitle.padX( 0.0, 20.0 ) );
		String texts = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < texts.length(); i++)
		{
			textElements.add( makeTextSource( texts.substring( i, i+1 ) ) );
		}
		DPElement textSection = styleSheet.withHBoxSpacing( 3.0 ).hbox( textElements );
		
		DPElement mathTitle = paletteSectionStyle.staticText( "Math:" );
		ArrayList<DPElement> mathElements = new ArrayList<DPElement>();
		mathElements.add( mathTitle.padX( 0.0, 20.0 ) );
		mathElements.add( makeFractionSource() );
		mathElements.add( makeScriptSource() );
		DPElement mathSection = styleSheet.withHBoxSpacing( 3.0 ).hbox( mathElements );
		
		DPElement vbox = styleSheet.withVBoxSpacing( 10.0 ).vbox( Arrays.asList( new DPElement[] { title, textSection, mathSection } ) );
		
		return outlineStyle.border( vbox.alignHExpand() ).alignHExpand().pad( 20.0, 5.0 ).alignHExpand();
	}
	
	protected DPElement makeFormula()
	{
		DPElement title = paletteTitleStyle.staticText( "Formula" ).alignHCentre();
		DPElement formula = makePlaceHolder();
		DPElement formulaPara = styleSheet.paragraph( Arrays.asList( new DPElement[] { formula } ) );
		
		DPElement vbox = styleSheet.withVBoxSpacing( 10.0 ).vbox( Arrays.asList( new DPElement[] { title, formulaPara } ) ).alignHExpand();
		
		return vbox.pad( 20.0, 5.0 );
	}
	
	
	
	protected DPElement createContents()
	{
		DPElement palette = makePalette();
		DPElement formula = makeFormula();
		
		return styleSheet.withVBoxSpacing( 20.0 ).vbox( Arrays.asList( new DPElement[] { palette.alignHExpand(), formula.alignHExpand() } ) ).alignHExpand();
	}
}
