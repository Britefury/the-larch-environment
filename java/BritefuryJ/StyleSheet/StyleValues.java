//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.StyleSheet;

import java.util.HashMap;

import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.ApplyStyleSheetValues;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Primitive;

public class StyleValues extends AttributeTable
{
	protected StyleValues()
	{
		super();
	}
	
	
	protected StyleValues newInstance()
	{
		return new StyleValues();
	}
	
	
	public ApplyStyleSheetValues applyTo(Pres child)
	{
		return new ApplyStyleSheetValues( this, child );
	}




	public StyleValues withAttr(AttributeBase fieldName, Object value)
	{
		return (StyleValues)super.withAttr( fieldName, value );
	}
	
	public StyleValues withAttrs(HashMap<AttributeBase, Object> valuesMap)
	{
		return (StyleValues)super.withAttrs( valuesMap );
	}
		
	public StyleValues withAttrs(AttributeTable attribs)
	{
		return (StyleValues)super.withAttrs( attribs );
	}
		
	public StyleValues withAttrFrom(AttributeBase destAttr, AttributeTable srcTable, AttributeBase srcAttr)
	{
		return (StyleValues)super.withAttrFrom( destAttr, srcTable, srcAttr );
	}
	
	public StyleValues withAttrsFrom(AttributeTable srcTable, AttributeBase srcAttr)
	{
		return (StyleValues)super.withAttrsFrom( srcTable, srcAttr );
	}
	
	public StyleValues withoutAttr(AttributeBase fieldName)
	{
		return (StyleValues)super.withoutAttr( fieldName );
	}
	
	public StyleValues useAttr(AttributeBase fieldName)
	{
		return (StyleValues)super.useAttr( fieldName );
	}
	
	public StyleValues remapAttr(AttributeBase destAttribute, AttributeBase sourceAttribute)
	{
		return (StyleValues)super.remapAttr( destAttribute, sourceAttribute );
	}
	
	
	
	public StyleValues align(HAlignment hAlign, VAlignment vAlign)
	{
		return withAttr( Primitive.hAlign, hAlign ).withAttr( Primitive.vAlign, vAlign );
	}
	

	public StyleValues alignH(HAlignment hAlign)
	{
		return withAttr( Primitive.hAlign, hAlign );
	}
	
	public StyleValues alignV(VAlignment vAlign)
	{
		return withAttr( Primitive.vAlign, vAlign );
	}
	

	public StyleValues alignHPack()
	{
		return alignH( HAlignment.PACK );
	}

	public StyleValues alignHLeft()
	{
		return alignH( HAlignment.LEFT );
	}

	public StyleValues alignHCentre()
	{
		return alignH( HAlignment.CENTRE );
	}

	public StyleValues alignHRight()
	{
		return alignH( HAlignment.RIGHT );
	}

	public StyleValues alignHExpand()
	{
		return alignH( HAlignment.EXPAND );
	}
	
	
	public StyleValues alignVRefY()
	{
		return alignV( VAlignment.REFY );
	}

	public StyleValues alignVRefYExpand()
	{
		return alignV( VAlignment.REFY_EXPAND );
	}

	public StyleValues alignVTop()
	{
		return alignV( VAlignment.TOP );
	}

	public StyleValues alignVCentre()
	{
		return alignV( VAlignment.CENTRE );
	}

	public StyleValues alignVBottom()
	{
		return alignV( VAlignment.BOTTOM );
	}

	public StyleValues alignVExpand()
	{
		return alignV( VAlignment.EXPAND );
	}
	


	public static final StyleValues instance = new StyleValues();
	private static StyleValues rootStyleValues = instance;
	
	
	public static void setRootStyleSheet(StyleSheet s)
	{
		rootStyleValues = instance.withAttrs( s );
	}
	
	public static StyleValues getRootStyle()
	{
		return rootStyleValues;
	}
}
