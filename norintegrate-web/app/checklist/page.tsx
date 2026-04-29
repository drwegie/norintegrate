import { getVisaTypes } from "@/lib/api";
import { VisaTypeCard } from "@/components/VisaTypeCard";
import { getTranslations } from "next-intl/server";

export default async function ChecklistPage() {
  const [visaTypes, t] = await Promise.all([
    getVisaTypes(),
    getTranslations("VisaSelector"),
  ]);

  return (
    <div>
      <h1 className="text-3xl font-bold mb-2">{t("heading")}</h1>
      <p className="text-gray-600 mb-8">{t("description")}</p>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {visaTypes.map((vt) => (
          <VisaTypeCard key={vt.id} visaType={vt} />
        ))}
      </div>
    </div>
  );
}
