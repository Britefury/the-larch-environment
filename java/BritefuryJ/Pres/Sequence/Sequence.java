//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Sequence;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;

public class Sequence
{
	public static final AttributeNamespace sequenceNamespace = new AttributeNamespace( "sequence" );
	
	
	public static final InheritedAttributeNonNull addLineBreaks = new InheritedAttributeNonNull( sequenceNamespace, "addLineBreaks", Boolean.class, true );
	public static final InheritedAttributeNonNull matchOuterIndentation = new InheritedAttributeNonNull( sequenceNamespace, "matchOuterIndentation", Boolean.class, true );
	public static final InheritedAttributeNonNull addLineBreakCost = new InheritedAttributeNonNull( sequenceNamespace, "addLineBreakCost", Boolean.class, true );
	public static final InheritedAttributeNonNull indentation = new InheritedAttributeNonNull( sequenceNamespace, "indentation", Double.class, 30.0 );
}
