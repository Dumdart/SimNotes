import runpy
import unittest
from datetime import datetime
from pathlib import Path

from fastapi.testclient import TestClient


APP_PATH = Path(__file__).resolve().parents[1] / "src" / "api" / "main.py"


class CrudApiTests(unittest.TestCase):
    def setUp(self):
        module = runpy.run_path(str(APP_PATH))
        self.client = self.enterContext(TestClient(module["app"]))

    def create_checklist(self, name="Shopping"):
        response = self.client.post("/checklists", json={"name": name})
        self.assertEqual(response.status_code, 201)
        return response.json()

    def test_checklist_lifecycle(self):
        checklist = self.create_checklist()
        path = f"/checklists/{checklist['id']}"
        self.assertEqual(checklist["notes"], [])
        self.assertIsNotNone(datetime.fromisoformat(checklist["created_at"]).tzinfo)
        self.assertIn(checklist, self.client.get("/checklists").json())
        self.assertEqual(self.client.get(path).json(), checklist)
        response = self.client.put(path, json={"name": "Groceries"})
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["name"], "Groceries")
        self.assertEqual(response.json()["created_at"], checklist["created_at"])
        self.assertEqual(response.json()["id"], checklist["id"])
        response = self.client.delete(path)
        self.assertEqual(response.status_code, 204)
        self.assertEqual(response.content, b"")
        self.assertEqual(self.client.get(path).status_code, 404)

    def test_note_lifecycle(self):
        checklist = self.create_checklist()
        notes_path = f"/checklists/{checklist['id']}/notes"
        self.assertEqual(self.client.get(notes_path).json(), [])
        response = self.client.post(notes_path, json={"content": "Eggs"})
        self.assertEqual(response.status_code, 201)
        note = response.json()
        self.assertFalse(note["is_checked"])
        self.assertIsNotNone(datetime.fromisoformat(note["created_at"]).tzinfo)
        path = f"{notes_path}/{note['id']}"
        self.assertEqual(self.client.get(path).json(), note)
        self.assertEqual(self.client.get(notes_path).json(), [note])
        response = self.client.put(path, json={"content": "Six eggs", "is_checked": True})
        self.assertEqual(response.status_code, 200)
        updated = response.json()
        self.assertEqual(updated["content"], "Six eggs")
        self.assertTrue(updated["is_checked"])
        self.assertEqual(updated["id"], note["id"])
        self.assertEqual(updated["created_at"], note["created_at"])
        self.assertEqual(
            self.client.get(f"/checklists/{checklist['id']}").json()["notes"], [updated]
        )
        self.assertIn(updated, self.client.get("/notes").json()["notes"])
        response = self.client.delete(path)
        self.assertEqual(response.status_code, 204)
        self.assertEqual(response.content, b"")
        self.assertEqual(self.client.get(path).status_code, 404)
        self.assertEqual(self.client.get(notes_path).json(), [])

    def test_missing_checklist(self):
        for method, path, body in [
            ("get", "/checklists/999", None),
            ("put", "/checklists/999", {"name": "Missing"}),
            ("delete", "/checklists/999", None),
            ("get", "/checklists/999/notes", None),
            ("post", "/checklists/999/notes", {"content": "Missing"}),
            ("get", "/checklists/999/notes/1", None),
            ("put", "/checklists/999/notes/1", {"content": "Missing"}),
            ("delete", "/checklists/999/notes/1", None),
        ]:
            with self.subTest(method=method, path=path):
                response = self.client.request(method, path, json=body)
                self.assertEqual(response.status_code, 404)
                self.assertEqual(response.json()["detail"], "Checklist not found")

    def test_missing_note_and_wrong_checklist(self):
        for note_id in (999, 4):
            path = f"/checklists/1/notes/{note_id}"
            for method in ("get", "put", "delete"):
                with self.subTest(note_id=note_id, method=method):
                    body = {"content": "Updated"} if method == "put" else None
                    response = self.client.request(method, path, json=body)
                    self.assertEqual(response.status_code, 404)
                    self.assertEqual(response.json()["detail"], "Note not found")
        self.assertEqual(self.client.get("/checklists/2/notes/4").json()["content"], "Do the laundry")

    def test_invalid_checklist_bodies(self):
        original = self.client.get("/checklists").json()
        for body in ({}, {"name": ""}, {"name": "  "}, {"name": None}, {"name": "A", "id": 7}):
            for method, path in (("post", "/checklists"), ("put", "/checklists/1")):
                with self.subTest(body=body, method=method):
                    self.assertEqual(self.client.request(method, path, json=body).status_code, 422)
        self.assertEqual(self.client.get("/checklists").json(), original)

    def test_invalid_note_bodies(self):
        original = self.client.get("/checklists/1/notes").json()
        for body in (
            {}, {"content": ""}, {"content": "  "}, {"content": None},
            {"content": "Milk", "is_checked": None},
            {"content": "Milk", "is_checked": "invalid"},
            {"content": "Milk", "created_at": "2026-11-15"},
        ):
            for method, path in (
                ("post", "/checklists/1/notes"), ("put", "/checklists/1/notes/1")
            ):
                with self.subTest(body=body, method=method):
                    self.assertEqual(self.client.request(method, path, json=body).status_code, 422)
        self.assertEqual(self.client.get("/checklists/1/notes").json(), original)

    def test_ids_are_not_reused_after_deletion(self):
        first = self.create_checklist()
        self.client.delete(f"/checklists/{first['id']}")
        second = self.create_checklist()
        self.assertGreater(second["id"], first["id"])
        path = f"/checklists/{second['id']}/notes"
        first_note = self.client.post(path, json={"content": "First"}).json()
        self.client.delete(f"{path}/{first_note['id']}")
        second_note = self.client.post("/checklists/1/notes", json={"content": "Second"}).json()
        self.assertGreater(second_note["id"], first_note["id"])

    def test_checklist_delete_removes_its_notes(self):
        response = self.client.delete("/checklists/1")
        self.assertEqual(response.status_code, 204)
        self.assertEqual(self.client.get("/checklists/1/notes/1").status_code, 404)
        remaining_ids = {note["id"] for note in self.client.get("/notes").json()["notes"]}
        self.assertEqual(remaining_ids, {4, 5, 6})

    def test_put_replaces_editable_note_fields(self):
        response = self.client.put("/checklists/1/notes/1", json={"content": "Updated"})
        self.assertEqual(response.status_code, 200)
        self.assertFalse(response.json()["is_checked"])

    def test_root_healthcheck(self):
        response = self.client.get("/")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.headers["content-type"], "application/json")
        self.assertEqual(response.json(), {"status": "ok"})

    def test_static_page_is_removed(self):
        self.assertEqual(self.client.get("/temp_web.html").status_code, 404)
        self.assertEqual(self.client.get("/static/temp_web.html").status_code, 404)

    def test_docs_and_openapi(self):
        self.assertEqual(self.client.get("/docs").status_code, 200)
        schema = self.client.get("/openapi.json").json()
        self.assertIn("post", schema["paths"]["/checklists"])
        self.assertIn("put", schema["paths"]["/checklists/{checklist_id}/notes/{note_id}"])
        self.assertEqual(self.client.get("/checklists/not-an-id").status_code, 422)


if __name__ == "__main__":
    unittest.main()
