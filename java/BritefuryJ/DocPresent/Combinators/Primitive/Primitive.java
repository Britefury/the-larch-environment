//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;

import BritefuryJ.AttributeTable.Attribute;
import BritefuryJ.AttributeTable.AttributeTable2;
import BritefuryJ.AttributeTable.DerivedValueTable;
import BritefuryJ.AttributeTable.InheritedAttribute;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.Border.AbstractBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.Painter;
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
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Primitive
{
	public static final InheritedAttributeNonNull fontFace = new InheritedAttributeNonNull( "primitive", "fontFace", String.class, "Sans serif" );
	public static final InheritedAttributeNonNull fontBold = new InheritedAttributeNonNull( "primitive", "fontBold", Boolean.class, false );
	public static final InheritedAttributeNonNull fontItalic = new InheritedAttributeNonNull( "primitive", "fontItalic", Boolean.class, false );
	public static final InheritedAttributeNonNull fontSize = new InheritedAttributeNonNull( "primitive", "fontSize", Integer.class, 14 );
	public static final InheritedAttributeNonNull fontScale = new InheritedAttributeNonNull( "primitive", "fontScale", Double.class, 1.0 );
	public static final InheritedAttributeNonNull border = new InheritedAttributeNonNull( "primitive", "border", AbstractBorder.class, new SolidBorder( 1.0, 2.0, Color.black, null ) );
	public static final Attribute background = new Attribute( "primitive", "background", Painter.class, null );
	public static final Attribute hoverBackground = new Attribute( "primitive", "hoverBackground", Painter.class, null );
	public static final InheritedAttribute cursor = new InheritedAttribute( "primitive", "cursor", Cursor.class, null );
	public static final InheritedAttributeNonNull fractionVSpacing = new InheritedAttributeNonNull( "primitive", "fractionVSpacing", Double.class, 2.0 );
	public static final InheritedAttributeNonNull fractionHPadding = new InheritedAttributeNonNull( "primitive", "fractionHPadding", Double.class, 3.0 );
	public static final InheritedAttributeNonNull fractionRefYOffset = new InheritedAttributeNonNull( "primitive", "fractionRefYOffset", Double.class, 5.0 );
	public static final InheritedAttributeNonNull fractionFontScale = new InheritedAttributeNonNull( "primitive", "fractionFontScale", Double.class, 0.9 );
	public static final InheritedAttributeNonNull fractionMinFontScale = new InheritedAttributeNonNull( "primitive", "fractionMinFontScale", Double.class, 0.9 );
	public static final InheritedAttributeNonNull editable = new InheritedAttributeNonNull( "primitive", "editable", Boolean.class, true );
	public static final InheritedAttributeNonNull foreground = new InheritedAttributeNonNull( "primitive", "foreground", Paint.class, Color.black );
	public static final InheritedAttribute hoverForeground = new InheritedAttribute( "primitive", "hoverForeground", Paint.class, null );
	public static final InheritedAttributeNonNull hboxSpacing = new InheritedAttributeNonNull( "primitive", "hboxSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull mathRootThickness = new InheritedAttributeNonNull( "primitive", "mathRootThickness", Double.class, 1.5 );
	public static final InheritedAttributeNonNull paragraphSpacing = new InheritedAttributeNonNull( "primitive", "paragraphSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull paragraphLineSpacing = new InheritedAttributeNonNull( "primitive", "paragraphLineSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull paragraphIndentation = new InheritedAttributeNonNull( "primitive", "paragraphIndentation", Double.class, 0.0 );
	public static final InheritedAttributeNonNull shapePainter = new InheritedAttributeNonNull( "primitive", "shapePainter", Painter.class, new FillPainter( Color.black ) );
	public static final InheritedAttribute hoverShapePainter = new InheritedAttribute( "primitive", "hoverShapePainter", Painter.class, null );
	public static final InheritedAttributeNonNull scriptColumnSpacing = new InheritedAttributeNonNull( "primitive", "scriptColumnSpacing", Double.class, 1.0 );
	public static final InheritedAttributeNonNull scriptRowSpacing = new InheritedAttributeNonNull( "primitive", "scriptRowSpacing", Double.class, 1.0 );
	public static final InheritedAttributeNonNull scriptFontScale = new InheritedAttributeNonNull( "primitive", "scriptFontScale", Double.class, 0.9 );
	public static final InheritedAttributeNonNull scriptMinFontScale = new InheritedAttributeNonNull( "primitive", "scriptMinFontScale", Double.class, 0.9 );
	public static final InheritedAttributeNonNull tableColumnSpacing = new InheritedAttributeNonNull( "primitive", "tableColumnSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull tableColumnExpand = new InheritedAttributeNonNull( "primitive", "tableColumnExpand", Boolean.class, false );
	public static final InheritedAttributeNonNull tableRowSpacing = new InheritedAttributeNonNull( "primitive", "tableRowSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull tableRowExpand = new InheritedAttributeNonNull( "primitive", "tableRowExpand", Boolean.class, false );
	public static final InheritedAttribute textSquiggleUnderlinePaint = new InheritedAttribute( "primitive", "textSquiggleUnderlinePaint", Paint.class, null );
	public static final InheritedAttributeNonNull textSmallCaps = new InheritedAttributeNonNull( "primitive", "textSmallCaps", Boolean.class, false );
	public static final InheritedAttributeNonNull vboxSpacing = new InheritedAttributeNonNull( "primitive", "vboxSpacing", Double.class, 0.0 );



	protected static DerivedValueTable<Font> font = new DerivedValueTable<Font>()
	{
		protected Font evaluate(AttributeTable2 attribs)
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
	
	protected static DerivedValueTable<StyleValues> useFont = new DerivedValueTable<StyleValues>()
	{
		protected StyleValues evaluate(AttributeTable2 style)
		{
			return (StyleValues)style.useAttr( fontFace ).useAttr( fontBold ).useAttr( fontItalic ).useAttr( fontSize ).useAttr( fontScale );
		}
	};

	
	
	protected static AbstractBorder getBorderParams(StyleValues style)
	{
		return style.get( border, AbstractBorder.class );
	}
	
	protected static DerivedValueTable<StyleValues> useBorderParams = new DerivedValueTable<StyleValues>()
	{
		protected StyleValues evaluate(AttributeTable2 style)
		{
			return (StyleValues)style.useAttr( border );
		}
	};

	
	
	protected static DerivedValueTable<ContainerStyleParams> containerParams = new DerivedValueTable<ContainerStyleParams>()
	{
		protected ContainerStyleParams evaluate(AttributeTable2 attribs)
		{
			return new ContainerStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ) );
		}
	};
	
	protected static DerivedValueTable<StyleValues> useContainerParams = new DerivedValueTable<StyleValues>()
	{
		protected StyleValues evaluate(AttributeTable2 style)
		{
			return (StyleValues)style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor );
		}
	};

	
	
	protected static DerivedValueTable<ContentLeafStyleParams> contentLeafParams = new DerivedValueTable<ContentLeafStyleParams>()
	{
		protected ContentLeafStyleParams evaluate(AttributeTable2 attribs)
		{
			return new ContentLeafStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ) );
		}
	};

	protected static StyleValues useContentLeafParams(StyleValues style)
	{
		return style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor );
	}

	
	
	protected static DerivedValueTable<FractionStyleParams> fractionParams = new DerivedValueTable<FractionStyleParams>()
	{
		protected FractionStyleParams evaluate(AttributeTable2 attribs)
		{
			return new FractionStyleParams(
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
		return style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( fractionVSpacing ).useAttr( fractionHPadding ).useAttr( fractionRefYOffset );
	}
	
	
	
	protected static DerivedValueTable<FractionStyleParams.BarStyleParams> fractionBarParams = new DerivedValueTable<FractionStyleParams.BarStyleParams>()
	{
		protected FractionStyleParams.BarStyleParams evaluate(AttributeTable2 attribs)
		{
			return new FractionStyleParams.BarStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( editable, Boolean.class ),
					attribs.get( foreground, Paint.class ),
					attribs.get( hoverForeground, Paint.class ) );
		}
	};
	
	protected static StyleValues useFractionBarParams(StyleValues style)
	{
		return style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( editable ).useAttr( foreground ).useAttr( hoverForeground );
	}
	
	
	
	protected static DerivedValueTable<GridRowStyleParams> gridRowParams = new DerivedValueTable<GridRowStyleParams>()
	{
		protected GridRowStyleParams evaluate(AttributeTable2 attribs)
		{
			return new GridRowStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ) );
		}
	};
	
	protected static StyleValues useGridRowParams(StyleValues style)
	{
		return style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor );
	}
	
	
	
	protected static DerivedValueTable<HBoxStyleParams> hboxParams = new DerivedValueTable<HBoxStyleParams>()
	{
		protected HBoxStyleParams evaluate(AttributeTable2 attribs)
		{
			return new HBoxStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( hboxSpacing, Double.class ) );
		}
	};
	
	protected static StyleValues useHBoxParams(StyleValues style)
	{
		return style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor ).useAttr( hboxSpacing );
	}

	
	
	protected static DerivedValueTable<MathRootStyleParams> mathRootParams = new DerivedValueTable<MathRootStyleParams>()
	{
		protected MathRootStyleParams evaluate(AttributeTable2 attribs)
		{
			return new MathRootStyleParams(
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
		return useFont.get( style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( foreground ).useAttr( hoverForeground ).useAttr( mathRootThickness ) );
	}
	
	
	
	protected static DerivedValueTable<ParagraphStyleParams> paragraphParams = new DerivedValueTable<ParagraphStyleParams>()
	{
		protected ParagraphStyleParams evaluate(AttributeTable2 attribs)
		{
			return new ParagraphStyleParams(
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
		return style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( paragraphSpacing ).useAttr( paragraphLineSpacing ).useAttr( paragraphIndentation );
	}

	
	
	protected static DerivedValueTable<ShapeStyleParams> shapeParams = new DerivedValueTable<ShapeStyleParams>()
	{
		protected ShapeStyleParams evaluate(AttributeTable2 attribs)
		{
			return new ShapeStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( shapePainter, Painter.class ),
					attribs.get( hoverShapePainter, Painter.class ) );
		}
	};
	
	protected static StyleValues useShapeParams(StyleValues style)
	{
		return style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor ).useAttr( shapePainter ).useAttr( hoverShapePainter );
	}
	
	
	
	protected static DerivedValueTable<ScriptStyleParams> scriptParams = new DerivedValueTable<ScriptStyleParams>()
	{
		protected ScriptStyleParams evaluate(AttributeTable2 attribs)
		{
			return new ScriptStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( scriptColumnSpacing, Double.class ),
					attribs.get( scriptRowSpacing, Double.class ) );
		}
	};
	
	protected static StyleValues useScriptParams(StyleValues style)
	{
		return style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor ).useAttr( scriptColumnSpacing ).useAttr( scriptRowSpacing );
	}

	
	
	protected static DerivedValueTable<TextStyleParams> staticTextParams = new DerivedValueTable<TextStyleParams>()
	{
		protected TextStyleParams evaluate(AttributeTable2 attribs)
		{
			return new TextStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					false,
					font.get( attribs ),
					attribs.get( foreground, Paint.class ),
					attribs.get( hoverForeground, Paint.class ),
					attribs.get( textSquiggleUnderlinePaint, Paint.class ),
					attribs.get( textSmallCaps, Boolean.class ) );
		}
	};
	
	protected static DerivedValueTable<StyleValues> useStaticTextParams = new DerivedValueTable<StyleValues>()
	{
		protected StyleValues evaluate(AttributeTable2 style)
		{
			return useFont.get( style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
					.useAttr( foreground ).useAttr( hoverForeground ).useAttr( textSquiggleUnderlinePaint ).useAttr( textSmallCaps ) );
		}
	};

	
	
	protected static DerivedValueTable<TextStyleParams> labelTextParams = new DerivedValueTable<TextStyleParams>()
	{
		protected TextStyleParams evaluate(AttributeTable2 attribs)
		{
			return new TextStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					false,
					font.get( attribs ),
					attribs.get( foreground, Paint.class ),
					attribs.get( hoverForeground, Paint.class ),
					attribs.get( textSquiggleUnderlinePaint, Paint.class ),
					attribs.get( textSmallCaps, Boolean.class ) );
		}
	};
	
	protected static StyleValues useLabelTextParams(StyleValues style)
	{
		return useFont.get( style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( foreground ).useAttr( hoverForeground ).useAttr( textSquiggleUnderlinePaint ).useAttr( textSmallCaps ) );
	}
	
	
	
	protected static DerivedValueTable<TextStyleParams> textParams = new DerivedValueTable<TextStyleParams>()
	{
		protected TextStyleParams evaluate(AttributeTable2 attribs)
		{
			return new TextStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( editable, Boolean.class ),
					font.get( attribs ),
					attribs.get( foreground, Paint.class ),
					attribs.get( hoverForeground, Paint.class ),
					attribs.get( textSquiggleUnderlinePaint, Paint.class ),
					attribs.get( textSmallCaps, Boolean.class ) );
		}
	};
	
	protected static StyleValues useTextParams(StyleValues style)
	{
		return useFont.get( style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( editable ).useAttr( foreground ).useAttr( hoverForeground ).useAttr( textSquiggleUnderlinePaint )
				.useAttr( textSmallCaps ) );
	}
	
	
	
	protected static DerivedValueTable<TableStyleParams> tableParams = new DerivedValueTable<TableStyleParams>()
	{
		protected TableStyleParams evaluate(AttributeTable2 attribs)
		{
			return new TableStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( tableColumnSpacing, Double.class ),
					attribs.get( tableColumnExpand, Boolean.class ),
					attribs.get( tableRowSpacing, Double.class ),
					attribs.get( tableRowExpand, Boolean.class ) );
		}
	};
	
	protected static StyleValues useTableParams(StyleValues style)
	{
		return style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor )
				.useAttr( tableColumnSpacing ).useAttr( tableColumnExpand ).useAttr( tableRowSpacing ).useAttr( tableRowExpand );
	}
	
	
	
	protected static DerivedValueTable<VBoxStyleParams> vboxParams = new DerivedValueTable<VBoxStyleParams>()
	{
		protected VBoxStyleParams evaluate(AttributeTable2 attribs)
		{
			return new VBoxStyleParams(
					attribs.get( background, Painter.class ),
					attribs.get( hoverBackground, Painter.class ),
					attribs.get( cursor, Cursor.class ),
					attribs.get( vboxSpacing, Double.class ) );
		}
	};
	
	protected static StyleValues useVBoxParams(StyleValues style)
	{
		return style.useAttr( background ).useAttr( hoverBackground ).useAttr( cursor ).useAttr( vboxSpacing );
	}
	
	
	
	public static boolean isEditable(StyleValues style)
	{
		return style.get( editable, Boolean.class );
	}
}
