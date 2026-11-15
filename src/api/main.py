from datetime import UTC, datetime
from itertools import count

from fastapi import FastAPI, HTTPException, Response
from pydantic import BaseModel, ConfigDict, Field

from api.data import CheckList, Note, get_data

app = FastAPI()

data = get_data()


class CheckListInput(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)

    name: str = Field(min_length=1)


class NoteInput(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)

    content: str = Field(min_length=1)
    is_checked: bool = False


next_checklist_id = count(max((item.id for item in data), default=0) + 1)
next_note_id = count(
    max((note.id for checklist in data for note in checklist.notes), default=0) + 1
)


def find_checklist(checklist_id: int) -> CheckList:
    for checklist in data:
        if checklist.id == checklist_id:
            return checklist
    raise HTTPException(status_code=404, detail="Checklist not found")


def find_note(checklist: CheckList, note_id: int) -> Note:
    for note in checklist.notes:
        if note.id == note_id:
            return note
    raise HTTPException(status_code=404, detail="Note not found")


@app.get("/notes")
async def get_notes() -> dict[str, list[Note]]:
    return {"notes": [note for checklist in data for note in checklist.notes]}


@app.get("/checklists")
async def get_checklists() -> list[CheckList]:
    return data


@app.post("/checklists", status_code=201)
async def create_checklist(body: CheckListInput) -> CheckList:
    checklist = CheckList(
        id=next(next_checklist_id),
        name=body.name,
        created_at=datetime.now(UTC),
        notes=[],
    )
    data.append(checklist)
    return checklist


@app.get("/checklists/{checklist_id}")
async def get_checklist(checklist_id: int) -> CheckList:
    return find_checklist(checklist_id)


@app.put("/checklists/{checklist_id}")
async def update_checklist(checklist_id: int, body: CheckListInput) -> CheckList:
    checklist = find_checklist(checklist_id)
    checklist.name = body.name
    return checklist


@app.delete("/checklists/{checklist_id}", status_code=204)
async def delete_checklist(checklist_id: int) -> Response:
    checklist = find_checklist(checklist_id)
    data.remove(checklist)
    return Response(status_code=204)


@app.get("/checklists/{checklist_id}/notes")
async def get_checklist_notes(checklist_id: int) -> list[Note]:
    return find_checklist(checklist_id).notes


@app.post("/checklists/{checklist_id}/notes", status_code=201)
async def create_note(checklist_id: int, body: NoteInput) -> Note:
    checklist = find_checklist(checklist_id)
    note = Note(
        id=next(next_note_id),
        content=body.content,
        is_checked=body.is_checked,
        created_at=datetime.now(UTC),
    )
    checklist.notes.append(note)
    return note


@app.get("/checklists/{checklist_id}/notes/{note_id}")
async def get_note(checklist_id: int, note_id: int) -> Note:
    return find_note(find_checklist(checklist_id), note_id)


@app.put("/checklists/{checklist_id}/notes/{note_id}")
async def update_note(checklist_id: int, note_id: int, body: NoteInput) -> Note:
    note = find_note(find_checklist(checklist_id), note_id)
    note.content = body.content
    note.is_checked = body.is_checked
    return note


@app.delete("/checklists/{checklist_id}/notes/{note_id}", status_code=204)
async def delete_note(checklist_id: int, note_id: int) -> Response:
    checklist = find_checklist(checklist_id)
    note = find_note(checklist, note_id)
    checklist.notes.remove(note)
    return Response(status_code=204)


@app.get("/")
async def healthcheck() -> dict[str, str]:
    return {"status": "ok"}
