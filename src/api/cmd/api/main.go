package main

import (
	"fmt"
	"net/http"

	"github.com/Dumdart/SimNotes/src/api/internal/handlers"
	"github.com/Dumdart/SimNotes/src/api/internal/routes"
	"github.com/go-chi/chi"
	log "github.com/sirupsen/logrus"
)

func main() {
	log.SetReportCaller(true)

	var r *chi.Mux = chi.NewRouter()

	handlers.Handler(r)
	routes.Routes(r)

	fmt.Println("Starting GO API service ...")

	err := http.ListenAndServe("localhost:8000", r)

	if err != nil {
		log.Error(err)
	}
}
