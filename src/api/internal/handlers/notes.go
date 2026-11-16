package handlers

import (
	"encoding/json"
	"net/http"

	"github.com/Dumdart/SimNotes/src/api/api"
	database "github.com/Dumdart/SimNotes/src/api/internal/data"
)

func GetNotesRoute(w http.ResponseWriter, r *http.Request) {
	db, err := database.NewDatabase()
	if err != nil {
		api.InternalErrorHandler(w)
		return
	}

	notes := (*db).GetAllNotes()
	payload, err := json.Marshal(notes)
	if err != nil {
		api.InternalErrorHandler(w)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.Write(payload)
}
