(function() {

	// private members
	var mHomeUrl = null;
	var mToken = null;
	var mConnected = false;

	// public interface
	var PBSB = {
		config : {
			verbose : true, // pertains to console logging
		// autoReconnect
		},

		listener : {
			onConnect : function() {
			},
			onDisconnect : function() {
			},
			onMessageReceived : function(msg, topic) {
			},
			onMessageSent : function(msg, subCount) {
			},
			onError : function(code, description) {
			},
			onToken : function(token) {
			},
			onSubscribe : function(topic, subCount) {
			}
		},

		connect : function(homeUrl) {
			mHomeUrl = homeUrl;
			GET(mHomeUrl, function(token) {
				mToken = token.trim();
				log('*** ontoken: ' + mToken);
				PBSB.listener.onToken(mToken);
				openChannel(mToken);
			});
		},

		subscribe : function(topic) {
			GET(mHomeUrl + "/subscribe/" + topic + "/" + mToken, function(data) {
				subCount = data.subCount;
				log('*** onsubscribe: ' + topic + '  (' + subCount + ' subscribers)');
				PBSB.listener.onSubscribe(topic, subCount);
			});
		},

		publish : function(topic, msg, excludeSelf) {
			var excStr = (excludeSelf && excludeSelf == true) ? ("?exclude=" + mToken) : "";
			POST(mHomeUrl + "/" + topic + excStr, msg, function(data) {
				log('*** onpublish (' + topic + '): ' + msg);
				PBSB.listener.onMessageSent(msg, data.subCount);
			});
		}
	};

	function openChannel(token) {
		var channel = new goog.appengine.Channel(token);
		channel.open({
			'onopen' : function() {
				log('*** onopen ***');
				mConnected = true;
				PBSB.listener.onConnect();
			},
			'onmessage' : function(evt) {
				log('*** onmessage: ' + JSON.stringify(evt));
				data = JSON.parse(evt.data);
				PBSB.listener.onMessageReceived(data.msg, data.topic);
			},
			'onerror' : function(evt) {
				log('*** onerror: ' + JSON.stringify(evt));
				PBSB.listener.onError(evt.code, evt.description);
			},
			'onclose' : function() {
				log('*** onclose ***');
				mConnected = false;
				PBSB.listener.onDisconnect();
				// TODO: initiate auto-reconnect?
			}
		});
	}

	function GET(url, successCallback, errorCallback) {
		var xhr = createXHR('GET', url, successCallback, errorCallback);
		xhr.send();
	}

	function POST(url, data, successCallback, errorCallback) {
		var xhr = createXHR('POST', url, successCallback, errorCallback);
		xhr.setRequestHeader("Content-type", "text/plain; charset=utf-8");
		xhr.send(data);
	}

	function createXHR(method, url, successCallback, errorCallback) {
		var xhr;
		try {
			xhr = new XMLHttpRequest();
		} catch (e) {
			xhr = new ActiveXObject("MSXML2.XMLHTTP.3.0");
		}
		xhr.open(method, url, true);
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4) {
				if (xhr.status == 200) {
					var ct = xhr.getResponseHeader("content-type");
					if (ct && ct.indexOf("application/json") >= 0)
						successCallback(JSON.parse(xhr.responseText));
					else
						successCallback(xhr.responseText);
				} else
					errorCallback(xhr.status, xhr.responseText);
			}
		};
		return xhr;
	}

	function log(text) {
		if (PBSB.config.verbose)
			console.log(text);
	}

	window.PBSB = PBSB;
})();
