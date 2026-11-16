package routes

import (
	"net/http"

	"github.com/Dumdart/SimNotes/src/api/api"
	"github.com/Dumdart/SimNotes/src/api/internal/handlers"
	"github.com/go-chi/chi"
)

type HealthReport struct {
	health   bool
	diagnose string
}

func IsHealthy(w http.ResponseWriter, r *http.Request) {
	api.InternalErrorHandler(w)
}

func Routes(r *chi.Mux) {
	r.Get("/", IsHealthy)
	r.Get("/notes", handlers.GetNotesRoute)
}
