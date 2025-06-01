export type User = { id: number; name: string; email: string; status: 'ACTIVE' | 'DISABLED'; createdAt: string };
export type UserPage = { items: User[]; total: number; page: number; size: number };
export type Point = { date: string; active: number; disabled: number };

async function request(path: string, token?: string, options: RequestInit = {}) {
  const headers = new Headers(options.headers);
  if (token) headers.set('Authorization', 'Bearer ' + token);
  const response = await fetch('/api' + path, { ...options, headers });
  if (!response.ok) {
    if (response.status === 401 && token) window.dispatchEvent(new Event('session-expired'));
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || '请求失败（' + response.status + '）');
  }
  return response;
}

export async function login(email: string, password: string): Promise<{ token: string }> {
  return (await request('/auth/login', undefined, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })).json();
}

export async function users(token: string, page: number, size: number, query: string): Promise<UserPage> {
  const params = new URLSearchParams({ page: String(page), size: String(size), q: query });
  return (await request('/users?' + params, token)).json();
}

export async function dashboard(token: string, from: string, to: string): Promise<Point[]> {
  const params = new URLSearchParams({ from, to });
  return (await request('/dashboard?' + params, token)).json();
}

export async function downloadUsers(token: string, query: string) {
  const params = new URLSearchParams({ q: query });
  const response = await request('/users/export?' + params, token);
  const url = URL.createObjectURL(await response.blob());
  const link = document.createElement('a');
  link.href = url;
  link.download = 'users.xlsx';
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
