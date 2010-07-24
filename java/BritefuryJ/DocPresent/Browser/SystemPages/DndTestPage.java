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
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.CustomAction;
import BritefuryJ.DocPresent.Combinators.ElementRef;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Pres.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Bin;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Fraction;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Proxy;
import BritefuryJ.DocPresent.Combinators.Primitive.Script;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.Math.Point2;

public class DndTestPage extends SystemPage
{
	private static StyleSheet2 mainStyle = StyleSheet2.instance;
	private static StyleSheet2 mathStyle = StyleSheet2.instance.withAttr( Primitive.fontSize, 16 ).withAttr( Primitive.editable, false );
	private static StyleSheet2 paletteTitleStyle = mainStyle.withAttr( Primitive.fontFace, "Serif" ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSize, 28 );
	private static StyleSheet2 paletteSectionStyle = mainStyle.withAttr( Primitive.fontFace, "Serif" ).withAttr( Primitive.fontSize, 18 );
	private static StyleSheet2 outlineStyle = mainStyle.withAttr( Primitive.border, new SolidBorder( 2.0, 10.0, new Color( 0.6f, 0.7f, 0.8f ), null ) );
	
	private static StyleSheet2 placeHolderStyle = mainStyle.withAttr( Primitive.background, new FillPainter( new Color( 1.0f, 0.9f, 0.75f  ) ) );
	private static StyleSheet2 sourceStyle = mainStyle.withAttr( Primitive.background, new FillPainter( new Color( 0.75f, 0.85f, 1.0f ) ) );

	
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
	
	
	protected Pres makeSource(Pres contents, final Pres factory)
	{
		Pres source = sourceStyle.applyTo( new Bin( contents.pad( 4.0, 2.0 ) ) );
		
		CustomAction action = new CustomAction()
		{
			@Override
			public void apply(DPElement element, PresentationContext ctx)
			{
				ObjectDndHandler.SourceDataFn sourceDataFn = new ObjectDndHandler.SourceDataFn()
				{
					public Object createSourceData(PointerInputElement sourceElement, int aspect)
					{
						return factory;
					}
				};
				
				element.addDragSource( Pres.class, DndHandler.ASPECT_NORMAL, sourceDataFn );
			}
		};
		
		return source.customAction( action );
	}

	protected Pres makeTextSource(final String text)
	{
		Pres factory = new StaticText( text );
		return makeSource( mathStyle.applyTo( new StaticText( text ) ), factory );
	}
	
	
	protected Pres makePlaceHolder()
	{
		Pres placeHolder = placeHolderStyle.applyTo( new Proxy( new Bin( new StaticText( " " ).pad( 8.0, 8.0 ) ) ) );
		final ElementRef ref = new ElementRef( placeHolder );
		
		CustomAction action = new CustomAction()
		{
			@Override
			public void apply(final DPElement element, final PresentationContext ctx)
			{
				ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
				{
					public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
					{
						Pres factory = (Pres)data;
						DPProxy placeHolder = (DPProxy)element;
						placeHolder.setChild( factory.present( ctx ) );
						return true;
					}
				};

				element.addDropDest( Pres.class, dropFn );
			}
		};
		
		return ref.customAction( action );
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
		Pres title = paletteTitleStyle.applyTo( new StaticText( "Palette" ) ).alignHCentre();
		
		Pres textsTitle = paletteSectionStyle.applyTo( new StaticText( "Text:" ) );
		ArrayList<Object> textElements = new ArrayList<Object>();
		textElements.add( textsTitle.padX( 0.0, 20.0 ) );
		String texts = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < texts.length(); i++)
		{
			textElements.add( makeTextSource( texts.substring( i, i+1 ) ) );
		}
		Pres textSection = mainStyle.withAttr( Primitive.hboxSpacing, 3.0 ).applyTo( new HBox( textElements ) );
		
		Pres mathTitle = paletteSectionStyle.applyTo( new StaticText( "Math:" ) );
		ArrayList<Object> mathElements = new ArrayList<Object>();
		mathElements.add( mathTitle.padX( 0.0, 20.0 ) );
		mathElements.add( makeFractionSource() );
		mathElements.add( makeScriptSource() );
		Pres mathSection = mainStyle.withAttr( Primitive.hboxSpacing, 3.0 ).applyTo( new HBox( mathElements ) );
		
		Pres vbox = mainStyle.withAttr( Primitive.vboxSpacing, 10.0 ).applyTo( new VBox( new Pres[] { title, textSection, mathSection } ) );
		
		return outlineStyle.applyTo( new Border( vbox.alignHExpand() ) ).alignHExpand().pad( 20.0, 5.0 ).alignHExpand();
	}
	
	protected Pres makeFormula()
	{
		Pres title = paletteTitleStyle.applyTo( new StaticText( "Formula" ) ).alignHCentre();
		Pres formula = mathStyle.applyTo( makePlaceHolder() );
		Pres formulaPara = mainStyle.applyTo( new Paragraph( new Pres[] { formula } ) );
		
		Pres vbox = mainStyle.withAttr( Primitive.vboxSpacing, 10.0 ).applyTo( new VBox( new Pres[] { title, formulaPara } ) ).alignHExpand();
		
		return vbox.pad( 20.0, 5.0 );
	}
	
	
	
	protected DPElement createContents()
	{
		Pres palette = makePalette();
		Pres formula = makeFormula();
		
		return mainStyle.withAttr( Primitive.vboxSpacing, 20.0 ).applyTo( new VBox( new Pres[] { palette.alignHExpand(), formula.alignHExpand() } ) ).alignHExpand().present();
	}
}
