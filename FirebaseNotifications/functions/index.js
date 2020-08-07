'use-strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
var app = admin.initializeApp();

exports.sendNotification = functions.firestore.document("networks/{network_id}/hospitals/{hospitals_id}/notifications/{notification_id}")
.onWrite((change, context) => {

	const notification_id = context.params.notification_id;

	//functions.logger.log("Notification ID: " + notification_id);

	const notification_doc = change.after.data();

	const notification_body = notification_doc.body;
	const notification_header = notification_doc.header;
	const topic = notification_doc.hospital_id;

	var message = {
	  data: {
	    header: notification_header,
	    body: notification_body
	  },
	  //topic: topic
  	  //token: "c_mjAu6KTkOK9Z_Qe8EopD:APA91bHSdvA0VcscfANPw8o4-LtAJCanoLfPsQzmnHl77-Jl3zLiDVhVJ3UxP1BQrklZUMS1mxb3QJXoqb3Ja-VMrFN7kcA33_2PEO3DJUSPdsKJCtzGzF5HPiVk0F41R76pSjv5JoYx"
	};
	return admin.messaging().sendToTopic(topic, message)
	.catch(error => {
		console.error("FCM error", error)
	});

	// admin.messaging().send(message).then((response) => {
	// 	console.log("Successfully sent message: ", response);
 //        return response;
	// })
	// .catch((error) => {
	// 	console.log("Error sending message: ", error);
 //        return error;
	// });


	// functions.logger.log("Notif Body: " + notification_body + "Notif Header: " + notification_header
	//  + "Notification Hospital ID: " + notification_hospital_id);

});

