//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class CustomPres extends Pres
{
	public static interface PresentationFn
	{
		public LSElement present(PresentationContext ctx);
	}
	
	
	private PresentationFn presFn;
	
	
	public CustomPres(PresentationFn presFn)
	{
		super();
		this.presFn = presFn;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return presFn.present( ctx );
	}
}
