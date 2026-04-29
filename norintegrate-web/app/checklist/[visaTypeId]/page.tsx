"use client";

import { useParams, useRouter } from "next/navigation";
import { signOut, useSession } from "next-auth/react";
import { useEffect, useState, useCallback } from "react";
import { useTranslations } from "next-intl";
import {
  getChecklist,
  getProgress,
  toggleComplete,
  type ChecklistItemResponse,
  type UserProgress,
} from "@/lib/api";
import { ChecklistItem } from "@/components/ChecklistItem";

export default function ChecklistDetailPage() {
  const params = useParams<{ visaTypeId: string }>();
  const router = useRouter();
  const { data: session } = useSession();
  const t = useTranslations("Checklist");
  const [steps, setSteps] = useState<ChecklistItemResponse[]>([]);
  const [progress, setProgress] = useState<UserProgress[]>([]);
  const [loading, setLoading] = useState(true);
  const [nextStepTitles, setNextStepTitles] = useState<string[]>([]);
  const [showCongrats, setShowCongrats] = useState(false);

  const getProcedureTitle = useCallback(
    (procedureId: number, fallback: string) => {
      const key = `procedures.${procedureId}.title`;
      return t.has(key) ? t(key) : fallback;
    },
    [t]
  );

  const handleSessionExpired = useCallback(async () => {
    await signOut({ redirect: false });
    router.push("/?expired=1");
  }, [router]);

  const fetchData = useCallback(async (showNextStep = false) => {
    try {
      if (session?.idToken) {
        try {
          const userProgress = await getProgress(session.idToken);
          setProgress(userProgress);

          const userCompletedIds = userProgress
            .filter((p) => p.completed)
            .map((p) => p.procedureId);

          const checklist = await getChecklist(params.visaTypeId, userCompletedIds);

          const fullChecklist = await getChecklist(params.visaTypeId);
          const remainingIds = new Set(checklist.map((s) => s.procedureId));
          const merged = fullChecklist.map((s) => ({
            ...s,
            isNext: remainingIds.has(s.procedureId)
              ? checklist.find((c) => c.procedureId === s.procedureId)!.isNext
              : false,
          }));
          setSteps(merged);

          if (showNextStep) {
            const allDone = merged.every((s) =>
              userCompletedIds.includes(s.procedureId)
            );
            if (allDone) {
              setShowCongrats(true);
            } else {
              const nextSteps = merged.filter(
                (s) => s.isNext && !userCompletedIds.includes(s.procedureId)
              );
              if (nextSteps.length > 0) {
                setNextStepTitles(
                  nextSteps.map((s) => getProcedureTitle(s.procedureId, s.title))
                );
                setTimeout(() => setNextStepTitles([]), 5000);
              }
            }
          }
        } catch {
          await handleSessionExpired();
          return;
        }
      } else {
        const checklist = await getChecklist(params.visaTypeId);
        setSteps(checklist);
      }
    } finally {
      setLoading(false);
    }
  }, [params.visaTypeId, session?.idToken, handleSessionExpired, getProcedureTitle]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleToggle = async (procedureId: number, completed: boolean) => {
    if (!session?.idToken) return;
    try {
      const checking = !completed;
      await toggleComplete(procedureId, checking, session.idToken);
      await fetchData(checking);
    } catch {
      await handleSessionExpired();
    }
  };

  if (loading) {
    return <p className="text-gray-500">{t("loading")}</p>;
  }

  const completedIds = new Set(
    progress.filter((p) => p.completed).map((p) => p.procedureId)
  );
  return (
    <div>
      <h1 className="text-3xl font-bold mb-2">{t("heading")}</h1>
      <p className="text-gray-600 mb-6">
        {session ? t("descriptionAuth") : t("descriptionGuest")}
      </p>
      {nextStepTitles.length > 0 && (
        <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 bg-blue-600 text-white px-6 py-4 rounded-lg shadow-lg max-w-2xl w-auto">
          <p className="font-medium mb-1">
            {nextStepTitles.length === 1 ? `${t("nextStep")}:` : `${t("nextSteps")}:`}
          </p>
          <ul className="space-y-1">
            {nextStepTitles.map((title) => (
              <li key={title}>- {title}</li>
            ))}
          </ul>
        </div>
      )}
      {showCongrats && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-2xl shadow-xl p-10 max-w-md mx-4 text-center">
            <p className="text-4xl mb-4">&#127881;</p>
            <p className="text-2xl font-bold text-green-800 mb-3">
              {t("congratsTitle")}
            </p>
            <p className="text-gray-600 mb-6">
              {t("congratsMessage")}
            </p>
            <button
              onClick={() => setShowCongrats(false)}
              className="bg-green-600 text-white px-6 py-2 rounded-lg font-medium hover:bg-green-700 transition-colors"
            >
              {t("close")}
            </button>
          </div>
        </div>
      )}
      <ol className="space-y-3">
        {steps.map((step) => (
          <ChecklistItem
            key={step.procedureId}
            step={step}
            completed={completedIds.has(step.procedureId)}
            canToggle={!!session?.idToken}
            onToggle={handleToggle}
          />
        ))}
      </ol>
    </div>
  );
}
