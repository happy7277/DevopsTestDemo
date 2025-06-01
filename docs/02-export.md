# 数据导出

已在基线实现。

## 需求背景

运营需要下载检索条件匹配的用户清单。

## 功能要求

- 复用用户列表筛选
- 四列 ID、Name、Email、Status 均为文本
- 成功导出写审计

## 接口定义

`GET /api/users/export?q=ada`

请求参数或请求体

```json
{
  "query": {
    "q": "ada"
  }
}
```

响应示例

```json
{
  "status": 200,
  "contentType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  "filename": "users.xlsx"
}
```

## 验收标准

- [ ] 实际响应为二进制 XLSX，JSON 描述 HTTP 元数据
- [ ] 空结果仍保留表头
- [ ] 超 10000 行返回 422
