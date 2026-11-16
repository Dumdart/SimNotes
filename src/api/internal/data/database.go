package tools

type Note struct {
	//Id         int64
	Content    string
	Is_checked bool
	// created_at time
}

type CheckList struct {
	// Id   int
	Name string
	// Created_at time
	Notes []Note
}

type DatabaseInterface interface {
	GetAllNotes() *[]Note
	SetupDatabase() error
}

func NewDatabase() (*DatabaseInterface, error) {
	var db DatabaseInterface = &mockDB{}

	err := db.SetupDatabase()

	if err != nil {
		return nil, err
	}

	return &db, nil

}
