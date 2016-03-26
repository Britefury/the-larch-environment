from java.awt import Color

from BritefuryJ.LSpace import Anchor

from BritefuryJ.Controls import Button

from BritefuryJ.Graphics import FillPainter

from BritefuryJ.Pres.Primitive import Primitive, Blank, Label, Row, Spacer, Column
from BritefuryJ.StyleSheet import StyleSheet

from LarchCore.ipython.widget import IPythonWidgetView



class BoxView (IPythonWidgetView):
	def on_display(self):
		pass

	def _present_children(self):
		child_view_ids = self.children
		children = [self._widget_manager.get_by_view_id(child_view_id)   for child_view_id in child_view_ids]

		print 'BoxView._present_children: child_comm_ids={0}'.format(child_view_ids)

		return self._column_style(Column(children))


	def __present__(self, fragment, inh):
		self._incr.onAccess()
		return self._present_children()


	_column_style = StyleSheet.style(Primitive.columnSpacing(3.0))




class FlexBoxView (BoxView):
	def _present_children(self):
		child_view_ids = self.children
		children = [self._widget_manager.get_by_view_id(child_view_id)   for child_view_id in child_view_ids]

		orientation = self.orientation

		if orientation == 'horizontal':
			return self._row_style(Row(children))
		elif orientation == 'vertical':
			return self._column_style(Column(children))
		else:
			raise ValueError, 'unreckognised orientation {0}'.format(orientation)


	_row_style = StyleSheet.style(Primitive.rowSpacing(3.0))

