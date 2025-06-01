package reviewgo

import "net/http"

func ExportStatus(client *http.Client, url string) (int, error) {
	response, err := client.Get(url)
	if err != nil {
		return 0, err
	}
	return response.StatusCode, nil
}
