import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { VisaTypeCard } from "@/components/VisaTypeCard";

const visaType = {
  id: "SKILLED_WORKER",
  name: "Skilled Worker Visa",
  description: "For qualified professionals with a job offer in Norway",
};

const translatedName = "Faglært arbeidstaker";
const translatedDescription = "Oppholdstillatelse for faglærte";

describe("VisaTypeCard", () => {
  it("renders the translated name and description", () => {
    render(
      <VisaTypeCard
        visaType={visaType}
        name={translatedName}
        description={translatedDescription}
      />
    );
    expect(screen.getByText(translatedName)).toBeInTheDocument();
    expect(screen.getByText(translatedDescription)).toBeInTheDocument();
  });

  it("links to the correct checklist URL", () => {
    render(
      <VisaTypeCard
        visaType={visaType}
        name={translatedName}
        description={translatedDescription}
      />
    );
    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("href", "/checklist/SKILLED_WORKER");
  });
});
