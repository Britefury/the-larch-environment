//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSMathRoot;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class MathRoot extends Pres
{
	private Pres child;
	
	
	public MathRoot(Object child)
	{
		this.child = coerce( child );
	}
	

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues childStyle = Primitive.useMathRootParams( style );
		LSElement childElement = child.present( ctx, Primitive.useMathRootParams( style ) ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ),
				childStyle.get( Primitive.vAlign, VAlignment.class ) );
		return new LSMathRoot( Primitive.mathRootParams.get( style ), childElement );
	}
}
