'use strict';

var messagesWaiting = false; //this variable is set in XMLHttpRequest .onreadysetchange to inform of 'get' success

var uniqueId = function() {
	var date = Date.now();
	var random = Math.random() * Math.random();

	return Math.floor(date * random).toString();
};

var theMessage = function(name, text, sentby) {
	return {
		userName: name,
		message: text,
		id: uniqueId(),
		state: 'standard',
		sender: sentby
	};
};

var appState = {
	mainUrl : 'chat',
	userEmail: localStorage.getItem('email'),
	userName : 'anonymous',
	messageList : [],
	firstToken: 'TE11EN',
	token : 'TE19EN',
	setPause : 0
};

function run() {
	if (appState.userEmail == null)
		window.location.href = "secret.html";

	var appContainer = document.getElementById('wrapper');
	appContainer.addEventListener('click', delegateEvent);
	appContainer.addEventListener('keydown', delegateEvent);

	firstRestore();

	process();
}

function process() {
	if(appState.setPause == 0 && messagesWaiting == false) {
		restore();
	}
	setTimeout(process,2000);
}

function delegateEvent(evtObj) {
	if(evtObj.type === 'click') {
		if(evtObj.target.id === 'submitbutton')
		sendMessage();
		if(evtObj.target.id === 'username')
		clearUsername();
		if(evtObj.target.className === 'editicon')
		editMessage(evtObj.target);
		if(evtObj.target.className === 'deleteicon')
		deleteMessage(evtObj.target.parentNode.parentNode.parentNode.parentNode.parentNode);
	}
	if(evtObj.type === 'keydown' && evtObj.keyCode == 13) {
			if(evtObj.target.id == 'message' && !evtObj.shiftKey) {
				sendMessage();
				evtObj.target.blur();
				setTimeout(function() { document.getElementById('message').focus(); }, 20);
			}
			if(evtObj.target.id == 'username') {
				enterName();
			}
		}
}

/////Dealing with messages/////

function createAllMessages(allMessages) {
	for(var i = 0; i < allMessages.length; i++) {
		if(allMessages[i].message != '') {
			if(allMessages[i].state == 'standard') {
				addInternal(allMessages[i]);
			} else if (allMessages[i].state == 'modified') {
				replaceMessage(allMessages[i]);
			}
			allMessages[i].state = 'builded';
		}
	}
}

function createAllMessagesFirst(allMessages) {
	for(var i = 0; i < allMessages.length; i++) {
		if(allMessages[i].message != '') {
			if(allMessages[i].state == 'standard') {
				addInternal(allMessages[i]);
			} else if (allMessages[i].state == 'modified') {
				var boxSize = document.getElementById('messages').childNodes.length;
				 if(boxSize < allMessages.length) {
				 addInternal(allMessages[i]);
				 }
				 else {
				 replaceMessage(allMessages[i]);
				 }
				replaceMessage(allMessages[i]);
			}
			allMessages[i].state = 'builded';
		}
	}
}

function addInternal(msg) {
	var message = createMessage(msg);
	var messages = document.getElementById('messages');
	messages.appendChild(message);
	messages.scrollTop = messages.scrollHeight; //move scrollbar to the end of div
}

function createMessage(msg) {
	var message = document.createElement('div');
	var name = msg.userName;
	var text = msg.message;
	var sender = msg.sender;
	message.className = 'message';
	message.id = msg.id;
	text = text.replace(/</g, '&lt;');
	text = text.replace(/>/g, '&gt;');
	text = text.replace(/\n/g, '<br />');
	var innerText = '<table><tr><td style="width: auto; min-width: 60px "><b>' + name + ':</b></td>';
	if(sender === appState.userEmail)
		innerText += '<td style="width: 75%;';
	else
		innerText += '<td style="width: 80%;';
	innerText += 'word-wrap: break-word" onBlur="makeUneditable()">' + text + '</td>';
	if(sender === appState.userEmail) {
		innerText += '<td width=5% style="padding-left: 5px"><img src="images/icon_edit.png" class="editicon"></img><img src="images/icon_delete.png" class="deleteicon"</img></td>';
	}
	innerText += '</tr></table>';
	message.innerHTML = innerText;
	return message;
}

function sendMessage() {
	var message = document.getElementById('message');
	var msg = message.value;
	if(msg !='') {
		var mes = theMessage(appState.userName, msg, appState.userEmail);
		message.value = '';
		addMessage(mes);
	}
}

function addMessage(msg) {
	post(appState.mainUrl, JSON.stringify(msg), function(){});
}

///////////////////////////

/////Redrawing messages/////

function replaceMessage(msg) {
	var message = createMessage(msg);
	if (message != "") {
		var box = document.getElementById('messages');
		var messages = box.childNodes;
		for(var i = 0; i < messages.length; i++) {
			if(messages[i].id == msg.id) {
				messages[i].innerHTML = message.innerHTML;
			}
		}
	}
}

function deleteFromBox(msg) {
	var box = document.getElementById('messages');
	var messages = box.childNodes;
	var toReplace;
	for(var i = 0; i < messages.length; i++) {
		if(messages[i].id == msg.id) {
			toReplace = messages[i];
		}
	}
	if (toReplace) {
		toReplace.parentNode.removeChild(toReplace);
	}
}

////////////////////////////

/////Dealing with username/////

function enterName() {
	if (event.keyCode == 13) {
		appState.userName = document.getElementById('username').value;
		document.getElementById('username').blur();
		return false;
	}
	return true;
}

