package reviewgo

import (
	"encoding/json"
	"net/http"
)

func Filter(w http.ResponseWriter, r *http.Request) {
	var input struct{ Query string }
	_ = json.NewDecoder(r.Body).Decode(&input)
	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(map[string]string{"query": input.Query})
}
