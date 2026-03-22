"use client";

import { useParams } from "next/navigation";
import { useSession } from "next-auth/react";
import { useEffect, useState, useCallback } from "react";
import {
  getChecklist,
  getProgress,
  toggleComplete,
  type ChecklistStep,
  type UserProgress,
} from "@/lib/api";
import { ChecklistItem } from "@/components/ChecklistItem";

export default function ChecklistDetailPage() {
  const params = useParams<{ visaTypeId: string }>();
  const { data: session } = useSession();
  const [steps, setSteps] = useState<ChecklistStep[]>([]);
  const [progress, setProgress] = useState<UserProgress[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchData = useCallback(async () => {
    try {
      const checklist = await getChecklist(params.visaTypeId);
      setSteps(checklist);

      if (session?.idToken) {
        const userProgress = await getProgress(session.idToken);
        setProgress(userProgress);
      }
    } finally {
      setLoading(false);
    }
  }, [params.visaTypeId, session?.idToken]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleToggle = async (procedureId: number, completed: boolean) => {
    if (!session?.idToken) return;
    await toggleComplete(procedureId, !completed, session.idToken);
    await fetchData();
  };

  if (loading) {
    return <p className="text-gray-500">Loading checklist...</p>;
  }

  const completedIds = new Set(progress.map((p) => p.procedureId));

  return (
    <div>
      <h1 className="text-3xl font-bold mb-2">Settlement Checklist</h1>
      <p className="text-gray-600 mb-6">
        {session
          ? "Check off procedures as you complete them."
          : "Sign in to track your progress."}
      </p>
      <ol className="space-y-3">
        {steps.map((step) => (
          <ChecklistItem
            key={step.procedure.id}
            step={step}
            completed={completedIds.has(step.procedure.id)}
            canToggle={!!session?.idToken}
            onToggle={handleToggle}
          />
        ))}
      </ol>
    </div>
  );
}
