//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.python.expose.ExposedMethod;

import BritefuryJ.DocPresent.DPAspectRatioBin;
import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPBox;
import BritefuryJ.DocPresent.DPCanvas;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPGridRow;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPHiddenContent;
import BritefuryJ.DocPresent.DPImage;
import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPLineBreakCostSpan;
import BritefuryJ.DocPresent.DPMathRoot;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPParagraphDedentMarker;
import BritefuryJ.DocPresent.DPParagraphIndentMarker;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.DPShape;
import BritefuryJ.DocPresent.DPSpaceBin;
import BritefuryJ.DocPresent.DPSpacer;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPViewport;
import BritefuryJ.DocPresent.DPWhitespace;
import BritefuryJ.DocPresent.Border.AbstractBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Canvas.DrawingNode;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;
import BritefuryJ.DocPresent.StyleParams.FractionStyleParams;
import BritefuryJ.DocPresent.StyleParams.GridRowStyleParams;
import BritefuryJ.DocPresent.StyleParams.HBoxStyleParams;
import BritefuryJ.DocPresent.StyleParams.MathRootStyleParams;
import BritefuryJ.DocPresent.StyleParams.ParagraphStyleParams;
import BritefuryJ.DocPresent.StyleParams.ScriptStyleParams;
import BritefuryJ.DocPresent.StyleParams.ShapeStyleParams;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
import BritefuryJ.DocPresent.StyleParams.VBoxStyleParams;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class PrimitiveStyleSheet extends StyleSheet
{
	private static final String defaultFontFace = "Sans serif";
	private static final int defaultFontSize = 14;
	
	private static final float defaultFractionFontScale = 0.9f;
	private static final float defaultFractionMinFontScale = 0.9f;
	
	private static final float defaultScriptFontScale = 0.9f;
	private static final float defaultScriptMinFontScale = 0.9f;
	
	private static final Painter default_shapePainter = new FillPainter( Color.black );
	
	private static final AbstractBorder default_border = new SolidBorder( 1.0, 2.0, Color.black, null );


	
	public static final PrimitiveStyleSheet instance = new PrimitiveStyleSheet();

	
	
	protected PrimitiveStyleSheet()
	{
		super();
		
		initAttr( "fontFace", defaultFontFace );
		initAttr( "fontItalic", false );
		initAttr( "fontBold", false );
		initAttr( "fontSize", defaultFontSize );
		initAttr( "fontScale", 1.0f );
		initAttr( "foreground", Color.black );
		initAttr( "hoverForeground", null );
		initAttr( "background", null );
		initAttr( "hoverBackground", null );
		initAttr( "shapePainter", default_shapePainter );
		initAttr( "hoverShapePainter", null );
		initAttr( "cursor", null );

		initAttr( "border", default_border );
		
		initAttr( "fractionVSpacing", 2.0 );
		initAttr( "fractionHPadding", 3.0 );
		initAttr( "fractionRefYOffset", 5.0 );
		initAttr( "fractionFontScale", defaultFractionFontScale );
		initAttr( "fractionMinFontScale", defaultFractionMinFontScale );
		
		initAttr( "hboxSpacing", 0.0 );
		
		initAttr( "mathRootThickness", 1.5 );
		
		initAttr( "paragraphSpacing", 0.0 );
		initAttr( "paragraphLineSpacing", 0.0 );
		initAttr( "paragraphIndentation", 0.0 );
		
		initAttr( "scriptColumnSpacing", 1.0 );
		initAttr( "scriptRowSpacing", 1.0 );
		initAttr( "scriptFontScale", defaultScriptFontScale );
		initAttr( "scriptMinFontScale", defaultScriptMinFontScale );
		
		initAttr( "tableColumnSpacing", 0.0 );
		initAttr( "tableColumnExpand", false );
		initAttr( "tableRowSpacing", 0.0 );
		initAttr( "tableRowExpand", false );
		
		initAttr( "textSquiggleUnderlinePaint", null );
		initAttr( "textSmallCaps", false );
		
		initAttr( "vboxSpacing", 0.0 );
		
		
		initAttr( "editable", true );
	}
	
	
	protected StyleSheet newInstance()
	{
		return new PrimitiveStyleSheet();
	}
	

	
	
	//
	// GENERAL
	//
	
	public PrimitiveStyleSheet withFontFace(String face)
	{
		return (PrimitiveStyleSheet)withAttr( "fontFace", face );
	}

	public PrimitiveStyleSheet withFontBold(boolean bBold)
	{
		return (PrimitiveStyleSheet)withAttr( "fontBold", bBold );
	}

	public PrimitiveStyleSheet withFontItalic(boolean bItalic)
	{
		return (PrimitiveStyleSheet)withAttr( "fontItalic", bItalic );
	}

	public PrimitiveStyleSheet withFontSize(int size)
	{
		return (PrimitiveStyleSheet)withAttr( "fontSize", size );
	}

	public PrimitiveStyleSheet withFontScale(float fontScale)
	{
		return (PrimitiveStyleSheet)withAttr( "fontScale", fontScale );
	}

	public PrimitiveStyleSheet withForeground(Paint paint)
	{
		return (PrimitiveStyleSheet)withAttr( "foreground", paint );
	}

	public PrimitiveStyleSheet withHoverForeground(Paint paint)
	{
		return (PrimitiveStyleSheet)withAttr( "hoverForeground", paint );
	}

	public PrimitiveStyleSheet withBackground(Painter background)
	{
		return (PrimitiveStyleSheet)withAttr( "background", background );
	}

	public PrimitiveStyleSheet withHoverBackground(Painter hoverBackground)
	{
		return (PrimitiveStyleSheet)withAttr( "hoverBackground", hoverBackground );
	}

	public PrimitiveStyleSheet withShapePainter(Painter shapePainter)
	{
		return (PrimitiveStyleSheet)withAttr( "shapePainter", shapePainter );
	}

	public PrimitiveStyleSheet withHoverShapePainter(Painter shapePainter)
	{
		return (PrimitiveStyleSheet)withAttr( "hoverShapePainter", shapePainter );
	}

	public PrimitiveStyleSheet withCursor(Cursor cursor)
	{
		return (PrimitiveStyleSheet)withAttr( "cursor", cursor );
	}

	
	
	//
	// BORDER
	//
	
	public PrimitiveStyleSheet withBorder(AbstractBorder border)
	{
		return (PrimitiveStyleSheet)withAttr( "border", border );
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
	
	public PrimitiveStyleSheet withFractionFontScale(float fontScale)
	{
		return (PrimitiveStyleSheet)withAttr( "fractionFontScale", fontScale );
	}
	
	public PrimitiveStyleSheet withFractionMinFontScale(float fontScale)
	{
		return (PrimitiveStyleSheet)withAttr( "fractionMinFontScale", fontScale );
	}
	
	
	
	//
	// HBOX
	//
	
	public PrimitiveStyleSheet withHBoxSpacing(double spacing)
	{
		return (PrimitiveStyleSheet)withAttr( "hboxSpacing", spacing );
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

	public PrimitiveStyleSheet withScriptFontScale(float fontScale)
	{
		return (PrimitiveStyleSheet)withAttr( "scriptFontScale", fontScale );
	}
	
	public PrimitiveStyleSheet withScriptMinFontScale(float fontScale)
	{
		return (PrimitiveStyleSheet)withAttr( "scriptMinFontScale", fontScale );
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
	
	
	
	//
	// GENERIC CONTROL
	//
	
	public PrimitiveStyleSheet withEditability(boolean bEditable)
	{
		return (PrimitiveStyleSheet)withAttr( "editable", bEditable );
	}
	
	public PrimitiveStyleSheet withEditable()
	{
		return withEditability( true );
	}
	
	public PrimitiveStyleSheet withNonEditable()
	{
		return withEditability( false );
	}
	
	public boolean isEditable()
	{
		return getNonNull( "editable", Boolean.class, true );
	}
	

	
	
	
	private Font styleSheetFont = null;
	
	private Font getFont()
	{
		if ( styleSheetFont == null )
		{
			String fontFace = getNonNull( "fontFace", String.class, defaultFontFace );
			boolean bBold = getNonNull( "fontBold", Boolean.class, false );
			boolean bItalic = getNonNull( "fontItalic", Boolean.class, false );
			int size = getNonNull( "fontSize", Integer.class, defaultFontSize );
			float scale = getNonNull( "fontScale", Float.class, 1.0f );
			int flags = ( bBold ? Font.BOLD : 0 )  |  ( bItalic ? Font.ITALIC : 0 );
			styleSheetFont = new Font( fontFace, flags, size ).deriveFont( (float)size * scale );
		}
		return styleSheetFont;
	}

	
	
	private static class BorderParams
	{
		public AbstractBorder border;
		
		public BorderParams(AbstractBorder border)
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
					get( "border", AbstractBorder.class, default_border ) );
		}
		return borderParams;
	}

	
	private ContainerStyleParams containerParams = null;

	private ContainerStyleParams getContainerParams()
	{
		if ( containerParams == null )
		{
			containerParams = new ContainerStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ) );
		}
		return containerParams;
	}

	
	private ContentLeafStyleParams contentLeafParams = null;

	private ContentLeafStyleParams getContentLeafStyleParams()
	{
		if ( contentLeafParams == null )
		{
			contentLeafParams = new ContentLeafStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ) );
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
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "fractionVSpacing", Double.class, 2.0 ),
					getNonNull( "fractionHPadding", Double.class, 3.0 ),
					getNonNull( "fractionRefYOffset", Double.class, 5.0 ),
					getFractionBarParams() );
		}
		return fractionParams;
	}
	
	
	private FractionStyleParams.BarStyleParams fractionBarParams = null;

	private FractionStyleParams.BarStyleParams getFractionBarParams()
	{
		if ( fractionBarParams == null )
		{
			fractionBarParams = new FractionStyleParams.BarStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "editable", Boolean.class, true ),
					getNonNull( "foreground", Paint.class, Color.black ),
					get( "hoverForeground", Paint.class, null ) );
		}
		return fractionBarParams;
	}
	
	
	private GridRowStyleParams gridRowParams = null;

	private GridRowStyleParams getGridRowParams()
	{
		if ( gridRowParams == null )
		{
			gridRowParams = new GridRowStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ) );
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
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "hboxSpacing", Double.class, 0.0 ) );
		}
		return hboxParams;
	}

	
	private MathRootStyleParams mathRootParams = null;

	private MathRootStyleParams getMathRootParams()
	{
		if ( mathRootParams == null )
		{
			mathRootParams = new MathRootStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getFont(),
					getNonNull( "foreground", Paint.class, Color.black ),
					get( "hoverForeground", Paint.class, null ),
					getNonNull( "mathRootThickness", Double.class, 1.5 ) );
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
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "paragraphSpacing", Double.class, 0.0 ),
					getNonNull( "paragraphLineSpacing", Double.class, 0.0 ),
					getNonNull( "paragraphIndentation", Double.class, 0.0 ) );
		}
		return paragraphParams;
	}
	
	
	private ShapeStyleParams shapeParams = null;

	private ShapeStyleParams getShapeParams()
	{
		if ( shapeParams == null )
		{
			shapeParams = new ShapeStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "shapePainter", Painter.class, default_shapePainter ),
					get( "hoverShapePainter", Painter.class, null ) );
		}
		return shapeParams;
	}
	
	
	private ScriptStyleParams scriptParams = null;

	private ScriptStyleParams getScriptParams()
	{
		if ( scriptParams == null )
		{
			scriptParams = new ScriptStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "scriptColumnSpacing", Double.class, 1.0 ),
					getNonNull( "scriptRowSpacing", Double.class, 1.0 ) );
		}
		return scriptParams;
	}
	
	
	private TextStyleParams staticTextParams = null;

	private TextStyleParams getStaticTextParams()
	{
		if ( staticTextParams == null )
		{
			staticTextParams = new TextStyleParams(
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					false,
					getFont(),
					getNonNull( "foreground", Paint.class, Color.black ),
					get( "hoverForeground", Paint.class, null ),
					get( "textSquiggleUnderlinePaint", Paint.class, null ),
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
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "tableColumnSpacing", Double.class, 0.0 ),
					getNonNull( "tableColumnExpand", Boolean.class, false ),
					getNonNull( "tableRowSpacing", Double.class, 0.0 ),
					getNonNull( "tableRowExpand", Boolean.class, false ) );
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
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "editable", Boolean.class, true ),
					getFont(),
					getNonNull( "foreground", Paint.class, Color.black ),
					get( "hoverForeground", Paint.class, null ),
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
					get( "background", Painter.class, null ),
					get( "hoverBackground", Painter.class, null ),
					get( "cursor", Cursor.class, null ),
					getNonNull( "vboxSpacing", Double.class, 0.0 ) );
		}
		return vboxParams;
	}
	
	
	private DPElement layoutWrap(DPElement child)
	{
		return child != null  ?  child.layoutWrap()  :  null;
	}

	
	
	public DPBin bin(DPElement child)
	{
		child = layoutWrap( child );
		DPBin bin = new DPBin( getContainerParams() );
		bin.setChild( child );
		return bin;
	}
	
	public DPSpaceBin spaceBin(DPElement child, double minWidth, double minHeight)
	{
		child = layoutWrap( child );
		DPSpaceBin bin = new DPSpaceBin( getContainerParams(), minWidth, minHeight );
		bin.setChild( child );
		return bin;
	}
	
	public DPAspectRatioBin aspectRatioBin(DPElement child, double minWidth, double aspectRatio)
	{
		child = layoutWrap( child );
		DPAspectRatioBin bin = new DPAspectRatioBin( getContainerParams(), minWidth, aspectRatio );
		bin.setChild( child );
		return bin;
	}
	
	public DPBorder border(DPElement child)
	{
		child = layoutWrap( child );
		DPBorder border = new DPBorder( getBorderParams().border );
		border.setChild( child );
		return border;
	}
	
	
	public DPCanvas canvas(DrawingNode drawing, double width, double height, boolean bShrinkX, boolean bShrinkY)
	{
		return canvas( drawing, width, height, bShrinkX, bShrinkY, "" );
	}
	
	public DPCanvas canvas(DrawingNode drawing, double width, double height, boolean bShrinkX, boolean bShrinkY, String textRepresentation)
	{
		return new DPCanvas( textRepresentation, drawing, width, height, bShrinkX, bShrinkY );
	}
	
	
	public DPFraction fraction(DPElement numerator, DPElement denominator, String barContent)
	{
		DPFraction element = new DPFraction( getFractionParams(), getTextParams(), barContent );
		element.setNumeratorChild( numerator );
		element.setDenominatorChild( denominator );
		return element;
	}
	
	public PrimitiveStyleSheet fractionNumeratorStyle()
	{
		float scale = getNonNull( "fontScale", Float.class, 1.0f );
		float fracScale = getNonNull( "fractionFontScale", Float.class, defaultFractionFontScale );
		float minFracScale = getNonNull( "fractionMinFontScale", Float.class, defaultFractionMinFontScale );
		scale = Math.max( scale * fracScale, minFracScale );
		return withFontScale( scale );
	}
	
	public PrimitiveStyleSheet fractionDenominatorStyle()
	{
		return fractionNumeratorStyle();
	}
	
	
	public DPHBox hbox(DPElement children[])
	{
		DPHBox element = new DPHBox( getHBoxParams() );
		element.setChildren( children );
		return element;
	}

	public DPHBox hbox(List<DPElement> children)
	{
		DPHBox element = new DPHBox( getHBoxParams() );
		element.setChildren( children );
		return element;
	}

	
	public DPHiddenContent hiddenContent(String textRepresentation)
	{
		return new DPHiddenContent( textRepresentation );
	}

	
	
	public DPMathRoot mathRoot(DPElement child)
	{
		child = layoutWrap( child );
		DPMathRoot element = new DPMathRoot( getMathRootParams() );
		element.setChild( child );
		return element;
	}
	
	
	@ExposedMethod( names={ "_paragraph" } )
	public DPParagraph paragraph(DPElement children[])
	{
		DPParagraph element = new DPParagraph( getParagraphParams() );
		element.setChildren( children );
		return element;
	}
	
	public DPParagraph paragraph(List<DPElement> children)
	{
		DPParagraph element = new DPParagraph( getParagraphParams() );
		element.setChildren( children );
		return element;
	}
	

	public DPProxy proxy(DPElement child)
	{
		DPProxy proxy = new DPProxy();
		proxy.setChild( child );
		proxy.copyAlignmentFlagsFrom( child );
		return proxy;
	}
	

	@ExposedMethod( names={ "_span" } )
	public DPSpan span(DPElement children[])
	{
		DPSpan element = new DPSpan( getContainerParams() );
		element.setChildren( children );
		return element;
	}
	
	public DPSpan span(List<DPElement> children)
	{
		DPSpan element = new DPSpan( getContainerParams() );
		element.setChildren( children );
		return element;
	}
	
	public DPLineBreakCostSpan lineBreakCostSpan(DPElement children[])
	{
		DPLineBreakCostSpan element = new DPLineBreakCostSpan( getContainerParams() );
		element.setChildren( children );
		return element;
	}
	
	public DPLineBreakCostSpan lineBreakCostSpan(List<DPElement> children)
	{
		DPLineBreakCostSpan element = new DPLineBreakCostSpan( getContainerParams() );
		element.setChildren( children );
		return element;
	}
	

	public DPLineBreak lineBreak()
	{
		return new DPLineBreak();
	}
	
	public DPParagraphIndentMarker paragraphIndentMarker()
	{
		return new DPParagraphIndentMarker();
	}
	
	public DPParagraphDedentMarker paragraphDedentMarker()
	{
		return new DPParagraphDedentMarker();
	}
	
	
	public DPRegion region(DPElement child)
	{
		DPRegion element = new DPRegion();
		element.setChild( child );
		element.copyAlignmentFlagsFrom( child );
		return element;
	}

	public DPRegion region(DPElement child, EditHandler editHandler)
	{
		DPRegion element = new DPRegion();
		element.setChild( child );
		element.setEditHandler( editHandler );
		element.copyAlignmentFlagsFrom( child );
		return element;
	}

	
	public DPScript script(DPElement mainChild, DPElement leftSuperChild, DPElement leftSubChild, DPElement rightSuperChild, DPElement rightSubChild)
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
	
	
	public DPScript scriptLSuper(DPElement mainChild, DPElement scriptChild)
	{
		return script( mainChild, scriptChild, null, null, null );
	}
	
	public DPScript scriptLSub(DPElement mainChild, DPElement scriptChild)
	{
		return script( mainChild, null, scriptChild, null, null );
	}
	
	public DPScript scriptRSuper(DPElement mainChild, DPElement scriptChild)
	{
		return script( mainChild, null, null, scriptChild, null );
	}
	
	public DPScript scriptRSub(DPElement mainChild, DPElement scriptChild)
	{
		return script( mainChild, null, null, null, scriptChild );
	}
	
	
	public PrimitiveStyleSheet scriptScriptChildStyle()
	{
		float scale = getNonNull( "fontScale", Float.class, 1.0f );
		float fracScale = getNonNull( "scriptFontScale", Float.class, defaultFractionFontScale );
		float minFracScale = getNonNull( "scriptMinFontScale", Float.class, defaultFractionMinFontScale );
		scale = Math.max( scale * fracScale, minFracScale );
		return withFontScale( scale );
	}
	
	
	public DPSegment segment(boolean bGuardBegin, boolean bGuardEnd, DPElement child)
	{
		DPSegment seg = new DPSegment( getContainerParams(), getTextParams(), bGuardBegin, bGuardEnd );
		seg.setChild( child );
		return seg;
	}
	
	
	public DPText staticText(String txt)
	{
		return new DPText( getStaticTextParams(), txt );
	}
	
	
	@ExposedMethod( names={ "_gridRow" } )
	public DPGridRow gridRow(DPElement children[])
	{
		DPGridRow element = new DPGridRow( getGridRowParams() );
		element.setChildren( children );
		return element;
	}
	
	public DPGridRow gridRow(List<DPElement> children)
	{
		DPGridRow element = new DPGridRow( getGridRowParams() );
		element.setChildren( children );
		return element;
	}
	
	public DPRGrid rgrid(DPElement children[])
	{
		DPRGrid element = new DPRGrid( getTableParams() );
		element.setChildren( children );
		return element;
	}
	
	public DPRGrid rgrid(List<DPElement> children)
	{
		DPRGrid element = new DPRGrid( getTableParams() );
		element.setChildren( children );
		return element;
	}
	
	
	public DPTable table()
	{
		return new DPTable( getTableParams() );
	}
	
	public DPTable table(DPElement children[][])
	{
		DPElement wrappedChildren[][] = new DPElement[children.length][];
		for (int y = 0; y < children.length; y++)
		{
			wrappedChildren[y] = new DPElement[children[y].length];
			for (int x = 0; x < children[y].length; x++)
			{
				wrappedChildren[y][x] = layoutWrap( children[y][x] );
			}
		}
		DPTable element = new DPTable( getTableParams() );
		element.setChildren( wrappedChildren );
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

	
	@ExposedMethod( names={ "_vbox" } )
	public DPVBox vbox(DPElement children[])
	{
		DPVBox element = new DPVBox( getVBoxParams() );
		element.setChildren( children );
		return element;
	}
	
	@ExposedMethod( names={ "_vbox" } )
	public DPVBox vbox(DPElement children[], int refPointIndex)
	{
		DPVBox element = new DPVBox( getVBoxParams() );
		element.setChildren( children );
		element.setRefPointIndex( refPointIndex );
		return element;
	}
	
	public DPVBox vbox(List<DPElement> children)
	{
		DPVBox element = new DPVBox( getVBoxParams() );
		element.setChildren( children );
		return element;
	}
	
	public DPVBox vbox(List<DPElement> children, int refPointIndex)
	{
		DPVBox element = new DPVBox( getVBoxParams() );
		element.setChildren( children );
		element.setRefPointIndex( refPointIndex );
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




	public DPImage image(BufferedImage image, double imageWidth, double imageHeight)
	{
		return new DPImage( getContentLeafStyleParams(), "", image, null, imageWidth, imageHeight );
	}
	
	public DPImage image(BufferedImage image, BufferedImage hoverImage, double imageWidth, double imageHeight)
	{
		return new DPImage( getContentLeafStyleParams(), "", image, hoverImage, imageWidth, imageHeight );
	}
	
	public DPImage image(BufferedImage image, BufferedImage hoverImage, double imageWidth)
	{
		return new DPImage( getContentLeafStyleParams(), "", image, hoverImage, imageWidth );
	}
	
	public DPImage image(BufferedImage image)
	{
		return new DPImage( getContentLeafStyleParams(), "", image, null );
	}
	
	public DPImage image(BufferedImage image, BufferedImage hoverImage)
	{
		return new DPImage( getContentLeafStyleParams(), "", image, hoverImage );
	}
	
	public DPImage image(File imageFile, double imageWidth, double imageHeight)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFile, null, imageWidth, imageHeight );
	}
	
	public DPImage image(File imageFile, double imageWidth)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFile, null, imageWidth );
	}
	
	public DPImage image(File imageFile, File hoverImageFile, double imageWidth, double imageHeight)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFile, hoverImageFile, imageWidth, imageHeight );
	}
	
	public DPImage image(File imageFile, File hoverImageFile, double imageWidth)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFile, hoverImageFile, imageWidth );
	}
	
	public DPImage image(File imageFile)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFile, null );
	}
	
	public DPImage image(File imageFile, File hoverImageFile)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFile, hoverImageFile );
	}
	
	public DPImage image(String imageFilename, double imageWidth, double imageHeight)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFilename, null, imageWidth, imageHeight );
	}
	
	public DPImage image(String imageFilename, double imageWidth)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFilename, null, imageWidth );
	}
	
	public DPImage image(String imageFilename, String hoverImageFilename, double imageWidth, double imageHeight)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFilename, hoverImageFilename, imageWidth, imageHeight );
	}
	
	public DPImage image(String imageFilename, String hoverImageFilename, double imageWidth)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFilename, hoverImageFilename, imageWidth );
	}
	
	public DPImage image(String imageFilename)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFilename, null );
	}
	
	public DPImage image(String imageFilename, String hoverImageFilename)
	{
		return new DPImage( getContentLeafStyleParams(), "", imageFilename, hoverImageFilename );
	}
	
	public DPImage systemIcon(String iconName)
	{
		return image( "icons/" + iconName + ".png" );
	}
	
	
	
	public DPBox box(double minWidth, double minHeight)
	{
		return new DPBox( getShapeParams(), "", minWidth, minHeight );
	}
	
	public DPSpacer spacer(double minWidth, double minHeight)
	{
		return new DPSpacer( minWidth, minHeight );
	}

	
	public DPShape rectangle(double x, double y, double w, double h)
	{
		return new DPShape( getShapeParams(), "", DPShape.rectangle( x, y, w, h ) );
	}
	
	public DPShape rectangle(Point2 pos, Vector2 size)
	{
		return new DPShape( getShapeParams(), "", DPShape.rectangle( pos, size ) );
	}
	
	public DPShape roundRectangle(double x, double y, double w, double h, double roundingX, double roundingY)
	{
		return new DPShape( getShapeParams(), "", DPShape.roundRectangle( x, y, w, h, roundingX, roundingY ) );
	}
	
	public DPShape roundRectangle(Point2 pos, Vector2 size, Vector2 rounding)
	{
		return new DPShape( getShapeParams(), "", DPShape.roundRectangle( pos, size, rounding ) );
	}
	
	public DPShape ellipse(double x, double y, double w, double h)
	{
		return new DPShape( getShapeParams(), "", DPShape.ellipse( x, y, w, h ) );
	}
	
	public DPShape ellipse(Point2 pos, Vector2 size)
	{
		return new DPShape( getShapeParams(), "", DPShape.ellipse( pos, size ) );
	}
	
	public DPShape shape(Shape shape)
	{
		return new DPShape( getShapeParams(), "", shape );
	}
	
	
	public DPViewport viewport(DPElement child, Range xRange, Range yRange, PersistentState state)
	{
		child = layoutWrap( child );
		DPViewport viewport = new DPViewport( getContainerParams(), xRange, yRange, state );
		viewport.setChild( child );
		return viewport;
	}

	public DPViewport viewport(DPElement child, PersistentState state)
	{
		child = layoutWrap( child );
		DPViewport viewport = new DPViewport( getContainerParams(), state );
		viewport.setChild( child );
		return viewport;
	}
}
