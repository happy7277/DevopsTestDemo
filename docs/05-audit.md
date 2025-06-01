# 审计日志

已在基线实现。

## 需求背景

运营需要追溯敏感操作的执行者和目标。

## 功能要求

- 记录成功登录、创建、更新、删除和导出
- 写操作和审计同一事务
- 按最新 ID 倒序查询

## 接口定义

`GET /api/audit?limit=1`

请求参数或请求体

```json
{
  "query": {
    "limit": 1
  }
}
```

响应示例

```json
[
  {
    "id": 7,
    "actor": "admin@example.com",
    "action": "USER_DELETE",
    "targetId": 3,
    "createdAt": "2025-01-01T09:00:00"
  }
]
```

## 验收标准

- [ ] 失败写操作无成功记录
- [ ] 记录含操作者、时间和可选对象 ID
- [ ] limit 超过 100 返回 400
