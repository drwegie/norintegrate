"use client";

import { useLocale, useTranslations } from "next-intl";
import { setLocale } from "@/app/actions/locale";
import { SUPPORTED_LOCALES } from "@/i18n/locales";
import type { Locale } from "@/i18n/locales";

export function LocaleSwitcher() {
  const locale = useLocale();
  const t = useTranslations("Locale");

  return (
    <select
      value={locale}
      onChange={(e) => setLocale(e.target.value as Locale)}
      aria-label={t("label")}
      className="text-sm bg-transparent border border-gray-300 rounded px-2 py-1 text-gray-600"
    >
      {SUPPORTED_LOCALES.map((loc) => (
        <option key={loc} value={loc}>
          {t(loc)}
        </option>
      ))}
    </select>
  );
}
