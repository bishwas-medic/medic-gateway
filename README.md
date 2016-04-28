`medic-gateway`
===============

<a href="https://travis-ci.org/alxndrsn/medic-gateway"><img src="https://travis-ci.org/alxndrsn/medic-gateway.svg?branch=master"/></a>

Download APKs from: https://github.com/alxndrsn/medic-gateway/releases

-----

An SMS gateway for Android.  Send and receive SMS from your webapp via an Android phone.

	+--------+                 +-----------+
	|  web   |                 |  medic-   | <-------- SMS
	| server | <---- HTTP ---- |  gateway  |
	|        |                 | (android) | --------> SMS
	+--------+                 +-----------+

# API

This is the API specification for communications between `medic-gateway` and a web server.  Messages in both directions are `application/json`.

Where a list of values is expected but there are no values provided, it is acceptable to:

* provide a `null` value; or
* provide an empty array (`[]`); or
* omit the field completely

Bar array behaviour specified above, `medic-gateway` _must_ include fields specified in this document, and the web server _must_ include all expected fields in its responses.  Either party _may_ include extra fields as they see fit.

## Idempotence

N.B. messages are cosidered duplicate by `medic-gateway` if they have identical values for `id`.  The webapp is expected to do the same.

`medic-gateway` will not re-process duplicate webapp-originating messages.

`medic-gateway` may forward a webapp-terminating message to the webapp multiple times.

`medic-gateway` may forward a delivery status report to the webapp multiple times for the same message.  This should indicate a change of state, but duplicate delivery reports may be delivered in some circumstances, including:

* the phone receives multiple delivery status reports from the mobile network for the same message
* `medic-gateway` failed to process the webapp's response when the delivery report was last forwarded from `medic-gateway` to webapp

## Authorisation

`medic-gateway` supports [HTTP Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication).  Just include the username and password for your web endpoint when configuring `medic-gateway`, e.g.:

	https://username:password@example.com/medic-gateway-api-endpoint

## Messages

THe entire API should be implemented by a webapp at a single endpoint, e.g. https://exmaple.com/medic-gateway-api-endpoint

### GET

Expected response:

	{
		"medic-gateway": true
	}

### POST

`medic-gateway` will accept and process any relevant data received in a response.  However, it may choose to only send certain types of information in a particular request (e.g. only provide a webapp-terminating SMS), and will also poll the web service periodically for webapp-originating messages, even if it has no new data to pass to the web service.

### Request

#### Headers

The following headers will be set by requests:

header           | value
-----------------|-------------------
`Accept`         | `application/json`
`Accept-Charset` | `utf-8`
`Accept-Encoding`| `gzip`
`Cache-Control`  | `no-cache`
`Content-Type`   | `application/json`

Requests and responses may be sent with `Content-Encoding` set to `gzip`.

#### Content

	{
		messages: [
			{
				id: <String: uuid, generated by `medic-gateway`>,
				from: <String: international phone number>,
				content: <String: message content>
			},
			...
		],
		deliveries: [
			{
				id: <String: uuid, generated by webapp>,
				status: <String: SENT|DELIVERED|REJECTED|FAILED>
			},
			...
		],
	}

### Response

#### Success

##### HTTP Status: `2xx`

Clients may respond with any status code in the `200`-`299` range, as they feel is
appropriate.  `medic-gateway` will treat all of these statuses the same.

##### Content

	{
		messages: [
			{
				id: <String: uuid, generated by webapp>,
				to: <String: local or international phone number>,
				content: <String: message content>
			},
			...
		],
	}

#### HTTP Status `400`+

Response codes of `400` and above will be treated as errors.

If the response's `Content-Type` header is set to `application/json`, `medic-gateway` will attempt to parse the body as JSON.  The following structure is expected:

	{
		error: true,
		message: <String: error message>
	}

The `message` property may be logged and/or displayed to users in the `medic-gateway` UI.

#### Other response codes

Treatment of response codes below `200` and between `300` and `399` will _probably_ be handled sensibly by Android.


# Development

## Requirements

### `medic-gateway`

* JDK

### `demo-server`

* npm

## Building locally

To build locally and install to an attached android device:

	make

To run tests and static analysis tools locally:

	make test

## `demo-server`

There is a demonstration implementation of a server included for `medic-gateway` to communicate with.  You can add messages to this server, and query it to see the interactions that it has with `medic-gateway`.

### Local

To start the demo server locally:

	make demo-server

To list the data stored on the server:

	curl http://localhost:8000

To make the next good request to `/app` return an error:

	curl -X POST http://localhost:8000/error --data '"Something failed!"'

To add a webapp-originating message (to be send by `medic-gateway`):

	curl -vvv -X POST -d '{ "id": "3E105262-070C-4913-949B-E7ACA4F42B71", "to": "+447123555888", "content": "hi" }' http://localhost:8000

To simulate a request from `medic-gateway`:

	curl -X POST http://localhost:8000/app -H "Accept: application/json" -H "Accept-Charset: utf-8" -H "Accept-Encoding: gzip" -H "Cache-Control: no-cache" -H "Content-Type: application/json" -d'{}'

To clear the data stored on the server:

	curl -X DELETE http://localhost:8000

To set a username and password on the demo server:

	curl -X POST -d'{"username":"alice", "password":"secret"}' http://localhost:8000/auth

### Remote

It's simple to deploy the demo server to remote NodeJS hosts.

#### Heroku

TODO walkthrough

#### Modulus

TODO walkthrough
