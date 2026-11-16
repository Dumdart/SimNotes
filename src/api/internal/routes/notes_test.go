package routes_test

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	database "github.com/Dumdart/SimNotes/src/api/internal/data"
	"github.com/Dumdart/SimNotes/src/api/internal/handlers"
	"github.com/Dumdart/SimNotes/src/api/internal/routes"
	"github.com/go-chi/chi"
)

func TestGetNotesRoute(t *testing.T) {
	router := chi.NewRouter()
	handlers.Handler(router)
	routes.Routes(router)

	server := httptest.NewServer(router)
	defer server.Close()

	response, err := server.Client().Get(server.URL + "/notes")
	if err != nil {
		t.Fatalf("GET /notes: %v", err)
	}
	defer response.Body.Close()

	if response.StatusCode != http.StatusOK {
		t.Fatalf("status = %d, want %d", response.StatusCode, http.StatusOK)
	}
	if contentType := response.Header.Get("Content-Type"); contentType != "application/json" {
		t.Fatalf("Content-Type = %q, want application/json", contentType)
	}

	var notes []database.Note
	if err := json.NewDecoder(response.Body).Decode(&notes); err != nil {
		t.Fatalf("decode notes array: %v", err)
	}

	expected := map[string]bool{
		"Milk":    false,
		"Sausage": true,
		"Bread":   false,
	}
	if len(notes) != len(expected) {
		t.Fatalf("received %d notes, want %d", len(notes), len(expected))
	}
	for _, note := range notes {
		checked, exists := expected[note.Content]
		if !exists {
			t.Fatalf("unexpected or duplicate note: %+v", note)
		}
		if note.Is_checked != checked {
			t.Errorf("note %q checked = %t, want %t", note.Content, note.Is_checked, checked)
		}
		delete(expected, note.Content)
	}
}

func TestNotesRouteRejectsPost(t *testing.T) {
	router := chi.NewRouter()
	handlers.Handler(router)
	routes.Routes(router)

	response := httptest.NewRecorder()
	router.ServeHTTP(response, httptest.NewRequest(http.MethodPost, "/notes", nil))

	if response.Code != http.StatusMethodNotAllowed {
		t.Fatalf("status = %d, want %d", response.Code, http.StatusMethodNotAllowed)
	}
}
