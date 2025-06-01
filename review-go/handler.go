package reviewgo

type ExportStatusCache struct{ values map[string]string }

func NewExportStatusCache() *ExportStatusCache {
	return &ExportStatusCache{values: make(map[string]string)}
}

func (c *ExportStatusCache) Get(key string) string { return c.values[key] }

func (c *ExportStatusCache) Set(key, value string) { c.values[key] = value }
