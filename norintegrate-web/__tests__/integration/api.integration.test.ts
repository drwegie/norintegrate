import { describe, it, expect, beforeAll } from "vitest";
import { getVisaTypes, getChecklist } from "@/lib/api";

beforeAll(async () => {
  try {
    await fetch("http://localhost:8080/api/v1/visa-types");
  } catch {
    throw new Error(
      "API is not reachable at http://localhost:8080. Start PostgreSQL and the API before running integration tests."
    );
  }
});

describe("API Integration: getVisaTypes", () => {
  it("returns an array of visa types", async () => {
    const types = await getVisaTypes();
    expect(types.length).toBeGreaterThan(0);
  });

  it("each visa type has the expected shape", async () => {
    const types = await getVisaTypes();
    for (const t of types) {
      expect(t).toHaveProperty("id");
      expect(t).toHaveProperty("name");
      expect(t).toHaveProperty("description");
      expect(typeof t.id).toBe("string");
      expect(typeof t.name).toBe("string");
      expect(typeof t.description).toBe("string");
    }
  });

  it("includes known visa types from seed data", async () => {
    const types = await getVisaTypes();
    const ids = types.map((t) => t.id);
    expect(ids).toContain("SKILLED_WORKER");
    expect(ids).toContain("FAMILY_REUNIFICATION");
    expect(ids).toContain("STUDENT");
  });
});

describe("API Integration: getChecklist", () => {
  it("returns checklist items for SKILLED_WORKER", async () => {
    const items = await getChecklist("SKILLED_WORKER");
    expect(items.length).toBeGreaterThan(0);
  });

  it("each item has the expected shape", async () => {
    const items = await getChecklist("SKILLED_WORKER");
    for (const item of items) {
      expect(typeof item.procedureId).toBe("number");
      expect(typeof item.title).toBe("string");
      expect(typeof item.authority).toBe("string");
      expect(typeof item.isNext).toBe("boolean");
      expect(Array.isArray(item.documents)).toBe(true);
      // description is nullable
      expect(
        item.description === null || typeof item.description === "string"
      ).toBe(true);
      // estimatedDays is nullable
      expect(
        item.estimatedDays === null || typeof item.estimatedDays === "number"
      ).toBe(true);
    }
  });

  it("documents have the expected shape", async () => {
    const items = await getChecklist("SKILLED_WORKER");
    const withDocs = items.filter((i) => i.documents.length > 0);
    expect(withDocs.length).toBeGreaterThan(0);
    for (const item of withDocs) {
      for (const doc of item.documents) {
        expect(typeof doc.documentName).toBe("string");
        expect(typeof doc.mandatory).toBe("boolean");
      }
    }
  });

  it("has at least one next step marked", async () => {
    const items = await getChecklist("SKILLED_WORKER");
    const nextSteps = items.filter((i) => i.isNext);
    expect(nextSteps.length).toBeGreaterThanOrEqual(1);
  });

  it("filters out completed procedures", async () => {
    const all = await getChecklist("SKILLED_WORKER");
    const firstId = all[0].procedureId;
    const filtered = await getChecklist("SKILLED_WORKER", [firstId]);
    expect(filtered.length).toBeLessThan(all.length);
    expect(filtered.find((i) => i.procedureId === firstId)).toBeUndefined();
  });

  it("returns items for all visa types", async () => {
    const types = await getVisaTypes();
    for (const t of types) {
      const items = await getChecklist(t.id);
      expect(items.length).toBeGreaterThan(0);
    }
  });
});
