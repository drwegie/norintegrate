import Link from "next/link";
import { Suspense } from "react";
import { SessionExpiredBanner } from "@/components/SessionExpiredBanner";
import { getTranslations } from "next-intl/server";

export default async function Home() {
  const t = await getTranslations("Landing");

  return (
    <div className="text-center py-16">
      <Suspense>
        <SessionExpiredBanner />
      </Suspense>
      <h1 className="text-4xl font-bold mb-4">{t("heading")}</h1>
      <p className="text-lg text-gray-600 mb-8 max-w-2xl mx-auto">
        {t("description")}
      </p>
      <Link
        href="/checklist"
        className="inline-block bg-blue-600 text-white px-8 py-3 rounded-lg text-lg font-medium hover:bg-blue-700 transition-colors"
      >
        {t("cta")}
      </Link>
    </div>
  );
}
