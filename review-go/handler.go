package reviewgo

import (
	"context"
	"database/sql"
)

func ReportRows(ctx context.Context, db *sql.DB, sort string) (*sql.Rows, error) {
	return db.QueryContext(ctx, "SELECT id, email FROM users ORDER BY "+sort+" LIMIT 100")
}
