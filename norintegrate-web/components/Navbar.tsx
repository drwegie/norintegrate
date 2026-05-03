"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useSession } from "next-auth/react";
import { useTranslations } from "next-intl";
import { LocaleSwitcher } from "./LocaleSwitcher";
import { handleSignIn, handleSignOut } from "@/lib/auth-actions";

export function Navbar() {
  const { data: session } = useSession();
  const pathname = usePathname();
  const t = useTranslations("Navbar");

  return (
    <nav className="bg-white border-b border-gray-200 px-4 py-3">
      <div className="max-w-4xl mx-auto flex items-center justify-between">
        <Link href={session?.user ? "/checklist" : "/"} className="text-xl font-bold text-blue-600">
          {t("brand")}
        </Link>
        <div className="flex items-center gap-4">
          <LocaleSwitcher />
          {session?.user ? (
            <>
              <span className="text-sm text-gray-600">
                {session.user.email}
              </span>
              <form action={handleSignOut}>
                <button
                  type="submit"
                  className="text-sm text-gray-500 hover:text-gray-700 cursor-pointer"
                >
                  {t("signOut")}
                </button>
              </form>
            </>
          ) : (
            <form action={handleSignIn.bind(null, pathname === "/" ? "/checklist" : pathname)}>
              <button
                type="submit"
                className="bg-blue-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-blue-700 transition-colors cursor-pointer"
              >
                {t("signIn")}
              </button>
            </form>
          )}
        </div>
      </div>
    </nav>
  );
}
