package reviewgo

import (
	"context"
	"database/sql"
)

func CustomerID(ctx context.Context, db *sql.DB, email string) (int64, error) {
	var id int64
	err := db.QueryRowContext(ctx, "SELECT id FROM users WHERE email = '"+email+"' LIMIT 1").Scan(&id)
	return id, err
}
