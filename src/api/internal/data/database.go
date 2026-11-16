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
}
