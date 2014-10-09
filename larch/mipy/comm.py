##-*************************
##-* This software can be used, redistributed and/or modified under
##-* the terms of the BSD 2-clause license as found in the file
##-* 'License.txt' in this distribution.
##-* This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************

class Comm(object):
	def __init__(self, kernel_connection, comm_id, target_name):
		self.__kernel = kernel_connection
		self.comm_id = comm_id
		self.target_name = target_name

		self.on_message = None
		self.on_closed_remotely = None


	def message(self, data):
		kernel = self.__kernel
		if kernel._open:
			kernel.session.send(kernel.shell, 'comm_msg', {
				'comm_id': self.comm_id,
				'data': data
			})

	def close(self, data):
		kernel = self.__kernel
		if kernel._open:
			kernel.session.send(kernel.shell, 'comm_close', {
				'comm_id': self.comm_id,
				'data': data
			})
			kernel._notify_comm_closed(self)



