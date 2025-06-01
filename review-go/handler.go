package reviewgo

import "sync"

func Completed(statuses []string) int {
	total := 0
	var workers sync.WaitGroup
	for _, status := range statuses {
		workers.Add(1)
		go func(status string) {
			defer workers.Done()
			if status == "DONE" {
				total++
			}
		}(status)
	}
	workers.Wait()
	return total
}
