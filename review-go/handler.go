package reviewgo

import (
	"context"
	"database/sql"
)

func FirstAction(ctx context.Context, db *sql.DB) (string, error) {
	rows, err := db.QueryContext(ctx, "SELECT action FROM audit_logs ORDER BY id DESC")
	if err != nil {
		return "", err
	}
	if !rows.Next() {
		return "", rows.Err()
	}
	var action string
	err = rows.Scan(&action)
	return action, err
}
