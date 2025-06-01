import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Button, Card, Layout, Segmented, Space, Typography } from 'antd';
import { Login } from './views/Login';
import { Users } from './views/Users';
import { Dashboard } from './views/Dashboard';
import 'antd/dist/reset.css';
import './style.css';

function App() {
  const [token, setToken] = useState(() => sessionStorage.getItem('demo-token') || '');
  const [view, setView] = useState<'用户列表' | '数据看板'>('用户列表');

  function logout() {
    sessionStorage.removeItem('demo-token');
    setToken('');
  }

  useEffect(() => {
    window.addEventListener('session-expired', logout);
    return () => window.removeEventListener('session-expired', logout);
  }, []);

  if (!token) {
    return <main className="login"><Card><Login onLogin={(value) => {
      sessionStorage.setItem('demo-token', value);
      setToken(value);
    }} /></Card></main>;
  }
  return <Layout className="shell">
    <header>
      <Typography.Title level={3}>用户运营台</Typography.Title>
      <Space wrap>
        <Segmented options={['用户列表', '数据看板']} value={view} onChange={setView} />
        <Button onClick={logout}>退出登录</Button>
      </Space>
    </header>
    <Layout.Content className="content">
      {view === '用户列表' ? <Users token={token} /> : <Dashboard token={token} />}
    </Layout.Content>
  </Layout>;
}

createRoot(document.getElementById('root')!).render(<App />);
