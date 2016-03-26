//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Formula;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.AttributeTable.DerivedValueTable;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class Formula
{
	public enum FormulaStyle
	{
		STANDARD,
		INLINE
	}
	
	
	
	public static final AttributeNamespace formulaNamespace = new AttributeNamespace( "formula" );
	
	
	public static final InheritedAttributeNonNull formulaStyle = new InheritedAttributeNonNull( formulaNamespace, "formulaStyle", FormulaStyle.class, FormulaStyle.STANDARD );
	public static final InheritedAttributeNonNull reductionSymbolScale_standard = new InheritedAttributeNonNull( formulaNamespace, "reductionSymbolScale_standard", double.class, 2.0 );
	public static final InheritedAttributeNonNull reductionSymbolScale_inline = new InheritedAttributeNonNull( formulaNamespace, "reductionSymbolScale_inline", double.class, 1.25 );
	public static final InheritedAttributeNonNull smallScale = new InheritedAttributeNonNull( formulaNamespace, "smallScale", double.class, 0.9 );




	protected static DerivedValueTable<StyleSheet> reductionSymbolStyle = new DerivedValueTable<StyleSheet>( formulaNamespace )
	{
		protected StyleSheet evaluate(AttributeTable attribs)
		{
			FormulaStyle st = attribs.get( formulaStyle, FormulaStyle.class );
			double scale = 1.0;
			
			if ( st == FormulaStyle.STANDARD )
			{
				scale = attribs.get( reductionSymbolScale_standard, double.class );
			}
			else if ( st == FormulaStyle.INLINE )
			{
				scale = attribs.get( reductionSymbolScale_inline, double.class );
			}
			else
			{
				throw new RuntimeException( "Unknown formula style" );
			}
			
			return StyleSheet.style( Primitive.fontScale.as( scale ) );
		}
	};

	protected static DerivedValueTable<StyleSheet> smallStyle = new DerivedValueTable<StyleSheet>( formulaNamespace )
	{
		protected StyleSheet evaluate(AttributeTable attribs)
		{
			return StyleSheet.style( Primitive.fontScale.as( attribs.get( smallScale, double.class ) ) );
		}
	};
}
