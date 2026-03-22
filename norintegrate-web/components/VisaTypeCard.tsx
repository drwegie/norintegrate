import Link from "next/link";
import type { VisaType } from "@/lib/api";

export function VisaTypeCard({ visaType }: { visaType: VisaType }) {
  return (
    <Link
      href={`/checklist/${visaType.id}`}
      className="block bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow"
    >
      <h2 className="text-lg font-semibold mb-2">{visaType.name}</h2>
      <p className="text-sm text-gray-600">{visaType.description}</p>
    </Link>
  );
}
