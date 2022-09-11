# Coda Gateway
This repository contains the implementation of the authentication server of Project Coda.
Note that as this server utilizes JWT authorization mechanics, it is
important to use HTTPS in production.

## Setup
There must be an `application.properties` file with the following Gateway keys:
```properties
coda.mongo-host=...
coda.mongo-name=...
coda.token-expiration=2
coda.refresh-expiration=15
```
where:
- `coda.mongo-host` is the MongoDB connection URL
- `coda.mongo-name` is the MongoDB database name
- `coda.token-expiration` is the length of the lifetime of an authorization token, in hours.
- `coda.refresh-expiration` is the length of the lifetime of a refresh token, in days.
- `coda.recaptcha-secret` is the ReCAPTCHA secret used to deter bots.

Also, please configure other Spring properties vital to run Gateway, like setting up HTTPS etc.
<br>
By convention, Gateway should be set explicitly to listen on port 20560.

## Permissions
The permissions that a user can possess are the following (from highest to lowest):
- `dev.projectcoda.gateway.admin` (Admin permissions)
- `dev.projectcoda.gateway.moderator` (Moderator permissions)
- `dev.projectcoda.gateway.user` (Normal user permissions)

### Admin permissions
Admins may do the following:
- Award badges (see `dev.projectcoda.gateway.data.User`)
- Promote users to moderators, and demote moderators to users.
- Anything that a moderator can do.

### Moderator permissions
Moderators may do the following:
- Impose temporary or permanent bans on players.
- Mute (temporarily or permanently) players.
- Anything that a user can do.

### User permissions
Users may use multiplayer Coda services that require authentication.
Prominent examples are official matchmaking, or rooms.

## I18N
Gateway error messages are I18N keys. Take note and implement the following:
```java
class I18N {
	public static final String
			USERNAME_IN_USE = "Gateway.UsernameUsed",
			EMAIL_IN_USE = "Gateway.EmailUsed",
			CAPTCHA_ERROR = "Gateway.Captcha",
			BAD_CREDENTIALS = "Gateway.BadCredentials",
			UNAUTHORIZED = "Gateway.Unauthorized",
			PARAMETER_ERROR = "Gateway.Parameters";
}
```
This set will rarely change: if it does, please make sure to update your implementation.

## Endpoints
All endpoints require the header `Content-Type` to be `application/json`,
unless otherwise specified. Note that the JSON below is serialized from classes in the
`dev.projectcoda.gateway.api` package

### Error message
Before implementing the endpoints, please ensure to handle
such an error response:

```json
{
  "timestamp": "...",
  "message": "..."
}
```
- `timestamp` is the Java-formatted timestamp when the error occurred.
- `message` is the reason why the error occurred. See the section on I18N for possible error responses.

### `POST` - `/gateway/signup?g-recaptcha-response=...`
Registers a user into the Coda Gateway. This requires a query parameter `g-recaptcha-response`,
which holds the reCAPTCHA response resulting from the CAPTCHA solved by the user.

#### Request
```json
{
  "username": "...",
  "email": "...",
  "password": "..."
}
```
- `username` is the desired username of the player.
- `email` is the email of the player. Only one email can be used per account.
- `password` is the password of the player, in plaintext.

#### Response
```json
{
  "uuid": "..."
}
```
- `uuid` is the UUID of the player.

### `POST` - `/gateway/login`
Provides a user with a refresh JWT token and an authorization JWT token,
provided they log in with the correct credentials.

#### Request
```json
{
  "username": "...",
  "password": "..."
}
```
- `username` is the username the requester wishes to use to log in.
- `password` is the password to use, in plaintext.

#### Response
```json
{
  "uuid": "...",
  "refreshToken": "...",
  "authToken": "..."
}
```
- `uuid` is the UUID of the player.
- `refreshToken` is a refresh token that can be used in the `/gateway/refresh` endpoint.
- `authToken` is a generated authorization token for convenience purposes. To
get another authorization token on expiry, see `/gateway/refresh`.

