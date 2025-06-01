# 用户状态过滤

给 GET /api/users 增加可选 status，并在列表提供筛选控件。

## 验收

ACTIVE 与 DISABLED 分别返回匹配数据；不传 status 保持现状；切换时回到第一页。
