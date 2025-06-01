import { expect, test } from '@playwright/test';

test.beforeEach(async ({ page }) => {
  await page.route('**/api/auth/login', (route) => route.fulfill({ json: { token: 'example-token' } }));
  await page.route('**/api/users?**', (route) => route.fulfill({ json: {
    items: [{ id: 1, name: 'Demo Ada', email: 'ada@example.com', status: 'ACTIVE' }],
    total: 1, page: 1, size: 10,
  } }));
  await page.route('**/api/dashboard?**', (route) => route.fulfill({ json: [] }));
  await page.goto('/');
});

test('signs in, views users and opens dashboard', async ({ page }) => {
  await page.getByLabel('邮箱').fill('admin@example.com');
  await page.getByLabel('密码').fill('example-password');
  await page.getByRole('button', { name: '登录', exact: true }).click();
  await expect(page.getByText('ada@example.com')).toBeVisible();
  await page.getByText('数据看板', { exact: true }).click();
  await expect(page.getByRole('img', { name: '新增用户趋势' })).toBeVisible();
  await page.getByRole('button', { name: '退出登录' }).click();
  await expect(page.getByRole('heading', { name: '登录' })).toBeVisible();
});
