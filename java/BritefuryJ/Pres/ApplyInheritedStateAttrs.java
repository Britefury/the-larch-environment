//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

import java.util.HashMap;
import java.util.HashSet;

public class ApplyInheritedStateAttrs extends Pres {
	private HashMap<String, Object> changedValues;
	private HashSet<String> removedValues;
	private Pres child;


	public ApplyInheritedStateAttrs(HashMap<String, Object> changedValues, HashSet<String> removedValues, Object child)
	{
		this.changedValues = changedValues;
		this.removedValues = removedValues;
		this.child = coerce( child );
	}


	public Pres withInheritedStateAttr(String fieldName, Object value) {
		HashMap<String, Object> changed = new HashMap<String, Object>();
		HashSet<String> removed = null;

		if (removedValues != null  &&  (removedValues.size() > 1  ||  !removedValues.contains(fieldName))) {
			removed = new HashSet<String>();
			removed.addAll(removedValues);
			removed.remove(fieldName);
		}

		changed.put(fieldName, value);
		return new ApplyInheritedStateAttrs(changed, removed, child);
	}

	public Pres withoutInheritedStateAttr(String fieldName) {
		HashMap<String, Object> changed = null;
		HashSet<String> removed = new HashSet<String>();

		if (changedValues != null  &&  (changedValues.size() > 1  ||  !changedValues.containsKey(fieldName))) {
			changed = new HashMap<String, Object>();
			changed.putAll(changedValues);
			changed.remove(fieldName);
		}

		removed.add(fieldName);
		return new ApplyInheritedStateAttrs(changed, removed, child);
	}



	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		SimpleAttributeTable inh = ctx.getInheritedState();

		if (changedValues != null) {
			inh = inh.withAttrs(changedValues);
		}
		if (removedValues != null) {
			for (String k: removedValues) {
				inh = inh.withoutAttr(k);
			}
		}

		PresentationContext newCtx = new PresentationContext(ctx.getFragment(), ctx.getPerspective(), inh);
		return child.present( newCtx, style );
	}
}
