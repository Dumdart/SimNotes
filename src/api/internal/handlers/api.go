package handlers

import (
	"github.com/Dumdart/SimNotes/src/api/internal/middleware"
	"github.com/go-chi/chi"
	chimiddleware "github.com/go-chi/chi/middleware"
)

func Handler(r *chi.Mux) {
	r.Use(chimiddleware.StripSlashes) // Remove last slash from routes
	r.Use(middleware.Authorization)
}
