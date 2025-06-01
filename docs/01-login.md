# 用户登录

已在基线实现。

## 需求背景

运营登录后才能访问信息。

## 功能要求

- 邮箱密码校验
- 签发一小时 JWT
- 成功时写审计

## 接口定义

`POST /api/auth/login`

请求参数或请求体

```json
{
  "email": "admin@example.com",
  "password": "example-password"
}
```

响应示例

```json
{
  "token": "example.jwt.value",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

## 验收标准

- [ ] 正确凭证返回 200
- [ ] 错误密码、篡改或到期令牌返回 401
- [ ] 退出回登录页
