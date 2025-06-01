import { useEffect, useState } from 'react';
import { Alert, Button, Card, Input, Space, Table, Tag } from 'antd';
import { downloadUsers, users, type User, type UserPage } from '../api';

const columns = [
  { title: '姓名', dataIndex: 'name' },
  { title: '邮箱', dataIndex: 'email' },
  { title: '状态', dataIndex: 'status', render: (status: User['status']) =>
    <Tag color={status === 'ACTIVE' ? 'green' : 'default'}>{status === 'ACTIVE' ? '启用' : '停用'}</Tag> },
];

export function Users({ token }: { token: string }) {
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [data, setData] = useState<UserPage>();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    let current = true;
    setLoading(true);
    setError('');
    users(token, page, 10, query)
      .then((result) => { if (current) setData(result); })
      .catch((cause: Error) => { if (current) setError(cause.message); })
      .finally(() => { if (current) setLoading(false); });
    return () => { current = false; };
  }, [token, page, query]);

  return <Card title="用户列表">
    <Space wrap className="toolbar">
      <Input.Search aria-label="搜索用户" placeholder="姓名或邮箱" onSearch={(value) => {
        setPage(1);
        setQuery(value.trim());
      }} />
      <Button onClick={() => downloadUsers(token, query).catch((cause: Error) => setError(cause.message))}>
        导出 CSV
      </Button>
    </Space>
    {error && <Alert type="error" message={error} />}
    <Table rowKey="id" columns={columns} dataSource={data?.items} loading={loading}
      scroll={{ x: 420 }} pagination={{ current: page, total: data?.total || 0,
        pageSize: 10, showSizeChanger: false, onChange: setPage }} />
  </Card>;
}
