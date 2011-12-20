//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.Border.AbstractBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.ElementRef;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Fraction;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Script;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

public class DndTestPage extends SystemPage
{
	private static StyleSheet mainStyle = StyleSheet.instance;
	private static StyleSheet mathStyle = StyleSheet.style( Primitive.fontSize.as( 16 ), Primitive.editable.as( false ) );
	private static StyleSheet paletteTitleStyle = mainStyle.withValues( Primitive.fontFace.as( "Serif" ), Primitive.fontBold.as( true ), Primitive.fontSize.as( 28 ) );
	private static StyleSheet paletteSectionStyle = mainStyle.withValues( Primitive.fontFace.as( "Serif" ), Primitive.fontSize.as( 18 ) );
	private static AbstractBorder outlineBorder = new SolidBorder( 2.0, 10.0, new Color( 0.6f, 0.7f, 0.8f ), null );

	private static StyleSheet placeHolderStyle = mainStyle.withValues( Primitive.background.as( new FillPainter( new Color( 1.0f, 0.9f, 0.75f ) ) ) );
	private static StyleSheet sourceStyle = mainStyle.withValues( Primitive.background.as( new FillPainter( new Color( 0.75f, 0.85f, 1.0f ) ) ) );

	
	protected DndTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Drag and Drop test";
	}
	
	protected String getDescription()
	{
		return "A very simple drag and drop based formula editor. Drag elements from the palette into the yellow formula boxes.";
	}
	
	
	protected Pres makeSource(final Pres contents, final Pres factory)
	{
		Pres source = sourceStyle.applyTo( new Bin( contents.pad( 4.0, 2.0 ) ) );
		
		ObjectDndHandler.SourceDataFn sourceDataFn = new ObjectDndHandler.SourceDataFn()
		{
			public Object createSourceData(PointerInputElement sourceElement, int aspect)
			{
				return factory;
			}
		};
		
		return source.withDragSource( Pres.class, DndHandler.ASPECT_NORMAL, sourceDataFn );
	}

	protected Pres makeTextSource(final String text)
	{
		Pres factory = new StaticText( text );
		return makeSource( mathStyle.applyTo( new StaticText( text ) ), factory );
	}
	
	
	protected Pres makePlaceHolder()
	{
		final ElementRef placeHolder = placeHolderStyle.applyTo( new Proxy( new Bin( new StaticText( " " ).pad( 8.0, 8.0 ) ) ) ).elementRef();
		
		ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
		{
			public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
			{
				Pres factory = (Pres)data;
				for (DPElement element: placeHolder.getElements())
				{
					DPProxy proxy = (DPProxy)element;
					proxy.setChild( factory.present( placeHolder.getContextForElement( element ), placeHolder.getStyleForElement( element ) ) );
				}
				return true;
			}
		};

		return placeHolder.withDropDest( Pres.class, dropFn );
	}
	
	
	protected Pres makeFractionSource()
	{
		Pres factory = new Fraction( makePlaceHolder(), makePlaceHolder(), "/" );

		return makeSource( mathStyle.applyTo( new Fraction( new StaticText( "a" ), new StaticText( "b" ), "/" ) ), factory );
	}
	
	protected Pres makeScriptSource()
	{
		Pres factory = new Script( makePlaceHolder(), makePlaceHolder(), makePlaceHolder(), makePlaceHolder(), makePlaceHolder() );

		return makeSource( mathStyle.applyTo( new Script( new StaticText( "a" ), new StaticText( "b" ), new StaticText( "c" ), new StaticText( "d" ), new StaticText( "e" ) ) ), factory );
	}
	
	
	protected Pres makePalette()
	{
		Pres title = paletteTitleStyle.applyTo( new Label( "Palette" ) ).alignHCentre();
		
		Pres textsTitle = paletteSectionStyle.applyTo( new Label( "Text:" ) );
		ArrayList<Object> textElements = new ArrayList<Object>();
		textElements.add( textsTitle.padX( 0.0, 20.0 ) );
		String texts = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < texts.length(); i++)
		{
			textElements.add( makeTextSource( texts.substring( i, i+1 ) ) );
		}
		Pres textSection = mainStyle.withValues( Primitive.rowSpacing.as( 3.0 ) ).applyTo( new Row( textElements ) );
		
		Pres mathTitle = paletteSectionStyle.applyTo( new Label( "Math:" ) );
		ArrayList<Object> mathElements = new ArrayList<Object>();
		mathElements.add( mathTitle.padX( 0.0, 20.0 ) );
		mathElements.add( makeFractionSource() );
		mathElements.add( makeScriptSource() );
		Pres mathSection = mainStyle.withValues( Primitive.rowSpacing.as( 3.0 ) ).applyTo( new Row( mathElements ) );
		
		Pres column = mainStyle.withValues( Primitive.columnSpacing.as( 10.0 ) ).applyTo( new Column( new Pres[] { title, textSection.alignHPack(), mathSection.alignHPack() } ) );
		
		return outlineBorder.surround( column ).pad( 20.0, 5.0 ).alignHExpand().alignVRefY();
	}
	
	protected Pres makeFormula()
	{
		Pres title = paletteTitleStyle.applyTo( new Label( "Formula" ) ).alignHCentre();
		Pres formula = mathStyle.applyTo( makePlaceHolder() );
		Pres formulaPara = new Bin( mainStyle.applyTo( new Paragraph( new Pres[] { formula } ) ) ).alignHPack();
		
		Pres column = mainStyle.withValues( Primitive.columnSpacing.as( 10.0 ) ).applyTo( new Column( new Pres[] { title, formulaPara } ) ).alignHExpand().alignVRefY();
		
		return column.pad( 20.0, 5.0 );
	}
	
	
	
	protected Pres createContents()
	{
		Pres palette = makePalette();
		Pres formula = makeFormula();
		
		return new Body( new Pres[] { palette, new Spacer( 0.0, 10.0 ), formula.alignHExpand() } );
	}
}
