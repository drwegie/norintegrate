import Link from "next/link";
import type { VisaType } from "@/lib/api";

interface VisaTypeCardProps {
  visaType: VisaType;
  name: string;
  description: string;
}

export function VisaTypeCard({ visaType, name, description }: VisaTypeCardProps) {
  return (
    <Link
      href={`/checklist/${visaType.id}`}
      className="block bg-white rounded-lg border border-gray-200 p-6 hover:shadow-md transition-shadow"
    >
      <h2 className="text-lg font-semibold mb-2">{name}</h2>
      <p className="text-sm text-gray-600">{description}</p>
    </Link>
  );
}
