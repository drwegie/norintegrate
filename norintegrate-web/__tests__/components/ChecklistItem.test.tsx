import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi } from "vitest";
import { ChecklistItem } from "@/components/ChecklistItem";
import type { ChecklistStep } from "@/lib/api";

function makeStep(overrides: Partial<ChecklistStep> = {}): ChecklistStep {
  return {
    displayOrder: 1,
    isNextStep: false,
    procedure: {
      id: 10,
      name: "Get D-nummer",
      description: "Apply for a temporary identification number",
      estimatedDays: 14,
      officialUrl: null,
      documents: [],
    },
    ...overrides,
  };
}

describe("ChecklistItem", () => {
  it("renders procedure name and description", () => {
    render(
      <ChecklistItem
        step={makeStep()}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByText("Get D-nummer")).toBeInTheDocument();
    expect(
      screen.getByText("Apply for a temporary identification number")
    ).toBeInTheDocument();
  });

  it("shows 'Next step' badge when isNextStep is true and not completed", () => {
    render(
      <ChecklistItem
        step={makeStep({ isNextStep: true })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByText("Next step")).toBeInTheDocument();
  });

  it("does NOT show 'Next step' badge when completed", () => {
    render(
      <ChecklistItem
        step={makeStep({ isNextStep: true })}
        completed={true}
        canToggle={true}
        onToggle={vi.fn()}
      />
    );
    expect(screen.queryByText("Next step")).not.toBeInTheDocument();
  });

  it("shows a checkbox when canToggle is true", () => {
    render(
      <ChecklistItem
        step={makeStep()}
        completed={false}
        canToggle={true}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByRole("checkbox")).toBeInTheDocument();
  });

  it("shows the display order number when canToggle is false", () => {
    render(
      <ChecklistItem
        step={makeStep({ displayOrder: 3 })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByText("3")).toBeInTheDocument();
    expect(screen.queryByRole("checkbox")).not.toBeInTheDocument();
  });

  it("calls onToggle with the correct arguments when checkbox is clicked", async () => {
    const onToggle = vi.fn();
    const user = userEvent.setup();
    render(
      <ChecklistItem
        step={makeStep()}
        completed={false}
        canToggle={true}
        onToggle={onToggle}
      />
    );
    await user.click(screen.getByRole("checkbox"));
    expect(onToggle).toHaveBeenCalledWith(10, false);
  });

  it("shows 'Official information' link when officialUrl is set", () => {
    render(
      <ChecklistItem
        step={makeStep({
          procedure: {
            id: 10,
            name: "Get D-nummer",
            description: "Apply for a temporary identification number",
            estimatedDays: 14,
            officialUrl: "https://udi.no/d-nummer",
            documents: [],
          },
        })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    const link = screen.getByText("Official information");
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "https://udi.no/d-nummer");
  });

  it("applies line-through styling to completed items", () => {
    render(
      <ChecklistItem
        step={makeStep()}
        completed={true}
        canToggle={true}
        onToggle={vi.fn()}
      />
    );
    const heading = screen.getByText("Get D-nummer");
    expect(heading).toHaveClass("line-through");
  });
});
