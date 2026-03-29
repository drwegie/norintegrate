import NextAuth from "next-auth";
import Google from "next-auth/providers/google";

declare module "next-auth" {
  interface Session {
    idToken?: string;
  }
}

declare module "@auth/core/jwt" {
  interface JWT {
    idToken?: string;
    refreshToken?: string;
    expiresAt?: number;
  }
}

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    Google({
      clientId: process.env.GOOGLE_CLIENT_ID!,
      clientSecret: process.env.GOOGLE_CLIENT_SECRET!,
      authorization: { params: { access_type: "offline", prompt: "consent" } },
    }),
  ],
  callbacks: {
    async jwt({ token, account }) {
      // On initial sign-in, store tokens and expiry
      if (account) {
        token.idToken = account.id_token ?? undefined;
        token.refreshToken = account.refresh_token ?? undefined;
        token.expiresAt = account.expires_at;
        return token;
      }

      // If the id_token hasn't expired yet, return as-is
      if (token.expiresAt && Date.now() / 1000 < token.expiresAt - 60) {
        return token;
      }

      // Token expired — refresh it using the refresh_token
      if (token.refreshToken) {
        try {
          const response = await fetch("https://oauth2.googleapis.com/token", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({
              client_id: process.env.GOOGLE_CLIENT_ID!,
              client_secret: process.env.GOOGLE_CLIENT_SECRET!,
              grant_type: "refresh_token",
              refresh_token: token.refreshToken,
            }),
          });

          const tokens = await response.json();
          if (!response.ok) throw tokens;

          token.idToken = tokens.id_token;
          token.expiresAt = Math.floor(Date.now() / 1000) + tokens.expires_in;
        } catch {
          // Refresh failed — force re-login
          token.idToken = undefined;
          token.refreshToken = undefined;
          token.expiresAt = undefined;
        }
      }

      return token;
    },
    async session({ session, token }) {
      session.idToken = token.idToken;
      return session;
    },
  },
});
