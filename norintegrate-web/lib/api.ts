const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export interface VisaType {
  id: string;
  name: string;
  description: string;
}

export interface ChecklistDocumentResponse {
  documentName: string;
  mandatory: boolean;
}

export interface ChecklistItemResponse {
  procedureId: number;
  title: string;
  description: string | null;
  authority: string;
  estimatedDays: number | null;
  isNext: boolean;
  documents: ChecklistDocumentResponse[];
}

export interface ChecklistResponse {
  visaTypeId: string;
  items: ChecklistItemResponse[];
}

export interface UserProgress {
  procedureId: number;
  completed: boolean;
  completedAt: string | null;
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

export async function getChecklist(
  visaTypeId: string,
  completedIds?: number[]
): Promise<ChecklistItemResponse[]> {
  const params =
    completedIds && completedIds.length > 0 ? `?completed=${completedIds.join(",")}` : "";
  const res = await apiFetch<ChecklistResponse>(
    `/api/v1/checklist/${visaTypeId}${params}`
  );
  return res.items;
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
