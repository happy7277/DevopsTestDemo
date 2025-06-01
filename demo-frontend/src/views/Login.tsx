import { useState } from 'react';
import { Alert, Button, Form, Input, Typography } from 'antd';
import { login } from '../api';

export function Login({ onLogin }: { onLogin: (token: string) => void }) {
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(value: { email: string; password: string }) {
    setError('');
    setLoading(true);
    try {
      onLogin((await login(value.email, value.password)).token);
    } catch (cause) {
      setError((cause as Error).message);
    } finally {
      setLoading(false);
    }
  }

  return <>
    <Typography.Title level={2}>登录</Typography.Title>
    {error && <Alert type="error" message={error} role="alert" />}
    <Form layout="vertical" onFinish={submit}>
      <Form.Item name="email" label="邮箱" rules={[{ required: true, type: 'email' }]}>
        <Input autoComplete="username" />
      </Form.Item>
      <Form.Item name="password" label="密码" rules={[{ required: true }]}>
        <Input.Password autoComplete="current-password" />
      </Form.Item>
      <Button type="primary" htmlType="button" aria-label="登录" loading={loading}>登录</Button>
    </Form>
  </>;
}