function clearUsername() {
	document.getElementById('username').value = '';
}

////////////////////////

/////Deleting messages/////

function deleteMessage(evtObj) {
	var toDelete = evtObj;
	toDelete.parentNode.removeChild(toDelete);
	deleteMessageFromList(toDelete.id);
}

function deleteMessageFromList(id, continueWith) {
	del(appState.mainUrl + '?id=' + id, function(){
		restore();
	});
}

function deleteFromList(id) {
	for(var i = 0; i < appState.messageList.length; i++) {
		if(appState.messageList[i].id == id) {
			appState.messageList.splice(i, 1);
			return;
		}
	}
}

////////////////////////

/////Editing messages/////

function editMessage(evtObj) {
	appState.setPause = 1;
	var toEdit = event.target;
	var edit = toEdit.parentNode.parentNode.parentNode.parentNode.parentNode;
	toEdit = toEdit.parentNode.previousElementSibling;
	toEdit.setAttribute('contenteditable', true);
	toEdit.setAttribute('outline', 'none');
	toEdit.focus();
}

function editInternal(id, msg, continueWith) {
	put(appState.mainUrl + '?id=' + id, JSON.stringify(msg), function(){
		continueWith();
	});
}

function editList(id, txt) {
	for(var i = 0; i < appState.messageList.length; i++) {
		if(appState.messageList[i].id == id) {
			appState.messageList[i].message = txt;
			appState.messageList[i].state = 'modified';
			return;
		}
	}
}

//Make td of a table uneditable after finishing
function makeUneditable() {
	var td = event.target;
	var txt = td.innerText;
	var msg = td.parentNode.parentNode.parentNode.parentNode;
	if(txt == '') {
		deleteMessage(msg);
		td.setAttribute('contenteditable', false);
		return;
	}
	td.setAttribute('contenteditable', false);
	appState.setPause = 0;
	editInternal(msg.id, appState.messageList[msg.id - 1], editList(msg.id, txt));
}
 
//////////////////////

/////Restoring messages/////

function restore(continueWith) {
	messagesWaiting = true;
	var url = appState.mainUrl + '?token=' + appState.token;

	get(url, function(responseText) {
		console.assert(responseText != null);

		var response = JSON.parse(responseText);

		addMessagesToList(response.messages);
		createAllMessages(appState.messageList);
		appState.token = response.token;

		continueWith && continueWith();
	});
}

function firstRestore(continueWith) {
	var url = appState.mainUrl + '?token=' + appState.firstToken;

	get(url, function(responseText) {
		console.assert(responseText != null);

		var response = JSON.parse(responseText);

		addMessagesToList(response.messages);
		createAllMessagesFirst(appState.messageList);

		continueWith && continueWith();
	});
}

/////Editing local message list/////

function addMessagesToList(responseMessages){
	for (var i = 0; i < responseMessages.length; ++i) {
		var used = 0;
		for (var j =0; j < appState.messageList.length; ++j) {
			if(responseMessages[i].id == appState.messageList[j].id) {
				used = 1;
				if (appState.messageList[j].message != responseMessages[i].message) {
					appState.messageList[j].message = responseMessages[i].message;
					appState.messageList[j].state = "modified";
					if(appState.messageList[j].message =='') {
						deleteFromBox(appState.messageList[j]); 
					}
				}
				break;
			}
		}
		if (used == 0) {
			appState.messageList.push(responseMessages[i]);
		}
	}
}

////////////////////////////////////

/////Some methods for server/////

function defaultErrorHandler(message) {
	console.error(message);
}

function get(url, continueWith, continueWithError) {
	ajax('GET', url, null, continueWith, continueWithError);
}

function post(url, data, continueWith, continueWithError) {
	ajax('POST', url, data, continueWith, continueWithError);	
}

function put(url, data, continueWith, continueWithError) {
	ajax('PUT', url, data, continueWith, continueWithError);	
}

function del(url, data, continueWith, continueWithError) {
	ajax('DELETE', url, null, continueWith, continueWithError);	
}

function trace(url, continueWith, continueWithError) {
	ajax('OPTIONS', url, null, continueWith, continueWithError);	
}

function isError(text) {
	if(text == "")
		return false;
	
	try {
		var obj = JSON.parse(text);
	} catch(ex) {
		return true;
	}

	return !!obj.error;
}

function ajax(method, url, data, continueWith, continueWithError) {
	var xhr = new XMLHttpRequest();

	xhr.onreadystatechange = function(){
		if (xhr.readyState==4 && xhr.status==200) {
			messagesWaiting = false;
		}
	}

	continueWithError = continueWithError || defaultErrorHandler;
	xhr.open(method || 'GET', url, true);

	xhr.onload = function () {
		if (xhr.readyState !== 4)
			return;

		if(xhr.status != 200) {
			continueWithError('Error on the server side, response ' + xhr.status);
			return;
		}

		if(isError(xhr.responseText)) {
			continueWithError('Error on the server side, response ' + xhr.responseText);
			return;
		}
		continueWith(xhr.responseText);
	};    

    xhr.ontimeout = function () {
    	continueWithError('Server timed out !');
    }

    xhr.onerror = function (e) {
    	var errMsg = 'Server connection error !\n'+
    	'\n' +
    	'Check if \n'+
    	'- server is active\n'+
    	'- server sends header "Access-Control-Allow-Origin:*"';

        continueWithError(errMsg);
    };

    xhr.send(data);
}

/////////////////////////////////