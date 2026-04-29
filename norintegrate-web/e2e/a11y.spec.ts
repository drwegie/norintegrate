import { test, expect } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";

test.describe("Accessibility", () => {
  test("landing page has no critical or serious WCAG 2.0 AA violations", async ({
    page,
  }) => {
    await page.goto("/");
    const results = await new AxeBuilder({ page })
      .withTags(["wcag2a", "wcag2aa"])
      .analyze();
    const critical = results.violations.filter((v) =>
      ["critical", "serious"].includes(v.impact!),
    );
    expect(critical).toEqual([]);
  });

  test("visa selector page has no critical or serious WCAG 2.0 AA violations", async ({
    page,
  }) => {
    await page.goto("/checklist");
    const results = await new AxeBuilder({ page })
      .withTags(["wcag2a", "wcag2aa"])
      .analyze();
    const critical = results.violations.filter((v) =>
      ["critical", "serious"].includes(v.impact!),
    );
    expect(critical).toEqual([]);
  });
});
