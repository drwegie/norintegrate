import { test, expect } from "@playwright/test";

test.describe("Checklist detail page", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/checklist/SKILLED_WORKER");
    await expect(page.getByRole("heading", { name: "Settlement Checklist" })).toBeVisible();
  });

  test("renders checklist items", async ({ page }) => {
    const items = page.locator("ol > li");
    await expect(items.first()).toBeVisible();
    expect(await items.count()).toBeGreaterThan(0);
  });

  test("shows sign-in prompt for unauthenticated users", async ({ page }) => {
    await expect(page.getByText("Sign in to track your progress")).toBeVisible();
  });

  test("items display title and authority", async ({ page }) => {
    const firstItem = page.locator("ol > li").first();
    // Each item should have a title (h3) and authority text
    await expect(firstItem.locator("h3")).toBeVisible();
    await expect(firstItem.locator("p")).toBeVisible();
  });

  test("at least one item has the 'Next step' badge", async ({ page }) => {
    await expect(page.getByText("Next step").first()).toBeVisible();
  });

  test("expanding an item shows details", async ({ page }) => {
    // Click the first expandable item
    const firstItem = page.locator("ol > li").first();
    await firstItem.click();

    // After expansion, the detail section should appear (description or documents)
    const detailSection = firstItem.locator(".border-t");
    await expect(detailSection).toBeVisible();
  });
});
