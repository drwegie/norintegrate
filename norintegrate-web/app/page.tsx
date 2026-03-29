import Link from "next/link";
import { Suspense } from "react";
import { SessionExpiredBanner } from "@/components/SessionExpiredBanner";

export default function Home() {
  return (
    <div className="text-center py-16">
      <Suspense>
        <SessionExpiredBanner />
      </Suspense>
      <h1 className="text-4xl font-bold mb-4">Welcome to NorIntegrate</h1>
      <p className="text-lg text-gray-600 mb-8 max-w-2xl mx-auto">
        A step-by-step guide to help immigrants navigate the settlement process
        in Norway. Track your progress through visa procedures, required
        documents, and official registrations.
      </p>
      <Link
        href="/checklist"
        className="inline-block bg-blue-600 text-white px-8 py-3 rounded-lg text-lg font-medium hover:bg-blue-700 transition-colors"
      >
        Get Started
      </Link>
    </div>
  );
}
