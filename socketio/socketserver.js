const express = require('express');
const socket = require('socket.io');
const {spawn} = require('child_process');

// App Setup
var app = express();
// Create server and listen to specific port number
var server = app.listen(4000, function(){
	console.log('Listening to requests on port 4000');
});

// Static files or middle-ware to serve public folder to client
app.use(express.static('public'));

// Socket setup; provide server reference to work with
var io = socket(server);

// Listening to an connection event; socket parameter refers to created/particular socket
io.on('connection', function(socket){
	// Print socket id on new connection
	console.log(`made socket connection, ${socket.id}`);

	// Listen for chat message being sent from client
	socket.on('chat1', function(data){
		console.log('-----------------')
		console.log(data)

		const sendingdata = "sendingdata"
		const python2 = spawn('python', ['test.py', sendingdata]) // python 파일을 실행시키고 나온 결과 값

		python2.stdout.on('data', (data) => {
			dataToSend = data.toString(); // python script를 통한 결과값
			console.log(dataToSend)
		})
		// Send received chat message to all connected clients
		io.sockets.emit('chat2',data);
	});

    socket.on('disconnect', () => {
        console.log(`Socket disconnected : ${socket.id}`)
    })
});
