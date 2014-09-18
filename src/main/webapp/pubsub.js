(function() {

	// private members
	var mHomeUrl = null;
	var mToken = null;
	var mConnected = false;

	// public interface
	var pubsub = {
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
			$.ajax({
				type : "GET",
				url : mHomeUrl,
				success : function(token) {
					mToken = token.trim();
					log('*** ontoken: ' + mToken);
					pubsub.listener.onToken(mToken);
					openChannel(mToken);
				}
			});
		},

		subscribe : function(topic) {
			$.ajax({
				type : "GET",
				url : mHomeUrl + "/subscribe/" + topic + "/" + mToken,
				success : function(data) {
					subCount = data.subCount;
					log('*** onsubscribe: ' + topic + '  (' + subCount + ' subscribers)');
					pubsub.listener.onSubscribe(topic, subCount);
				}
			});
		},

		publish : function(topic, msg, excludeSelf) {
			var excStr = (excludeSelf && excludeSelf == true) ? ("?exclude=" + mToken) : "";
			$.ajax({
				type : "POST",
				url : mHomeUrl + "/" + topic + excStr,
				data : msg,
				contentType : "text/plain; charset=utf-8",
				success : function(data) {
					log('*** onpublish (' + topic + '): ' + msg);
					pubsub.listener.onMessageSent(msg, data.subCount);
				}
			});
		}
	};

	function openChannel(token) {
		var channel = new goog.appengine.Channel(token);
		channel.open({
			'onopen' : function() {
				log('*** onopen ***');
				mConnected = true;
				pubsub.listener.onConnect();
			},
			'onmessage' : function(evt) {
				log('*** onmessage: ' + JSON.stringify(evt));
				data = JSON.parse(evt.data);
				pubsub.listener.onMessageReceived(data.msg, data.topic);
			},
			'onerror' : function(evt) {
				log('*** onerror: ' + JSON.stringify(evt));
				pubsub.listener.onError(evt.code, evt.description);
			},
			'onclose' : function() {
				log('*** onclose ***');
				mConnected = false;
				pubsub.listener.onDisconnect();
				// TODO: initiate auto-reconnect?
			}
		});
	}

	function log(text) {
		if (pubsub.config.verbose)
			console.log(text);
	}

	window.pubsub = pubsub;
})();
