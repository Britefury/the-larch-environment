from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import Button, Checkbox, IntSlider, ToggleButton

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer, Column

from LarchCore.ipython.widget import IPythonWidgetView



class ContainerView (IPythonWidgetView):
	def on_display(self):
		pass
		# for child_comm_id in self._children:
		# 	widget_model = self._widget_manager.get_by_comm_id(child_comm_id)
		# 	widget_model.display(self._widget_model.result)

	def __present__(self, fragment, inh):
		self._incr.onAccess()

		child_comm_ids = self._children
		children = [self._widget_manager.get_by_comm_id(child_comm_id)   for child_comm_id in child_comm_ids]

		return Column(children)


