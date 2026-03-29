import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi } from "vitest";
import { ChecklistItem } from "@/components/ChecklistItem";
import type { ChecklistItemResponse } from "@/lib/api";

function makeStep(
  overrides: Partial<ChecklistItemResponse> = {}
): ChecklistItemResponse {
  return {
    procedureId: 10,
    title: "Get D-nummer",
    description: null,
    authority: "Skatteetaten",
    estimatedDays: 14,
    isNext: false,
    documents: [],
    ...overrides,
  };
}

describe("ChecklistItem", () => {
  it("renders procedure title and authority", () => {
    render(
      <ChecklistItem
        step={makeStep()}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByText("Get D-nummer")).toBeInTheDocument();
    expect(screen.getByText("Skatteetaten")).toBeInTheDocument();
  });

  it("shows 'Next step' badge when isNext is true and not completed", () => {
    render(
      <ChecklistItem
        step={makeStep({ isNext: true })}
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
        step={makeStep({ isNext: true })}
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

  it("shows a bullet when canToggle is false", () => {
    render(
      <ChecklistItem
        step={makeStep()}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
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

  it("shows estimated days when set", () => {
    render(
      <ChecklistItem
        step={makeStep({ estimatedDays: 14 })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByText("Estimated: 14 days")).toBeInTheDocument();
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

  it("expands to show description on click", async () => {
    const user = userEvent.setup();
    render(
      <ChecklistItem
        step={makeStep({ description: "Visit Skatteetaten to register." })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.queryByText("Visit Skatteetaten to register.")).not.toBeInTheDocument();
    await user.click(screen.getByText("Get D-nummer"));
    expect(screen.getByText("Visit Skatteetaten to register.")).toBeInTheDocument();
  });

  it("expands to show documents on click", async () => {
    const user = userEvent.setup();
    render(
      <ChecklistItem
        step={makeStep({
          documents: [
            { documentName: "Passport copy", mandatory: true },
            { documentName: "Photo", mandatory: false },
          ],
        })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.queryByText("Passport copy")).not.toBeInTheDocument();
    await user.click(screen.getByText("Get D-nummer"));
    expect(screen.getByText("Passport copy")).toBeInTheDocument();
    expect(screen.getByText("Mandatory")).toBeInTheDocument();
    expect(screen.getByText("Photo")).toBeInTheDocument();
    expect(screen.getByText("Optional")).toBeInTheDocument();
  });

  it("checkbox click does not toggle expand", async () => {
    const onToggle = vi.fn();
    const user = userEvent.setup();
    render(
      <ChecklistItem
        step={makeStep({ description: "Some details here." })}
        completed={false}
        canToggle={true}
        onToggle={onToggle}
      />
    );
    await user.click(screen.getByRole("checkbox"));
    expect(onToggle).toHaveBeenCalledWith(10, false);
    expect(screen.queryByText("Some details here.")).not.toBeInTheDocument();
  });
});
