# 按操作筛选审计

给 GET /api/audit 增加按 action 筛选。

## 验收

在 limit 前筛选；未知 action 返回 400；保留默认倒序。
