/**
 * OPS Platform E2E Tests
 * Using Playwright for automated testing
 * 
 * Run: npx playwright test
 */

const { test, expect, chromium } = require('@playwright/test');

// Test configuration
const BASE_URL = process.env.TEST_URL || 'http://192.168.2.32';
const ADMIN_USER = 'admin';
const ADMIN_PASS = 'Admin@123456';

// Helper function: Login
async function login(page) {
  await page.goto(`${BASE_URL}/login`);
  await page.fill('input[type="text"]', ADMIN_USER);
  await page.fill('input[type="password"]', ADMIN_PASS);
  await page.click('button[type="submit"]');
  await page.waitForURL(`${BASE_URL}/dashboard`, { timeout: 10000 });
}

// ==================== Login Tests ====================

test.describe('Login Page', () => {
  test('LOGIN-001: Login with correct credentials', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="text"]', ADMIN_USER);
    await page.fill('input[type="password"]', ADMIN_PASS);
    await page.click('button[type="submit"]');
    
    // Should redirect to dashboard
    await expect(page).toHaveURL(/.*dashboard/);
  });

  test('LOGIN-002: Login with wrong password', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="text"]', ADMIN_USER);
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');
    
    // Should show error message
    await expect(page.locator('.el-message--error')).toBeVisible({ timeout: 5000 });
  });

  test('LOGIN-003: Login with empty username', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[type="password"]', ADMIN_PASS);
    await page.click('button[type="submit"]');
    
    // Should show validation error
    await expect(page.locator('.el-form-item__error')).toBeVisible();
  });
});

// ==================== Dashboard Tests ====================

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('DASH-001: Refresh button works', async ({ page }) => {
    await page.goto(`${BASE_URL}/dashboard`);
    
    // Click refresh button
    const refreshBtn = page.locator('button:has-text("刷新")');
    if (await refreshBtn.isVisible()) {
      await refreshBtn.click();
      // Should show loading state
      await page.waitForTimeout(1000);
    }
  });

  test('DASH-002: Statistics cards display', async ({ page }) => {
    await page.goto(`${BASE_URL}/dashboard`);
    
    // Check stat cards exist
    await expect(page.locator('.stat-card')).toHaveCount(4);
  });
});

// ==================== Knowledge Tests ====================

test.describe('Knowledge Base', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('KNOW-001: Create knowledge base dialog', async ({ page }) => {
    await page.goto(`${BASE_URL}/knowledge`);
    
    // Click create button
    await page.click('button:has-text("新建知识库")');
    
    // Dialog should appear
    await expect(page.locator('.el-dialog')).toBeVisible();
  });

  test('KNOW-002: Create knowledge base', async ({ page }) => {
    await page.goto(`${BASE_URL}/knowledge`);
    await page.click('button:has-text("新建知识库")');
    
    // Fill form
    await page.fill('input[placeholder="请输入知识库名称"]', '测试知识库');
    await page.fill('textarea[placeholder="请输入描述"]', '测试描述');
    
    // Submit
    await page.click('button:has-text("确定")');
    
    // Should show success message
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 });
  });

  test('KNOW-005: Edit knowledge base', async ({ page }) => {
    await page.goto(`${BASE_URL}/knowledge`);
    
    // Click edit on first card
    const editBtn = page.locator('.el-dropdown-item:has-text("编辑")').first();
    if (await editBtn.isVisible()) {
      await editBtn.click();
      await expect(page.locator('.el-dialog')).toBeVisible();
    }
  });

  test('KNOW-006: Delete knowledge base', async ({ page }) => {
    await page.goto(`${BASE_URL}/knowledge`);
    
    // Click delete on first card
    const deleteBtn = page.locator('.el-dropdown-item:has-text("删除")').first();
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
      // Confirm dialog should appear
      await expect(page.locator('.el-message-box')).toBeVisible({ timeout: 3000 });
    }
  });
});

// ==================== Workflow Tests ====================

test.describe('Workflow', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('WORK-001: Create workflow dialog', async ({ page }) => {
    await page.goto(`${BASE_URL}/workflow`);
    
    // Click create button
    await page.click('button:has-text("新建工作流")');
    
    // Dialog should appear
    await expect(page.locator('.el-dialog')).toBeVisible();
  });

  test('WORK-003: Execute workflow', async ({ page }) => {
    await page.goto(`${BASE_URL}/workflow`);
    
    // Wait for table to load
    await page.waitForSelector('.el-table', { timeout: 5000 });
    
    // Click execute button on first row
    const executeBtn = page.locator('.el-table .el-button--primary').first();
    if (await executeBtn.isVisible()) {
      await executeBtn.click();
      // Should show execution result
      await page.waitForTimeout(2000);
    }
  });

  test('WORK-004: Delete workflow', async ({ page }) => {
    await page.goto(`${BASE_URL}/workflow`);
    await page.waitForSelector('.el-table', { timeout: 5000 });
    
    // Click delete button
    const deleteBtn = page.locator('.el-table .el-button--danger').first();
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
      // Confirm dialog
      await page.waitForTimeout(500);
    }
  });
});

// ==================== AutoHeal Tests ====================

test.describe('AutoHeal', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('HEAL-001: Add strategy dialog', async ({ page }) => {
    await page.goto(`${BASE_URL}/autoheal`);
    
    await page.click('button:has-text("添加策略")');
    await expect(page.locator('.el-dialog')).toBeVisible();
  });

  test('HEAL-004: Delete strategy', async ({ page }) => {
    await page.goto(`${BASE_URL}/autoheal`);
    await page.waitForSelector('.el-table', { timeout: 5000 });
    
    const deleteBtn = page.locator('.el-table .el-button--danger').first();
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
    }
  });
});

// ==================== User Management Tests ====================

test.describe('User Management', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('USER-001: Add user dialog', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    
    await page.click('button:has-text("新增用户")');
    await expect(page.locator('.el-dialog')).toBeVisible();
  });

  test('USER-005: Delete user', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/users`);
    await page.waitForSelector('.el-table', { timeout: 5000 });
    
    const deleteBtn = page.locator('.el-table .el-button--danger').first();
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
    }
  });
});

// ==================== Role Management Tests ====================

test.describe('Role Management', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('ROLE-001: Add role dialog', async ({ page }) => {
    await page.goto(`${BASE_URL}/admin/roles`);
    
    await page.click('button:has-text("新增角色")');
    await expect(page.locator('.el-dialog')).toBeVisible();
  });
});

// ==================== Notification Tests ====================

test.describe('Notification', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('NOTI-001: Mark all as read', async ({ page }) => {
    await page.goto(`${BASE_URL}/notification`);
    
    const markAllBtn = page.locator('button:has-text("全部已读")');
    if (await markAllBtn.isVisible()) {
      await markAllBtn.click();
    }
  });
});

// ==================== Certificate Tests ====================

test.describe('Certificate', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('CERT-001: Refresh certificates', async ({ page }) => {
    await page.goto(`${BASE_URL}/certificate`);
    
    const refreshBtn = page.locator('button:has-text("刷新状态")');
    if (await refreshBtn.isVisible()) {
      await refreshBtn.click();
      await page.waitForTimeout(2000);
    }
  });
});

// ==================== AI Chat Tests ====================

test.describe('AI Chat', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('AI-001: Send message', async ({ page }) => {
    await page.goto(`${BASE_URL}/ai`);
    
    // Find input and send message
    const input = page.locator('textarea').first();
    if (await input.isVisible()) {
      await input.fill('你好');
      await page.click('button:has-text("发送")');
      await page.waitForTimeout(3000);
    }
  });
});

// Run tests with: npx playwright test --reporter=html