### `POST` - `/gateway/valid`
> **DEPRECATION NOTICE** This endpoint is subject to removal in a future release, in favor of `/gateway/`.

Checks whether a token, refresh or authorization, is valid.
If it's valid, the type will be stated.

#### Request
```json
{
  "token": "..."
}
```
- `token` is the JWT token to validate.

#### Response
```json
{
  "valid": true,
  "type": "...",
  "permissions": [
    "...", 
    "..."
  ]
}
```
- `valid` is whether the given token is valid.
- `type` is the type of token. It is either `refresh` for a refresh token, or `auth` for an authorization token.
- `permissions` is an array of permissions possessed by the user. For more details on permissions see above.

### GET - `/gateway/`
Gets Gateway metadata.

#### Request
No request body is required, and it accepts
any `Content-Type`, contrary to other endpoints that strictly
only accept `application/json`.

### `GET` - `/gateway/user/{id}`
Gets information on the given user, denoted by the
path variable `{id}`.

#### Request
No request body is required, and it accepts
any `Content-Type`, contrary to other endpoints that strictly
only accept `application/json`.

#### Response

```json
{
  "username": "...",
  "uuid": "...",
  "bio": "...",
  "badges": [
    "...",
    "..."
  ],
  "rating": 1234,
  "rank": "SP",
  "permissions": [
    "...",
    "..."
  ],
  "email": "...",
  "avatar": "...",
  "friends": [
    "...",
    "..."
  ],
  "won": 1234,
  "totalPlayed": 1234
}
```
- `username` is the username of the user.
- `uuid` is the UUID of the user.
- `bio` is the bio of the user, in CommonMark Markdown.
- `badges` is an array of badge identifiers that the user may possess.
- `rating` is a Glicko rating of the player.
- `rank` is the rank of the user.
- `email` is the email of the user.
- `avatar` is the URL avatar of the user.
- `friends` is the user's friends. This can be mutual (both have friended each other), or one sided (e.g., a player friends a famous player, but the famous player
doesn't friend them back).
- `won` is the number of games won by the user.
- `totalPlayed` is the number of total games played by the user.

The user's rank is dependent on the rating of the user. From highest to lowest:
```java
enum Rank {
	SP("S+", 4500), 
    S("S", 3500), 
    A("A", 2000), 
    B("B", 1600), 
    C("C", 1200), 
    D("D", 2000), 
    E("E", 750), 
    F("F", 0),
	UNRANKED("Unranked", 0) // player needs 10 games played minimum to be ranked.
}
```
where the format is `[name]('"' [user-friendly name] '"', [threshold]`
and:
- `name` is the name that is actually returned as the `rank` property of the response.
- `user-friendly name` is the name that should be displayed to users.
- `threshold` is the threshold required to reach the given rank.

### `PUT` - `/gateway/user/{id}`
Updates information of the given user, denoted by the
path variable `{id}`. Compared to the above, this requires `Content-Type` to be `application/json`.
The header `Authorization` must be set to employ the `Bearer` scheme.
For example, the following can be used:
```http request
PUT /gateway/user/{id}
Content-Type: application/json
Authorization: Bearer ...

<request>
```
where `{id}` is the UUID of the user, and `...` is the JWT authorization token for the corresponding player.

#### Request
```json
{
  "bio": "...",
  "email": "..."
}
```
- `bio` is the new bio of the user, in CommonMark markdown.
- `email` is the new email of the user. This input field is subjected to the constraints in `/gateway/signup`.

#### Response
A `204 No Content` response is returned.

### `PUT` - `/user/{id}/server`
This endpoint should be ignored, as it is meant to be called
solely by the server. Hence, it is not documented here.

## FAQ

### Why not include keys that persist throughout Gateway runs?
This is a deliberate design of Gateway, as we can easily invalidate JWTs when
we fix bugs or major security issues.