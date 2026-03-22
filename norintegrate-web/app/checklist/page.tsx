import { getVisaTypes } from "@/lib/api";
import { VisaTypeCard } from "@/components/VisaTypeCard";

export default async function ChecklistPage() {
  const visaTypes = await getVisaTypes();

  return (
    <div>
      <h1 className="text-3xl font-bold mb-2">Choose Your Visa Type</h1>
      <p className="text-gray-600 mb-8">
        Select your visa category to see the required settlement procedures.
      </p>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {visaTypes.map((vt) => (
          <VisaTypeCard key={vt.id} visaType={vt} />
        ))}
      </div>
    </div>
  );
}
