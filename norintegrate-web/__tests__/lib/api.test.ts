import { describe, it, expect, vi, beforeEach } from "vitest";
import {
  getVisaTypes,
  getChecklist,
  getProgress,
  toggleComplete,
} from "@/lib/api";

const mockFetch = vi.fn();
global.fetch = mockFetch;

beforeEach(() => {
  vi.clearAllMocks();
});

describe("getVisaTypes", () => {
  it("calls the correct URL and returns parsed JSON", async () => {
    const data = [{ id: "SKILLED_WORKER", name: "Skilled Worker", description: "desc" }];
    mockFetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(data),
    });
    const result = await getVisaTypes();
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/visa-types",
      expect.objectContaining({
        headers: expect.objectContaining({ "Content-Type": "application/json" }),
      })
    );
    expect(result).toEqual(data);
  });
});

describe("getChecklist", () => {
  it("calls the correct URL with visaTypeId", async () => {
    const data = [{ procedure: { id: 1 }, displayOrder: 1, isNextStep: true }];
    mockFetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(data),
    });
    const result = await getChecklist("FAMILY_IMMIGRATION");
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/checklist/FAMILY_IMMIGRATION",
      expect.any(Object)
    );
    expect(result).toEqual(data);
  });
});

describe("getProgress", () => {
  it("sends Authorization header with Bearer token", async () => {
    const data = [{ procedureId: 1, completedAt: "2024-01-01T00:00:00Z" }];
    mockFetch.mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(data),
    });
    await getProgress("my-id-token");
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/progress",
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: "Bearer my-id-token",
        }),
      })
    );
  });
});

describe("toggleComplete", () => {
  it("uses POST when complete is true", async () => {
    mockFetch.mockResolvedValue({ ok: true });
    await toggleComplete(5, true, "token");
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/progress/5/complete",
      expect.objectContaining({ method: "POST" })
    );
  });

  it("uses DELETE when complete is false", async () => {
    mockFetch.mockResolvedValue({ ok: true });
    await toggleComplete(5, false, "token");
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/progress/5/complete",
      expect.objectContaining({ method: "DELETE" })
    );
  });
});

describe("API error handling", () => {
  it("throws an Error when response is not ok", async () => {
    mockFetch.mockResolvedValue({
      ok: false,
      status: 500,
      statusText: "Internal Server Error",
    });
    await expect(getVisaTypes()).rejects.toThrow(
      "API error: 500 Internal Server Error"
    );
  });
});
