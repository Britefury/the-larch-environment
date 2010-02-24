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

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPButton;
import BritefuryJ.DocPresent.DPHiddenContent;
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
import BritefuryJ.DocPresent.DPParagraphStructureSpan;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWhitespace;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleParams.ButtonStyleParams;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;
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

public class PrimitiveStyleSheet extends StyleSheet
{
	private static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );
	
	private static final Border default_border = new EmptyBorder();

	private static final Paint default_buttonBorderPaint = new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 0.2f, 0.3f, 0.5f ), new Color( 0.3f, 0.45f, 0.75f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE );
	private static final Paint default_buttonBackgroundPaint = new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 0.9f, 0.92f, 1.0f ), new Color( 0.75f, 0.825f, 0.9f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE );
	private static final Paint default_buttonHighlightBackgroundPaint = new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 1.0f, 1.0f, 1.0f ), new Color( 0.85f, 0.85f, 0.85f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE );
	
	private static final Font defaultLinkFont = new Font( "Sans serif", Font.PLAIN, 14 );
	

	
	public static final PrimitiveStyleSheet instance = new PrimitiveStyleSheet();

	
	
	protected PrimitiveStyleSheet()
	{
		super();
		
		initAttr( "font", defaultFont );
		initAttr( "foreground", Color.black );
		initAttr( "background", null );

		initAttr( "border", default_border );
		
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
	
	
	public Object newInstance()
	{
		return new PrimitiveStyleSheet();
	}
	

	
	
	//
	// GENERAL
	//
	
	public PrimitiveStyleSheet withFont(Font font)
	{
		return (PrimitiveStyleSheet)withAttr( "font", font );
	}

	public PrimitiveStyleSheet withForeground(Paint paint)
	{
		return (PrimitiveStyleSheet)withAttr( "foreground", paint );
	}

	public PrimitiveStyleSheet withBackground(Painter background)
	{
		return (PrimitiveStyleSheet)withAttr( "background", background );
	}


	
	//
	// BORDER
	//
	
	public PrimitiveStyleSheet withBorder(Border border)
	{
		return (PrimitiveStyleSheet)withAttr( "border", border );
	}


	
	//
	// BUTTON
	//
	
	public PrimitiveStyleSheet withButtonBorderPaint(Paint paint)
	{
		return (PrimitiveStyleSheet)withAttr( "buttonBorderPaint", paint );
	}

	public PrimitiveStyleSheet withButtonBackgroundPaint(Paint paint)
	{
		return (PrimitiveStyleSheet)withAttr( "buttonBackgroundPaint", paint );
	}

	public PrimitiveStyleSheet withButtonHighlightBackgroundPaint(Paint paint)
	{
		return (PrimitiveStyleSheet)withAttr( "buttonHighlightBackgroundPaint", paint );
	}
	
	
	
	//
	// FRACTION
	//
	
	public PrimitiveStyleSheet withFractionVSpacing(double vSpacing)
	{
		return (PrimitiveStyleSheet)withAttr( "fractionVSpacing", vSpacing );
	}

	public PrimitiveStyleSheet withFractionHPadding(double hPadding)
	{
		return (PrimitiveStyleSheet)withAttr( "fractionHPadding", hPadding );
	}

	public PrimitiveStyleSheet withFractionRefYOffset(double refYOffset)
	{
		return (PrimitiveStyleSheet)withAttr( "fractionRefYOffset", refYOffset );
	}
	
	
	
	//
	// HBOX
	//
	
	public PrimitiveStyleSheet withHBoxSpacing(double spacing)
	{
		return (PrimitiveStyleSheet)withAttr( "hboxSpacing", spacing );
	}

	

	//
	// LINE
	//
	
	public PrimitiveStyleSheet withLineDirection(LineStyleParams.Direction direction)
	{
		return (PrimitiveStyleSheet)withAttr( "lineDirection", direction );
	}

	public PrimitiveStyleSheet withLineThickness(double thickness)
	{
		return (PrimitiveStyleSheet)withAttr( "lineThickness", thickness );
	}

	public PrimitiveStyleSheet withLineInset(double inset)
	{
		return (PrimitiveStyleSheet)withAttr( "lineInset", inset );
	}

	public PrimitiveStyleSheet withLinePadding(double padding)
	{
		return (PrimitiveStyleSheet)withAttr( "linePadding", padding );
	}

	

	//
	// LINE
	//
	
	public PrimitiveStyleSheet withLinkFont(Font font)
	{
		return (PrimitiveStyleSheet)withAttr( "linkFont", font );
	}

	public PrimitiveStyleSheet withLinkPaint(Paint paint)
	{
		return (PrimitiveStyleSheet)withAttr( "linkPaint", paint );
	}

	public PrimitiveStyleSheet withLinkSmallCaps(boolean smallCaps)
	{
		return (PrimitiveStyleSheet)withAttr( "linkSmallCaps", smallCaps );
	}


	
	//
	// MATH ROOT
	//
	
	public PrimitiveStyleSheet withMathRootThickness(double thickness)
	{
		return (PrimitiveStyleSheet)withAttr( "mathRootThickness", thickness );
	}
	
	
	
	//
	// PARAGRAPH
	//
	
	public PrimitiveStyleSheet withParagraphSpacing(double spacing)
	{
		return (PrimitiveStyleSheet)withAttr( "paragraphSpacing", spacing );
	}

	public PrimitiveStyleSheet withParagraphLineSpacing(double lineSpacing)
	{
		return (PrimitiveStyleSheet)withAttr( "paragraphLineSpacing", lineSpacing );
	}

	public PrimitiveStyleSheet withParagraphIndentation(double indentation)
	{
		return (PrimitiveStyleSheet)withAttr( "paragraphIndentation", indentation );
	}


	
	//
	// SCRIPT
	//
	
	public PrimitiveStyleSheet withScriptColumnSpacing(double columnSpacing)
	{
		return (PrimitiveStyleSheet)withAttr( "scriptColumnSpacing", columnSpacing );
	}

	public PrimitiveStyleSheet withScriptRowSpacing(double rowSpacing)
	{
		return (PrimitiveStyleSheet)withAttr( "scriptRowSpacing", rowSpacing );
	}


	
	//
	// TABLE
	//
	
	public PrimitiveStyleSheet withTableColumnSpacing(double columnSpacing)
	{
		return (PrimitiveStyleSheet)withAttr( "tableColumnSpacing", columnSpacing );
	}

	public PrimitiveStyleSheet withTableColumnExpand(boolean columnExpand)
	{
		return (PrimitiveStyleSheet)withAttr( "tableColumnExpand", columnExpand );
	}

	public PrimitiveStyleSheet withTableRowSpacing(double rowSpacing)
	{
		return (PrimitiveStyleSheet)withAttr( "tableRowSpacing", rowSpacing );
	}

	public PrimitiveStyleSheet withTableRowExpand(boolean rowExpand)
	{
		return (PrimitiveStyleSheet)withAttr( "tableRowExpand", rowExpand );
	}


	
	//
	// TEXT
	//
	
	public PrimitiveStyleSheet withTextSquiggleUnderlinePaint(Paint paint)
	{
		return (PrimitiveStyleSheet)withAttr( "textSquiggleUnderlinePaint", paint );
	}

	public PrimitiveStyleSheet withTextSmallCaps(boolean smallCaps)
	{
		return (PrimitiveStyleSheet)withAttr( "textSmallCaps", smallCaps );
	}
	
	

	//
	// VBOX
	//
	
	public PrimitiveStyleSheet withVBoxSpacing(double spacing)
	{
		return (PrimitiveStyleSheet)withAttr( "vboxSpacing", spacing );
	}


	
	
	private static class BorderParams
	{
		public Border border;
		
		public BorderParams(Border border)
		{
			this.border = border;
		}
	}
	
	BorderParams borderParams = null;

	private BorderParams getBorderParams()
	{
		if ( borderParams == null )
		{
			borderParams = new BorderParams(
					get( "border", Border.class, default_border ) );
		}
		return borderParams;
	}

	
	private ButtonStyleParams buttonParams = null;

	private ButtonStyleParams getButtonParams()
	{
		if ( buttonParams == null )
		{
			buttonParams = new ButtonStyleParams(
					get( "background", Painter.class, null ),
					get( "buttonBorderPaint", Paint.class, default_buttonBorderPaint ),
					get( "buttonBackgroundPaint", Paint.class, default_buttonBackgroundPaint ), get( "buttonHighlightBackgroundPaint", Paint.class, default_buttonHighlightBackgroundPaint ) );
		}
		return buttonParams;
	}

	
	private ContainerStyleParams containerParams = null;

	private ContainerStyleParams getContainerParams()
	{
		if ( containerParams == null )
		{
			containerParams = new ContainerStyleParams(
					get( "background", Painter.class, null ) );
		}
		return containerParams;
	}

	
	private ContentLeafStyleParams contentLeafParams = null;

	private ContentLeafStyleParams getContentLeafStyleParams()
	{
		if ( contentLeafParams == null )
		{
			contentLeafParams = new ContentLeafStyleParams(
					get( "background", Painter.class, null ) );
		}
		return contentLeafParams;
	}

	
	private FractionStyleParams fractionParams = null;

	private FractionStyleParams getFractionParams()
	{
		if ( fractionParams == null )
		{
			fractionParams = new FractionStyleParams(
					get( "background", Painter.class, null ),
					get( "fractionVSpacing", Double.class, 2.0 ),
					get( "fractionHPadding", Double.class, 3.0 ),
					get( "fractionRefYOffset", Double.class, 5.0 ),
					get( "background", Painter.class, null ),
					get( "foreground", Paint.class, Color.black ) );
		}
		return fractionParams;
	}
	
	
	private GridRowStyleParams gridRowParams = null;

	private GridRowStyleParams getGridRowParams()
	{
		if ( gridRowParams == null )
		{
			gridRowParams = new GridRowStyleParams(
					get( "background", Painter.class, null ) );
		}
		return gridRowParams;
	}
	
	
	private HBoxStyleParams hboxParams = null;

	private HBoxStyleParams getHBoxParams()
	{
		if ( hboxParams == null )
		{
			hboxParams = new HBoxStyleParams(
					get( "background", Painter.class, null ),
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
					get( "background", Painter.class, null ),
					get( "lineDirection", LineStyleParams.Direction.class, LineStyleParams.Direction.HORIZONTAL ),
					get( "foreground", Paint.class, Color.black ),
					get( "lineThickness", Double.class, 1.0 ),
					get( "lineInset", Double.class, 0.0 ), get( "linePadding", Double.class, 0.0 ) );
		}
		return lineParams;
	}
	
	
	private LinkStyleParams linkParams = null;

	private LinkStyleParams getLinkParams()
	{
		if ( linkParams == null )
		{
			linkParams = new LinkStyleParams(
					get( "background", Painter.class, null ),
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
					get( "background", Painter.class, null ),
					get( "font", Font.class, defaultFont ),
					get( "foreground", Paint.class, Color.black ),
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
					get( "background", Painter.class, null ),
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
					get( "background", Painter.class, null ),
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
					get( "background", Painter.class, null ),
					get( "font", Font.class, defaultFont ),
					get( "foreground", Paint.class, Color.black ),
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
					get( "background", Painter.class, null ),
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
					get( "background", Painter.class, null ),
					get( "font", Font.class, defaultFont ),
					get( "foreground", Paint.class, Color.black ),
					get( "textSquiggleUnderlinePaint", Paint.class, null ), getNonNull( "textSmallCaps", Boolean.class, false ) );
		}
		return textParams;
	}
	
	
	private VBoxStyleParams vboxParams = null;

	private VBoxStyleParams getVBoxParams()
	{
		if ( vboxParams == null )
		{
			vboxParams = new VBoxStyleParams(
					get( "background", Painter.class, null ),
					get( "vboxSpacing", Double.class, 0.0 ) );
		}
		return vboxParams;
	}

	
	
	public DPBorder border(DPWidget child)
	{
		DPBorder border = new DPBorder( getBorderParams().border );
		border.setChild( child );
		return border;
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
	
	
	public DPHiddenContent hiddenContent(String textRepresentation)
	{
		return new DPHiddenContent( textRepresentation );
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
	
	public DPSpan span(List<DPWidget> children)
	{
		DPSpan element = new DPSpan( getContainerParams() );
		element.setChildren( children );
		return element;
	}
	
	public DPParagraphStructureSpan paragraphStructureSpan(List<DPWidget> children)
	{
		DPParagraphStructureSpan element = new DPParagraphStructureSpan( getContainerParams() );
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
		return new DPParagraphIndentMarker();
	}
	
	public DPParagraphDedentMarker paragraphDedentMarker()
	{
		return new DPParagraphDedentMarker();
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
	
	
	public DPSegment segment(boolean bGuardBegin, boolean bGuardEnd, DPWidget child)
	{
		DPSegment seg = new DPSegment( getContainerParams(), getTextParams(), bGuardBegin, bGuardEnd );
		seg.setChild( child );
		return seg;
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
	
	
	public DPWhitespace whitespace(String txt, double width)
	{
		return new DPWhitespace( getContentLeafStyleParams(), txt, width );
	}

	public DPWhitespace whitespace(String txt)
	{
		return new DPWhitespace( getContentLeafStyleParams(), txt, 0.0 );
	}
}
