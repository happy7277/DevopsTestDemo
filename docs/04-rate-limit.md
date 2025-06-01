# API 限流

已在基线实现。

## 需求背景

服务需要应对短时突发流量。

## 功能要求

- 60 秒窗口默认允许 120 次
- 已认证按 JWT subject，未认证按来源 IP
- 原子计数，Redis 异常返回 503

## 接口定义

`GET /api/users`

请求参数或请求体

```json
{
  "headers": {
    "Authorization": "Bearer example.jwt.value"
  }
}
```

响应示例（429）

```json
{
  "message": "Rate limit exceeded"
}
```

## 验收标准

- [ ] 第 120 次允许，第 121 次返回 429
- [ ] 429 带 Retry-After
- [ ] 窗口到期可再次访问且健康检查不受限
