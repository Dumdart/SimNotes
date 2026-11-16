package tools

import "time"

type mockDB struct{}

var notes = map[string]Note{
	"1": {
		Content:    "Milk",
		Is_checked: false,
	},
	"2": {
		Content:    "Sausage",
		Is_checked: true,
	},
	"3": {
		Content:    "Bread",
		Is_checked: false,
	},
}

func (d *mockDB) GetAllNotes() *[]Note {
	time.Sleep(time.Second * 2)
	return &[]Note{}
}
