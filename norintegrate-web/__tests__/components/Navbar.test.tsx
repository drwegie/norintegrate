import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { useSession } from "next-auth/react";
import { usePathname } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { renderWithIntl } from "../test-utils";

const mockHandleSignIn = vi.fn();
const mockHandleSignOut = vi.fn();

vi.mock("next-auth/react", () => ({
  useSession: vi.fn(),
}));

vi.mock("next/navigation", () => ({
  usePathname: vi.fn(),
}));

vi.mock("@/lib/auth-actions", () => ({
  handleSignIn: (...args: unknown[]) => mockHandleSignIn(...args),
  handleSignOut: (...args: unknown[]) => mockHandleSignOut(...args),
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

  it("renders sign-in button inside a form", () => {
    mockUsePathname.mockReturnValue("/");
    mockUseSession.mockReturnValue({
      data: null,
      status: "unauthenticated",
      update: vi.fn(),
    });
    renderWithIntl(<Navbar />);
    const button = screen.getByText("Sign in with Google");
    expect(button.closest("form")).toBeInTheDocument();
  });

  it("renders sign-out button inside a form", () => {
    mockUseSession.mockReturnValue({
      data: {
        user: { email: "user@example.com", name: "Test User", image: null },
        expires: "2099-01-01",
      },
      status: "authenticated",
      update: vi.fn(),
    });
    renderWithIntl(<Navbar />);
    const button = screen.getByText("Sign out");
    expect(button.closest("form")).toBeInTheDocument();
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
