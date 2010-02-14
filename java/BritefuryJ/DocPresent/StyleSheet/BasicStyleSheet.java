//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.util.List;

import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPButton;
import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPGridRow;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPLine;
import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPMathRoot;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPParagraphDedentMarker;
import BritefuryJ.DocPresent.DPParagraphIndentMarker;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWhitespace;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleParams.ButtonStyleParams;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.StyleParams.FractionStyleParams;
import BritefuryJ.DocPresent.StyleParams.GridRowStyleParams;
import BritefuryJ.DocPresent.StyleParams.HBoxStyleParams;
import BritefuryJ.DocPresent.StyleParams.LineStyleParams;
import BritefuryJ.DocPresent.StyleParams.LinkStyleParams;
import BritefuryJ.DocPresent.StyleParams.MathRootStyleParams;
import BritefuryJ.DocPresent.StyleParams.ParagraphStyleParams;
import BritefuryJ.DocPresent.StyleParams.ScriptStyleParams;
import BritefuryJ.DocPresent.StyleParams.StaticTextStyleParams;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
import BritefuryJ.DocPresent.StyleParams.VBoxStyleParams;

public class BasicStyleSheet extends StyleSheet
{
	private static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	private static final Paint default_buttonBorderPaint = new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 0.2f, 0.3f, 0.5f ), new Color( 0.3f, 0.45f, 0.75f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE );
	private static final Paint default_buttonBackgroundPaint = new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 0.9f, 0.92f, 1.0f ), new Color( 0.75f, 0.825f, 0.9f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE );
	private static final Paint default_buttonHighlightBackgroundPaint = new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 1.0f, 1.0f, 1.0f ), new Color( 0.85f, 0.85f, 0.85f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE );
	
	private static final Font defaultLinkFont = new Font( "Sans serif", Font.PLAIN, 14 );
	

	
	public static final BasicStyleSheet defaultStyleSheet = new BasicStyleSheet();

	
	
	protected BasicStyleSheet()
	{
		super();
		
		initAttr( "font", defaultFont );
		initAttr( "foregroundPaint", Color.black );

		initAttr( "buttonBorderPaint", default_buttonBorderPaint );
		initAttr( "buttonBackgroundPaint", default_buttonBackgroundPaint );
		initAttr( "buttonHighlightBackgroundPaint", default_buttonHighlightBackgroundPaint );

		initAttr( "fractionVSpacing", 2.0 );
		initAttr( "fractionHPadding", 3.0 );
		initAttr( "fractionRefYOffset", 5.0 );
		
		initAttr( "hboxSpacing", 0.0 );
		
		initAttr( "lineDirection", LineStyleParams.Direction.HORIZONTAL );
		initAttr( "lineThickness", 1.0 );
		initAttr( "lineInset", 0.0 );
		initAttr( "linePadding", 0.0 );
		
		initAttr( "linkFont", defaultLinkFont );
		initAttr( "linkPaint", Color.blue );
		initAttr( "linkSmallCaps", false );
		
		initAttr( "mathRootThickness", 1.5 );
		
		initAttr( "paragraphSpacing", 0.0 );
		initAttr( "paragraphLineSpacing", 0.0 );
		initAttr( "paragraphIndentation", 0.0 );
		
		initAttr( "scriptColumnSpacing", 1.0 );
		initAttr( "scriptRowSpacing", 1.0 );
		
		initAttr( "tableColumnSpacing", 0.0 );
		initAttr( "tableColumnExpand", false );
		initAttr( "tableRowSpacing", 0.0 );
		initAttr( "tableRowExpand", false );
		
		initAttr( "textSquiggleUnderlinePaint", null );
		initAttr( "textSmallCaps", false );
		
		initAttr( "vboxSpacing", 0.0 );
	}
	
	protected BasicStyleSheet(StyleSheet prototype)
	{
		super( prototype );
	}
	
	
	public Object clone()
	{
		return new BasicStyleSheet( this );
	}
	

	
	
	//
	// GENERAL
	//
	
	public BasicStyleSheet withFont(Font font)
	{
		return (BasicStyleSheet)withAttr( "font", font );
	}

	public BasicStyleSheet withForegroundPaint(Paint paint)
	{
		return (BasicStyleSheet)withAttr( "foregroundPaint", paint );
	}


	
	//
	// BUTTON
	//
	
	public BasicStyleSheet withButtonBorderPaint(Paint paint)
	{
		return (BasicStyleSheet)withAttr( "buttonBorderPaint", paint );
	}

	public BasicStyleSheet withButtonBackgroundPaint(Paint paint)
	{
		return (BasicStyleSheet)withAttr( "buttonBackgroundPaint", paint );
	}

	public BasicStyleSheet withButtonHighlightBackgroundPaint(Paint paint)
	{
		return (BasicStyleSheet)withAttr( "buttonHighlightBackgroundPaint", paint );
	}
	
	
	
	//
	// FRACTION
	//
	
	public BasicStyleSheet withFractionVSpacing(double vSpacing)
	{
		return (BasicStyleSheet)withAttr( "fractionVSpacing", vSpacing );
	}

	public BasicStyleSheet withFractionHPadding(double hPadding)
	{
		return (BasicStyleSheet)withAttr( "fractionHPadding", hPadding );
	}

	public BasicStyleSheet withFractionRefYOffset(double refYOffset)
	{
		return (BasicStyleSheet)withAttr( "fractionRefYOffset", refYOffset );
	}
	
	
	
	//
	// HBOX
	//
	
	public BasicStyleSheet withHBoxSpacing(double spacing)
	{
		return (BasicStyleSheet)withAttr( "hboxSpacing", spacing );
	}

	

	//
	// LINE
	//
	
	public BasicStyleSheet withLineDirection(LineStyleParams.Direction direction)
	{
		return (BasicStyleSheet)withAttr( "lineDirection", direction );
	}

	public BasicStyleSheet withLineThickness(double thickness)
	{
		return (BasicStyleSheet)withAttr( "lineThickness", thickness );
	}

	public BasicStyleSheet withLineInset(double inset)
	{
		return (BasicStyleSheet)withAttr( "lineInset", inset );
	}

	public BasicStyleSheet withLinePadding(double padding)
	{
		return (BasicStyleSheet)withAttr( "linePadding", padding );
	}

	

	//
	// LINE
	//
	
	public BasicStyleSheet withLinkFont(Font font)
	{
		return (BasicStyleSheet)withAttr( "linkFont", font );
	}

	public BasicStyleSheet withLinkPaint(Paint paint)
	{
		return (BasicStyleSheet)withAttr( "linkPaint", paint );
	}

	public BasicStyleSheet withLinkSmallCaps(boolean smallCaps)
	{
		return (BasicStyleSheet)withAttr( "linkSmallCaps", smallCaps );
	}


	
	//
	// MATH ROOT
	//
	
	public BasicStyleSheet withMathRootThickness(double thickness)
	{
		return (BasicStyleSheet)withAttr( "mathRootThickness", thickness );
	}
	
	
	
	//
	// PARAGRAPH
	//
	
	public BasicStyleSheet withParagraphSpacing(double spacing)
	{
		return (BasicStyleSheet)withAttr( "paragraphSpacing", spacing );
	}

	public BasicStyleSheet withParagraphLineSpacing(double lineSpacing)
	{
		return (BasicStyleSheet)withAttr( "paragraphLineSpacing", lineSpacing );
	}

	public BasicStyleSheet withParagraphIndentation(double indentation)
	{
		return (BasicStyleSheet)withAttr( "paragraphIndentation", indentation );
	}


	
	//
	// SCRIPT
	//
	
	public BasicStyleSheet withScriptColumnSpacing(double columnSpacing)
	{
		return (BasicStyleSheet)withAttr( "scriptColumnSpacing", columnSpacing );
	}

	public BasicStyleSheet withScriptRowSpacing(double rowSpacing)
	{
		return (BasicStyleSheet)withAttr( "scriptRowSpacing", rowSpacing );
	}


	
	//
	// TABLE
	//
	
	public BasicStyleSheet withTableColumnSpacing(double columnSpacing)
	{
		return (BasicStyleSheet)withAttr( "tableColumnSpacing", columnSpacing );
	}

	public BasicStyleSheet withTableColumnExpand(boolean columnExpand)
	{
		return (BasicStyleSheet)withAttr( "tableColumnExpand", columnExpand );
	}

	public BasicStyleSheet withTableRowSpacing(double rowSpacing)
	{
		return (BasicStyleSheet)withAttr( "tableRowSpacing", rowSpacing );
	}

	public BasicStyleSheet withTableRowExpand(boolean rowExpand)
	{
		return (BasicStyleSheet)withAttr( "tableRowExpand", rowExpand );
	}


	
	//
	// TEXT
	//
	
	public BasicStyleSheet withTextSquiggleUnderlinePaint(Paint paint)
	{
		return (BasicStyleSheet)withAttr( "textSquiggleUnderlinePaint", paint );
	}

	public BasicStyleSheet withTextSmallCaps(boolean smallCaps)
	{
		return (BasicStyleSheet)withAttr( "textSmallCaps", smallCaps );
	}
	
	

	//
	// VBOX
	//
	
	public BasicStyleSheet withVBoxSpacing(double spacing)
	{
		return (BasicStyleSheet)withAttr( "vboxSpacing", spacing );
	}


	
	
	private ButtonStyleParams buttonParams = null;

	private ButtonStyleParams getButtonParams()
	{
		if ( buttonParams == null )
		{
			buttonParams = new ButtonStyleParams(
					get( "buttonBorderPaint", Paint.class, default_buttonBorderPaint ),
					get( "buttonBackgroundPaint", Paint.class, default_buttonBackgroundPaint ),
					get( "buttonHighlightBackgroundPaint", Paint.class, default_buttonHighlightBackgroundPaint ) );
		}
		return buttonParams;
	}

	
	private ContainerStyleParams containerParams = null;

	private ContainerStyleParams getContainerParams()
	{
		if ( containerParams == null )
		{
			containerParams = new ContainerStyleParams();
		}
		return containerParams;
	}

	
	private FractionStyleParams fractionParams = null;

	private FractionStyleParams getFractionParams()
	{
		if ( fractionParams == null )
		{
			fractionParams = new FractionStyleParams(
					get( "fractionVSpacing", Double.class, 2.0 ),
					get( "fractionHPadding", Double.class, 3.0 ),
					get( "fractionRefYOffset", Double.class, 5.0 ),
					get( "foregroundPaint", Paint.class, Color.black ) );
		}
		return fractionParams;
	}
	
	
	private GridRowStyleParams gridRowParams = null;

	private GridRowStyleParams getGridRowParams()
	{
		if ( gridRowParams == null )
		{
			gridRowParams = new GridRowStyleParams();
		}
		return gridRowParams;
	}
	
	
	private HBoxStyleParams hboxParams = null;

	private HBoxStyleParams getHBoxParams()
	{
		if ( hboxParams == null )
		{
			hboxParams = new HBoxStyleParams(
					get( "hboxSpacing", Double.class, 0.0 ) );
		}
		return hboxParams;
	}

	
	private LineStyleParams lineParams = null;

	private LineStyleParams getLineParams()
	{
		if ( lineParams == null )
		{
			lineParams = new LineStyleParams(
					get( "lineDirection", LineStyleParams.Direction.class, LineStyleParams.Direction.HORIZONTAL ),
					get( "foregroundPaint", Paint.class, Color.black ),
					get( "lineThickness", Double.class, 1.0 ),
					get( "lineInset", Double.class, 0.0 ),
					get( "linePadding", Double.class, 0.0 ) );
		}
		return lineParams;
	}
	
	
	private LinkStyleParams linkParams = null;

	private LinkStyleParams getLinkParams()
	{
		if ( linkParams == null )
		{
			linkParams = new LinkStyleParams(
					get( "linkFont", Font.class, defaultLinkFont ),
					get( "linkPaint", Paint.class, Color.black ),
					get( "linkSmallCaps", Boolean.class, false ) );
		}
		return linkParams;
	}
	
	
	private MathRootStyleParams mathRootParams = null;

	private MathRootStyleParams getMathRootParams()
	{
		if ( mathRootParams == null )
		{
			mathRootParams = new MathRootStyleParams(
					get( "font", Font.class, defaultFont ),
					get( "foregroundPaint", Paint.class, Color.black ),
					get( "mathRootThickness", Double.class, 1.5 ) );
		}
		return mathRootParams;
	}
	
	
	private ParagraphStyleParams paragraphParams = null;

	private ParagraphStyleParams getParagraphParams()
	{
		if ( paragraphParams == null )
		{
			paragraphParams = new ParagraphStyleParams(
					get( "paragraphSpacing", Double.class, 0.0 ),
					get( "paragraphLineSpacing", Double.class, 0.0 ),
					get( "paragraphIndentation", Double.class, 0.0 ) );
		}
		return paragraphParams;
	}
	
	
	private ScriptStyleParams scriptParams = null;

	private ScriptStyleParams getScriptParams()
	{
		if ( scriptParams == null )
		{
			scriptParams = new ScriptStyleParams(
					get( "scriptColumnSpacing", Double.class, 1.0 ),
					get( "scriptRowSpacing", Double.class, 1.0 ) );
		}
		return scriptParams;
	}
	
	
	private StaticTextStyleParams staticTextParams = null;

	private StaticTextStyleParams getStaticTextParams()
	{
		if ( staticTextParams == null )
		{
			staticTextParams = new StaticTextStyleParams(
					get( "font", Font.class, defaultFont ),
					get( "foregroundPaint", Paint.class, Color.black ),
					getNonNull( "textSmallCaps", Boolean.class, false ) );
		}
		return staticTextParams;
	}
	
	
	private TableStyleParams tableParams = null;

	private TableStyleParams getTableParams()
	{
		if ( tableParams == null )
		{
			tableParams = new TableStyleParams(
					get( "tableColumnSpacing", Double.class, 0.0 ),
					get( "tableColumnExpand", Boolean.class, false ),
					get( "tableRowSpacing", Double.class, 0.0 ),
					get( "tableRowExpand", Boolean.class, false ) );
		}
		return tableParams;
	}
	
	
	private TextStyleParams textParams = null;

	private TextStyleParams getTextParams()
	{
		if ( textParams == null )
		{
			textParams = new TextStyleParams(
					get( "font", Font.class, defaultFont ),
					get( "foregroundPaint", Paint.class, Color.black ),
					get( "textSquiggleUnderlinePaint", Paint.class, null ),
					getNonNull( "textSmallCaps", Boolean.class, false ) );
		}
		return textParams;
	}
	
	
	private VBoxStyleParams vboxParams = null;

	private VBoxStyleParams getVBoxParams()
	{
		if ( vboxParams == null )
		{
			vboxParams = new VBoxStyleParams(
					get( "vboxSpacing", Double.class, 0.0 ) );
		}
		return vboxParams;
	}

	
	
	public DPButton button(DPButton.ButtonListener listener, DPWidget child)
	{
		DPButton element = new DPButton( getButtonParams(), listener );
		element.setChild( child );
		return element;
	}
	
	public DPButton button(PyObject listener, DPWidget child)
	{
		DPButton element = new DPButton( getButtonParams(), listener );
		element.setChild( child );
		return element;
	}

	
	public DPFraction fraction(DPWidget numerator, DPWidget denominator, String barContent)
	{
		DPFraction element = new DPFraction( getFractionParams(), getTextParams(), barContent );
		element.setNumeratorChild( numerator );
		element.setDenominatorChild( denominator );
		return element;
	}

	
	public DPHBox hbox(List<DPWidget> children)
	{
		DPHBox element = new DPHBox( getHBoxParams() );
		element.setChildren( children );
		return element;
	}

	
	public DPLine line()
	{
		return new DPLine( getLineParams() );
	}
	

	public DPLink link(String txt, String targetLocation)
	{
		return new DPLink( getLinkParams(), txt, targetLocation );
	}
	
	public DPLink link(String txt, DPLink.LinkListener listener)
	{
		return new DPLink( getLinkParams(), txt, listener );
	}
	
	public DPLink link(String txt, PyObject listener)
	{
		return new DPLink( getLinkParams(), txt, listener );
	}
	
	
	public DPMathRoot mathRoot(DPWidget child)
	{
		DPMathRoot element = new DPMathRoot( getMathRootParams() );
		element.setChild( child );
		return element;
	}
	
	
	public DPParagraph paragraph(List<DPWidget> children)
	{
		DPParagraph element = new DPParagraph( getParagraphParams() );
		element.setChildren( children );
		return element;
	}
	

	public DPLineBreak lineBreak(DPWidget child)
	{
		DPLineBreak element = new DPLineBreak( getContainerParams() );
		if ( child != null )
		{
			element.setChild( child );
		}
		return element;
	}
	
	public DPLineBreak lineBreak()
	{
		return new DPLineBreak( getContainerParams() );
	}
	
	public DPParagraphIndentMarker paragraphIndentMarker()
	{
		return new DPParagraphIndentMarker( );
	}
	
	public DPParagraphDedentMarker paragraphDedentMarker()
	{
		return new DPParagraphDedentMarker( );
	}

	
	public DPScript script(DPWidget mainChild, DPWidget leftSuperChild, DPWidget leftSubChild, DPWidget rightSuperChild, DPWidget rightSubChild)
	{
		DPScript element = new DPScript( getScriptParams(), getTextParams() );
		element.setMainChild( mainChild );
		if ( leftSuperChild != null )
		{
			element.setLeftSuperscriptChild( leftSuperChild );
		}
		if ( leftSubChild != null )
		{
			element.setLeftSubscriptChild( leftSubChild );
		}
		if ( rightSuperChild != null )
		{
			element.setRightSuperscriptChild( rightSuperChild );
		}
		if ( rightSubChild != null )
		{
			element.setRightSubscriptChild( rightSubChild );
		}
		return element;
	}
	
	
	public DPScript scriptLSuper(DPWidget mainChild, DPWidget scriptChild)
	{
		return script( mainChild, scriptChild, null, null, null );
	}
	
	public DPScript scriptLSub(DPWidget mainChild, DPWidget scriptChild)
	{
		return script( mainChild, null, scriptChild, null, null );
	}
	
	public DPScript scriptRSuper(DPWidget mainChild, DPWidget scriptChild)
	{
		return script( mainChild, null, null, scriptChild, null );
	}
	
	public DPScript scriptRSub(DPWidget mainChild, DPWidget scriptChild)
	{
		return script( mainChild, null, null, null, scriptChild );
	}
	
	
	public DPStaticText staticText(String txt)
	{
		return new DPStaticText( getStaticTextParams(), txt );
	}
	
	
	public DPWidget gridRow(List<DPWidget> children)
	{
		DPGridRow element = new DPGridRow( getGridRowParams() );
		element.setChildren( children );
		return element;
	}
	
	public DPWidget rgrid(List<DPWidget> children)
	{
		DPRGrid element = new DPRGrid( getTableParams() );
		element.setChildren( children );
		return element;
	}
	
	
	public DPTable table(DPWidget children[][])
	{
		DPTable element = new DPTable( getTableParams() );
		element.setChildren( children );
		return element;
	}
	

	public DPText text(String text)
	{
		return new DPText( getTextParams(), text );
	}

	public DPText textWithContent(String text, String textRepresentation)
	{
		return new DPText( getTextParams(), text, textRepresentation );
	}

	
	public DPVBox vbox(List<DPWidget> children)
	{
		DPVBox element = new DPVBox( getVBoxParams() );
		element.setChildren( children );
		return element;
	}
	
	
	public DPWhitespace whitespace(String txt, float width)
	{
		return new DPWhitespace( txt, width );
	}

	public DPWhitespace whitespace(String txt)
	{
		return new DPWhitespace( txt, 0.0 );
	}
}
