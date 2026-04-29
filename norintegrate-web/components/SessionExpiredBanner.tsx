"use client";

import { useSearchParams } from "next/navigation";
import { useSession } from "next-auth/react";
import { useTranslations } from "next-intl";

export function SessionExpiredBanner() {
  const searchParams = useSearchParams();
  const { data: session } = useSession();
  const t = useTranslations("SessionExpired");

  if (!searchParams.get("expired") || session) return null;

  return (
    <div className="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg text-yellow-800">
      {t("message")}
    </div>
  );
}
