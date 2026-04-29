import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { useSession, signIn, signOut } from "next-auth/react";
import { usePathname } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { renderWithIntl } from "../test-utils";

vi.mock("next-auth/react", () => ({
  useSession: vi.fn(),
  signIn: vi.fn(),
  signOut: vi.fn(),
}));

vi.mock("next/navigation", () => ({
  usePathname: vi.fn(),
}));

vi.mock("@/app/actions/locale", () => ({
  setLocale: vi.fn(),
}));

const mockUseSession = vi.mocked(useSession);
const mockUsePathname = vi.mocked(usePathname);

describe("Navbar", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUsePathname.mockReturnValue("/");
  });

  it("renders the brand link pointing to / when unauthenticated", () => {
    mockUseSession.mockReturnValue({
      data: null,
      status: "unauthenticated",
      update: vi.fn(),
    });
    renderWithIntl(<Navbar />);
    const brandLink = screen.getByText("NorIntegrate");
    expect(brandLink.closest("a")).toHaveAttribute("href", "/");
  });

  it("renders the brand link pointing to /checklist when authenticated", () => {
    mockUseSession.mockReturnValue({
      data: {
        user: { email: "user@example.com", name: "Test User", image: null },
        expires: "2099-01-01",
      },
      status: "authenticated",
      update: vi.fn(),
    });
    renderWithIntl(<Navbar />);
    const brandLink = screen.getByText("NorIntegrate");
    expect(brandLink.closest("a")).toHaveAttribute("href", "/checklist");
  });

  it("shows sign-in button when not authenticated", () => {
    mockUseSession.mockReturnValue({
      data: null,
      status: "unauthenticated",
      update: vi.fn(),
    });
    renderWithIntl(<Navbar />);
    expect(screen.getByText("Sign in with Google")).toBeInTheDocument();
    expect(screen.queryByText("Sign out")).not.toBeInTheDocument();
  });

  it("shows user email and sign-out button when authenticated", () => {
    mockUseSession.mockReturnValue({
      data: {
        user: { email: "user@example.com", name: "Test User", image: null },
        expires: "2099-01-01",
      },
      status: "authenticated",
      update: vi.fn(),
    });
    renderWithIntl(<Navbar />);
    expect(screen.getByText("user@example.com")).toBeInTheDocument();
    expect(screen.getByText("Sign out")).toBeInTheDocument();
    expect(screen.queryByText("Sign in with Google")).not.toBeInTheDocument();
  });

  it("calls signIn with callbackUrl /checklist from the landing page", async () => {
    mockUsePathname.mockReturnValue("/");
    mockUseSession.mockReturnValue({
      data: null,
      status: "unauthenticated",
      update: vi.fn(),
    });
    const user = userEvent.setup();
    renderWithIntl(<Navbar />);
    await user.click(screen.getByText("Sign in with Google"));
    expect(signIn).toHaveBeenCalledWith("google", { callbackUrl: "/checklist" });
  });

  it("calls signIn with current path as callbackUrl from a checklist page", async () => {
    mockUsePathname.mockReturnValue("/checklist/SKILLED_WORKER");
    mockUseSession.mockReturnValue({
      data: null,
      status: "unauthenticated",
      update: vi.fn(),
    });
    const user = userEvent.setup();
    renderWithIntl(<Navbar />);
    await user.click(screen.getByText("Sign in with Google"));
    expect(signIn).toHaveBeenCalledWith("google", { callbackUrl: "/checklist/SKILLED_WORKER" });
  });

  it("calls signOut when sign-out button is clicked", async () => {
    mockUseSession.mockReturnValue({
      data: {
        user: { email: "user@example.com", name: "Test User", image: null },
        expires: "2099-01-01",
      },
      status: "authenticated",
      update: vi.fn(),
    });
    const user = userEvent.setup();
    renderWithIntl(<Navbar />);
    await user.click(screen.getByText("Sign out"));
    expect(signOut).toHaveBeenCalled();
  });

  it("renders the locale switcher", () => {
    mockUseSession.mockReturnValue({
      data: null,
      status: "unauthenticated",
      update: vi.fn(),
    });
    renderWithIntl(<Navbar />);
    expect(screen.getByLabelText("Language")).toBeInTheDocument();
  });
});
