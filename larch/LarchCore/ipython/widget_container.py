from java.awt import Color

from BritefuryJ.LSpace import Anchor

from BritefuryJ.Controls import Button

from BritefuryJ.Graphics import FillPainter

from BritefuryJ.Pres.Primitive import Primitive, Blank, Label, Row, Spacer, Column
from BritefuryJ.StyleSheet import StyleSheet

from LarchCore.ipython.widget import IPythonWidgetView



class ContainerView (IPythonWidgetView):
	def on_display(self):
		pass


	def _present_children(self):
		child_comm_ids = self._children
		children = [self._widget_manager.get_by_comm_id(child_comm_id)   for child_comm_id in child_comm_ids]

		return self._container_style(Column(children))

	def __present__(self, fragment, inh):
		self._incr.onAccess()
		return self._present_children()


	_container_style = StyleSheet.style(Primitive.columnSpacing(3.0))


class PopupView (ContainerView):
	def on_display(self):
		pass

	def __present__(self, fragment, inh):
		self._incr.onAccess()

		def _on_show(button, event):
			self._present_children().popup(button.element, Anchor.BOTTOM_LEFT, Anchor.TOP_LEFT, True, True)

		return Button.buttonWithLabel(self.button_text, _on_show)

	_container_style = StyleSheet.style(Primitive.columnSpacing(3.0), Primitive.background(FillPainter(Color(1.0, 1.0, 1.0))))
