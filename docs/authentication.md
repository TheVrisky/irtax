# Android authentication

## Current client behavior
- Uses Laravel Sanctum personal access tokens as Bearer tokens.
- Saves the token in AndroidX EncryptedSharedPreferences backed by Android Keystore.
- Restores the token before the main screen is displayed.
- Clears the local token on logout, even if the server is offline.
- Does not store passwords.
- Never log tokens, passwords, or Authorization headers.

## Required Laravel contract
### POST /api/v1/auth/login
Request:
```json
{ "email": "user@example.com", "password": "secret" }
```
Response:
```json
{
  "data": {
    "token": "sanctum-token",
    "token_type": "Bearer",
    "user": { "id": "123", "name": "User", "email": "user@example.com" }
  },
  "message": null,
  "errors": null
}
```

### POST /api/v1/auth/logout
Must require Sanctum authentication and revoke only the current access token.
Response uses the common API envelope.

## Security and backup
The encrypted session preferences file is excluded from Android cloud backup
and device transfer. Android Keystore keys are device-bound.

## Not implemented yet
- Login screen and account UI.
- Password reset / email verification.
- Token expiry refresh (Sanctum personal access tokens do not refresh automatically).
- Server-side account status and role display.
