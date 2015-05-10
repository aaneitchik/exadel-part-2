var mainUrl = 'index';
var registerUp, registerBottom, errorUp, errorBottom;

var userProfile = function(mail, pass, action) {
	return {
		email: mail,
		password: pass,
		actionType: action
	};
};

function run() {
	var appContainer = document.getElementById('wrapper');
	registerUp = document.getElementById('register');
	registerBottom = document.getElementById('registerbottom');
	errorUp = registerUp.innerHTML;
	errorBottom = registerBottom.innerHTML;

	appContainer.addEventListener('click', delegateEvent);
	appContainer.addEventListener('keydown', delegateEvent);
}

function delegateEvent(evtObj) {
	if(evtObj.type === 'click') {
		if(evtObj.target.id === 'signinbutton')
		signIn();
		if(evtObj.target.id === 'signupbutton')
		signUp();
	}
}

function signIn() {
	var email = document.getElementById('enteremail').value;
	var password = document.getElementById('enterpassword').value;
	var user = userProfile(email, password, 'signin');
	if(email === '' || password === '') {
		var error = '<p class="error">You must enter email and password!</p>' + errorUp;
		registerUp.innerHTML = error;
	}
	else {
		post(mainUrl, JSON.stringify(user), function (responseText) {
			console.assert(responseText != null);

			var response = JSON.parse(responseText);
			handleSigning(response.answer, email, 'signin');

			continueWith && continueWith();
		});
	}
}

function signUp() {
	var email = document.getElementById('registeremail').value;
	var password = document.getElementById('registerpassword').value;
	if(email === '' || password === '') {
		var error = '<p class="error">You must enter email and password!</p>' + errorBottom;
		registerBottom.innerHTML = error;
	}
	else {
		var user = userProfile(email, password, 'signup');

		post(mainUrl, JSON.stringify(user), function (responseText) {
			console.assert(responseText != null);

			var response = JSON.parse(responseText);
			handleSigning(response.answer, email, 'signup');

			continueWith && continueWith();
		});
	}
}

function handleSigning(answer, email, type) {
	if(answer === 'OK') {
		if(typeof(Storage) == "undefined") {
			alert('localStorage is not accessible');
			return;
		}
		localStorage.setItem("email", email);
		registerUp.innerHTML = errorUp;
		window.location.href = "chat.html";
	}
	else if(answer === 'ERROR'){
		if(type === 'signin') {
			var error = '<p class="error">Wrong email or password!</p>' + errorUp;
			registerUp.innerHTML = error;
		}
		if(type === 'signup') {
			var error = '<p class="error">Sorry, this user already exists</p>' + errorBottom;
			registerBottom.innerHTML = error;
		}
	}
}

/////AJAX/////

function defaultErrorHandler(message) {
	console.error(message);
}

function post(url, data, continueWith, continueWithError) {
	ajax('POST', url, data, continueWith, continueWithError);	
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
