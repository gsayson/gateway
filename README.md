# Coda Gateway
This repository contains the implementation of the authentication server of Project Coda.
Note that as this server utilizes JWT authorization mechanics, it is
important to use HTTPS in production.

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

## Endpoints
All endpoints require the header `Content-Type` to be `application/json`,
unless otherwise specified.

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
- `message` is the reason why the error occurred.

### `POST` - `/gateway/signup`
Registers a user into the Coda Gateway.

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

### `GET` - `/gateway/login`
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

### `GET` - `/gateway/valid`
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
  "permissions": ["...", "..."]
}
```
- `valid` is whether the given token is valid.
- `type` is the type of token. It is either `refresh` for a refresh token, or `auth` for an authorization token.
- `permissions` is an array of permissions possessed by the user. For more details on permissions see above.

### `GET` - `/gateway/user/{id}`
Gets information on the given user, denoted by the
path variable `{id}`.

#### Request
No request body is required, and it accepts
any `Content-Type`, contrary to other endpoints that strictly
only accept `application/json`.

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