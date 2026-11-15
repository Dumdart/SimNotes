from dataclasses import dataclass
from datetime import datetime


@dataclass
class Note:
    id: int
    content: str
    is_checked: bool
    created_at: datetime


@dataclass
class CheckList:
    id: int
    name: str
    created_at: datetime
    notes: list[Note]


def get_data():
    return [
        CheckList(
            id=1,
            name="Cooking",
            created_at=datetime(2026, 11, 15, 9, 0),
            notes=[
                Note(
                    id=1,
                    content="Milk",
                    is_checked=True,
                    created_at=datetime(2026, 11, 15, 9, 1),
                ),
                Note(
                    id=2,
                    content="Bread",
                    is_checked=False,
                    created_at=datetime(2026, 11, 15, 9, 2),
                ),
                Note(
                    id=3,
                    content="Ginger",
                    is_checked=False,
                    created_at=datetime(2026, 11, 15, 9, 3),
                ),
            ],
        ),
        CheckList(
            id=2,
            name="Weekend tasks",
            created_at=datetime(2026, 11, 15, 10, 0),
            notes=[
                Note(
                    id=4,
                    content="Do the laundry",
                    is_checked=True,
                    created_at=datetime(2026, 11, 15, 10, 1),
                ),
                Note(
                    id=5,
                    content="Water the plants",
                    is_checked=False,
                    created_at=datetime(2026, 11, 15, 10, 2),
                ),
                Note(
                    id=6,
                    content="Clean the kitchen",
                    is_checked=False,
                    created_at=datetime(2026, 11, 15, 10, 3),
                ),
            ],
        ),
    ]
