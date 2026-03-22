import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { VisaTypeCard } from "@/components/VisaTypeCard";

const visaType = {
  id: "SKILLED_WORKER",
  name: "Skilled Worker Visa",
  description: "For qualified professionals with a job offer in Norway",
};

describe("VisaTypeCard", () => {
  it("renders the visa type name and description", () => {
    render(<VisaTypeCard visaType={visaType} />);
    expect(screen.getByText("Skilled Worker Visa")).toBeInTheDocument();
    expect(
      screen.getByText("For qualified professionals with a job offer in Norway")
    ).toBeInTheDocument();
  });

  it("links to the correct checklist URL", () => {
    render(<VisaTypeCard visaType={visaType} />);
    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("href", "/checklist/SKILLED_WORKER");
  });
});
