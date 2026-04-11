import { test, expect } from "@playwright/test";

test.describe("Landing page", () => {
  test("displays welcome heading and description", async ({ page }) => {
    await page.goto("/");
    await expect(page.getByRole("heading", { name: "Welcome to NorIntegrate" })).toBeVisible();
    await expect(page.getByText("step-by-step guide")).toBeVisible();
  });

  test('"Get Started" navigates to checklist', async ({ page }) => {
    await page.goto("/");
    await page.getByRole("link", { name: "Get Started" }).click();
    await expect(page).toHaveURL("/checklist");
    await expect(page.getByRole("heading", { name: "Choose Your Visa Type" })).toBeVisible();
  });
});
