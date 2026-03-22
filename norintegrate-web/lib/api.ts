const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export interface VisaType {
  id: string;
  name: string;
  description: string;
}

export interface Procedure {
  id: number;
  name: string;
  description: string;
  estimatedDays: number | null;
  officialUrl: string | null;
  documents: DocumentRequirement[];
}

export interface DocumentRequirement {
  id: number;
  name: string;
  description: string;
  url: string | null;
}

export interface ChecklistStep {
  procedure: Procedure;
  displayOrder: number;
  isNextStep: boolean;
}

export interface UserProgress {
  procedureId: number;
  completedAt: string;
}

async function apiFetch<T>(path: string, idToken?: string): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  if (idToken) {
    headers["Authorization"] = `Bearer ${idToken}`;
  }
  const res = await fetch(`${API_BASE}${path}`, { headers, cache: "no-store" });
  if (!res.ok) {
    throw new Error(`API error: ${res.status} ${res.statusText}`);
  }
  return res.json();
}

export function getVisaTypes(): Promise<VisaType[]> {
  return apiFetch("/api/v1/visa-types");
}

export function getChecklist(visaTypeId: string): Promise<ChecklistStep[]> {
  return apiFetch(`/api/v1/checklist/${visaTypeId}`);
}

export function getProgress(idToken: string): Promise<UserProgress[]> {
  return apiFetch("/api/v1/progress", idToken);
}

export async function toggleComplete(
  procedureId: number,
  complete: boolean,
  idToken: string
): Promise<void> {
  const method = complete ? "POST" : "DELETE";
  const res = await fetch(
    `${API_BASE}/api/v1/progress/${procedureId}/complete`,
    {
      method,
      headers: { Authorization: `Bearer ${idToken}` },
    }
  );
  if (!res.ok) {
    throw new Error(`API error: ${res.status} ${res.statusText}`);
  }
}
