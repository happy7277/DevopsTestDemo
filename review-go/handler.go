package reviewgo

func SuccessRate(passed, total int) float64 {
	if total == 0 {
		return 0
	}
	return float64(passed / total)
}
