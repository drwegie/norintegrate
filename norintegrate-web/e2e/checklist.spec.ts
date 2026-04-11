import { test, expect } from "@playwright/test";

test.describe("Checklist page — visa type selector", () => {
  test("shows visa type cards", async ({ page }) => {
    await page.goto("/checklist");
    await expect(page.getByRole("heading", { name: "Choose Your Visa Type" })).toBeVisible();

    const cards = page.getByRole("link").filter({ hasText: /Worker|Reunification|Student/i });
    await expect(cards.first()).toBeVisible();
  });

  test("clicking a visa type navigates to checklist detail", async ({ page }) => {
    await page.goto("/checklist");

    // Click the first visa type card link
    const firstCard = page.getByRole("link").filter({ hasText: /Worker|Reunification|Student/i }).first();
    await firstCard.click();

    await expect(page.getByRole("heading", { name: "Settlement Checklist" })).toBeVisible();
  });
});
