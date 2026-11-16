package middleware

import (
	"errors"
	"net/http"
)

var UnAuthorizedError = errors.New("Invalid username or token.")

func Authorization(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// TODO: Add custom key for NoteAPI

		// var token = r.Header.Get("Authorization") // AuthToken
		// api.RequestErrorHandler(w, UnAuthorizedError)
		next.ServeHTTP(w, r)
	})
}
