//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;

import BritefuryJ.AttributeTable.Attribute;
import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.AttributeNonNull;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.AttributeTable.DerivedValueTable;
import BritefuryJ.AttributeTable.InheritedAttribute;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.TableBackgroundPainter;
import BritefuryJ.DocPresent.Border.AbstractBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleParams.ColumnStyleParams;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;
import BritefuryJ.DocPresent.StyleParams.FractionStyleParams;
import BritefuryJ.DocPresent.StyleParams.GridRowStyleParams;
import BritefuryJ.DocPresent.StyleParams.MathRootStyleParams;
import BritefuryJ.DocPresent.StyleParams.ParagraphStyleParams;
import BritefuryJ.DocPresent.StyleParams.RegionStyleParams;
import BritefuryJ.DocPresent.StyleParams.RowStyleParams;
import BritefuryJ.DocPresent.StyleParams.ScriptStyleParams;
import BritefuryJ.DocPresent.StyleParams.ShapeStyleParams;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Primitive
{
	public static final AttributeNamespace primitiveNamespace = new AttributeNamespace( "primitive" );
	
	
	public static final InheritedAttributeNonNull hAlign = new InheritedAttributeNonNull( primitiveNamespace, "hAlign", HAlignment.class, HAlignment.PACK );
	public static final InheritedAttributeNonNull vAlign = new InheritedAttributeNonNull( primitiveNamespace, "vAlign", VAlignment.class, VAlignment.REFY );

	public static final InheritedAttributeNonNull fontFace = new InheritedAttributeNonNull( primitiveNamespace, "fontFace", String.class, "Sans serif" );
	public static final InheritedAttributeNonNull fontBold = new InheritedAttributeNonNull( primitiveNamespace, "fontBold", Boolean.class, false );
	public static final InheritedAttributeNonNull fontItalic = new InheritedAttributeNonNull( primitiveNamespace, "fontItalic", Boolean.class, false );
	public static final InheritedAttributeNonNull fontUnderline = new InheritedAttributeNonNull( primitiveNamespace, "fontUnderline", Boolean.class, false );
	public static final InheritedAttributeNonNull fontStrikethrough = new InheritedAttributeNonNull( primitiveNamespace, "fontStrikethrough", Boolean.class, false );
	public static final InheritedAttributeNonNull fontSmallCaps = new InheritedAttributeNonNull( primitiveNamespace, "fontSmallCaps", Boolean.class, false );
	public static final InheritedAttributeNonNull fontSize = new InheritedAttributeNonNull( primitiveNamespace, "fontSize", Integer.class, 14 );
	public static final InheritedAttributeNonNull fontScale = new InheritedAttributeNonNull( primitiveNamespace, "fontScale", Double.class, 1.0 );
	public static final AttributeNonNull border = new AttributeNonNull( primitiveNamespace, "border", AbstractBorder.class, new SolidBorder( 1.0, 2.0, Color.black, null ) );
	public static final Attribute background = new Attribute( primitiveNamespace, "background", Painter.class, null );
	public static final Attribute hoverBackground = new Attribute( primitiveNamespace, "hoverBackground", Painter.class, null );
	public static final Attribute cursor = new Attribute( primitiveNamespace, "cursor", Cursor.class, null );
	public static final AttributeNonNull columnSpacing = new AttributeNonNull( primitiveNamespace, "columnSpacing", Double.class, 0.0 );
	public static final AttributeNonNull rowSpacing = new AttributeNonNull( primitiveNamespace, "rowSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull fractionVSpacing = new InheritedAttributeNonNull( primitiveNamespace, "fractionVSpacing", Double.class, 2.0 );
	public static final InheritedAttributeNonNull fractionHPadding = new InheritedAttributeNonNull( primitiveNamespace, "fractionHPadding", Double.class, 3.0 );
	public static final InheritedAttributeNonNull fractionRefYOffset = new InheritedAttributeNonNull( primitiveNamespace, "fractionRefYOffset", Double.class, 5.0 );
	public static final InheritedAttributeNonNull fractionFontScale = new InheritedAttributeNonNull( primitiveNamespace, "fractionFontScale", Double.class, 0.9 );
	public static final InheritedAttributeNonNull fractionMinFontScale = new InheritedAttributeNonNull( primitiveNamespace, "fractionMinFontScale", Double.class, 0.85 );
	public static final InheritedAttributeNonNull editable = new InheritedAttributeNonNull( primitiveNamespace, "editable", Boolean.class, true );
	public static final InheritedAttributeNonNull selectable = new InheritedAttributeNonNull( primitiveNamespace, "selectable", Boolean.class, true );
	public static final InheritedAttributeNonNull foreground = new InheritedAttributeNonNull( primitiveNamespace, "foreground", Paint.class, Color.black );
	public static final InheritedAttribute hoverForeground = new InheritedAttribute( primitiveNamespace, "hoverForeground", Paint.class, null );
	public static final InheritedAttributeNonNull mathRootThickness = new InheritedAttributeNonNull( primitiveNamespace, "mathRootThickness", Double.class, 1.5 );
	public static final InheritedAttributeNonNull paragraphSpacing = new InheritedAttributeNonNull( primitiveNamespace, "paragraphSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull paragraphLineSpacing = new InheritedAttributeNonNull( primitiveNamespace, "paragraphLineSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull paragraphIndentation = new InheritedAttributeNonNull( primitiveNamespace, "paragraphIndentation", Double.class, 0.0 );
	public static final InheritedAttribute shapePainter = new InheritedAttribute( primitiveNamespace, "shapePainter", Painter.class, new FillPainter( Color.black ) );
	public static final InheritedAttribute hoverShapePainter = new InheritedAttribute( primitiveNamespace, "hoverShapePainter", Painter.class, null );
	public static final InheritedAttributeNonNull scriptColumnSpacing = new InheritedAttributeNonNull( primitiveNamespace, "scriptColumnSpacing", Double.class, 1.0 );
	public static final InheritedAttributeNonNull scriptRowSpacing = new InheritedAttributeNonNull( primitiveNamespace, "scriptRowSpacing", Double.class, 1.0 );
	public static final InheritedAttributeNonNull scriptFontScale = new InheritedAttributeNonNull( primitiveNamespace, "scriptFontScale", Double.class, 0.75 );
	public static final InheritedAttributeNonNull scriptMinFontScale = new InheritedAttributeNonNull( primitiveNamespace, "scriptMinFontScale", Double.class, 0.7 );
	public static final AttributeNonNull tableColumnSpacing = new AttributeNonNull( primitiveNamespace, "tableColumnSpacing", Double.class, 3.0 );
	public static final AttributeNonNull tableColumnExpand = new AttributeNonNull( primitiveNamespace, "tableColumnExpand", Boolean.class, false );
	public static final AttributeNonNull tableRowSpacing = new AttributeNonNull( primitiveNamespace, "tableRowSpacing", Double.class, 3.0 );
	public static final AttributeNonNull tableRowExpand = new AttributeNonNull( primitiveNamespace, "tableRowExpand", Boolean.class, false );
	public static final Attribute tableBackgroundPainter = new Attribute( primitiveNamespace, "tableBackgroundPainter", TableBackgroundPainter.class, null );
	public static final AttributeNonNull tableCellBoundaryWidth = new AttributeNonNull( primitiveNamespace, "tableCellBoundaryWidth", Double.class, 1.0 );
	public static final Attribute tableCellBoundaryPaint = new Attribute( primitiveNamespace, "tableCellBoundaryPaint", Paint.class, null );
	public static final Attribute tableBorder = new Attribute( primitiveNamespace, "tableBorder", AbstractBorder.class, null );
	public static final InheritedAttribute textSquiggleUnderlinePaint = new InheritedAttribute( primitiveNamespace, "textSquiggleUnderlinePaint", Paint.class, null );



	protected static DerivedValueTable<Font> font = new DerivedValueTable<Font>( primitiveNamespace )
	{
		protected Font evaluate(AttributeTable attribs)
		{
			String face = attribs.get( fontFace, String.class );
			boolean bBold = attribs.get( fontBold, Boolean.class );
			boolean bItalic = attribs.get( fontItalic, Boolean.class );
			int size = attribs.get( fontSize, Integer.class );
			double scale = attribs.get( fontScale, Double.class );
			int flags = ( bBold ? Font.BOLD : 0 )  |  ( bItalic ? Font.ITALIC : 0 );
			return new Font( face, flags, size ).deriveFont( (float)( size * scale ) );
		}
	};
	
	protected static DerivedValueTable<StyleValues> useFont = new DerivedValueTable<StyleValues>( primitiveNamespace )
	{
		protected StyleValues evaluate(AttributeTable style)
		{
			return (StyleValues)style.useAttr( fontFace ).useAttr( fontBold ).useAttr( fontItalic ).useAttr( fontSize ).useAttr( fontScale );
		}
	};

	
	
	protected static AbstractBorder getBorderParams(StyleValues style)
	{
		return style.get( border, AbstractBorder.class );
	}
	
	protected static DerivedValueTable<StyleValues> useBorderParams = new DerivedValueTable<StyleValues>( primitiveNamespace )
	{
		protected StyleValues evaluate(AttributeTable style)
		{
			return (StyleValues)style.useAttr( border );
		}
	};

	
	
	protected static DerivedValueTable<ContainerStyleParams> containerParams = new DerivedValueTable<ContainerStyleParams>( primitiveNamespace )
	{
		protected ContainerStyleParams evaluate(AttributeTable attribs)
		{
			return new ContainerStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ) );
		}
	};
	
	public static ContainerStyleParams getContainerStyleParams(StyleValues values)
	{
		return containerParams.get( values );
	}
	
	protected static DerivedValueTable<StyleValues> useContainerParams = new DerivedValueTable<StyleValues>( primitiveNamespace )
	{
		protected StyleValues evaluate(AttributeTable style)
		{
			return (StyleValues)style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor );
		}
	};

	
	
	protected static DerivedValueTable<ContentLeafStyleParams> contentLeafParams = new DerivedValueTable<ContentLeafStyleParams>( primitiveNamespace )
	{
		protected ContentLeafStyleParams evaluate(AttributeTable attribs)
		{
			return new ContentLeafStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ) );
		}
	};

	protected static StyleValues useContentLeafParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor );
	}

	
	
	protected static DerivedValueTable<FractionStyleParams> fractionParams = new DerivedValueTable<FractionStyleParams>( primitiveNamespace )
	{
		protected FractionStyleParams evaluate(AttributeTable attribs)
		{
			return new FractionStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( fractionVSpacing, Double.class ),
					attribs.get( fractionHPadding, Double.class ),
					attribs.get( fractionRefYOffset, Double.class ),
					fractionBarParams.get( attribs ) );
		}
	};
	
	protected static StyleValues useFractionParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( fractionVSpacing ).useAttr( fractionHPadding ).useAttr( fractionRefYOffset );
	}
	
	
	
	protected static DerivedValueTable<FractionStyleParams.BarStyleParams> fractionBarParams = new DerivedValueTable<FractionStyleParams.BarStyleParams>( primitiveNamespace )
	{
		protected FractionStyleParams.BarStyleParams evaluate(AttributeTable attribs)
		{
			return new FractionStyleParams.BarStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( editable, Boolean.class ),
					attribs.get( selectable, Boolean.class ),
					attribs.get( foreground, Paint.class ),
					attribs.get( hoverForeground, Paint.class ) );
		}
	};
	
	protected static StyleValues useFractionBarParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( editable ).useAttr( foreground ).useAttr( hoverForeground );
	}
	
	
	
	protected static DerivedValueTable<GridRowStyleParams> gridRowParams = new DerivedValueTable<GridRowStyleParams>( primitiveNamespace )
	{
		protected GridRowStyleParams evaluate(AttributeTable attribs)
		{
			return new GridRowStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ) );
		}
	};
	
	protected static StyleValues useGridRowParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor );
	}
	
	
	
	protected static DerivedValueTable<RowStyleParams> rowParams = new DerivedValueTable<RowStyleParams>( primitiveNamespace )
	{
		protected RowStyleParams evaluate(AttributeTable attribs)
		{
			return new RowStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( rowSpacing, Double.class ) );
		}
	};
	
	protected static StyleValues useRowParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor ).useAttr( rowSpacing );
	}

	
	
	protected static DerivedValueTable<MathRootStyleParams> mathRootParams = new DerivedValueTable<MathRootStyleParams>( primitiveNamespace )
	{
		protected MathRootStyleParams evaluate(AttributeTable attribs)
		{
			return new MathRootStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					font.get( attribs ),
					attribs.get( foreground, Paint.class ),
					attribs.get( hoverForeground, Paint.class ),
					attribs.get( mathRootThickness, Double.class ) );
		}
	};
	
	protected static StyleValues useMathRootParams(StyleValues style)
	{
		return useFont.get( style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( foreground ).useAttr( hoverForeground ).useAttr( mathRootThickness ) );
	}
	
	
	
	protected static DerivedValueTable<ContainerStyleParams> overlayParams = new DerivedValueTable<ContainerStyleParams>( primitiveNamespace )
	{
		protected ContainerStyleParams evaluate(AttributeTable attribs)
		{
			return new ContainerStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ) );
		}
	};
	
	protected static StyleValues useOverlayParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor );
	}

	
	
	protected static DerivedValueTable<ParagraphStyleParams> paragraphParams = new DerivedValueTable<ParagraphStyleParams>( primitiveNamespace )
	{
		protected ParagraphStyleParams evaluate(AttributeTable attribs)
		{
			return new ParagraphStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( paragraphSpacing, Double.class ),
					attribs.get( paragraphLineSpacing, Double.class ),
					attribs.get( paragraphIndentation, Double.class ) );
		}
	};
	
	protected static StyleValues useParagraphParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( paragraphSpacing ).useAttr( paragraphLineSpacing ).useAttr( paragraphIndentation );
	}

	
	
	protected static DerivedValueTable<RegionStyleParams> regionParams = new DerivedValueTable<RegionStyleParams>( primitiveNamespace )
	{
		protected RegionStyleParams evaluate(AttributeTable attribs)
		{
			return new RegionStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( editable, Boolean.class ),
					attribs.get( selectable, Boolean.class ));
		}
	};
	
	protected static DerivedValueTable<StyleValues> useRegionParams = new DerivedValueTable<StyleValues>( primitiveNamespace )
	{
		protected StyleValues evaluate(AttributeTable style)
		{
			return (StyleValues)style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor ).useAttr( editable ).useAttr( selectable );
		}
	};

	
	
	protected static DerivedValueTable<ShapeStyleParams> shapeParams = new DerivedValueTable<ShapeStyleParams>( primitiveNamespace )
	{
		protected ShapeStyleParams evaluate(AttributeTable attribs)
		{
			return new ShapeStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( shapePainter, Painter.class ),
					attribs.get( hoverShapePainter, Painter.class ) );
		}
	};
	
	protected static StyleValues useShapeParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor ).useAttr( shapePainter ).useAttr( hoverShapePainter );
	}
	
	
	
	protected static DerivedValueTable<ScriptStyleParams> scriptParams = new DerivedValueTable<ScriptStyleParams>( primitiveNamespace )
	{
		protected ScriptStyleParams evaluate(AttributeTable attribs)
		{
			return new ScriptStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( scriptColumnSpacing, Double.class ),
					attribs.get( scriptRowSpacing, Double.class ) );
		}
	};
	
	protected static StyleValues useScriptParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor ).useAttr( scriptColumnSpacing ).useAttr( scriptRowSpacing );
	}

	
	
	protected static DerivedValueTable<TextStyleParams> staticTextParams = new DerivedValueTable<TextStyleParams>( primitiveNamespace )
	{
		protected TextStyleParams evaluate(AttributeTable attribs)
		{
			return new TextStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					false,
					attribs.get( selectable, Boolean.class ),
					font.get( attribs ),
					attribs.get( foreground, Paint.class ),
					attribs.get( hoverForeground, Paint.class ),
					attribs.get( textSquiggleUnderlinePaint, Paint.class ),
					attribs.get( fontUnderline, Boolean.class ),
					attribs.get( fontStrikethrough, Boolean.class ),
					attribs.get( fontSmallCaps, Boolean.class ) );
		}
	};
	
	protected static DerivedValueTable<StyleValues> useStaticTextParams = new DerivedValueTable<StyleValues>( primitiveNamespace )
	{
		protected StyleValues evaluate(AttributeTable style)
		{
			return useFont.get( style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor ).useAttr( selectable )
					.useAttr( foreground ).useAttr( hoverForeground ).useAttr( textSquiggleUnderlinePaint ).useAttr( fontUnderline ).useAttr( fontStrikethrough ).useAttr( fontSmallCaps ) );
		}
	};

	
	
	protected static DerivedValueTable<TextStyleParams> labelTextParams = new DerivedValueTable<TextStyleParams>( primitiveNamespace )
	{
		protected TextStyleParams evaluate(AttributeTable attribs)
		{
			return new TextStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					false,
					false,
					font.get( attribs ),
					attribs.get( foreground, Paint.class ),
					attribs.get( hoverForeground, Paint.class ),
					attribs.get( textSquiggleUnderlinePaint, Paint.class ),
					attribs.get( fontUnderline, Boolean.class ),
					attribs.get( fontStrikethrough, Boolean.class ),
					attribs.get( fontSmallCaps, Boolean.class ) );
		}
	};
	
	protected static StyleValues useLabelTextParams(StyleValues style)
	{
		return useFont.get( style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( foreground ).useAttr( hoverForeground ).useAttr( textSquiggleUnderlinePaint ).useAttr( fontUnderline ).useAttr( fontStrikethrough ).useAttr( fontSmallCaps ) );
	}
	
	
	
	protected static DerivedValueTable<TextStyleParams> textParams = new DerivedValueTable<TextStyleParams>( primitiveNamespace )
	{
		protected TextStyleParams evaluate(AttributeTable attribs)
		{
			return new TextStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( editable, Boolean.class ),
					attribs.get( selectable, Boolean.class ),
					font.get( attribs ),
					attribs.get( foreground, Paint.class ),
					attribs.get( hoverForeground, Paint.class ),
					attribs.get( textSquiggleUnderlinePaint, Paint.class ),
					attribs.get( fontUnderline, Boolean.class ),
					attribs.get( fontStrikethrough, Boolean.class ),
					attribs.get( fontSmallCaps, Boolean.class ) );
		}
	};
	
	protected static StyleValues useTextParams(StyleValues style)
	{
		return useFont.get( style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( editable ).useAttr( selectable ).useAttr( foreground ).useAttr( hoverForeground ).useAttr( textSquiggleUnderlinePaint )
				.useAttr( fontUnderline ).useAttr( fontStrikethrough ).useAttr( fontSmallCaps ) );
	}
	
	
	
	protected static DerivedValueTable<TableStyleParams> tableParams = new DerivedValueTable<TableStyleParams>( primitiveNamespace )
	{
		protected TableStyleParams evaluate(AttributeTable attribs)
		{
			return new TableStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( tableColumnSpacing, Double.class ),
					attribs.get( tableColumnExpand, Boolean.class ),
					attribs.get( tableRowSpacing, Double.class ),
					attribs.get( tableRowExpand, Boolean.class ),
					attribs.get( tableBackgroundPainter, TableBackgroundPainter.class ),
					new BasicStroke( (float)(double)attribs.get( tableCellBoundaryWidth, Double.class ) ),
					attribs.get( tableCellBoundaryPaint, Paint.class ) );
		}
	};
	
	protected static StyleValues useTableParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( tableColumnSpacing ).useAttr( tableColumnExpand ).useAttr( tableRowSpacing ).useAttr( tableRowExpand )
				.useAttr( tableCellBoundaryWidth ).useAttr( tableCellBoundaryPaint ).useAttr( tableBorder );
	}
	
	
	
	protected static DerivedValueTable<ColumnStyleParams> columnParams = new DerivedValueTable<ColumnStyleParams>( primitiveNamespace )
	{
		protected ColumnStyleParams evaluate(AttributeTable attribs)
		{
			return new ColumnStyleParams(
					attribs.get( hAlign, HAlignment.class ),
					attribs.get( vAlign, VAlignment.class ),
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( columnSpacing, Double.class ) );
		}
	};
	
	protected static StyleValues useColumnParams(StyleValues style)
	{
		return style.useAttr( hAlign ).useAttr( vAlign ).useAttr( background ).useAttr( hoverBackground ).useAttr( cursor ).useAttr( columnSpacing );
	}
	
	
	
	public static boolean isEditable(StyleValues style)
	{
		return style.get( editable, Boolean.class );
	}
	
	
	
	
	
	public static StyleSheet align(HAlignment h, VAlignment v)
	{
		return StyleSheet.style( hAlign.as( h ), vAlign.as( v ) );
	}
	
	public static StyleSheet alignH(HAlignment h)
	{
		return StyleSheet.style( hAlign.as( h ) );
	}
	
	public static StyleSheet alignV(VAlignment v)
	{
		return StyleSheet.style( vAlign.as( v ) );
	}


	public static final StyleSheet alignHPack = StyleSheet.style( hAlign.as( HAlignment.PACK ) );
	public static final StyleSheet alignHLeft = StyleSheet.style( hAlign.as( HAlignment.LEFT ) );
	public static final StyleSheet alignHCentre = StyleSheet.style( hAlign.as( HAlignment.CENTRE ) );
	public static final StyleSheet alignHRight = StyleSheet.style( hAlign.as( HAlignment.RIGHT ) );
	public static final StyleSheet alignHExpand = StyleSheet.style( hAlign.as( HAlignment.EXPAND ) );

	public static final StyleSheet alignVRefY = StyleSheet.style( vAlign.as( VAlignment.REFY ) );
	public static final StyleSheet alignVRefYExpand = StyleSheet.style( vAlign.as( VAlignment.REFY_EXPAND ) );
	public static final StyleSheet alignVTop = StyleSheet.style( vAlign.as( VAlignment.TOP ) );
	public static final StyleSheet alignVCentre = StyleSheet.style( vAlign.as( VAlignment.CENTRE ) );
	public static final StyleSheet alignVBottom = StyleSheet.style( vAlign.as( VAlignment.BOTTOM ) );
	public static final StyleSheet alignVExpand = StyleSheet.style( vAlign.as( VAlignment.EXPAND ) );
}
